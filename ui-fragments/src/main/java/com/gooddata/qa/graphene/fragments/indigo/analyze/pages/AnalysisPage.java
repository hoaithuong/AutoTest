package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributeFilterPickerPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.CatalogPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MainEditor;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricsBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.VisualizationReportTypePicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.ConfigurationPanelBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureAsColumnBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FilterBarPicker;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MeasureValueFilterPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.RankingFilterPanel;
import com.gooddata.qa.graphene.fragments.indigo.OptionalExportMenu.File;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.ElementUtils.BY_BUBBLE_CONTENT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForIndigoMessageDisappear;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForCollectionIsNotEmpty;
import static com.gooddata.qa.browser.BrowserUtils.dragAndDropWithCustomBackend;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertTrue;

/**
 * many React fragments used
 */
public class AnalysisPage extends AbstractFragment {

    @FindBy(className = "adi-editor-header")
    private AnalysisPageHeader pageHeader;

    @FindBy(className = "s-catalogue")
    private CatalogPanel catalogPanel;

    @FindBy(className = "adi-editor-main")
    private MainEditor mainEditor;

    @FindBy(className = "s-visualization-picker")
    private VisualizationReportTypePicker reportTypePicker;

    @FindBy(className = MEASURES_BUCKET_CLASS_NAME)
    private MetricsBucket metricsBucket;

    @FindBy(className = MEASURES_BUCKET_CLASS_NAME)
    private MeasureAsColumnBucket  measureAsColumnBucket;

    @FindBy(className = "s-bucket-secondary_measures")
    private MetricsBucket metricsSecondaryBucket;

    @FindBy(className = "s-bucket-tertiary_measures")
    private MetricsBucket metricsTertiaryBucket;

    @FindBy(css = ".s-bucket-attribute, .s-bucket-view, .s-bucket-trend")
    private AttributesBucket attributesBucket;

    @FindBy(className = "s-bucket-columns")
    private AttributesBucket attributesColumnsBucket;

    @FindBy(css = ".s-bucket-attribute-title, .s-bucket-view-title, .s-bucket-trend-title")
    private WebElement attributesBucketTitle;

    @FindBy(css = StacksBucket.CSS_SELECTOR)
    private StacksBucket stacksBucket;

    @FindBy(className = "s-bucket-filters")
    private FiltersBucket filterBuckets;

    @FindBy(className = "s-configuration-panel")
    private ConfigurationPanelBucket configurationPanelBucket;

    @FindBy(css = ".s-visualization-picker button")
    private List<WebElement> visualization;

    @FindBy(className = "s-filter-bar-button")
    private WebElement filterBarButton;

    @FindBy(className = "s-bucket-title")
    private List<WebElement> listAttributeBucketTitle;

    @FindBy(className = "s-bucket-location")
    private AttributesBucket locationBucket;

    @FindBy(className = "s-bucket-size")
    private AttributesBucket measureSizeBucket;

    @FindBy(className = "s-bucket-color")
    private AttributesBucket measureColorBucket;

    @FindBy(className = "adi-bucket-invitation-inner")
    private List<WebElement> bucketInvitationList;

    @FindBy(css = ".s-bucket-view .adi-bucket-invitation-inner span")
    private WebElement droptoSwitch;

    public static final String MAIN_CLASS = "adi-editor";
    private static final By BY_TRASH_PANEL = className("s-trash");
    private static final By BY_BUCKET_NOT_EMPTY = className("s-bucket-not-empty");
    private static final String MEASURES_BUCKET_CLASS_NAME = "s-bucket-measures";
    private static final By BY_ATTR_BUCKET_HEADER = className("adi-bucket-item-header");

