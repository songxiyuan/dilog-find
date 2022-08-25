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
import java.awt.event.*;
import java.util.Set;

public class logListWindow {

    private JPanel logListWindowContent;
    private JTable logsTable;
    private JTable infoTable;
    private JButton clipboardButton;
    private JButton traceButton;
    private JTextField textTrace;

    DefaultTableModel model;
    Object[] columns = {"time", "tag"};//字段
    Position[] positions;
    ProjectManager projectManager;

    public logListWindow(ToolWindow toolWindow) {
        projectManager = ProjectManager.getInstance();
        model = new DefaultTableModel(columns, 0);
        logsTable.setModel(model);
        clipboardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
//                SystemUtil.getTextFromClipBoard();
                ParsingLogs(new String[]{"aaa.java", "aaa.java"});
            }
        });
        logsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int row = logsTable.getSelectedRow();
                GotoPos(positions[row]);
            }
        });
    }

    public Position ParsingLog(String log) {
        return new Position(log, 3, 4);
    }

    public void ParsingLogs(String[] log) {
        Object[][] data = new Object[log.length][2];
        positions = new Position[log.length];
        for (int i = 0; i < log.length; i++) {
            positions[i] = ParsingLog(log[i]);
            data[i][0] = positions[i].time;
            data[i][1] = positions[i].tag;
        }
        model = new DefaultTableModel(data, columns);
        logsTable.setModel(model);
    }

    public void GotoPos(Position pos) {
        Project[] project = projectManager.getOpenProjects();
        Set<VirtualFile> vfSet = (Set<VirtualFile>) FilenameIndex.getVirtualFilesByName(pos.file, GlobalSearchScope.projectScope(project[0]));
        for (VirtualFile vf : vfSet) {
            String vfPath = vf.getPath();
            if (vfPath.contains(pos.file)) {
                new OpenFileDescriptor(project[0], vf, pos.row, pos.column).navigate(true);
                return;
            }
        }
    }

    public JPanel getContent() {
        return logListWindowContent;
    }
}
