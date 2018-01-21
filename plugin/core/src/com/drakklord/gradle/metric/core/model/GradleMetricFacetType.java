package com.drakklord.gradle.metric.core.model;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class GradleMetricFacetType extends FacetType<GradleMetricFacet, GradleMetricFacetConfiguration> {

    public static final String TYPE_ID = "metric";

    public GradleMetricFacetType() {
        super(GradleMetricFacet.ID, TYPE_ID, "metric");
    }


    @Override
    public GradleMetricFacetConfiguration createDefaultConfiguration() {
        return new GradleMetricFacetConfiguration();
    }

    @Override
    public GradleMetricFacet createFacet(@NotNull Module module,
                                    String name,
                                    @NotNull GradleMetricFacetConfiguration configuration,
                                    @Nullable Facet underlyingFacet) {
        // DO NOT COMMIT MODULE-ROOT MODELS HERE!
        // modules are not initialized yet, so some data may be lost

        return new GradleMetricFacet(module, name, configuration);
    }

    @Override
    public boolean isSuitableModuleType(ModuleType moduleType) {
        return moduleType instanceof JavaModuleType;
    }

    @Override
    public Icon getIcon() {
        return PlatformIcons.ADVICE_ICON;
    }
}
