package com.drakklord.gradle.metric.core.contributor;

import junit.framework.TestCase;

/** Created by DrakkLord on 2016. 03. 16.. */
public class GradleMetricSeverityTest extends TestCase {

    public void testValueOfValid() {
        assertEquals(GradleMetricSeverity.valueOf(GradleMetricSeverity.ERROR.ordinal()), GradleMetricSeverity.ERROR);
        assertEquals(GradleMetricSeverity.valueOf(GradleMetricSeverity.WARNING.ordinal()), GradleMetricSeverity.WARNING);
        assertEquals(GradleMetricSeverity.valueOf(GradleMetricSeverity.SUGGESTION.ordinal()), GradleMetricSeverity.SUGGESTION);
    }

    public void testValueOfInvalid() {
        try {
            GradleMetricSeverity.valueOf(-1);
            fail("GradleMetricSeverity valuOf passed with invalid value" );
        } catch (IllegalStateException expectedException) {
            // empty on purpose
        }
    }
}
