package com.gooddata.qa.graphene.fragments.disc;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.ScheduleStatus;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import static com.gooddata.qa.graphene.common.CheckUtils.*;
import static org.testng.Assert.*;

public class ProjectDetailPage extends AbstractFragment {

    private static final String PROCESS_METADATA_ID = "Process ID";
    private static final By BY_PROCESS_TITLE = By.cssSelector(".ait-process-title");
    private final static By BY_PROCESS_DELETE_BUTTON = By.cssSelector(".ait-process-delete-btn");
    private final static By BY_PROCESS_DOWNLOAD_BUTTON = By
            .cssSelector(".ait-process-download-btn");
    private final static By BY_PROCESS_REDEPLOY_BUTTON = By
            .cssSelector(".ait-process-redeploy-btn");
    private final static By BY_CREATE_NEW_SCHEDULE_LINK = By.cssSelector(".action-important-link");
    private final static By BY_PROCESS_SCHEDULE_TAB = By.cssSelector(".ait-process-schedules-btn");
    private final static By BY_PROCESS_EXECUTABLE_TABLE = By
            .cssSelector(".ait-process-executable-list");
    private final static By BY_PROCESS_EXECUTABLE_TAB = By
            .cssSelector(".ait-process-executables-btn");
    private final static By BY_PROCESS_METADATA_TAB = By.cssSelector(".ait-process-metadata-btn");
    private final static By BY_BROKEN_SCHEDULE_MESSAGE = By
            .cssSelector(".broken-schedules-section .message");
    private final static By BY_PROJECT_METADATA_KEY = By.cssSelector(".ait-metadata-key");
    private final static By BY_PROJECT_METADATA_VALUE = By.cssSelector(".ait-metadata-value");
    private final static By BY_PROCESS_METADATA_KEY = By.cssSelector(".ait-process-metadata-key");
    private final static By BY_PROCESS_METADATA_VALUE = By
            .cssSelector(".ait-process-metadata-value");
    private final static By BY_PROCESS_NOTIFICATION_RULE_BUTTON = By
            .cssSelector(".ait-process-notification-rules-btn");
    private final static By BY_DEPLOY_ERROR_DIALOG = By.cssSelector(".error_dialog .dialog-body"); 

    private static final String DELETE_PROCESS_DIALOG_MESSAGE =
            "Are you sure you want to delete process \"%s\"?";
    private static final String DELETE_PROCESS_DIALOG_TITLE = "Delete \"%s\" process";
    private final static String BROKEN_SCHEDULE_SECTION_MESSAGE =
            "The schedules cannot be executed. "
                    + "Its process has been re-deployed with modified graphs or a different folder structure.";
    private static final String EXECUTABLE_NO_SCHEDULES = "No schedules";
    private static final String EXECUTABLE_SCHEDULE_NUMBER = "Scheduled %d time%s";

    @FindBy(css = ".ait-project-title")
    private WebElement displayedProjectTitle;

    @FindBy(css = ".ait-project-metadata-list-item")
    private List<WebElement> projectMetadataItems;

    @FindBy(css = ".ait-process-metadata-list-item")
    private List<WebElement> processMetadataItems;

    @FindBy(xpath = "//a/span[text()='Go to Dashboards']")
    private WebElement goToDashboardsLink;

    @FindBy(css = ".ait-project-empty-state .title")
    private WebElement projectEmptyStateTitle;

    @FindBy(css = ".ait-project-empty-state .message")
    private WebElement projectEmptyStateMessage;

    @FindBy(css = ".ait-project-deploy-process-btn")
    private WebElement deployProcessButton;

    @FindBy(css = ".ait-process-list-item")
    private List<WebElement> processes;

    @FindBy(css = ".ait-process-list-item.active")
    private WebElement activeProcess;

    @FindBy(css = ".ait-process-executable-list")
    private ExecutablesTable executablesTable;

    @FindBy(css = ".error_dialog .s-btn-ok")
    private WebElement deployErrorDialogOKButton;

    @FindBy(css = ".ait-project-new-schedule-btn")
    private WebElement newScheduleButton;

    @FindBy(css = ".ait-process-list-item.active")
    private SchedulesTable scheduleTable;

    @FindBy(css = ".ait-process-delete-fragment")
    private WebElement processDeleteDialog;

    @FindBy(css = ".dialog-title")
    private WebElement processDeleteDialogTitle;

    @FindBy(css = ".dialog-body")
    private WebElement processDeleteDialogMessage;

    @FindBy(css = ".ait-process-delete-confirm-btn")
    private WebElement processDeleteConfirmButton;

