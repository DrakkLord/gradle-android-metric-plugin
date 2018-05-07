package com.drakklord.gradle.metric.checkstyle.contributor;

import com.drakklord.gradle.metric.core.contributor.*;
import com.drakklord.gradle.metric.tooling.checkstyle.CheckstyleMetricModel;
import com.drakklord.gradle.metric.tooling.checkstyle.CheckstyleMetricModelImpl;
import com.drakklord.gradle.metric.tooling.checkstyle.CheckstyleTaskContainer;
import com.drakklord.gradle.metric.tooling.checkstyle.CheckstyleTaskContainerImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.gradle.tooling.model.idea.IdeaModule;
import org.jdom.Attribute;
import org.jdom.Element;
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

    @Override
    public void serializeMetricHolderInto(GradleMetricModelHolder holder, Element out) {
        if (!(holder instanceof CheckstyleTasksHolder)) {
            throw new IllegalArgumentException("unable to serialize metric holder of different type");
        }
        final CheckstyleTasksHolder localHolder = (CheckstyleTasksHolder) holder;

        final Map<String, CheckstyleTaskContainer> tasks = localHolder.getTasks();
        for (Map.Entry<String, CheckstyleTaskContainer> taskInfo : tasks.entrySet()) {
            final CheckstyleTaskContainer t = taskInfo.getValue();

            final Element taskHolder = new Element(taskInfo.getKey());
            taskHolder.setAttribute(new Attribute("name", t.getName()));
            taskHolder.setAttribute(new Attribute("ignoreFailures", Boolean.toString(t.isIgnoreFailures())));
            taskHolder.setAttribute(new Attribute("enabled", Boolean.toString(t.isEnabled())));
            taskHolder.setAttribute(new Attribute("xmlReportEnabled", Boolean.toString(t.isXmlReportEnabled())));
            out.addContent(taskHolder);
        }
    }

    @Override
    public GradleMetricModelHolder serializeMetricHolderFrom(Element in) {
        final ArrayList<CheckstyleTaskContainer> tasks = new ArrayList<CheckstyleTaskContainer>();
        for (Element e : in.getChildren()) {
            final String taskName = e.getName();
            if (taskName == null) {
                continue;
            }

            final String taskNameInner = e.getAttributeValue("name");
            if (!taskName.equalsIgnoreCase(taskNameInner)) {
                continue;
            }

            final boolean taskIgnoreFailures = Boolean.parseBoolean(e.getAttributeValue("ignoreFailures"));
            final boolean taskEnabled = Boolean.parseBoolean(e.getAttributeValue("enabled"));
            final boolean taskXmlReportEnabled = Boolean.parseBoolean(e.getAttributeValue("xmlReportEnabled"));

            tasks.add(new CheckstyleTaskContainerImpl(taskNameInner, taskIgnoreFailures, taskEnabled, taskXmlReportEnabled));
        }
        return new CheckstyleTasksHolder(tasks);
    }
}
