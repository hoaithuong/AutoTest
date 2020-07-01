/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static com.gooddata.sdk.model.md.report.MetricGroup.METRIC_GROUP;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_DISMISS_BUTTON;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR;
import static com.gooddata.qa.graphene.utils.CheckUtils.BY_RED_BAR_WARNING;
import static com.gooddata.qa.graphene.utils.CheckUtils.checkRedBar;
import static com.gooddata.qa.graphene.utils.CheckUtils.logRedBarMessageInfo;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_ACTIVITY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_PRODUCT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.ATTR_YEAR_SNAPSHOT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_AMOUNT;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.METRIC_NUMBER_OF_ACTIVITIES;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_ACTIVITIES_BY_TYPE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_INCOMPUTABLE;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_NO_DATA;
import static com.gooddata.qa.graphene.utils.GoodSalesUtils.REPORT_TOO_LARGE;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEqualCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.mdObjects.dashboard.Dashboard;
import com.gooddata.qa.mdObjects.dashboard.tab.Tab;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.dashboards.DashboardRestRequest;
import com.gooddata.qa.utils.http.scheduleEmail.ScheduleEmailRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest.UserStatus;
import com.gooddata.qa.utils.java.Builder;
import org.apache.http.ParseException;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.md.Attribute;
import com.gooddata.sdk.model.md.Metric;
import com.gooddata.sdk.model.md.report.AttributeInGrid;
import com.gooddata.sdk.model.md.report.GridReportDefinitionContent;
import com.gooddata.sdk.model.md.report.MetricElement;
import com.gooddata.sdk.model.md.report.Report;
import com.gooddata.sdk.model.md.report.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.entity.variable.AttributeVariable;
import com.gooddata.qa.graphene.entity.variable.NumericVariable;
import com.gooddata.qa.graphene.enums.GDEmails;
import com.gooddata.qa.graphene.enums.report.ExportFormat;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage;
import com.gooddata.qa.graphene.fragments.manage.EmailSchedulePage.RepeatTime;
import com.gooddata.qa.utils.mail.ImapClient;
import com.gooddata.qa.utils.mail.ImapUtils;

public class GoodSalesEmailSchedulesFullTest extends AbstractGoodSalesEmailSchedulesTest {

    private String emptyDashboardTitle = "Empty-Dashboard";
    private String filteredVariableReportTitle = "Filtered-Variable-Report";
    private String numericVariableReportTitle = "Numeric-Variable-Report";
    private String mufReportTitle = "MUF-Report";
    private String noDataReportTitle = REPORT_NO_DATA;
    private String incomputableReportTitle = REPORT_INCOMPUTABLE;
    private String tooLargeReportTitle = REPORT_TOO_LARGE;

    private static final String DASHBOARD_HAVING_TAB = "Dashboard having tab";
    private static final String OTHER_DASHBOARD_HAVING_TAB = "Other dashboard having tab";
    private static final String EDITOR_EMAIL = "editoremail@gooddata.com";

    private Map<String, List<Message>> messages;
    private Map<String, MessageContent> attachments = new HashMap<String, MessageContent>();
    private String dashboardTitle = "Dashboard Scheduled Email";

    private DashboardRestRequest dashboardRequest;
    private UserManagementRestRequest userManagementRestRequest;

    @Override
    protected void addUsersWithOtherRolesToProject() throws IOException, JSONException {
        addUserToProject(imapUser, UserRoles.ADMIN);
        String editor = createAndAddUserToProject(UserRoles.EDITOR);
        new UserManagementRestRequest(new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId())
                .updateEmailOfAccount(testParams.getUserDomain(), editor, EDITOR_EMAIL);
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        attachmentsDirectory = new File(getProperty("maven.project.build.directory", "./target/attachments"));

        List<Message> emptyMessage = Collections.emptyList();
        messages = new HashMap<String, List<Message>>();
        messages.put(emptyDashboardTitle += identification, emptyMessage);
        messages.put(noDataReportTitle += identification, emptyMessage);
        messages.put(incomputableReportTitle += identification, emptyMessage);
        messages.put(tooLargeReportTitle += identification, emptyMessage);
        messages.put(filteredVariableReportTitle += identification, emptyMessage);
        messages.put(numericVariableReportTitle += identification, emptyMessage);
        messages.put(mufReportTitle += identification, emptyMessage);
        dashboardTitle = dashboardTitle + identification;
    }

