package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.CheckUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.utils.http.RestUtils.changeMetricFormat;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.GoodData;
import com.gooddata.md.Attribute;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.md.Restriction;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.entity.indigo.ReportDefinition;
import com.gooddata.qa.graphene.enums.indigo.CatalogFilterType;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesVisualizationTest extends AnalyticalDesignerAbstractTest {

    private static final String NUMBER_OF_WON_OPPS = "# of Won Opps.";
    private static final String PERCENT_OF_GOAL = "% of Goal";
    private static final String IS_WON = "Is Won?";

    private static final String EXPORT_ERROR_MESSAGE = "Visualization is not compatible with Report Editor. "
            + "\"Stage Name\" is in configuration twice. Remove one attribute to Open as Report.";

    private static final String PERCENT_OF_GOAL_URI = "/gdc/md/%s/obj/8136";

    private Project project;
    private MetadataService mdService;

    @SuppressWarnings("serial")
    private static final Map<String, String> walkmeContents = new HashMap<String, String>() {{
        put("Welcome to the Analytical Designer", "This interactive environment allows you to explore your data "
                + "and create visualizations quickly and easily. Intelligent on-screen recommendations help you "
                + "discover new and surprising insights. Let's get started!");

        put("Begin by exploring your data", "Measures represent quantitative data (values).\n\n"
                + "Attributes represent qualitative data (categories).\n\n"
                + "Date is a special item which represents all the dates in your project.");

        put("Create a new visualization", "Drag data from the list onto the canvas and watch as your "
                + "visualization takes shape!");

        put("Remove data", "Drag data items from these zones back to the list to remove them from your "
                + "visualization.");

        put("Change visualization type", "Choose how to visualize your data.");

        put("Filter your visualization", "Drag the Date field or any attribute here.");

        put("Save your visualization as a report", "When you are ready, open your visualization in the "
                + "Report Editor. From there you can save it and add it to a dashboard.");

        put("Clear your canvas", "Restart your exploration at any time.");

        put("You're ready.", "Go ahead. Start discovering what insights await in your data!");
    }};

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Indigo-GoodSales-Demo-Visualization-Test";
    }

    @Test(dependsOnMethods = {"enableAnalyticalDesigner"}, groups = {"turnOfWalkme"}, priority = 0)
    public void testWalkme() {
        initAnalysePage();

        final By title = By.className("walkme-custom-balloon-title");
        final By content = By.className("walkme-custom-balloon-content");
        final By nextBtn = By.className("walkme-action-next");
        final By backBtn = By.className("walkme-action-back");
        final By doneBtn = By.className("walkme-action-done");

        waitForElementVisible(title, browser);
        while (true) {
            assertEquals(waitForElementVisible(content, browser).getText(),
                    walkmeContents.get(waitForElementVisible(title, browser).getText()));

            waitForElementVisible(nextBtn, browser).click();

            if (!browser.findElements(doneBtn).isEmpty()) {
                break;
            }

            waitForElementVisible(backBtn, browser).click();
            assertEquals(waitForElementVisible(content, browser).getText(),
                    walkmeContents.get(waitForElementVisible(title, browser).getText()));
            waitForElementVisible(nextBtn, browser).click();
        }
        waitForElementVisible(doneBtn, browser).click();
        isWalkmeTurnOff = true;
    }

    @Test(dependsOnGroups = {"init"})
    public void testResetFunction() {
        initAnalysePage();

        ChartReport report = analysisPage.addMetric(NUMBER_OF_ACTIVITIES).getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addCategory(ACTIVITY_TYPE);
        assertThat(report.getTrackersCount(), equalTo(4));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void disableExportForUnexportableVisualization() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT));
        ChartReport report = analysisPage.getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertTrue(analysisPage.isExportToReportButtonEnabled());

        analysisPage.addCategory(STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        analysisPage.addStackBy(STAGE_NAME);
        assertEquals(report.getTrackersCount(), 8);

        assertFalse(analysisPage.isExportToReportButtonEnabled());
        assertEquals(analysisPage.getExportToReportButtonTooltipText(), EXPORT_ERROR_MESSAGE);
    }

    @Test(dependsOnGroups = {"init"})
    public void dontShowLegendWhenOnlyOneMetric() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT).withCategories(STAGE_NAME));
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 8);
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertFalse(report.isLegendVisible());

        analysisPage.changeReportType(ReportType.LINE_CHART);
        assertFalse(report.isLegendVisible());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInChartHasManyMetrics() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT, NUMBER_OF_ACTIVITIES));
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsHorizontal());
    }

    @Test(dependsOnGroups = {"init"})
    public void testLegendsInStackBy() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACTIVITY_TYPE)).addStackBy(DEPARTMENT);
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());

        analysisPage.changeReportType(ReportType.LINE_CHART).waitForReportComputing();
        assertTrue(report.isLegendVisible());
        assertTrue(report.areLegendsVertical());
    }

    @Test(dependsOnGroups = {"init"})
    public void showLegendForStackedChartWithOneSeries() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_WON_OPPS)).addStackBy(STAGE_NAME);
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertTrue(report.isLegendVisible());
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends.get(0), "Closed Won");

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        report = analysisPage.getChartReport();
        assertTrue(report.isLegendVisible());
        legends = report.getLegends();
        assertEquals(legends.size(), 1);
        assertEquals(legends.get(0), "Closed Won");
    }

    @Test(dependsOnGroups = {"init"})
    public void resetSpecialReports() {
        initAnalysePage();
        analysisPage.resetToBlankState();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(ACCOUNT));
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Too many data points to display");
        analysisPage.resetToBlankState();

        analysisPage.createReport(new ReportDefinition().withMetrics(NUMBER_OF_ACTIVITIES)
                .withCategories(STAGE_NAME));
        analysisPage.waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible());
        assertEquals(analysisPage.getExplorerMessage(), "Visualization cannot be displayed");
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"init"})
    public void testFilteringFieldsInCatalog() {
        initAnalysePage();
        analysisPage.searchBucketItem("am");
        analysisPage.filterCatalog(CatalogFilterType.METRICS_N_FACTS);
        assertTrue(Iterables.all(analysisPage.getAllCatalogFieldsInViewPort(), new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                String cssClass = input.getAttribute("class");
                return cssClass.contains(FieldType.METRIC.toString()) ||
                        cssClass.contains(FieldType.FACT.toString());
            }
        }));

        analysisPage.filterCatalog(CatalogFilterType.ATTRIBUTES);
        assertTrue(Iterables.all(analysisPage.getAllCatalogFieldsInViewPort(), new Predicate<WebElement>() {
            @Override
            public boolean apply(WebElement input) {
                return input.getAttribute("class").contains(FieldType.ATTRIBUTE.toString());
            }
        }));
    }

    @Test(dependsOnGroups = {"init"})
    public void testCreateReportWithFieldsInCatalogFilter() {
        initAnalysePage();
        analysisPage.filterCatalog(CatalogFilterType.METRICS_N_FACTS)
            .addMetric(AMOUNT)
            .filterCatalog(CatalogFilterType.ATTRIBUTES)
            .addCategory(STAGE_NAME)
            .waitForReportComputing();
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-7777")
    public void testAggregationFunctionList() {
        initAnalysePage();
        assertEquals(analysisPage.addMetricFromFact(AMOUNT)
            .expandMetricConfiguration("Sum of " + AMOUNT)
            .getAllMetricAggregations("Sum of " + AMOUNT),
            asList("Sum", "Average", "Minimum", "Maximum", "Median", "Running sum"));
    }

    @Test(dependsOnGroups = {"init"}, description = "https://jira.intgdc.com/browse/CL-6401")
    public void gridlinesShouldBeCheckedWhenExportBarChart() {
        initAnalysePage();
        analysisPage.createReport(new ReportDefinition().withMetrics(AMOUNT)
                .withCategories(STAGE_NAME).withType(ReportType.BAR_CHART))
                .waitForReportComputing()
                .exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);
        checkRedBar(browser);

        reportPage.showConfiguration();
        waitForElementVisible(By.cssSelector(".globalSettings .btnSilver"), browser).click();
        WebElement gridlines = waitForElementVisible(
                By.xpath("//input[./following-sibling::*[@title='Gridlines']]"), browser);
        assertTrue(gridlines.isSelected());

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void initGoodDataClient() {
        GoodData goodDataClient = getGoodDataClient();
        project = goodDataClient.getProjectService().getProjectById(testParams.getProjectId());
        mdService = goodDataClient.getMetadataService();
    }

    @Test(dependsOnMethods = {"initGoodDataClient"}, description = "https://jira.intgdc.com/browse/CL-6942")
    public void testCaseSensitiveSortInAttributeMetric() {
        initManagePage();
        String attribute = mdService.getObjUri(project, Attribute.class,
                Restriction.identifier("attr.product.id"));
        mdService.createObj(project, new Metric("aaaaA1", "SELECT COUNT([" + attribute + "])", "#,##0"));
        mdService.createObj(project, new Metric("AAAAb2", "SELECT COUNT([" + attribute + "])", "#,##0"));

        try {
            initAnalysePage();
            analysisPage.searchBucketItem("aaaa");
            assertEquals(analysisPage.getAllCatalogFieldNamesInViewPort(), asList("aaaaA1", "AAAAb2"));
        } finally {
            deleteMetric("aaaaA1");
            deleteMetric("AAAAb2");
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void checkXssInMetricAttribute() {
        String xssAttribute = "<button>" + IS_WON + "</button>";
        String xssMetric = "<button>" + PERCENT_OF_GOAL + "</button>";

        initAttributePage();
        waitForFragmentVisible(attributePage).initAttribute(IS_WON);
        waitForFragmentVisible(attributeDetailPage).renameAttribute(xssAttribute);

        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        waitForFragmentVisible(metricDetailPage).renameMetric(xssMetric);

        try {
            initAnalysePage();
            assertFalse(analysisPage.searchBucketItem("<button> test XSS </button>"));
            assertFalse(analysisPage.searchBucketItem("<script> alert('test'); </script>"));
            assertTrue(analysisPage.searchBucketItem("<button>"));
            assertEquals(analysisPage.getAllCatalogFieldNamesInViewPort(), asList(xssMetric, xssAttribute));

            StringBuilder expected = new StringBuilder(xssMetric).append("\n")
                    .append("Field Type\n")
                    .append("Calculated Measure\n")
                    .append("Defined As\n")
                    .append("select Won/Quota\n");
            assertEquals(analysisPage.getMetricDescription(xssMetric), expected.toString());

            expected = new StringBuilder(xssAttribute).append("\n")
                    .append("Field Type\n")
                    .append("Attribute\n")
                    .append("Values\n")
                    .append("false\n")
                    .append("true\n");
            assertEquals(analysisPage.getAttributeDescription(xssAttribute), expected.toString());

            analysisPage.createReport(new ReportDefinition().withMetrics(xssMetric).withCategories(xssAttribute))
                .waitForReportComputing();
            assertEquals(analysisPage.getAllAddedMetricNames(), asList(xssMetric));
            assertEquals(analysisPage.getAllAddedCategoryNames(), asList(xssAttribute));
            assertTrue(analysisPage.isFilterVisible(xssAttribute));
            assertEquals(analysisPage.getChartReport().getTooltipTextOnTrackerByIndex(0),
                    asList(asList(IS_WON, "true"), asList(xssMetric, "1,160.9%")));
        } finally {
            initAttributePage();
            waitForFragmentVisible(attributePage).initAttribute(xssAttribute);
            waitForFragmentVisible(attributeDetailPage).renameAttribute(IS_WON);

            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(xssMetric);
            waitForFragmentVisible(metricDetailPage).renameMetric(PERCENT_OF_GOAL);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void checkXssInMetricData() throws ParseException, JSONException, IOException {
        initMetricPage();
        waitForFragmentVisible(metricPage).openMetricDetailPage(PERCENT_OF_GOAL);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();

        String uri = format(PERCENT_OF_GOAL_URI, testParams.getProjectId());
        changeMetricFormat(getRestApiClient(), uri, "<script> alert('test'); </script> #,##0.00");

        try {
            initAnalysePage();
            analysisPage.createReport(new ReportDefinition().withMetrics(PERCENT_OF_GOAL)
                    .withCategories(IS_WON))
                  .addStackBy(IS_WON)
                  .waitForReportComputing();
            ChartReport report = analysisPage.getChartReport();
            assertTrue(report.getTrackersCount() >= 1);
            assertEquals(report.getLegends(), asList("true"));

            assertEquals(report.getTooltipTextOnTrackerByIndex(0),
                    asList(asList(IS_WON, "true"), asList("true", "<script> alert('test')")));
        } finally {
            changeMetricFormat(getRestApiClient(), uri, oldFormat);
        }
    }

    @Test(dependsOnGroups = {"init"})
    public void exportCustomDiscovery() {
        initAnalysePage();

        assertTrue(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
                .addCategory(ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .isExportToReportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPage.exportReport();
        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }

        com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport =
                Graphene.createPageFragment(
                        com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                        waitForElementVisible(By.id("gridContainerTab"), browser));

        Iterator<String> attributes = tableReport.getAttributeElements().iterator();

        sleepTight(2000); // wait for metric values is calculated and loaded
        Iterator<String> metrics = tableReport.getRawMetricElements().iterator();

        List<List<String>> content = new ArrayList<>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(asList(attributes.next(), metrics.next()));
        }

        assertThat(content, equalTo(analysisContent));

        List<String> headers = tableReport.getAttributesHeader();
        headers.addAll(tableReport.getMetricsHeader());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertThat(reportheaders.next().toLowerCase(), equalTo(analysisHeaders.next().toLowerCase()));
        }
        checkRedBar(browser);

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"init"})
    public void exportVisualizationWithOneAttributeInChart() {
        initAnalysePage();

        analysisPage.createReport(new ReportDefinition().withCategories(ACTIVITY_TYPE));
        assertEquals(analysisPage.getExplorerMessage(), "Now select a measure to display");
        assertFalse(analysisPage.isExportToReportButtonEnabled());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreDate() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(DATE).append("\n")
                .append("Represents all your dates in project. Can group by Day, Week, Month, Quarter & Year.\n")
                .append("Field Type\n")
                .append("Date\n");
        assertEquals(analysisPage.getTimeDescription(DATE), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreAttribute() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(DEPARTMENT).append("\n")
                .append("Field Type\n")
                .append("Attribute\n")
                .append("Values\n")
                .append("Direct Sales\n")
                .append("Inside Sales\n");
        assertEquals(analysisPage.getAttributeDescription(DEPARTMENT), expected.toString());
    }

    @Test(dependsOnGroups = {"init"})
    public void exploreMetric() {
        initAnalysePage();
        StringBuilder expected = new StringBuilder(NUMBER_OF_ACTIVITIES).append("\n")
                .append("Field Type\n")
                .append("Calculated Measure\n")
                .append("Defined As\n")
                .append("SELECT COUNT(Activity)\n");
        assertEquals(analysisPage.getMetricDescription(NUMBER_OF_ACTIVITIES), expected.toString());
    }

    private void deleteMetric(String metric) {
        initMetricPage();
        metricPage.openMetricDetailPage(metric);
        waitForFragmentVisible(metricDetailPage).deleteMetric();
        assertFalse(metricPage.isMetricVisible(metric));
    }
}
