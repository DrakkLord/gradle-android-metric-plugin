package com.drakklord.gradle.metric.core.model;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializer;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * Created by DrakkLord on 2016. 10. 02..
 */
public class GradleMetricFacetConfiguration implements FacetConfiguration, PersistentStateComponent<Element> {

    private MetricGradleModel myModel;

    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext editorContext, FacetValidatorsManager validatorsManager) {
        return new FacetEditorTab[0];
    }

    @Deprecated
    @Override
    public void readExternal(Element element) throws InvalidDataException {
    }

    @Deprecated
    @Override
    public void writeExternal(Element element) throws WriteExternalException {
    }

    public MetricGradleModel getModel() {
        return myModel;
    }

    public void setModel(MetricGradleModel myModel) {
        this.myModel = myModel;
    }

    @Nullable
    @Override
    public Element getState() {
        final Element out = new Element("configuration");
        if (myModel != null) {
            myModel.toXMLElement(out);
        }
        return out;
    }

    @Override
    public void loadState(Element input) {
        myModel = new MetricGradleModel(input);
    }
}
