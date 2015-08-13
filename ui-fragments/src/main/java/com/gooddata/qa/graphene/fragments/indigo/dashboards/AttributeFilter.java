package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class AttributeFilter extends ReactDropdownParent {

    @FindBy(className = "button-title")
    private WebElement buttonTitle;

    @FindBy(className = "button-subtitle")
    private WebElement buttonText;

    @Override
    public String getDropdownCssSelector() {
        return ".overlay .attributevalues-list";
    }

    public AttributeFilter selectByName(String name, boolean cleanSelection) {
        ensureDropdownOpen();

        if (cleanSelection) {
            clearAllCheckedValues();
        }

        String nameSimplified = simplifyText(name);
        // in case there is a search field, use it
        if (this.hasSearchField()) {
            this.searchForText(name);
        }

        By selectedItem = cssSelector(getDropdownCssSelector() + " .s-" + nameSimplified);
        waitForElementVisible(selectedItem, browser).click();

        By applyButton = cssSelector("button.s-apply_button");
        waitForElementVisible(applyButton, browser).click();

        return this;
    }

    @Override
    protected AttributeFilter toggleDropdown() {
        waitForElementVisible(getRoot()).click();
        return this;
    }

    public void clearAllCheckedValues() {
        waitForElementVisible(cssSelector("button.s-clear"), browser).click();
    }

    public String getTitle() {
        waitForElementVisible(buttonTitle);
        return buttonTitle.getText();
    }

    public String getSelection() {
        waitForElementVisible(buttonText);
        return buttonText.getText();
    }

    public String getSelectedItems() {
        return waitForElementVisible(buttonText.findElement(className("button-selected-items"))).getText();
    }

    public String getSelectedItemsCount() {
        return waitForElementVisible(buttonText.findElement(className("button-selected-items-count"))).getText();
    }
}
