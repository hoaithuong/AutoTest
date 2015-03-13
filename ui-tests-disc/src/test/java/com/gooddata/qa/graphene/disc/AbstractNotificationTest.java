package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang.SystemUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.disc.ExecutionDetails;
import com.gooddata.qa.graphene.entity.disc.NotificationBuilder;
import com.gooddata.qa.graphene.entity.disc.NotificationParameters;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.NotificationEvents;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.fragments.greypages.md.obj.ObjectFragment;
import com.gooddata.qa.utils.http.RestUtils;
import com.gooddata.qa.utils.mail.ImapClient;
import com.google.common.base.Predicate;

public class AbstractNotificationTest extends AbstractDISCTest {

    protected static final String NOTIFICATION_TEST_PROCESS = "Notification Test Process";
    protected static final String SUCCESS_NOTIFICATION_TEST_PROCESS = "Success Notification Test";
    protected static final String FAILURE_NOTIFICATION_TEST_PROCESS = "Failure Notification Test";
    protected static final String CUSTOM_NOTIFICATION_TEST_PROCESS =
            "Custom Event Notification Test";
    protected static final String REPEATED_FAILURES_NOTIFICATION_SUBJECT =
            "Repeated data loading failure: \"%s\" process";
    protected static final String REPEATED_FAILURES_NOTIFICATION_BODY =
            "Hello, the %s schedule within the \"%s\" process of your \"%s\" GoodData project (id: %s) has failed for the ${numberOfFailures}th time."
                    + " We highly recommend disabling failing schedules until the issues are addressed: Go to"
                    + " the schedule page Click \"Disable\" button If you require assistance with troubleshooting"
                    + " data uploading, please visit the GoodData Support Portal. At your service, The GoodData Team";
    protected static final String REPEATED_FAILURES_NOTIFICATION_MESSAGE_1 =
            "<p>the <a href=\"%s\">%s schedule</a> within the &quot;%s&quot; process of your &quot;%s&quot;"
                    + " GoodData project (id: %s) has failed for the ${numberOfFailures}th time.</p>";
    protected static final String REPEATED_FAILURES_NOTIFICATION_MESSAGE_2 =
            "<li>Go to the <a href=\"%s\">schedule page</a></li>";
    protected static final String SCHEDULE_DISABLED_NOTIFICATION_SUBJECT =
            "Schedule disabled: \"%s\" process";
    protected static final String SCHEDULE_DISABLED_NOTIFICATION_BODY =
            "Hello, the %s schedule within the \"%s\" process of your \"%s\" GoodData project "
                    + "(id: %s) has been automatically disabled following its ${numberOfFailures}th consecutive failure. "
                    + "To resume scheduled uploads from this process: Go to the schedule page Click "
                    + "\"Enable\" If you require assistance with troubleshooting data uploading, "
                    + "please visit the GoodData Support Portal. At your service, The GoodData Team";
    protected static final String SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_1 =
            "<p>the <a href=\"%s\">%s schedule</a> within the &quot;%s&quot; process of your &quot;%s&quot;"
                    + " GoodData project (id: %s) has been <strong>automatically disabled</strong> following its "
                    + "${numberOfFailures}th consecutive failure.</p>";
    protected static final String SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_2 =
            "<li>Go to the <a href=\"%s\">schedule page</a></li>";
    protected static final String NOTIFICATION_SUPPORT_MESSAGE =
            "<p>If you require assistance with troubleshooting data uploading, please visit the "
                    + "<a href=\"http://support.gooddata.com/entries/23541617-Automatic-Disabling-of-Failed-Schedules\">GoodData Support Portal</a>.</p>";
    protected static final String NOTIFICATION_RULES_EMPTY_STATE_MESSAGE =
            "No event (eg. schedule start, finish, fail, etc.) will trigger a notification email.";
    protected static final String FROM = "no-reply@gooddata.com";
    protected String successNotificationSubject = "Success Notification_"
            + Calendar.getInstance().getTime();
    protected String successNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*"
            + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.PROCESS_NAME=${params.PROCESS_NAME}" + "*"
            + "params.EXECUTABLE=${params.EXECUTABLE}" + "*"
            + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*"
            + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}" + "*" + "params.LOG=${params.LOG}"
            + "*" + "params.START_TIME=${params.START_TIME}" + "*"
            + "params.FINISH_TIME=${params.FINISH_TIME}";
    protected String successNotificationParams =
            "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
                    + "${params.PROCESS_URI}${params.PROCESS_ID}${params.PROCESS_NAME}${params.EXECUTABLE}"
                    + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.LOG}${params.START_TIME}"
                    + "${params.FINISH_TIME}";
    protected String failureNotificationSubject = "Failure Notification_"
            + Calendar.getInstance().getTime();
    protected String failureNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*"
            + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.PROCESS_NAME=${params.PROCESS_NAME}" + "*"
            + "params.EXECUTABLE=${params.EXECUTABLE}" + "*"
            + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*"
            + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}" + "*" + "params.LOG=${params.LOG}"
            + "*" + "params.START_TIME=${params.START_TIME}" + "*"
            + "params.FINISH_TIME=${params.FINISH_TIME}" + "*"
            + "params.ERROR_MESSAGE=${params.ERROR_MESSAGE}";
    protected String failureNotificationParams =
            "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
                    + "${params.PROCESS_URI}${params.PROCESS_ID}${params.PROCESS_NAME}${params.EXECUTABLE}"
                    + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.LOG}${params.START_TIME}"
                    + "${params.FINISH_TIME}${params.ERROR_MESSAGE}";
    protected String processStartedNotificationSubject = "Process Started Notification_"
            + Calendar.getInstance().getTime();
    protected String processStartedNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*"
            + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.PROCESS_NAME=${params.PROCESS_NAME}" + "*"
            + "params.EXECUTABLE=${params.EXECUTABLE}" + "*"
            + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*"
            + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}" + "*" + "params.LOG=${params.LOG}"
            + "*" + "params.START_TIME=${params.START_TIME}";
    protected String processStartedNotificationParams =
            "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
                    + "${params.PROCESS_URI}${params.PROCESS_ID}${params.PROCESS_NAME}${params.EXECUTABLE}"
                    + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.LOG}${params.START_TIME}";
    protected String processScheduledNotificationSubject = "Process Scheduled Notification_"
            + Calendar.getInstance().getTime();
    protected String processScheduledNotificationMessage = "params.PROJECT=${params.PROJECT}" + "*"
            + "params.USER=${params.USER}" + "*" + "params.USER_EMAIL=${params.USER_EMAIL}" + "*"
            + "params.PROCESS_URI=${params.PROCESS_URI}" + "*"
            + "params.PROCESS_ID=${params.PROCESS_ID}" + "*"
            + "params.EXECUTABLE=${params.EXECUTABLE}" + "*"
            + "params.SCHEDULE_ID=${params.SCHEDULE_ID}" + "*"
            + "params.SCHEDULE_NAME=${params.SCHEDULE_NAME}" + "*"
            + "params.SCHEDULED_TIME=${params.SCHEDULED_TIME}";
    protected String processScheduledNotificationParams =
            "${params.PROJECT}${params.USER}${params.USER_EMAIL}"
                    + "${params.PROCESS_URI}${params.PROCESS_ID}${params.EXECUTABLE}"
                    + "${params.SCHEDULE_ID}${params.SCHEDULE_NAME}${params.SCHEDULED_TIME}";
    protected String customEventNotificationSubject = "Custom Event_"
            + Calendar.getInstance().getTime();
    protected String customEventNotificationMessage = "params.CUSTOM=${params.hello}";
    protected String notificationSubject = "Notification Subject_";
    protected String notificationMessage = "params.PROJECT=${params.PROJECT}";
    protected String userProfileId;
    protected String successProcessUri;
    protected String failureProcessUri;
    protected String successfulScheduleId;
    protected String failedScheduleId;

    @FindBy(tagName = "pre")
    protected ObjectFragment objectFragment;
    protected ExecutionDetails successfulExecutionDetails = new ExecutionDetails()
            .setStatus(ScheduleStatus.OK);
    protected ExecutionDetails failedExecutionDetails = new ExecutionDetails()
            .setStatus(ScheduleStatus.ERROR);

    enum RepeatedDataLoadingFailureNumber {
        TO_SEND_MAIL(2, 5),
        TO_DISABLE_SCHEDULE(3, 30);

        private int piNumber;
        private int stagingNumber;

        private RepeatedDataLoadingFailureNumber(int pi, int staging) {
            this.piNumber = pi;
            this.stagingNumber = staging;
        }

        public int getNumber(String host) {
            if (host.contains("staging"))
                return stagingNumber;
            else
                return piNumber;
        }
    }

    protected int getNumberOfFailuresToSendMail() {
        return RepeatedDataLoadingFailureNumber.TO_SEND_MAIL.getNumber(testParams.getHost());
    }

    protected int getNumberOfFailuresToDisableSchedule() {
        return RepeatedDataLoadingFailureNumber.TO_DISABLE_SCHEDULE.getNumber(testParams.getHost());
    }

    protected String createGdcUserWithImapUser(String imapUser, String imapPassword) {
        try {
            String imapUserUri =
                    RestUtils.createNewUser(getRestApiClient(), imapUser, imapPassword);
            return imapUserUri;
        } catch (Exception e) {
            throw new IllegalStateException("There is an exeception when creating a new user!", e);
        }
    }

    protected void deleteImapUser(String imapUserUri) {
        if (!imapUserUri.isEmpty())
            RestUtils.deleteUser(getRestApiClient(), imapUserUri);
    }

    protected void editNotification(NotificationBuilder newNotificationBuilder) {
        openProjectDetailPage(getWorkingProject());
        projectDetailPage.getNotificationButton(newNotificationBuilder.getProcessName()).click();
        waitForElementVisible(discNotificationRules.getRoot());
        int notificationIndex = newNotificationBuilder.getIndex();
        discNotificationRules.expandNotificationRule(notificationIndex);
        discNotificationRules.clearNotificationEmail(notificationIndex);
        discNotificationRules.clearNotificationSubject(notificationIndex);
        discNotificationRules.clearNotificationMessage(notificationIndex);
        discNotificationRules.setNotificationFields(newNotificationBuilder);
        if (newNotificationBuilder.isSaved())
            discNotificationRules.saveNotification(notificationIndex);
        else
            discNotificationRules.cancelSaveNotification(notificationIndex);
    }

    protected void waitForNotification(String subject, NotificationParameters expectedParams) {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        try {
            System.out.println("Waiting for notification...");
            checkScheduleEventNotification(imapClient, subject, expectedParams);
        } finally {
            imapClient.close();
        }
    }

    protected void waitForRepeatedFailuresEmail(ScheduleBuilder scheduleBuilder) {
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        try {
            System.out.println("Waiting for notification...");
            checkRepeatFailureEmail(imapClient, scheduleBuilder);
        } finally {
            imapClient.close();
        }
    }

    protected void checkNotification(NotificationEvents event, NotificationParameters expectedParams) {
        String notificationSubject = null;
        switch (event) {
            case SUCCESS:
                notificationSubject = successNotificationSubject;
                break;
            case FAILURE:
                notificationSubject = failureNotificationSubject;
                break;
            case PROCESS_STARTED:
                notificationSubject = processStartedNotificationSubject;
                break;
            case PROCESS_SCHEDULED:
                notificationSubject = processScheduledNotificationSubject;
                break;
            case CUSTOM_EVENT:
                notificationSubject = customEventNotificationSubject;
                break;
            default:
                break;
        }
        waitForNotification(notificationSubject, expectedParams);
    }

    protected void checkNotificationNumber(int expectedNotificationNumber, String processName) {
        openProjectDetailPage(getWorkingProject());
        waitForElementVisible(projectDetailPage.getRoot());
        if (expectedNotificationNumber == 0) {
            assertEquals(projectDetailPage.getNotificationButton(processName).getText(),
                    "No notification rules");
        } else {
            String notificationNumber =
                    String.valueOf(expectedNotificationNumber) + " notification rule"
                            + (expectedNotificationNumber > 1 ? "s" : "");
            assertEquals(projectDetailPage.getNotificationButton(processName).getText(),
                    notificationNumber);
        }
        projectDetailPage.getNotificationButton(processName).click();
        waitForElementVisible(discNotificationRules.getRoot());
        assertEquals(expectedNotificationNumber, discNotificationRules.getNotificationNumber());
        discNotificationRules.closeNotificationRulesDialog();
    }

    protected void deleteNotification(NotificationBuilder notificationInfo) {
        openProjectDetailPage(getWorkingProject());
        projectDetailPage.getNotificationButton(notificationInfo.getProcessName()).click();
        waitForElementVisible(discNotificationRules.getRoot());
        discNotificationRules.deleteNotification(notificationInfo);
    }

    protected String getProcessUri(String url) {
        String processUri =
                "/gdc/projects/" + testParams.getProjectId() + "/dataload/"
                        + url.substring(url.indexOf("processes"), url.lastIndexOf("schedules") - 1);
        return processUri;
    }

    protected void getExecutionInfoFromGreyPage(ExecutionDetails executionDetails,
            String executionLogLink) {
        executionDetails.setScheduleLogLink(executionLogLink);
        browser.get(executionLogLink.replace("ea.", "").replace("/log", "/detail"));
        waitForElementVisible(objectFragment.getRoot());
        JSONObject jsonObject = null;
        JSONObject executionDetailJSONObject;
        try {
            jsonObject = objectFragment.getObject();
            executionDetailJSONObject = jsonObject.getJSONObject("executionDetail");
            executionDetails.setStartTime(executionDetailJSONObject.getString("started"));
            executionDetails.setEndTime(executionDetailJSONObject.getString("finished"));
            executionDetails.setScheduledTime(executionDetailJSONObject.getString("created"));
            if (executionDetails.getStatus().equals(ScheduleStatus.ERROR))
                executionDetails.setErrorMessage(executionDetailJSONObject.getJSONObject("error")
                        .getString("message").replaceAll("\n(\\s*)", ""));
        } catch (JSONException e) {
            fail("There is problem with jsonObject: " + e);
        }
    }

    protected static Message getNotification(final ImapClient imapClient, final String subject) {
        Message[] notifications = new Message[0];

        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).pollingEvery(5, TimeUnit.SECONDS)
                .withMessage("Notification is not sent!").until(new Predicate<WebDriver>() {

                    @Override
                    public boolean apply(WebDriver arg0) {
                        System.out.println("Waiting for notification...");
                        return imapClient.getMessagesFromInbox(FROM, subject).length > 0;
                    }
                });
        notifications = imapClient.getMessagesFromInbox(FROM, subject);
        assertTrue(notifications.length == 1, "More than 1 notification!");
        return notifications[0];
    }

    private void checkRepeatFailureEmail(ImapClient imapClient, ScheduleBuilder scheduleBuilder) {
        boolean isEnabled = scheduleBuilder.isEnabled();
        String subjectFormat =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_SUBJECT
                        : SCHEDULE_DISABLED_NOTIFICATION_SUBJECT;
        String processName = scheduleBuilder.getProcessName();
        String notificationSubject = String.format(subjectFormat, processName);
        String notificationMessage1 =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_MESSAGE_1.replace("${numberOfFailures}",
                        Integer.toString(getNumberOfFailuresToSendMail()))
                        : SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_1.replace("${numberOfFailures}",
                                Integer.toString(getNumberOfFailuresToDisableSchedule()));
        String notificationMessage2 =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_MESSAGE_2
                        : SCHEDULE_DISABLED_NOTIFICATION_MESSAGE_2;
        String notificationBody =
                isEnabled ? REPEATED_FAILURES_NOTIFICATION_BODY.replace("${numberOfFailures}",
                        Integer.toString(getNumberOfFailuresToSendMail()))
                        : SCHEDULE_DISABLED_NOTIFICATION_BODY.replace("${numberOfFailures}",
                                Integer.toString(getNumberOfFailuresToDisableSchedule()));

        Message notification = getNotification(imapClient, notificationSubject);
        Document message = null;
        try {
            message = Jsoup.parse(notification.getContent().toString());
        } catch (IOException e) {
            fail("There is problem during parsing message: " + e);
        } catch (MessagingException e) {
            fail("There is problem during getting repeated failures email: " + e);
        }
        System.out.println("Notification message: " + message.getElementsByTag("body").text());

        String scheduleName = scheduleBuilder.getScheduleName();
        assertEquals(
                message.getElementsByTag("body").text(),
                String.format(notificationBody, scheduleName, processName, projectTitle,
                        testParams.getProjectId()));
        String scheduleUrl = scheduleBuilder.getScheduleUrl();
        assertEquals(
                message.getElementsByTag("p").get(1).toString().replace("https://ea.", "https://"),
                String.format(notificationMessage1, scheduleUrl, scheduleName, processName,
                        projectTitle, testParams.getProjectId()));
        assertEquals(
                message.getElementsByTag("li").get(0).toString().replace("https://ea.", "https://"),
                String.format(notificationMessage2, scheduleUrl));
        assertEquals(message.getElementsByTag("p").get(3).toString(), NOTIFICATION_SUPPORT_MESSAGE);
    }

    private void checkScheduleEventNotification(ImapClient imapClient, String subject,
            NotificationParameters expectedParams) {
        Message notification = getNotification(imapClient, subject);
        ArrayList<String> paramValues = new ArrayList<String>();
        String notificationContent = "";
        try {
            notificationContent = notification.getContent().toString() + "*";
        } catch (IOException e) {
            fail("There is problem when checking schedule event notification: " + e);
        } catch (MessagingException e) {
            fail("There is problem when checking schedule event notification: " + e);
        }
        while (!notificationContent.isEmpty()) {
            if (notificationContent.substring(0, notificationContent.indexOf("*")).contains(
                    "params.LOG"))
                paramValues.add(notificationContent.substring(0, notificationContent.indexOf("*"))
                        .replace("https://ea.", "https://"));
            else
                paramValues.add(notificationContent.substring(0, notificationContent.indexOf("*")));
            System.out.println("Param value: " + paramValues.get(paramValues.size() - 1));
            if (notificationContent.substring(notificationContent.indexOf("*")).equals("*"))
                notificationContent = "";
            else
                notificationContent =
                        notificationContent.substring(notificationContent.indexOf("*") + 1);
        }

        for (String paramItem : paramValues) {
            String paramName = paramItem.substring(0, paramItem.indexOf("="));
            String paramValue = paramItem.substring(paramItem.indexOf("=") + 1);
            if (paramValue.contains(SystemUtils.LINE_SEPARATOR)) {
                StringBuilder builder = new StringBuilder();
                for (String s : paramValue.split(SystemUtils.LINE_SEPARATOR)) {
                    builder.append(s.trim());
                }
                paramValue = builder.toString();
            }
            if (paramName.contains("TIME") && !paramValue.isEmpty()) {
                long timeDifferenceInSeconds =
                        timeDifferenceInSecond(expectedParams.getParamValue(paramName), paramValue);
                assertTrue(timeDifferenceInSeconds >= -2 && timeDifferenceInSeconds <= 2,
                        "Not allowed time difference in seconds: " + timeDifferenceInSeconds);
            } else
                assertEquals(paramValue, expectedParams.getParamValue(paramName));
        }
    }

    private long timeDifferenceInSecond(String expectedTime, String actualTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            long compareResult =
                    format2.parse(actualTime).getTime() - format.parse(expectedTime).getTime();
            return TimeUnit.MILLISECONDS.toSeconds(compareResult);
        } catch (ParseException e) {
            fail("Exeception in parsing time: " + e);
        }
        return 0;
    }
}
