package com.gooddata.qa.graphene.fragments.modeler;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

public class Modeler extends AbstractFragment {
    private static final String MODELER = "gdc-modeler";

    @FindBy(className = "gdc-ldm-sidebar")
    private Sidebar sidebar;

    @FindBy(className = "gdc-ldm-component-container")
    private ComponentContainer componentContainer;

    @FindBy(className = "gdc-ldm-layout")
    private Layout layout;

    public static Modeler getInstance(SearchContext searchContext) {
        return Graphene.createPageFragment(
                Modeler.class, waitForElementVisible(className(MODELER), searchContext));
    }

    public Sidebar getSidebar() {
        return sidebar;
    }

    public ComponentContainer getComponentContainer() {
        return componentContainer;
    }

    public Layout getLayout() {
        return layout;
    }
}