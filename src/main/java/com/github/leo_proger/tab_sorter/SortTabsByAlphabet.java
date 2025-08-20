package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;


public class SortTabsByAlphabet extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project project = e.getProject();
		if (project == null) return;

		VirtualFile clickedFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
		if (clickedFile == null) return;

		FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
		if (!(fileEditorManager instanceof FileEditorManagerImpl manager)) return;


		EditorWindow targetWindow = findWindowContainingFile(manager, clickedFile);
		if (targetWindow == null) return;

		List<VirtualFile> files = targetWindow.getFileList();
		if (files.size() < 2) return;

		files.sort(Comparator.comparing(VirtualFile::getName, String.CASE_INSENSITIVE_ORDER));

		reorderTabs(targetWindow, files);
	}

	private void reorderTabs(EditorWindow window, List<VirtualFile> sortedFiles) {
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

	private EditorWindow findWindowContainingFile(FileEditorManagerImpl manager, VirtualFile clickedFile) {
		EditorWindow[] windows = manager.getWindows();

		for (EditorWindow window : windows)
		{
			List<VirtualFile> files = window.getFileList();
			for (VirtualFile file : files)
			{
				if (file.equals(clickedFile))
				{
					return window;
				}
			}
		}
		return null;
	}

}
