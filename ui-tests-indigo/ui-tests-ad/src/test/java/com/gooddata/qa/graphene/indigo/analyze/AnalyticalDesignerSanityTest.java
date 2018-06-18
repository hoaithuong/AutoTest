package com.gooddata.qa.graphene.indigo.analyze;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.indigo.ShortcutPanel;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.MetricConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.ComparisonRecommendation;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AnalyticalDesignerSanityTest extends AbstractAnalyseTest {

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Indigo-GoodSales-Demo-Sanity-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createSnapshotBOPMetric();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testWithAttribute() {
        assertEquals(initAnalysePage().addAttribute(ATTR_ACTIVITY_TYPE)
                .getExplorerMessage(), "NO MEASURE IN YOUR INSIGHT");

        assertEquals(analysisPage.changeReportType(ReportType.BAR_CHART)
                .getExplorerMessage(), "NO MEASURE IN YOUR INSIGHT");

        assertEquals(analysisPage.changeReportType(ReportType.LINE_CHART)
                .getExplorerMessage(), "NO MEASURE IN YOUR INSIGHT");

        TableReport report = analysisPage
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing()
                .getTableReport();
        assertThat(report.getHeaders().stream().map(String::toLowerCase).collect(toList()),
                equalTo(asList(ATTR_ACTIVITY_TYPE.toLowerCase())));
        checkingOpenAsReport("testWithAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dragMetricToColumnChartShortcutPanel() {
        WebElement metric = initAnalysePage().getCataloguePanel()
                .searchAndGet(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC);

        Supplier<WebElement> recommendation = () ->
            waitForElementPresent(ShortcutPanel.AS_A_COLUMN_CHART.getLocator(), browser);

        ChartReport report = analysisPage.drag(metric, recommendation)
                    .waitForNonEmptyBuckets()
                    .waitForReportComputing()
                    .getChartReport();

        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.SEE_TREND));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertTrue(browser.findElements(RecommendationContainer.LOCATOR).size() == 0);

        analysisPage.addAttribute(ATTR_ACTIVITY_TYPE).waitForReportComputing();
        assertThat(report.getTrackersCount(), equalTo(4));
        checkingOpenAsReport("dragMetricToColumnChartShortcutPanel");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSimpleContribution() {
        ChartReport report = initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer
                .isRecommendationVisible(RecommendationStep.SEE_PERCENTS));
        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        assertTrue(analysisPage.waitForReportComputing().isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 4);

        MetricConfiguration metricConfiguration = analysisPage.getMetricsBucket()
                .getMetricConfiguration("% " + METRIC_NUMBER_OF_ACTIVITIES)
                .expandConfiguration();
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());

        assertTrue(analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing()
                .isReportTypeSelected(ReportType.BAR_CHART));
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(metricConfiguration.isShowPercentEnabled());
        assertTrue(metricConfiguration.isShowPercentSelected());
        checkingOpenAsReport("testSimpleContribution");
    }
    @Test(dependsOnGroups = {"createProject"})
    public void testSimpleComparison() {
        ChartReport report = initAnalysePage()
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 1);
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        ComparisonRecommendation comparisonRecommendation =
                recommendationContainer.getRecommendation(RecommendationStep.COMPARE);
        comparisonRecommendation.select(ATTR_ACTIVITY_TYPE).apply();
        assertTrue(analysisPage.waitForReportComputing().getAttributesBucket().getItemNames()
                .contains(ATTR_ACTIVITY_TYPE));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ATTR_ACTIVITY_TYPE), ATTR_ACTIVITY_TYPE + ":\nAll");
        assertEquals(report.getTrackersCount(), 4);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));

        analysisPage.replaceAttribute(ATTR_ACTIVITY_TYPE, ATTR_DEPARTMENT).waitForReportComputing();
        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(ATTR_DEPARTMENT));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ATTR_DEPARTMENT), ATTR_DEPARTMENT + ":\nAll");
        assertEquals(report.getTrackersCount(), 2);
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        checkingOpenAsReport("testSimpleComparison");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void displayWhenDraggingFirstMetric() {
        WebElement metric = initAnalysePage().getCataloguePanel()
                .searchAndGet(METRIC_SNAPSHOT_BOP, FieldType.METRIC);

        Supplier<WebElement> trendRecommendation = () ->
            waitForElementPresent(ShortcutPanel.TRENDED_OVER_TIME.getLocator(), browser);

        analysisPage.drag(metric, trendRecommendation)
            .waitForNonEmptyBuckets()
            .waitForReportComputing();

        assertTrue(analysisPage.getAttributesBucket().getItemNames().contains(DATE));
        assertTrue(analysisPage.getFilterBuckets().isFilterVisible("Activity"));
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: Last 4 quarters");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("displayWhenDraggingFirstMetric");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void exportCustomDiscovery() {
        assertTrue(initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing()
                .getPageHeader()
                .isExportButtonEnabled());
        TableReport analysisReport = analysisPage.getTableReport();
        List<List<String>> analysisContent = analysisReport.getContent();
        Iterator<String> analysisHeaders = analysisReport.getHeaders().iterator();

        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);

        try {
            waitForAnalysisPageLoaded(browser);

            com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport =
                    reportPage.getTableReport();

            assertThat(tableReport.getDataContent(), equalTo(analysisContent));

            List<String> headers = tableReport.getAttributeHeaders();
            headers.addAll(tableReport.getMetricHeaders());
            Iterator<String> reportheaders = headers.iterator();

            while (analysisHeaders.hasNext() && reportheaders.hasNext()) {
                assertThat(reportheaders.next().toLowerCase(), equalTo(analysisHeaders.next().toLowerCase()));
            }
            checkRedBar(browser);
        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void filterOnDateAttribute() throws ParseException {
        final FiltersBucket filtersBucket = initAnalysePage().getFilterBuckets();

        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter()
                .waitForReportComputing();
        assertEquals(analysisPage.getChartReport().getTrackersCount(), 4);
        assertEquals(filtersBucket.getDateFilterText(), "Activity: All time");

        filtersBucket.configDateFilter("01/01/2016", "01/01/2017");
        analysisPage.waitForReportComputing();
        assertEquals(filtersBucket.getFilterText("Activity"), "Activity: Jan 1, 2016 - Jan 1, 2017");
        assertTrue(analysisPage.getChartReport().getTrackersCount() >= 1);
        checkingOpenAsReport("filterOnDateAttribute");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void testSimplePoP() throws ParseException {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .addDate()
            .getFilterBuckets()
            .configDateFilter("01/01/2012", "12/31/2012");

        assertTrue(analysisPage.getFilterBuckets()
                .isFilterVisible(ATTR_ACTIVITY));
        assertEquals(analysisPage.getFilterBuckets().getFilterText(ATTR_ACTIVITY),
                "Activity: Jan 1, 2012 - Dec 31, 2012");
        ChartReport report = analysisPage.waitForReportComputing().getChartReport();
        assertThat(report.getTrackersCount(), equalTo(1));
        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        assertTrue(recommendationContainer.isRecommendationVisible(RecommendationStep.COMPARE));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        analysisPage.waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        List<String> legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES));

        analysisPage.addMetric(METRIC_SNAPSHOT_BOP).waitForReportComputing();
        assertTrue(report.getTrackersCount() >= 1);
        legends = report.getLegends();
        assertEquals(legends.size(), 2);
        assertEquals(legends, asList(METRIC_NUMBER_OF_ACTIVITIES, METRIC_SNAPSHOT_BOP));
        checkingOpenAsReport("testSimplePoP");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void dropAttributeToReportHaveOneMetric() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE);
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 4);

        analysisPage.addStack(ATTR_DEPARTMENT);
        assertEquals(analysisPage.waitForReportComputing().getChartReport().getTrackersCount(), 8);
    }
}
