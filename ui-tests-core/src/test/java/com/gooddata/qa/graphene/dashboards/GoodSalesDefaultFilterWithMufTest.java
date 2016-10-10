package com.gooddata.qa.graphene.dashboards;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForDashboardPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.addMufToUser;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.createMufObjectByUri;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.getVariableUri;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.removeAllMufFromUser;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Metric;
import com.gooddata.md.report.AttributeInGrid;
import com.gooddata.md.report.Filter;
import com.gooddata.md.report.GridReportDefinitionContent;
import com.gooddata.md.report.MetricElement;
import com.gooddata.qa.graphene.AbstractDashboardWidgetTest;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.enums.dashboard.DashboardWidgetDirection;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.AddDashboardFilterPanel.DashAttributeFilterTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardsPage;
import com.gooddata.qa.graphene.fragments.dashboards.SavedViewWidget;

public class GoodSalesDefaultFilterWithMufTest extends AbstractDashboardWidgetTest {

    private static final String DASHBOARD_MUF = "Muf-Dashboard";

    private static final String MUF_DF_VARIABLE = "Muf-Df-Variable";
    private static final String REPORT_MUF = "Report-muf";

    private static final String COMPUSCI = "CompuSci";
    private static final String EDUCATIONLY = "Educationly";
    private static final String EXPLORER = "Explorer";
    private static final String PHOENIXSOFT = "PhoenixSoft";
    private static final String WONDERKID = "WonderKid";
    private static final String ALL = "All";

    private boolean multipleChoice;

    @BeforeClass(alwaysRun = true)
    public void setUp(ITestContext context) {
        multipleChoice = parseBoolean(context.getCurrentXmlTest().getParameter("multipleChoice"));
    }

