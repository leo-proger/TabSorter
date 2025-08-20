package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;


public class SortTabsByAlphabet extends Sorter {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		List<VirtualFile> openFiles = getOpenFiles(e);
		if (openFiles == null) return;

		openFiles.sort(Comparator.comparing(VirtualFile::getName, String.CASE_INSENSITIVE_ORDER));

		reorderTabs(findWindowContainingFile(e), openFiles);
	}

}
