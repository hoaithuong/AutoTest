package com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals;

import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.analyze.description.DescriptionPanel;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.gooddata.qa.graphene.utils.ElementUtils.getElementTexts;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

/**
 * search() method needs to sleep tight for 1 sec in order to work.
 */
public class CatalogPanel extends AbstractFragment {

    @FindBy(css = ".s-catalogue-search input")
    private WebElement searchInput;

    @FindBy(className = "s-date")
    private WebElement dateItem;

    @FindBy(className = "s-catalog-item")
    private List<WebElement> items;

    @FindBy(className = CATALOG_LOADED_CLASS_NAME)
    private WebElement catalogLoaded;

    @FindBy(className = "s-filter-all")
    private WebElement filterAll;

    @FindBy(className = "s-filter-measures")
    private WebElement filterMeasures;

    @FindBy(className = "s-filter-attributes")
    private WebElement filterAttributes;

    @FindBy(className = "s-dataset-picker-toggle")
    private WebElement datasetPicker;

    @FindBy(className = "s-catalog-group-label")
    private List<WebElement> catalogGroupLabels;

    @FindBy(className = "s-no-objects-found")
    private WebElement noObjectsFound;

    public static final By BY_LOADING_ICON = By.cssSelector(".gd-spinner.small");
    public static final String CATALOG_LOADED_CLASS_NAME = "s-catalogue-loaded";

    private static final By BY_INLINE_HELP = By.cssSelector(".inlineBubbleHelp");
    private static final By BY_NO_ITEMS = By.className("adi-no-items");
    private static final By BY_UNRELATED_ITEMS_HIDDEN = By.cssSelector("footer > div");
    private static final By BY_UNAVAILABLE_ITEMS_MATCHED = By.className("s-unavailable-items-matched");
    private static final By BY_ADD_DATA = By.cssSelector(".csv-link-section .s-btn-add_data");
    private static final By BY_CLEAR_SEARCH_FIELD = By.className("gd-input-icon-clear");
    private static final By BY_DATASOURCE_DROPDOWN = By.className("data-source-picker-dropdown");

    private static final String WEIRD_STRING_TO_CLEAR_ALL_ITEMS = "!@#$%^";

    public int getUnrelatedItemsHiddenCount() {
        waitForItemLoaded();
        By locator = isElementPresent(BY_NO_ITEMS, browser) ?
                BY_UNAVAILABLE_ITEMS_MATCHED : BY_UNRELATED_ITEMS_HIDDEN;

        if (!isElementPresent(locator, getRoot())) {
            return 0;
        }

        String unrelatedItemsHiddenMessage = waitForElementVisible(locator, getRoot()).getText().trim();
        return Integer.parseInt(unrelatedItemsHiddenMessage.split(" ")[0]);
    }

    public CatalogPanel filterCatalog(CatalogFilterType type) {
        WebElement filter;
        switch(type) {
            case ALL:
                filter = filterAll;
                break;
            case MEASURES:
                filter = filterMeasures;
                break;
            case ATTRIBUTES:
                filter = filterAttributes;
                break;
            default:
                filter = filterAll;
                break;
        }
        waitForElementVisible(filter).click();
        waitForItemLoaded();
        return this;
    }

    public WebElement getDate() {
        clearInputText();
        waitForElementVisible(dateItem);
        return waitForCollectionIsNotEmpty(items).stream()
            .filter(date -> "Date".equals(date.getText()))
            .filter(date -> date.getAttribute("class").contains(FieldType.DATE.toString()))
            .findFirst()
            .get();
    }

    public String getDateDescription() {
        WebElement field = getDate();
        getActions().moveToElement(field).perform();
        getActions().moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getTimeDescription();
    }

    public String getAttributeDescription(String attribute) {
        WebElement field = searchAndGet(attribute, FieldType.ATTRIBUTE);
        getActions().moveToElement(field).perform();
        getActions().moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getAttributeDescription();
    }

    public String getMetricDescription(String metric) {
        WebElement field = searchAndGet(metric, FieldType.METRIC);

        getActions().moveToElement(field).perform();
        getActions().moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getMetricDescription();
    }

    public String getMetricDescriptionAndGroupCatalog(String metric) {
        return getMetricDescription(metric).concat(getDescriptionPanel().getGroupCatalog());
    }

