package com.drakklord.gradle.metric.core.contributor;

import junit.framework.TestCase;

/** Created by DrakkLord on 2016. 03. 16.. */
public class GradleMetricContributorExceptionTest extends TestCase {

    public void testExceptionPasstrough() {
        final GradleMetricContributorException e = new GradleMetricContributorException("test_cause");
        assertEquals(e.getMessage(), "test_cause");
    }
}
