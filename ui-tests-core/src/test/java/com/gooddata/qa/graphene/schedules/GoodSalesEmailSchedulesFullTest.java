/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.ScheduleMailPssClient;
import com.gooddata.qa.utils.mail.ImapClient;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.mail.Message;
import javax.mail.Part;
import java.io.File;
import java.util.List;

import static org.testng.Assert.*;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

@Test(groups = {"GoodSalesSchedules"}, description = "Tests for GoodSales project (email schedules functionality) in GD platform")
public class GoodSalesEmailSchedulesFullTest extends AbstractGoodSalesEmailSchedulesTest {

    private String reportTitle = "UI-Graphene-core-Report";
    private String dashboardTitle = "UI-Graphene-core-Dashboard";

    private File attachmentsDirectory;

    @BeforeClass
    public void setUp() throws Exception {
        String identification = ": " + testParams.getHost() + " - " + testParams.getTestIdentification();
        reportTitle = reportTitle + identification;
        dashboardTitle = dashboardTitle + identification;

        attachmentsDirectory = new File(System.getProperty("maven.project.build.directory", "./target/attachments"));
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createDashboardSchedule() {
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewDahboardEmail(testParams.getUser(), dashboardTitle, "Scheduled email test - dashboard.",
                "Outlook");
        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void createReportSchedule() {
        initEmailSchedulesPage();
        emailSchedulesPage.scheduleNewReportEmail(testParams.getUser(), reportTitle, "Scheduled email test - report.",
                "Activities by Type", ExportFormat.ALL);
        checkRedBar(browser);
        Screenshots.takeScreenshot(browser, "Goodsales-schedules-report", this.getClass());
    }

    @Test(dependsOnGroups = {"schedules"})
    public void verifyCreatedSchedules() {
        initEmailSchedulesPage();
        assertEquals(emailSchedulesPage.getNumberOfSchedules(), 2, "Schedules are properly created.");
        Screenshots.takeScreenshot(browser, "Goodsales-schedules", this.getClass());
    }

    @Test(dependsOnMethods = {"verifyCreatedSchedules"})
    public void updateScheduledMailRecurrency() throws Exception {
        initEmailSchedulesPage();

        String reportScheduleUri = emailSchedulesPage.getScheduleMailUriByName(reportTitle);
        String dashboardScheduleUri = emailSchedulesPage.getScheduleMailUriByName(dashboardTitle);
        updateRecurrencyString(reportScheduleUri);
        updateRecurrencyString(dashboardScheduleUri);
    }

    @Test(dependsOnMethods = {"updateScheduledMailRecurrency"})
    public void waitForMessages() throws Exception {
        ScheduleMailPssClient pssClient = new ScheduleMailPssClient(getRestApiClient(), testParams.getProjectId());
        ImapClient imapClient = new ImapClient(imapHost, imapUser, imapPassword);
        try {
            System.out.println("ACCELERATE scheduled mails processing");
            pssClient.accelerate();
            checkMailbox(imapClient);
        } finally {
            System.out.println("DECELERATE scheduled mails processing");
            pssClient.decelerate();
            imapClient.close();
        }
    }

    private void checkMailbox(ImapClient imapClient) throws Exception {
        Message[] reportMessages = new Message[0];
        Message[] dashboardMessages = new Message[0];
        int loops = 0;

        while (!bothEmailsArrived(reportMessages, dashboardMessages) && (loops < 24)) {  // 4 min
            System.out.println("Waiting for messages, try " + (loops + 1));
            reportMessages = imapClient.getMessagesFromInbox(FROM, reportTitle);
            dashboardMessages = imapClient.getMessagesFromInbox(FROM, dashboardTitle);

            if (bothEmailsArrived(reportMessages, dashboardMessages)) {
                System.out.println("Both export messages arrived");
                break;
            }

            Thread.sleep(10000);
            loops++;
        }

        System.out.println("Saving dashboard message ...");
        ImapClient.saveMessageAttachments(dashboardMessages[0], attachmentsDirectory);

        System.out.println("Saving report messages ...");
        ImapClient.saveMessageAttachments(reportMessages[0], attachmentsDirectory);

        System.out.println("Email checks ...");
        assertEquals(reportMessages.length, 1, "Report message arrived.");
        assertEquals(dashboardMessages.length, 1, "Dashboard message arrived.");

        // REPORT EXPORT
        List<Part> reportAttachmentParts = ImapClient.getAttachmentParts(reportMessages[0]);
        assertEquals(reportAttachmentParts.size(), 4, "Report message has correct number of attachments.");

        Part pdfPart = findPartByContentType(reportAttachmentParts, "application/pdf");
        verifyAttachment(pdfPart, "PDF", 3200);

        Part xlsPart = findPartByContentType(reportAttachmentParts, "application/vnd.ms-excel");
        verifyAttachment(xlsPart, "XLS", 7700);

        Part xlsxPart = findPartByContentType(reportAttachmentParts,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        verifyAttachment(xlsxPart, "XLSX", 7500);

        Part csvPart = findPartByContentType(reportAttachmentParts, "text/csv");
        verifyAttachment(csvPart, "CSV", 120);


        // DASHBOARD EXPORT
        List<Part> dashboardAttachmentParts = ImapClient.getAttachmentParts(dashboardMessages[0]);
        assertEquals(dashboardAttachmentParts.size(), 1, "Dashboard message has correct number of attachments.");
        assertTrue(dashboardAttachmentParts.get(0).getContentType().contains("application/pdf".toUpperCase()),
                "Dashboard attachment has PDF content type.");
        verifyAttachment(dashboardAttachmentParts.get(0), "PDF", 67000);
    }

    private boolean bothEmailsArrived(Message[] reportMessages, Message[] dashboardMessages) {
        return reportMessages.length > 0 && dashboardMessages.length > 0;
    }

    private void verifyAttachment(Part attachment, String type, long minimalSize) throws Exception {
        assertTrue(attachment.getSize() > minimalSize,
                "The attachment (" + type + ") has the expected minimal size. Expected " + minimalSize + "B, found " + attachment.getSize() + "B.");
    }
}
