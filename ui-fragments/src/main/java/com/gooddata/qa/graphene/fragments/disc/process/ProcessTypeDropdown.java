package com.gooddata.qa.graphene.fragments.disc.process;

import com.gooddata.qa.graphene.fragments.common.AbstractDropDown;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.openqa.selenium.By;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;

public class ProcessTypeDropdown extends AbstractDropDown {

    @Override
    protected String getDropdownCssSelector() {
        return ".process-selection-dropdown";
    }

    @Override
    protected String getDropdownButtonCssSelector() {
        return "button";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-view-item";
    }

    @Override
    protected void waitForPickerLoaded() {
        // Picker is loaded instantly and no need to wait more
    }

    public ProcessTypeDropdown expand() {
        if (isCollapsed()) {
            this.getRoot().click();
        }
        return this;
    }

    public ProcessTypeDropdown selectProcessType(String processType) {
        By selector = By.cssSelector(".s-" + simplifyText(processType));
        ElementUtils.scrollElementIntoView(By.cssSelector(".ember-view.ember-list-view"), browser, 50);
        waitForElementVisible(selector, browser).click();
        return this;
    }

    private boolean isCollapsed() {
        return !isElementVisible(By.cssSelector(getDropdownCssSelector()), browser);
    }

}
