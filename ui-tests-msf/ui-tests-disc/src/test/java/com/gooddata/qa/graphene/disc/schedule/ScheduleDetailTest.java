package com.gooddata.qa.graphene.disc.schedule;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;

import java.io.IOException;

import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.Schedule;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.common.AbstractProcessTest;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleCronTime;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.process.AbstractProcessDetail.Tab;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.CronEditor;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.common.AbstractScheduleDetail.ExecutionHistoryItem;

public class ScheduleDetailTest extends AbstractProcessTest {

    private static final String BROKEN_SCHEDULE_MESSAGE = "The schedules cannot be executed. Its process has been"
            + " re-deployed with modified graphs or a different folder structure.";

    private static final String BROKEN_MESSAGE_IN_SCHEDULE_DETAIL = "The graph errorGraph.grf doesn't exist"
            + " because it has been changed (renamed or deleted). It isn't possible to execute this schedule"
            + " because there is no graph to execute.";

    private static final String TRIGGERING_SCHEDULE_ERROR_MESSAGE = "The schedule that triggers this schedule"
            + " is missing. To run this schedule, set it to be run manually, or set a new trigger, or set a cron frequency.";

    private static final String SCHEDULE_IN_LOOP_MESSAGE = "Schedules cannot be scheduled in a loop";

