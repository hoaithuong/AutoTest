package com.gooddata.qa.graphene.reports;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_OPPORTUNITY;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.RangeFilterItem.RangeType;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem.Ranking;
import com.gooddata.qa.graphene.entity.report.HowItem;
import com.gooddata.qa.graphene.entity.report.HowItem.Position;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.fragments.reports.report.ReportPage;

public class GoodSalesAddingFilterFromReportContextMenuTest extends GoodSalesAbstractTest {

    private static final String REPORT_NAME = "Report";

    @BeforeClass
    public void setProjectTitle() {
        projectTitle = "GoodSales-adding-filter-from-report-context-menu-test";
    }

    @Test(dependsOnGroups = "createProject")
    public void addAndRemoveReportFilterInSndDialog() {
        createReport(new UiReportDefinition()
                .withName(REPORT_NAME)
                .withWhats(METRIC_AMOUNT)
                .withHows(ATTR_OPPORTUNITY)
                .withHows(new HowItem(ATTR_YEAR_SNAPSHOT, Position.TOP, "2010")),
                "Report");

        reportPage.openFilterPanel();
        assertThat(reportPage.getFilters(), hasItem("Year (Snapshot) is 2010"));

        reportPage.openHowPanel()
                .selectAttribute(ATTR_YEAR_SNAPSHOT)
                .deleteFilterInSndDialog()
                .doneSndPanel();

        reportPage.saveReport();
        checkRedBar(browser);

        reportPage.openFilterPanel();
        assertThat(reportPage.getFilters(), not(hasItem("Year (Snapshot) is 2010")));
    }

    @Test(dependsOnMethods = "addAndRemoveReportFilterInSndDialog")
    public void addAttributeFilterFromAttributeHeader() {
        initReport().getTableReport()
                .openContextMenuFromCellValue("2010")
                .selectItem("Show only \"2010\"");
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        reportPage.openFilterPanel();
        assertThat(reportPage.getFilters(), hasItem("Year (Snapshot) is 2010"));

        reportPage.getTableReport()
                .openContextMenuFromCellValue("2010")
                .selectItem("Remove filter (Show All)");
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        reportPage.openFilterPanel();
        assertThat(reportPage.getFilters(), not(hasItem("Year (Snapshot) is 2010")));
    }

    @Test(dependsOnMethods = "addAndRemoveReportFilterInSndDialog")
    public void addRankingFilterFromMetricHeader() {
        initReport().getTableReport()
                .openContextMenuFromCellValue(METRIC_AMOUNT)
                .hoverToItem("Numbers in column")
                .addRankingFilter(Ranking.TOP, 6);
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        reportPage.openFilterPanel();
        assertThat(reportPage.getFilters(), hasItem("Top 6 Opportunity by Amount and Year (Snapshot) is 2010"));
    }

    @Test(dependsOnMethods = "addAndRemoveReportFilterInSndDialog")
    public void addRangeFilterFromAttributeHeader() {
        initReport().getTableReport()
                .openContextMenuFromCellValue("2010")
                .hoverToItem(METRIC_AMOUNT + " in 2010")
                .addRangeFilter(RangeType.IS_GREATER_THAN, 27000);
        waitForReportLoaded();

        reportPage.saveReport();
        checkRedBar(browser);

        reportPage.openFilterPanel();
        assertThat(reportPage.getFilters(), hasItem("Opportunity where Amount is greater than 27000 and "
                + "Year (Snapshot) is 2010"));
    }

    private ReportPage initReport() {
        initReportsPage();
        reportsPage.getReportsList().openReport(REPORT_NAME);
        waitForAnalysisPageLoaded(browser);

        return waitForFragmentVisible(reportPage);
    }

    private void waitForReportLoaded() {
        reportPage.getTableReport()
                .waitForReportLoading();
    }
}
