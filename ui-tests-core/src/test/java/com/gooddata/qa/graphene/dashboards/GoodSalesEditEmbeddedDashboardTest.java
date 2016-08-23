package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentNotVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.browser.BrowserUtils.switchToMainWindow;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.GridElement;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.Report;
import com.gooddata.md.report.ReportDefinition;
import com.gooddata.qa.graphene.GoodSalesAbstractTest;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.DashboardEditWidgetToolbarPanel;
import com.gooddata.qa.graphene.fragments.manage.MetricEditorDialog;
import com.gooddata.qa.graphene.fragments.reports.ReportsPage;
import com.gooddata.qa.graphene.fragments.reports.report.EmbeddedReportPage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;

public class GoodSalesEditEmbeddedDashboardTest extends GoodSalesAbstractTest {

    private static final String EMBEDDED_DASHBOARD = "embedded-dashboard";

    private static final String METRIC_AMOUNT = "Amount";
    private static final String ATTR_PRODUCT = "Product";

    private static final String RED_BAR_MESSAGE =
            "Please remove this report from all dashboards and email distribution lists before deleting.";

    private String embeddedCode;
    private String embedUri;

    @Test(dependsOnGroups = "createProject", groups = "precondition")
    public void initData() {
        EmbedDashboardDialog embeddedDialog = initDashboardsPage()
                .addNewDashboard("New Dashboard")
                .openEmbedDashboardDialog();

        embeddedCode = embeddedDialog.getEmbedCode();
        embedUri = embeddedDialog.getPreviewURI().replace("dashboard.html", "embedded.html");
    }

    @Test(dependsOnGroups = "precondition")
    public void editEmbeddedDashboardUsingEmbeddedHtmlLink() {
        final String report = "Report-" + System.currentTimeMillis();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboardUsingEmbeddedHtmlLink();

        embeddedDashboard
                .openEmbeddedReportPage()
                .createReport(new UiReportDefinition()
                        .withName(report)
                        .withWhats(METRIC_AMOUNT)
                        .withHows(ATTR_PRODUCT));

        EmbeddedDashboard.waitForDashboardLoaded(browser);

        TableReport tableReport = embeddedDashboard.getReport(report, TableReport.class);

        takeScreenshot(browser, "create-report-" + report + "-in-embedded-dashboard-using-embedded-html-link", getClass());
        assertThat(tableReport.getAttributeElements(),
                hasItems("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid"));

        DashboardEditWidgetToolbarPanel.removeWidget(tableReport.getRoot(), browser);

        embeddedDashboard.saveDashboard();

        takeScreenshot(browser, "delete-report" + report + "-in-embedded-dashboard", getClass());
        assertTrue(embeddedDashboard.getContent().isEmpty(), "Report is not deleted successfully");
    }

    @Test(dependsOnGroups = "precondition")
    public void initEmbeddedDashboardWithIframe() {
        initDashboardsPage()
                .addNewDashboard(EMBEDDED_DASHBOARD)
                .addWebContentToDashboard(embeddedCode)
                .getLastEmbeddedWidget()
                .resizeFromTopLeftButton(-300, 0)
                .resizeFromBottomRightButton(200, 600);

        dashboardsPage.saveDashboard();
    }

    @Test(dependsOnMethods = "initEmbeddedDashboardWithIframe")
    public void createAndDeleteReportInEmbeddedDashboard() {
        final String report = "Report-" + System.currentTimeMillis();

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard(EMBEDDED_DASHBOARD);

        embeddedDashboard
                .openEmbeddedReportPage()
                .createReport(new UiReportDefinition()
                        .withName(report)
                        .withWhats(METRIC_AMOUNT)
                        .withHows(ATTR_PRODUCT));

        EmbeddedDashboard.waitForDashboardLoaded(browser);

        TableReport tableReport = embeddedDashboard.getReport(report, TableReport.class);

        takeScreenshot(browser, "create-report-" + report + "-in-embedded-dashboard", getClass());
        assertThat(tableReport.getAttributeElements(),
                hasItems("CompuSci", "Educationly", "Explorer", "Grammar Plus", "PhoenixSoft", "WonderKid"));

        openReportDetailFromEmbeddedDashboard(embeddedDashboard, report).deleteCurrentReport();
        EmbeddedDashboard.waitForDashboardLoaded(browser);

        switchToMainWindow(browser);
        initReportsPage();

        takeScreenshot(browser, "Report-" + report + "-is-deleted", getClass());
        assertFalse(reportsPage.isReportVisible(report), "Report: " + report + " is not deleted");
    }

