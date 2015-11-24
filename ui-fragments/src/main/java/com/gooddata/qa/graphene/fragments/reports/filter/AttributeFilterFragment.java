package com.gooddata.qa.graphene.fragments.reports.filter;

import static com.gooddata.qa.graphene.utils.CheckUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;

import java.util.Collection;
import java.util.Optional;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.filter.AttributeFilterItem;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.FloatingTime;
import com.gooddata.qa.graphene.entity.filter.FloatingTime.Time;
import com.gooddata.qa.graphene.fragments.common.SelectItemPopupPanel;

public class AttributeFilterFragment extends AbstractFilterFragment {

    private static final By ERROR_MESSAGE_LOCATOR = By.cssSelector(".message-error");
    private static final By FLOATING_MESSAGE = By.cssSelector(".c-disableOverlayPlugin:not(.hidden) .message");
    private static final By FLOATING_RANGE_PANEL_LOCATOR = By.cssSelector(".floatingFilterContainer");

    @FindBy(className = "listContainer")
    private AttributeValueSelectorPanel valuesPanel;

    @FindBy(className = "s-btn-select_attribute")
    private WebElement selectButton;

    @Override
    public void addFilter(FilterItem filterItem) {
        AttributeFilterItem attributeFilterItem = (AttributeFilterItem) filterItem;

        searchAndSelectAttribute(attributeFilterItem.getAttribute())
                .searchAndSelectAttributeValues(attributeFilterItem.getValues())
                .apply();
        waitForFragmentNotVisible(this);
    }

    public AttributeFilterFragment deselectAllValues() {
        waitForFragmentVisible(valuesPanel).deselectAllValues();
        return this;
    }

    public AttributeFilterFragment selectAllValues() {
        waitForFragmentVisible(valuesPanel).selectAllValues();
        return this;
    }

    public boolean areAllValuesSelected() {
        return waitForFragmentVisible(valuesPanel).areAllValuesSelected();
    }

    public String getErrorMessage() {
        return waitForElementVisible(ERROR_MESSAGE_LOCATOR, browser).getText();
    }

    public AttributeFilterFragment selectFloatingTime(FloatingTime from, FloatingTime to) {
        openFloatingRangePanel().selectRange(from, to);
        return this;
    }

    public AttributeFilterFragment selectFloatingTime(Time time) {
        openFloatingRangePanel().selectTime(time);
        return this;
    }

    public AttributeFilterFragment searchAndSelectAttribute(String attribute) {
        if(!isElementPresent(SelectItemPopupPanel.LOCATOR, browser)){
            waitForElementVisible(selectButton).click();
        }

        searchAndSelectItem(attribute);
        return this;
    }

    public AttributeFilterFragment searchAndSelectAttributeValues(Collection<String> values) {
        waitForFragmentVisible(valuesPanel).searchAndSelectItems(values);
        return this;
    }

    private FloatingRangePanel openFloatingRangePanel() {
        Optional.of(waitForElementVisible(FLOATING_RANGE_PANEL_LOCATOR, browser))
                .filter(e -> !e.getAttribute("class").contains("disabled"))
                .orElse(waitForElementVisible(FLOATING_MESSAGE, browser))
                .click();

        return Graphene.createPageFragment(FloatingRangePanel.class,
                waitForElementVisible(FLOATING_RANGE_PANEL_LOCATOR, browser));
    }
}
