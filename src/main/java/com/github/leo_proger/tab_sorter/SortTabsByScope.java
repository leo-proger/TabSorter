package com.github.leo_proger.tab_sorter;


import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.FileColorManager;
import com.intellij.ui.tabs.FileColorManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SortTabsByScope extends Sorter {

	private static final Logger log = Logger.getInstance(SortTabsByScope.class);

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		List<VirtualFile> openFiles = getOpenFiles(e);
		Project project = e.getProject();

		if (openFiles == null || project == null)
		{
			return;
		}

		List<VirtualFile> sortedFiles = sort(project, openFiles);

		reorderTabs(project, findWindowContainingFile(e), sortedFiles);
	}

	private List<VirtualFile> sort(Project project, List<VirtualFile> files) {
		FileColorManager manager = FileColorManager.getInstance(project);
		if (manager == null)
		{
			return files;
		}

		Map<Color, Integer> colorOrder = buildColorOrder(manager);

		// Resolve each file's color index once — the comparator runs O(n log n)
		// times, and getFileColor can trigger PSI work.
		Map<VirtualFile, Integer> fileIndex = new HashMap<>();
		for (VirtualFile file : files)
		{
			Color color = manager.getFileColor(file);
			int idx = color != null ? colorOrder.getOrDefault(color, Integer.MAX_VALUE) : Integer.MAX_VALUE;
			fileIndex.put(file, idx);
			log.debug("File: " + file.getPath() + " color=" + color + " index=" + idx);
		}

		files.sort(
				Comparator
						.comparingInt((VirtualFile file) -> fileIndex.get(file))
						.thenComparing(
								(VirtualFile file) -> file.getParent() != null ? file.getParent().getPath() : "",
								String.CASE_INSENSITIVE_ORDER
						)
						.thenComparing(VirtualFile::getName, String.CASE_INSENSITIVE_ORDER));

		if (log.isDebugEnabled())
		{
			log.debug("Final files sorted in File Color order:");
			for (VirtualFile file : files)
			{
				log.debug("  " + file.getPath());
			}
		}

		return files;
	}

	/**
	 Color -> first index across local and shared File Colors configurations,
	 preserving the order configured in Settings | Appearance | File Colors.
	 <p>
	 The IntelliJ Platform does not expose the configured scope list publicly,
	 so we read it via reflection. Color/scope resolution itself goes through
	 the public API (getScopeColor, getFileColor).
	 */
	private Map<Color, Integer> buildColorOrder(FileColorManager manager) {
		if (!(manager instanceof FileColorManagerImpl))
		{
			log.debug("FileColorManager is not FileColorManagerImpl: " + manager.getClass().getName());
			return Map.of();
		}

		Object model = getModel((FileColorManagerImpl) manager);
		if (model == null)
		{
			return Map.of();
		}

		List<String> scopeNames = new ArrayList<>();
		scopeNames.addAll(getScopeNames(model, "getLocalConfigurations"));
		scopeNames.addAll(getScopeNames(model, "getSharedConfigurations"));

		Map<Color, Integer> colorOrder = new HashMap<>();
		int index = 0;
		for (String scopeName : scopeNames)
		{
			Color color = manager.getScopeColor(scopeName);
			if (color == null)
			{
				continue;
			}
			if (colorOrder.putIfAbsent(color, index) == null)
			{
				index++;
			}
		}
		return colorOrder;
	}

	private Object getModel(FileColorManagerImpl manager) {
		try
		{
			Method getModel = FileColorManagerImpl.class.getDeclaredMethod("getModel");
			getModel.setAccessible(true);
			return getModel.invoke(manager);
		} catch (ReflectiveOperationException ex)
		{
			log.warn("Failed to get FileColorManager model", ex);
			return null;
		}
	}

	private List<String> getScopeNames(Object model, String methodName) {
		try
		{
			Method getConfigs = model.getClass().getDeclaredMethod(methodName);
			getConfigs.setAccessible(true);
			List<?> configs = (List<?>) getConfigs.invoke(model);
			if (configs == null || configs.isEmpty())
			{
				return List.of();
			}

			Method getScopeName = configs.get(0).getClass().getDeclaredMethod("getScopeName");
			getScopeName.setAccessible(true);

			List<String> result = new ArrayList<>(configs.size());
			for (Object cfg : configs)
			{
				String name = (String) getScopeName.invoke(cfg);
				if (name != null)
				{
					result.add(name);
				}
			}
			return result;
		} catch (ReflectiveOperationException ex)
		{
			log.warn("Failed to read FileColorManager " + methodName, ex);
			return List.of();
		}
	}

}
