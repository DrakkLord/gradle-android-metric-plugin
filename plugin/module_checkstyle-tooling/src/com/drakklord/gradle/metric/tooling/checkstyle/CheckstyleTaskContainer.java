package com.drakklord.gradle.metric.tooling.checkstyle;

import java.io.File;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public interface CheckstyleTaskContainer {
    String getName();
    boolean isIgnoreFailures();
    boolean isEnabled();
    boolean isXmlReportEnabled();
    File getXmlReportTarget();
}
