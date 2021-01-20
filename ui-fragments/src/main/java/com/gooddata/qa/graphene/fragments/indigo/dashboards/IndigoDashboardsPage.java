package com.gooddata.qa.graphene.fragments.indigo.dashboards;

import static com.gooddata.qa.browser.BrowserUtils.dragAndDropWithCustomBackend;
import static com.gooddata.qa.browser.BrowserUtils.moveToBottomOfElement;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.scrollElementIntoView;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_PROGRESS_MESSAGE_BAR;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_SUCCESS_MESSAGE_BAR;
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
import static java.util.Collections.singletonList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;
import static java.util.Collections.EMPTY_LIST;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.enums.indigo.ResizeBullet;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.dashboards.menu.OptionalHeaderMenu;
import com.gooddata.qa.graphene.fragments.indigo.HamburgerMenu;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Widget.DropZone;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.utils.CalculateUtils;
import com.gooddata.qa.graphene.utils.ElementUtils;
import com.gooddata.qa.graphene.utils.Sleeper;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class IndigoDashboardsPage extends AbstractFragment {

    @FindBy(css = ROW_CONTAINER)
    private List<Row> rows;

    @FindBy(className = "mobile-navigation-button")
    private WebElement mobileNavigationButton;

    @FindBy(className = "dash-item")
    private List<Widget> widgets;

    @FindBy(className = SPLASH_SCREEN_CLASS_NAME)
    private SplashScreen splashScreen;

    @FindBy(className = SPLASH_SCREEN_WRAPPER_CLASS_NAME)
    private SplashScreen splashScreenWrapper;

    @FindBy(className = "gd-header")
    private Header header;

    @FindBy(className = EDIT_BUTTON_CLASS_NAME)
    private WebElement editButton;

    @FindBy(className = HEADER_OPTIONS_BUTTON_CLASS_NAME)
    private WebElement headerOptionsButton;

    @FindBy(className = "s-cancel_button")
    private WebElement cancelButton;

    @FindBy(className = SAVE_BUTTON_CLASS_NAME)
    private WebElement saveButton;

    @FindBy(css = "." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)")
    private WebElement enabledSaveButton;

    @FindBy(className = "s-attribute_select")
    private AttributeSelect attributeSelect;

    @FindBy(className = DATE_FILTER_SELECTOR)
    private DateFilter dateFilter;

    @FindBy(className = DELETE_BUTTON_CLASS_NAME)
    private WebElement deleteButton;

    @FindBy(className = ATTRIBUTE_FITERS_PANEL_CLASS_NAME)
    private AttributeFiltersPanel attributeFiltersPanel;

    @FindBy(className = DASHBOARD_TITLE_CLASS_NAME)
    private WebElement dashboardTitle;

    @FindBy(css = ".mobile-navigation-button .gd-button-text")
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

    @FindBy(css = ADD_KPI_PLACEHOLDER)
    private WebElement addKpiPlaceHolder;

    @FindBy(className = "dash-item-action-delete")
    protected WebElement deleteInsightItemButton;

    @FindBy(className = "button-filter-bar-show-all")
    protected WebElement showAllFilterBtn;

    @FindBy(className = "s-dash-item-action-placeholder")
    private WebElement exportCsvAndXlsxIcon;

    @FindBy(xpath = "//span[text()='Save as new']")
    private WebElement saveAsNewButton;

    public static final String LEGEND_ITEM = ".viz-legend .series .series-item";
    public static final String LEGEND_ITEM_ICON = LEGEND_ITEM + " .series-icon";
    private static final String LEGEND_COLOR_ATTRIBUTE = "style";
    private static final String DATE_FILTER_SELECTOR = "dash-filters-date";

    private static final String EDIT_BUTTON_CLASS_NAME = "s-edit_button";
    private static final String HEADER_OPTIONS_BUTTON_CLASS_NAME = "s-header-options-button";
    private static final String SAVE_BUTTON_CLASS_NAME = "s-save_button";
    private static final String DELETE_BUTTON_CLASS_NAME = "s-delete_dashboard";
    private static final String ALERTS_LOADED_CLASS_NAME = "alerts-loaded";
    private static final String SPLASH_SCREEN_CLASS_NAME = "splashscreen";
    private static final String SPLASH_SCREEN_WRAPPER_CLASS_NAME = "splashscreen-wrapper";
    private static final String FLUID_LAYOUT_ROWS_CSS = ".gd-fluidlayout-row:not(.s-fluid-layout-row-dropzone)";
    private static final String ATTRIBUTE_FITERS_PANEL_CLASS_NAME = "dash-filters-all";
    private static final String FLUID_LAYOUT_WIDTH_RESIZER_CLASS_NAME = "gd-fluidlayout-width-resizer";
    private static final String CONTAINER_WIDTH_RESIZER_CLASS_NAME = "dash-width-resizer-container";
    private static final String HOTSPOT_WIDTH_RESIZER_CLASS_NAME = "dash-width-resizer-hotspot";
    private static final String RESIZE_BULLET_CSS = ".s-resize-bullet-%s";
    private static final String DASHBOARD_TITLE_CLASS_NAME = "dash-title";
    private static final String DASHBOARD_FILTER_CLASS_NAME = "dash-filters-visible";
    private static final String ROW_CONTAINER = ".gd-fluidlayout-row:not(.s-fluid-layout-row-dropzone)";
    private static final String ADD_INSIGHT_PLACEHOLDER = ".add-insight-placeholder";

    private static final String ADD_KPI_PLACEHOLDER = ".add-kpi-placeholder";
    private static final String DASHBOARD_BODY = ".dash-section";
    private static final String DELETE_DROPZONE = ".gd-dropzone-delete";

    private static final String ADD_ATTRIBUTE_FILTER_PLACEHOLDER = ".add-attribute-filter-placeholder";
    private static final String ADD_ATTRIBUTE_FILTER_DROPZONE = ".s-last-filter-drop-position";
    private static final String FIRST_ATTRIBUTE_FILTER = ".s-attribute-filter-0";

    private static final By DASHBOARD_LOADED = By.cssSelector(".is-dashboard-loaded");
    private static final By SAVE_BUTTON_ENABLED = By.cssSelector("." + SAVE_BUTTON_CLASS_NAME + ":not(.disabled)");
    private static final By DISABLED_EMPTYDASHBOARDBUTTON_BUBBLE_MESSAGE = cssSelector(".bubble-content div.content");

    /* This snippet get value from background-color to a semicolon ";"
     *  For example : "background-color: rgb(255, 0, 0);"  ->  " rgb(255, 0, 0)"
     */
    private static final String ATTRIBUTE_COLOR_REGEX = ".*background-color: ([^;]*);.*";

    public static final String MAIN_ID = "app-dashboards";

    public static final IndigoDashboardsPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(IndigoDashboardsPage.class, waitForElementVisible(id(MAIN_ID), context));
    }

    public IndigoDashboardsPage addDashboard() {
        if (isSplashScreenPresent()) {
            getSplashScreen().startEditingWidgets();
        } else {
            waitForElementVisible(addDashboard).click();
        }
        waitForElementVisible(cancelButton);
        getInsightSelectionPanel().waitForLoading();
        return this;
    }

    public IndigoDashboardsPage createDashboard() {
        getSplashScreen().startEditingWidgets();
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

    public boolean isWidthResizerColorVisible() {
        return waitForElementVisible(By.className(FLUID_LAYOUT_WIDTH_RESIZER_CLASS_NAME), browser)
                .getCssValue("background-color").equals("rgba(20, 178, 226, 0.15)");
    }

    public boolean isDashboardNotFound() {
        return isElementVisible(By.className("embedded-error"), root);
    }

    public int getSizeWidthResizer() {
        return getRoot().findElements(By.className(FLUID_LAYOUT_WIDTH_RESIZER_CLASS_NAME)).size();
    }

    public SplashScreen getSplashScreen() {
        return waitForFragmentVisible(splashScreen);
    }

    public SplashScreen getSplashScreenWrapper() {
        return waitForFragmentVisible(splashScreenWrapper);
    }

    public WebElement getSaveButton() {
        return waitForElementVisible(saveButton);
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
        return ConfigurationPanel.getInstance(browser);
    }

    public InsightConfigurationPanel getInsightConfigurationPanel() {
        return InsightConfigurationPanel.getInstance(browser);
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

        try {
            waitForElementVisible(ElementUtils.BY_CLOSED_ICON_MESSAGE_BAR, browser, 3).click();
            waitForElementNotVisible(ElementUtils.BY_CLOSED_ICON_MESSAGE_BAR, browser);
        } catch (TimeoutException | NoSuchElementException e) {
            // Success message isn't displayed so WebDriver unable to catch the loading indicator
        }
        return this;
    }

    public IndigoDashboardsPage waitForSaveButtonEnabled() {
        waitForElementVisible(enabledSaveButton);
        return this;
    }

    public IndigoDashboardsPage saveEditModeWithoutWidgets() {
        waitForElementVisible(enabledSaveButton).click();
        ConfirmDialog.getInstance(browser).submitClick();
        waitForFragmentVisible(splashScreen);

        return this;
    }

    public IndigoDashboardsPage saveEditMode() {
        // In case need quickly and don't need to wait for element loading
        waitForElementVisible(enabledSaveButton).click();
        waitForElementVisible(editButton);
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

        return setupConfigurationPanel(config);
    }

    public AnalysisPage addNewInsight() {
        waitForElementVisible(cssSelector(ADD_INSIGHT_PLACEHOLDER), getRoot());
        dragAddInsightPlaceholder();
        sleepTightInSeconds(1);
        browser.switchTo().frame(waitForElementVisible(tagName("iframe"), browser));
        return AnalysisPage.getInstance(browser);
    }

    //Add KPI by order from left to right
    public IndigoDashboardsPage addKpiToBeginningOfRow(KpiConfiguration config) {
        if (getWidgets().size() > 0) {
            dragAndDropWidgetToCreateSameRow(waitForElementPresent(By.cssSelector(ADD_KPI_PLACEHOLDER), browser),
                    getWidgets().get(0).getRoot(), DropZone.PREV, true);
        } else {
            dragAddKpiPlaceholder();
        }

        return setupConfigurationPanel(config);
    }

    public boolean isEditButtonVisible() {
        By buttonVisible = By.className(EDIT_BUTTON_CLASS_NAME);
        return isElementPresent(buttonVisible, browser);
    }

    public boolean isHeaderOptionsButtonVisible() {
        return isElementVisible(By.className(HEADER_OPTIONS_BUTTON_CLASS_NAME), browser);
    }

    public IndigoDashboardsPage exportDashboardToPDF() {
        //wait for exporting dashboard tab in maximum 10 minutes
        int exportingTextDisplayedTimeoutInSeconds = 600;

        openHeaderOptionsButton().exportToPDF();

        waitForElementVisible(BY_PROGRESS_MESSAGE_BAR, browser);
        waitForElementNotPresent(BY_PROGRESS_MESSAGE_BAR, exportingTextDisplayedTimeoutInSeconds);

        waitForElementVisible(BY_SUCCESS_MESSAGE_BAR, browser);
        waitForElementNotPresent(BY_SUCCESS_MESSAGE_BAR, exportingTextDisplayedTimeoutInSeconds);

        return this;
    }

    public OptionalHeaderMenu openHeaderOptionsButton() {
        waitForElementVisible(headerOptionsButton).click();
        return OptionalHeaderMenu.getInstance(browser);
    }

    public ScheduleEmailDialog scheduleEmailing() {
        return openHeaderOptionsButton().scheduleEmailing();
    }

    public SaveAsDialog saveAsDialog() {
        return openHeaderOptionsButton().saveAsNew();
    }

    public boolean hasDateFilter() {
        return isElementVisible(className(DATE_FILTER_SELECTOR), getRoot());
    }

    public DateFilter getDateFilter() {
        return waitForFragmentVisible(dateFilter);
    }

    public ExtendedDateFilterPanel openExtendedDateFilterPanel() {
        if (!isElementPresent(className("is-active"), waitForFragmentVisible(dateFilter).getRoot())) {
            dateFilter.getRoot().click();
        }
        return ExtendedDateFilterPanel.getInstance(browser);
    }

    public IndigoDashboardsPage selectDateFilterByName(String dateFilterName) {
        openExtendedDateFilterPanel().selectPeriod(dateFilterName).apply();

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
        waitForOpeningIndigoDashboard();
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

    public IndigoDashboardsPage deleteDashboardOnMenuItem(boolean confirm) {
        openHeaderOptionsButton().clickOnDeleteMenuItem();
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

        Function<WebDriver, Boolean> isDotsElementPresent = browser -> !isElementPresent(cssSelector(".gd-loading-dots,.gd-loading-equalizer"), browser);
        Graphene.waitGui().until(isDotsElementPresent);

        return this;
    }

    public IndigoDashboardsPage waitForDrillModalDialogLoading() {
        // ensure dots element is present
        sleepTightInSeconds(1);

        Function<WebDriver, Boolean> isFadeElementPresent =
                browser -> !isElementPresent(className("gd-loading-equalizer-fade"), browser);
        Graphene.waitGui().until(isFadeElementPresent);

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
        dragAddKpiPlaceholder(DropZone.LAST);
        return this;
    }

    public IndigoDashboardsPage dragAddInsightPlaceholder() {
        dragAddInsightPlaceholder(DropZone.LAST);
        return this;
    }

    public IndigoDashboardsPage dragAddKpiPlaceholderNext() {
        dragAddKpiPlaceholder(DropZone.NEXT);
        return this;
    }

    public IndigoDashboardsPage dragAddKpiPlaceholder(DropZone dropZone) {
        // should fetch dashboard elements to avoid caching in view mode
        waitForElementVisible(cssSelector(ADD_KPI_PLACEHOLDER), getRoot());
        dragAndDropWithCustomBackend(browser, ADD_KPI_PLACEHOLDER, DASHBOARD_BODY, dropZone.getCss());

        return this;
    }

    public IndigoDashboardsPage dragAddInsightPlaceholder(DropZone dropZone) {
        // should fetch dashboard elements to avoid caching in view mode
        waitForElementVisible(cssSelector(ADD_INSIGHT_PLACEHOLDER), getRoot());
        dragAndDropWithCustomBackend(browser, ADD_INSIGHT_PLACEHOLDER, DASHBOARD_BODY, dropZone.getCss());

        return this;
    }

    public IndigoDashboardsPage tryToDragAndDropKPI(WebElement target) {
        waitForElementVisible(cssSelector(ADD_KPI_PLACEHOLDER), getRoot());
        dragAndDropWithCustomBackend(browser, addKpiPlaceHolder, target);
        return this;
    }

    public IndigoDashboardsPage dragAddAttributeFilterPlaceholder() {
        waitForElementVisible(cssSelector(ADD_ATTRIBUTE_FILTER_PLACEHOLDER), getRoot());
        dragAndDropWithCustomBackend(browser, ADD_ATTRIBUTE_FILTER_PLACEHOLDER, ADD_ATTRIBUTE_FILTER_DROPZONE, ADD_ATTRIBUTE_FILTER_DROPZONE);

        return this;
    }

    public IndigoDashboardsPage dragDateAttributeToFilterPlaceholder() {
        dragAndDropWithCustomBackend(browser, browser.findElement(By.className(DATE_FILTER_SELECTOR)),
                browser.findElement(By.cssSelector(ADD_ATTRIBUTE_FILTER_DROPZONE)));
        return this;
    }

    public IndigoDashboardsPage deleteAttributeFilter(String attribute) {
        String targetFilter = convertCSSClassTojQuerySelector(
                getAttributeFiltersPanel().getAttributeFilter(attribute).getRoot().getAttribute("class"));

        dragAndDropWithCustomBackend(browser, targetFilter, DASHBOARD_BODY, DELETE_DROPZONE);

        return this;
    }

    public List<String> getListCurrentAttributeFilter() {
        List<String> listAttribute = getAttributeFiltersPanel().getAttributeFilters()
                .stream().map(att -> att.getTitle()).collect(Collectors.toList());
        log.info("Attribute Filter List Is : " + listAttribute);
        return listAttribute;
    }

    public String getFirstAttributeFilter() {
        return getAttributeFiltersPanel().getDateAttributeFilters().getTitle();
    }

    /**
     * Add an insight to last position in dashboard by drag and drop mode
     *
     * @param insight
     * @return
     */
    public IndigoDashboardsPage addInsight(final String insight) {
        return this.addInsight(insight, DropZone.LAST);
    }

    /**
     * Add an insight to first next position in dashboard by drag and drop mode
     *
     * @param insight
     * @return
     */
    public IndigoDashboardsPage addInsightNext(final String insight) {
        return this.addInsight(insight, DropZone.NEXT);
    }

    public IndigoDashboardsPage addInsight(final String insight, DropZone dropZoneType) {
        dragAndDropWithCustomBackend(browser,
                convertCSSClassTojQuerySelector(
                        getInsightSelectionPanel().getInsightItem(insight).getRoot().getAttribute("class")),
                DASHBOARD_BODY, dropZoneType.getCss());

        return this;
    }

    public IndigoDashboardsPage addInsightToCreateSameRow(final String newInsight, String oldInsight, DropZone position) {
        WebElement source = getInsightItemOnSelectionPanel(newInsight);
        dragAndDropWidgetToCreateSameRow(source, getWidgetFluidLayout(oldInsight, 0), position, true);

        return this;
    }

    public IndigoDashboardsPage dragInsightToCreateSameRow(final String newInsight, String oldInsight, DropZone position) {
        WebElement source = getInsightItemOnSelectionPanel(newInsight);
        dragAndDropWidgetToCreateSameRow(source, getWidgetFluidLayout(oldInsight, 0), position, false);

        return this;
    }

    public IndigoDashboardsPage addInsightToCreateANewRow(final String newInsight, String oldInsight,
                                                          Widget.FluidLayoutPosition position) {
        WebElement source = getInsightItemOnSelectionPanel(newInsight);
        dragAndDropWidgetToCreateANewRow(source, getWidgetFluidLayout(oldInsight, 0), position, true);

        return this;
    }

    public IndigoDashboardsPage dragInsightToCreateANewRow(final String newInsight, String oldInsight,
                                                           Widget.FluidLayoutPosition position) {
        WebElement source = getInsightItemOnSelectionPanel(newInsight);
        dragAndDropWidgetToCreateANewRow(source, getWidgetFluidLayout(oldInsight, 0), position, false);

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

    public String getDashboardBodyText() {
        return waitForElementVisible(cssSelector(DASHBOARD_BODY), browser).getText();
    }

    public List<List<String>> getHeaderWidgetFluidLayout() {
        List<WebElement> fluidLayoutRows = getRoot().findElements(By.cssSelector(FLUID_LAYOUT_ROWS_CSS));
        if (fluidLayoutRows.isEmpty()) {
            return singletonList(EMPTY_LIST);
        }
        return fluidLayoutRows.stream()
                .filter(ElementUtils::isElementVisible)
                .map(tableRow -> tableRow.findElements(By.cssSelector(".s-fluid-layout-column .s-headline")))
                .map(cells -> cells.stream().map(WebElement::getText)
                        .collect(toList()))
                .collect(toList());
    }

    public List<Row> getRows() {
        return rows;
    }

    public WebElement getWidgetFluidLayout(String columnTitle) {
        return getWidgetFluidLayout(columnTitle, 0);
    }

    public String getWidthDashboardSectionKPIs() {
        return getRoot().getCssValue("width");
    }

    public WebElement getWidgetFluidLayout(String columnTitle, int cellIndex) {
        List<WebElement> fluidLayoutRows = getRoot().findElements(By.cssSelector(FLUID_LAYOUT_ROWS_CSS));

        List<List<WebElement>> elements = fluidLayoutRows.stream()
                .filter(ElementUtils::isElementVisible)
                .map(tableRow -> tableRow.findElements(By.xpath(
                        "//div[contains(concat(' ', @class, ' '), 's-dash-item')]" +
                                "/ancestor::div[contains(concat(' ', @class, ' '), 's-fluid-layout-column')]")))
                .map(cells -> cells.stream().collect(toList()))
                .collect(toList());

        List<String> listHeaders = getHeaderWidgetFluidLayout().stream()
                .flatMap(List::stream)
                .collect(toList());
        int columnIndex = listHeaders.indexOf(columnTitle);
        WebElement cell = elements.get(cellIndex).get(columnIndex);
        return cell;
    }

    public String getMinHeightLayoutPlaceHolder() {
        return getElementFluidLayoutPlaceHolder().findElement(
                By.xpath("//ancestor::div[@class='gd-fluidlayout-column-container last']")).getAttribute("style");
    }

    public String getAttributeClassFluidLayout() {
        return getElementFluidLayoutPlaceHolder().findElement(
                By.xpath("//ancestor::div[contains(@class,'s-fluid-layout-column')]")).getAttribute("class");
    }

    public void hoverOnResizerWidget() {
        WebElement hotspot = waitForElementPresent(By.className(HOTSPOT_WIDTH_RESIZER_CLASS_NAME), browser);
        String javaScript = "var evObj = document.createEvent('MouseEvents');" +
                "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" +
                "arguments[0].dispatchEvent(evObj);";

        ((JavascriptExecutor) browser).executeScript(javaScript, hotspot);
    }

    public IndigoDashboardsPage resizeWidthOfWidget(ResizeBullet resizeBullet) {
        String resizeBulletCss = String.format(RESIZE_BULLET_CSS, resizeBullet.getNumber());

        dragAndDropResizer(resizeBulletCss);
        return this;
    }

    public IndigoDashboardsPage resizeMinimumWidget() {
        String resizeBulletCss = String.format(RESIZE_BULLET_CSS, 1);

        dragAndDropResizer(resizeBulletCss);
        return this;
    }

    public IndigoDashboardsPage dragToResizeMinimumWidget() {
        String resizeBulletCss = String.format(RESIZE_BULLET_CSS, 1);

        dragAndDropResizer(resizeBulletCss, false);
        return this;
    }

    public IndigoDashboardsPage resizeMaximumWidget() {
        String resizeBulletCss = String.format(RESIZE_BULLET_CSS, 12);

        dragAndDropResizer(resizeBulletCss);
        return this;
    }

    public IndigoDashboardsPage dragToResizeMaximumWidget() {
        String resizeBulletCss = String.format(RESIZE_BULLET_CSS, 12);

        dragAndDropResizer(resizeBulletCss, false);
        return this;
    }

    public Boolean isDashboardTitleVisible() {
        return isElementVisible(className(DASHBOARD_TITLE_CLASS_NAME), getRoot());
    }

    public Boolean isDashboardFilterVisible() {
        return isElementVisible(className(DASHBOARD_FILTER_CLASS_NAME), getRoot());
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
        waitForElementNotPresent(className("is-dashboard-loading"));
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

    private WebElement getElementFluidLayoutPlaceHolder() {
        List<WebElement> fluidLayoutRows = getRoot().findElements(By.cssSelector(FLUID_LAYOUT_ROWS_CSS));
        return waitForCollectionIsNotEmpty(fluidLayoutRows).stream()
                .map(layoutRow -> layoutRow.findElement(By.cssSelector(".s-fluid-layout-column .can-drop.is-over")))
                .findFirst()
                .get();
    }

    private IndigoDashboardsPage setupConfigurationPanel(KpiConfiguration config) {
        ConfigurationPanel configurationPanel = getConfigurationPanel()
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

    private void dragAndDropWidgetToCreateSameRow(WebElement source, WebElement webElementWidget, DropZone position,
                                                  boolean isDrop) {
        Actions driverActions = new Actions(browser);
        driverActions.clickAndHold(source).perform();

        try {
            WebElement target = waitForElementVisible(saveButton);
            driverActions.moveToElement(target).perform();
            WebElement drop = waitForElementPresent(webElementWidget.findElement(By.xpath(position.getXpath())));

            scrollElementIntoView(drop, browser);
            if (position.equals(DropZone.PREV)) {
                ElementUtils.moveToElementActions(drop, 5, drop.getSize().width / 2).perform();
            } else {
                driverActions.moveToElement(drop).perform();
            }
        } finally {
            if (isDrop) driverActions.release().perform();
        }
    }

    private void dragAndDropWidgetToCreateANewRow(WebElement source, WebElement webElementWidget,
                                                  Widget.FluidLayoutPosition fluidLayoutPosition, boolean isDrop) {
        Actions driverActions = new Actions(browser);
        driverActions.clickAndHold(source).perform();

        try {
            WebElement target = waitForElementVisible(getInsightSelectionPanel().getRoot());
            moveToBottomOfElement(browser, target);

            WebElement drop = waitForElementPresent(webElementWidget.findElement(
                    By.xpath(fluidLayoutPosition.getXpath())));

            scrollElementIntoView(drop, browser);
            driverActions.moveToElement(drop).perform();
        } finally {
            if (isDrop) driverActions.release().perform();
        }
    }

    private WebElement getInsightItemOnSelectionPanel(String newInsight) {
        return waitForElementVisible(By.cssSelector(convertCSSClassTojQuerySelector(
                getInsightSelectionPanel().getInsightItem(newInsight).getRoot().getAttribute("class"))), browser);
    }

    /**
     * @param index : A color position in color palette
     * @return : A color position in color palette is selected
     */
    public String getColor(int index) {
        List<WebElement> list = waitForCollectionIsNotEmpty(getRoot()
                .findElements(By.cssSelector(".gd-base-visualization .highcharts-series rect")));
        return list.get(index).getAttribute("fill");
    }

    public String getNavigationDashboardWidth() {
        return getRoot().findElement(By.className("dash-nav")).getCssValue("width");
    }

    public String getDashboardContentWidth() {
        return getRoot().findElement(By.className("dashboard")).getCssValue("width");
    }

    public String getDistanceFromLeftToAlertDialogContent() {
        return browser.findElement(By.className("kpi-alert-dialog-overlay")).getCssValue("left");
    }

    public boolean isAlertDialogContentCenter(String navigatorWidth, String dashboardContentWidth, String dialogContentWidth, String distanceFromLeftToDialogContent) {
        return CalculateUtils.addWidth(CalculateUtils.convertToInteger(removePX(navigatorWidth)), CalculateUtils.halfOfWidth(removePX(dashboardContentWidth)))
                .compareTo(CalculateUtils.addWidth(CalculateUtils.halfOfWidth(removePX(dialogContentWidth)), CalculateUtils.convertToInteger(removePX(distanceFromLeftToDialogContent)))) == 0 ? true : false;
    }

    private String removePX(String value) {
        return value.replace("px", "");
    }

    public boolean isMinimizeSpacingChanged() {
        if (BrowserUtils.isFirefox()) {
            return isDashboardSectionChanged() && isDashboardItemContentChangedOnFirefox();
        } else {
            return isDashboardSectionChanged() && isDashboardItemContentChanged();
        }
    }

    private boolean isDashboardSectionChanged() {
        return getRoot().findElement(className("dash-section")).getCssValue("padding-left").contains("20px");
    }

    private boolean isDashboardItemContentChanged() {
        WebElement element = getDashboardItemContent();
        return element.getCssValue("padding").contains("8px")
                && element.getCssValue("border").contains("2px solid rgba(0, 0, 0, 0)");
    }

    private WebElement getDashboardItemContent() {
        return getRoot().findElement(className("dash-item-content"));
    }

    public WebElement getDropzonePosition() {
        return getRoot().findElement(cssSelector(ADD_ATTRIBUTE_FILTER_DROPZONE));
    }

    public IndigoDashboardsPage clickFilterShowAllOnFilterBar() {
        boolean isShowAllFilter = showAllFilterBtn.getText().equals("Show all");
        if (isShowAllFilter) showAllFilterBtn.click();
        return this;
    }

    public IndigoDashboardsPage clickFilterShowLessOnFilterBar() {
        boolean isShowLessFilter = showAllFilterBtn.getText().equals("Show less");
        if (isShowLessFilter) showAllFilterBtn.click();
        return this;
    }

    private boolean isDashboardItemContentChangedOnFirefox() {
        WebElement element = getDashboardItemContent();
        return element.getCssValue("padding-top").contains("8px") &&
                element.getCssValue("padding-left").contains("8px") &&
                element.getCssValue("padding-bottom").contains("8px") &&
                element.getCssValue("padding-right").contains("8px") &&
                element.getCssValue("border-top-width").contains("2px") &&
                element.getCssValue("border-left-width").contains("2px") &&
                element.getCssValue("border-bottom-width").contains("2px") &&
                element.getCssValue("border-right-width").contains("2px") &&
                element.getCssValue("border-top-style").contains("solid") &&
                element.getCssValue("border-left-style").contains("solid") &&
                element.getCssValue("border-bottom-style").contains("solid") &&
                element.getCssValue("border-right-style").contains("solid") &&
                element.getCssValue("border-top-color").contains("rgba(0, 0, 0, 0)") &&
                element.getCssValue("border-left-color").contains("rgba(0, 0, 0, 0)") &&
                element.getCssValue("border-bottom-color").contains("rgba(0, 0, 0, 0)") &&
                element.getCssValue("border-right-color").contains("rgba(0, 0, 0, 0)");
    }

    private void dragAndDropResizer(String dropSelector) {
        dragAndDropResizer(dropSelector, true);
    }

    private void dragAndDropResizer(String resizeBulletDropSelector, Boolean isDrop) {
        Actions driverActions = new Actions(browser);
        hoverOnResizerWidget();

        ElementUtils.moveToElementActions(waitForElementPresent(
                By.className(FLUID_LAYOUT_WIDTH_RESIZER_CLASS_NAME), browser), 0, 0).clickAndHold().perform();

        try {
            WebElement target = waitForElementPresent(By.className(DASHBOARD_TITLE_CLASS_NAME), browser);
            driverActions.moveToElement(target).perform();

            WebElement drop = waitForElementPresent(By.cssSelector(resizeBulletDropSelector), browser);
            driverActions.moveToElement(drop).perform();
        } finally {
            if (isDrop) {
                driverActions.release().perform();
            }
        }
    }

    private void waitForOpeningIndigoDashboard() {
        final By loadingLabel = className("gd-loading-equalizer");
        try {
            Function<WebDriver, Boolean> isLoadingLabelPresent = browser -> isElementPresent(loadingLabel, browser);
            Graphene.waitGui().withTimeout(2, TimeUnit.SECONDS).until(isLoadingLabelPresent);
        } catch (TimeoutException e) {
            //do nothing
        }

        waitForElementNotPresent(loadingLabel);
    }

    public SaveAsDialog saveAsNewOnLockMode() {
        waitForElementVisible(saveAsNewButton).click();
        return SaveAsDialog.getInstance(browser);
    }

    public String getMessageDisabledEmptyDashboardbutton () {
        if (isElementVisible(saveButton)){
            getActions().moveToElement(saveButton).perform();
            saveButton.click();
        }
        return waitForElementVisible(DISABLED_EMPTYDASHBOARDBUTTON_BUBBLE_MESSAGE, browser).getText();
    }
}