    @Override
    protected void customizeProject() throws Throwable {
        getMetricCreator().createNumberOfActivitiesMetric();
        getMetricCreator().createAmountMetric();
        getReportCreator().createEmptyReport();
        getReportCreator().createIncomputableReport();
        getReportCreator().createTooLargeReport();
        getReportCreator().createActivitiesByTypeReport();

        dashboardRequest = new DashboardRestRequest(getAdminRestClient(), testParams.getProjectId());
        dashboardRequest.createDashboard(createSimpleDashboard(DASHBOARD_HAVING_TAB).getMdObject());
        dashboardRequest.createDashboard(createSimpleDashboard(OTHER_DASHBOARD_HAVING_TAB).getMdObject());
        userManagementRestRequest = new UserManagementRestRequest(
                new RestClient(getProfile(Profile.DOMAIN)), testParams.getProjectId());
    }

    @Test(dependsOnGroups = {"createProject"}, groups = {"precondition"})
    public void signInImapUser() throws JSONException {
        logout();
        signInAtGreyPages(imapUser, imapPassword);
    }
    /**
     * Automate testscase for Jira ticket CL-12102
     * testcase is "disable a user which is the only one used in schedule email, then try to save the schedule email
     * with empty Emails To field, and make sure schedule email to save success after add a new email".
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     */
    @Test(dependsOnMethods = {"signInImapUser"}, groups = {"schedules"})
    public void verifyEmailToWhenOnlyUserIsDisabled() throws JSONException, ParseException, IOException {
        //automate testscase for Jira ticket CL-12102
        String dashboard = "dashboard " + generateHashString();
        EmailSchedulePage emailSchedulePage = initEmailSchedulesPage().scheduleNewDashboardEmail(
                singletonList(testParams.getUser()), dashboard, "Scheduled email test - dashboard.",
                singletonList(DASHBOARD_HAVING_TAB));
        userManagementRestRequest.updateUserStatusInProject(testParams.getUser(), UserStatus.DISABLED);
        try {
            assertThat(emailSchedulePage.openSchedule(dashboard).getEmailToListItem(), not(hasItem(testParams.getUser())));

            emailSchedulePage.trySaveSchedule();
            assertEquals(emailSchedulePage.getValidationErrorMessages(), "Should not be empty");

            emailSchedulePage.changeEmailTo(dashboard, singletonList(imapUser));
            userManagementRestRequest.updateUserStatusInProject(testParams.getUser(), UserStatus.ENABLED);
            assertEquals(emailSchedulePage.openSchedule(dashboard).getEmailToListItem(), asList(imapUser, testParams.getUser()));
        } finally {
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(dashboard);
            userManagementRestRequest.updateUserStatusInProject(testParams.getUser(), UserStatus.ENABLED);
        }
    }

    /**
     * Automate testscase for Jira ticket CL-12102
     * testcase is "disable a user which is one of used users in schedule email, then change dashboard and save schedule".
     * @throws JSONException
     * @throws ParseException
     * @throws IOException
     */
    @Test(dependsOnMethods = {"signInImapUser"}, groups = {"schedules"})
    public void verifyEmailToWhenOneOfUsersIsDisabled() throws JSONException, ParseException, IOException {
        String dashboard = "dashboard " + generateHashString();
        EmailSchedulePage emailSchedulePage = initEmailSchedulesPage();
        emailSchedulePage.scheduleNewDashboardEmail(asList(imapUser, testParams.getUser()), dashboard,
                "Scheduled email test - dashboard.", singletonList(DASHBOARD_HAVING_TAB));
        userManagementRestRequest.updateUserStatusInProject(testParams.getUser(), UserStatus.DISABLED);
        try {
            assertThat(emailSchedulePage.openSchedule(dashboard).getEmailToListItem(), not(hasItem(testParams.getUser())));
            emailSchedulePage.changeDashboards(dashboard, asList(DASHBOARD_HAVING_TAB, OTHER_DASHBOARD_HAVING_TAB));
            userManagementRestRequest.updateUserStatusInProject(testParams.getUser(), UserStatus.ENABLED);
            assertEquals(emailSchedulePage.openSchedule(dashboard).getEmailToListItem(),
                    asList(imapUser, testParams.getUser()));
        } finally {
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(dashboard);
            userManagementRestRequest.updateUserStatusInProject(testParams.getUser(), UserStatus.ENABLED);
        }
    }

