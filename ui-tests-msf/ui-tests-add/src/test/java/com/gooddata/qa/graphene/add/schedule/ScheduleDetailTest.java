package com.gooddata.qa.graphene.add.schedule;

import static com.gooddata.qa.graphene.enums.ResourceDirectory.MAQL_FILES;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.SQL_FILES;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.executeProcess;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.time.LocalTime;

import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.entity.model.SqlBuilder;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage.OverviewState;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewProjects.OverviewProjectItem;
import com.gooddata.qa.graphene.fragments.disc.process.ProcessDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail.ExecutionHistoryItem;

public class ScheduleDetailTest extends AbstractDataloadProcessTest {

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        setupMaql(LdmModel.loadFromFile(MAQL_FILES.getPath() + TxtFile.CREATE_LDM.getName()));

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_TABLE.getName()));

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void executeDataloadSchedule() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule)
                .schedule();

        try {
            ScheduleDetail scheduleDetail = ScheduleDetail.getInstance(browser)
                    .executeSchedule().waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void autoExecuteDataloadSchedule() {
        String schedule = "Schedule-" + generateHashString();
        LocalTime autoStartTime = LocalTime.now().plusMinutes(2);

        ((CreateScheduleForm) initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectRunTimeByCronExpression(parseTimeToCronExpression(autoStartTime)))
                .enterScheduleName(schedule)
                .schedule();

        try {
            ScheduleDetail scheduleDetail = ScheduleDetail.getInstance(browser)
                    .waitForAutoExecute(autoStartTime).waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkConcurrentDataLoadSchedule() {
        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.LARGE_ADS_TABLE.getName()));

        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        String schedule1 = "Schedule-" + generateHashString();
        String schedule2 = "Schedule-" + generateHashString();

        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule1)
                .schedule();
        ScheduleDetail.getInstance(browser).close();

        projectDetailPage
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule2)
                .schedule();
        ScheduleDetail.getInstance(browser).close();

        try {
            ProcessDetail processDetail = projectDetailPage.getProcess(DEFAULT_DATAlOAD_PROCESS_NAME);
            processDetail.openSchedule(schedule1).executeSchedule().close();
            processDetail.openSchedule(schedule2).executeSchedule().waitForExecutionFinish();

            ExecutionHistoryItem executionItem = ScheduleDetail.getInstance(browser)
                    .getLastExecutionHistoryItem();
            assertEquals(executionItem.getStatusDescription(), "SCHEDULER_ERROR");
            assertEquals(executionItem.getErrorMessage(), "The schedule did not run because one or more of the "
                    + "datasets in this schedule is already synchronizing.");

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule1);
            deleteScheduleByName(getDataloadProcess(), schedule2);

            parameters.addParameter("SQL_QUERY",
                    SqlBuilder.loadFromFile(SQL_FILES.getPath() + TxtFile.ADS_TABLE.getName()));
            executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                    parameters.getParameters(), parameters.getSecureParameters());
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void disableDataloadSchedule() {
        String schedule = "Schedule-" + generateHashString();
        LocalTime autoStartTime = LocalTime.now().plusMinutes(2);

        ((CreateScheduleForm) initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectRunTimeByCronExpression(parseTimeToCronExpression(autoStartTime)))
                .enterScheduleName(schedule)
                .schedule();

        try {
            ScheduleDetail scheduleDetail = ScheduleDetail.getInstance(browser).disableSchedule();
            assertFalse(scheduleDetail.canAutoTriggered(autoStartTime),
                    "Schedule executed automatically although disabled");

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void dependentDataloadSchedule() {
        String schedule1 = "Schedule-" + generateHashString();
        String schedule2 = "Schedule-" + generateHashString();

        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule1)
                .schedule();
        ScheduleDetail.getInstance(browser).close();

        ((CreateScheduleForm) projectDetailPage.openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .selectRunTimeByTriggeringSchedule(getScheduleId(getDataloadProcess(), schedule1)))
                .enterScheduleName(schedule2)
                .schedule();
        ScheduleDetail.getInstance(browser).close();

        try {
            ProcessDetail processDetail = projectDetailPage.getProcess(DEFAULT_DATAlOAD_PROCESS_NAME);
            processDetail.openSchedule(schedule1).executeSchedule().close();

            ScheduleDetail scheduleDetail = processDetail.openSchedule(schedule2)
                    .waitForAutoExecute(LocalTime.now()).waitForExecutionFinish();
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule1);
            deleteScheduleByName(getDataloadProcess(), schedule2);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDataloadScheduleAtOverviewPage() {
        String schedule = "Schedule-" + generateHashString();
        initDiscProjectDetailPage()
                .openCreateScheduleForm()
                .selectProcess(DEFAULT_DATAlOAD_PROCESS_NAME)
                .enterScheduleName(schedule)
                .schedule();

        try {
            ScheduleDetail.getInstance(browser).executeSchedule().waitForExecutionFinish();

            OverviewProjectItem project = initDiscOverviewPage()
                    .selectState(OverviewState.SUCCESSFUL).getOverviewProject(projectTitle);
            assertTrue(project.expand().hasSchedule(schedule), "Schedule " + schedule + " not show");
            assertEquals(project.getScheduleExecutable(schedule), "");

        } finally {
            deleteScheduleByName(getDataloadProcess(), schedule);
        }
    }
}
