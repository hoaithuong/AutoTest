package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AnalysisPageHeader;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.AttributesBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_IS_WON;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_STAGE_NAME;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_BEST_CASE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_PERCENT_OF_GOAL;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class GoodSalesVisualizationTest extends AbstractAnalyseTest {

    private static final String EXPORT_ERROR_MESSAGE = "The insight is not compatible with Report Editor. "
            + "\"Stage Name\" is in configuration twice. Remove one attribute to open as a report.";

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Visualization-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        Metrics metricCreator = getMetricCreator();
        metricCreator.createAmountMetric();
        metricCreator.createNumberOfActivitiesMetric();
        metricCreator.createNumberOfLostOppsMetric();
        metricCreator.createNumberOfOpenOppsMetric();
        metricCreator.createNumberOfOpportunitiesBOPMetric();
        metricCreator.createNumberOfWonOppsMetric();
        metricCreator.createPercentOfGoalMetric();
        metricCreator.createQuotaMetric();
        metricCreator.createBestCaseMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testWithAttribute() {
        assertEquals(initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
                .getExplorerMessage(), "NO MEASURE IN YOUR INSIGHT");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
                .getExplorerMessage(), "NO MEASURE IN YOUR INSIGHT");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART)
                .getExplorerMessage(), "NO MEASURE IN YOUR INSIGHT");

        TableReport report = analysisPage.changeReportType(ReportType.TABLE)
                .waitForReportComputing().getTableReport();
        assertThat(report.getHeaders().stream().map(String::toLowerCase).collect(toList()),
                equalTo(singletonList(ATTR_ACTIVITY_TYPE.toLowerCase())));
        checkingOpenAsReport("testWithAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testResetFunction() {
        ChartReport report = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing().getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND),
                "See trend recommendation should display");
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE),
                "Compare recommendation should display");

        analysisPage.changeReportType(ReportType.BAR_CHART).waitForReportComputing();
        assertEquals(browser.findElements(RecommendationContainer.LOCATOR).size(), 0);

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertThat(report.getTrackersCount(), equalTo(4));

        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void disableExportForUnexportableVisualization() {
        final AnalysisPageHeader pageHeader = initAnalysePage().getPageHeader();
        ChartReport report = analysisPage.addMetric(METRIC_AMOUNT)
                .waitForReportComputing().getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        assertTrue(pageHeader.isExportButtonEnabled(), "Export button should be enabled");

        analysisPage.addAttribute(ATTR_STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        analysisPage.addStack(ATTR_STAGE_NAME).waitForReportComputing();
        assertEquals(report.getTrackersCount(), 8);

        assertFalse(pageHeader.isExportButtonEnabled(), "Export button shouldn't be enabled");
        assertEquals(pageHeader.getExportButtonTooltipText(), EXPORT_ERROR_MESSAGE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void resetSpecialReports() {
        initAnalysePage().resetToBlankState();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACCOUNT).waitForReportComputing();
        assertTrue(analysisPage.isExplorerMessageVisible(), "Explore message should display");
        assertEquals(analysisPage.getExplorerMessage(), "TOO MANY DATA POINTS TO DISPLAY");
        analysisPage.resetToBlankState();
    }

    @Test(dependsOnGroups = {"createProject"}, description = "https://jira.intgdc.com/browse/CL-6401")
    public void gridlinesShouldBeCheckedWhenExportBarChart() {
        initAnalysePage().addMetric(METRIC_AMOUNT)
                .addAttribute(ATTR_STAGE_NAME)
                .changeReportType(ReportType.BAR_CHART)
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
        assertTrue(gridlines.isSelected(), "Grid lines should be selected");

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkXssInMetricData() throws ParseException, JSONException, IOException {
        String oldFormat = initMetricPage().openMetricDetailPage(METRIC_PERCENT_OF_GOAL)
                .getMetricFormat();

        String uri = getMetricByTitle(METRIC_PERCENT_OF_GOAL).getUri();
        DashboardRestRequest dashboardRequest = new DashboardRestRequest(
                getAdminRestClient(), testParams.getProjectId());
        dashboardRequest.changeMetricFormat(uri, "<script> alert('test'); </script> #,##0.00");

        try {
            initAnalysePage();
            analysisPage.addMetric(METRIC_PERCENT_OF_GOAL)
                  .addAttribute(ATTR_IS_WON)
                  .addStack(ATTR_IS_WON)
                  .waitForReportComputing();
            ChartReport report = analysisPage.getChartReport();
            assertTrue(report.getTrackersCount() >= 1, "Trackers should display");
            assertEquals(report.getLegends(), singletonList("true"));

            assertEquals(report.getTooltipTextOnTrackerByIndex(0, 0),
                    asList(asList(ATTR_IS_WON, "true"), asList("true", "<script> alert('test')")));
        } finally {
            dashboardRequest.changeMetricFormat(uri, oldFormat);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportCustomDiscovery() {
        assertTrue(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing()
                .getPageHeader()
                .isExportButtonEnabled(), "Trackers should display");
        TableReport analysisReport = analysisPage.waitForReportComputing().getTableReport();
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

        Iterator<String> attributes = tableReport.waitForLoaded().getAttributeValues().iterator();
        Iterator<String> metrics = tableReport.getRawMetricValues().iterator();

        List<List<String>> content = new ArrayList<>();
        while (attributes.hasNext() && metrics.hasNext()) {
            content.add(asList(attributes.next(), metrics.next()));
        }

        assertThat(content, equalTo(analysisContent));

        List<String> headers = tableReport.getAttributeHeaders();
        headers.addAll(tableReport.getMetricHeaders());
        Iterator<String> reportheaders = headers.iterator();

        while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
            assertThat(reportheaders.next().toLowerCase(), equalTo(analysisHeaders.next().toLowerCase()));
        }
        checkRedBar(browser);

        browser.close();
        browser.switchTo().window(currentWindowHandle);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportVisualizationWithOneAttributeInChart() {
        assertEquals(initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE).getExplorerMessage(),
                "NO MEASURE IN YOUR INSIGHT");
        assertFalse(analysisPage.getPageHeader().isExportButtonEnabled(), "Export button shouldn't be enabled");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchReportHasOneMetricManyAttributes() {
        initAnalysePage().changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPage.getStacksBucket().getAttributeName(), ATTR_DEPARTMENT);
                assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
                assertEquals(analysisPage.getMetricsBucket().getWarningMessage(), type.getMetricMessage());
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasOneMetricManyAttributes-backToTable", getClass());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchReportHasManyMetricsManyAttributes() {
        initAnalysePage().changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addMetric(METRIC_QUOTA)
            .waitForReportComputing();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-" + type.name(), getClass());
                assertEquals(analysisPage.getAttributesBucket().getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
                assertEquals(analysisPage.getStacksBucket().getWarningMessage(), type.getStackByMessage());
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.COLUMN_CHART)
            .changeReportType(ReportType.TABLE);
        takeScreenshot(browser, "switchReportHasManyMetricsManyAttributes-backToTable", getClass());
        assertEquals(analysisPage.getAttributesBucket().getItemNames(), asList(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void switchReportWithDateAttributes() {
        final StacksBucket stacksBucket = initAnalysePage().getStacksBucket();
        final AttributesBucket categoriesBucket = analysisPage.getAttributesBucket();

        analysisPage.changeReportType(ReportType.TABLE)
            .addDate()
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-firstDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ATTR_ACTIVITY_TYPE);
                assertEquals(categoriesBucket.getItemNames(), singletonList(DATE));
                analysisPage.undo();
        });

        analysisPage.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addDate()
            .addAttribute(ATTR_DEPARTMENT);

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-secondDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), singletonList(DATE));

        analysisPage.resetToBlankState()
            .changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addDate();

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART)
            .forEach(type -> {
                analysisPage.changeReportType(type);
                takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + type.name(), getClass());
                assertEquals(stacksBucket.getAttributeName(), ATTR_DEPARTMENT);
                assertEquals(categoriesBucket.getItemNames(), singletonList(ATTR_ACTIVITY_TYPE));
                analysisPage.undo();
        });

        analysisPage.changeReportType(ReportType.LINE_CHART);
        takeScreenshot(browser, "switchReportWithDateAttributes-thirdDate-" + ReportType.LINE_CHART.name(),
                getClass());
        assertEquals(stacksBucket.getAttributeName(), ATTR_ACTIVITY_TYPE);
        assertEquals(categoriesBucket.getItemNames(), singletonList(DATE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addStackByIfMoreThanOneMetricInReport() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addMetric(METRIC_BEST_CASE).addAttribute("Region");

        final StacksBucket stacksBucket = analysisPage.getStacksBucket();
        assertTrue(stacksBucket.isDisabled(), "Stacks bucket should display");
        assertEquals(stacksBucket.getWarningMessage(), "TO STACK BY, AN INSIGHT CAN HAVE ONLY ONE MEASURE");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void addSecondMetricIfAttributeInStackBy() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE).addStack(ATTR_DEPARTMENT);
        assertEquals(analysisPage.getMetricsBucket().getWarningMessage(), "TO ADD ADDITIONAL MEASURE, REMOVE FROM STACK BY");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createChartReportWithMoreThan3Metrics() {
        List<String> legends = initAnalysePage().addMetric(METRIC_NUMBER_OF_LOST_OPPS)
                .addMetric(METRIC_NUMBER_OF_OPEN_OPPS)
                .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
                .addMetric(METRIC_NUMBER_OF_WON_OPPS)
                .waitForReportComputing()
                .getChartReport()
                .getLegends();
        assertEquals(legends, asList(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS, METRIC_NUMBER_OF_OPPORTUNITIES,
                METRIC_NUMBER_OF_WON_OPPS));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-chartReport");

        List<String> headers = analysisPage.changeReportType(ReportType.TABLE)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS,
                METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
    }
}
