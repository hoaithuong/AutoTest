package com.gooddata.qa.graphene.dashboards;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.dashboard.TextObject;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.EmbedDashboardDialog;
import com.gooddata.qa.graphene.fragments.dashboards.EmbeddedDashboard;
import com.gooddata.qa.graphene.fragments.dashboards.widget.EmbeddedWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.FilterWidget;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.AvailableValuesConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.SelectionConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel;
import com.gooddata.qa.graphene.fragments.dashboards.widget.configuration.WidgetConfigPanel.Tab;
import com.gooddata.qa.graphene.fragments.dashboards.widget.filter.TimeFilterPanel;
import com.gooddata.qa.graphene.i18n.AbstractEmbeddedModeTest;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.filter.FilterItemContent;
import com.gooddata.qa.mdObjects.dashboard.tab.FilterItem;
import com.gooddata.qa.mdObjects.dashboard.tab.TabItem;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.java.Builder;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_DEPARTMENT;
import static java.lang.String.format;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;
import static org.testng.Assert.assertEquals;

public class GoodSalesDashboardMacroTest extends AbstractEmbeddedModeTest {

    private static final String ALL = "all";
    private static final String NONE = "None";
    private static final String DEPARTMENT_IDENTIFIER = "attr.owner.department";
    private static final String METRIC_AVAILABLE = "Metric available";
    private static final String DIRECT_SALES = "Direct Sales";
    private static final String WEB_CONTENT = "https://urlecho.appspot.com/echo?status=200&body=%s";
    private String statusVariableUri;
    private DashboardRestRequest dashboardRequest;

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Dashboard Macro";
    }

    @Override
    protected void customizeProject() throws Throwable {
        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        Metric amountMetric = getMetricCreator().createAmountMetric();
        statusVariableUri = getVariableCreator().createStatusVariable();
        String expressionAvailableMetric = format("SELECT [%s] WHERE [%s] IN ([%s])", amountMetric.getUri(),
            getAttributeByTitle(ATTR_DEPARTMENT).getUri(), getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES));
        createMetric(METRIC_AVAILABLE, expressionAvailableMetric, DEFAULT_METRIC_FORMAT);
    }

    @Test(dependsOnGroups = "createProject")
    public void createSimpleDashboardMacro() throws IOException {
        final String DEFAULT_DASHBOARD = "Default dashboard";

        initDashboardsPage().addNewDashboard(DEFAULT_DASHBOARD);
        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%CURRENT_PROJECT_HASH%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), testParams.getProjectId());

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%CURRENT_DASHBOARD_URI%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), dashboardRequest.getDashboardUri(DEFAULT_DASHBOARD));

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%CURRENT_DASHBOARD_TAB_URI%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.MIDDLE.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), dashboardRequest.getTabId(DEFAULT_DASHBOARD, "First Tab"));

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%CURRENT_USER_EMAIL_MD5%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Simple_Dashboard_Marco", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), convertStringToMD5(testParams.getUser()));
    }

    @Test(dependsOnGroups = "createProject")
    public void createMultipleDashboardMacros() throws IOException {
        final String TEST_DASHBOARD = "Test dashboard";

        initDashboardsPage().addNewDashboard(TEST_DASHBOARD);
        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT,
                "%CURRENT_PROJECT_HASH%%CURRENT_DASHBOARD_URI%%CURRENT_DASHBOARD_TAB_URI%%CURRENT_USER_EMAIL_MD5%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.LEFT.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), testParams.getProjectId() +
                dashboardRequest.getDashboardUri(TEST_DASHBOARD) +
                dashboardRequest.getTabId(TEST_DASHBOARD, "First Tab") +
                convertStringToMD5(testParams.getUser()));

        //To cover bug CL-13000
        //Dashboard macro applied only once
        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%CURRENT_PROJECT_HASH%%CURRENT_PROJECT_HASH%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Multiple_Dashboard_Marco", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), testParams.getProjectId() + testParams.getProjectId());
    }

    @Test(dependsOnGroups = "createProject")
    public void createFilterTitleAndFilterValueMacro() throws IOException {
        String dashboardName = createDashboardHasFilter(createSingleValueFilter(getAttributeByTitle(ATTR_DEPARTMENT)));
        initDashboardsPage().selectDashboard(dashboardName);

        dashboardsPage.addTextToDashboard(TextObject.HEADLINE, "", "%FILTER_TITLE(" + DEPARTMENT_IDENTIFIER + ")%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), DIRECT_SALES);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%FILTER_TITLE(" + DEPARTMENT_IDENTIFIER + ")%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), DIRECT_SALES);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%FILTER_VALUE(" + DEPARTMENT_IDENTIFIER + ")%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Filter_Title_&_Value_Macro", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES));
    }

    @Test(dependsOnGroups = "createProject")
    public void createAttributeFilterMacrosApplyUseAvailable() throws IOException {
        String dashboardName = createDashboardHasFilter(createMultipleValuesFilter(getAttributeByTitle(ATTR_DEPARTMENT)));
        WidgetConfigPanel widgetConfigPanel = initDashboardsPage().selectDashboard(dashboardName).editDashboard()
            .getDashboardEditFilter().openWidgetConfigPanel(ATTR_DEPARTMENT);
        widgetConfigPanel.getTab(Tab.AVAILABLE_VALUES, AvailableValuesConfigPanel.class).selectMetric(METRIC_AVAILABLE);
        widgetConfigPanel.saveConfiguration();

        dashboardsPage.saveDashboard().editDashboard()
            .addWebContentToDashboard(format(WEB_CONTENT, "%FILTER_TITLE(" + DEPARTMENT_IDENTIFIER + ")%"))
            .addTextToDashboard(TextObject.HEADLINE, "", "%FILTER_TITLE(" + DEPARTMENT_IDENTIFIER + ")%");
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), DIRECT_SALES);
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), DIRECT_SALES);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%FILTER_VALUE(" + DEPARTMENT_IDENTIFIER + ")%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Attribute_Filter_Macro_Apply_Use_Available", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), getAttributeElementUri(ATTR_DEPARTMENT, DIRECT_SALES));
    }

    @Test(dependsOnGroups = "createProject")
    public void createVariableValueMacro() throws IOException {
        String dashboardName = createDashboardHasFilter(createSingleValuesFilterBy(statusVariableUri));
        initDashboardsPage().selectDashboard(dashboardName)
            .addWebContentToDashboard(format(WEB_CONTENT, "%VARIABLE_VALUE(" + getVariableIdentifier() + ")%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Variable_Value_Macro", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), "Open");
    }

    @Test(dependsOnGroups = "createProject")
    public void createDateFilterMacro() throws IOException {
        String dashboardName = createDashboardHasFilter(createDateFilter(getAttributeByIdentifier("closed.euweek"), 0, 0));
        final String MONDAY_OF_THIS_WEEK = DateRange.now().with(previousOrSame(MONDAY)).toString();
        final String SUNDAY_OF_THIS_WEEK = DateRange.now().with(nextOrSame(SUNDAY)).toString();
        initDashboardsPage().selectDashboard(dashboardName);
        dashboardsPage.addTextToDashboard(TextObject.HEADLINE, "", "%DATE_FILTER_VALUE(closed.euweek,FROM)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), MONDAY_OF_THIS_WEEK);

        dashboardsPage.addTextToDashboard(TextObject.HEADLINE, "", "%DATE_FILTER_VALUE(closed.euweek,TO)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), SUNDAY_OF_THIS_WEEK);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%DATE_FILTER_VALUE(closed.euweek,FROM)%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), MONDAY_OF_THIS_WEEK);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%DATE_FILTER_VALUE(closed.euweek,TO)%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Date_Filter_Macro", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), SUNDAY_OF_THIS_WEEK);
    }

    /**
     * Follow to createDateFilterMacro Test to verify that the first date filter macro "Date (Closed)" be correct
     * So that, just check on second macro "Date (Created)" to limit overlapped widgets on dashboard
     */
    @Test(dependsOnGroups = "createProject")
    public void createMoreDateFilterMacros() throws IOException {
        final String DATE_CREATED = "Date (Created)";
        String dashboardName = createDashboardHasFilter(createDateFilter(getAttributeByIdentifier("closed.euweek"), 0, 0));
        initDashboardsPage().selectDashboard(dashboardName).editDashboard()
            .addTimeFilterToDashboard(DATE_CREATED, TimeFilterPanel.DateGranularity.WEEK, "last");
        final String MONDAY_OF_LAST_WEEK = DateRange.now().minusWeeks(1).with(previousOrSame(MONDAY)).toString();
        final String SUNDAY_OF_LAST_WEEK = DateRange.now().minusWeeks(1).with(nextOrSame(SUNDAY)).toString();
        FilterWidget filterWidget= dashboardsPage.getFilterWidgetByName(DATE_CREATED);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(filterWidget.getRoot());
        //Filter just applies after saving
        dashboardsPage.saveDashboard().addTextToDashboard(TextObject.HEADLINE, "", "%DATE_FILTER_VALUE(created.euweek,FROM)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), MONDAY_OF_LAST_WEEK);

        dashboardsPage.addTextToDashboard(TextObject.HEADLINE, "", "%DATE_FILTER_VALUE(created.euweek,TO)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), SUNDAY_OF_LAST_WEEK);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%DATE_FILTER_VALUE(created.euweek,FROM)%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), MONDAY_OF_LAST_WEEK);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%DATE_FILTER_VALUE(created.euweek,TO)%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_More_Date_Filter_Macro", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), SUNDAY_OF_LAST_WEEK);
    }

    @Test(dependsOnGroups = "createProject")
    public void createDateFilterAndDependentAttributeFilterMacros() throws IOException {
        final String EMPTY_VALUE = "(empty value)";
        final String weekTitle = getAttributeByIdentifier("closed.euweek").getTitle();
        String dashboardName =
            createDashboardHasFilter(createDateFilter(getAttributeByIdentifier("closed.euweek"), 0, 0));
        final String MONDAY_OF_THIS_WEEK = DateRange.now().with(previousOrSame(MONDAY)).toString();
        final String SUNDAY_OF_THIS_WEEK = DateRange.now().with(nextOrSame(SUNDAY)).toString();

        WidgetConfigPanel widgetConfigPanel = initDashboardsPage().selectDashboard(dashboardName).editDashboard()
            .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, weekTitle)
            .getDashboardEditFilter().openWidgetConfigPanel(weekTitle);
        widgetConfigPanel.getTab(Tab.SELECTION, SelectionConfigPanel.class).changeSelectionToOneValue();
        widgetConfigPanel.saveConfiguration();
        FilterWidget filterWidget = dashboardsPage.getFilterWidgetByName(weekTitle);
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(filterWidget.getRoot());
        dashboardsPage.saveDashboard().addTextToDashboard(TextObject.HEADLINE, "", "%DATE_FILTER_VALUE(closed.euweek,FROM)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), MONDAY_OF_THIS_WEEK);

        dashboardsPage.addTextToDashboard(TextObject.HEADLINE, "", "%FILTER_TITLE(closed.euweek)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), EMPTY_VALUE);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%DATE_FILTER_VALUE(closed.euweek,TO)%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        assertEquals(embeddedWidget.getContentBodyAsText(), SUNDAY_OF_THIS_WEEK);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%FILTER_TITLE(closed.euweek)%"));
        embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Date_Filter_&_Attribute_Filter_Macros", getClass());
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), EMPTY_VALUE);
    }

    @Test(dependsOnGroups = "createProject")
    public void createDateFilterAndMacrosNotDepend() throws IOException {
        String dashboardName =
            createDashboardHasFilter(createDateFilter(getAttributeByIdentifier("closed.euweek"), 0, 0));

        initDashboardsPage().selectDashboard(dashboardName)
            .addTextToDashboard(TextObject.HEADLINE, "", "%FILTER_TITLE(closed.euweek)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), ALL);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%FILTER_TITLE(closed.euweek)%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Date_Filter_&_Macros_Not_Depend", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), NONE);
    }

    @Test(dependsOnGroups = "createProject")
    public void createMacrosWithoutFilter() throws IOException {
        initDashboardsPage().addNewDashboard("Empty Dashboard")
            .addTextToDashboard(TextObject.HEADLINE, "", "%FILTER_TITLE(closed.euweek)%");
        assertEquals(dashboardsPage.getContent().getLastTextWidgetValue(), ALL);

        dashboardsPage.addWebContentToDashboard(format(WEB_CONTENT, "%DATE_FILTER_VALUE(closed.euweek,TO)%"));
        EmbeddedWidget embeddedWidget = dashboardsPage.getLastEmbeddedWidget();
        DashboardWidgetDirection.DOWN.moveElementToRightPlace(embeddedWidget.getRoot());
        Screenshots.takeScreenshot(browser, "Create_Macros_Without_Filter", getClass());
        assertEquals(embeddedWidget.getContentBodyAsText(), NONE);
    }

    @Test(dependsOnGroups = "createProject")
    public void createUrlParameterFilterMacro() throws IOException {
        initDashboardsPage()
            .addNewDashboard("URL")
            .addTextToDashboard(TextObject.HEADLINE, "", "%URL_PARAM(label.owner.department)%")
            .addWebContentToDashboard(format(WEB_CONTENT, "%URL_PARAM(label.owner.department)%"));
        dashboardsPage.saveDashboard();
        EmbedDashboardDialog embedDashboardDialog = dashboardsPage.openEmbedDashboardDialog();
        embedDashboardDialog.selectFilterAttribute(ATTR_DEPARTMENT, DIRECT_SALES);
        embeddedUri = embedDashboardDialog.getPreviewURI();
        EmbeddedDashboard embeddedDashboard = initEmbeddedDashboard();

        Screenshots.takeScreenshot(browser, "Create_Url_Parameter_Filter_Macro", getClass());
        assertEquals(embeddedDashboard.getLastEmbeddedWidget().getContentBodyAsText(), DIRECT_SALES);
        assertEquals(embeddedDashboard.getContent().getLastTextWidgetValue(), DIRECT_SALES);
    }

    private String getVariableIdentifier() throws IOException {
        JSONObject json = new CommonRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .getJsonObject(statusVariableUri);
        return json.getJSONObject("prompt").getJSONObject("meta").getString("identifier");
    }

    private String createDashboardHasFilter(FilterItemContent filter) throws JSONException, IOException {
        String dashboardName = "dashboard" + generateHashString();
        Dashboard dashboard =
            Builder.of(Dashboard::new).with(dash -> {
                dash.setName(dashboardName);
                dash.addTab(Builder.of(com.gooddata.qa.mdObjects.dashboard.tab.Tab::new)
                    .with(tab -> {
                        FilterItem filterItem = Builder.of(FilterItem::new).with(item -> {
                            item.setContentId(filter.getId());
                            item.setPosition(TabItem.ItemPosition.LEFT);
                        }).build();
                        tab.addItem(filterItem);
                    })
                    .build());
                dash.addFilter(filter);
            }).build();

        dashboardRequest.createDashboard(dashboard.getMdObject());
        return dashboardName;
    }

    private String convertStringToMD5(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(string.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                sb.append(format("%02x", b & 0xff));
            }

            return sb.toString();
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException("algorithm not found");
        }
    }
}
