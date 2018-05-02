package com.gooddata.qa.graphene.fragments.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.utils.CssUtils.simplifyText;

import org.openqa.selenium.WebElement;

import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;

public class DatePresetsSelect extends AbstractReactDropDown {
    @Override
    protected String getDropdownCssSelector() {
        return ".overlay.dropdown-body";
    }

    @Override
    public boolean isDropdownOpen() {
        return waitForElementEnabled(getDropdownButton()).getAttribute("class").contains("is-dropdown-open");
    }

    @Override
    protected String getSearchInputCssSelector() {
        return null;
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not([class*='item-header'])";
    }
    
    @Override
    protected WebElement getElementByName(final String name) {
        String selector = ".s-filter-" + simplifyText(name);
        return getElement(selector);
    }
}
