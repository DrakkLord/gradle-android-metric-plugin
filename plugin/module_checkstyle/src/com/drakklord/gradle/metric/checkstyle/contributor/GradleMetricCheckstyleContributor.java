package com.drakklord.gradle.metric.checkstyle.contributor;

import com.drakklord.gradle.metric.core.contributor.*;
import com.drakklord.gradle.metric.tooling.checkstyle.CheckstyleMetricModel;
import com.drakklord.gradle.metric.tooling.checkstyle.CheckstyleTaskContainer;
import com.intellij.openapi.module.Module;
import org.gradle.tooling.model.idea.IdeaModule;
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext;

import javax.swing.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

/**
 * Extension point interface for gradle metric contributors ( other plugins ).
 * Created by DrakkLord on 2015.11.23..
 */
public class GradleMetricCheckstyleContributor implements GradleMetricContributor {

    @Override
    public String getName() {
        return "Checkstyle";
    }

    @Override
    public String getDescription() {
        return "Checkstyle metric processor";
    }

    @Override
    public Icon getIcon() {
        return Icons.CHECKSTYLE_REPORT_ICON;
    }

    @Override
    public Icon getNamespaceIcon(String namespace) {
        return null;
    }

    @Override
    public Icon getTypeIcon(String type) {
        return null;
    }

    @Override
    public String getConfigurationIssueReport(GradleMetricModelHolder model) {
        final ArrayList<String> list = new ArrayList<String>();
        if (!(model instanceof CheckstyleTasksHolder)) {
            return null;
        }
        final CheckstyleTasksHolder ctx = (CheckstyleTasksHolder) model;

        if (ctx.getTasks() == null || ctx.getTasks().isEmpty()) {
            return null;
        }

        for (CheckstyleTaskContainer t : ctx.getTasks().values()) {
            if (t.isEnabled()) {
                if (!t.isXmlReportEnabled()) {
                    return "one of the tasks have checkstyle.report.xml is set to false, it should be set to true in order to get checkstyle reports";
                } else if (!t.isIgnoreFailures()) {
                    return "one of the tasks have checkstyle.ignoreFailures is set to false, it should be set to true in order to get checkstyle reports";
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getGradleTasksToExecute(GradleMetricModelHolder model) {
        final ArrayList<String> list = new ArrayList<String>();
        if (!(model instanceof CheckstyleTasksHolder)) {
            return Collections.unmodifiableList(list);
        }
        final CheckstyleTasksHolder ctx = (CheckstyleTasksHolder) model;

        if (ctx.getTasks() == null || ctx.getTasks().isEmpty()) {
            return Collections.unmodifiableList(list);
        }

        for (CheckstyleTaskContainer t : ctx.getTasks().values()) {
            if (t.isEnabled() && t.isXmlReportEnabled()) {
                list.add(t.getName());
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<GradleMetricEntry> onGradleTasksCompleted(GradleMetricUtil u, Module module, boolean wasSuccess) throws GradleMetricContributorException {
        if (!wasSuccess || module == null) {
            throw new GradleMetricContributorException("invalid input parameters");
        }



        if (u.isCollectorCancelled()) {
            return null;
        }
/*
        u.setCollectorProgressFraction((double) i / (double) (modules.length-1));
        u.setCollectorProgressMessage(m.getName());
        i++;
*/
        final String buildPath = u.getModuleGradleBuildPath(module);
        if (buildPath == null) {
            throw new GradleMetricContributorException("failed to get module's build path");
        }

        // excluded projects may not even make the folder or projects that do not have the checkstyle task
        final File reportFolder = new File(buildPath + File.separatorChar + "reports" + File.separatorChar + "checkstyle");
        if (!reportFolder.exists() || !reportFolder.isDirectory()) {
            return null;
        }

        Collection<String> xmlList = u.listFilesRecursively(reportFolder, new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        if (xmlList == null || xmlList.isEmpty()) {
            return null;
        }

        final ArrayList<GradleMetricEntry> results = new ArrayList<GradleMetricEntry>();
        for (String s : xmlList) {
            if (u.isCollectorCancelled()) {
                return null;
            }
/*
            TODO progress inidicator is borken in counter, it should be done in the core plugin
            TODO if a specific module is selected only parse results for that module
            TODO show specific module results differently so user knows it
*/
            final File reportFile = new File(reportFolder.getAbsolutePath() + File.separatorChar + s);
            if (!reportFile.exists() || !reportFile.isFile()) {
                throw new GradleMetricContributorException("failed to find report file >> " + reportFile.getAbsolutePath());
            }
            CheckstyleReportParser.parseModuleReport(this, u, module, reportFile, results);
        }

        return results;
    }

    @Override
    public void getProjectResolverModelClasses(HashSet<Class> classList) {
        classList.add(CheckstyleMetricModel.class);
    }

    @Override
    public GradleMetricModelHolder getModuleModel(IdeaModule gradleModule, ProjectResolverContext resolverCtx) {
        CheckstyleMetricModel chk = resolverCtx.getExtraProject(gradleModule, CheckstyleMetricModel.class);
        if (chk == null || chk.getTasks() == null || chk.getTasks().isEmpty()) {
            return null;
        }
        return new CheckstyleTasksHolder(chk.getTasks());
    }
}
