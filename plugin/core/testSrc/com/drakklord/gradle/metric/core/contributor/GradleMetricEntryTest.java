package com.drakklord.gradle.metric.core.contributor;

import com.drakklord.gradle.metric.core.contributor.mock.GradleMetricMockContributor;
import junit.framework.TestCase;

/** Created by DrakkLord on 2016. 03. 16.. */
public class GradleMetricEntryTest extends TestCase {

    public void testConsturctor() {
        final GradleMetricEntry e = new GradleMetricEntry(GradleMetricMockContributor.MOCK_CONTRIBUTOR,
                "test_namespace", "test_type", "file_name",
                10, 15, 5, 10, GradleMetricSeverity.ERROR);

        checkEntry(e);

        final GradleMetricEntry e2 = new GradleMetricEntry(e);
        checkEntry(e);
    }

    private void checkEntry(GradleMetricEntry e) {
        assertEquals(e.source, GradleMetricMockContributor.MOCK_CONTRIBUTOR);
        assertEquals(e.metricNamespace, "test_namespace");
        assertEquals(e.metricType, "test_type");
        assertEquals(e.fileName, "file_name");
        assertEquals(e.lineStart, 10);
        assertEquals(e.lineEnd,15);
        assertEquals(e.columnStart, 5);
        assertEquals(e.columnEnd, 10);
        assertEquals(e.severity, GradleMetricSeverity.ERROR);
    }
}