    @FindBy(css = ".ait-process-delete-cancel-btn")
    private WebElement processDeleteCancelButton;

    public String getDisplayedProjectTitle() {
        return waitForElementVisible(displayedProjectTitle).getText();
    }

    public String getProjectMetadata(final String metadataKey) {
        return Iterables.find(projectMetadataItems, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement projectMetadataItem) {
                return projectMetadataItem.findElement(BY_PROJECT_METADATA_KEY).getText()
                        .equals(metadataKey);
            }
        }).findElement(BY_PROJECT_METADATA_VALUE).getText();
    }

    public String getProcessMetadata(final String metadataKey) {
        return Iterables.find(processMetadataItems, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement processMetadataItem) {
                return processMetadataItem.findElement(BY_PROCESS_METADATA_KEY).getText()
                        .equals(metadataKey);
            }
        }).findElement(BY_PROCESS_METADATA_VALUE).getText();
    }

    public void goToDashboards() {
        waitForElementVisible(goToDashboardsLink).click();
    }

    public String getProjectEmptyStateTitle() {
        return waitForElementVisible(projectEmptyStateTitle).getText();
    }

    public String getProjectEmptyStateMessage() {
        return waitForElementVisible(projectEmptyStateMessage).getText();
    }

    public WebElement getDeployErrorDialog() {
        return waitForElementVisible(BY_DEPLOY_ERROR_DIALOG, browser);
    }
    
    public boolean isErrorDialogVisible() {
        return getRoot().findElements(BY_DEPLOY_ERROR_DIALOG).size() > 0;
    }

    public void closeDeployErrorDialogButton() {
        waitForElementVisible(deployErrorDialogOKButton).click();
    }

    public void clickOnDeployProcessButton() {
        waitForElementPresent(deployProcessButton).click();
    }

    public void assertNewDeployedProcessInList(String processName, DeployPackages deployPackage) {
        WebElement process = findProcess(processName);
        process.findElement(BY_PROCESS_SCHEDULE_TAB).click();
        assertEquals("0 schedules", process.findElement(BY_PROCESS_SCHEDULE_TAB).getText());
        process.findElement(BY_PROCESS_EXECUTABLE_TAB).click();
        List<Executables> executables = deployPackage.getExecutables();
        String executableTitle =
                deployPackage.getPackageType().getProcessTypeExecutable()
                        + (executables.size() > 1 ? "s" : "");
        assertEquals(String.format("%d %s total", executables.size(), executableTitle), process
                .findElement(BY_PROCESS_EXECUTABLE_TAB).getText());
        waitForElementVisible(executablesTable.getRoot());
        assertExecutableList(executables);
    }

    public void clickOnNewScheduleButton() {
        waitForElementVisible(newScheduleButton).click();
    }

    public int getNumberOfProcesses() {
        if (processes == null) {
            throw new NullPointerException();
        }
        return processes.size();
    }

    public void assertExecutableList(List<Executables> executables) {
        waitForElementVisible(BY_PROCESS_EXECUTABLE_TABLE, browser);
        executablesTable.assertExecutableList(executables);
    }

    public void assertActiveProcessInList(String processName, DeployPackages deployPackage,
            ScheduleBuilder... schedules) {
        waitForElementVisible(activeProcess);
        assertEquals(activeProcess.findElement(BY_PROCESS_TITLE).getText(), processName);
        activeProcess.findElement(BY_PROCESS_SCHEDULE_TAB).click();
        String scheduleTabTitle =
                String.format("%d schedule", schedules.length) + (schedules.length == 1 ? "" : "s");
        assertEquals(activeProcess.findElement(BY_PROCESS_SCHEDULE_TAB).getText(), scheduleTabTitle);
        activeProcess.findElement(BY_PROCESS_EXECUTABLE_TAB).click();
        List<Executables> executableList = deployPackage.getExecutables();
        String executableTabTitle =
                String.format("%d %s total", executableList.size(), deployPackage.getPackageType()
                        .getProcessTypeExecutable() + (executableList.size() > 1 ? "s" : ""));
        assertEquals(activeProcess.findElement(BY_PROCESS_EXECUTABLE_TAB).getText(),
                executableTabTitle);
        assertExecutableList(executableList);
    }

    public WebElement getNewScheduleLinkInSchedulesList(String processName) {
        return findProcess(processName).findElement(BY_CREATE_NEW_SCHEDULE_LINK);
    }

    public void clickOnRedeployButton(String processName) {
        findProcess(processName).findElement(BY_PROCESS_REDEPLOY_BUTTON).click();
    }

    public WebElement getExecutableTabByProcessName(String processName) {
        return findProcess(processName).findElement(BY_PROCESS_EXECUTABLE_TAB);
    }

    public WebElement getScheduleTabByProcessName(String processName) {
        return findProcess(processName).findElement(BY_PROCESS_SCHEDULE_TAB);
    }

    public WebElement getExecutableScheduleLink(String executableName) {
        return executablesTable.getExecutableScheduleLink(executableName);
    }

    public WebElement checkEmptySchedulesList(String processName) {
        return findProcess(processName).findElement(BY_CREATE_NEW_SCHEDULE_LINK);
    }

    public void checkBrokenScheduleSection(String processName) {
        waitForElementVisible(activeProcess).findElement(BY_PROCESS_SCHEDULE_TAB).click();
        System.out.println("Broken schedule message in project detail page: "
                + activeProcess.findElement(BY_BROKEN_SCHEDULE_MESSAGE).getText());
        assertEquals(BROKEN_SCHEDULE_SECTION_MESSAGE,
                activeProcess.findElement(BY_BROKEN_SCHEDULE_MESSAGE).getText());
    }

    public void checkDownloadProcess(String processName, String downloadFolder, String projectID,
            final long minimumDownloadedFileSize) {
        String processID = getProcessID(processName);
        clickOnDownloadProcessButton(processName);
        final File zipDownload =
                new File(downloadFolder + projectID + "_" + processID + "-decrypted.zip");
        Graphene.waitGui().withTimeout(3, TimeUnit.MINUTES).pollingEvery(10, TimeUnit.SECONDS)
                .until(new Predicate<WebDriver>() {

                    @Override
                    public boolean apply(WebDriver arg0) {
                        System.out.println("Wait for downloading process!");
                        return zipDownload.length() > minimumDownloadedFileSize;
                    }
                });
        System.out.println("Download file size: " + zipDownload.length());
        System.out.println("Download file path: " + zipDownload.getPath());
        System.out.println("Download file name: " + zipDownload.getName());
        assertTrue(zipDownload.length() > minimumDownloadedFileSize, "Process \"" + processName
                + "\" is downloaded sucessfully!");
        zipDownload.delete();
    }

    public void checkSortedProcesses() {
        for (int i = 0; i < processes.size(); i++) {
            System.out
                    .println("Title of Process[" + i + "] : " + getProcessTitle(processes.get(i)));
            if (i > 0) {
                assertTrue(getProcessTitle(processes.get(i)).compareTo(
                        getProcessTitle(processes.get(i - 1))) >= 0);
            }
        }
    }

    public void checkFocusedProcess(final String processName) {
        waitForElementVisible(activeProcess);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return processName.equals(getProcessTitle(activeProcess));
            }
        });
    }

    public void deleteProcess(String processName) {
        final int processNumberBeforeDelete = processes.size();
        System.out.println("Process to delete: " + processName);
        clickOnDeleteProcessButton(processName);
        waitForElementVisible(processDeleteConfirmButton).click();
        waitForElementNotPresent(processDeleteDialog);
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return processes.size() == processNumberBeforeDelete - 1;
            }
        });
        System.out.println("Process " + processName + " has been deleted!");
        waitForElementVisible(getRoot());
    }

    public void checkDeleteProcessDialog(String processName) {
        String deleteProcessTitle = String.format(DELETE_PROCESS_DIALOG_TITLE, processName);
        String deleteProcessMessage = String.format(DELETE_PROCESS_DIALOG_MESSAGE, processName);
        clickOnDeleteProcessButton(processName);
        waitForElementVisible(processDeleteDialogTitle);
        assertEquals(processDeleteDialogTitle.getText(), deleteProcessTitle);
        waitForElementVisible(processDeleteDialogMessage);
        assertEquals(processDeleteDialogMessage.getText(), deleteProcessMessage);
    }

    public void checkCancelDeleteProcess(String processName) {
        clickOnDeleteProcessButton(processName);
        waitForElementVisible(processDeleteCancelButton).click();
        waitForElementNotPresent(processDeleteDialog);
        assertTrue(assertIsExistingProcess(processName));
    }

    public void deleteAllProcesses() {
        for (int i = processes.size() - 1; i >= 0; i--) {
            deleteProcess(getProcessTitle(processes.get(i)));
        }
    }

    public boolean assertIsExistingProcess(String processName) {
        waitForElementVisible(getRoot());
        Optional<WebElement> existingProcess = tryToFindProcess(processName);
        return existingProcess.isPresent();
    }

    public void checkExecutableScheduleNumber(String processName, String executableName,
            int scheduleNumber) {
        String executableScheduleNumber =
                String.format(EXECUTABLE_SCHEDULE_NUMBER, scheduleNumber, (scheduleNumber > 1 ? "s"
                        : ""));
        getExecutableTabByProcessName(processName).click();
        waitForElementVisible(executablesTable.getRoot());
        if (scheduleNumber > 0) {
            assertEquals(executableScheduleNumber,
                    executablesTable.getExecutableScheduleNumber(executableName));
        } else
            assertEquals(EXECUTABLE_NO_SCHEDULES,
                    executablesTable.getExecutableScheduleNumber(executableName));
    }

    public void selectScheduleTab(String processName) {
        getScheduleTabByProcessName(processName).click();
        assertActiveProcessTabs(processName, true, false, false);
    }

    public void selectExecutableTab(String processName) {
        getExecutableTabByProcessName(processName).click();
        assertActiveProcessTabs(processName, false, true, false);
    }

    public void selectMetadataTab(String processName) {
        getMetadataTabByProcessName(processName).click();
        assertActiveProcessTabs(processName, false, false, true);
    }

    public void assertScheduleStatus(String scheduleName, ScheduleStatus scheduleStatus) {
        WebElement schedule = scheduleTable.getSchedule(scheduleName);
        assertNotNull(schedule);
        if (scheduleStatus == ScheduleStatus.ERROR)
            assertTrue(schedule.getAttribute("class").contains("is-error"));
        assertNotNull(schedule.findElement(scheduleStatus.getIconByCss()));
        if (scheduleStatus == ScheduleStatus.UNSCHEDULED)
            assertFalse(schedule.getAttribute("class").contains("is-error"));
    }

    public WebElement getNotificationButton(String processName) {
        return waitForElementVisible(getElementFromSpecificProcess(processName,
                BY_PROCESS_NOTIFICATION_RULE_BUTTON));
    }

    public void assertScheduleInList(final SchedulesTable schedulesTable,
            final ScheduleBuilder scheduleBuilder) {
        Graphene.waitGui().until(new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver arg0) {
                return schedulesTable.getScheduleTitle(scheduleBuilder.getScheduleName()) != null;
            }
        });
        assertEquals(schedulesTable.getScheduleCron(scheduleBuilder.getScheduleName()).getText(),
                scheduleBuilder.getCronTimeBuilder().getCronFormatInProjectDetailPage());
    }

    private void assertActiveProcessTabs(String processName, boolean activeScheduleTab,
            boolean activeExecutableTab, boolean activeMetadataTab) {
        assertEquals(
                getScheduleTabByProcessName(processName).getAttribute("class").contains("active"),
                activeScheduleTab);
        assertEquals(
                getExecutableTabByProcessName(processName).getAttribute("class").contains("active"),
                activeExecutableTab);
        assertEquals(
                getMetadataTabByProcessName(processName).getAttribute("class").contains("active"),
                activeMetadataTab);
    }

    private void clickOnDeleteProcessButton(String processName) {
        findProcess(processName).findElement(BY_PROCESS_DELETE_BUTTON).click();
        waitForElementVisible(processDeleteDialog);
    }

    private void clickOnDownloadProcessButton(String processName) {
        findProcess(processName).findElement(BY_PROCESS_DOWNLOAD_BUTTON).click();
    }

    private String getProcessID(String processName) {
        waitForElementVisible(getElementFromSpecificProcess(processName, BY_PROCESS_METADATA_TAB))
                .click();
        return getProcessMetadata(PROCESS_METADATA_ID);
    }

    private String getProcessTitle(WebElement process) {
        return process.findElement(BY_PROCESS_TITLE).getText();
    }

    private WebElement getMetadataTabByProcessName(String processName) {
        return findProcess(processName).findElement(BY_PROCESS_METADATA_TAB);
    }

    private WebElement getElementFromSpecificProcess(String processName, By elementLocator) {
        return findProcess(processName).findElement(elementLocator);
    }

    private Optional<WebElement> tryToFindProcess(final String processName) {
        Optional<WebElement> existingProcess =
                Iterables.tryFind(processes, new Predicate<WebElement>() {

                    @Override
                    public boolean apply(WebElement process) {
                        return getProcessTitle(process).equals(processName);
                    }
                });
        return existingProcess;
    }

    private WebElement findProcess(final String processName) {
        return Iterables.find(processes, new Predicate<WebElement>() {

            @Override
            public boolean apply(WebElement process) {
                return process.findElement(BY_PROCESS_TITLE).getText().equals(processName);
            }
        });
    }
}
