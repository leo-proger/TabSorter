package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.EditorsSplitters;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
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
		FileEditorManagerImpl manager = getFileEditorManager(e);
		EditorWindow[] windows = manager.getWindows();

		for (EditorWindow window : windows)
		{
			List<VirtualFile> files = window.getFileList();
			for (VirtualFile file : files)
			{
				if (file.equals(getClickedFile(e)))
				{
					return window;
				}
			}
		}
		return null;
	}

	protected List<VirtualFile> getOpenFiles(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) return null;

		FileEditorManagerImpl manager = getFileEditorManager(e);
		if (manager == null) return null;

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

	protected FileEditorManagerImpl getFileEditorManager(AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) return null;

		return (FileEditorManagerImpl) FileEditorManager.getInstance(project);
	}

}
