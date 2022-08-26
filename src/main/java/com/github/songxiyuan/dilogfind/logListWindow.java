// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.github.songxiyuan.dilogfind;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class logListWindow {

    private JPanel logListWindowContent;
    private JTable logsTable;
    private JButton clipboardButton;
    private JButton traceButton;
    private JTextField textTrace;
    private JLabel infoLabel;
    private JList logInfoList;

    DefaultTableModel model;
    Object[] columns = {"time", "tag"};//字段
    Vector<LogParser> logParsers;
    ProjectManager projectManager;

    public logListWindow(ToolWindow toolWindow) {
        projectManager = ProjectManager.getInstance();
        model = new DefaultTableModel(columns, 0);
        logsTable.setModel(model);
        clipboardButton.addActionListener(this::clipboardButtonAction);
        logsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = logsTable.getSelectedRow();
                LogParser logParser = logParsers.elementAt(row);
                GotoPos(logParser);
                DefaultListModel listModel = new DefaultListModel();
                listModel.addAll(List.of(logParser.params));
                logInfoList.setModel(listModel);
                super.mouseClicked(e);
            }
        });
    }

    public void clipboardButtonAction(ActionEvent actionEvent) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable content = clipboard.getContents(null);//从系统剪切板中获取数据
        String text = null;
        if (!content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return;
        }
        try {
            text = (String) content.getTransferData(DataFlavor.stringFlavor);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ParsingLogs(text.split("\n"));
    }

    public void ParsingLogs(String[] logs) {
        logParsers = new Vector<>();
        for (String log : logs) {
            LogParser logParser = new LogParser(log);
            if (logParser.file != null) {
                logParsers.add(logParser);
            }
        }
        Object[][] data = new Object[logParsers.size()][2];
        for (int i = 0; i < logParsers.size(); i++) {
            data[i][0] = logParsers.elementAt(i).time;
            data[i][1] = logParsers.elementAt(i).tag;
        }
        model = new DefaultTableModel(data, columns);
        logsTable.setModel(model);
    }

    public void GotoPos(LogParser pos) {
        Project[] project = projectManager.getOpenProjects();
        Set<VirtualFile> vfSet = (Set<VirtualFile>) FilenameIndex.getVirtualFilesByName(pos.file, GlobalSearchScope.projectScope(project[0]));
        for (VirtualFile vf : vfSet) {
            String vfPath = vf.getPath();
            if (!vfPath.contains(pos.file)) {
                continue;
            }
            new OpenFileDescriptor(project[0], vf, pos.row, 0).navigate(true);
            infoLabel.setText("find source success:" + pos.file);
            return;

        }
        infoLabel.setText("find file failed:" + pos.file);
    }

    public JPanel getContent() {
        return logListWindowContent;
    }
}
