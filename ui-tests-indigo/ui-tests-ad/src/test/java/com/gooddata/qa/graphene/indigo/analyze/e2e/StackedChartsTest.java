package com.gooddata.qa.graphene.indigo.analyze.e2e;

import static com.gooddata.qa.graphene.utils.ElementUtils.isElementPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static java.util.Arrays.asList;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.internals.StacksBucket;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;

public class StackedChartsTest extends AbstractAdE2ETest {

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle = "Stacked-Charts-E2E-Test";
    }

    @Test(dependsOnGroups = {"init"})
    public void should_put_stack_by_attribute_into_color_series() {
        initAnalysePageByUrl();

        assertEquals(analysisPage.addStack(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing()
            .getChartReport()
            .getLegends(), asList("Email", "In Person Meeting", "Phone Call", "Web Meeting"));
    }

    @Test(dependsOnGroups = {"init"})
    public void should_show_totals_for_stacked_columns() {
        initAnalysePageByUrl();

        analysisPage.addStack(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .waitForReportComputing();

        assertEquals(browser.findElements(cssSelector(".highcharts-stack-labels text")).size(), 2);
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_stack_warn_msg_when_there_is_something_in_stack_by_bucket() {
        initAnalysePageByUrl();

        assertFalse(analysisPage.addStack(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .getMetricsBucket()
            .getWarningMessage()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_display_stack_warn_msg_if_there_is_more_than_1_metrics() {
        initAnalysePageByUrl();

        assertFalse(analysisPage.addMetric(NUMBER_OF_ACTIVITIES)
            .addMetric(NUMBER_OF_LOST_OPPS)
            .addAttribute(ACCOUNT)
            .getStacksBucket()
            .getWarningMessage()
            .isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disappear_when_visualization_is_switched_to_table_and_should_be_empty_when_going_back() {
        initAnalysePageByUrl();

        analysisPage.addStack(ACTIVITY_TYPE)
            .addAttribute(DEPARTMENT)
            .addMetric(NUMBER_OF_ACTIVITIES)
            .changeReportType(ReportType.TABLE);

        assertFalse(isElementPresent(className(StacksBucket.CSS_CLASS), browser));

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertFalse(analysisPage.getMetricsBucket().isEmpty());
        assertFalse(analysisPage.getStacksBucket().isEmpty());
        assertFalse(analysisPage.getAttributesBucket().isEmpty());
    }

    @Test(dependsOnGroups = {"init"})
    public void should_disappear_when_switched_to_table_via_result_too_large_link() {
        initAnalysePageByUrl();

        analysisPage.addStack(ACTIVITY_TYPE)
            .addAttribute(ACCOUNT)
            .addMetric(NUMBER_OF_ACTIVITIES);

        waitForElementVisible(cssSelector(".s-error-too-many-data-points .s-switch-to-table"), browser).click();

        analysisPage.changeReportType(ReportType.BAR_CHART);
        assertFalse(analysisPage.getMetricsBucket().isEmpty());
        assertFalse(analysisPage.getStacksBucket().isEmpty());
        assertFalse(analysisPage.getAttributesBucket().isEmpty());
    }
}
