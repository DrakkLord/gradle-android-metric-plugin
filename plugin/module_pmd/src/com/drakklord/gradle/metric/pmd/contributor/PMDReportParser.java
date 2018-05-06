package com.drakklord.gradle.metric.pmd.contributor;

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
public class PMDReportParser {

    private PMDReportParser() {
    }

    public static void parseModuleReport(GradleMetricContributor thiz, GradleMetricUtil u, Module m, File reportFile,
                                         ArrayList<GradleMetricEntry> r) throws GradleMetricContributorException {

        final IXMLElement root = u.parseXMLFrom(reportFile);
        // check the root and version
        if (!"pmd".equalsIgnoreCase(root.getName())) {
            throw new GradleMetricContributorException("not a PMD report file : " + reportFile.getAbsolutePath());
        }

        final String reportVersion = root.getAttribute("version", "");
        if (reportVersion.isEmpty()) {
            throw new GradleMetricContributorException("invalid PMD report file version : " + reportFile.getAbsolutePath());
        }
        final String[] verSplit = reportVersion.split("\\.");
        if (verSplit.length < 1) {
            throw new GradleMetricContributorException("invalid PMD report file version : " + reportFile.getAbsolutePath());
        }

        final int baseVersion = Integer.valueOf(verSplit[0]);
        if (baseVersion < 5) {
            throw new GradleMetricContributorException("PMD report file major version is less than 5 : " + reportFile.getAbsolutePath());
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
                throw new GradleMetricContributorException("invalid element ["+e.getName()+"] in main block of PMD report : " + reportFile.getAbsolutePath());
            }

            final String targetFile = e.getAttribute("name", "");
            if (targetFile.isEmpty()) {
                throw new GradleMetricContributorException("file element with empty name in PMD report : " + reportFile.getAbsolutePath());
            }
            final File fileCheck = new File(targetFile);
            if (!fileCheck.exists() || !fileCheck.isFile()) {
                throw new GradleMetricContributorException("file element points to a non-existing file in PMD report : " + reportFile.getAbsolutePath());
            }
            if (!u.isFilePartOfTheProject(fileCheck)) {
                throw new GradleMetricContributorException("file element is not part of the project in PMD report : " + reportFile.getAbsolutePath());
            }

            // parse error elements
            @SuppressWarnings("unchecked")
            Enumeration<IXMLElement> errors = e.enumerateChildren();
            if (errors == null) {
                continue;
            }

            IXMLElement re;
            while (errors.hasMoreElements() && (re = errors.nextElement()) != null) {
                if (!"violation".equalsIgnoreCase(re.getName())) {
                    throw new GradleMetricContributorException("invalid element ["+re.getName()+"] in file block of PMD report : " + reportFile.getAbsolutePath());
                }

                final String bline = re.getAttribute("beginline", "");
                if (bline.isEmpty()) {
                    throw new GradleMetricContributorException("no begin line number for violation element of PMD report : " + reportFile.getAbsolutePath());
                }
                final String eline = re.getAttribute("endline", "");
                if (eline.isEmpty()) {
                    throw new GradleMetricContributorException("no end line number for violation element of PMD report : " + reportFile.getAbsolutePath());
                }

                final String bcol = re.getAttribute("begincolumn", "");
                if (bcol.isEmpty()) {
                    throw new GradleMetricContributorException("no begin column number for violation element of PMD report : " + reportFile.getAbsolutePath());
                }
                final String ecol = re.getAttribute("endcolumn", "");
                if (ecol.isEmpty()) {
                    throw new GradleMetricContributorException("no end column number for violation element of PMD report : " + reportFile.getAbsolutePath());
                }

                final String severity = re.getAttribute("priority", "");
                if (severity.isEmpty()) {
                    throw new GradleMetricContributorException("no priority for violation element of PMD report : " + reportFile.getAbsolutePath());
                }
                int severityNum = Integer.valueOf(severity);
                if (severityNum < 0 || severityNum > 3) {
                    severityNum = 1;
                }
                severityNum--;

                final String ruleset = re.getAttribute("ruleset", "");
                if (ruleset.isEmpty()) {
                    throw new GradleMetricContributorException("no ruleset for violation element of PMD report : " + reportFile.getAbsolutePath());
                }
                final String rule = re.getAttribute("rule", "");
                if (rule.isEmpty()) {
                    throw new GradleMetricContributorException("no rule for violation element of PMD report : " + reportFile.getAbsolutePath());
                }

                r.add(new GradleMetricEntry(thiz, ruleset, rule, targetFile,
                                Integer.valueOf(bline), Integer.valueOf(eline),
                                Integer.valueOf(bcol), Integer.valueOf(eline),
                                GradleMetricSeverity.valueOf(severityNum)));
            }
        }
    }
}
