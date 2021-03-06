package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.fixture.ResourceManagement.ResourceTemplate.SINGLE_INVOICE;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.SP_YEAR_AGO;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static java.lang.String.format;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.gooddata.qa.graphene.enums.DateGranularity;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.CompareTypeDropdown;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.jboss.arquillian.graphene.Graphene;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.RecommendationStep;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.DateFilterPickerPanel;
import com.gooddata.qa.graphene.indigo.analyze.common.AbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.FiltersBucket;
import com.gooddata.qa.graphene.fragments.indigo.analyze.recommendation.RecommendationContainer;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.ChartReport;
import com.gooddata.qa.graphene.fragments.reports.filter.ReportFilter;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class DateFilterTest extends AbstractAnalyseTest {

    private static final String DATE_INVOICE = "templ:DateInvoice";
    private static final String METRIC_NUMBER_OF_PERSONS = "# Of attr:Persons";
    private static final String METRIC_NUMBER_OF_PERSONS_YEAR_AGO = METRIC_NUMBER_OF_PERSONS + SP_YEAR_AGO;
    private static final String ATTR_PERSON = "attr:Person";
    private static final String ATTR_INVOICE_ITEM = "attr:Invoice Item";

    @Override
    public void initProperties() {
        projectTitle += "Date-Filter-Test";
        appliedFixture = SINGLE_INVOICE;
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        new ProjectRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
            .setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_ANALYTICAL_DESIGNER_EXPORT, false);
        createMetricIfNotExist(new RestClient(getProfile(Profile.ADMIN)), METRIC_NUMBER_OF_PERSONS,
                format("SELECT COUNT([%s], [%s])",
                        getAttributeByTitle(ATTR_PERSON).getUri(),
                        getAttributeByTitle(ATTR_INVOICE_ITEM).getUri()), DEFAULT_METRIC_FORMAT);
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void checkDefaultValueInDateRange() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        initAnalysePage().addDateFilter()
                .getFilterBuckets()
                .getFilter(DATE_INVOICE).click();
        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));

        panel.selectStaticPeriod();
        assertEquals(panel.getToDate(), DateRange.now().format(dateTimeFormatter));
        assertEquals(panel.getFromDate(), DateRange.LAST_30_DAYS.getFrom().format(dateTimeFormatter));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applyFilterWithoutChange() {
        initAnalysePage().addDateFilter()
                .getFilterBuckets()
                .getFilter(DATE_INVOICE).click();

        DateFilterPickerPanel panel = Graphene.createPageFragment(DateFilterPickerPanel.class,
                waitForElementVisible(DateFilterPickerPanel.LOCATOR, browser));
        assertFalse(panel.isApplyButtonEnabled(), "Apply button should be disabled without change");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void allowFilterByRange() throws ParseException {
        final FiltersBucket filtersBucketReact = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
            .getFilterBuckets();

        ChartReport report = analysisPage.addMetric(METRIC_NUMBER_OF_PERSONS)
                .addAttribute(ATTR_PERSON)
                .addDateFilter()
                .waitForReportComputing()
                .getChartReport();

        filtersBucketReact.configDateFilterByRangeButNotApply("12/12/2016", "01/12/2017");
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        waitForFragmentVisible(reportPage);
        takeScreenshot(browser, "allowDateFilterByRange-emptyFilters", getClass());
        assertTrue(reportPage.getFilters().isEmpty(), "Should be empty filter");
        browser.close();
        BrowserUtils.switchToFirstTab(browser);

        filtersBucketReact.configDateFilter("12/12/2016", "01/12/2017");
        analysisPage.waitForReportComputing();
        assertEquals(report.getTrackersCount(), 13);
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            List<String> filters = reportPage.getFilters();
            takeScreenshot(browser, "allowDateFilterByRange-dateFilters", getClass());
            assertEquals(filters.size(), 1);
            assertEquals(filters.get(0), "Date (templ:DateInvoice) is between 12/12/2016 and 01/12/2017");
            checkRedBar(browser);
        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"}, description = "covered by TestCafe")
    public void testDateInCategoryAndDateInFilter() {
        assertTrue(initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_PERSONS)
                .addDate()
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount() >= 1, "Tracker should display");
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getFilterText(DATE_INVOICE)),
                asList("templ:DateInvoice", DateRange.ALL_TIME.toString()));
        assertEquals(analysisPage.getAttributesBucket().getAllGranularities(), asList(DateGranularity.DAY.toString(),
                DateGranularity.WEEK_SUN_SAT.toString(), DateGranularity.MONTH.toString(),
                DateGranularity.QUARTER.toString(), DateGranularity.YEAR.toString()));
        checkingOpenAsReport("testDateInCategoryAndDateInFilter");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void showPercentAfterConfigDate() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_PERSONS)
                .addDate()
                .getFilterBuckets()
                .configDateFilter(DateRange.LAST_90_DAYS.toString());
        analysisPage.getMetricsBucket()
                .getMetricConfiguration(METRIC_NUMBER_OF_PERSONS)
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
        }), "Missing data");
        checkingOpenAsReport("showPercentAfterConfigDate");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void applySamePeriodComparisonAfterConfigDate() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_PERSONS)
                .addDate()
                .getFilterBuckets()
                .configDateFilter(DateRange.LAST_90_DAYS.toString());

        analysisPage.getFilterBuckets()
                .openDateFilterPickerPanel()
                .applyCompareType(CompareTypeDropdown.CompareType.SAME_PERIOD_PREVIOUS_YEAR);

        analysisPage.waitForReportComputing();

        if (analysisPage.isExplorerMessageVisible()) {
            log.info("Visual cannot be rendered! Message: " + analysisPage.getExplorerMessage());
            return;
        }

        ChartReport report = analysisPage.getChartReport();

        assertEquals(report.getLegends(), asList(METRIC_NUMBER_OF_PERSONS_YEAR_AGO, METRIC_NUMBER_OF_PERSONS));
        checkingOpenAsReport("applySamePeriodComparisonAfterConfigDate");
    }

    @Test(dependsOnGroups = {"createProject"}, description = "CL-9807: Problems with export of date filters")
    public void exportDateFilter() {
        final String dateFilterValue = DateRange.LAST_4_QUARTERS.toString();
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addDateFilter()
                .getFilterBuckets()
                .configDateFilter(dateFilterValue);
        analysisPage.exportReport();
        BrowserUtils.switchToLastTab(browser);
        try {
            waitForAnalysisPageLoaded(browser);
            final ReportFilter reportFilter = reportPage.openFilterPanel();
            takeScreenshot(browser, "export-date-filter", getClass());
            assertTrue(reportFilter.getFilterElement("Quarter/Year (templ:DateInvoice) is the last 4 quarters").isDisplayed(),
                    dateFilterValue + " filter is not displayed");

        } finally {
            browser.close();
            BrowserUtils.switchToFirstTab(browser);
        }
    }

    @Test(dependsOnGroups = {"createProject"},
            description = "CL-9955: Date is changed to unrelated when adding percent for viz."
                    + " After this CL-10156, the metric and attribute combination is changed "
                    + "into # of Persons and attr:Person")
    public void keepDateRelationAfterAddingPercent() {

        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric(METRIC_NUMBER_OF_PERSONS, FieldType.METRIC).addAttribute(ATTR_PERSON)
                .waitForReportComputing();

        RecommendationContainer recommendationContainer =
                Graphene.createPageFragment(RecommendationContainer.class,
                        waitForElementVisible(RecommendationContainer.LOCATOR, browser));
        recommendationContainer.getRecommendation(RecommendationStep.COMPARE).apply();
        analysisPage.waitForReportComputing();

        final List<String> dateFilterTexts = parseFilterText(analysisPage.getFilterBuckets().getDateFilterText());
        final List<String> expectedDateFilterTexts = Arrays.asList(
                "templ:DateInvoice\n:\nThis quarter\nCompare (all) to", "Same period (SP) previous year");
        assertEquals(dateFilterTexts, expectedDateFilterTexts,
                "Date was not displayed after applying compare recommendation");

        // because no data
        FiltersBucket filtersBucketReact = analysisPage.getFilterBuckets();
        filtersBucketReact.configDateFilter("06/01/2020", "12/31/2020");
        analysisPage.waitForReportComputing();
        final List<String> expectedDateFilterAfterChangeTexts = Arrays.asList(
                "templ:DateInvoice\n:\nJun 1, 2020 - Dec 31, 2020\nCompare (all) to", "Same period (SP) previous year");
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getDateFilterText()), expectedDateFilterAfterChangeTexts,
                "Date has been changed after changing to static period");

        recommendationContainer.getRecommendation(RecommendationStep.SEE_PERCENTS).apply();
        analysisPage.waitForReportComputing();
        assertTrue(
                analysisPage.getMetricsBucket().getMetricConfiguration("% " + METRIC_NUMBER_OF_PERSONS)
                        .expandConfiguration().isShowPercentSelected(),
                "Percent was not added after using see percent recommendation");

        takeScreenshot(browser, "keep-date-relation-after-adding-percent", getClass());
        assertEquals(parseFilterText(analysisPage.getFilterBuckets().getDateFilterText()), expectedDateFilterAfterChangeTexts,
                "Date has been changed after adding percent");
    }
}
