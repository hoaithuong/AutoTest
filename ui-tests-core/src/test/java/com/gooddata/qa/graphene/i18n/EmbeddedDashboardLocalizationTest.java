package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EmbeddedDashboardLocalizationTest extends GoodSalesAbstractLocalizationTest {

    @BeforeClass(alwaysRun = true)
    public void setProjectTitle() {
        projectTitle = "GoodSales-embeded-dashboard-localization-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"precondition"})
    public void initEmbeddedDashboardUri() {
        embeddedUri = initDashboardsPage()
            .openEmbedDashboardDialog()
            .getPreviewURI()
            .replace("dashboard.html", "embedded.html");
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardPage() {
        initEmbeddedDashboard();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardSettingsDialog() {
        initEmbeddedDashboard()
            .editDashboard()
            .openDashboardSettingsDialog();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardScheduleDialog() {
        initEmbeddedDashboard()
            .showDashboardScheduleDialog();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkManagePage() {
        initEmbeddedDashboard()
            .openEmbeddedReportsPage();
        checkLocalization(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkReportPage() {
        initEmbeddedDashboard()
            .openEmbeddedReportPage();
        checkLocalization(browser);
    }
}
