package com.drakklord.gradle.metric.core.contributor;

import com.intellij.openapi.module.Module;
import org.gradle.tooling.model.idea.IdeaModule;
import org.jdom.Element;
import org.jetbrains.plugins.gradle.service.project.ProjectResolverContext;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;

/**
 * Extension point interface for gradle metric contributors ( other plugins ).
 * Created by DrakkLord on 2015.11.23..
 */
public interface GradleMetricContributor {
    String getName();

    String getDescription();

    Icon getIcon();

    Icon getNamespaceIcon(String namespace);

    Icon getTypeIcon(String type);

    /** Get the description of a problem with the current configuration or return null for no issues. */
    String getConfigurationIssueReport(GradleMetricModelHolder model);

    /** Return the tasks that this contributor wants to run for the specified project.*/
    List<String> getGradleTasksToExecute(GradleMetricModelHolder model);

    /** Called after the gradle tasks complete. */
    List<GradleMetricEntry> onGradleTasksCompleted(GradleMetricUtil u, Module m, boolean wasSuccess) throws GradleMetricContributorException;

    /** Get project resolver model classes. */
    void getProjectResolverModelClasses(HashSet<Class> classList);

    /** Get the model of the contributor for the module. */
    GradleMetricModelHolder getModuleModel(IdeaModule gradleModule, ProjectResolverContext resolverCtx);

    /** Serialize a metric model holder belonging to this contributor into an XML element. */
    void serializeMetricHolderInto(GradleMetricModelHolder holder, Element out);

    /** Serialize a metric model holder belonging to this contributor from an XML element. */
    GradleMetricModelHolder serializeMetricHolderFrom(Element in);
}
