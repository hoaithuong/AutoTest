package com.gooddata.qa.graphene.i18n;

import static com.gooddata.qa.graphene.utils.CheckUtils.checkLocalization;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACCOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.VARIABLE_STATUS;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static org.openqa.selenium.By.className;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.dashboard.WidgetTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardAddWidgetPanel;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.reports.report.ChartReport;

public class EmbeddedDashboardWidgetSettingLocalizationTest extends AbstractEmbeddedDashboardTest {

    private static final String DASHBOARD_NAME = "Widget setting";

    @Override
    protected void initProperties() {
        super.initProperties();
        projectTitle = "GoodSales-embeded-dashboard-widget-setting-localization-test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        createActivitiesByTypeReport();
        createStatusVariable();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void initEmbeddedDashboardUri() {
        initDashboardsPage()
            .addNewDashboard(DASHBOARD_NAME);

        embeddedUri = initDashboardsPage()
            .selectDashboard(DASHBOARD_NAME)
            .openEmbedDashboardDialog()
            .getPreviewURI()
            .replace("dashboard.html", "embedded.html");
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardReportSetting() {
        EmbeddedDashboard dashboard = initEmbeddedDashboard();
        dashboard.editDashboard()
            .addReportToDashboard(REPORT_ACTIVITIES_BY_TYPE);

        WebElement elem = dashboard.getContent().getReport(REPORT_ACTIVITIES_BY_TYPE, ChartReport.class).getRoot();
        checkLocalizationThroughTabs(WidgetConfigPanel.openConfigurationPanelFor(elem, browser).getTabs());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardKeyMetricSetting() {
        initEmbeddedDashboard()
            .editDashboard();

        openAddWidgetPanel()
            .initWidget(WidgetTypes.KEY_METRIC);

        WidgetConfigPanel widgetConfigPanel = Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
        checkLocalizationThroughTabs(widgetConfigPanel.getTabs());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardKeyMetricWithTrendSetting() {
        initEmbeddedDashboard()
            .editDashboard();

        openAddWidgetPanel()
            .initWidget(WidgetTypes.KEY_METRIC_WITH_TREND);

        WidgetConfigPanel widgetConfigPanel = Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
        checkLocalizationThroughTabs(widgetConfigPanel.getTabs());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardGeoChartSetting() {
        initEmbeddedDashboard()
            .editDashboard();

        openAddWidgetPanel()
            .initWidget(WidgetTypes.GEO_CHART);

        WidgetConfigPanel widgetConfigPanel = Graphene.createPageFragment(WidgetConfigPanel.class,
                waitForElementVisible(WidgetConfigPanel.LOCATOR, browser));
        checkLocalizationThroughTabs(widgetConfigPanel.getTabs());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardAttributeFilterSetting() {
        EmbeddedDashboard dashboard = initEmbeddedDashboard();
        dashboard.editDashboard()
            .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_ACCOUNT);

        WebElement elem = dashboard.getContent().getFirstFilter().getRoot();
        checkLocalizationThroughTabs(WidgetConfigPanel.openConfigurationPanelFor(elem, browser).getTabs());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardVariableFilterSetting() {
        EmbeddedDashboard dashboard = initEmbeddedDashboard();
        dashboard.editDashboard()
            .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, VARIABLE_STATUS);

        WebElement elem = dashboard.getContent().getFirstFilter().getRoot();
        checkLocalizationThroughTabs(WidgetConfigPanel.openConfigurationPanelFor(elem, browser).getTabs());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"i18n"})
    public void checkDashboardWebContentSetting() {
        EmbeddedDashboard dashboard = initEmbeddedDashboard();
        dashboard.editDashboard()
            .addWebContentToDashboard(embeddedUri);

        WebElement elem = waitForElementVisible(className("yui3-c-iframedashboardwidget"), browser);
        checkLocalizationThroughTabs(WidgetConfigPanel.openConfigurationPanelFor(elem, browser).getTabs());
    }

    private DashboardAddWidgetPanel openAddWidgetPanel() {
        waitForElementVisible(className("s-btn-widget"), browser).click();
        return Graphene.createPageFragment(DashboardAddWidgetPanel.class,
                waitForElementVisible(DashboardAddWidgetPanel.LOCATOR, browser));
    }

    private void checkLocalizationThroughTabs(List<WebElement> tabs) {
        for (WebElement element: tabs) {
            element.click();
            sleepTightInSeconds(1); // buffer time for rendering configuration panel
            checkLocalization(browser);
        }
    }
}
