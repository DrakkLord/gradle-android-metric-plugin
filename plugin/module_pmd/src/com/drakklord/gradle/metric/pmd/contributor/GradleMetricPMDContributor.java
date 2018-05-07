package com.drakklord.gradle.metric.pmd.contributor;

import com.drakklord.gradle.metric.core.contributor.*;
import com.drakklord.gradle.metric.tooling.pmd.PMDMetricModel;
import com.drakklord.gradle.metric.tooling.pmd.PMDMetricModelImpl;
import com.drakklord.gradle.metric.tooling.pmd.PMDTaskContainer;
import com.drakklord.gradle.metric.tooling.pmd.PMDTaskContainerImpl;
import com.intellij.openapi.module.Module;
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
public class GradleMetricPMDContributor implements GradleMetricContributor {

    @Override
    public String getName() {
        return "PMD";
    }

    @Override
    public String getDescription() {
        return "PMD metric processor";
    }

    @Override
    public Icon getIcon() {
        return Icons.PMD_REPORT_ICON;
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
        if (!(model instanceof PMDTasksHolder)) {
            return null;
        }
        final PMDTasksHolder ctx = (PMDTasksHolder) model;

        if (ctx.getTasks() == null || ctx.getTasks().isEmpty()) {
            return null;
        }

        for (PMDTaskContainer t : ctx.getTasks().values()) {
            if (t.isEnabled()) {
                if (!t.isXmlReportEnabled()) {
                    return "one of the tasks have pmd.report.xml is set to false, it should be set to true in order to get PMD reports";
                } else if (!t.isIgnoreFailures()) {
                    return "one of the tasks have pmd.ignoreFailures is set to false, it should be set to true in order to get PMD reports";
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getGradleTasksToExecute(GradleMetricModelHolder model) {
        final ArrayList<String> list = new ArrayList<String>();
        if (!(model instanceof PMDTasksHolder)) {
            return Collections.unmodifiableList(list);
        }
        final PMDTasksHolder ctx = (PMDTasksHolder) model;

        if (ctx.getTasks() == null || ctx.getTasks().isEmpty()) {
            return Collections.unmodifiableList(list);
        }

        for (PMDTaskContainer t : ctx.getTasks().values()) {
            if (t.isEnabled() && t.isXmlReportEnabled()) {
                list.add(t.getName());
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<GradleMetricEntry> onGradleTasksCompleted(GradleMetricUtil u, Module module, boolean wasSuccess) throws GradleMetricContributorException {
        if (!wasSuccess || module == null ) {
            throw new GradleMetricContributorException("invalid input parameters");
        }

        final ArrayList<GradleMetricEntry> results = new ArrayList<GradleMetricEntry>();
//        int i = 0;

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
        final File reportFolder = new File(buildPath + File.separatorChar + "reports" + File.separatorChar + "pmd");
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

        for (String s : xmlList) {
            if (u.isCollectorCancelled()) {
                return null;
            }

            final File reportFile = new File(reportFolder.getAbsolutePath() + File.separatorChar + s);
            if (!reportFile.exists() || !reportFile.isFile()) {
                throw new GradleMetricContributorException("failed to find report file >> " + reportFile.getAbsolutePath());
            }
            PMDReportParser.parseModuleReport(this, u, module, reportFile, results);
        }
        return results;
    }

    @Override
    public void getProjectResolverModelClasses(HashSet<Class> classList) {
        classList.add(PMDMetricModel.class);
    }

    @Override
    public GradleMetricModelHolder getModuleModel(IdeaModule gradleModule, ProjectResolverContext resolverCtx) {
        PMDMetricModel pmd = resolverCtx.getExtraProject(gradleModule, PMDMetricModel.class);
        if (pmd == null || pmd.getTasks() == null || pmd.getTasks().isEmpty()) {
            return null;
        }
        return new PMDTasksHolder(pmd.getTasks());
    }

    @Override
    public void serializeMetricHolderInto(GradleMetricModelHolder holder, Element out) {
        if (!(holder instanceof PMDTasksHolder)) {
            throw new IllegalArgumentException("unable to serialize metric holder of different type");
        }
        final PMDTasksHolder localHolder = (PMDTasksHolder) holder;

        final Map<String, PMDTaskContainer> tasks = localHolder.getTasks();
        for (Map.Entry<String, PMDTaskContainer> taskInfo : tasks.entrySet()) {
            final PMDTaskContainer t = taskInfo.getValue();

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
        final ArrayList<PMDTaskContainer> tasks = new ArrayList<PMDTaskContainer>();
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

            tasks.add(new PMDTaskContainerImpl(taskNameInner, taskIgnoreFailures, taskEnabled, taskXmlReportEnabled));
        }
        return new PMDTasksHolder(tasks);
    }
}