    @Test(dependsOnMethods = {"signInImapUser"}, groups = {"schedules"})
    public void verifyAuthorEmailWhenDeletedUserOnGlobalScheduledEmails() throws ParseException, JSONException, MessagingException, IOException {
        String userA = createDynamicUserFrom(testParams.getUser());
        String userB = createDynamicUserFrom(imapUser);
        try {
            addUserToProject(userA, UserRoles.ADMIN);
            addUserToProject(userB, UserRoles.EDITOR);

            logout();
            signInAtGreyPages(userA, testParams.getPassword());

            EmailSchedulePage emailSchedulePage = initEmailSchedulesPage();
            emailSchedulePage.scheduleNewDashboardEmail(asList(userB), dashboardTitle,
                    "Global Scheduled email test - dashboard.", singletonList(DASHBOARD_HAVING_TAB));
            assertEquals(emailSchedulePage.openSchedule(dashboardTitle).getEmailToListItem(), asList(userB));

            logoutAndLoginAs(true, UserRoles.ADMIN);
            userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), userA);

            logout();
            signInAtGreyPages(userB, testParams.getPassword());
            assertEquals(initEmailSchedulesPage().openSchedule(dashboardTitle).getEmailToListItem(), asList(userB));

            initEmailSchedulesPage();
            String dashboardScheduleUri = EmailSchedulePage.getInstance(browser).getScheduleMailUriByName(dashboardTitle);
            updateRecurrencyString(dashboardScheduleUri);

