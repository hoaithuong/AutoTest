package com.gooddata.qa.graphene.indigo.dashboards;

import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.graphene.indigo.dashboards.common.AbstractDashboardTest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.getAnalyticalDashboards;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Need to run on empty project because neither csv upload (upload.html) nor
 * webdav upload work for GoodSales demo project (old template, dli/sli)
 */
public class KpiAlertEvaluateTest extends AbstractDashboardTest {

    private static final String KPI_DATE_DIMENSION = "dt_minimalistic.dataset.dt";

    private static final String MAQL_PATH = "/minimalistic/minimalistic-maql.txt";

    private static final String CSV_PATH = "/minimalistic/minimalistic.csv";
    private static final String UPLOADINFO_PATH = "/minimalistic/upload_info.json";

    private static final String CSV_INCREASED_PATH = "/minimalistic-increased/minimalistic-increased.csv";
    private static final String UPLOADINFO_INCREASED_PATH = "/minimalistic-increased/upload_info.json";

    private String factUri;
    private String dateDatasetUri;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Override
    public void initProperties() {
        // create empty project and use customized data8
        // init imap properties
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        setupMaql(LdmModel.loadFromFile(MAQL_PATH));
        factUri = getFactByTitle("Fact").getUri();
        dateDatasetUri = getDatasetByIdentifier(KPI_DATE_DIMENSION).getUri();
        logout();
        signInAtGreyPages(imapUser, imapPassword);
    }

    @DataProvider(name = "alertsProvider")
    public Object[][] alertsProvider() {
        Supplier<Metric> metric1 = () -> createMetric("Metric-" + generateHashString(),
                format("select sum([%s])", factUri), "#,##0.00");
        Supplier<Metric> metric2 = () -> createMetric("Metric-" + generateHashString(),
                format("select sum([%s]) / 100", factUri), "#,##0.00%");

        return new Object[][] {
            {metric1, "2"},
            {metric2, "50"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "alertsProvider", groups = "desktop")
    public void checkKpiAlertEvaluation(Supplier<Metric> metricSupplier, String threshold)
            throws JSONException,IOException, URISyntaxException {

        try {
            setupData(CSV_PATH, UPLOADINFO_PATH);

            Metric metric = metricSupplier.get();
            String kpiUri = createKpiUsingRest(createDefaultKpiConfigure(metric, dateDatasetUri));
            createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

            initIndigoDashboardsPageWithWidgets();
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, threshold);

            setupData(CSV_INCREASED_PATH, UPLOADINFO_INCREASED_PATH);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getLastWidget(Kpi.class);
            assertTrue(kpi.isAlertTriggered(), "Kpi " + metric.getTitle() + " alert is not triggered");

            if (!testParams.isClusterEnvironment()) return;

            Document email = getLastAlertEmailContent(GDEmails.NOREPLY, metric.getTitle());
            kpi = openDashboardFromLink(getDashboardLinkFromEmail(email)).getLastWidget(Kpi.class);

            takeScreenshot(browser, "Kpi-" + metric.getTitle() + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi " + metric.getTitle() + " alert is not triggered");

        } finally {
            getMdService().removeObjByUri(getAnalyticalDashboards(restApiClient, testParams.getProjectId()).get(0));
        }
    }


    @DataProvider(name = "viewPermissionProviderNonEmbeddedModeWithHidingParams")
    public Object[][] getViewPermissionProviderNonEmbeddedModeWithHidingParams() {
        return new Object[][]{
                {"showNavigation=false"},
                {"showNavigation=null"},
                {"showNavigation=0"},
        };
    }
    @Test(dependsOnGroups = {"createProject"}, dataProvider = "viewPermissionProviderNonEmbeddedModeWithHidingParams",
            groups = "desktop")
    public void testShowingNavigationParamInAlertEmail(String params) throws JSONException, IOException {
        try {
            setupData(CSV_PATH, UPLOADINFO_PATH);

            Metric metric = createMetric("Metric-" + generateHashString(),
                    format("select sum([%s])", factUri), "#,##0.00");
            String kpiUri = createKpiUsingRest(createDefaultKpiConfigure(metric, dateDatasetUri));
            createAnalyticalDashboard(getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

            initIndigoDashboardsPageWithWidgets(params);
            setAlertForLastKpi(TRIGGERED_WHEN_GOES_ABOVE, "2");
            setupData(CSV_INCREASED_PATH, UPLOADINFO_INCREASED_PATH);

            Document email = getLastAlertEmailContent(GDEmails.NOREPLY, metric.getTitle());
            openDashboardFromLink(getDashboardLinkFromEmail(email));
            assertFalse(browser.getCurrentUrl().contains(params),
                    "Dashboard clicked from alert email should not contain showNavigation param");
        } finally {
            getMdService().removeObjByUri(getAnalyticalDashboards(restApiClient, testParams.getProjectId()).get(0));
        }
    }

    private String getDashboardLinkFromEmail(Document email) {
        String dashboardUrl = email.getElementsByAttributeValueMatching("class", "s-kpi-link").first().attr("href");

        Pattern pattern = Pattern.compile("https://.*\\.com/");
        Matcher matcher = pattern.matcher(dashboardUrl);
        if (!matcher.find()) {
            throw new RuntimeException("Dashboard link not contain test host domain!");
        }

        return dashboardUrl.replace(matcher.group(), "");
    }

    private IndigoDashboardsPage openDashboardFromLink(String link) {
        openUrl(link);
        waitForOpeningIndigoDashboard();
        return IndigoDashboardsPage.getInstance(browser).waitForDashboardLoad();
    }

    private KpiMDConfiguration createDefaultKpiConfigure(Metric metric, String dateDatasetUri) {
        return new KpiMDConfiguration.Builder()
                .title(metric.getTitle())
                .metric(metric.getUri())
                .dateDataSet(dateDatasetUri)
                .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(ComparisonDirection.GOOD)
                .build();
    }
}
