package com.drakklord.gradle.metric.core.model;

import com.android.tools.idea.gradle.project.sync.setup.Facets;
import com.intellij.facet.ModifiableFacetModel;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class MetricFacetModuleCustomizer {

    @SuppressWarnings("unchecked")
    public void customizeModule(@NotNull Project project,
                                @NotNull Module module,
                                @NotNull IdeModifiableModelsProvider modelsProvider,
                                @Nullable MetricGradleModel androidModel) {
        if (androidModel == null) {
            // this causes the unchecked warning
            Facets.removeAllFacets(modelsProvider.getModifiableFacetModel(module), GradleMetricFacet.ID);
        }
        else {
            GradleMetricFacet facet = Facets.findFacet(module, modelsProvider, GradleMetricFacet.ID);
            if (facet != null) {
                configureFacet(facet, androidModel);
            }
            else {
                // Module does not have Android facet. Create one and add it.
                ModifiableFacetModel model = modelsProvider.getModifiableFacetModel(module);
                GradleMetricFacetType facetType = GradleMetricFacet.getFacetType();
                facet = facetType.createFacet(module, GradleMetricFacet.NAME, facetType.createDefaultConfiguration(), null);
                model.addFacet(facet);
                configureFacet(facet, androidModel);
            }
        }
    }

    private static void configureFacet(@NotNull GradleMetricFacet facet, @NotNull MetricGradleModel androidModel) {
        facet.setMetricModel(androidModel);
    }
}
