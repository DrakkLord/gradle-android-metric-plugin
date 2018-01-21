package com.drakklord.gradle.metric.tooling.pmd;

import org.gradle.api.reporting.SingleFileReport;

import java.io.File;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public interface PMDTaskContainer {
    String getName();
    boolean isIgnoreFailures();
    boolean isEnabled();
    boolean isXmlReportEnabled();
    File getXmlReportTarget();
}
