package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.DATE_DATASET_CREATED;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import java.io.IOException;

import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import org.json.JSONException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class RoutingTest extends AbstractDashboardTest {
    private static KpiConfiguration kpi; 

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        kpi = new KpiConfiguration.Builder()
            .metric(METRIC_AMOUNT)
            .dataSet(DATE_DATASET_CREATED)
            .comparison(Kpi.ComparisonType.NO_COMPARISON.toString())
            .build();
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkRedirectedToProjectRoute() throws JSONException {
        String url = PAGE_INDIGO_DASHBOARDS + "#/p/" + testParams.getProjectId();
        openUrl(url);

        waitForOpeningIndigoDashboard();
        IndigoDashboardsPage.getInstance(browser).getSplashScreen();

        assertTrue(browser.getCurrentUrl().contains(getIndigoDashboardsPageUri()),
                "Expecting /p/ to be redirected to /project/");
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"desktop"})
    public void checkDashboardIdentifierAppended() throws JSONException, IOException {
        initIndigoDashboardsPage()
            .getSplashScreen()
            .startEditingWidgets()
            .addKpi(kpi)
            .saveEditModeWithWidgets();

        try {
            initIndigoDashboardsPageWithWidgets();
            assertThat(browser.getCurrentUrl(), containsString("/dashboard/"));
        } finally {
            new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                    .deleteAnalyticalDashboard(getWorkingDashboardUri());
        }
    }
}
