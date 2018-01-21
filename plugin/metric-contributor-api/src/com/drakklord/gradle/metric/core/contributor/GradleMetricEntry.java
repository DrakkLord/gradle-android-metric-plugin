package com.drakklord.gradle.metric.core.contributor;

/**
 * Entry that contains the issue reported by a contributor.
 * Created by DrakkLord on 2015.11.23..
 */
public class GradleMetricEntry {

    public final GradleMetricContributor source;
    public final String metricNamespace;
    public final String metricType;

    public final String fileName;
    public final int lineStart;
    public final int lineEnd;
    public final int columnStart;
    public final int columnEnd;
    public final GradleMetricSeverity severity;

    public GradleMetricEntry(GradleMetricContributor _source,
                             String _metricNamespace, String _metricType, String _fileName,
                             int _lineStart, int _lineEnd, int _columnStart, int _columnEnd,
                             GradleMetricSeverity _severity) {

        source = _source;
        metricNamespace = _metricNamespace;
        metricType = _metricType;
        fileName = _fileName;
        lineStart = _lineStart;
        lineEnd = _lineEnd;
        columnStart = _columnStart;
        columnEnd = _columnEnd;
        severity = _severity;
    }

    public GradleMetricEntry(GradleMetricEntry e) {
        source = e.source;
        metricNamespace = e.metricNamespace;
        metricType = e.metricType;
        fileName = e.fileName;
        lineStart = e.lineStart;
        lineEnd = e.lineEnd;
        columnStart = e.columnStart;
        columnEnd = e.columnEnd;
        severity = e.severity;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof  GradleMetricEntry) {
            final GradleMetricEntry other = (GradleMetricEntry) o;
            if (source == null || metricNamespace == null || metricType == null || fileName == null) {
                return super.equals(o);
            }
            // TODO checkstyle plugin may report an issue with the same paramters except the message
            return source.equals(other.source) &&
                    metricNamespace.equals(other.metricNamespace) &&
                    metricType.equals(other.metricType) &&
                    fileName.equals(other.fileName) &&
                    lineStart == other.lineStart &&
                    lineEnd == other.lineEnd &&
                    columnStart == other.columnStart &&
                    columnEnd == other.columnEnd &&
                    severity == other.severity;
        }
        return super.equals(o);
    }
}
