package com.gooddata.qa.graphene.fragments.disc.process;

import com.gooddata.qa.graphene.fragments.common.AbstractDropDown;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;

public class DatasourceProviderDropDown extends AbstractDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".datasource-provider-selection-dropdown";
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return "datasource-provider-dropdown-button";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-view-item";
    }

    @Override
    protected void waitForPickerLoaded() {
        // Picker is loaded instantly and no need to wait more
    }

    public DatasourceProviderDropDown expand() {
        if (isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public DatasourceProviderDropDown collapse() {
        if (!isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public DatasourceProviderDropDown selectDatasource(String dataSourceTitle) {
        By selector = By.cssSelector(".s-" + simplifyText(dataSourceTitle));
        waitForElementVisible(selector, browser).click();
        return this;
    }

    private boolean isCollapsed() {
        return !isElementVisible(By.cssSelector(getDropdownCssSelector()), browser);
    }
}
