package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_LOST_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPEN_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_OPPORTUNITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_WON_OPPS;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_QUOTA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_SNAPSHOT_BOP;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTight;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.By.id;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.browser.BrowserUtils;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.indigo.analyze.common.GoodSalesAbstractAnalyseTest;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport;
import com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils;
import com.google.common.collect.Lists;

public class GoodSalesTableReportTest extends GoodSalesAbstractAnalyseTest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Table-Report-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void createTableReportWithMoreThan3Metrics() {
        List<String> headers = analysisPageReact.changeReportType(ReportType.TABLE)
            .addMetric(METRIC_NUMBER_OF_LOST_OPPS)
            .addMetric(METRIC_NUMBER_OF_OPEN_OPPS)
            .addMetric(METRIC_NUMBER_OF_OPPORTUNITIES)
            .addMetric(METRIC_NUMBER_OF_WON_OPPS)
            .addAttribute(ATTR_DEPARTMENT)
            .addAttribute(ATTR_PRODUCT)
            .waitForReportComputing()
            .getTableReport()
            .getHeaders()
            .stream()
            .map(String::toLowerCase)
            .collect(toList());
        assertEquals(headers, Stream.of(ATTR_DEPARTMENT, ATTR_PRODUCT, METRIC_NUMBER_OF_LOST_OPPS, METRIC_NUMBER_OF_OPEN_OPPS,
                METRIC_NUMBER_OF_OPPORTUNITIES, METRIC_NUMBER_OF_WON_OPPS).map(String::toLowerCase).collect(toList()));
        checkingOpenAsReport("createReportWithMoreThan3Metrics-tableReport");

