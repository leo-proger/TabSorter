package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;


public class SortTabsByExtension extends Sorter {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		List<VirtualFile> openFiles = getOpenFiles(e);
		if (openFiles == null) return;

		openFiles.sort(getExtensionAndNameComparator());

		reorderTabs(findWindowContainingFile(e), openFiles);
	}

	private Comparator<VirtualFile> getExtensionAndNameComparator() {
		return Comparator
				       .comparing(this::getFileExtension, String.CASE_INSENSITIVE_ORDER)
				       .thenComparing(VirtualFile::getName, String.CASE_INSENSITIVE_ORDER);
	}

	private String getFileExtension(VirtualFile file) {
		String extension = file.getExtension();
		return extension != null ? extension : "zzz_no_extension";
	}

}
