package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;


abstract public class Sorter extends AnAction {

	protected void reorderTabs(EditorWindow window, List<VirtualFile> sortedFiles) {
		List<VirtualFile> currentFiles = window.getFileList();
		for (VirtualFile file : currentFiles)
		{
			window.closeFile(file, false, false);
		}

		for (VirtualFile file : sortedFiles)
		{
			window.getManager().openFileImpl2(window, file, true);
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
		Project project = getProject(e);
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

	protected Project getProject(AnActionEvent e) {
		return e.getProject();
	}

	protected FileEditorManagerImpl getFileEditorManager(AnActionEvent e) {
		return (FileEditorManagerImpl) FileEditorManager.getInstance(getProject(e));
	}

}
