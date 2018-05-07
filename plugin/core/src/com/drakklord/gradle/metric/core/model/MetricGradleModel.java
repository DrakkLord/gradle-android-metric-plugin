package com.drakklord.gradle.metric.core.model;

import com.drakklord.gradle.metric.core.Extensions;
import com.drakklord.gradle.metric.core.contributor.GradleMetricContributor;
import com.drakklord.gradle.metric.core.contributor.GradleMetricModelHolder;
import com.intellij.openapi.module.Module;
import com.intellij.util.containers.HashMap;
import com.intellij.util.xmlb.annotations.MapAnnotation;
import org.jdom.Attribute;
import org.jdom.Content;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class MetricGradleModel {
    private HashMap<GradleMetricContributor, GradleMetricModelHolder> modelMapping = new HashMap<GradleMetricContributor, GradleMetricModelHolder>();
    private String mModuleName;
    private String mGradlePath;

    public MetricGradleModel(Element in) {
        fromXMLElement(in);
    }

    public MetricGradleModel(String moduleName, String gradlePath, HashMap<GradleMetricContributor, GradleMetricModelHolder> modelMapping) {
        this.modelMapping.putAll(modelMapping);
        this.mModuleName = moduleName;
        this.mGradlePath = gradlePath;
    }

    public String getModuleName() {
        return mModuleName;
    }

    public boolean hasModels() {
        return !modelMapping.isEmpty();
    }

    public GradleMetricModelHolder getModelFor(GradleMetricContributor c) {
        return modelMapping.get(c);
    }

    public String getGradePath() {
        return mGradlePath;
    }

    @Nullable
    public static MetricGradleModel get(@NotNull Module module) {
        GradleMetricFacet facet = GradleMetricFacet.getInstance(module);
        return facet != null ? get(facet) : null;
    }

    @Nullable
    public static MetricGradleModel get(@NotNull GradleMetricFacet androidFacet) {
        return androidFacet.getMetricModel();
    }

    private void fromXMLElement(Element in) {
        // mModuleName -- attribute
        mModuleName = null;
        final Attribute attr = in.getAttribute("mModuleName");
        if (attr != null) {
            mModuleName = attr.getValue();
        }
        if (mModuleName == null) {
            mModuleName = "";
        }

        // mGradlePath -- attribute
        mGradlePath = null;
        final Attribute attr2 = in.getAttribute("mGradlePath");
        if (attr2 != null) {
            mGradlePath = attr2.getValue();
        }
        if (mGradlePath == null) {
            mGradlePath = "";
        }

        // modelMapping -- simplified map
        // root node
        modelMapping.clear();
        Element taskMappingRoot = null;
        for (Element e :  in.getChildren()) {
            if ("taskMapping".equals(e.getName())) {
                taskMappingRoot = e;
                break;
            }
        }
        if (taskMappingRoot == null) {
            return;
        }

        // map children nodes
        final List<Element> children = taskMappingRoot.getChildren();
        for (Element e : children) {
            final String contributorName = e.getName();
            if (contributorName == null) {
                continue;
            }

            // find matching contributor name
            GradleMetricContributor targetCont = null;
            for (GradleMetricContributor gc : Extensions.EP_NAME.getExtensions()) {
                if (contributorName.equals(gc.getName())) {
                    targetCont = gc;
                    break;
                }
            }
            if (targetCont == null) {
                continue;
            }

            final GradleMetricModelHolder holder = targetCont.serializeMetricHolderFrom(e);
            if (holder != null) {
                modelMapping.put(targetCont, holder);
            }
        }
    }

    void toXMLElement(Element out) {
        out.setAttribute(new Attribute("mModuleName", mModuleName,null));
        out.setAttribute(new Attribute("mGradlePath", mGradlePath,null));

        // modelMapping -- simplified map
        final ArrayList<Content> contentHolder = new ArrayList<Content>();
        final Element mapHolder = new Element("taskMapping");
        for (Map.Entry<GradleMetricContributor, GradleMetricModelHolder> e : modelMapping.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            if (e.getKey().getName() == null) {
                continue;
            }

            final Element mapKey = new Element(e.getKey().getName());
            e.getKey().serializeMetricHolderInto(e.getValue(), mapKey);
            if (!mapKey.getChildren().isEmpty()) {
                contentHolder.add(mapKey);
            }
        }
        if (!contentHolder.isEmpty()) {
            mapHolder.setContent(contentHolder);
        }
        out.addContent(mapHolder);
    }
}
