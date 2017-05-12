package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.FACT_AMOUNT;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class GoodSalesDateFilterTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Date-Filter-Test";
    }

    @Test(dependsOnGroups = {"init"}, description = "covered by TestCafe")
    public void checkDefaultValueInDateRange() {
        analysisPage.addDateFilter()
            .getFilterBuckets()
            .getFilter("Activity").click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));

        panel.changeToDateRangeSection();

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
        assertEquals(panel.getToDate(), getTimeString(date));

        date.add(Calendar.DAY_OF_MONTH, -29);
        assertEquals(panel.getFromDate(), getTimeString(date));
    }

    @Test(dependsOnGroups = {"init"}, description = "covered by TestCafe")
    public void switchingDateRangeNotComputeReport() {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter()
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText("Activity"), "Activity: All time");

        WebElement dateFilter = filtersBucketReact.getFilter("Activity");
        dateFilter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changeToDateRangeSection();
        assertFalse(analysisPage.isReportComputing());
        panel.changeToPresetsSection();
        assertFalse(analysisPage.isReportComputing());
        dateFilter.click();
        waitForFragmentNotVisible(panel);
    }

    @Test(dependsOnGroups = {"init"})
    public void allowFilterByRange() throws ParseException {
        final FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addDateFilter()
                .waitForReportComputing()
                .getChartReport();
        assertEquals(report.getTrackersCount(), 4);
        assertEquals(filtersBucketReact.getFilterText("Activity"), "Activity: All time");

        filtersBucketReact.configDateFilterByRangeButNotApply("01/12/2014", "01/12/2015");
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        waitForFragmentVisible(reportPage);
        takeScreenshot(browser, "allowDateFilterByRange-emptyFilters", getClass());
        assertTrue(reportPage.getFilters().isEmpty());
        browser.close();
        BrowserUtils.switchToFirstTab(browser);

        filtersBucketReact.configDateFilter("01/12/2014", "01/12/2015");
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 4);
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        waitForFragmentVisible(reportPage);
        List<String> filters = reportPage.getFilters();
        takeScreenshot(browser, "allowDateFilterByRange-dateFilters", getClass());
        assertEquals(filters.size(), 1);
        assertEquals(filters.get(0), "Date (Activity) is between 01/12/2014 and 01/12/2015");
        checkRedBar(browser);
        browser.close();
        BrowserUtils.switchToFirstTab(browser);
    }

    @Test(dependsOnGroups = {"init"}, description = "covered by TestCafe")
    public void testDateInCategoryAndDateInFilter() {
        assertTrue(analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addDate()
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() >= 1);
        assertEquals(analysisPage.getFilterBuckets().getFilterText("Activity"), "Activity: All time");
        assertEquals(analysisPage.getAttributesBucket().getAllGranularities(),
                Arrays.asList("Day", "Week (Sun-Sat)", "Month", "Quarter", "Year"));
        checkingOpenAsReport("testDateInCategoryAndDateInFilter");
    }

    @Test(dependsOnGroups = {"init"}, description = "covered by TestCafe")
    public void switchBetweenPresetsAndDataRange() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES).addDate().getFilterBuckets().configDateFilter("Last 90 days");
        analysisPage.waitForReportComputing();

        WebElement dateFilter = analysisPage.getFilterBuckets().getFilter("Activity");
        dateFilter.click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        panel.changeToDateRangeSection();
        assertFalse(analysisPage.isReportComputing());
        panel.configTimeFilter("01/14/2015", "04/13/2015");
        analysisPage.waitForReportComputing();

        dateFilter.click();
        panel.changeToPresetsSection();
        assertFalse(analysisPage.isReportComputing());
        panel.select("This month");
        analysisPage.waitForReportComputing();
        checkingOpenAsReport("switchBetweenPresetsAndDataRange");
    }

    @Test(dependsOnGroups = {"init"})
    public void showPercentAfterConfigDate() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addDate()
                    .getFilterBuckets()
                    .configDateFilter("Last 90 days");
        analysisPage.getMetricsBucket()
                    .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
                    .expandConfiguration()
                    .showPercents();
        analysisPage.waitForReportComputing();
        // wait for data labels rendered
        sleepTight(2000);

        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPage.getExplorerMessage());
            return;
        }

        ChartReport report = analysisPage.getChartReport();
        assertTrue(Iterables.all(report.getDataLabels(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.endsWith("%");
            }
        }));
        checkingOpenAsReport("showPercentAfterConfigDate");
    }

    @Test(dependsOnGroups = {"init"})
    public void popAfterConfigDate() {
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addDate()
                    .getFilterBuckets()
                    .configDateFilter("Last 90 days");

        analysisPage.getMetricsBucket()
            .getMetricConfiguration(METRIC_NUMBER_OF_ACTIVITIES)
            .expandConfiguration()
            .showPop();

        analysisPage.waitForReportComputing();
        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPage.getExplorerMessage());
            return;
        }

        ChartReport report = analysisPage.getChartReport();

        assertTrue(isEqualCollection(report.getLegends(),
                asList(METRIC_NUMBER_OF_ACTIVITIES + " - previous year", METRIC_NUMBER_OF_ACTIVITIES)));
        checkingOpenAsReport("popAfterConfigDate");
    }

    @Test(dependsOnGroups = {"init"}, description = "CL-9807: Problems with export of date filters")
    public void exportDateFilter() {
        final String dateFilterValue = "Last 4 quarters";
        analysisPage.addDateFilter()
                .getFilterBuckets()
                .configDateFilter(dateFilterValue);
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            final ReportFilter reportFilter = reportPage.openFilterPanel();
            takeScreenshot(browser, "export-date-filter", getClass());
            assertTrue(reportFilter.getFilterElement("Quarter/Year (Activity) is the last 4 quarters").isDisplayed(),
                    dateFilterValue + " filter is not displayed");
            browser.close();
        } finally {
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"init"},
            description = "CL-9980: Date filter isn't remained when adding trending from recommendation panel, " +
                    "covered by TestCafe")
    public void keepDateDimensionAfterApplyingSeeTrendRecommendation() {
        final String newDateDimension = "Created";
        analysisPage.addMetric(FACT_AMOUNT, FieldType.FACT).addDateFilter().getFilterBuckets()
                .changeDateDimension("Closed", newDateDimension);

        assertTrue(analysisPage.waitForReportComputing().getFilterBuckets().getDateFilterText()
                .startsWith(newDateDimension), "Date dimension was not changed to " + newDateDimension);

        Graphene.createPageFragment(RecommendationContainer.class,
                waitForElementVisible(RecommendationContainer.LOCATOR, browser))
                .getRecommendation(RecommendationStep.SEE_TREND).apply();

        analysisPage.waitForReportComputing();
        takeScreenshot(browser, "keep-date-dimension-after-applying-seetrend-recommendation", getClass());
        assertTrue(analysisPage.getFilterBuckets().getDateFilterText().startsWith(newDateDimension),
                "Date dimension was changed after user applied see trend recommendation");
    }

    @Test(dependsOnGroups = {"init"},
            description = "CL-9955: Date is changed to unrelated when adding percent for viz."
                    + " After this CL-10156, the metric and attribute combination is changed "
                    + "into # of Activities and Activity Type")
    public void keepDateRelationAfterAddingPercent() {
        final String expectedDate = "Activity: This quarter";
        analysisPage.addMetric(METRIC_NUMBER_OF_ACTIVITIES, FieldType.METRIC).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing();

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        analysisPage.waitForReportComputing();
        assertEquals(analysisPage.getFilterBuckets().getDateFilterText(), expectedDate,
                "Date was not displayed after applying compare recommendation");

        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        analysisPage.waitForReportComputing();

        assertTrue(
                analysisPage.getMetricsBucket().getMetricConfiguration("% " + METRIC_NUMBER_OF_ACTIVITIES)
                        .expandConfiguration().isShowPercentSelected(),
                "Percent was not added after using see percent recommendation");

        takeScreenshot(browser, "keep-date-relation-after-adding-percent", getClass());
        assertEquals(analysisPage.getFilterBuckets().getDateFilterText(), expectedDate,
                "Date has been changed after adding percent");
    }

    private String getTimeString(Calendar date) {
        StringBuilder timeBuilder = new StringBuilder();
        timeBuilder.append(String.format("%02d", date.get(Calendar.MONTH) + 1)).append("/");
        timeBuilder.append(String.format("%02d", date.get(Calendar.DAY_OF_MONTH))).append("/");
        timeBuilder.append(date.get(Calendar.YEAR));
        return timeBuilder.toString();
    }
}
