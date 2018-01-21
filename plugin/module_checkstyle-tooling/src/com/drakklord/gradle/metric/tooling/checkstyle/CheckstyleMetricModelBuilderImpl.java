package com.drakklord.gradle.metric.tooling.checkstyle;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.quality.Checkstyle;
import org.jetbrains.plugins.gradle.tooling.ErrorMessageBuilder;
import org.jetbrains.plugins.gradle.tooling.ModelBuilderService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Model builder implementation for
 * Created by DrakkLord on 2016. 11. 01..
 */
public class CheckstyleMetricModelBuilderImpl implements ModelBuilderService {

    @Override
    public boolean canBuild(String modelName) {
        return CheckstyleMetricModel.class.getName().equals(modelName);
    }

    @Override
    public Object buildAll(String s, Project project) {
        final ArrayList<CheckstyleTaskContainer> tasks = new ArrayList<CheckstyleTaskContainer>();
        if (project.getPlugins().hasPlugin(Constants.GRADLE_PLUGIN_NAME)) {
            SortedMap<String, Task> taskList = project.getTasks().getAsMap();
            for (Task t : taskList.values()) {
                if (t instanceof Checkstyle) {
                    tasks.add(CheckstyleTaskContainerImpl.createFrom((Checkstyle) t));
                }
            }
        }
        if (tasks.isEmpty()) {
            return null;
        }
        return new CheckstyleMetricModelImpl(tasks);
    }

    @Override
    public ErrorMessageBuilder getErrorMessageBuilder(Project project, Exception e) {
        return ErrorMessageBuilder.create(project, e, "Android gradle Checkstyle resolve errors")
                                        .withDescription("Unable to resolve additional Checkstyle tasks from build");
    }
}

