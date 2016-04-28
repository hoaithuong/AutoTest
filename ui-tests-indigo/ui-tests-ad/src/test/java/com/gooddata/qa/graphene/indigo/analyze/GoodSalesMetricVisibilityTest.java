package com.gooddata.qa.graphene.indigo.analyze;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.enums.user.UserRoles;

public class GoodSalesMetricVisibilityTest extends AnalyticalDesignerAbstractTest {

    private static final String RATIO_METRIC = "Ratio metric";

    @BeforeClass(alwaysRun = true)
    public void initialize() {
        projectTitle += "Metric-Visibility-Test";
    }

    @Test(dependsOnGroups = {"init"}, groups = {"precondition"})
    public void addUsersWithOtherRolesToProject() throws ParseException, IOException, JSONException {
        super.addUsersWithOtherRolesToProject();
    }

    @Test(dependsOnMethods = {"addUsersWithOtherRolesToProject"}, groups = {"precondition"})
    public void createPrivateMetric() {
        assertTrue(deleteMetric(RATIO_METRIC));
        initMetricPage();
        waitForFragmentVisible(metricPage).createRatioMetric(RATIO_METRIC, NUMBER_OF_WON_OPPS,
                NUMBER_OF_OPEN_OPPS);

        String expectedMaql = "SELECT " + NUMBER_OF_WON_OPPS + " / " + NUMBER_OF_OPEN_OPPS;
        assertTrue(metricPage.isMetricCreatedSuccessfully(RATIO_METRIC, expectedMaql, "#,##0.00"));
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"test"})
    public void testPrivateMetric() {
        assertEquals(analysisPage.addMetric(RATIO_METRIC)
                .addAttribute(DEPARTMENT)
                .waitForReportComputing()
                .getChartReport()
                .getTrackersCount(), 2);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"test"})
    public void testPrivateMetricVisibility() throws JSONException, IOException {
        try {
            initDashboardsPage();

            logout();
            signIn(false, UserRoles.EDITOR);

            initAnalysePage();
            assertFalse(analysisPage.getCataloguePanel().search(RATIO_METRIC));
        } finally {
            logout();
            signIn(false, UserRoles.ADMIN);
        }
    }

    private boolean deleteMetric(String metric) {
        initMetricPage();
        if (!waitForFragmentVisible(metricPage).isMetricVisible(RATIO_METRIC)) {
            return true;
        }
        metricPage.openMetricDetailPage(RATIO_METRIC);
        waitForFragmentVisible(metricDetailPage).deleteMetric();

        initMetricPage();
        return !waitForFragmentVisible(metricPage).isMetricVisible(RATIO_METRIC);
    }
}