    public String getFactDescription(String fact) {
        WebElement field = searchAndGet(fact, FieldType.FACT);

        Actions actions = getActions();
        actions.moveToElement(field).perform();
        actions.moveToElement(field.findElement(BY_INLINE_HELP)).perform();

        return Graphene.createPageFragment(DescriptionPanel.class,
                waitForElementVisible(DescriptionPanel.LOCATOR, browser)).getFactDescription();
    }

    public List<String> getFieldNamesInViewPort() {
        waitForItemLoaded();
        return getElementTexts(items);
    }

    public List<String> getTextCatalogGroupLabels() {
        waitForItemLoaded();
        return getElementTexts(catalogGroupLabels);
    }

    public String getNoObjectsFound() {
        return waitForElementVisible(noObjectsFound).getText();
    }

    public CatalogPanel expandCatalogGroupLabels(String nameGroup) {
        if (!isGroupLabelExpanded(nameGroup)) {
            getCatalogGroupLabels(nameGroup).click();
        }
        return this;
    }

    public Boolean isGroupLabelExpanded(String nameGroup) {
        WebElement groupWebElement = getCatalogGroupLabels(nameGroup);
        return groupWebElement.getAttribute("class").contains("s-is-expanded");
    }

    public boolean isEmpty() {
        return getFieldNamesInViewPort().isEmpty();
    }

    public String getEmptyMessage() {
        return waitForElementVisible(BY_NO_ITEMS, getRoot())
                .findElement(By.className("s-not-matching-message")).getText().trim();
    }

    /**
     * Search metric/attribute/fact ... in catalogue panel (The panel in the left of Analysis Page)
     * @param item
     * @return true if found something from search input, otherwise return false
     */
    public CatalogPanel search(String item) {
        clearInputText();
        searchInput.sendKeys(item);
        waitForItemLoaded();
        return this;
    }

    public CatalogPanel clearInputText() {
        if (!searchInput.getAttribute("value").isEmpty()) {
            waitForElementVisible(BY_CLEAR_SEARCH_FIELD, getRoot()).click();
            waitForItemLoaded();
        }
        return this;
    }

    public Collection<WebElement> getFieldsInViewPort() {
        return items;
    }

    public boolean isDataApplicable(final String data) {
        return items.stream()
            .map(WebElement::getText)
            .anyMatch(text -> data.equals(text.trim()));
    }

    public boolean isAddDataLinkVisible() {
        if (!isElementPresent(BY_ADD_DATA, getRoot())) {
            return false;
        }

        waitForElementVisible(BY_ADD_DATA, getRoot());
        return true;
    }

    public CatalogPanel changeDataset(String dataset) {
        waitForElementVisible(datasetPicker).click();
        Graphene.createPageFragment(DatasourceDropDown.class,
                waitForElementVisible(BY_DATASOURCE_DROPDOWN, browser)).select(dataset);
        waitForItemLoaded();
        return this;
    }

    public WebElement searchAndGet(final String item, final FieldType type) {
        search(item);
        return items.stream()
            .filter(e -> item.equals(e.getText().trim()))
            .filter(e -> e.getAttribute("class").contains(type.toString()))
            .findFirst()
            .get();
    }

    public boolean isDatasetApplied(final String dataset) {
        waitForItemLoaded();
        return waitForElementVisible(className("data-source-picker"), browser).getText().equals(dataset);
    }

    public CatalogPanel waitForItemLoaded() {
        Function<WebDriver, Boolean> itemsLoaded = browser -> !isElementPresent((BY_LOADING_ICON), browser);
        waitForElementVisible(catalogLoaded);
        Graphene.waitGui().until(itemsLoaded);
        return this;
    }

    private DescriptionPanel getDescriptionPanel() {
        return Graphene.createPageFragment(DescriptionPanel.class,
            waitForElementVisible(DescriptionPanel.LOCATOR, browser));
    }

    public class DatasourceDropDown extends AbstractFragment {

        @FindBy(className = "gd-list-item")
        private List<WebElement> items;

        public void select(final String dataset) {
            waitForCollectionIsNotEmpty(items).stream()
                .filter(e -> dataset.equals(e.getText()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Cannot find dataset: " + dataset))
                .click();
        }
    }

    private WebElement getCatalogGroupLabels(String nameGroup) {
        waitForItemLoaded();
        return catalogGroupLabels.stream()
            .filter(e -> e.getText().equals(nameGroup))
            .findFirst().get();
    }
}
