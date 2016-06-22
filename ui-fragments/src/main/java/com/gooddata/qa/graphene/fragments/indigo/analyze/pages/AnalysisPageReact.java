package com.gooddata.qa.graphene.fragments.indigo.analyze.pages;

import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.*;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReportReact;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReportReact;
import com.google.common.base.Predicate;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.function.Supplier;

import static com.gooddata.qa.graphene.utils.WaitUtils.*;
import static org.openqa.selenium.By.className;
import static org.testng.Assert.assertTrue;

/**
 * many React fragments used
 */
public class AnalysisPageReact extends AbstractFragment {

    @FindBy(className = "adi-editor-header")
    private AnalysisPageHeaderReact pageHeader;

    @FindBy(className = "s-catalogue")
    private CataloguePanelReact cataloguePanel;

    @FindBy(className = "adi-editor-main")
    private MainEditorReact mainEditor;

    @FindBy(className = "s-visualization-picker")
    private VisualizationReportTypePickerReact reportTypePicker;

    @FindBy(className = "s-bucket-metrics")
    private MetricsBucket metricsBucket;

    @FindBy(className = "s-bucket-categories")
    private AttributesBucketReact attributesBucket;

    @FindBy(className = StacksBucket.CSS_CLASS)
    private StacksBucket stacksBucket;

    @FindBy(className = "s-bucket-filters")
    private FiltersBucketReact filterBuckets;

    public static final String MAIN_CLASS = "adi-editor";

    private static final By BY_TRASH_PANEL = className("s-trash");

    public AnalysisPageReact startDrag(WebElement source) {
        WebElement editor = waitForElementVisible(getRoot());

        Point location = editor.getLocation();
        Dimension dimension = editor.getSize();
        getActions().clickAndHold(source)
                .moveByOffset(location.x + dimension.width / 2, location.y + dimension.height / 2).perform();
        return this;
    }

    public AnalysisPageReact stopDrag(Point offset) {
        getActions().moveByOffset(offset.x, offset.y).release().perform();
        return this;
    }

