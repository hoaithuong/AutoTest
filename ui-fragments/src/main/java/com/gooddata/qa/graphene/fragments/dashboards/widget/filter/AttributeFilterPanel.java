package com.gooddata.qa.graphene.fragments.dashboards.widget.filter;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;

public class AttributeFilterPanel extends SelectItemPopupPanel {

    @FindBy(css = "div.yui3-c-simpleColumn-underlay .yui3-widget:not(.gdc-hidden)")
    private List<WebElement> listAttrValues;

    public static final By LOCATOR = By.className("yui3-listfilterpanel");

    private static final By SHOW_ALL_BUTTON_LOCATOR = By.className("s-btn-show_all");

    public static final AttributeFilterPanel getInstance(SearchContext searchContext) {
        return getInstance(AttributeFilterPanel.class, LOCATOR, searchContext);
    }

    public AttributeFilterPanel showAllAttributes() {
        waitForElementVisible(SHOW_ALL_BUTTON_LOCATOR, getRoot()).click();
        return this;
    }

    public void changeValues(String... values) {
        boolean singleMode = isOnSingleMode();
        boolean groupMode = isOnGroupMode();
        boolean editMode = isEditMode();

        if (!singleMode) clearAllItems();
        searchAndSelectItems(values);

        if (!singleMode) {
            if (!groupMode || editMode) {
                submitPanel();
            }
        }
    }

    public boolean isOnSingleMode() {
        return !getRoot().getAttribute("class").contains("multiple");
    }

    @Override
    public List<WebElement> getItemElements() {
        return listAttrValues;
    }

    private boolean isEditMode() {
        return DashboardEditWidgetToolbarPanel.isVisible(browser) &&
                !isElementVisible(By.cssSelector(".infoThisIsJustPreview"), this.getRoot());
    }

    private boolean isOnGroupMode() {
        return getRoot().getAttribute("class").contains("inFilterGroup");
    }
}