    @DataProvider(name = "mufProvider")
    public Object[][] getMufProvider() throws ParseException, JSONException, IOException {
        final Attribute productAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PRODUCT));
        final String productValues = getMdService()
                .getAttributeElements(productAttribute)
                .subList(0, 3)
                .stream()
                .map(e -> e.getUri())
                .map(e -> format("[%s]", e))
                .collect(joining(","));

        final String expression = format("[%s] IN (%s)", productAttribute.getUri(), productValues);
        final String mufUri = createMufObjectByUri(getRestApiClient(), testParams.getProjectId(), "muf", expression);

        return new Object[][] {
            {testParams.getEditorUser(), UserRoles.EDITOR, mufUri},
            {testParams.getViewerUser(), UserRoles.VIEWER, mufUri}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void prepareDashboardForMufUser() throws JSONException, IOException {
        initVariablePage().createVariable(new AttributeVariable(MUF_DF_VARIABLE).withAttribute(ATTR_PRODUCT));

        Metric amountMetric = getMdService().getObj(getProject(), Metric.class, title(METRIC_AMOUNT));
        Attribute productAttribute = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PRODUCT));
        String promptFilterUri = getVariableUri(getRestApiClient(), testParams.getProjectId(), MUF_DF_VARIABLE);

        createReportViaRest(GridReportDefinitionContent.create(REPORT_MUF,
                singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(productAttribute.getDefaultDisplayForm().getUri(), productAttribute.getTitle())),
                singletonList(new MetricElement(amountMetric)),
                singletonList(new Filter(format("[%s]", promptFilterUri)))));

        initDashboardsPage()
                .addNewDashboard(DASHBOARD_MUF)
                .addReportToDashboard(REPORT_MUF)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.ATTRIBUTE, ATTR_PRODUCT)
                .addAttributeFilterToDashboard(DashAttributeFilterTypes.PROMPT, MUF_DF_VARIABLE)
                .turnSavedViewOption(true);

        DashboardWidgetDirection.LEFT.moveElementToRightPlace(getReport(REPORT_MUF).getRoot());
        DashboardWidgetDirection.RIGHT.moveElementToRightPlace(getFilter(ATTR_PRODUCT).getRoot());

        if (!multipleChoice) {
            getFilter(ATTR_PRODUCT).changeSelectionToOneValue();
            getFilter(MUF_DF_VARIABLE).changeSelectionToOneValue();
        }

        dashboardsPage.saveDashboard().publishDashboard(true);
        takeScreenshot(browser, "Muf-Dashboard", getClass());
        checkRedBar(browser);
    }

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "mufProvider", groups = {"df-multiple"},
            description = "Verify muf user default view shows correctly after assigned muf in multiple choice mode")
    public void checkMufUserDefaultViewInMultipleChoiceMode(String user, UserRoles role, String mufObjectUri)
            throws ParseException, IOException, JSONException {
        addMufToUser(getRestApiClient(), testParams.getProjectId(), user, mufObjectUri);

        try {
            initDashboardsPage().selectDashboard(DASHBOARD_MUF).editDashboard();
            getFilter(ATTR_PRODUCT).changeAttributeFilterValues(COMPUSCI, EDUCATIONLY, PHOENIXSOFT);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValues(COMPUSCI, PHOENIXSOFT);

            dashboardsPage.saveDashboard();
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), asList(COMPUSCI, PHOENIXSOFT));

            logoutAndLoginAs(canAccessGreyPage(browser), role);
            initDashboardsPage().selectDashboard(DASHBOARD_MUF);

            takeScreenshot(browser, "DF-is-updated-with-muf-value-in-default-view-with-" + role, getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), String.join(", ", COMPUSCI, EDUCATIONLY));
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), COMPUSCI);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), singletonList(COMPUSCI));

            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            initDashboardsPage().selectDashboard(DASHBOARD_MUF).editDashboard();
            getFilter(ATTR_PRODUCT).changeAttributeFilterValues(PHOENIXSOFT);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValues(PHOENIXSOFT);

            dashboardsPage.saveDashboard();
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), singletonList(PHOENIXSOFT));

            logoutAndLoginAs(canAccessGreyPage(browser), role);
            initDashboardsPage().selectDashboard(DASHBOARD_MUF);

            takeScreenshot(browser, "DF-shows-all-when-value-set-by-admin-out-of-range-with-" + role, getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), ALL);
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), asList(COMPUSCI, EDUCATIONLY, EXPLORER));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            removeAllMufFromUser(getRestApiClient(), testParams.getProjectId(), user);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "mufProvider", groups = {"df-single"}, 
            description = "Verify muf user default view shows correctly after assigned muf in single choice mode")
    public void checkMufUserDefaultViewInSingleChoiceMode(String user, UserRoles role, String mufObjectUri)
            throws ParseException, IOException, JSONException {
        addMufToUser(getRestApiClient(), testParams.getProjectId(), user, mufObjectUri);

        try {
            initDashboardsPage().selectDashboard(DASHBOARD_MUF).editDashboard();
            getFilter(ATTR_PRODUCT).changeAttributeFilterValues(PHOENIXSOFT);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValues(PHOENIXSOFT);

            dashboardsPage.saveDashboard();
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), singletonList(PHOENIXSOFT));

            logoutAndLoginAs(canAccessGreyPage(browser), role);
            initDashboardsPage().selectDashboard(DASHBOARD_MUF);

            takeScreenshot(browser, "DF-shows-first-value-when-value-set-by-admin-out-of-range-with-" + role, getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), COMPUSCI);
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), COMPUSCI);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), singletonList(COMPUSCI));

        } finally {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
            removeAllMufFromUser(getRestApiClient(), testParams.getProjectId(), user);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "mufProvider", groups = {"df-multiple"},
            description = "Verify muf user saved view shows correctly after assigned muf in multiple choice mode")
    public void checkMufUserSavedViewInMultipleChoiceMode(String user, UserRoles role, String mufObjectUri)
            throws JSONException, ParseException, IOException {
        final String savedView1 = "SavedView1";
        final String savedView2 = "SavedView2";

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), role);
            SavedViewWidget savedViewWidget = initDashboardsPage()
                    .selectDashboard(DASHBOARD_MUF)
                    .getSavedViewWidget();

            getFilter(ATTR_PRODUCT).changeAttributeFilterValues(WONDERKID);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValues(WONDERKID);
            savedViewWidget.openSavedViewMenu().saveCurrentView(savedView1);

            getFilter(ATTR_PRODUCT).changeAttributeFilterValues(COMPUSCI, WONDERKID);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValues(COMPUSCI, WONDERKID);
            savedViewWidget.openSavedViewMenu().saveCurrentView(savedView2);

            addMufToUser(getRestApiClient(), testParams.getProjectId(), user, mufObjectUri);

            savedViewWidget = refreshDashboardsPage()
                    .getSavedViewWidget()
                    .openSavedViewMenu()
                    .selectSavedView(savedView1);

            takeScreenshot(browser, "Saved-view-shows-all-when-value-out-of-range-muf-assigned-for-" + role, getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), ALL);
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), asList(COMPUSCI, EDUCATIONLY, EXPLORER));

            savedViewWidget.openSavedViewMenu().selectSavedView(savedView2);
            takeScreenshot(browser, "Saved-view-updated-with-muf-assigned-for-" + role, getClass());
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), COMPUSCI);
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), COMPUSCI);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), singletonList(COMPUSCI));

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            removeAllMufFromUser(getRestApiClient(), testParams.getProjectId(), user);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, dataProvider = "mufProvider", groups = {"df-single"}, 
            description = "Verify muf user saved view shows correctly after assigned muf in single choice mode")
    public void checkMufUserSavedViewInSingleChoiceMode(String user, UserRoles role, String mufObjectUri)
            throws JSONException, ParseException, IOException {
        final String savedView = "SavedView";

        try {
            logoutAndLoginAs(canAccessGreyPage(browser), role);
            SavedViewWidget savedViewWidget = initDashboardsPage()
                    .selectDashboard(DASHBOARD_MUF)
                    .getSavedViewWidget();

            getFilter(ATTR_PRODUCT).changeAttributeFilterValues(WONDERKID);
            getFilter(MUF_DF_VARIABLE).changeAttributeFilterValues(WONDERKID);
            savedViewWidget.openSavedViewMenu().saveCurrentView(savedView);

            addMufToUser(getRestApiClient(), testParams.getProjectId(), user, mufObjectUri);

            refreshDashboardsPage()
                    .getSavedViewWidget()
                    .openSavedViewMenu()
                    .selectSavedView(savedView);

            takeScreenshot(browser, "Saved-view-shows-first-value-when-out-of-range-muf-assigned-for-" + role, getClass());
            // Due to WA-6145, filter shows all instead of first value in available list
            assertEquals(getFilter(ATTR_PRODUCT).getCurrentValue(), ALL);
            assertEquals(getFilter(MUF_DF_VARIABLE).getCurrentValue(), ALL);
            assertEquals(getReport(REPORT_MUF).getAttributeElements(), asList(COMPUSCI, EDUCATIONLY, EXPLORER));

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            removeAllMufFromUser(getRestApiClient(), testParams.getProjectId(), user);
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    private DashboardsPage refreshDashboardsPage() {
        browser.navigate().refresh();
        waitForDashboardPageLoaded(browser);
        return dashboardsPage;
    }
}
