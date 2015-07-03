package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForFragmentVisible;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.NoSuchElementException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess;
import com.gooddata.qa.graphene.entity.disc.OverviewProjectDetails.OverviewProcess.OverviewSchedule;
import com.gooddata.qa.graphene.entity.disc.ProjectInfo;
import com.gooddata.qa.graphene.entity.disc.ScheduleBuilder;
import com.gooddata.qa.graphene.enums.disc.DeployPackages;
import com.gooddata.qa.graphene.enums.disc.DeployPackages.Executables;
import com.gooddata.qa.graphene.enums.disc.OverviewProjectStates;
import com.gooddata.qa.graphene.enums.disc.ScheduleCronTimes;

public class OverviewPageTest extends AbstractOverviewProjectsTest {

    @BeforeClass
    public void initProperties() {
        projectTitle = "Disc-test-overview-page";
        startPageContext = new StartPageContext() {
            
            @Override
            public void waitForStartPageLoaded() {
                waitForFragmentVisible(discOverview);
            }
            
            @Override
            public String getStartPage() {
                return DISC_OVERVIEW_PAGE;
            }
        };
    }

    @AfterMethod
    public void afterTest(Method m) {
        cleanWorkingProjectAfterTest(m);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkDefaultOverviewState() {
        openOverviewPage();
        assertTrue(discOverview.isActive(OverviewProjectStates.FAILED));
        assertFalse(discOverview.isActive(OverviewProjectStates.RUNNING));
        assertFalse(discOverview.isActive(OverviewProjectStates.SCHEDULED));
        assertFalse(discOverview.isActive(OverviewProjectStates.SUCCESSFUL));
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyFailedState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.FAILED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptyRunningState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.RUNNING, getWorkingProject());
    }