            checkMailBoxDashboardExport(imapUser, imapPassword);

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(dashboardTitle);
            userManagementRestRequest.deleteUserByEmail(testParams.getUserDomain(), userB);
            logout();
            signInAtGreyPages(imapUser, imapPassword);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void createEmptyDashboardSchedule() {
        initDashboardsPage();
        dashboardsPage.addNewDashboard("Empty dashboard");
        initEmailSchedulesPage().scheduleNewDashboardEmail(singletonList(imapUser), emptyDashboardTitle,
                "Scheduled email test - empty dashboard.", singletonList("First Tab"));
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-empty-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"createEmptyDashboardSchedule"}, groups = {"schedules"})
    public void checkEmailToField() {
        assertEquals(initEmailSchedulesPage().openNewSchedule().getEmailToListItem().size(),0);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void deleteDashboardUsedInSchedule() {
        String dashboardTitle = "Schedule dashboard";
        initEmailSchedulesPage().scheduleNewDashboardEmail(singletonList(imapUser), dashboardTitle,
                "Scheduled email test - dashboard.", singletonList(DASHBOARD_HAVING_TAB));

        try {
            initDashboardsPage();
            dashboardsPage.selectDashboard(DASHBOARD_HAVING_TAB);
            dashboardsPage.editDashboard();
            dashboardsPage.getDashboardEditBar().tryToDeleteDashboard();
            Graphene.waitGui().until(browser -> browser.findElements(BY_RED_BAR).size() != 0);
            assertEquals(waitForElementVisible(browser.findElement(BY_RED_BAR)).getText(), CANNOT_DELETE_DASHBOARD_MESSAGE);
            logRedBarMessageInfo(browser);
            waitForElementVisible(BY_DISMISS_BUTTON, browser).click();
            dashboardsPage.getDashboardEditBar().cancelDashboard();
        } finally {
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(dashboardTitle);
        }
    }

    @Test(dependsOnMethods = {"createEmptyDashboardSchedule"}, groups = {"schedules"})
    public void duplicateSchedule() {
        waitForFragmentVisible(initEmailSchedulesPage()).duplicateSchedule(emptyDashboardTitle);
    }

    @Test(dependsOnMethods = {"duplicateSchedule"}, groups = {"schedules"})
    public void deleteSchedule() {
        waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(emptyDashboardTitle);
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleEmptyReport() {
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), noDataReportTitle,
                "Scheduled email test - no data report.", singletonList(REPORT_NO_DATA), ExportFormat.ALL);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-no-data-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleIncomputableReport() {
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), incomputableReportTitle,
                "Scheduled email test - incomputable report.", singletonList(REPORT_INCOMPUTABLE), ExportFormat.PDF);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-incomputable-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleTooLargeReport() {
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), tooLargeReportTitle,
                "Scheduled email test - too large report.", singletonList(REPORT_TOO_LARGE), ExportFormat.PDF);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-too-large-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleReportApplyFilteredVariable() {
        initVariablePage().createVariable(new AttributeVariable("FVariable")
            .withAttribute(ATTR_ACTIVITY_TYPE).withAttributeValues("Email"));

        initReportsPage();
        UiReportDefinition rd = new UiReportDefinition().withName("Filtered variable report")
                .withHows(ATTR_ACTIVITY_TYPE).withWhats(METRIC_NUMBER_OF_ACTIVITIES);
        createReport(rd, "Filtered variable report");
        reportPage.addFilter(FilterItem.Factory.createPromptFilter("FVariable", "Email"));
        reportPage.saveReport();

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), filteredVariableReportTitle,
                "Scheduled email test - Filtered variable report.", singletonList("Filtered variable report"),
                ExportFormat.SCHEDULES_EMAIL_CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-filtered-variable-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleReportApplyNumericVariable() {
        String variableUri = initVariablePage().createVariable(new NumericVariable("NVariable").withDefaultNumber(2012));

        String report = "Sum amount in 2012";
        String expression = format("SELECT SUM([%s]) WHERE [%s] = [%s]",
                getMetricByTitle(METRIC_AMOUNT).getUri(), getAttributeByTitle(ATTR_YEAR_SNAPSHOT).getUri(), variableUri);

        Metric metric = createMetric(report, expression, "#,##0");
        Attribute yearSnapshot = getAttributeByIdentifier("snapshot.year");
        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(yearSnapshot.getDefaultDisplayForm().getUri(), yearSnapshot.getTitle())),
                singletonList(new MetricElement(metric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), numericVariableReportTitle,
                "Scheduled email test - Numeric variable report.", singletonList(report), ExportFormat.SCHEDULES_EMAIL_CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-numeric-variable-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"schedules"})
    public void scheduleMufReport() throws IOException, JSONException {
        initEmailSchedulesPage();
        Attribute product = getAttributeByTitle(ATTR_PRODUCT);
        Metric amountMetric = getMetricByTitle(METRIC_AMOUNT);
        String report = "MUF report";

        final String explorerUri = getMdService().getAttributeElements(product).stream()
                .filter(e -> e.getTitle().equals("Explorer")).findFirst().get().getUri();

        Map<String, Collection<String>> conditions = new HashMap();
        conditions.put(product.getUri(), singletonList(explorerUri));
        dashboardRequest.addMufToUser(userManagementRestRequest.getUserProfileUri(testParams.getUserDomain(), imapUser),
                dashboardRequest.createSimpleMufObjByUri("Product user " +
                "filter", conditions));

        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList(METRIC_GROUP),
                singletonList(new AttributeInGrid(product.getDefaultDisplayForm().getUri(), product.getTitle())),
                singletonList(new MetricElement(amountMetric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage().scheduleNewReportEmail(asList(imapUser, testParams.getUser(), EDITOR_EMAIL), mufReportTitle,
                "Scheduled email test - MUF report.", singletonList(report), ExportFormat.SCHEDULES_EMAIL_CSV);
        checkRedBar(browser);
        takeScreenshot(browser, "Goodsales-schedules-muf-report", this.getClass());
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"verify-UI"})
    public void deleteReport() {
        String title = "verify-UI-title";
        String report = "# test report";

        initReportsPage();
        Metric amountMetric = getMetricByTitle(METRIC_AMOUNT);

        ReportDefinition definition = GridReportDefinitionContent.create(report, singletonList(METRIC_GROUP),
                Collections.<AttributeInGrid>emptyList(),
                singletonList(new MetricElement(amountMetric)));
        definition = getMdService().createObj(getProject(), definition);
        getMdService().createObj(getProject(), new Report(definition.getTitle(), definition));

        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), title,
                "Scheduled email test - report.", singletonList(report), ExportFormat.ALL);

        try {
            initReportsPage()
                .tryDeleteReports(report);
            Graphene.waitGui().until(browser -> browser.findElements(BY_RED_BAR_WARNING).size() != 0);
            assertEquals(waitForElementVisible(browser.findElement(BY_RED_BAR_WARNING)).getText(), "0 report(s) deleted."
                    + " 1 report(s) are in use on a dashboard or an email distribution list and were not deleted.");
            logRedBarMessageInfo(browser);
            waitForElementVisible(BY_DISMISS_BUTTON, browser).click();
        } finally {
            waitForFragmentVisible(initEmailSchedulesPage()).deleteSchedule(title);
            initReportsPage()
                .deleteReports(report);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"verify-UI"})
    public void editSchedule() {
        String title = "verify-UI-title";
        String updatedTitle = title + "Updated";
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), title,
                "Scheduled email test - report.", singletonList(REPORT_ACTIVITIES_BY_TYPE),
                ExportFormat.SCHEDULES_EMAIL_CSV);
        try {
            EmailSchedulePage.getInstance(browser).openSchedule(title)
                .setSubject(updatedTitle)
                .setMessage("Scheduled email test - report. (Updated)")
                .selectReportFormat(ExportFormat.ALL)
                .saveSchedule();
            assertFalse(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(title),
                    title + " shouldn't display");
            assertTrue(EmailSchedulePage.getInstance(browser).isGlobalSchedulePresent(updatedTitle),
                    updatedTitle + " should display");

            assertEquals(EmailSchedulePage.getInstance(browser).openSchedule(updatedTitle).getMessageFromInput(),
                    "Scheduled email test - report. (Updated)");
            assertEquals(EmailSchedulePage.getInstance(browser).getSelectedFormats(), asList("Inline message", "PDF", "Excel (XLSX)", "CSV"));
        } finally {
            if (initEmailSchedulesPage().isGlobalSchedulePresent(updatedTitle)) {
                waitForFragmentVisible(EmailSchedulePage.getInstance(browser)).deleteSchedule(updatedTitle);
                return;
            }
            waitForFragmentVisible(EmailSchedulePage.getInstance(browser)).deleteSchedule(title);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"verify-UI"})
    public void changeScheduleTime() {
        String title = "verify-UI-title";
        initEmailSchedulesPage().scheduleNewReportEmail(singletonList(imapUser), title,
                "Scheduled email test - report.", singletonList(REPORT_ACTIVITIES_BY_TYPE), ExportFormat.ALL);

        try {
            String timeDescription = "";
            for (RepeatTime time : RepeatTime.values()) {
                timeDescription = EmailSchedulePage.getInstance(browser).openSchedule(title)
                    .changeTime(time)
                    .getTimeDescription();
                EmailSchedulePage.getInstance(browser).saveSchedule();
                assertEquals(EmailSchedulePage.getInstance(browser).getScheduleDescription(title),
                        format(title + " (%s)", timeDescription));
            }
        } finally {
            EmailSchedulePage.getInstance(browser).deleteSchedule(title);
        }
    }

    @Test(dependsOnGroups = {"schedules", "verify-UI"})
    public void verifyCreatedSchedules() {
        assertEquals(initEmailSchedulesPage().getNumberOfGlobalSchedules(), messages.size(),
                "Schedules are properly created.");
        takeScreenshot(browser, "Goodsales-schedules", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedSchedules"})
    public void updateScheduledMailRecurrency() throws IOException {
        initEmailSchedulesPage();
        updateRecurrencies(messages);
    }

    @Test(dependsOnMethods = {"updateScheduledMailRecurrency"})
    public void waitForScheduledMailMessages() throws MessagingException, IOException {
        ScheduleEmailRestRequest scheduleEmailRestRequest = initScheduleEmailRestRequest();
        try (ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword)) {
            System.out.println("ACCELERATE scheduled mails processing");
            scheduleEmailRestRequest.accelerate();
            checkMailbox(imapClient);
        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            scheduleEmailRestRequest.decelerate();
        }
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyEmptyDashboardSchedule() {
        assertEquals(attachments.get(emptyDashboardTitle).savedAttachments.size(), 1,
                "ERROR: Dashboard message does not have correct number of attachments.");
        assertTrue(attachments.get(emptyDashboardTitle).savedAttachments.get(0).contentType
                .contains("application/pdf".toUpperCase()),
                "ERROR: Dashboard attachment does not have PDF content type.");
        verifyAttachment(attachments.get(emptyDashboardTitle).savedAttachments.get(0), "PDF", 10000);
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyNoDataReport() {
        String error = format("Report '%s' is empty and, as a result, is not attached", REPORT_NO_DATA);
        assertTrue(attachments.get(noDataReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyIncomputableReport() {
        String error = format("Report '%s' cannot be exported to '%s' format as it is not currently computable",
                REPORT_INCOMPUTABLE, "pdf");
        assertTrue(attachments.get(incomputableReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyTooLargeReport() {
        String error = format("Report '%s' is too large and, as a result, cannot be exported to '%s' format", REPORT_TOO_LARGE,
                "pdf");
        assertTrue(attachments.get(tooLargeReportTitle).body.contains(error),
                "Cannot find message: [" + error + "] in email!");
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyFilteredVariableReport() throws IOException {
        List<String> reportResult = getCsvContent(new File(attachmentsDirectory,
                attachments.get(filteredVariableReportTitle).savedAttachments.get(0).fileName));
        assertTrue(isEqualCollection(reportResult, asList("Email", "33920")),
                "Data in report is not correct!");
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyNumericVariableReport() throws IOException {
        List<String> reportResult = getCsvContent(new File(attachmentsDirectory,
                attachments.get(numericVariableReportTitle).savedAttachments.get(0).fileName));
        assertTrue(isEqualCollection(reportResult, asList("2012", "38596194.86")),
                "Data in report is not correct!");
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyMufReport() throws IOException {
        List<String> reportResult = getCsvContent(new File(attachmentsDirectory,
                attachments.get(mufReportTitle).savedAttachments.get(0).fileName));
        assertTrue(isEqualCollection(reportResult, asList("Explorer", "38596194.86")),
                "Data in report is not correct!");
    }

    @Test(dependsOnMethods = {"waitForScheduledMailMessages"})
    public void verifyEmailToWhenUserHasDifferentLoginAndEmail() {
        //To test for issue CL-12072
        assertEquals(initEmailSchedulesPage().openSchedule(mufReportTitle).getEmailToListItem(),
                asList(imapUser, testParams.getUser(), EDITOR_EMAIL));
        Screenshots.takeScreenshot(browser, "verify email", getClass());
    }

    private Dashboard createSimpleDashboard(String title) throws IOException {
        return Builder.of(Dashboard::new).with(dash -> {
            dash.setName(title);
            dash.addTab(Builder.of(Tab::new).with(tab -> tab.setTitle("Tab")).build());
        }).build();
    }

    private void updateRecurrencies(Map<String, List<Message>> messages) throws IOException {
        for (String recurrency : messages.keySet()) {
            updateRecurrencyString(EmailSchedulePage.getInstance(browser).getScheduleMailUriByName(recurrency));
        }
    }

    private void checkMailbox(ImapClient imapClient) throws MessagingException, IOException {
        getMessagesFromInbox(imapClient);
        saveMessageAttachments(messages);
    }

    private void saveMessageAttachments(Map<String, List<Message>> messages) throws MessagingException, IOException {
        for (String title : messages.keySet()) {
            System.out.println("Saving message ...");
            ImapUtils.saveMessageAttachments(messages.get(title).get(0), attachmentsDirectory);
            attachments.put(title, new MessageContent().setBody(ImapUtils.getEmailBody(messages.get(title).get(0))));
            List<Part> attachmentParts = ImapUtils.getAttachmentParts(messages.get(title).get(0));
            List<SavedAttachment> savedAttachments = new ArrayList<SavedAttachment>();
            for (Part part : attachmentParts) {
                savedAttachments.add(new SavedAttachment().setContentType(part.getContentType())
                        .setSize(part.getSize()).setFileName(part.getFileName()));
            }
            attachments.get(title).setSavedAttachments(savedAttachments);
        }
    }

    private List<String> getCsvContent(File csvFile) throws IOException {
        List<String> reportResult = new ArrayList<String>();
        ICsvListReader listReader = null;
        List<String> result;

        try {
            listReader = new CsvListReader(new FileReader(csvFile), CsvPreference.STANDARD_PREFERENCE);

            listReader.getHeader(true);
            while ((result = listReader.read()) != null) {
                reportResult.addAll(result);
            }

            return reportResult;
        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }
    }

    private void getMessagesFromInbox(ImapClient imapClient) throws MessagingException {
        for (String title : messages.keySet()) {
            messages.put(title, waitForMessages(imapClient, GDEmails.NOREPLY, title, 1));
        }
    }

    private void verifyAttachment(SavedAttachment attachment, String type, long minimalSize) {
        assertTrue(attachment.size > minimalSize, "The attachment (" + type + ") has the expected minimal size."
                + " Expected " + minimalSize + "B, found " + attachment.size + "B.");
    }

    private class SavedAttachment {
        private String contentType;
        private int size;
        private String fileName;

        public SavedAttachment setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public SavedAttachment setSize(int size) {
            this.size = size;
            return this;
        }

        public SavedAttachment setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
    }

    private class MessageContent {
        private List<SavedAttachment> savedAttachments;
        private String body;

        public MessageContent setSavedAttachments(List<SavedAttachment> savedAttachments) {
            this.savedAttachments = savedAttachments;
            return this;
        }

        public MessageContent setBody(String body) {
            this.body = body;
            return this;
        }
    }

    private void checkMailBoxDashboardExport(String userName, String password) throws MessagingException, IOException {
        ScheduleEmailRestRequest scheduleEmailRestRequest = initScheduleEmailRestRequest();
        try (ImapClient imapClient = new ImapClient(imapHost, userName, password)) {
            System.out.println("ACCELERATE scheduled mails processing");
            scheduleEmailRestRequest.accelerate();
            List<Message> dashboardMessages = waitForMessages(imapClient, GDEmails.NOREPLY, dashboardTitle, 1);
            System.out.println("Saving dashboard message ...");
            ImapUtils.saveMessageAttachments(dashboardMessages.get(0), attachmentsDirectory);
            // DASHBOARD EXPORT
            List<Part> dashboardAttachmentParts = ImapUtils.getAttachmentParts(dashboardMessages.get(0));
            assertEquals(dashboardAttachmentParts.size(), 1, "Dashboard message has correct number of attachments.");
            assertTrue(dashboardAttachmentParts.get(0).getContentType().contains("application/pdf".toUpperCase()),
                    "Dashboard attachment has PDF content type.");
        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            scheduleEmailRestRequest.decelerate();
        }
    }
}