    public static AnalysisPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(AnalysisPage.class,
                waitForElementVisible(className(MAIN_CLASS), context));
    }

    public AnalysisPage tryToDrag(WebElement source, WebElement target) {
        startDrag(source);
        getActions().moveToElement(target).release().perform();
        return this;
    }

    public AnalysisPage startDrag(WebElement source) {
        WebElement editor = waitForElementVisible(getRoot());

        Point location = editor.getLocation();
        Dimension dimension = editor.getSize();
        getActions().clickAndHold(source)
                .moveByOffset(location.x + dimension.width / 3, location.y + dimension.height / 3).perform();
        return this;
    }

    public AnalysisPage stopDrag(Point offset) {
        getActions().moveByOffset(offset.x, offset.y).release().perform();
        return this;
    }

    public AnalysisPage addMetricToRecommendedStepsPanelOnCanvas(String metricTitle) {
        return addMetricToRecommendedStepsPanelOnCanvas(metricTitle, FieldType.METRIC);
    }

    public AnalysisPage addMetricToRecommendedStepsPanelOnCanvas(String metricTitle, FieldType type) {
        WebElement source = getCatalogPanel().searchAndGet(metricTitle, type);
        Supplier<WebElement> recommendation = () ->
                waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);
        return drag(source, recommendation);
    }

    public AnalysisPage moveAttributeFromRowsToColumnsBucket(String attributeTitle) {
        WebElement target = getAttributesBucket().get(attributeTitle);
        WebElement source = getAttributesColumnsBucket().getInvitation();
        return drag(target, source);
    }

    public AnalysisPage drag(WebElement source, Supplier<WebElement> target) {
        startDrag(source);
        try {
            getActions().moveToElement(target.get()).moveByOffset(1,1).perform();
            Graphene.waitGui().until(browser -> !isElementPresent(className("s-loading"), target.get()));
        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPage drag(WebElement source, WebElement target) {
        startDrag(source);
        try {
            if (!waitForElementVisible(target).getAttribute("class").contains("adi-droppable-active")) {
                moveToBottomDropLineOfElement(target);
                return this;
            }

            // In some specific cases, the target to be dropped is not in viewport,
            // so the selenium script when dragging and dropping element to
            // the target will have a risk that it cannot drop to the right position of target element
            // and element will not be droppable.
            // The solution is move element continuously until the target element is in viewport and droppable.
            Function<WebDriver, Boolean> droppable = browser -> {
                getActions().moveToElement(target).perform();

                return target.getAttribute("class").contains("adi-droppable-hover");
            };

            Graphene.waitGui().until(droppable);

        } finally {
            getActions().release().perform();
            ElementUtils.makeSureNoPopupVisible();
        }
        return this;
    }

    public AnalysisPage addMetric(String metric) {
        return addMetric(metric, FieldType.METRIC);
    }

    public AnalysisPage addMetricByAttribute(String attribute) {
        return addMetric(attribute, FieldType.ATTRIBUTE);
    }

    public AnalysisPage addMetric(String data, FieldType type) {
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        WebElement target = getMetricsBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addMetricToSecondaryBucket(String metric) {
        return addMetricToSecondaryBucket(metric, FieldType.METRIC);
    }

    public AnalysisPage addMetricToTertiaryBucket(String metric) {
        return addCatalogItemToTertiaryBucket(metric, FieldType.METRIC);
    }

    public AnalysisPage addMetricToSecondaryBucket(String data, FieldType type) {
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        WebElement target = getMetricsSecondaryBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addCatalogItemToTertiaryBucket(String data, FieldType type) {
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        WebElement target = getMetricsTertiaryBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addAttribute(String attribute) {
        WebElement target = getAttributesBucket().getInvitation();
        return addAttribute(target, attribute);
    }

    public AnalysisPage addMetricToTertiaryBucket(String data, FieldType type) {
        WebElement target = getMetricsTertiaryBucket().getInvitation();
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        return drag(source, target);
    }

    public AnalysisPage addAttributeToLocationPushpin(String data, FieldType type) {
        WebElement target = getLocationBucket().getInvitation();
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        return drag(source, target);
    }

    public AnalysisPage addAttributeToMeasureSize(String data, FieldType type) {
        WebElement target = getMeasureSizeBucket().getInvitation();
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        return drag(source, target);
    }

    public AnalysisPage addAttributeToMeasureColor(String data, FieldType type) {
        WebElement target = getMeasureColorBucket().getInvitation();
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        return drag(source, target);
    }

    public AnalysisPage tryToDragAttributeToLocationPushpin(String data, FieldType type) {
        WebElement source = getCatalogPanel().searchAndGet(data, type);
        dragAndDropWithCustomBackend(browser, source, getMeasureColorBucket().getRoot());
        return this;
    }

    public AnalysisPage addColumnsAttribute(String attribute) {
        WebElement target = getAttributesColumnsBucket().getInvitation();
        return addAttribute(target, attribute);
    }

    public AnalysisPage addDate() {
        WebElement target = getAttributesBucket().getInvitation();
        return addDate(target);
    }

    public AnalysisPage addDateToColumnsAttribute() {
        WebElement target = getAttributesColumnsBucket().getInvitation();
        return addDate(target);
    }

    public AnalysisPage addStack(String attribute) {
        WebElement source = getCatalogPanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addFilter(String attribute) {
        WebElement source = getCatalogPanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addDateFilter() {
        WebElement source = getCatalogPanel().getDate();
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPage addMetricAfter(String metric, String newMetric) {
        WebElement source = getCatalogPanel().searchAndGet(newMetric, FieldType.METRIC);
        WebElement target = getMetricsBucket().get(metric);

        try {
            startDrag(source);
            getActions().moveToElement(target).perform();

            Graphene.waitGui().until(browser -> {
                getActions().moveByOffset(0, 5).perform();
                return isElementPresent(By.className("adi-drop-line-bottom"), target.findElement(BY_PARENT));
            });

        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPage replaceAttribute(String oldAttr, String newAttr) {
        WebElement source = getCatalogPanel().searchAndGet(newAttr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPage swapAttributeToStack(String attr, String stack) {
        WebElement source = getAttributesBucket().get(attr);
        WebElement target = getStacksBucket().get(stack);
        return drag(source, target);
    }

    public AnalysisPage reorderMetric(String sourceMetric, String targetMetric) {
        WebElement source = getMetricsBucket().get(sourceMetric);
        WebElement target = getMetricsBucket().get(targetMetric);
        return drag(source, target);
    }

    public AnalysisPage reorderSecondaryMetric(String sourceMetric, String targetMetric) {
        WebElement source = getMetricsBucket().get(sourceMetric);
        WebElement target = getMetricsSecondaryBucket().get(targetMetric);
        return drag(source, target);
    }

    public AnalysisPage reorderAttribute(String sourceAttribute, String targetAttribute) {
        WebElement source = getAttributesBucket().get(sourceAttribute);
        WebElement target = getAttributesBucket().get(targetAttribute);
        return drag(source, target);
    }

    public AnalysisPage reorderRowAndColumn(String sourceAttribute, String targetAttribute) {
        WebElement source = getStacksBucket().get(sourceAttribute);
        WebElement target = getAttributesBucket().get(targetAttribute);
        return drag(source, target);
    }

    public AnalysisPage replaceAttributeWithDate(String oldAttr) {
        WebElement source = getCatalogPanel().getDate();
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPage replaceAttribute(String attr) {
        WebElement source = getCatalogPanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().getFirst();
        return drag(source, target);
    }

    public AnalysisPage replaceStack(String attr) {
        WebElement source = getCatalogPanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().get();
        return drag(source, target);
    }

    public AnalysisPage removeMetric(String metric) {
        WebElement header = getMetricsBucket().get(metric).findElement(By.className("s-bucket-item-header"));
        return drag(header ,
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeSecondaryMetric(String metric) {
        WebElement header = getMetricsSecondaryBucket().get(metric).findElement(By.className("s-bucket-item-header"));
        return drag(header ,
            () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeAttribute(String attr) {
        return drag(getAttributesBucket().get(attr).findElement(By.className("adi-bucket-item-header")),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeFilter(String attr) {
        return drag(getFilterBuckets().getFilter(attr),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeMeasureFilter(String measure, int index) {
        return drag(getFilterBuckets().getFilter(measure, index),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeDateFilter() {
        return drag(getFilterBuckets().getDateFilter(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeStack() {
        return drag(getStacksBucket().get(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeColumn(String attr) {
        return drag(getAttributesColumnsBucket().get(attr).findElement(BY_ATTR_BUCKET_HEADER),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeGeoPushpin(String attr) {
        return drag(getLocationBucket().get(attr).findElement(BY_ATTR_BUCKET_HEADER),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeMetricsSecondaryBucket(String attr) {
        return drag(getMetricsSecondaryBucket().get(attr).findElement(BY_ATTR_BUCKET_HEADER),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeMeasureSizeBucket(String attr) {
        return drag(getMeasureSizeBucket().get(attr).findElement(BY_ATTR_BUCKET_HEADER),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage removeMeasureColorBucket(String attr) {
        return drag(getMeasureColorBucket().get(attr).findElement(BY_ATTR_BUCKET_HEADER),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPage changeReportType(ReportType type) {
        makeSureNoPopupVisible(BY_BUBBLE_CONTENT);
        waitForFragmentVisible(reportTypePicker).setReportType(type);
        return this;
    }

    public boolean isReportTypeSelected(ReportType type) {
        return waitForFragmentVisible(reportTypePicker).isSelected(type);
    }

    public AnalysisPage resetToBlankState() {
        getPageHeader().resetToBlankState();
        assertTrue(isBlankState(), "Should be blank state");
        return this;
    }

    public boolean isBlankState() {
        return getFilterBuckets().isEmpty()
                && getMetricsBucket().isEmpty()
                && getAttributesBucket().isEmpty()
                && getAttributesColumnsBucket().isEmpty()
                && getMainEditor().isEmpty()
                && getPageHeader().isBlankState();
    }

    public AnalysisPage exportReport() {
        getPageHeader().exportReport();
        return this;
    }

    public AnalysisPage exportTo(File file) {
        clickOptionsButton().exportTo(file);
        return this;
    }

    public OptionalExportMenu clickOptionsButton() {
        return getPageHeader().clickOptionsButton();
    }

    public String getExplorerMessage() {
        return getMainEditor().getExplorerMessage();
    }

    public boolean isExplorerMessageVisible() {
        return getMainEditor().isExplorerMessageVisible();
    }

    public AnalysisPage waitForReportComputing() {
        getMainEditor().waitForReportComputing();
        return this;
    }

    public boolean isReportComputing() {
        return getMainEditor().isReportComputing();
    }

    public AnalysisPage undo() {
        getPageHeader().undo();
        return this;
    }

    public AnalysisPage redo() {
        getPageHeader().redo();
        return this;
    }

    public AnalysisPage cancel() {
        getPageHeader().cancel();
        return this;
    }

    public AnalysisPage clear() {
        getPageHeader().getResetButton().click();
        return this;
    }

    public PivotTableReport getPivotTableReport() {
        return getMainEditor().getPivotTableReport();
    }

    public ChartReport getChartReport() {
        return getMainEditor().getChartReport();
    }

    public CatalogPanel getCatalogPanel() {
        return waitForFragmentVisible(catalogPanel).waitForItemLoaded();
    }

    public MetricsBucket getMetricsBucket() {
        return waitForFragmentVisible(metricsBucket);
    }

    //using this fragment when applying the Headline insight(Headline Report Type) for Analysis Page
    public MetricsBucket getMetricsSecondaryBucket() {
        return waitForFragmentVisible(metricsSecondaryBucket);
    }

    public MetricsBucket getMetricsTertiaryBucket() {
        return waitForFragmentVisible(metricsTertiaryBucket);
    }

    public MeasureAsColumnBucket getMeasureAsColumnBucketBucket() {
        return waitForFragmentVisible(measureAsColumnBucket);
    }

    public AttributesBucket getAttributesBucket() {
        return waitForFragmentVisible(attributesBucket);
    }

    public AttributesBucket getLocationBucket() {
        return waitForFragmentVisible(locationBucket);
    }

    public AttributesBucket getMeasureSizeBucket() {
        return waitForFragmentVisible(measureSizeBucket);
    }

    public AttributesBucket getMeasureColorBucket() {
        return waitForFragmentVisible(measureColorBucket);
    }

    //To use this function when applying the pivot table
    public AttributesBucket getAttributesColumnsBucket() {
        return waitForFragmentVisible(attributesColumnsBucket);
    }

    public StacksBucket getStacksBucket() {
        return waitForFragmentVisible(stacksBucket);
    }

    public FiltersBucket getFilterBuckets() {
        return waitForFragmentVisible(filterBuckets);
    }

    public MainEditor getMainEditor() {
        return waitForFragmentVisible(mainEditor);
    }

    public AnalysisPageHeader getPageHeader() {
        return waitForFragmentVisible(pageHeader);
    }

    public List<String> getListVisualization() {
        return waitForCollectionIsNotEmpty(visualization).stream()
                .map(e -> e.getAttribute("class").replaceAll("\\s(.*)",""))
                .collect(Collectors.toList());
    }

    public List<String> getListAttributeBucketTitle() {
        return waitForCollectionIsNotEmpty(listAttributeBucketTitle).stream().map(e -> e.getText().toUpperCase())
                .collect(Collectors.toList());
    }

    public List<String> getListAttributeBucketInvitation() {
        return waitForCollectionIsNotEmpty(bucketInvitationList).stream().map(e -> e.getText().toUpperCase())
                .collect(Collectors.toList());
    }

    public AnalysisPage switchProject(String name) {
        log.info("Switching to project: " + name);

        Graphene.createPageFragment(Header.class,
                waitForElementVisible(By.className("gd-header"), browser))
                .switchProject(name);

        return this;
    }

    public boolean searchInsight(final String insight) {
        return getPageHeader().expandInsightSelection().searchInsight(insight);
    }

    public AnalysisPage openInsight(final String insight) {
        getPageHeader().expandInsightSelection().openInsight(insight);
        getPageHeader().waitForOpenEnabled();
        return this;
    }

    public AnalysisPage saveInsight() {
        getPageHeader().saveInsight();
//        waitForIndigoMessageDisappear(browser);
        return this;
    }

    public boolean isSaveInsightEnabled() {
        return getPageHeader().isSaveButtonEnabled();
    }

    public boolean isUndoInsightEnabled() {
        return getPageHeader().isUndoButtonEnabled();
    }

    public boolean isRedoInsightDisabled() {
        return getPageHeader().isRedoButtonDisabled();
    }

    public boolean isRedoInsightEnabled() {
        return getPageHeader().isRedoButtonEnabled();
    }

    public boolean isResetInsightEnabled() {
        return getPageHeader().isResetButtonEnabled();
    }

    public boolean isCancelInsightEnabled() {
        return getPageHeader().isCancelButtonEnabled();
    }

    public AnalysisPage saveInsight(final String insight) {
        getPageHeader().saveInsight(insight);
        waitForIndigoMessageDisappear(browser);
        return this;
    }

    public AnalysisPage saveInsightAs(final String insight) {
        getPageHeader().saveInsightAs(insight);
        waitForIndigoMessageDisappear(browser);
        return this;
    }

    public AnalysisPage setInsightTitle(final String title) {
        getPageHeader().setInsightTitle(title);
        return this;
    }

    public AnalysisPage waitForNonEmptyBuckets() {
        waitForElementVisible(BY_BUCKET_NOT_EMPTY, browser);
        return this;
    }

    public AnalysisPage setFilterIsNotValues(String filter, String... values) {
        AttributeFilterPickerPanel panel = openFilterPanel(filter);
        panel.checkAllCheckbox();
        Arrays.stream(values).forEach(panel::selectItem);  //To uncheck element
        panel.getApplyButton().click();
        return this;
    }

    public FilterBarPicker openFilterBarPicker() {
        if (!waitForElementVisible(filterBarButton).getAttribute("class").contains("is-active")) {
            filterBarButton.click();
        }
        return FilterBarPicker.getInstance(browser);
    }

    public boolean isFilterBarButtonEnabled() {
        return !waitForElementVisible(filterBarButton).getAttribute("class").contains("disabled");
    }

    public AnalysisPage setFilterIsValues(String filter, String... values) {
        openFilterPanel(filter).select(values);
        return this;
    }

    public MeasureValueFilterPanel openMeasureFilterPanel(String measure, Integer index) {
        getFilterBuckets().getFilter(measure, index).click();
        return MeasureValueFilterPanel.getInstance(browser);
    }

    private AttributeFilterPickerPanel openFilterPanel(String filter) {
        getFilterBuckets().getFilter(filter).click();
        return AttributeFilterPickerPanel.getInstance(browser);
    }

    public RankingFilterPanel openRankingFilterPanel(String filter) {
        getFilterBuckets().getFilter(filter).click();
        return RankingFilterPanel.getInstance(browser);
    }

    private void moveToBottomDropLineOfElement(WebElement target) {
        String browserName = ((RemoteWebDriver) BrowserUtils.getBrowserContext()).getCapabilities()
                .getBrowserName().toLowerCase();

        Function<WebDriver, Boolean> droppable = browser -> {
            if ("chrome".equals(browserName)) {
                getActions().moveToElement(target, 1, target.getSize().height / 2 + 1).perform();
            } else {
                getActions().moveToElement(target).perform();
            }

            return isElementPresent(By.className("adi-drop-line-bottom"), getRoot());
        };

        Graphene.waitGui().until(droppable);
    }

    private AnalysisPage addAttribute(WebElement typeAttribute, String attribute) {
        WebElement source = getCatalogPanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        //To avoid move target out of bounds of viewport, should scroll element into view
        Point location = typeAttribute.getLocation();
        Dimension dimension = typeAttribute.getSize();
        if ((location.y + dimension.height) > getRoot().getSize().height) {
            getActions().click(attributesBucketTitle).sendKeys(Keys.END).perform();
        }
        return drag(source, typeAttribute);
    }

    private AnalysisPage addDate(WebElement typeAttribute) {
        WebElement source = getCatalogPanel().getDate();
        return drag(source, typeAttribute);
    }

    private void makeSureNoPopupVisible(By popupElement) {
        ElementUtils.moveToElementActions(getRoot(), 1, 1).perform();
        ElementUtils.moveToElementActions(getFilterBuckets().getFiltersLabel(), 1, 1).click().perform();
        Function<WebDriver, Boolean> isDismissed = context -> !isElementVisible(popupElement, context);
        Graphene.waitGui().until(isDismissed);
    }

    public AnalysisPage resetColorPicker() {
        openConfigurationPanelBucket().openColorConfiguration().resetColor();
        return this;
    }

    public String getDeprecatedMessage(){
        WebElement webElement = waitForElementVisible(browser.findElement(By.cssSelector(".gd-message-text div:first-child")));
        return webElement.getText();
    }

    public boolean isDialogDropdownBoxVisible() {
        return isElementVisible(className("gd-color-drop-down"), browser);
    }

    public ConfigurationPanelBucket openConfigurationPanelBucket() {
        return waitForFragmentVisible(configurationPanelBucket).expandConfigurationPanel();
    }

    public MetricsBucket getMeasureConfigurationPanelBucket() {
        return waitForFragmentVisible(metricsBucket).expandMeasureConfigurationPanel();
    }

    public boolean isCustomColorPaletteDialogVisible() {
        return isElementVisible(By.className("color-picker-container"), browser);
    }

    public StacksBucket getStackConfigurationPanelBucket() {
        return waitForFragmentVisible(stacksBucket).expandStackConfigurationPanel();
    }

    public String checkColorItems() {
        openConfigurationPanelBucket().openColorConfiguration();
        return waitForElementPresent(cssSelector(".gd-color-unsupported"), browser).getText();
    }

    public boolean isDisableOpenAsReport() {
        return waitForElementVisible(cssSelector(".s-open_as_report"), getRoot()).getAttribute("class").contains("disabled");
    }

    public boolean isReportNotFound() {
        return isElementVisible(By.className("s-report-message-type-report_not_found"), root);
    }

    public void dragAndHold(WebElement source, WebElement target) {
        getActions().clickAndHold(source).moveToElement(target).perform();
    }

    public void releaseElement(WebElement target) {
        getActions().moveToElement(target).release().perform();
    }
    
    public WebElement waitForDropToSwitchVisible () {
        return waitForElementVisible(droptoSwitch);
    }
}
