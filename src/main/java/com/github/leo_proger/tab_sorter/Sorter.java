package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.fileEditor.impl.EditorWindow;
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;


abstract public class Sorter extends AnAction {

	protected void reorderTabs(EditorWindow window, List<VirtualFile> files) {
		List<VirtualFile> currentFiles = window.getFileList();
		for (VirtualFile file : currentFiles)
		{
			window.closeFile(file, false, false);
		}

		for (VirtualFile file : files)
		{
			window.getManager().openFileImpl2(window, file, true);
		}
	}

	protected EditorWindow findWindowContainingFile(FileEditorManagerImpl manager, VirtualFile clickedFile) {
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
