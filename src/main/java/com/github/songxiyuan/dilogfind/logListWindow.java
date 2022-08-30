// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.songxiyuan.dilogfind;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

public class logListWindow {

    private JPanel logListWindowContent;
    private JTable logsTable;
    private JButton clipboardButton;
    private JLabel infoLabel;
    private JList logInfoList;
    private JButton down;
    private JButton up;

    DefaultTableModel model;
    Object[] columns = {"time", "tag"};//字段
    Vector<LogParser> logParsers;
    ProjectManager projectManager;

    public logListWindow(ToolWindow toolWindow) {
        projectManager = ProjectManager.getInstance();
        model = new DefaultTableModel(columns, 0);
        logsTable.setModel(model);
        clipboardButton.addActionListener(this::clipboardButtonAction);
        logsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent lse) {
                GotoLineSource();
            }
        });
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int row = logsTable.getSelectedRow();
                row--;
                if (row < 0) {
                    row = logParsers.size() - 1;
                }
                logsTable.setRowSelectionInterval(row, row);
            }
        });
        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int row = logsTable.getSelectedRow();
                row++;
                if (row >= logParsers.size()) {
                    row = 0;
                }
                logsTable.setRowSelectionInterval(row, row);
            }
        });
        logInfoList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    String value = logInfoList.getSelectedValue().toString();
                    int index = value.indexOf("=");
                    if (index >= 0) {
                        value = value.substring(index + 1);
                    }
                    clipboard.setContents(new StringSelection(value), null);
                    infoLabel.setText("value copied");
                }
            }
        });
        logsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                GotoLineSource();
            }
        });
    }

    public void GotoLineSource() {
        int row = logsTable.getSelectedRow();
        LogParser logParser = logParsers.elementAt(row);
        GotoPos(logParser);
        DefaultListModel listModel = new DefaultListModel();
        for (int i = 0; i < logParser.params.length; i++) {
            if (!logParser.params[i].endsWith("=")) {//过滤没有参数的字段
                listModel.addElement(logParser.params[i]);
            }
        }
        logInfoList.setModel(listModel);
    }

    public void clipboardButtonAction(ActionEvent actionEvent) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(null);//从系统剪切板中获取数据
        String text = null;
        if (!content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            infoLabel.setText("clipboard not string");
            return;
        }
        try {
            text = (String) content.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (Objects.equals(text, "")) {
            infoLabel.setText("clipboard is null");
            return;
        }
        Object[][] data = ParsingLogs(text.split("\n"));
        if (data.length == 0) {
            infoLabel.setText("no correct log in clipboard");
            return;
        }
        model = new DefaultTableModel(data, columns);
        logsTable.setModel(model);
        GotoPos(logParsers.elementAt(0));
        logsTable.setRowSelectionInterval(0, 0);
        DefaultTableCellRenderer tcr = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                cell.setForeground(JBColor.RED);
                if (Objects.equals(logParsers.elementAt(row).level, "ERROR") || Objects.equals(logParsers.elementAt(row).level, "WARN")) {
                    cell.setForeground(JBColor.RED);
                } else {
                    cell.setForeground(Color.getColor("BBBBBB"));
                }
                return cell;
            }
        };
        logsTable.getColumn("tag").setCellRenderer(tcr);
    }

    public Object[][] ParsingLogs(String[] logs) {
        logParsers = new Vector<>();
        for (String log : logs) {
            LogParser logParser = new LogParser(log);
            if (logParser.fileName != null) {
                logParsers.add(logParser);
            }
        }
        Object[][] data = new Object[logParsers.size()][2];
        for (int i = 0; i < logParsers.size(); i++) {
            data[i][0] = logParsers.elementAt(i).time;
            data[i][1] = logParsers.elementAt(i).tag;
        }
        return data;
    }

    public void GotoPos(LogParser pos) {
        Project[] project = projectManager.getOpenProjects();
        if (project.length == 0) {
            infoLabel.setText("no project");
            return;
        }
        for (int i = 0; i < project.length; i++) {
            Set<VirtualFile> vfSet = (Set<VirtualFile>) FilenameIndex.getVirtualFilesByName(pos.fileName, GlobalSearchScope.projectScope(project[i]));
            for (VirtualFile vf : vfSet) {
                String vfPath = vf.getPath();
                for (int j = 0; j < pos.paths.length; j++) {
                    String findPath = StringUtils.join(pos.paths, "/", j, pos.paths.length);
                    if (vfPath.endsWith(findPath)) {
                        new OpenFileDescriptor(project[i], vf, pos.row, 0).navigate(true);
                        infoLabel.setText("find source: " + pos.fileName);
                        return;
                    }
                }
            }
        }
        infoLabel.setText("find failed: " + pos.fileName);
    }

    public JPanel getContent() {
        return logListWindowContent;
    }
}
