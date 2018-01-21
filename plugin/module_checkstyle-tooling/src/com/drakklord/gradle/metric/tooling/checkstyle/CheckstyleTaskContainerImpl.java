package com.drakklord.gradle.metric.tooling.checkstyle;

import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleReports;

import java.io.File;
import java.io.Serializable;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public class CheckstyleTaskContainerImpl implements Serializable, CheckstyleTaskContainer {

    private final String name;
    private final boolean ignoreFailures;
    private final boolean enabled;
    private final boolean xmlReportEnabled;

    private final File xmlTarget;

    private CheckstyleTaskContainerImpl(String name, boolean ignoreFailures, boolean enabled,
                                 boolean xmlReportEnabled, File xmlTarget) {
        this.name = name;
        this.ignoreFailures = ignoreFailures;
        this.enabled = enabled;
        this.xmlReportEnabled = xmlReportEnabled;
        this.xmlTarget = xmlTarget;
    }

    public String getName() {
        return name;
    }

    public boolean isIgnoreFailures() {
        return ignoreFailures;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isXmlReportEnabled() {
        return xmlReportEnabled;
    }

    public File getXmlReportTarget() {
        return xmlTarget;
    }

    static CheckstyleTaskContainer createFrom(Checkstyle p) {
        final CheckstyleReports r = p.getReports();

        return new CheckstyleTaskContainerImpl(p.getName(), p.getIgnoreFailures(),
                                               p.getEnabled(), r.getXml().isEnabled(),
                                               r.getXml().getDestination());
    }

    public static CheckstyleTaskContainer copy(CheckstyleTaskContainer o) {
        return new CheckstyleTaskContainerImpl(o.getName(), o.isIgnoreFailures(), o.isEnabled(),
                                               o.isXmlReportEnabled(), o.getXmlReportTarget());
    }
}
