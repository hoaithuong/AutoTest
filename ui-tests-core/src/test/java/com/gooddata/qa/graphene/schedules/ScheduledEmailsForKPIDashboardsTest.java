package com.gooddata.qa.graphene.schedules;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.graphene.entity.kpi.KpiConfiguration;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.DateRange;
import com.gooddata.qa.graphene.enums.indigo.FieldType;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.analyze.pages.AnalysisPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.ScheduleEmailDialog;
import com.gooddata.qa.graphene.fragments.manage.MetricFormatterDialog;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.fact.FactRestRequest;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.project.ProjectRestRequest;
import com.gooddata.qa.utils.http.scheduleEmail.ScheduleEmailRestRequest;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.*;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class ScheduledEmailsForKPIDashboardsTest extends AbstractGoodSalesEmailSchedulesTest {

    private String DASHBOARD_CONTAIN_XSS = "<button> abc </button>";
    private static final String INSIGHT_HAS_RESTRICTED_FACT = "Restricted fact";
    private static final String INSIGHT_HAS_XSS = "Metric XSS";
    private String identification;
    private IndigoRestRequest indigoRestRequest;
    private ProjectRestRequest projectRestRequest;
    private CommonRestRequest commonRestRequest;
    private FactRestRequest factRestRequest;
    private String today;

    @BeforeClass
    public void setUp() {
        identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        attachmentsDirectory =
            new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"schedules"})
    public void signInImapUser() throws JSONException {
        logout();
        signInAtGreyPages(imapUser, imapPassword);
    }

    @AfterClass
    public void validateProjectAfterClass() throws JSONException {
        validateAfterClass = false;
    }

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        addUserToProject(imapUser, UserRoles.ADMIN);
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createAmountMetric();
        getMetricCreator().createAvgAmountMetric();
        getMetricCreator().createAmountBOPMetric();
        getMetricCreator().createWonMetric();
        getMetricCreator().createBestCaseMetric();
        getMetricCreator().createWinRateMetric();
        factRestRequest = new FactRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        commonRestRequest = new CommonRestRequest(getAdminRestClient(), testParams.getProjectId());
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
        projectRestRequest = new ProjectRestRequest(getAdminRestClient(), testParams.getProjectId());
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_SCHEDULE, true);
        projectRestRequest.setFeatureFlagInProjectAndCheckResult(ProjectFeatureFlags.ENABLE_METRIC_DATE_FILTER, true);
    }

    @Test(dependsOnGroups = "createProject")
    public void cannotCreateScheduleEmail() {
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        assertFalse(indigoDashboardsPage.isHeaderOptionsButtonVisible(),
            "There should be not schedule email option");

        indigoDashboardsPage.addDashboard();
        assertFalse(indigoDashboardsPage.isHeaderOptionsButtonVisible(),
            "There should be not schedule email option");
    }

    @Test(dependsOnMethods = "signInImapUser", dataProvider = "getAllRolesUser")
    public void scheduleEmailWithAllRolesUser(UserRoles userRoles) throws IOException, MessagingException{
        addUserToProject(imapUser, userRoles);
        signInAtGreyPages(imapUser, imapPassword);
        try {
            String DASHBOARD_TITLE_ALL_USER_ROLES = "Dashboard All User Roles";
            DASHBOARD_TITLE_ALL_USER_ROLES = DASHBOARD_TITLE_ALL_USER_ROLES + " - " + userRoles.toString()
                + " - " + identification;
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
            addKPIToDashboard(indigoDashboardsPage, DASHBOARD_TITLE_ALL_USER_ROLES);
            String expectedResult = indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AVG_AMOUNT).getValue();

            indigoDashboardsPage.scheduleEmailing().submit();

            updateRecurrencyString(commonRestRequest.getLastScheduleUri());
            List<String> contents =
                asList(getFirstPdfContentFrom(waitForScheduleMessages(DASHBOARD_TITLE_ALL_USER_ROLES, 1).get(0)).split("\n"));
            today = DateRange.getCurrentDate();
            log.info("Content: " + contents.toString());
            assertThat(contents, hasItems(METRIC_AVG_AMOUNT, today, "Page 1/1", expectedResult));
        } finally {
            addUserToProject(imapUser, UserRoles.ADMIN);
        }
    }

    @DataProvider(name = "getAllRolesUser")
    public Object[][] getAllRolesUser() {
        return new Object[][]{
            {UserRoles.ADMIN},
            {UserRoles.EDITOR},
        };
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void scheduleEmailXSSNames() throws IOException, MessagingException{
        DASHBOARD_CONTAIN_XSS = DASHBOARD_CONTAIN_XSS + identification;
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addKPIToDashboard(indigoDashboardsPage, DASHBOARD_CONTAIN_XSS);
        String expectedResult = indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AVG_AMOUNT).getValue();
        indigoDashboardsPage.scheduleEmailing().submit();

        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String > contents = asList(getFirstPdfContentFrom(
            waitForScheduleMessages(DASHBOARD_CONTAIN_XSS, 1).get(0)).split("\n"));

        today = DateRange.getCurrentDate();
        log.info("Content: " + contents.toString());
        assertThat(contents, hasItems(METRIC_AVG_AMOUNT, today, "Page 1/1", expectedResult));
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void prepareDashboardWithSpecialCases() {
        createSimpleInsight(REPORT_NO_DATA, METRIC_WON);
        AnalysisPage analysisPage = initAnalysePage();
        analysisPage.openInsight(REPORT_NO_DATA).waitForReportComputing().getMetricsBucket()
            .getMetricConfiguration(METRIC_WON).expandConfiguration()
            .addFilterByDate(DATE_DATASET_CLOSED, "01/01/2019", "01/01/2019");
        analysisPage.saveInsight();

        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(FACT_AMOUNT, FieldType.FACT)
            .addAttribute(ATTR_ACCOUNT).addAttribute(ATTR_OPP_SNAPSHOT).saveInsight(REPORT_TOO_LARGE)
            .waitForReportComputing();

        createSimpleInsight(REPORT_INCOMPUTABLE, METRIC_AMOUNT);
        analysisPage = initAnalysePage();
        analysisPage.openInsight(REPORT_INCOMPUTABLE).addDate().waitForReportComputing().getMetricsBucket()
            .getMetricConfiguration(METRIC_AMOUNT).expandConfiguration()
            .addFilterByDate(DATE_DATASET_CLOSED, "01/01/5000", "01/01/5000");
        analysisPage.saveInsight();

        String factUri = factRestRequest.getFactByTitle(METRIC_AMOUNT).getUri();
        factRestRequest.setFactRestricted(factUri);
        initAnalysePage().changeReportType(ReportType.TABLE).addMetric(METRIC_AMOUNT, FieldType.FACT)
            .waitForReportComputing().saveInsight(INSIGHT_HAS_RESTRICTED_FACT).waitForReportComputing();

        createMetric("metricXSSFormat", format(
            "SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(METRIC_AMOUNT))),
            MetricFormatterDialog.Formatter.XSS.toString());
        initAnalysePage().changeReportType(ReportType.COLUMN_CHART).addMetric("metricXSSFormat").saveInsight(INSIGHT_HAS_XSS);
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void inputEmailAddressOnToField() {
        String nameDashboard = "Dashboard Test To Field" + identification;
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addKPIToDashboard(indigoDashboardsPage, nameDashboard);

        ScheduleEmailDialog scheduleEmailDialog = indigoDashboardsPage.scheduleEmailing();
        assertEquals(scheduleEmailDialog.getRecipientValues().size(), 1);
        scheduleEmailDialog.clickToField();
        assertThat(scheduleEmailDialog.getRecipientsSuggestion().toString(), containsString(testParams.getUser()));

        scheduleEmailDialog.addRecipientToField(testParams.getUser());
        scheduleEmailDialog.removeRecipients();
        scheduleEmailDialog.clickToField();
        assertThat(scheduleEmailDialog.getRecipientsSuggestion().toString(), containsString(testParams.getUser()));

        scheduleEmailDialog.addRecipientToField("mabdalhammedmabdalhammed.awan@senerza.press");
        assertThat(scheduleEmailDialog.getRecipientValues(), hasItem("mabdalhammedmabdalhammed.awan@senerza.press"));
        scheduleEmailDialog.removeRecipients();

        scheduleEmailDialog.clickToField();
        new Actions(browser).sendKeys("mabdalhammedmabdalhammed.awan@senerza.press").perform();
        takeScreenshot(browser, "An special email outside the workspace", getClass());
        assertEquals(scheduleEmailDialog.getMessageText(), "This email address does not belong to the " +
            "workspace. Recipient may receive sensitive data.");
        new Actions(browser).sendKeys(Keys.ENTER).perform();
        scheduleEmailDialog.removeRecipients();

        new Actions(browser).sendKeys("mabdalhammedmabdalhammed.awan").sendKeys(Keys.ENTER).perform();
        takeScreenshot(browser, "An wrong format email", getClass());
        assertTrue(scheduleEmailDialog.isRecipientValueWrongFormat("mabdalhammedmabdalhammed.awan"));
        scheduleEmailDialog.removeRecipients();

        IntStream.range(0, 20).forEach(number -> scheduleEmailDialog.addRecipientToField(number + "@email.com"));
        assertEquals(scheduleEmailDialog.getRecipientValues().size(), 20);
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void cannotInputRecipientIntoToField() {
        try {
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_SCHEDULE_RECIPIENTS, false);
            IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
            addKPIToDashboard(indigoDashboardsPage, "Dashboard");
            ScheduleEmailDialog scheduleEmailDialog = indigoDashboardsPage.scheduleEmailing();
            assertFalse(scheduleEmailDialog.isRecipientsInputPresent(), "Recipipents Input should be not present");
        } finally {
            initIndigoDashboardsPage().switchToEditMode().deleteDashboardOnMenuItem(true);
            projectRestRequest.setFeatureFlagInProjectAndCheckResult(
                ProjectFeatureFlags.ENABLE_KPI_DASHBOARD_SCHEDULE_RECIPIENTS, true);
        }
    }

    @Test(dependsOnMethods = "signInImapUser")
    public void setTimeAndBodyForScheduleEmail() throws IOException, MessagingException{
        String nameDashboard = "<button>Dashboard Set Time<button>" + identification;
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage();
        addKPIToDashboard(indigoDashboardsPage, nameDashboard);
        String expectedResult = indigoDashboardsPage.getWidgetByHeadline(Kpi.class, METRIC_AVG_AMOUNT).getValue();

        indigoDashboardsPage.scheduleEmailing().setDate("01/1/2020").chooseTime("12:00 AM")
                .chooseRepeats("Custom").chooseRepeatsFrequency("Month").setSubject(nameDashboard);
        ScheduleEmailDialog.getInstance(browser).submit();

        updateRecurrencyString(commonRestRequest.getLastScheduleUri());
        List<String > contents = asList(getFirstPdfContentFrom(
                waitForScheduleMessages(nameDashboard, 1).get(0)).split("\n"));

        today = DateRange.getCurrentDate();
        log.info("Content: " + contents.toString());
        assertThat(contents, hasItems(METRIC_AVG_AMOUNT, today, "Page 1/1", expectedResult));
    }

    protected ScheduleEmailRestRequest initScheduleEmailRestRequest() {
        return new ScheduleEmailRestRequest(new RestClient(
            new RestClient.RestProfile(testParams.getHost(), imapUser, imapPassword, true)), testParams.getProjectId());
    }

    private void createSimpleInsight(String title, String metric) {
        indigoRestRequest.createInsight(
            new InsightMDConfiguration(title, ReportType.TABLE)
                .setMeasureBucket(
                    singletonList(MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric)))));
    }

    private void addKPIToDashboard(IndigoDashboardsPage indigoDashboardsPage, String nameDashboard) {
        indigoDashboardsPage.addDashboard().addKpi(new KpiConfiguration.Builder()
            .metric(METRIC_AVG_AMOUNT)
            .dataSet(DATE_DATASET_CREATED)
            .build()).changeDashboardTitle(nameDashboard).saveEditModeWithWidgets();
        indigoDashboardsPage.openExtendedDateFilterPanel().selectPeriod(DateRange.ALL_TIME).apply();
    }
}