    @Test(dependsOnMethods = "initEmbeddedDashboardWithIframe")
    public void editReportInEmbeddedDashboard() {
        final Report report = createReport("Report-" + System.currentTimeMillis());

        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard(EMBEDDED_DASHBOARD);

        embeddedDashboard.addReportToDashboard(report.getTitle()).saveDashboard();

        openReportDetailFromEmbeddedDashboard(embeddedDashboard, report.getTitle()).cancel();
        EmbeddedDashboard.waitForDashboardLoaded(browser);

        openReportDetailFromEmbeddedDashboard(embeddedDashboard, report.getTitle())
                .addFilter(FilterItem.Factory.createAttributeFilter(ATTR_PRODUCT, "CompuSci", "Educationly"))
                .clickSaveReport();
        EmbeddedDashboard.waitForDashboardLoaded(browser);

        List<String> attributeValues = embeddedDashboard
                .getReport(report.getTitle(), TableReport.class).getAttributeElements();

        takeScreenshot(browser, "edit-report-in-embedded-dashboard", getClass());
        assertThat(attributeValues.size(), is(2));
        assertThat(attributeValues, hasItems("CompuSci", "Educationly"));

        openReportDetailFromEmbeddedDashboard(embeddedDashboard, report.getTitle()).deleteCurrentReport();
        assertEquals(getRedBarMessage(), RED_BAR_MESSAGE);
    }

    @DataProvider(name = "metricProvider")
    public Object[][] getMetricProvider() {
        final String localMetric = createMetric("localMetric", "Select 1", "#,##0").getTitle();
        final String deleteLocalMetricMessage = "Are you sure you want to delete this metric? This action cannot be undone.";

        final String globalMetric = "# of Lost Opps.";
        final String deleteGlobalMetricMessage = "This metric is used in other reports. "
                + "If you delete this metric, it will remain valid in these reports. However, "
                + "it will no longer be possible to add it to new reports.";

        return new Object[][] {
            {localMetric, deleteLocalMetricMessage},
            {globalMetric, deleteGlobalMetricMessage}
        };
    }

    @Test(dependsOnMethods = "initEmbeddedDashboardWithIframe", dataProvider = "metricProvider")
    public void manageMetricInEmbeddedDashboard(String metric, String deleteMessage) {
        EmbeddedReportPage embeddedReportPage =
                initEmbeddedDashboard(EMBEDDED_DASHBOARD).openEmbeddedReportPage();

        embeddedReportPage.openWhatPanel().selectMetric(metric);

        assertTrue(embeddedReportPage.isSndEditMetricButtonVisible(), "Edit button in Snd Metric detail is not visible");
        assertTrue(embeddedReportPage.isSndDeleteMetricButtonVisible(), "Delete button in Snd Metric detail is not visible");

        String editedMetricName = "Edited-" + metric;
        MetricEditorDialog metricEditorDialog = embeddedReportPage.clickEditInSndMetricDetail();

        metricEditorDialog.enterMetricName(editedMetricName).save();
        waitForFragmentNotVisible(metricEditorDialog);

        switchToMainWindow(browser);
        assertTrue(initMetricPage().isMetricVisible(editedMetricName), "There is an error when editing metric name");

        embeddedReportPage = initEmbeddedDashboard(EMBEDDED_DASHBOARD).openEmbeddedReportPage();

        embeddedReportPage.openWhatPanel().selectMetric(editedMetricName);
        embeddedReportPage.clickDeleteInSndMetricDetail();

        takeScreenshot(browser, "delete-confirmation-message-for-metric" + metric, getClass());
        assertThat(getMessageFromConfirmDeleteDialog(), containsString(deleteMessage));

        confirmDelete();

        switchToMainWindow(browser);
        assertFalse(initMetricPage().isMetricVisible(editedMetricName), "Metric is still not deleted");
    }

