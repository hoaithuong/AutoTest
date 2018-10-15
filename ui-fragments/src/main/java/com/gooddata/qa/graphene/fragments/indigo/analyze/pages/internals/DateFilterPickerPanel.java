package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown.CompareType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DatePresetsSelect;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.common.AbstractReactDropDown;
import com.gooddata.qa.graphene.fragments.indigo.analyze.DateDimensionSelect;

public class DateFilterPickerPanel extends AbstractFragment {

    public static final String STATIC_PERIOD_DROPDOWN_ITEM = "Static period";

    @FindBy(className = "s-date-preset-button")
    private WebElement presetsDropdown;

    @FindBy(css = ".s-date-presets-list .gd-list-item")
    private List<WebElement> periods;

    @FindBy(css = ".adi-date-input-from .input-text")
    private WebElement fromDate;

    @FindBy(css = ".adi-date-input-to .input-text")
    private WebElement toDate;

    @FindBy(css = ".adi-date-input-from > span")
    private WebElement fromDateCalendarIcon;

    @FindBy(css = ".adi-date-input-to > span")
    private WebElement toDateCalendarIcon;

    @FindBy(className = "s-date-filter-cancel")
    private WebElement cancelButton;

    @FindBy(className = "s-date-filter-apply")
    private WebElement applyButton;

    @FindBy(className = "s-compare-apply-measures-button")
    private WebElement compareApplyMeasuresButton;

    @FindBy(className = "s-compare-apply-measures")
    private CompareApplyMeasure compareApplyMeasures;

    public static final By LOCATOR = By.className("adi-date-filter-picker");

    public DateFilterPickerPanel changeDateDimension(String switchDimension) {
        getDateDatasetSelect().selectByName(switchDimension);
        return this;
    }

    public DateFilterPickerPanel changePeriod(final String period) {
        getDatePresetSelect().selectByName(period);
        getDatePresetSelect().ensureDropdownClosed();
        return this;
    }

    public DateFilterPickerPanel selectStaticPeriod() {
        changePeriod(STATIC_PERIOD_DROPDOWN_ITEM);
        return this;
    }

    public DateFilterPickerPanel configTimeFilterByRangeHelper(String from, String to) {
        selectStaticPeriod();

        fillInDateRange(waitForElementVisible(fromDate), from);
        waitForElementVisible(fromDateCalendarIcon).click();

        fillInDateRange(waitForElementVisible(toDate), to);
        waitForElementVisible(toDateCalendarIcon).click();
        return this;
    }

    public DateFilterPickerPanel changeCompareType(CompareType compareType) {
        getCompareTypeDropdown().selectCompareType(compareType.getCompareTypeName());
        return this;
    }

    public List<String> getPeriods() {
        return getDatePresetSelect()
                .getValues()
                .stream()
                .collect(Collectors.toList());
    }

    public Collection<String> getDimensionSwitchs() {
        DateDimensionSelect select = getDateDatasetSelect();
        Collection<String> values = select.getValues();
        select.ensureDropdownClosed();
        return values;
    }

    /**
     * @param from
     *         format MM/DD/YYYY
     * @param to
     *         format MM/DD/YYYY
     */
    public void configTimeFilterByRangeButNotApply(String from, String to) {
        configTimeFilterByRangeHelper(from, to, false);
    }

    /**
     * @param from
     *         format MM/DD/YYYY
     * @param to
     *         format MM/DD/YYYY
     */
    public void configTimeFilter(String from, String to) {
        configTimeFilterByRangeHelper(from, to, true);
    }

    public String getFromDate() {
        return waitForElementVisible(fromDate).getAttribute("value");
    }

    public String getToDate() {
        return waitForElementVisible(toDate).getAttribute("value");
    }

    public String getCompareApplyMeasuresText() {
        return waitForElementVisible(compareApplyMeasuresButton).getText();
    }

    public boolean isDimensionSwitcherEnabled() {
        return getDateDatasetSelect().isEnabled();
    }

    public String getSelectedDimensionSwitch() {
        return getDateDatasetSelect().getRoot().getText();
    }

    public DateDimensionSelect getDateDatasetSelect() {
        return Graphene.createPageFragment(DateDimensionSelect.class,
                waitForElementVisible(By.className("adi-date-dataset-select-dropdown"), browser));
    }

    public AbstractReactDropDown getDatePresetSelect() {
        return Graphene.createPageFragment(DatePresetsSelect.class,
                waitForElementVisible(By.className("adi-date-preset-select-dropdown"), browser));
    }

    public CompareApplyMeasure openCompareApplyMeasures() throws NoSuchFieldException {
        if(!isElementVisible(compareApplyMeasuresButton)) {
            throw new NoSuchFieldException("Compare the period with nothing doesn't need to choose measure");
        }
        waitForFragmentVisible(compareApplyMeasures).ensureDropdownOpen();
        return compareApplyMeasures;
    }

    public void apply() {
        waitForElementVisible(applyButton).click();
        waitForFragmentNotVisible(this);
    }

    /**
     * applies given compareType on insight
     * @param compareType compare type to be applied
     */
    public void applyCompareType(CompareType compareType) {
        getCompareTypeDropdown().selectCompareType(compareType.getCompareTypeName());
        apply();
    }

    /**
     * check whether comparison type is enabled
     * @param compareType compare type to check whether it is enabled
     * @return true whether it is enabled
     */
    public boolean isCompareTypeEnabled(final CompareType compareType) {
        return getCompareTypeDropdown().isCompareTypeEnabled(compareType);
    }

    public boolean isApplyButtonEnabled() {
        return !waitForElementVisible(applyButton).getAttribute("class").contains("disabled");
    }

    public void fillInDateRange(WebElement dateInput, String date) {
        dateInput.sendKeys(Keys.END);
        int length = dateInput.getAttribute("value").length();
        for (int i = 0; i <= length; i++) {
            dateInput.sendKeys(Keys.BACK_SPACE);
        }
        dateInput.sendKeys(date, Keys.ENTER);
    }

    private void configTimeFilterByRangeHelper(String from, String to, boolean apply) {
        configTimeFilterByRangeHelper(from, to);
        waitForElementVisible(apply ? applyButton : cancelButton).click();
        waitForFragmentNotVisible(this);
    }

    private CompareTypeDropdown getCompareTypeDropdown() {
        return Graphene.createPageFragment(CompareTypeDropdown.class,
                waitForElementVisible(By.className("adi-compare-apply-select"), browser));
    }
}
