package com.drakklord.gradle.metric.tooling.pmd;

import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.PmdReports;
import org.gradle.api.reporting.SingleFileReport;

import java.io.File;
import java.io.Serializable;

/**
 * Created by DrakkLord on 2016. 11. 01..
 */
public class PMDTaskContainerImpl implements Serializable, PMDTaskContainer {

    private final String name;
    private final boolean ignoreFailures;
    private final boolean enabled;
    private final boolean xmlReportEnabled;
    private final File xmlReportTarget;

    public PMDTaskContainerImpl(String name, boolean ignoreFailures, boolean enabled,
                                 boolean xmlReportEnabled, File xmlReportTarget) {
        this.name = name;
        this.ignoreFailures = ignoreFailures;
        this.enabled = enabled;
        this.xmlReportEnabled = xmlReportEnabled;
        this.xmlReportTarget = xmlReportTarget;
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

    @Override
    public File getXmlReportTarget() {
        return xmlReportTarget;
    }

    static PMDTaskContainer createFrom(Pmd p) {
        final PmdReports r = p.getReports();

        return new PMDTaskContainerImpl(p.getName(), p.getIgnoreFailures(), p.getEnabled(),
                                        r.getXml().isEnabled(), r.getXml().getDestination());
    }

    public static PMDTaskContainer copy(PMDTaskContainer o) {
        return new PMDTaskContainerImpl(o.getName(), o.isIgnoreFailures(), o.isEnabled(),
                                        o.isXmlReportEnabled(), o.getXmlReportTarget());
    }
}
