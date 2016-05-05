package com.gooddata.qa.graphene.indigo.dashboards;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.graphene.fragments.indigo.dashboards.KpiAlertDialog.TRIGGERED_WHEN_GOES_ABOVE;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.dashboards.DashboardsRestUtils.changeMetricExpression;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createAnalyticalDashboard;
import static com.gooddata.qa.utils.http.indigo.IndigoRestUtils.createKpiWidget;
import static com.gooddata.qa.utils.mail.ImapUtils.areMessagesArrived;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils.deleteUserByEmail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.md.Attribute;
import com.gooddata.md.Dataset;
import com.gooddata.md.Fact;
import com.gooddata.md.Metric;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestUtils;

public class KpiAlertSpecialCaseTest extends AbstractProjectTest {

    private static final String DATASET_NAME = "User";
    private static final String OTHER_DATASET_NAME = "Other User";

    private String csvFilePath;
    private String otherCsvFilePath;

    private String numberFactUri;
    private String dateDatasetUri;

    private String sumOfNumberMetricUri;

    @BeforeClass(alwaysRun = true)
    public void initImapUser() {
        imapHost = testParams.loadProperty("imap.host");
        imapUser = testParams.loadProperty("imap.user");
        imapPassword = testParams.loadProperty("imap.password");
    }

