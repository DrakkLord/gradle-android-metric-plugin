package com.drakklord.gradle.metric.core.model;

import com.intellij.facet.*;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class GradleMetricFacet extends Facet<GradleMetricFacetConfiguration> {
    public static final FacetTypeId<GradleMetricFacet> ID = new FacetTypeId<GradleMetricFacet>("gradle-metric");
    public static final String NAME = "GradleMetric";

    public GradleMetricFacet(@NotNull Module module, String name, @NotNull GradleMetricFacetConfiguration configuration) {
        super(getFacetType(), module, name, configuration, null);
    }

    @NotNull
    public static GradleMetricFacetType getFacetType() {
        return (GradleMetricFacetType) FacetTypeRegistry.getInstance().findFacetType(ID);
    }

    public GradleMetricFacet(@NotNull FacetType facetType, @NotNull Module module, @NotNull String name, @NotNull GradleMetricFacetConfiguration configuration, Facet underlyingFacet) {
        super(facetType, module, name, configuration, underlyingFacet);
    }

    public void setMetricModel(MetricGradleModel mm) {
        getConfiguration().setModel(mm);
    }

    public MetricGradleModel getMetricModel() {
        return getConfiguration().getModel();
    }

    @Nullable
    public static GradleMetricFacet getInstance(@NotNull Module module) {
        return FacetManager.getInstance(module).getFacetByType(ID);
    }
}