    // Disable this test until MSF-7415 is fixed, because this test fails randomly when run DISC
    // tests in parallel
    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void checkEmptyScheduledState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.SCHEDULED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkEmptySucessfulState() {
        openOverviewPage();
        checkFilteredOutOverviewProject(OverviewProjectStates.SUCCESSFUL, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkFailedStateNumber() {
        checkOverviewStateNumber(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkRunningStateNumber() {
        checkOverviewStateNumber(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkSuccessfulStateNumber() {
        checkOverviewStateNumber(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkScheduledStateNumber() {
        OverviewProjectDetails overviewProject = new OverviewProjectDetails();
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Scheduled State Number");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
        prepareDataForOverviewScheduledStateTests(getProjects(),
                overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

        openOverviewPage();
        discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
        discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                discOverviewProjects.getOverviewProjectNumber());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkCombinedStatesNumber() {
        String processName = "Check Combined States Number";
        openProjectDetailByUrl(getWorkingProject().getProjectId());
        deployInProjectDetailPage(DeployPackages.BASIC, processName);

        createSchedule(new ScheduleBuilder().setProcessName(processName)
                .setExecutable(Executables.FAILED_GRAPH)
                .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                .setMinuteInHour("59"));
        scheduleDetail.manualRun();
        scheduleDetail.assertFailedExecution(Executables.FAILED_GRAPH);
        scheduleDetail.clickOnCloseScheduleButton();

        createSchedule(new ScheduleBuilder().setProcessName(processName)
                .setExecutable(Executables.SUCCESSFUL_GRAPH)
                .setCronTime(ScheduleCronTimes.CRON_EVERYDAY).setHourInDay("23")
                .setMinuteInHour("59"));
        scheduleDetail.manualRun();
        scheduleDetail.assertSuccessfulExecution();

        openOverviewPage();
        discOverview.selectOverviewState(OverviewProjectStates.FAILED);
        discOverview.assertOverviewStateNumber(OverviewProjectStates.FAILED,
                discOverviewProjects.getOverviewProjectNumber());
        discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
        discOverview.assertOverviewStateNumber(OverviewProjectStates.SUCCESSFUL,
                discOverviewProjects.getOverviewProjectNumber());
        checkFilteredOutOverviewProject(OverviewProjectStates.RUNNING, getWorkingProject());
        /*
         * Remove checking step in SCHEDULED state until MSF-7415 is fixed
         * 
         * checkFilteredOutOverviewProject(OverviewProjectStates.SCHEDULED, getWorkingProject());
         */
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewFailedProjects() {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Failed Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Failed Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

        prepareDataForCheckingOverviewState(OverviewProjectStates.FAILED, overviewProject);
        openOverviewPage();
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.assertOverviewProject(OverviewProjectStates.FAILED, overviewProject);

        checkOtherOverviewStates(OverviewProjectStates.FAILED, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewSuccessfulProject() {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Successful Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Successful Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

        prepareDataForCheckingOverviewState(OverviewProjectStates.SUCCESSFUL, overviewProject);
        openOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(OverviewProjectStates.SUCCESSFUL);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.assertOverviewProject(OverviewProjectStates.SUCCESSFUL,
                overviewProject);

        checkOtherOverviewStates(OverviewProjectStates.SUCCESSFUL, getWorkingProject());
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void checkOverviewScheduledProject() {
        ProjectInfo projectInfo = new ProjectInfo().setProjectName("Disc-test-scheduled-state");
        List<ProjectInfo> additionalProjects = Arrays.asList(projectInfo);
        createMultipleProjects(additionalProjects);
        try {
            OverviewProjectDetails overviewProject =
                    new OverviewProjectDetails().setProjectInfo(getWorkingProject());
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Overview Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
            prepareDataForOverviewScheduledStateTests(additionalProjects, overviewProject);

            openOverviewPage();
            waitForElementVisible(discOverview.getRoot());
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.assertOverviewProject(OverviewProjectStates.SCHEDULED,
                    overviewProject);
            checkOtherOverviewStates(OverviewProjectStates.SCHEDULED, getWorkingProject());
        } finally {
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void checkOverviewRunningProject() {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Running Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Running Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));

        prepareDataForCheckingOverviewState(OverviewProjectStates.RUNNING, overviewProject);
        openOverviewPage();
        waitForElementVisible(discOverview.getRoot());
        discOverview.selectOverviewState(OverviewProjectStates.RUNNING);
        waitForElementVisible(discOverviewProjects.getRoot());
        discOverviewProjects.assertOverviewProject(OverviewProjectStates.RUNNING, overviewProject);

        checkOtherOverviewStates(OverviewProjectStates.RUNNING, getWorkingProject());
    }

    @Test(dependsOnMethods = {"createProject"})
    public void restartFailedProjects() {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Overview Failed Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Failed Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
        bulkActionsProjectInOverviewPage(OverviewProjectStates.FAILED, overviewProject);

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        try {
            assertTrue(scheduleDetail.isStarted());
        } catch (NoSuchElementException ex) {
            assertEquals(scheduleDetail.getExecutionItemsNumber(), 2);
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void restartFailedSchedule() {
        bulkActionsScheduleInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableFailedProjects() {
        disableProjectInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableFailedSchedule() {
        disableScheduleInOverviewPage(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void runSuccessfulProjects() {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess()
                        .setProcessName("Check Run Overview Successful Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Successful Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
        bulkActionsProjectInOverviewPage(OverviewProjectStates.SUCCESSFUL, overviewProject);

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        assertTrue(scheduleDetail.isStarted());
        scheduleDetail.assertSuccessfulExecution();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void runSuccessfulSchedule() {
        bulkActionsScheduleInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableSuccessfulProjects() {
        disableProjectInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableSuccessfulSchedule() {
        disableScheduleInOverviewPage(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void stopRunningProjects() {
        OverviewProjectDetails overviewProject =
                new OverviewProjectDetails().setProjectInfo(getWorkingProject());
        OverviewProcess overviewProcess =
                overviewProject.newProcess().setProcessName("Check Stop Overview Running Project");
        OverviewSchedule overviewSchedule =
                overviewProcess.newSchedule().setScheduleName("Running Schedule");
        overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule));
        bulkActionsProjectInOverviewPage(OverviewProjectStates.RUNNING, overviewProject);

        browser.get(overviewSchedule.getScheduleUrl());
        waitForElementVisible(scheduleDetail.getRoot());
        scheduleDetail.assertManualStoppedExecution();
    }

    @Test(dependsOnMethods = {"createProject"})
    public void stopRunningSchedule() {
        bulkActionsScheduleInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableRunningProjects() {
        disableProjectInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(dependsOnMethods = {"createProject"})
    public void disableRunningSchedule() {
        disableScheduleInOverviewPage(OverviewProjectStates.RUNNING);
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void disableScheduledProjects() {
        ProjectInfo projectInfo = new ProjectInfo().setProjectName("Disc-test-scheduled-state");
        List<ProjectInfo> additionalProjects = Arrays.asList(projectInfo);
        createMultipleProjects(additionalProjects);
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Disable Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForOverviewScheduledStateTests(additionalProjects,
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            waitForElementVisible(discOverviewProjects.getRoot());
            discOverviewProjects.checkAllProjects();
            discOverviewProjects.disableAction();
            checkOtherOverviewStates(OverviewProjectStates.ALL, getWorkingProject());
        } finally {
            deleteProjects(additionalProjects);
        }
    }

    @Test(enabled = false, dependsOnMethods = {"createProject"})
    public void stopScheduledProjects() {
        ProjectInfo projectInfo = new ProjectInfo().setProjectName("Disc-test-scheduled-state");
        List<ProjectInfo> additionalProjects = Arrays.asList(projectInfo);
        createMultipleProjects(additionalProjects);
        try {
            OverviewProjectDetails overviewProject = new OverviewProjectDetails();
            OverviewProcess overviewProcess =
                    overviewProject.newProcess().setProcessName("Check Disable Scheduled Project");
            OverviewSchedule overviewSchedule =
                    overviewProcess.newSchedule().setScheduleName("Scheduled schedule");
            prepareDataForOverviewScheduledStateTests(additionalProjects,
                    overviewProject.addProcess(overviewProcess.addSchedule(overviewSchedule)));

            openOverviewPage();
            discOverview.selectOverviewState(OverviewProjectStates.SCHEDULED);
            discOverview.assertOverviewStateNumber(OverviewProjectStates.SCHEDULED,
                    discOverviewProjects.getOverviewProjectNumber());
            discOverviewProjects.checkAllProjects();
            discOverviewProjects.bulkAction(OverviewProjectStates.SCHEDULED);
            checkOtherOverviewStates(OverviewProjectStates.FAILED, getWorkingProject());

            browser.get(overviewSchedule.getScheduleUrl());
            waitForElementVisible(scheduleDetail.getRoot());
            scheduleDetail.assertManualStoppedExecution();
        } finally {
            deleteProjects(additionalProjects);
        }
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInFailedState() {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.FAILED);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInSucessfulState() {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.SUCCESSFUL);
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"project-overview"})
    public void checkProjectsNotAdminInRunningState() {
        checkOverviewProjectWithoutAdminRole(OverviewProjectStates.RUNNING);
    }
}
