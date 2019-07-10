package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.dragAndDropWithCustomBackend;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementEnabled;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.CssUtils.convertCSSClassTojQuerySelector;
import static com.gooddata.qa.utils.CssUtils.isShortenedTitleDesignByCss;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget.DropZone;
import com.gooddata.qa.graphene.utils.Sleeper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class IndigoDashboardsPage extends AbstractFragment {

    @FindBy(className = "mobile-navigation-button")
    private WebElement mobileNavigationButton;

    @FindBy(className = "dash-item")
    private List<Widget> widgets;

    @FindBy(className = SPLASH_SCREEN_CLASS_NAME)
    private SplashScreen splashScreen;

    @FindBy(className = "gd-header")
    private Header header;

    @FindBy(className = EDIT_BUTTON_CLASS_NAME)
    private WebElement editButton;

    @FindBy(className = "s-cancel_button")
    private WebElement cancelButton;

    @FindBy(className = SAVE_BUTTON_CLASS_NAME)
    private WebElement saveButton;

    @FindBy(css = "." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)")
    private WebElement enabledSaveButton;

    @FindBy(css = ".dash-nav-right .configuration-panel")
    private ConfigurationPanel configurationPanel;

    @FindBy(className = "s-attribute_select")
    private AttributeSelect attributeSelect;

    @FindBy(className = "dash-filters-date")
    private DateFilter dateFilter;

    @FindBy(className = DELETE_BUTTON_CLASS_NAME)
    private WebElement deleteButton;

    @FindBy(className = ATTRIBUTE_FITERS_PANEL_CLASS_NAME)
    private AttributeFiltersPanel attributeFiltersPanel;

    @FindBy(className = "dash-title")
    private WebElement dashboardTitle;

    @FindBy(className = "gd-button-text")
    private WebElement dashboardTitleOnMobile;

    @FindBy(className = "navigation-list")
    private WebElement dashboardsList;

    @FindBy(className = "navigation")
    private WebElement navigationBar;

    @FindBy(className = "navigation-add-dashboard")
    private WebElement addDashboard;

    @FindBy(css = ".highcharts-series")
    private List<WebElement> trackers;

    @FindBy(css = LEGEND_ITEM_ICON)
    private List<WebElement> legendIcons;

    @FindBy(className = "dash-item-action-delete")
    protected WebElement deleteInsightItemButton;

    public static final String LEGEND_ITEM = ".viz-legend .series .series-item";
    public static final String LEGEND_ITEM_ICON = LEGEND_ITEM + " .series-icon";
    private static final String LEGEND_COLOR_ATTRIBUTE = "style";

    private static final String EDIT_BUTTON_CLASS_NAME = "s-edit_button";
    private static final String SAVE_BUTTON_CLASS_NAME = "s-save_button";
    private static final String DELETE_BUTTON_CLASS_NAME = "s-delete_dashboard";
    private static final String ALERTS_LOADED_CLASS_NAME = "alerts-loaded";
    private static final String SPLASH_SCREEN_CLASS_NAME = "splashscreen";
    private static final String ATTRIBUTE_FITERS_PANEL_CLASS_NAME = "dash-filters-all";

    private static final String ADD_KPI_PLACEHOLDER = ".add-kpi-placeholder";
    private static final String DASHBOARD_BODY = ".dash-section";
    private static final String DELETE_DROPZONE = ".gd-dropzone-delete";

    private static final String ADD_ATTRIBUTE_FILTER_PLACEHOLDER = ".add-attribute-filter-placeholder";
    private static final String ADD_ATTRIBUTE_FILTER_DROPZONE = ".s-last-filter-drop-position";
    private static final String FIRST_ATTRIBUTE_FILTER = ".s-attribute-filter-0";

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final By SAVE_BUTTON_ENABLED = By.cssSelector("." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)");

    /* This snippet get value from background-color to a semicolon ";"
     *  For example : "background-color: rgb(255, 0, 0);"  ->  " rgb(255, 0, 0)"
     */
    private static final String ATTRIBUTE_COLOR_REGEX = ".*background-color: ([^;]*);.*";

    public static final String MAIN_ID = "app-dashboards";

    public static final IndigoDashboardsPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(IndigoDashboardsPage.class, waitForElementVisible(id(MAIN_ID), context));
    }

    public IndigoDashboardsPage addDashboard() {
        waitForElementVisible(addDashboard).click();
        waitForElementVisible(cancelButton);
        getInsightSelectionPanel().waitForLoading();
        return this;
    }

    public String checkColorColumn(int xAxis, int yAxis) {
        List<WebElement> list = waitForCollectionIsNotEmpty(getRoot()
                .findElements(By.cssSelector(String.format(".gd-base-visualization .highcharts-series-%s rect", xAxis))));
        return list.get(yAxis).getAttribute("fill");
    }

    /*Set value from "ATTRIBUTE_COLOR_REGEX" to "$1" and remove space value by replace method
     * For Example : " rgb(255, 0, 0)" -> "rgb(255, 0, 0)"
     */
    public List<String> getKpiLegendColors() {
        return waitForCollectionIsNotEmpty(legendIcons).stream()
                .map(e -> e.getAttribute(LEGEND_COLOR_ATTRIBUTE))
                .map(e -> e.replaceAll(ATTRIBUTE_COLOR_REGEX, "$1").replace(" ", ""))
                .collect(toList());
    }

    public boolean hasColorLegend() {
        return waitForCollectionIsNotEmpty(trackers).size() > 1;
    }

    public IndigoDashboardsPage deleteInsightItem() {
        waitForElementVisible(deleteInsightItemButton).click();
        return this;
    }

    public boolean isAddDashboardVisible() {
        return isElementVisible(By.className("navigation-add-dashboard"), browser);
    }

    public SplashScreen getSplashScreen() {
        return waitForFragmentVisible(splashScreen);
    }

    public boolean isSplashScreenPresent() {
        return isElementPresent(className(SPLASH_SCREEN_CLASS_NAME), browser);
    }

    public boolean isNavigationBarPresent() {
        return isElementPresent(className("navigation"), browser);
    }

    public boolean isNavigationBarVisible() {
        return isElementVisible(navigationBar);
    }

    public ConfigurationPanel getConfigurationPanel() {
        return waitForFragmentVisible(configurationPanel);
    }

    public IndigoDashboardsPage clickDashboardBody() {
        waitForElementPresent(cssSelector(DASHBOARD_BODY), getRoot()).click();
        return this;
    }

    public IndigoDashboardsPage switchToEditMode() {
        waitForElementEnabled(waitForElementVisible(editButton)).click();

        waitForElementVisible(cancelButton);
        if (isElementPresent(className("gd-visualizations-list"), getRoot())) {
            getInsightSelectionPanel().waitForLoading();
        } else {
            // There's an animation switching to edit mode,
            // so wait until the css transition is finished
            Sleeper.sleepTight(500);
        }

        // wait until editing is allowed
        return waitForWidgetsEditable();
    }

    public IndigoDashboardsPage cancelEditModeWithoutChange() {
        waitForElementVisible(cancelButton).click();
        waitForElementNotVisible(cancelButton);

        return this;
    }

    public IndigoDashboardsPage cancelEditModeWithChanges() {
        waitForElementVisible(cancelButton).click();
        ConfirmDialog.getInstance(browser).submitClick();
        waitForElementNotVisible(cancelButton);

        return this;
    }

    public IndigoDashboardsPage tryCancelingEditModeWithoutApplying() {
        waitForElementVisible(cancelButton).click();
        ConfirmDialog.getInstance(browser).cancelClick();

        return this;
    }

    public IndigoDashboardsPage tryCancelingEditModeByClickCloseButton() {
        waitForElementVisible(cancelButton).click();
        ConfirmDialog.getInstance(browser).closeClick();

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithWidgets() {
        waitForElementVisible(enabledSaveButton).click();
        waitForElementVisible(editButton);
        waitForWidgetsLoading();

        return this;
    }

    public IndigoDashboardsPage saveEditModeWithoutWidgets() {
        waitForElementVisible(enabledSaveButton).click();
        ConfirmDialog.getInstance(browser).submitClick();
        waitForFragmentVisible(splashScreen);

        return this;
    }

    public boolean isSaveEnabled() {
        return isElementPresent(SAVE_BUTTON_ENABLED, browser);
    }

    // if save is disabled, use cancel. But leave edit mode in any case
    public IndigoDashboardsPage leaveEditMode() {
        if (isSaveEnabled()) {
            return saveEditModeWithWidgets();
        }

        waitForElementVisible(cancelButton).click();
        if (ConfirmDialog.isPresent(browser))
            ConfirmDialog.getInstance(browser).submitClick();

        waitForElementNotVisible(cancelButton);
        return this;
    }

    public IndigoDashboardsPage waitForDashboardLoad() {
        waitForElementVisible(DASHBOARD_LOADED, browser);

        return this;
    }

    public IndigoDashboardsPage addAttributeFilter(String attributeTitle) {
        dragAddAttributeFilterPlaceholder();

        attributeSelect.selectByName(attributeTitle);
        return this;
    }

    public IndigoDashboardsPage addAttributeFilter(String attributeTitle, String value) {
        addAttributeFilter(attributeTitle).getAttributeFiltersPanel()
            .getAttributeFilter(attributeTitle)
            .clearAllCheckedValues()
            .selectByNames(value);
        return this;
    }

    public IndigoDashboardsPage addKpi(KpiConfiguration config) {
        dragAddKpiPlaceholder();
        configurationPanel
            .selectMetricByName(config.getMetric())
            .selectDateDataSetByName(config.getDataSet());

        if (config.hasComparison()) {
            configurationPanel.selectComparisonByName(config.getComparison());
        }

        if (config.hasDrillTo()) {
            configurationPanel.selectDrillToByName(config.getDrillTo());
        }

        return waitForWidgetsLoading();
    }

    public boolean isEditButtonVisible() {
        By buttonVisible = By.className(EDIT_BUTTON_CLASS_NAME);
        return isElementPresent(buttonVisible, browser);
    }

    public DateFilter waitForDateFilter() {
        return waitForFragmentVisible(dateFilter);
    }

    public IndigoDashboardsPage selectDateFilterByName(String dateFilterName) {
        waitForWidgetsLoading();

        waitForFragmentVisible(dateFilter)
            .selectByName(dateFilterName);

        return waitForWidgetsLoading();
    }

    public IndigoDashboardsPage dragWidget(final Widget source, final Widget target, DropZone dropZone) {
        final String sourceSelector = convertCSSClassTojQuerySelector(source.getRoot().getAttribute("class"));
        final String targetSelector = convertCSSClassTojQuerySelector(target.getRoot().getAttribute("class"));
        final String dropZoneSelector = targetSelector + " " + dropZone.getCss();

        dragAndDropWithCustomBackend(browser, sourceSelector, targetSelector, dropZoneSelector);

        return this;
    }

    public boolean searchInsight(final String insight) {
        return getInsightSelectionPanel().searchInsight(insight);
    }

    public void logout() {
        waitForFragmentVisible(header).logout();
    }

    public boolean isHamburgerMenuLinkPresent() {
        return waitForFragmentVisible(header).isHamburgerMenuLinkPresent();
    }

    public HamburgerMenu openHamburgerMenu() {
        return waitForFragmentVisible(header).openHamburgerMenu();
    }

    public IndigoDashboardsPage closeHamburgerMenu() {
        waitForFragmentVisible(header).closeHamburgerMenu();
        return this;
    }

    public IndigoDashboardsPage switchProject(String name) {
        log.info("Switching to project: " + name);
        waitForFragmentVisible(header).switchProject(name);

        return this;
    }

    public String getCurrentProjectName() {
        return waitForFragmentVisible(header).getCurrentProjectName();
    }

    public String getDashboardTitle() {
        return waitForElementVisible(isMobileMode() ? dashboardTitleOnMobile : dashboardTitle).getText();
    }

    public boolean isShortenTitleDesignByCss(int sizeText) {
        return isShortenedTitleDesignByCss(isMobileMode() ? dashboardTitleOnMobile : dashboardTitle, sizeText);
    }

    public IndigoDashboardsPage changeDashboardTitle(String newTitle) {
        waitForElementVisible(By.cssSelector(".dash-title.editable"), getRoot());
        // in the very first time, the label dash-title is not ready to click and could make the test unstable.
        // So sleep for 1 second for sure.
        // fix for ticket QA-7610
        sleepTightInSeconds(1);
        waitForElementVisible(dashboardTitle).click();
        waitForElementVisible(By.tagName("textarea"), dashboardTitle).sendKeys(newTitle, Keys.ENTER);
        return this;
    }

    public List<String> getDashboardTitles() {
        List<String> listTitle;
        if (isMobileMode()) {
            waitForElementVisible(mobileNavigationButton).click();
            listTitle = getDashboardTiltes(getMobileKpiDashboardSelection().dropdownList);
        } else {
            listTitle = getDashboardTiltes(dashboardsList);
        }
        return listTitle;
    }

    public IndigoDashboardsPage selectKpiDashboard(String title) {
        if (isMobileMode()) {
            waitForElementVisible(mobileNavigationButton).click();
            selectDashboard(title, getMobileKpiDashboardSelection().dropdownList);
            waitForFragmentVisible(this);
        } else {
            selectDashboard(title, dashboardsList);
        }
        waitForDashboardLoad();
        return this;
    }

    public String getSelectedKpiDashboard() {
        return waitForElementVisible(dashboardsList)
                .findElement(By.className("navigation-list-item-selected")).getText();
    }

    public IndigoDashboardsPage deleteDashboard(boolean confirm) {
        waitForElementVisible(deleteButton).click();
        ConfirmDialog deleteConfirmDialog = ConfirmDialog.getInstance(browser);

        if (confirm) {
            deleteConfirmDialog.submitClick();
            waitForFragmentNotVisible(deleteConfirmDialog);
        } else {
            deleteConfirmDialog.cancelClick();
        }

        return this;
    }

    public boolean isDeleteButtonVisible() {
        return isElementPresent(By.className(DELETE_BUTTON_CLASS_NAME), this.getRoot());
    }

    public IndigoDashboardsPage waitForSplashscreenMissing() {
        waitForFragmentNotVisible(splashScreen);
        return this;
    }

    public String getDateFilterSelection() {
        return waitForFragmentVisible(dateFilter).getSelection();
    }

    public AttributeFiltersPanel getAttributeFiltersPanel() {
        waitForElementPresent(By.className(ATTRIBUTE_FITERS_PANEL_CLASS_NAME), browser);
        return attributeFiltersPanel.waitForAttributeFiltersLoaded();
    }

    public IndigoDashboardsPage waitForAlertsLoaded() {
        waitForElementPresent(By.className(ALERTS_LOADED_CLASS_NAME), browser);
        return this;
    }

    public IndigoDashboardsPage waitForEditingControls() {
        waitForElementVisible(saveButton);
        waitForElementVisible(cancelButton);
        return this;
    }

    public IndigoDashboardsPage waitForWidgetsLoading() {
        // ensure dots element is present
        sleepTightInSeconds(1);

        Function<WebDriver, Boolean> isDotsElementPresent = browser -> !isElementPresent(className("gd-loading-dots"), browser);
        Graphene.waitGui().until(isDotsElementPresent);

        return this;
    }

    public <T extends Widget> T getWidgetByIndex(final Class<T> clazz, final int index) {
        return initWidgetObject(clazz, scrollWidgetIntoView(getWidgets().get(index)));
    }

    public <T extends Widget> T getWidgetByHeadline(final Class<T> clazz, final String searchString) {
        return initWidgetObject(clazz, scrollWidgetIntoView(
                getWidgets().stream()
                    .filter(widget -> {
                        // handle shortened name on preview mode
                        String widgetHeadline = widget.getHeadline();
                        return isShortenedTitle(widgetHeadline) ? compareShortenedTitle(widgetHeadline,
                                searchString) : searchString.equals(widgetHeadline);
                    })
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("Cannot find widget with headline: " + searchString))));
    }

    public <T extends Widget> T getLastWidget(final Class<T> clazz) {
        return getWidgetByIndex(clazz, getWidgets().size() - 1);
    }

    public <T extends Widget> T getFirstWidget(final Class<T> clazz) {
        return getWidgetByIndex(clazz, 0);
    }

    public <T extends Widget> T selectWidget(final Class<T> clazz, final int index) {
        return (T) getWidgetByIndex(clazz, index).clickOnContent();
    }

    public <T extends Widget> T selectWidgetByHeadline(final Class<T> clazz, final String headline) {
        return (T) getWidgetByHeadline(clazz, headline).clickOnContent();
    }

    public <T extends Widget> T selectFirstWidget(final Class<T> clazz) {
        return selectWidget(clazz, 0);
    }

    public <T extends Widget> T selectLastWidget(final Class<T> clazz) {
        return selectWidget(clazz, getWidgets().size() - 1);
    }

    public int getKpisCount() {
        return (int) getWidgets().stream().filter(Kpi::isKpi).count();
    }

    public int getInsightsCount() {
        return (int) getWidgets().stream().filter(Insight::isInsight).count();
    }

    public List<String> getInsightTitles() {
        return getWidgets().stream().filter(Insight::isInsight)
                .map(Widget::getHeadline).collect(Collectors.toList());
    }

    public List<String> getKpiTitles() {
        return getWidgets().stream().filter(Kpi::isKpi)
                .map(Widget::getHeadline).collect(Collectors.toList());
    }

    public IndigoDashboardsPage dragAddKpiPlaceholder() {
        // should fetch dashboard elements to avoid caching in view mode
        waitForElementVisible(cssSelector(ADD_KPI_PLACEHOLDER), getRoot());
        dragAndDropWithCustomBackend(browser, ADD_KPI_PLACEHOLDER, DASHBOARD_BODY, DropZone.LAST.getCss());

        return this;
    }

    public IndigoDashboardsPage dragAddAttributeFilterPlaceholder() {
        waitForElementVisible(cssSelector(ADD_ATTRIBUTE_FILTER_PLACEHOLDER), getRoot());
        dragAndDropWithCustomBackend(browser, ADD_ATTRIBUTE_FILTER_PLACEHOLDER, ADD_ATTRIBUTE_FILTER_DROPZONE, ADD_ATTRIBUTE_FILTER_DROPZONE);

        return this;
    }

    public IndigoDashboardsPage deleteAttributeFilter(String attribute) {
        String targetFilter = convertCSSClassTojQuerySelector(
                getAttributeFiltersPanel().getAttributeFilter(attribute).getRoot().getAttribute("class"));

        dragAndDropWithCustomBackend(browser, targetFilter, DASHBOARD_BODY, DELETE_DROPZONE);

        return this;
    }

    /**
     * Add an insight to last position in dashboard by drag and drop mode
     * @param insight
     * @return
     */
    public IndigoDashboardsPage addInsight(final String insight) {
        dragAndDropWithCustomBackend(browser,
                convertCSSClassTojQuerySelector(
                        getInsightSelectionPanel().getInsightItem(insight).getRoot().getAttribute("class")),
                DASHBOARD_BODY, DropZone.LAST.getCss());

        return this;
    }

    public IndigoInsightSelectionPanel getInsightSelectionPanel() {
        return IndigoInsightSelectionPanel.getInstance(browser);
    }

    public boolean isOnEditMode() {
        return isElementPresent(By.className("edit-mode-on"), getRoot());
    }

    public AttributeSelect getAttributeSelect() {
        return waitForFragmentVisible(attributeSelect);
    }

    public boolean hasAttributeFilterPlaceholder() {
        waitForElementVisible(className("add-item-panel"), browser);

        return isElementVisible(cssSelector(ADD_ATTRIBUTE_FILTER_PLACEHOLDER), browser);
    }

    public boolean hasAttributeFilterTrash() {
        return isElementVisible(cssSelector(DELETE_DROPZONE), browser);
    }

    public Dimension getDashboardBodySize() {
        return waitForElementVisible(cssSelector(DASHBOARD_BODY), browser).getSize();
    }

    private MobileKpiDashboardSelection getMobileKpiDashboardSelection() {
        return Graphene.createPageFragment(MobileKpiDashboardSelection.class,
                waitForElementVisible(className("gd-mobile-dropdown-overlay"), browser));
    }

    private IndigoDashboardsPage waitForWidgetsEditable() {
        waitForElementNotPresent(By.cssSelector(".dash-item-content > div:not(.is-editable)"));
        return this;
    }

    private Widget scrollWidgetIntoView(final Widget widget) {
        // Indigo DashBoard can load only one widget on mobile screen, and the others will present in DOM
        // and just visible when scrolling down, so the widget we need to work with may not be visible and interact.
        // And selenium script cannot do anything when an element is not visible.
        // In this stage, java script will be good solution when it can scroll to an element
        // although the element just present and not visible.

        // And the way java script makes element visible is not like when doing manual,
        // it just scroll and calculate until it think the element visible.
        // So in reality view, the element may show full or just a small part of it.
        if (!isElementVisible(widget.getRoot())) {
            scrollElementIntoView(widget.getRoot(), browser);
        }

        return waitForFragmentVisible(widget);
    }

    private List<Widget> getWidgets() {
        return waitForDashboardLoad().waitForWidgetsLoading().widgets;
    }

    private <T extends Widget> T initWidgetObject(final Class<T> clazz, final Widget widget) {
        if (clazz.isAssignableFrom(Kpi.class) && Kpi.isKpi(widget)) {
            return (T) Kpi.getInstance(widget.getRoot());
        }

        if (clazz.isAssignableFrom(Insight.class) && Insight.isInsight(widget)) {
            return (T) Insight.getInstance(widget.getRoot());
        }

        if (clazz.isAssignableFrom(Widget.class))
            return (T) widget;

        throw new RuntimeException("Widget type is not correct !!!");
    }

    private boolean compareShortenedTitle(String widgetHeadline, String searchString) {
        return searchString.startsWith(widgetHeadline.substring(0, widgetHeadline.indexOf("...")));
    }

    private boolean isShortenedTitle(String title) {
        if (!isOnEditMode() && title.contains("...")) {
            log.info("shortened widget name: " + title);
            return true;
        }

        return false;
    }

    private boolean isMobileMode() {
        return isElementPresent(By.className("mobile-navigation-button"), browser);
    }

    private void selectDashboard(String title, WebElement element) {
        waitForElementVisible(element)
            .findElements(By.className(isMobileMode() ? "gd-list-item" : "navigation-list-item")).stream()
            .filter(items -> items.getText().equals(title))
            .findFirst()
            .get()
            .click();
    }

    private List<String> getDashboardTiltes(WebElement element) {
        return waitForElementVisible(element)
                .findElements(cssSelector(".gd-list-item, .navigation-list-item"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
    }

    private class MobileKpiDashboardSelection extends AbstractFragment {
        @FindBy(className = "gd-mobile-dropdown-content")
        private WebElement dropdownList;
    }

    /**
     *
     * @param index : A color position in color palette
     * @return : A color position in color palette is selected
     */
    public String getColor(int index) {
        List<WebElement> list = waitForCollectionIsNotEmpty(getRoot()
                .findElements(By.cssSelector(".gd-base-visualization .highcharts-series rect")));
        return list.get(index).getAttribute("fill");
    }
}
