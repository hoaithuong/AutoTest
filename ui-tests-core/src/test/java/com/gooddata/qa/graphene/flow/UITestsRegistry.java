package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.dashboards.DashboardSavedFiltersTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesCascadingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesCellLimitTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesConnectingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardAllKindsFiltersTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDefaultFilterMultipleChoiceTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesDashboardWidgetManipulationTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesFilterDropdownAttributeValueTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesFilterGroupTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesAdvancedConnectingFilterTest;
import com.gooddata.qa.graphene.dashboards.GoodSalesKeyMetricTest;
import com.gooddata.qa.graphene.filters.DashboardFilterVisualTest;
import com.gooddata.qa.graphene.i18n.LocalizationTest;
import com.gooddata.qa.graphene.manage.AttributeLabelsTest;
import com.gooddata.qa.graphene.manage.GoodSalesFactTest;
import com.gooddata.qa.graphene.manage.GoodSalesManageObjectsTest;
import com.gooddata.qa.graphene.manage.GoodSalesMetricNumberFormatterTest;
import com.gooddata.qa.graphene.manage.GoodSalesViewModelVisualizationTest;
import com.gooddata.qa.graphene.manage.SimpleProjectGeoLabelTest;
import com.gooddata.qa.graphene.project.SimpleProjectEtlTest;
import com.gooddata.qa.graphene.reports.CopyReportTableTest;
import com.gooddata.qa.graphene.reports.DynamicImageTest;
import com.gooddata.qa.graphene.reports.GoodSalesAddingFilterFromReportContextMenuTest;
import com.gooddata.qa.graphene.reports.GoodSalesAdvanceRangeFilterReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesBasicFilterReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesCreateReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillDownToExportSpecialTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillReportInReportPageTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesDrillReportToExportTest;
import com.gooddata.qa.graphene.reports.GoodSalesGridModificationTest;
import com.gooddata.qa.graphene.reports.GoodSalesManipulationFilterReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportFilterTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportStatisticsTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportsPageTest;
import com.gooddata.qa.graphene.reports.GoodSalesReportsTest;
import com.gooddata.qa.graphene.reports.GoodSalesRunningTotalsTest;
import com.gooddata.qa.graphene.reports.GoodSalesSaveReportTest;
import com.gooddata.qa.graphene.reports.GoodSalesSortByTotalsTest;
import com.gooddata.qa.graphene.reports.GoodSalesTotalsInReportTest;
import com.gooddata.qa.graphene.reports.GoodsalesMufReportTest;
import com.gooddata.qa.graphene.reports.ReportWithEmptyValuesInTimeDimensionTest;
import com.gooddata.qa.graphene.reports.SimpleCompAttributesTest;
import com.gooddata.qa.graphene.reports.TimeFormattingTest;
import com.gooddata.qa.utils.flow.PredefineParameterTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object[]> suites = new HashMap<>();

        suites.put("basic", new Object[] {
            SimpleProjectEtlTest.class,
            GoodSalesDashboardTest.class,
            GoodSalesReportsTest.class,

            new PredefineParameterTest("testng-imap-GoodSales-email-schedule.xml")
                .param("GRAPHENE_USER", "gd.scheduledemail@gmail.com")
                .param("GRAPHENE_PASSWORD", "$CHECKLIST_SCHEDULED_EMAIL_USER_PASSWORD"),

            new PredefineParameterTest(LocalizationTest.class)
                .param("LANGUAGE_CODE", "fr-FR"),

            "testng-imap-project-n-users-sanity-test.xml"
        });

        suites.put("basic-vertica", new Object[] {
            new PredefineParameterTest(SimpleProjectEtlTest.class)
                .param("PROJECT_AUTHORIZATION_TOKEN", "$PROJECT_AUTHORIZATION_TOKEN2"),

            GoodSalesDashboardTest.class,
            GoodSalesReportsTest.class
        });

        suites.put("filters", new Object[] {
            GoodSalesDashboardAllKindsFiltersTest.class,
            GoodSalesFilterDropdownAttributeValueTest.class,
            GoodSalesCascadingFilterTest.class,
            GoodSalesConnectingFilterTest.class,
            GoodSalesFilterGroupTest.class,
            DashboardFilterVisualTest.class,
            DashboardSavedFiltersTest.class,
            GoodSalesAdvancedConnectingFilterTest.class
        });

        suites.put("default-filter-feature", new Object[] {
            GoodSalesDefaultFilterMultipleChoiceTest.class
        });

        suites.put("reports", new Object[] {
            GoodSalesDrillReportTest.class,
            GoodSalesDrillReportToExportTest.class,
            GoodSalesDrillDownToExportSpecialTest.class,
            GoodSalesDrillReportInReportPageTest.class,
            GoodSalesTotalsInReportTest.class,
            GoodSalesSortByTotalsTest.class,
            GoodSalesRunningTotalsTest.class,
            GoodSalesReportFilterTest.class,
            GoodSalesBasicFilterReportTest.class,
            GoodSalesManipulationFilterReportTest.class,
            GoodSalesAdvanceRangeFilterReportTest.class,
            GoodSalesAddingFilterFromReportContextMenuTest.class,
            GoodsalesMufReportTest.class,
            GoodSalesGridModificationTest.class,
            SimpleCompAttributesTest.class,
            GoodSalesReportStatisticsTest.class,
            CopyReportTableTest.class,
            GoodSalesReportsPageTest.class,
            GoodSalesSaveReportTest.class,
            GoodSalesCreateReportTest.class,
            ReportWithEmptyValuesInTimeDimensionTest.class,
            DynamicImageTest.class,
            TimeFormattingTest.class,
            "testng-permissions-EmbeddedReport.xml"
        });

        suites.put("dashboards", new Object[] {
            GoodSalesDashboardAllKindsFiltersTest.class,
            GoodSalesFilterDropdownAttributeValueTest.class,
            GoodSalesCascadingFilterTest.class,
            GoodSalesConnectingFilterTest.class,
            GoodSalesAdvancedConnectingFilterTest.class,
            GoodSalesFilterGroupTest.class,
            DashboardFilterVisualTest.class,
            GoodSalesDashboardWidgetManipulationTest.class,
            DashboardSavedFiltersTest.class,
            GoodSalesKeyMetricTest.class,
            "testng-permissions-PersonalObjectsInDashboardWidget.xml",
            "testng-permissions-EmbeddedDashboard.xml",
            "testng-permissions-ReportWidgetOnDashboard.xml"
        });

        suites.put("dashboards-cell-limit", new Object[] {
            GoodSalesCellLimitTest.class
        });

        suites.put("manage", new Object[] {
            GoodSalesManageObjectsTest.class,
            GoodSalesFactTest.class,
            GoodSalesViewModelVisualizationTest.class,
            GoodSalesMetricNumberFormatterTest.class,
            AttributeLabelsTest.class,
            SimpleProjectGeoLabelTest.class,
            "testng-manage-aggregation-metric-test.xml",
            "testng-manage-different-granularity-logical-metric-test.xml",
            "testng-manage-filter-share-ratio-metric-test.xml",
            "testng-manage-numeric-metric-test.xml",
            "testng-manage-non-UI-metric-test.xml",
            "testng-permissions-Variable.xml",
            "testng-permissions-Folder.xml",
            "testng-permissions-ComputedAttributes.xml"
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
