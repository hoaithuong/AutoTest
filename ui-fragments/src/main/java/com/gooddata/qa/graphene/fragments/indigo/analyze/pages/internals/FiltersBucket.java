package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.testng.Assert.*;

/**
 * Locator for filters differs in React app.
 */
public class FiltersBucket extends AbstractBucket {

    @FindBy(css = ".adi-bucket-item .button")
    private List<WebElement> filters;

    private static final String LOADING = "...";
    private static final By BY_FILTER_TEXT = By.cssSelector(".button-text");

    public int getFiltersCount() {
        return filters.size();
    }

    public FiltersBucket configDateFilter(String period) {
        WebElement filter = getDateFilter();
        filter.click();
        Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser)).select(period);
        assertTrue(getFilterTextHelper(filter).endsWith(": " + period));
        return this;
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     * @throws ParseException 
     */
    public FiltersBucket configDateFilter(String from, String to) throws ParseException {
        WebElement filter = getDateFilter();
        openDatePanelOfFilter(filter).configTimeFilter(from, to);
        return this;
    }

    public FiltersBucket configAttributeFilter(String attribute, String... values) {
        WebElement filter = getFilter(attribute);
        String oldText = getFilterTextHelper(filter);
        filter.click();
        AttributeFilterPickerPanel.getInstance(browser).select(values);
        assertNotEquals(getFilterTextHelper(filter), oldText);
        return this;
    }

    public boolean isFilterVisible(String dateOrAttribute) {
        if (filters.isEmpty()) {
            return false;
        }

        try {
            getFilter(dateOrAttribute);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public String getFilterText(String dateOrAttribute) {
        waitForCollectionIsNotEmpty(filters);
        return getFilterTextHelper(getFilter(dateOrAttribute));
    }

    public String getDateFilterText() {
        waitForCollectionIsNotEmpty(filters);
        return getFilterTextHelper(getDateFilter());
    }

    public WebElement getFilter(final String dateOrAttribute) {
        return waitForCollectionIsNotEmpty(filters).stream()
            .filter(e -> waitForFilterLoaded(e).findElement(BY_FILTER_TEXT).getText().startsWith(dateOrAttribute + ":"))
            .findFirst()
            .get();
    }

    public WebElement getDateFilter() {
        return waitForCollectionIsNotEmpty(filters).stream()
            .filter(e -> waitForFilterLoaded(e).getAttribute("class").contains("adi-date-filter-button"))
            .findFirst()
            .get();
    }

    public List<String> getDateFilterOptions() {
        WebElement filter = getDateFilter();
        filter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        List<String> ret = panel.getPeriods();
        filter.click();
        waitForFragmentNotVisible(panel);
        return ret;
    }

    /**
     * @param from format MM/DD/YYYY
     * @param to   format MM/DD/YYYY
     */
    public void configDateFilterByRangeButNotApply(String from, String to) {
        WebElement filter = getDateFilter();
        String oldFilterText = getFilterTextHelper(filter);
        openDatePanelOfFilter(filter).configTimeFilterByRangeButNotApply(from, to);
        assertEquals(getFilterTextHelper(filter), oldFilterText);
    }

    public void changeDateDimension(String currentDimension, String switchDimension) {
        WebElement filter = getFilter(currentDimension);
        openDatePanelOfFilter(filter).changeDateDimension(switchDimension);
    }

    public FiltersBucket changeDateDimension(String switchDimension) {
        openDatePanelOfFilter(getDateFilter()).changeDateDimension(switchDimension);

        return this;
    } 

    public boolean isDateFilterVisible() {
        if (filters.isEmpty()) {
            return false;
        }

        try {
            getDateFilter();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    @Override
    public String getWarningMessage() {
        throw new UnsupportedOperationException();
    }

    public DateFilterPickerPanel openDatePanelOfFilter(WebElement filter) {
        if (!isElementPresent(DateFilterPickerPanel.LOCATOR, browser))
            filter.click();

        return Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
    }

    private String getFilterTextHelper(WebElement filter) {
        return filter.findElement(BY_FILTER_TEXT).getText();
    }

    private WebElement waitForFilterLoaded(final WebElement filter) {
        Function<WebDriver, Boolean> filterLoaded = browser -> !LOADING.equals(getFilterTextHelper(filter));
        Graphene.waitGui().until(filterLoaded);
        return filter;
    }
}
