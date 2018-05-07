package com.drakklord.gradle.metric.tooling.pmd;

import java.io.File;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public interface PMDTaskContainer {
    String getName();
    boolean isIgnoreFailures();
    boolean isEnabled();
    boolean isXmlReportEnabled();
}
