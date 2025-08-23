package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collections;
import java.util.List;


abstract public class Sorter extends AnAction {

	protected void reorderTabs(Project project, EditorWindow window, List<VirtualFile> sortedFiles) {
		EditorsSplitters splitters = window.getOwner();
		splitters.setCurrentWindow$intellij_platform_ide_impl(window);

		List<VirtualFile> filesToCloseInCurrentWindow = window.getFileList();
		Collections.reverse(filesToCloseInCurrentWindow);

		for (VirtualFile file : filesToCloseInCurrentWindow)
		{
			window.closeFile(file, false, false);
		}

		FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		for (VirtualFile file : sortedFiles)
		{
			fileEditorManager.openFile(file, true);
		}
	}

	protected EditorWindow findWindowContainingFile(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) return null;

		EditorWindow window = e.getData(EditorWindow.DATA_KEY);
		if (window != null) return window; // if action was invoked by clicking on tab

		FileEditorManagerEx fem = FileEditorManagerEx.getInstanceEx(project);

		EditorsSplitters splitters = fem.getSplitters();
		return splitters.getCurrentWindow(); // if action was NOT invoked by clicking on tab (from another location)
	}

	protected List<VirtualFile> getOpenFiles(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) return null;

		VirtualFile clickedFile = getClickedFile(e);
		if (clickedFile == null) return null;

		EditorWindow targetWindow = findWindowContainingFile(e);
		if (targetWindow == null) return null;

		List<VirtualFile> openFiles = targetWindow.getFileList();
		if (openFiles.size() < 2) return null;

		return openFiles;
	}

	protected VirtualFile getClickedFile(AnActionEvent e) {
		return e.getData(CommonDataKeys.VIRTUAL_FILE);
	}

}
