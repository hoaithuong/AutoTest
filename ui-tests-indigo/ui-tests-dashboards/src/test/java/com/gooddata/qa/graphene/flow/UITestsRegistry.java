package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.indigo.dashboards.AddKpiWithoutDataSetTest;
import com.gooddata.qa.graphene.indigo.dashboards.DataSetTest;
import com.gooddata.qa.graphene.indigo.dashboards.DragWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.HeaderTest;
import com.gooddata.qa.graphene.indigo.dashboards.KpiAlertTest;
import com.gooddata.qa.graphene.indigo.dashboards.ManipulateWidgetsTest;
import com.gooddata.qa.graphene.indigo.dashboards.MetricsDropdownTest;
import com.gooddata.qa.graphene.indigo.dashboards.NonProductionDatasetTest;
import com.gooddata.qa.graphene.indigo.dashboards.PartialExportDashboardsTest;
import com.gooddata.qa.graphene.indigo.dashboards.ReorderInsightTest;
import com.gooddata.qa.graphene.indigo.dashboards.VisualizationsTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object[]> suites = new HashMap<>();

        suites.put("sanity", new Object[] {
            ManipulateWidgetsTest.class,
            "testng-desktop-permissions-EditMode.xml",
            "testng-desktop-imap-KpiAlertEvaluate.xml",
            "testng-desktop-permissions-SplashScreen.xml"
        });

        suites.put("pull-request", new Object[] {
            DataSetTest.class,
            HeaderTest.class,
            KpiAlertTest.class,
            ManipulateWidgetsTest.class,
            DragWidgetsTest.class,
            PartialExportDashboardsTest.class,
            AddKpiWithoutDataSetTest.class,
            MetricsDropdownTest.class,
            VisualizationsTest.class,
            ReorderInsightTest.class,
            NonProductionDatasetTest.class,
            "testng-desktop-AttributeFiltering.xml",
            "testng-desktop-DateFiltering.xml",
            "testng-desktop-permissions-EditMode.xml",
            "testng-desktop-permissions-KpiDrillTo.xml",
            "testng-desktop-KpiPop.xml",
            "testng-desktop-permissions-SplashScreen.xml",
            "testng-desktop-MetricFormatting.xml",
            "testng-desktop-ResponsiveNavigation.xml",
            "testng-desktop-KpiPopChangeValueExceedLimit.xml",
            "testng-desktop-permissions-MetricsAccessibility.xml",
            "testng-desktop-ProjectSwitch.xml",
            "testng-desktop-permissions-InsightOnDashboard.xml",
            "testng-desktop-EmptyErrorKpiValue.xml",
            "testng-mobile-AttributeFiltering.xml",
            "testng-mobile-DateFiltering.xml",
            "testng-mobile-EditMode.xml",
            "testng-mobile-KpiDrillTo.xml",
            "testng-mobile-KpiPop.xml",
            "testng-mobile-MetricFormatting.xml",
            "testng-mobile-ResponsiveNavigation.xml",
            "testng-mobile-SplashScreen.xml",
            "testng-mobile-KpiPopChangeValueExceedLimit.xml",
            "testng-mobile-ProjectSwitch.xml"
        });

        suites.put("all", new Object[] {
            DataSetTest.class,
            HeaderTest.class,
            KpiAlertTest.class,
            ManipulateWidgetsTest.class,
            DragWidgetsTest.class,
            PartialExportDashboardsTest.class,
            AddKpiWithoutDataSetTest.class,
            MetricsDropdownTest.class,
            VisualizationsTest.class,
            ReorderInsightTest.class,
            NonProductionDatasetTest.class,
            "testng-desktop-AttributeFiltering.xml",
            "testng-desktop-DateFiltering.xml",
            "testng-desktop-permissions-EditMode.xml",
            "testng-desktop-imap-KpiAlertEvaluate.xml",
            "testng-desktop-permissions-KpiDrillTo.xml",
            "testng-desktop-KpiPop.xml",
            "testng-desktop-permissions-SplashScreen.xml",
            "testng-desktop-MetricFormatting.xml",
            "testng-desktop-ResponsiveNavigation.xml",
            "testng-desktop-KpiPopChangeValueExceedLimit.xml",
            "testng-desktop-permissions-MetricsAccessibility.xml",
            "testng-desktop-ProjectSwitch.xml",
            "testng-desktop-imap-KpiAlertNullValue.xml",
            "testng-desktop-imap-KpiValueFormatInAlertEmail.xml",
            "testng-desktop-imap-KpiAlertSpecialCaseTest.xml",
            "testng-desktop-permissions-InsightOnDashboard.xml",
            "testng-desktop-EmptyErrorKpiValue.xml",
            "testng-mobile-AttributeFiltering.xml",
            "testng-mobile-DateFiltering.xml",
            "testng-mobile-EditMode.xml",
            "testng-mobile-KpiDrillTo.xml",
            "testng-mobile-KpiPop.xml",
            "testng-mobile-MetricFormatting.xml",
            "testng-mobile-ResponsiveNavigation.xml",
            "testng-mobile-SplashScreen.xml",
            "testng-mobile-KpiPopChangeValueExceedLimit.xml",
            "testng-mobile-ProjectSwitch.xml"
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
