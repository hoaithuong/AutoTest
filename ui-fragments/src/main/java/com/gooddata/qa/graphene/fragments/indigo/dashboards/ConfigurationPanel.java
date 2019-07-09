package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;

import java.util.Collection;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConfigurationPanel extends AbstractFragment {

    private static final By BY_DRILL_TO_SELECT = By.className("s-drill_to_select");

    @FindBy(className = "s-metric_select")
    private MetricSelect metricSelect;

    @FindBy(css = ".s-metric_select button")
    private WebElement metricSelectLoaded;

    @FindBy(css = ".s-date-dataset-button")
    private WebElement dataSetSelectLoaded;

    @FindBy(css = ".s-filter-date-dropdown")
    private DateDimensionSelect dateDataSetSelect;

    @FindBy(className = "s-compare_with_select")
    private ComparisonSelect comparisonSelect;

    @FindBy(className = "s-button-remove-drill-to")
    private WebElement removeDrillToButton;

    @FindBy(className = "s-widget-alerts-information-loaded")
    private WebElement widgetAlertsLoaded;

    @FindBy(className = "s-alert-edit-warning")
    private WebElement alertEditWarning;

    @FindBy(className = "s-unlisted_measure")
    private WebElement unlistedMeasure;

    @FindBy(className = "s-attribute-filter-by-item")
    private List<FilterByItem> filterByAttributeFilters;

    @FindBy(className = "s-date-filter-by-item")
    private FilterByItem filterByDateFilter;

    public List<FilterByItem> getFilterByAttributeFilters() {
        return filterByAttributeFilters;
    }

    public FilterByItem getFilterByAttributeFilter(String filterTitle) {
        return getFilterByAttributeFilters()
                .stream()
                .filter(filter -> filter.getTitle().equalsIgnoreCase(filterTitle))
                .findFirst()
                .get();
    }

    public FilterByItem getFilterByDateFilter() {
        return filterByDateFilter;
    }

    private static final By ERROR_MESSAGE_LOCATOR = By.cssSelector(".gd-message.error");

    private ConfigurationPanel waitForVisDateDataSetsLoaded() {
        final Function<WebDriver, Boolean> dataSetLoaded =
                browser -> !dataSetSelectLoaded.getAttribute("class").contains("is-loading");
        Graphene.waitGui().until(dataSetLoaded);
        return this;
    }

    public List<String> getListDateDataset(){
        waitForVisDateDataSetsLoaded();
        waitForElementVisible(dataSetSelectLoaded).click();
        return waitForCollectionIsNotEmpty(browser.findElements(By.className("gd-list-item-shortened"))
                .stream().map(e->e.getText()).collect(Collectors.toList()));
    }

    public ConfigurationPanel waitForButtonsLoaded() {
        waitForElementVisible(metricSelectLoaded);
        return waitForVisDateDataSetsLoaded();
    }

    public ConfigurationPanel selectMetricByName(String name) {
        waitForFragmentVisible(metricSelect).selectByName(name);
        return this;
    }

    public ConfigurationPanel selectDateDataSetByName(String name) {
        waitForFragmentVisible(dateDataSetSelect).selectByName(name);
        return this;
    }

    public ConfigurationPanel selectComparisonByName(String name) {
        waitForFragmentVisible(comparisonSelect).selectByName(name);
        return this;
    }

    public ConfigurationPanel selectDrillToByName(String name) {
        getDrillToSelect().selectByName(name);
        return this;
    }

    public String getDrillToValue() {
        return getDrillToSelect().getSelection();
    }

    public ConfigurationPanel clickRemoveDrillToButton() {
        waitForElementVisible(removeDrillToButton).click();
        return this;
    }

    public String getSelectedDataSet() {
        waitForVisDateDataSetsLoaded();
        return waitForFragmentVisible(dateDataSetSelect).getSelection();
    }

    public Collection<String> getDataSets() {
        return waitForFragmentVisible(dateDataSetSelect).getValues();
    }

    public boolean isDateDataSetDropdownVisible() {
        return isElementPresent(By.className("s-filter-date-dropdown"), browser);
    }

    public MetricSelect getMetricSelect() {
        return waitForFragmentVisible(metricSelect);
    }

    public String getSelectedMetric() {
        return waitForFragmentVisible(metricSelect).getSelection();
    }

    public ConfigurationPanel waitForSelectedMetricIsUnlisted() {
        waitForElementVisible(unlistedMeasure);
        return this;
    }

    public ConfigurationPanel waitForAlertEditWarning() {
        waitForElementPresent(widgetAlertsLoaded);
        waitForElementVisible(alertEditWarning);

        return this;
    }

    public ConfigurationPanel waitForAlertEditWarningMissing() {
        waitForElementPresent(widgetAlertsLoaded);
        waitForElementNotVisible(alertEditWarning, 20);

        return this;
    }

    public String getKpiAlertMessage() {
        return waitForElementVisible(alertEditWarning).getText();
    }

    public ConfigurationPanel enableDateFilter() {
        getFilterByDateFilter().setChecked(true);
        return this;
    }

    public ConfigurationPanel disableDateFilter() {
        getFilterByDateFilter().setChecked(false);
        return this;
    }

    public boolean isDateFilterCheckboxChecked() {
        return getFilterByDateFilter().isChecked();
    }

    public boolean isDateFilterCheckboxEnabled() {
        return getFilterByDateFilter().isCheckboxEnabled();
     }

    public DateDimensionSelect openDateDataSet() {
        waitForFragmentVisible(dateDataSetSelect).ensureDropdownOpen();

        return dateDataSetSelect;
    }

    public boolean isDateDataSetSelectCollapsed() {
        return !dateDataSetSelect.isDropdownOpen();
    }

    public String getErrorMessage() {
        return waitForElementVisible(ERROR_MESSAGE_LOCATOR, getRoot()).getText();
    }

    public String getSelectedDataSetColor() {
        return waitForFragmentVisible(dateDataSetSelect).getSelectionColor();
    }

    public boolean isErrorMessagePresent() {
        waitForVisDateDataSetsLoaded();
        return isElementPresent(ERROR_MESSAGE_LOCATOR, getRoot());
    }

    public boolean isDrillToSelectVisible() {
        return isElementVisible(BY_DRILL_TO_SELECT, getRoot());
    }

    private DrillToSelect getDrillToSelect() {
        return Graphene.createPageFragment(DrillToSelect.class, waitForElementVisible(BY_DRILL_TO_SELECT, getRoot()));
    }
}