    public AnalysisPageReact drag(WebElement source, Supplier<WebElement> target) {
        startDrag(source);
        try {
            getActions().moveToElement(target.get()).perform();
        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPageReact drag(WebElement source, WebElement target) {
        startDrag(source);
        try {
            if (!waitForElementVisible(target).getAttribute("class").contains("adi-droppable-active")) {
                getActions().moveToElement(target).perform();
                return this;
            }

            // In some specific cases, the target to be dropped is not in viewport,
            // so the selenium script when dragging and dropping element to
            // the target will have a risk that it cannot drop to the right position of target element
            // and element will not be droppable.
            // The solution is move element continuously until the target element is in viewport and droppable.
            Predicate<WebDriver> droppable = browser -> {
                getActions().moveToElement(target).perform();

                return target.getAttribute("class").contains("adi-droppable-hover");
            };

            Graphene.waitGui().until(droppable);

        } finally {
            getActions().release().perform();
        }
        return this;
    }

    public AnalysisPageReact addMetric(String metric) {
        return addMetric(metric, FieldType.METRIC);
    }

    public AnalysisPageReact addMetric(String data, FieldType type) {
        WebElement source = getCataloguePanel().searchAndGet(data, type);
        WebElement target = getMetricsBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addAttribute(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addDate() {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getAttributesBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addStack(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addFilter(String attribute) {
        WebElement source = getCataloguePanel().searchAndGet(attribute, FieldType.ATTRIBUTE);
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact addDateFilter() {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getFilterBuckets().getInvitation();
        return drag(source, target);
    }

    public AnalysisPageReact replaceMetric(String oldMetric, String newMetric) {
        WebElement source = getCataloguePanel().searchAndGet(newMetric, FieldType.METRIC);
        WebElement target = getMetricsBucket().get(oldMetric);
        return drag(source, target);
    }

    public AnalysisPageReact replaceAttribute(String oldAttr, String newAttr) {
        WebElement source = getCataloguePanel().searchAndGet(newAttr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPageReact replaceAttributeWithDate(String oldAttr) {
        WebElement source = getCataloguePanel().getDate();
        WebElement target = getAttributesBucket().get(oldAttr);
        return drag(source, target);
    }

    public AnalysisPageReact replaceAttribute(String attr) {
        WebElement source = getCataloguePanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getAttributesBucket().getFirst();
        return drag(source, target);
    }

    public AnalysisPageReact replaceStack(String attr) {
        WebElement source = getCataloguePanel().searchAndGet(attr, FieldType.ATTRIBUTE);
        WebElement target = getStacksBucket().get();
        return drag(source, target);
    }

    public AnalysisPageReact removeMetric(String metric) {
        WebElement header = getMetricsBucket().get(metric).findElement(By.className("s-bucket-item-header"));
        return drag(header ,
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeAttribute(String attr) {
        return drag(getAttributesBucket().get(attr).findElement(By.className("adi-bucket-item-header")),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeFilter(String attr) {
        return drag(getFilterBuckets().getFilter(attr),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeDateFilter() {
        return drag(getFilterBuckets().getDateFilter(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact removeStack() {
        return drag(getStacksBucket().get(),
                () -> waitForElementPresent(BY_TRASH_PANEL, browser));
    }

    public AnalysisPageReact changeReportType(ReportType type) {
        waitForFragmentVisible(reportTypePicker).setReportType(type);
        return this;
    }

    public boolean isReportTypeSelected(ReportType type) {
        return waitForFragmentVisible(reportTypePicker).isSelected(type);
    }

    public AnalysisPageReact resetToBlankState() {
        getPageHeader().resetToBlankState();
        assertTrue(isBlankState());
        return this;
    }

    public boolean isBlankState() {
        return getFilterBuckets().isEmpty() &&
            getMetricsBucket().isEmpty() &&
            getAttributesBucket().isEmpty() &&
            getStacksBucket().isEmpty() &&
            getMainEditor().isEmpty();
    }

    public AnalysisPageReact exportReport() {
        getPageHeader().exportReport();
        return this;
    }

    public String getExplorerMessage() {
        return getMainEditor().getExplorerMessage();
    }

    public boolean isExplorerMessageVisible() {
        return getMainEditor().isExplorerMessageVisible();
    }

    public AnalysisPageReact waitForReportComputing() {
        getMainEditor().waitForReportComputing();
        return this;
    }

    public boolean isReportComputing() {
        return getMainEditor().isReportComputing();
    }

    public AnalysisPageReact undo() {
        getPageHeader().undo();
        return this;
    }

    public AnalysisPageReact redo() {
        getPageHeader().redo();
        return this;
    }

    public TableReportReact getTableReport() {
        return getMainEditor().getTableReport();
    }

    public ChartReportReact getChartReport() {
        return getMainEditor().getChartReport();
    }

    public CataloguePanelReact getCataloguePanel() {
        return waitForFragmentVisible(cataloguePanel);
    }

    public MetricsBucket getMetricsBucket() {
        return waitForFragmentVisible(metricsBucket);
    }

    public AttributesBucketReact getAttributesBucket() {
        return waitForFragmentVisible(attributesBucket);
    }

    public StacksBucket getStacksBucket() {
        return waitForFragmentVisible(stacksBucket);
    }

    public FiltersBucketReact getFilterBuckets() {
        return waitForFragmentVisible(filterBuckets);
    }

    public MainEditorReact getMainEditor() {
        return waitForFragmentVisible(mainEditor);
    }

    public AnalysisPageHeaderReact getPageHeader() {
        return waitForFragmentVisible(pageHeader);
    }

    public AnalysisPageReact switchProject(String name) {
        log.info("Switching to project: " + name);

        Graphene.createPageFragment(Header.class,
                waitForElementVisible(By.className("gd-header"), browser))
                .switchProject(name);

        return this;
    }
}