    @Test(dependsOnMethods = "initEmbeddedDashboardWithIframe")
    public void manageReportInEmbeddedDomainPage() {
        final Report report = createReport("report-" + System.currentTimeMillis());
        final String reportFolder = "embedded-report";

        ReportsPage embeddedReportsPage =
                initEmbeddedDashboard(EMBEDDED_DASHBOARD).openEmbeddedReportsPage();

        embeddedReportsPage.getReportsList().openReport(report.getTitle());

        Graphene.createPageFragment(EmbeddedReportPage.class,
                waitForElementVisible(EmbeddedReportPage.LOCATOR, browser))
                .cancel();
        waitForFragmentVisible(embeddedReportsPage);

        embeddedReportsPage
                .addNewFolder(reportFolder)
                .moveReportsToFolder(reportFolder, report.getTitle())
                .openCustomFolder(reportFolder);

        takeScreenshot(browser, "Moving-report-to-other-folder in-embedded-Domain-page", getClass());
        assertEquals(embeddedReportsPage.getReportsList().getReports().size(), 1);
        assertTrue(embeddedReportsPage.isReportVisible(report.getTitle()), "There is an error when moving report");

        embeddedReportsPage.getReportsList().openReport(report.getTitle());

        Graphene.createPageFragment(EmbeddedReportPage.class,
                waitForElementVisible(EmbeddedReportPage.LOCATOR, browser))
                .deleteCurrentReport();
        waitForFragmentVisible(embeddedReportsPage);

        takeScreenshot(browser, "Delete-report-in-embedded-Domain-page", getClass());
        assertFalse(embeddedReportsPage.openCustomFolder(reportFolder).isReportVisible(report.getTitle()),
                "Report is still not deleted");
    }

    private EmbeddedDashboard initEmbeddedDashboard(String dashboardName) {
        return initDashboardsPage()
                .selectDashboard(dashboardName)
                .getLastEmbeddedWidget()
                .getEmbeddedDashboard();
    }

    private Report createReport(String reportName) {
        String amountMetricUri = getMdService().getObjUri(getProject(), Metric.class, title(METRIC_AMOUNT));
        Attribute product = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PRODUCT));

        ReportDefinition definition = GridReportDefinitionContent.create(
                reportName,
                singletonList("metricGroup"),
                singletonList(new AttributeInGrid(product.getDefaultDisplayForm().getUri())),
                singletonList(new GridElement(amountMetricUri, METRIC_AMOUNT)));

        definition = getMdService().createObj(getProject(), definition);
        return getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));
    }

    private EmbeddedReportPage openReportDetailFromEmbeddedDashboard(EmbeddedDashboard embeddedDashboard,
            String reportName) {
        embeddedDashboard
                .getReport(reportName, TableReport.class)
                .openReportInfoViewPanel()
                .clickViewReportButton();

        EmbeddedReportPage.waitForPageLoaded(browser);

        return Graphene.createPageFragment(EmbeddedReportPage.class,
                waitForElementVisible(EmbeddedReportPage.LOCATOR, browser));
    }

    private EmbeddedDashboard initEmbeddedDashboardUsingEmbeddedHtmlLink() {
        browser.get(embedUri);
        return Graphene.createPageFragment(EmbeddedDashboard.class,
                waitForElementVisible(EmbeddedDashboard.LOCATOR, browser));
    }

    private String getMessageFromConfirmDeleteDialog() {
        return waitForElementVisible(By.className("c-confirmDeleteDialog"), browser).getText();
    }

    private void confirmDelete() {
        waitForElementVisible(By.cssSelector(".c-confirmDeleteDialog button[class*='s-btn-delete']"), browser).click();
    }

    private String getRedBarMessage() {
        return waitForElementVisible(BY_RED_BAR, browser).getText();
    }
}