    @Test(dependsOnMethods = "createProject", groups = "precondition")
    public void inviteUserToProject() throws ParseException, IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
        logoutAndLoginAs(imapUser, imapPassword);
    }

    @Test(dependsOnMethods = "inviteUserToProject", groups = "precondition")
    public void initData() throws IOException {
        csvFilePath = new CsvFile(DATASET_NAME)
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"), new CsvFile.Column("Date"))
                .rows("Khoa", "15", getCurrentDate())
                .saveToDisc(testParams.getCsvFolder());

        otherCsvFilePath = new CsvFile(OTHER_DATASET_NAME)
                .columns(new CsvFile.Column("Firstname"), new CsvFile.Column("Number"), new CsvFile.Column("Date"))
                .rows("Khoa", "5", getCurrentDate())
                .saveToDisc(testParams.getCsvFolder());

        uploadCSV(csvFilePath);

        numberFactUri = getMdService().getObjUri(getProject(), Fact.class, title("Number"));

        dateDatasetUri = getMdService().getObjUri(getProject(), Dataset.class,
                identifier("date.dataset.dt"));

        String maqlExpression = format("SELECT SUM([%s])", numberFactUri);

        sumOfNumberMetricUri = getMdService()
                .createObj(getProject(), new Metric("SumOfNumber", maqlExpression, "#,##0"))
                .getUri();
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop")
    public void checkAlertWithSpecialMetricExpression() throws JSONException, IOException {
        String yearAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, identifier("date.year"));

        String maqlExpression = format("SELECT SUM([%s]) WHERE [%s] = this", numberFactUri, yearAttributeUri);

        Metric metric = getMdService().createObj(getProject(),
                new Metric(generateUniqueName(), maqlExpression, "#,##0"));

        String kpiName = metric.getTitle();
        String kpiUri = createKpi(kpiName, metric.getUri());

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

            updateCsvDataset(DATASET_NAME, csvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
            getMdService().removeObj(metric);
        }
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop")
    public void checkAlertWhenChangeMetricExpression() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

            String firstNameAttributeUri = getMdService().getObjUri(getProject(), Attribute.class, title("Firstname"));
            String firstNameValueUri = firstNameAttributeUri + "/elements?id=2";

            String newMaqlExpression = format(
                    "SELECT SUM([%s]) WHERE [%s] = [%s]", numberFactUri, firstNameAttributeUri, firstNameValueUri);

            changeMetricExpression(getRestApiClient(), sumOfNumberMetricUri, newMaqlExpression);

            updateCsvDataset(DATASET_NAME, csvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop")
    public void checkAlertWhenUploadAnotherDataset() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

            uploadCSV(otherCsvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnMethods = "checkAlertWhenUploadAnotherDataset", groups = "desktop")
    public void checkAlertWhenUpdateAnotherDataset() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

            updateCsvDataset(OTHER_DATASET_NAME, otherCsvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop")
    public void checkPlainMailSendWithAlert() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

            updateCsvDataset(DATASET_NAME, csvFilePath);

            Kpi kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is not sent to mailbox");

            updateCsvDataset(DATASET_NAME, otherCsvFilePath);

            kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-not-triggered", getClass());
            assertFalse(kpi.isAlertTriggered(), "Kpi alert is triggered although the condition is not reached");

            assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 2)),
                    "Alert email is sent to mailbox again");

            updateCsvDataset(DATASET_NAME, csvFilePath);

            kpi = initIndigoDashboardsPageWithWidgets().getKpiByHeadline(kpiName);

            takeScreenshot(browser, "Kpi-" + kpiName + "-alert-triggered-again", getClass());
            assertTrue(kpi.isAlertTriggered(), "Kpi alert is not triggered");

            assertTrue(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 2)),
                    "Alert email is not sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop")
    public void removeKpiAfterSettingAlert() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

            getMdService().removeObjByUri(kpiUri);

            updateCsvDataset(DATASET_NAME, csvFilePath);

            assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is sent to mailbox");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop")
    public void removeDashboardAfterSettingAlert() throws JSONException, IOException {
        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

        } finally {
            getMdService().removeObjByUri(indigoDashboardUri);
        }

        updateCsvDataset(DATASET_NAME, csvFilePath);

        assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                "Alert email is sent to mailbox");
    }

    @Test(dependsOnGroups = "precondition", groups = "desktop")
    public void removeUserAfterSettingAlert() throws JSONException, IOException {
        String newImapUser = generateEmail(imapUser);

        UserManagementRestUtils.createUser(getRestApiClient(), newImapUser, imapPassword);
        addUserToProject(newImapUser, UserRoles.ADMIN);

        String kpiName = generateUniqueName();
        String kpiUri = createKpi(kpiName, sumOfNumberMetricUri);

        String indigoDashboardUri = createAnalyticalDashboard(
                getRestApiClient(), testParams.getProjectId(), singletonList(kpiUri));

        try {
            logoutAndLoginAs(newImapUser, imapPassword);

            setAlertForKpi(kpiName, TRIGGERED_WHEN_GOES_ABOVE, "10");

            logoutAndLoginAs(imapUser, imapPassword);

            deleteUserByEmail(getRestApiClient(), newImapUser);

            updateCsvDataset(DATASET_NAME, csvFilePath);

            assertFalse(doActionWithImapClient(imapClient -> areMessagesArrived(imapClient, GDEmails.NOREPLY, kpiName, 1)),
                    "Alert email is sent to mailbox");

        } finally {
            logoutAndLoginAs(imapUser, imapPassword);

            deleteUserByEmail(getRestApiClient(), newImapUser);

            getMdService().removeObjByUri(indigoDashboardUri);
        }
    }

    private String generateUniqueName() {
        return "sumOfNumber-" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String createKpi(String title, String metricUri) throws JSONException, IOException {
        return createKpiWidget(getRestApiClient(), testParams.getProjectId(),
                new KpiMDConfiguration.Builder()
                        .title(title)
                        .metric(metricUri)
                        .dateDataSet(dateDatasetUri)
                        .comparisonType(ComparisonType.PREVIOUS_PERIOD)
                        .comparisonDirection(ComparisonDirection.GOOD)
                        .build());
    }

    private void setAlertForKpi(String title, String triggerCondition, String threshold) {
        initIndigoDashboardsPageWithWidgets()
                .getKpiByHeadline(title)
                .openAlertDialog()
                .selectTriggeredWhen(triggerCondition)
                .setThreshold(threshold)
                .setAlert();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private void logoutAndLoginAs(String username, String password) throws JSONException {
        logout();
        signInAtGreyPages(username, password);
    }
}
