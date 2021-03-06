package com.gooddata.qa.graphene.fragments.common;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementDisabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;

import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.gooddata.qa.graphene.utils.ElementUtils;

import com.gooddata.qa.graphene.utils.Sleeper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class AbstractReactDropDown extends AbstractDropDown {

    private static final String IS_SELECTED = "is-selected";
    private static final String DISABLED_CLASS = "disabled";
    private static final String IS_LOADING_CLASS = "s-isLoading";

    @Override
    protected String getDropdownButtonCssSelector() {
        return "button";
    }

    @Override
    protected String getListItemsCssSelector() {
        return ".gd-list-item:not(.is-header)";
    }

    @Override
    protected String getSearchInputCssSelector() {
        return ".gd-input-search input";
    }

    @Override
    protected void waitForPickerLoaded() {
        waitForElementNotPresent(cssSelector(getDropdownCssSelector() + " ." + IS_LOADING_CLASS));
    }

    @Override
    public AbstractPicker searchForText(String text) {
        if (hasSearchField()) {
            return super.searchForText(text);
        }
        return this;
    }

    public boolean isApplyButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(className("s-apply"), getPanelRoot()));
    }

    public Boolean isItemSelected(String nameItem) {
        return waitForCollectionIsNotEmpty(getElements()).stream()
                .filter(e -> e.getText().equals(nameItem))
                .map(e -> e.getAttribute("class").contains(IS_SELECTED))
                .findFirst().get();
    }

    public Collection<String> getTitleItems() {
        return getElementTitles(getElements());
    }

    protected boolean isDropdownOpen() {
        String enabledButtonCSSSelector = getDropdownButtonCssSelector() + ":not(." + DISABLED_CLASS + ")";
        waitForElementVisible(By.cssSelector(enabledButtonCSSSelector), getRoot());

        return isElementPresent(By.cssSelector(getDropdownCssSelector()), browser);
    }

    public void ensureDropdownOpen() {
        if (!this.isDropdownOpen()) {
            this.toggleDropdown();
        }
    }

    public void ensureDropdownClosed() {
        if (this.isDropdownOpen()) {
            this.toggleDropdown();
        }
    }

    public WebElement getDropdownButton() {
        // some drop down button is not visible, so in general case, make sure it appears in DOM
        return waitForElementPresent(By.cssSelector(getDropdownButtonCssSelector()), getRoot());
    }

    protected void toggleDropdown() {
        if (isDropdownButtonEnabled()) {
            waitForElementVisible(By.cssSelector(getDropdownButtonCssSelector()), getRoot()).click();
        }
    }

    public Boolean isDropdownButtonEnabled() {
        return !isElementDisabled(waitForElementVisible(getDropdownButton()));
    }

    public AbstractReactDropDown selectByName(String name) {
        ensureDropdownOpen();
        searchForText(name);
        getElementByName(name).click();

        // wait until the selection is made and propagated to the button title
        waitForSelectionIsApplied(name);

        return this;
    }

    public AbstractReactDropDown waitForLoadingIconHidden() {
        final By loadingIcon = className("s-isLoading");
        try {
            Function<WebDriver, Boolean> isLoadingIconPresent = browser -> isElementPresent(loadingIcon, browser);
            Graphene.waitGui().withTimeout(3, TimeUnit.SECONDS).until(isLoadingIconPresent);
        } catch (TimeoutException e) {
            //do nothing
        }
        waitForElementNotPresent(loadingIcon);
        return this;
    }

    public AbstractReactDropDown selectAttributeByName(String name) {
        ensureDropdownOpen();
        waitForLoadingIconHidden();
        searchForText(name);
        getElementByName(name).click();
        waitForSelectionIsApplied(name);
        return this;
    }

    public AbstractReactDropDown scrollToViewDateFilter(String item) {
        By selector = item.contains("week") ? By.cssSelector(".s-" + simplifyText(item) + "_us")
                : By.cssSelector(".s-" + simplifyText(item));
        if (item.equals("Week (Sun-Sat)")) selector = By.cssSelector(".s-week");
        ElementUtils.scrollElementIntoView(waitForElementPresent(selector, browser), browser);
        return this;
    }

    public AbstractReactDropDown addByNames(String... names) {
        ensureDropdownOpen();
        for (String name : names) {
            searchForText(name);
            getElementByName(name).click();
            Graphene.waitGui().until(browser ->
                    getElementByName(name).getAttribute("class").contains("is-selected"));
        }
        return this;
    }

    public String getSelection() {
        return getDropdownButton().getText();
    }

    protected void waitForSelectionIsApplied(String name) {
        By buttonTitle = cssSelector(getDropdownButtonCssSelector() + ".s-" + simplifyText(name));
        waitForElementVisible(buttonTitle, this.getRoot());
    }

    public Collection<String> getValues() {
        ensureDropdownOpen();
        return getElementTexts(getElements());
    }

    public boolean isShowingNoMatchingDataMessage() {
        waitForPickerLoaded();
        return isElementPresent(cssSelector(getNoMatchingDataMessageCssSelector()), getPanelRoot());
    }

    /**
     * get all values on dropdown in case having scrollbar
     * @return list of value names
     */
    public List<String> getValuesWithScrollbar() {
        // does not work if value list is empty
        waitForElementVisible(cssSelector(getSearchInputCssSelector()), getPanelRoot());

        // add a break to handle lazy load (< 20 values)
        // for larger number of values, it should be managed by scrolling (use js) to the end of scrollbar.
        Sleeper.sleepTightInSeconds(1);

        List<WebElement> elements = getPanelRoot().findElements(By.cssSelector(getListItemsCssSelector()));

        return elements.stream()
                .map(e -> {
                    scrollElementIntoView(e, browser);// return empty value if the element is not in viewport
                    return e.getText();
                })
                .collect(Collectors.toList());
    }

    protected String getNoMatchingDataMessageCssSelector() {
        return ".gd-no-matching-data";
    }

    private static List<String> getElementTitles(Collection<WebElement> elements) {
        return elements.stream()
                .map(name -> name.getAttribute("title"))
                .collect(toList());
    }
}
