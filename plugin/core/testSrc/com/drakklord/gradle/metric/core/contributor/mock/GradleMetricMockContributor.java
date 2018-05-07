package com.drakklord.gradle.metric.core.contributor.mock;

import com.drakklord.gradle.metric.core.contributor.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.Module;
import org.gradle.tooling.model.idea.IdeaModule;
import org.jdom.Element;
import org.jetbrains.plugins.gradle.model.ExternalProject;
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext;

import javax.swing.*;
import java.util.*;

/**
 * Mocked contributor implementation that returns a known set of values.
 * Created by DrakkLord on 2016. 03. 16..
 */
public class GradleMetricMockContributor implements GradleMetricContributor {

    public static final GradleMetricMockContributor MOCK_CONTRIBUTOR = new GradleMetricMockContributor();

    @Override
    public String getName() {
        return "mock name";
    }

    @Override
    public String getDescription() {
        return "mock description";
    }

    @Override
    public Icon getIcon() {
        return AllIcons.Ide.Error;
    }

    @Override
    public Icon getNamespaceIcon(String namespace) {
        return AllIcons.Ide.HectorOn;
    }

    @Override
    public Icon getTypeIcon(String type) {
        return AllIcons.Ide.IncomingChangesOn;
    }

    @Override
    public String getConfigurationIssueReport(GradleMetricModelHolder model) {
        return null;
    }

    @Override
    public List<String> getGradleTasksToExecute(GradleMetricModelHolder model) {
        final ArrayList<String> list = new ArrayList<String>();
        list.add("test_task");
        return Collections.unmodifiableList(list);
    }

    @Override
    public List<GradleMetricEntry> onGradleTasksCompleted(GradleMetricUtil u, Module module, boolean wasSuccess) {
        return Collections.singletonList(new GradleMetricEntry(this, "test_namespace", "test_type", "file_path",
                                                               11, 15, 0, 7, GradleMetricSeverity.ERROR));
    }

    @Override
    public void getProjectResolverModelClasses(HashSet<Class> classList) {
        // TODO
    }

    @Override
    public GradleMetricModelHolder getModuleModel(IdeaModule gradleModule, ProjectResolverContext resolverCtx) {
        // TODO
        return null;
    }

    @Override
    public void serializeMetricHolderInto(GradleMetricModelHolder holder, Element out) {
        // TODO
    }

    @Override
    public GradleMetricModelHolder serializeMetricHolderFrom(Element in) {
        // TODO
        return null;
    }
}
