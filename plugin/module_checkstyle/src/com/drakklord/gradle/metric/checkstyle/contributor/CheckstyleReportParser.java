package com.drakklord.gradle.metric.checkstyle.contributor;

import com.drakklord.gradle.metric.core.contributor.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.MessageType;
import net.n3.nanoxml.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * Created by DrakkLord on 2015.11.25..
 */
public class CheckstyleReportParser {

    private CheckstyleReportParser() {
    }

    public static void parseModuleReport(GradleMetricContributor thiz, GradleMetricUtil u, Module m, File reportFile,
                                         ArrayList<GradleMetricEntry> r) throws GradleMetricContributorException {

        final IXMLElement root = u.parseXMLFrom(reportFile);
        // check the root and version
        if (!"checkstyle".equalsIgnoreCase(root.getName())) {
            throw new GradleMetricContributorException("not a checkstyle report file : " + reportFile.getAbsolutePath());
        }

        final String reportVersion = root.getAttribute("version", "");
        if (reportVersion.isEmpty()) {
            throw new GradleMetricContributorException("invalid checkstyle report file version : " + reportFile.getAbsolutePath());
        }
        final String[] verSplit = reportVersion.split("\\.");
        if (verSplit == null || verSplit.length < 1) {
            throw new GradleMetricContributorException("invalid checkstyle report file version : " + reportFile.getAbsolutePath());
        }

        final int baseVersion = Integer.valueOf(verSplit[0]);
        if (baseVersion < 5) {
            throw new GradleMetricContributorException("checkstyle report file major version is less than 5 : " + reportFile.getAbsolutePath());
        }

        // parse file blocks
        @SuppressWarnings("unchecked")
        Enumeration<IXMLElement> children = root.enumerateChildren();
        if (children == null) {
            return;
        }

        IXMLElement e;
        while (children.hasMoreElements() && (e = children.nextElement()) != null) {
            if (!"file".equalsIgnoreCase(e.getName())) {
                throw new GradleMetricContributorException("invalid element ["+e.getName()+"] in main block of checkstyle report : " + reportFile.getAbsolutePath());
            }

            final String targetFile = e.getAttribute("name", "");
            if (targetFile.isEmpty()) {
                throw new GradleMetricContributorException("file element with empty name in checkstyle report : " + reportFile.getAbsolutePath());
            }
            final File fileCheck = new File(targetFile);
            if (!fileCheck.exists() || !fileCheck.isFile()) {
                throw new GradleMetricContributorException("file element points to a non-existing file in checkstyle report : " + reportFile.getAbsolutePath());
            }
            if (!u.isFilePartOfTheProject(fileCheck)) {
                throw new GradleMetricContributorException("file element is not part of the project in checkstyle report : " + reportFile.getAbsolutePath());
            }

            // parse error elements
            @SuppressWarnings("unchecked")
            Enumeration<IXMLElement> errors = e.enumerateChildren();
            if (errors == null) {
                continue;
            }

            IXMLElement re;
            while (errors.hasMoreElements() && (re = errors.nextElement()) != null) {
                if (!"error".equalsIgnoreCase(re.getName())) {
                    throw new GradleMetricContributorException("invalid element ["+re.getName()+"] in file block of checkstyle report : " + reportFile.getAbsolutePath());
                }

                final String line = re.getAttribute("line", "");
                if (line.isEmpty()) {
                    throw new GradleMetricContributorException("no line number for error element of checkstyle report : " + reportFile.getAbsolutePath());
                }

                final String severity = re.getAttribute("severity", "");
                if (severity.isEmpty()) {
                    throw new GradleMetricContributorException("no severity for error element of checkstyle report : " + reportFile.getAbsolutePath());
                }

                final String source = re.getAttribute("source", "");
                if (source.isEmpty()) {
                    throw new GradleMetricContributorException("no source for error element of checkstyle report : " + reportFile.getAbsolutePath());
                }
                final String[] sourceSpl = source.split("\\.");
                if (sourceSpl.length < 2) {
                    throw new GradleMetricContributorException("unable to split source value for error element of checkstyle report : " + reportFile.getAbsolutePath());
                }

                int startColumn = -1;
                final String column = re.getAttribute("column", "");
                if (!column.isEmpty()) {
                    startColumn = Integer.valueOf(column);
                }

                r.add(new GradleMetricEntry(thiz, sourceSpl[sourceSpl.length - 2], sourceSpl[sourceSpl.length-1],
                                            targetFile, Integer.valueOf(line), -1,
                                            startColumn, -1, GradleMetricSeverity.valueOf(severity.toUpperCase())));
            }
        }
    }
}
