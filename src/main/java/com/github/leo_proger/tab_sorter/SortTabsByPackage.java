package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class SortTabsByPackage extends Sorter {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		List<VirtualFile> openFiles = getOpenFiles(e);
		Project project = getProject(e);
		if (openFiles == null || project == null) return;

		List<VirtualFile> sortedFiles = sort(project, openFiles);

		reorderTabs(findWindowContainingFile(e), sortedFiles);
	}

	private List<VirtualFile> sort(Project project, List<VirtualFile> files) {
		List<VirtualFile> projectFiles = new ArrayList<>();
		List<VirtualFile> externalFiles = new ArrayList<>();

		for (VirtualFile file : files)
		{
			String projectPath = project.getBasePath();
			if (projectPath != null)
			{
				if (file.getPath().contains(projectPath))
				{
					projectFiles.add(file);
				} else
				{
					externalFiles.add(file);
				}
			}
		}
		projectFiles.sort(Comparator
				                  .<VirtualFile, String>comparing(
						                  file -> file.getParent().getPath(),
						                  String.CASE_INSENSITIVE_ORDER
				                  )
				                  .thenComparing(VirtualFile::getName));
		externalFiles.sort(Comparator
				                   .<VirtualFile, String>comparing(
						                   file -> file.getParent().getPath(),
						                   String.CASE_INSENSITIVE_ORDER
				                   )
				                   .thenComparing(VirtualFile::getName));

		projectFiles.addAll(externalFiles);

		return projectFiles;
	}

}