        Stream.of(ReportType.COLUMN_CHART, ReportType.BAR_CHART, ReportType.LINE_CHART)
            .forEach(type -> {
                analysisPageReact.changeReportType(type);
                takeScreenshot(browser, "createReportWithMoreThan3Metrics-switchFromTableTo-" + type.name(),
                        getClass());
                assertFalse(analysisPageReact.waitForReportComputing()
                    .isExplorerMessageVisible());
                analysisPageReact.undo();
            });
    }

    @Test(dependsOnGroups = {"init"})
    public void checkReportContentWhenAdd3Metrics1Attribute() {
        TableReport report = analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addMetric(METRIC_QUOTA)
                .addMetric(METRIC_SNAPSHOT_BOP)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing()
                .getTableReport();
        sleepTight(3000);
        List<List<String>> analysisContent = report.getContent();

        analysisPageReact.exportReport();
        BrowserUtils.switchToLastTab(browser);

        assertEquals(analysisContent, getTableContentFromReportPage(Graphene.createPageFragment(
                com.gooddata.qa.graphene.fragments.reports.report.TableReport.class,
                waitForElementVisible(id("gridContainerTab"), browser))));
        checkRedBar(browser);

        browser.close();
        BrowserUtils.switchToFirstTab(browser);
    }

    
    @Test(dependsOnGroups = {"init"})
    public void createReportWithManyAttributes() {
        List<List<String>> adReportContent = analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"init"})
    public void filterReportIncludeManyAttributes() {
        analysisPageReact.changeReportType(ReportType.TABLE)
            .addAttribute(ATTR_ACTIVITY_TYPE)
            .addAttribute(ATTR_DEPARTMENT)
            .addMetric(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPageReact.getFilterBuckets().configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email")
            .configAttributeFilter(ATTR_DEPARTMENT, "Direct Sales");

        List<List<String>> adReportContent = analysisPageReact.waitForReportComputing()
            .getTableReport()
            .getContent();

        assertEquals(adReportContent, getTableReportContentInReportPage());
    }

    @Test(dependsOnGroups = {"init"})
    public void orderDataInTableReport() {
        List<List<String>> content = sortReportBaseOnHeader(
                analysisPageReact.changeReportType(ReportType.TABLE)
                    .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .waitForReportComputing()
                    .getTableReport(),
                METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("154,271")));

        content = sortReportBaseOnHeader(
                analysisPageReact.addMetric(METRIC_QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                METRIC_QUOTA);
        assertEquals(content, asList(asList("154,271", "$3,300,000")));

        content = sortReportBaseOnHeader(
                analysisPageReact.resetToBlankState()
                    .changeReportType(ReportType.TABLE)
                    .addAttribute(ATTR_ACTIVITY_TYPE)
                    .waitForReportComputing()
                    .getTableReport(),
                ATTR_ACTIVITY_TYPE);
        assertEquals(content, asList(asList("Web Meeting"), asList("Phone Call"), asList("In Person Meeting"),
                asList("Email")));

        content = sortReportBaseOnHeader(
                analysisPageReact.addAttribute(ATTR_DEPARTMENT)
                    .waitForReportComputing()
                    .getTableReport(),
                ATTR_DEPARTMENT);
        assertEquals(content, asList(asList("Email", "Direct Sales"), asList("In Person Meeting", "Direct Sales"),
                asList("Phone Call", "Direct Sales"), asList("Web Meeting", "Direct Sales"),
                asList("Email", "Inside Sales"), asList("In Person Meeting", "Inside Sales"),
                asList("Phone Call", "Inside Sales"), asList("Web Meeting", "Inside Sales")));

        content = sortReportBaseOnHeader(
                analysisPageReact.addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                    .addMetric(METRIC_QUOTA)
                    .waitForReportComputing()
                    .getTableReport(),
                METRIC_NUMBER_OF_ACTIVITIES);
        assertEquals(content, asList(asList("Phone Call", "Direct Sales", "33,420", "$3,300,000"),
                asList("Web Meeting", "Direct Sales", "23,931", "$3,300,000"),
                asList("In Person Meeting", "Direct Sales", "22,088", "$3,300,000"),
                asList("Email", "Direct Sales", "21,615", "$3,300,000"),
                asList("Phone Call", "Inside Sales", "17,360", "$3,300,000"),
                asList("In Person Meeting", "Inside Sales", "13,887", "$3,300,000"),
                asList("Email", "Inside Sales", "12,305", "$3,300,000"),
                asList("Web Meeting", "Inside Sales", "9,665", "$3,300,000")));
    }

    @Test(dependsOnGroups = {"init"})
    public void testFormat() throws ParseException, JSONException, IOException {
        initMetricPage();

        waitForFragmentVisible(metricPage).openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES);
        String oldFormat = waitForFragmentVisible(metricDetailPage).getMetricFormat();
        String metricUri = format("/gdc/md/%s/obj/14636", testParams.getProjectId());
        DashboardsRestUtils.changeMetricFormat(getRestApiClient(), metricUri, oldFormat + "[red]");

        try {
            initAnalysePage();

            com.gooddata.qa.graphene.fragments.indigo.analyze.reports.TableReport tableReport =
                    analysisPageReact.changeReportType(ReportType.TABLE)
                        .addMetric(METRIC_NUMBER_OF_ACTIVITIES).getTableReport();
            assertEquals(tableReport.getFormatFromValue(), "color: rgb(255, 0, 0);");
        } finally {
            DashboardsRestUtils.changeMetricFormat(getRestApiClient(), metricUri, oldFormat);
            initMetricPage();
            waitForFragmentVisible(metricPage).openMetricDetailPage(METRIC_NUMBER_OF_ACTIVITIES);
            assertEquals(metricDetailPage.getMetricFormat(), oldFormat);
        }
    }

    private List<List<String>> getTableContentFromReportPage(
            com.gooddata.qa.graphene.fragments.reports.report.TableReport tableReport) {
        List<List<String>> content = Lists.newArrayList();
        List<String> attributes = tableReport.getAttributeElements();
        List<String> metrics = tableReport.getRawMetricElements();
        int totalAttributes = attributes.size();
        int i = 0;
        for (String attr: attributes) {
            List<String> row = Lists.newArrayList(attr);
            for (int k = i; k < metrics.size(); k += totalAttributes) {
                row.add(metrics.get(k));
            }
            content.add(row);
            i++;
        }

        return content;
    }

    private List<List<String>> sortReportBaseOnHeader(TableReport report, String name) {
        report.sortBaseOnHeader(name);
        analysisPageReact.waitForReportComputing();
        return report.getContent();
    }

    private List<List<String>> getTableReportContentInReportPage() {
        analysisPageReact.exportReport();

        String currentWindowHandle = browser.getWindowHandle();
        for (String handle : browser.getWindowHandles()) {
            if (!handle.equals(currentWindowHandle))
                browser.switchTo().window(handle);
        }
        waitForAnalysisPageLoaded(browser);
        waitForFragmentVisible(reportPage);

        try {
            com.gooddata.qa.graphene.fragments.reports.report.TableReport report = reportPage.getTableReport();
            List<List<String>> attributesByRow = report.getAttributeElementsByRow();
            List<String> metrics = report.getRawMetricElements();

            for (int i = 0; i < metrics.size(); i++) {
                attributesByRow.get(i).add(metrics.get(i));
            }

            return attributesByRow;
        } finally {
            browser.close();
            browser.switchTo().window(currentWindowHandle);
        }
    }
}