    @DataProvider(name = "rubyGitStoreProvider")
    public Object[][] rubyGitStoreProvider() {
        return new Object[][] {
            {"${PUBLIC_APPSTORE}:branch/prodigy-testing:/vietnam/default/ReadFile"},
            {"${PRIVATE_APPSTORE}:branch/prodigy-testing:/vietnam/default/ReadFile"}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "rubyGitStoreProvider")
    public void executeScheduleWithRubyInGitStore(String gitStorePath) {
        String processName = generateProcessName();
        initDiscProjectDetailPage().deployProcessWithGitStorePath(processName, gitStorePath);

        try {
            projectDetailPage.openCreateScheduleForm().schedule();

        ScheduleDetail scheduleDetail = ScheduleDetail.getInstance(browser);
        scheduleDetail.executeSchedule().waitForExecutionFinish();

        takeScreenshot(browser, "Execute-schedule-from-process-" + processName, getClass());
        assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
        assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                ScheduleStatus.OK.toString());

        } finally {
            initDiscProjectDetailPage().deleteProcess(processName);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void stopExecution() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.LONG_TIME_RUNNING_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.executeSchedule().stopExecution();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), "MANUALLY STOPPED");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkBrokenSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.ERROR_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());
            processDetail.redeployWithZipFile(process.getName(), ProcessType.CLOUD_CONNECT, PackageFile.ONE_GRAPH.loadFile());

            takeScreenshot(browser, "Broken-schedule-message-shows", getClass());
            assertEquals(processDetail.getBrokenScheduleMessage(), BROKEN_SCHEDULE_MESSAGE);

            ScheduleDetail scheduleDetail = processDetail.openSchedule(schedule.getName());
            takeScreenshot(browser, "Broken-schedule-message-shows-in-schedule-detail", getClass());
            assertEquals(scheduleDetail.getBrokenScheduleMessage(), BROKEN_MESSAGE_IN_SCHEDULE_DETAIL);

            scheduleDetail.selectExecutable(Executable.SUCCESSFUL_GRAPH).saveChanges();
            scheduleDetail.executeSchedule().waitForExecutionFinish();

            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(), ScheduleStatus.OK.toString());

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkExecutionHistoryEmptyState() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            assertEquals(initScheduleDetail(schedule).getExecutionHistoryEmptyMessage(),
                    "No history available. This schedule has not been run yet.");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSuccessfulExecutionGroup() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            executeScheduleWithSpecificTimes(scheduleDetail, 3);

            ExecutionHistoryItem item = scheduleDetail.getLastExecutionHistoryItem();
            assertTrue(item.isItemGroup(), "All sucessful executions are not grouped");
            assertEquals(item.getStatusDescription(), "OK 3×");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleCannotTriggerByItself() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            CronEditor cronEditor = initDiscProjectDetailPage()
                    .openCreateScheduleForm()
                    .getCronEditor()
                    .selectRunTime(ScheduleCronTime.AFTER);
            assertEquals(cronEditor.getEmptyTriggeringScheduleMessage(), SCHEDULE_IN_LOOP_MESSAGE);

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkScheduleTriggerInLoop() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.ERROR_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            createSchedule(process, Executable.SUCCESSFUL_GRAPH, schedule);

            CronEditor cronEditor = initScheduleDetail(schedule)
                    .getCronEditor().selectRunTime(ScheduleCronTime.AFTER);
            assertEquals(cronEditor.getEmptyTriggeringScheduleMessage(), SCHEDULE_IN_LOOP_MESSAGE);

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkMissingScheduleTrigger() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule1 = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule schedule2 = createSchedule(process, Executable.ERROR_GRAPH, schedule1);

            initScheduleDetail(schedule1).deleteSchedule();

            ProcessDetail processDetail = projectDetailPage.getProcess(process.getName());
            assertEquals(processDetail.getScheduleCronTime(schedule2.getName()), "Trigger schedule missing!");

            ScheduleDetail scheduleDetail = processDetail.openSchedule(schedule2.getName());
            assertEquals(scheduleDetail.getTriggeringScheduleErrorMessage(), TRIGGERING_SCHEDULE_ERROR_MESSAGE);
            assertTrue(scheduleDetail.getCronEditor().isSelectedManually());

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkExecutionLog() throws IOException, JSONException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            scheduleDetail.executeSchedule().waitForExecutionFinish();
            final CommonRestRequest restRequest = new CommonRestRequest(
                    new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
            assertNotNull(restRequest.getResource(scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK));

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkEffectiveUserOfSchedule() throws ParseException, JSONException, IOException {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());
        String otherUser = createAndAddUserToProject(UserRoles.ADMIN);

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            assertEquals(initScheduleDetail(schedule).getEffectiveUser(), testParams.getUser());

            logout();
            signInAtGreyPages(otherUser, testParams.getPassword());

            assertEquals(initScheduleDetail(schedule).getEffectiveUser(), testParams.getUser());

            ScheduleDetail.getInstance(browser).close();
            ScheduleDetail scheduleDetail = projectDetailPage.getProcess(process.getName())
                    .redeployWithZipFile(process.getName(), ProcessType.CLOUD_CONNECT, PackageFile.BASIC.loadFile())
                    .openSchedule(schedule.getName());
            assertEquals(scheduleDetail.getEffectiveUser(), otherUser);

        } finally {
            getProcessService().removeProcess(process);
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkExecutionTooltipOnTimeLine() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule successfulSchedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());
            Schedule failedSchedule = createSchedule(process, Executable.ERROR_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            String executionTimelineTooltip = initScheduleDetail(successfulSchedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getExecutionTimelineTooltip();
            takeScreenshot(browser, "Successful-execution-timeline-tooltip", getClass());
            assertThat(executionTimelineTooltip, containsString("Successful execution"));

            executionTimelineTooltip = initScheduleDetail(failedSchedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getExecutionTimelineTooltip();
            takeScreenshot(browser, "Failed-execution-timeline-tooltip", getClass());
            assertThat(executionTimelineTooltip, containsString("Failed execution"));

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void closeScheduleDetail() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ProcessDetail processDetail = initDiscProjectDetailPage().getProcess(process.getName());
            processDetail.openTab(Tab.SCHEDULE);
            processDetail.openSchedule(schedule.getName());
            processDetail.openTab(Tab.EXECUTABLE);
            assertFalse(ScheduleDetail.isVisible(browser), "Schedule detail is not close");

            processDetail.openTab(Tab.SCHEDULE);
            processDetail.openSchedule(schedule.getName()).close();
            assertFalse(ScheduleDetail.isVisible(browser), "Schedule detail is not close");

        } finally {
            getProcessService().removeProcess(process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void deleteSchedule() {
        DataloadProcess process = createProcessWithBasicPackage(generateProcessName());

        try {
            Schedule schedule = createSchedule(process, Executable.SUCCESSFUL_GRAPH,
                    ScheduleCronTime.EVERY_30_MINUTES.getExpression());

            ScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            scheduleDetail.clickDeleteScheduleButton().discard();
            assertTrue(projectDetailPage.getProcess(process.getName()).hasSchedule(schedule.getName()),
                    "Schedule is deleted");

            scheduleDetail.deleteSchedule();
            assertFalse(projectDetailPage.getProcess(process.getName()).hasSchedule(schedule.getName()),
                    "Schedule is not deleted");

        } finally {
            getProcessService().removeProcess(process);
        }
    }
}
