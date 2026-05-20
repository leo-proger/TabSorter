package com.github.leo_proger.tab_sorter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.ui.FileColorManager;
import com.intellij.ui.tabs.FileColorManagerImpl;

public class SortTabsByScope extends Sorter {
    private static final Logger log = Logger.getInstance(SortTabsByScope.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        List<VirtualFile> openFiles = getOpenFiles(e);
        Project project = e.getProject();

        if (openFiles == null || project == null) {
            return;
        }

        List<VirtualFile> sortedFiles = sort(project, openFiles);

        reorderTabs(project, findWindowContainingFile(e), sortedFiles);
    }

    private List<VirtualFile> sort(Project project, List<VirtualFile> files) {
        FileColorManager manager = FileColorManager.getInstance(project);

        log.warn("FileColorManager implementation class: " + manager.getClass().getName());

        for (Method method : manager.getClass().getDeclaredMethods()) {
            log.warn(" - " + method.toString());
        }

        var configs = getFileColorConfigs(manager);
        List<String> scopeOrder = getFileColorOrder(configs);

        files.sort(
            Comparator
                .comparingInt((VirtualFile file) -> {
                    String scope = findMatchingScopeName(project, file, configs);

                    int fileIndex = scopeOrder.indexOf(scope);

                    log.warn("Filepath: " + file.getPath());
                    log.warn("  Scope: " + scope);
                    log.warn("  Index: " + fileIndex);

                    return fileIndex >= 0 ? fileIndex : Integer.MAX_VALUE;
                })
                .thenComparing((VirtualFile file) ->
                    file.getParent() != null
                        ? file.getParent().getPath()
                        : "",
                    String.CASE_INSENSITIVE_ORDER
                )
                .thenComparing(VirtualFile::getName, String.CASE_INSENSITIVE_ORDER));

        log.warn("// Final files sorted in File Color order");

        for (VirtualFile file : files) {
            log.warn(file.getPath());
        }

        return files;
    }

    private List<?> getFileColorConfigs(FileColorManager manager) {
        try {
            Method getModelMethod = FileColorManagerImpl.class.getDeclaredMethod("getModel");
            getModelMethod.setAccessible(true);

            Object model = getModelMethod.invoke(manager);

            Method method = model.getClass().getDeclaredMethod("getLocalConfigurations");
            method.setAccessible(true);

            List<?> configs = (List<?>) method.invoke(model);

            return configs;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            log.warn("// Failed to read FileColorManager configurations", ex);
        }

        return Collections.emptyList();
    }

    private List<String> getFileColorOrder(List<?> configs) {
        List<String> scopeOrder = new ArrayList<>();

        log.warn("// File Color Order");

        try {
            for (Object cfg : configs) {
                Method getScopeName = cfg.getClass().getDeclaredMethod("getScopeName");

                getScopeName.setAccessible(true);

                String scopeName = (String) getScopeName.invoke(cfg);

                scopeOrder.add(scopeName);

                log.warn(" - " + scopeName);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            log.warn("// Failed to read FileColorManager color order", ex);
        }

        return scopeOrder;
    }

    /**
     * The following won't work, since it returns default scopes: `com.intellij.packageDependencies.DependencyValidationManager`;
     * Therefore, we're trying to get the File Color scope order via reflection.
     *
     * Though, the following is public, too I see: `com.intellij.ui.FileColorManager.getFileColor`
     */
    private String findMatchingScopeName(Project project, VirtualFile file, List<?> configs) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);

        if (psiFile == null) {
            return null;
        }

        NamedScopeManager scopeManager = NamedScopeManager.getInstance(project);

        for (Object cfg : configs) {
            try {
                Method getScopeName = cfg.getClass().getDeclaredMethod("getScopeName");
                getScopeName.setAccessible(true);

                String scopeName = (String) getScopeName.invoke(cfg);
                NamedScope scope = scopeManager.getScope(scopeName);

                if (scope == null)
                    continue;

                PackageSet set = scope.getValue();

                if (set != null && set.contains(psiFile, scopeManager)) {
                    return scopeName;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}