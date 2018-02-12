package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createVisualizationWidget;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.deleteAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAllInsightNames;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAnalyticalDashboards;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getInsightUri;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.FilterType;
import com.gooddata.qa.graphene.fragments.indigo.insight.AbstractInsightSelectionPanel.InsightItem;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createInsight;

public class InsightOnDashboardTest extends AbstractDashboardTest {

    private static final String TEST_INSIGHT = "Test-Insight";
    private static final String RENAMED_TEST_INSIGHT = "Renamed-Test-Insight";
    private static final String INSIGHT_CREATED_BY_EDITOR = "Insight-Created-By-Editor";
    private static final String INSIGHT_CREATED_BY_MAIN_USER = "Insight-Created-By-Main-User";
    private static final List<String> INSIGHTS_FOR_FILTER_TEST = asList(INSIGHT_CREATED_BY_EDITOR,
            INSIGHT_CREATED_BY_MAIN_USER);

    @Override
    public void initProperties() {
        super.initProperties();
        projectTitle += "Insight-On-Dashboard-Test";
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"setupDashboardENV"})
    public void testBlankStateInEditMode() {
        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets();
        // ignore checking existing of insight panel
        // because below assertions definitely fail in that case
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterActive(FilterType.BY_ME),
                "Created by me tab is not default filter");
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterVisible(FilterType.ALL),
                "All tab is not visible in filter section");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"createInsight"})
    public void testCreatingSimpleInsightUsingAd() throws JSONException, IOException {
        // need an insight having real data, so we can't use REST API
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).addAttribute(ATTR_ACTIVITY_TYPE)
                .waitForReportComputing().saveInsight(TEST_INSIGHT);
        assertTrue(getAllInsightNames(getRestApiClient(), testParams.getProjectId()).contains(TEST_INSIGHT),
                TEST_INSIGHT + " is not created");
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testAddInsight() {
        assertTrue(initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .getLastWidget(Insight.class)
            .isDeleteButtonVisible(), "Added insight is not focused");

        assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getHeadline(), TEST_INSIGHT);
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testSavedDashboardContainingInsight() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .saveEditModeWithWidgets();
        try {
            assertEquals(indigoDashboardsPage.getLastWidget(Insight.class).getHeadline(), TEST_INSIGHT);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(),
                    getAnalyticalDashboards(getRestApiClient(), testParams.getProjectId()).get(0));
        }
    }

    @DataProvider(name = "insightNameProvider")
    public Object[][] insightNameProvider() {
        return new Object[][]{
            { RENAMED_TEST_INSIGHT },
            { "<button>hello</button>" },
            { "新年快樂" }
        };
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"}, dataProvider = "insightNameProvider")
    public void testRenameInsightOnDashboard(String newInsightName) throws JSONException, IOException {
        IndigoDashboardsPage idp = initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .saveEditModeWithWidgets();

        try {
            idp.switchToEditMode()
                    .getLastWidget(Insight.class)
                    .clickOnContent()
                    .setHeadline(newInsightName);
            idp.saveEditModeWithWidgets();

            String headline = initIndigoDashboardsPageWithWidgets()
                    .getLastWidget(Insight.class)
                    .getHeadline();

            takeScreenshot(browser, "testRenameInsightOnDashboard-renamed", getClass());
            assertEquals(headline, newInsightName, "Insight not properly renamed");
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(),
                    getAnalyticalDashboards(getRestApiClient(), testParams.getProjectId()).get(0));
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightNamePlaceholder() throws JSONException, IOException {
        IndigoDashboardsPage idp = initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addInsight(TEST_INSIGHT)
            .saveEditModeWithWidgets();

        try {
            String headlinePlaceholder = idp.switchToEditMode()
                .getLastWidget(Insight.class)
                .clickOnContent()
                .clearHeadline()
                .getHeadlinePlaceholder();
            assertEquals(headlinePlaceholder, TEST_INSIGHT, "Insight placeholder not properly correct."
                    + "Expected: " + TEST_INSIGHT + " but: " + headlinePlaceholder);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(),
                    getAnalyticalDashboards(getRestApiClient(), testParams.getProjectId()).get(0));
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightRenderInViewModeAfterSwitchingPage() throws JSONException, IOException {
        final String dashboardUri = createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(
                        createVisualizationWidget(
                                getRestApiClient(),
                                testParams.getProjectId(),
                                getInsightUri(TEST_INSIGHT, getRestApiClient(), testParams.getProjectId()),
                                TEST_INSIGHT
                        )
                ));
        try {
            initAnalysePage();
            assertTrue(browser.getCurrentUrl().contains("/reportId/edit"), "AD page is not loaded");
            checkInsightRender(initIndigoDashboardsPageWithWidgets().getLastWidget(Insight.class),
                    TEST_INSIGHT, 4);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightTitleOnDashboardAfterRenamedInAD() throws JSONException, IOException {
        final String dashboardUri = createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(
                        createVisualizationWidget(
                                getRestApiClient(),
                                testParams.getProjectId(),
                                getInsightUri(TEST_INSIGHT, getRestApiClient(), testParams.getProjectId()),
                                TEST_INSIGHT
                        )
                ));

        try {
            initIndigoDashboardsPageWithWidgets();
            takeScreenshot(browser, "testInsightTitleOnDashboardAfterRenamedInAD-beforeRename", getClass());

            initAnalysePage().openInsight(TEST_INSIGHT).waitForReportComputing().setInsightTitle(RENAMED_TEST_INSIGHT).saveInsight();
            String insightInsertedBeforeRenameTitle = initIndigoDashboardsPageWithWidgets()
                .getLastWidget(Insight.class)
                .getHeadline();

            takeScreenshot(browser, "testInsightTitleOnDashboardAfterRenamedInAD-afterRename", getClass());
            assertEquals(insightInsertedBeforeRenameTitle, TEST_INSIGHT);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), dashboardUri);
            initAnalysePage().openInsight(RENAMED_TEST_INSIGHT).setInsightTitle(TEST_INSIGHT).saveInsight();
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightTitleOnDashboardAddedAfterRename() throws JSONException, IOException {
        final String dashboardUri = createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(
                        createVisualizationWidget(
                                getRestApiClient(),
                                testParams.getProjectId(),
                                getInsightUri(TEST_INSIGHT, getRestApiClient(), testParams.getProjectId()),
                                TEST_INSIGHT
                        )
                ));

        try {
            initAnalysePage().openInsight(TEST_INSIGHT).waitForReportComputing().setInsightTitle(RENAMED_TEST_INSIGHT).saveInsight();
            String insightInsertedAfterRenameTitle = initIndigoDashboardsPageWithWidgets()
                .switchToEditMode()
                .addInsight(RENAMED_TEST_INSIGHT)
                .getLastWidget(Insight.class)
                .getHeadline();

            takeScreenshot(browser, "testInsightTitleOnDashboardAddedAfterRename", getClass());
            assertEquals(insightInsertedAfterRenameTitle, RENAMED_TEST_INSIGHT);
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), dashboardUri);
            initAnalysePage().openInsight(RENAMED_TEST_INSIGHT).setInsightTitle(TEST_INSIGHT).saveInsight();
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testInsightTitleInADAfterRenamedOnDashboard() throws JSONException, IOException {
        final String dashboardUri = createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(),
                singletonList(
                        createVisualizationWidget(
                                getRestApiClient(),
                                testParams.getProjectId(),
                                getInsightUri(TEST_INSIGHT, getRestApiClient(), testParams.getProjectId()),
                                TEST_INSIGHT
                        )
                ));

        try {
            IndigoDashboardsPage idp = initIndigoDashboardsPageWithWidgets();
            idp.switchToEditMode()
                .getLastWidget(Insight.class)
                .clickOnContent()
                .setHeadline(RENAMED_TEST_INSIGHT);
            idp.saveEditModeWithWidgets();

            takeScreenshot(browser, "testInsightTitleInADAfterRenamedOnDashboard-insightRenamedInDashboards", getClass());

            AnalysisPage ap = initAnalysePage();

            assertTrue(ap.searchInsight(TEST_INSIGHT));
            takeScreenshot(browser, "testInsightTitleInADAfterRenamedOnDashboard-insightWithOriginalNameFound", getClass());
            assertFalse(ap.searchInsight(RENAMED_TEST_INSIGHT));
            takeScreenshot(browser, "testInsightTitleInADAfterRenamedOnDashboard-insightWithNewNameNotFound", getClass());
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), dashboardUri);
        }
    }

    @Test(dependsOnGroups = {"setupDashboardENV"})
    public void testCreatingInsightsForFilterTest() throws ParseException, IOException, JSONException {
        initProjectsPage();

        createInsight(getRestApiClient(testParams.getEditorUser(), testParams.getPassword()), testParams.getProjectId(),
                new InsightMDConfiguration(INSIGHT_CREATED_BY_EDITOR, ReportType.BAR_CHART));

        createInsight(getRestApiClient(), testParams.getProjectId(),
                new InsightMDConfiguration(INSIGHT_CREATED_BY_MAIN_USER, ReportType.BAR_CHART));

        // need refresh to make sure the insights are added to working project
        browser.navigate().refresh();
        waitForProjectsPageLoaded(browser);

        assertEquals(
                getAllInsightNames(getRestApiClient(), testParams.getProjectId()).stream()
                        .filter(INSIGHTS_FOR_FILTER_TEST::contains).count(),
                2, "The number of created insights is not correct");
    }

    @Test(dependsOnMethods = {"testCreatingInsightsForFilterTest"})
    public void testInsightListWithCreatedByMeFilter() {
        final List<InsightItem> insights = initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .getInsightSelectionPanel()
            .waitForInsightListVisible()
            .getInsightItems();
        takeScreenshot(browser, "Test-Insight-List-With-Created-By-Me-Filter", getClass());
        assertTrue(insights.stream().anyMatch(e -> e.matchesTitle(INSIGHT_CREATED_BY_MAIN_USER)),
                INSIGHT_CREATED_BY_MAIN_USER + " does not exist on result list");

        // ONE-1653: List of insights created by me show as all insights on KPIs
        assertFalse(insights.stream().anyMatch(e -> e.matchesTitle(INSIGHT_CREATED_BY_EDITOR)),
                INSIGHT_CREATED_BY_EDITOR + " exists on result list");
    }

    @Test(dependsOnMethods = {"testCreatingInsightsForFilterTest"})
    public void testInsightListWithAllFilter() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .getInsightSelectionPanel()
            .switchFilter(FilterType.ALL)
            .searchInsight("Insight-Created-By");

        assertEquals(
                indigoDashboardsPage.getInsightSelectionPanel()
                        .waitForInsightListVisible()
                        .getInsightItems()
                        .stream()
                        .filter(insight -> INSIGHTS_FOR_FILTER_TEST.stream()
                                .anyMatch(insight::matchesTitle))
                        .count(),
                2, "The expected insights are not displayed");
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testNoMatchingSearch() {
        final String nonExistingInsight = "Non-Existing-Insight";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(nonExistingInsight);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(), "No data message is not displayed");
    }

    @DataProvider(name = "specialInsightNameProvider")
    public Object[][] specialInsightNameProvider() {
        return new Object[][] {
            { "report !@#$" },
            { "<a href=\"http://www.w3schools.com\">Visit W3Schools.com!</a>" },
            { "                     " }
        };
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"}, dataProvider = "specialInsightNameProvider")
    public void testSearchUsingSpecialValues(final String searchValue) {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(searchValue);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message is not dispayed");
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"})
    public void testClearSearchInput() {
        final String insight = "Test-Clear-Search-Input";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(insight);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message is not dispayed");

        indigoDashboardsPage.getInsightSelectionPanel().clearInputText();
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isSearchTextBoxEmpty(),
                "Search text box is not empty");
        assertFalse(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message still exists after clicking on clear icon");
    }

    @Test(dependsOnGroups = {"setupDashboardENV", "createInsight"},
            description = "ONE-1671: Search insights in dashboard always switch to created by me tab after clear search keyword")
    public void testSelectedFilterAfterClearingInput() {
        final String insight = "Test-Selected-Filter-After-Clearing-Search-Input";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .getInsightSelectionPanel()
            .switchFilter(FilterType.ALL);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterActive(FilterType.ALL),
                "All tab was not selected ");

        indigoDashboardsPage.searchInsight(insight);
        indigoDashboardsPage.getInsightSelectionPanel().clearInputText();
        takeScreenshot(browser, "Test-Selected-Filter-After-Clearing-Input", getClass());
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isFilterActive(FilterType.ALL),
                "All tab was not selected");
    }

    @DataProvider(name = "filterTypeProvider")
    public Object[][] filterTypeProvider() {
        return new Object[][] {
            {FilterType.ALL, INSIGHTS_FOR_FILTER_TEST},
            {FilterType.BY_ME, singletonList(INSIGHT_CREATED_BY_MAIN_USER)}
        };
    }

    @Test(dependsOnMethods = {"testCreatingInsightsForFilterTest"}, dataProvider = "filterTypeProvider")
    public void testInsightListWithFilterAfterClearingSearchInput(FilterType type,
            final List<String> expectedInsights) {
        final String nonExistingInsight = "Non-Existing-Insight";
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .searchInsight(nonExistingInsight);
        assertTrue(indigoDashboardsPage.getInsightSelectionPanel().isEmpty(),
                "No data message is not dispayed");

        indigoDashboardsPage.getInsightSelectionPanel().clearInputText().switchFilter(type);

        final List<String> expectedInsightClasses = expectedInsights.stream()
                .map(e -> "s-" + simplifyText(e))
                .collect(toList());

        final List<String> insights = indigoDashboardsPage.getInsightSelectionPanel()
                .waitForInsightListVisible()
                .getInsightItems().stream()
                        .filter(e -> expectedInsightClasses.stream().anyMatch(t -> e.getCSSClass().contains(t)))
                        .map(InsightItem::getCSSClass)
                        .collect(toList());

        assertEquals(insights.size(), expectedInsights.size(), "The expected insights are not displayed");
    }

    @Test(dependsOnGroups = {"setupDashboardENV"},
            description = "CL-10262: Save&Publish button is enabled right when selecting insight")
    public void disableSaveIfHavingNoChange() throws JSONException, IOException {
        String insight = "Insight-Created-From-Metric";
        initAnalysePage().addMetric(METRIC_NUMBER_OF_ACTIVITIES).waitForReportComputing().saveInsight(insight);

        initIndigoDashboardsPage().getSplashScreen().startEditingWidgets().addInsight(insight)
                .saveEditModeWithWidgets();

        try {
            initIndigoDashboardsPageWithWidgets().switchToEditMode().selectLastWidget(Insight.class);
            assertFalse(indigoDashboardsPage.isSaveEnabled(),
                    "Save button is enabled when dashboard has no change");
        } finally {
            deleteAnalyticalDashboard(getRestApiClient(), getWorkingDashboardUri());
        }
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
    }

    private void checkInsightRender(final Insight insight, final String expectedHeadline,
            final int expectedTracker) {
        assertEquals(insight.getHeadline(), expectedHeadline);
        assertEquals(insight.getChartReport().getTrackersCount(), expectedTracker,
                "The chart is not rendered correctly");
    }
}
