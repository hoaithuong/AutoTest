package com.gooddata.qa.graphene.indigo.analyze.e2e;

import com.gooddata.sdk.model.md.Metric;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.analyze.reports.PivotTableReport;
import com.gooddata.qa.graphene.indigo.analyze.e2e.common.AbstractAdE2ETest;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class E2ePivotTableTest extends AbstractAdE2ETest {

    private String emptyMetricUri;
    private DashboardRestRequest dashboardRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle = "Pivot-Table-E2E-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        Metric metric;
        if (testParams.isReuseProject()) {
            metric = getMdService().getObj(getProject(), Metric.class, title("__EMPTY__"));
        } else {
            String activitiesUri = getMdService().getObjUri(getProject(), Metric.class, title("# of Activities"));
            metric = createMetric("__EMPTY__", "SELECT [" + activitiesUri + "] WHERE 1 = 0", "#,##0");
        }

        emptyMetricUri = metric.getUri();
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void it_should_be_dash_if_null_not_formatted() throws ParseException, JSONException, IOException {
        dashboardRequest.changeMetricFormat(emptyMetricUri, "#,##0");

        analysisPage = initAnalysePage().addMetric("__EMPTY__")
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        List<List<String>> expectedValues = Collections.singletonList(asList("–", "154,271"));
        assertEquals(pivotTableReport.getBodyContent(), expectedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void it_should_be_empty_if_formatted() throws ParseException, JSONException, IOException {
        dashboardRequest.changeMetricFormat(emptyMetricUri, "[=null] empty");

        initAnalysePage().addMetric("__EMPTY__")
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertEquals(pivotTableReport.getCellElementText("__EMPTY__", 0, 0), "empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_zeros_as_usual() throws ParseException, JSONException, IOException {
        dashboardRequest.changeMetricFormat(emptyMetricUri, "[=null] 0.00 $");

        initAnalysePage().addMetric("__EMPTY__")
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .changeReportType(ReportType.TABLE)
                .waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertEquals(pivotTableReport.getCellElementText("__EMPTY__", 0, 0), "0.00 $");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_show_table_correctly_when_filter_is_removed() {
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .addFilter(ATTR_ACTIVITY_TYPE)
                .getFilterBuckets()
                .configAttributeFilter(ATTR_ACTIVITY_TYPE, "Email");

        analysisPage.changeReportType(ReportType.TABLE)
                .getFilterBuckets()
                .configAttributeFilter(ATTR_ACTIVITY_TYPE, "All");
        analysisPage.waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertFalse(pivotTableReport.getCellElementText(METRIC_NUMBER_OF_ACTIVITIES, 0, 0).isEmpty(),
                "Cell shouldn't be empty");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_ordered_by_first_column_in_asc_by_default() {
        beforeOrderingTable();

        assertTrue(analysisPage.getPivotTableReport().isRowHeaderSortedUp(ATTR_ACTIVITY_TYPE));

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertEquals(pivotTableReport.getCellElement(0, 0).getText(), "Email");
        assertEquals(pivotTableReport.getCellElement(0, 1).getText(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_order_the_table_by_attribute() {
        beforeOrderingTable();

        analysisPage.getPivotTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPage.waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertEquals(pivotTableReport.getCellElement(0, 0).getText(), "Web Meeting");
        assertEquals(pivotTableReport.getCellElement(0, 1).getText(), "Phone Call");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_order_the_table_by_metric() {
        beforeOrderingTable();

        analysisPage.getPivotTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        List<Integer> values = newArrayList();
        IntStream.rangeClosed(0, 3).forEach(i -> values
            .add(unformatNumber(pivotTableReport.getCellElement(1, i).getText())));

        List<Integer> sortedValues = newArrayList(values);
        sortedValues.sort((a, b) -> b - a);

        assertEquals(values, sortedValues);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void clean_sorting_if_column_removed() {
        beforeOrderingTable();

        analysisPage.getPivotTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.removeMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
        assertTrue(analysisPage.getPivotTableReport().isRowHeaderSortedUp(ATTR_ACTIVITY_TYPE));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_shift_keep_sorting_on_metric_if_attribute_added() {
        beforeOrderingTable();

        analysisPage.getPivotTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.addAttribute(ATTR_DEPARTMENT)
                .waitForReportComputing();
        assertFalse(analysisPage.getPivotTableReport().isRowHeaderSortedUp(METRIC_NUMBER_OF_ACTIVITIES));
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_order_the_table_in_asc_order_if_same_column_clicked_twice() {
        beforeOrderingTable();

        analysisPage.getPivotTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPage.waitForReportComputing();
        analysisPage.getPivotTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPage.waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertEquals(pivotTableReport.getCellElement(0, 0).getText(), "Email");
        assertEquals(pivotTableReport.getCellElement(0, 1).getText(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_order_the_table_only_by_one_column_at_the_time() {
        beforeOrderingTable();

        analysisPage.getPivotTableReport().sortBaseOnHeader(METRIC_NUMBER_OF_ACTIVITIES);
        analysisPage.waitForReportComputing();
        analysisPage.getPivotTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPage.waitForReportComputing();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertEquals(pivotTableReport.getCellElement(0, 0).getText(), "Email");
        assertEquals(pivotTableReport.getCellElement(0, 1).getText(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_work_with_undo_redo() {
        beforeOrderingTable();

        analysisPage.getPivotTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);
        analysisPage.waitForReportComputing();
        analysisPage.getPivotTableReport().sortBaseOnHeader(ATTR_ACTIVITY_TYPE);

        analysisPage.waitForReportComputing().undo();

        PivotTableReport pivotTableReport = analysisPage.getPivotTableReport();
        assertEquals(pivotTableReport.getCellElement(0, 0).getText(), "Web Meeting");
        assertEquals(pivotTableReport.getCellElement(0, 1).getText(), "Phone Call");

        analysisPage.redo();

        assertEquals(pivotTableReport.getCellElement(0, 0).getText(), "Email");
        assertEquals(pivotTableReport.getCellElement(0, 1).getText(), "In Person Meeting");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_be_possible_to_drag_more_than_one_attribute_to_category() {
        assertEquals(initAnalysePage().changeReportType(ReportType.TABLE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addAttribute(ATTR_ACCOUNT)
                .addAttribute("Activity")
                .getAttributesBucket()
                .getItemNames()
                .size(), 3);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_move_stacks_to_categories_when_switching_to_table() {
        final AnalysisPage analysisPage = initAnalysePage().changeReportType(ReportType.COLUMN_CHART)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addStack(ATTR_ACCOUNT)
                .changeReportType(ReportType.TABLE);
        assertEquals(analysisPage
                .getAttributesBucket()
                .getItemNames()
                .size(), 1);
        assertEquals(analysisPage
                .getAttributesColumnsBucket()
                .getItemNames()
                .size(), 1);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void should_move_second_category_to_stacks_and_remove_to_rest_when_switching_to_chart() {
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addAttribute(ATTR_ACTIVITY_TYPE)
            .addStack(ATTR_ACCOUNT)
            .changeReportType(ReportType.TABLE)
            .changeReportType(ReportType.BAR_CHART);

        assertEquals(analysisPage.getAttributesBucket().getItemNames().size(), 1);
        assertFalse(analysisPage.getStacksBucket().isEmpty(), "Stacks bucket shouldn't be empty");
    }

    private void beforeOrderingTable() {
        initAnalysePage().changeReportType(ReportType.TABLE)
                .addAttribute(ATTR_ACTIVITY_TYPE)
                .addMetric(METRIC_NUMBER_OF_ACTIVITIES)
                .waitForReportComputing();
    }

    private Integer unformatNumber(String number) {
        return new Integer(number.replace(",", ""));
    }
}
