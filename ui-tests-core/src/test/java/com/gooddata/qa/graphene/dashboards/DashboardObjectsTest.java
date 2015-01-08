package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.qa.graphene.common.CheckUtils.checkRedBar;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.enums.DashFilterTypes;
import com.gooddata.qa.graphene.enums.TextObject;
import com.gooddata.qa.graphene.enums.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;

@Test(groups = {"dashboardObjects"}, description = "Tests for simple project and dashboard objects functionality in GD platform")
public class DashboardObjectsTest extends AbstractProjectTest {

    private final String variableName = "FVariable";
    private static final long expectedDashboardExportSize = 65000L;

    @BeforeClass
    public void initStartPage() {
        projectTitle = "SimpleProject-test-dashboard-objects";
    }

    @Test(dependsOnMethods = {"createProject"})
    public void uploadDataTest() throws InterruptedException {
        String csvFilePath = testParams.loadProperty("csvFilePath");
        uploadCSV(csvFilePath + "payroll.csv", null, "simple-ws");
    }

    @Test(dependsOnMethods = {"uploadDataTest"})
    public void createvariableTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|variables");
        variablePage.createVariable(new AttributeVariable(variableName)
                .withAttribute("Education")
                .withAttributeElements("Bachelors Degree", "Graduate Degree"));
    }

    @Test(dependsOnMethods = {"uploadDataTest"})
    public void changeStateLabelTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + testParams.getProjectId() + "|dataPage|attributes");
        attributePage.configureAttributeLabel("State", AttributeLabelTypes.US_STATE_NAME);
    }

    @Test(dependsOnMethods = {"changeStateLabelTest", "createvariableTest"})
    public void addDashboardObjectsTest() throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        String dashboardName = "Test";
        dashboardsPage.addNewDashboard(dashboardName);
        dashboardsPage.editDashboard();
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.ATTRIBUTE, "County");
        dashboardEditBar.addListFilterToDashboard(DashFilterTypes.PROMPT, this.variableName);
        dashboardEditBar.addTimeFilterToDashboard(0, "7 ago");
        dashboardEditBar.addReportToDashboard("Amount Overview table");
        Thread.sleep(2000);
        dashboardEditBar.addTextToDashboard(TextObject.HEADLINE, "Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.SUB_HEADLINE, "Sub-Headline", "google.com");
        dashboardEditBar.addTextToDashboard(TextObject.DESCRIPTION, "Description", "google.com");
        dashboardEditBar.addLineToDashboard();
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.KEY_METRIC, "Avg of Amount");
        Thread.sleep(2000);
        dashboardEditBar.addWidgetToDashboard(WidgetTypes.GEO_CHART, "Avg of Amount");
        Thread.sleep(2000);
        dashboardEditBar.addWebContentToDashboard();
        dashboardEditBar.saveDashboard();
    }

    @Test(dependsOnMethods = {"addDashboardObjectsTest"})
    public void printDashboardTest() throws InterruptedException {
        initDashboardsPage();
        String exportedDashboardName = dashboardsPage.printDashboardTab(0);
        verifyDashboardExport(exportedDashboardName.replace(" ", "_"), expectedDashboardExportSize);
        checkRedBar(browser);
    }
}