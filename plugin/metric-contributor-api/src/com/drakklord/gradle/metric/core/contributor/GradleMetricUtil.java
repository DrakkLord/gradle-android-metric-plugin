package com.drakklord.gradle.metric.core.contributor;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;

/**
 * Utility to help contributors with common functions
 * Created by DrakkLord on 2015.11.24..
 */
public interface GradleMetricUtil {

    /** Get the project of the calling context. */
    Project getProject();

    /** Send a simple message to the UI. */
    void addToEventLog(String msg, MessageType type);

    /** Update the current progress fraction. */
    void setCollectorProgressFraction(double pct);

    /** Set the secondary progress message usually the current module name. */
    void setCollectorProgressMessage(String text);

    /** Check to see if the process was cancelled by the user. */
    boolean isCollectorCancelled();

    /** Looks up the module's path to the 'build' directory. */
    String getModuleGradleBuildPath(Module module);

    /** Check whenever the file is reachable from the project. */
    boolean isFilePartOfTheProject(File file);

    /** Initialize an XML parser from a file. */
    IXMLElement parseXMLFrom(File xmlFile) throws GradleMetricContributorException;

    /** List files from a folder recursively. */
    Collection<String> listFilesRecursively(File rootDir, FilenameFilter filter);
}
