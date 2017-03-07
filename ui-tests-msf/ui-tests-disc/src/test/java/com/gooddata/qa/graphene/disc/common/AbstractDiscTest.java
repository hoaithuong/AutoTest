package com.gooddata.qa.graphene.disc.common;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;

import java.time.LocalTime;

import org.openqa.selenium.support.FindBy;

import com.gooddata.GoodData;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessService;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.fragments.disc.overview.OverviewPage;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectDetailPage;
import com.gooddata.qa.graphene.fragments.disc.projects.ProjectsPage;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;

public class AbstractDiscTest extends AbstractProjectTest {

    @FindBy(className = "l-page")
    protected OverviewPage overviewPage;

    @FindBy(className = "ait-projects-fragment")
    protected ProjectsPage projectsPage;

    @FindBy(className = ProjectDetailPage.CLASS_NAME)
    protected ProjectDetailPage projectDetailPage;

    protected OverviewPage initDiscOverviewPage() {
        openUrl(OverviewPage.URI);
        return waitForFragmentVisible(overviewPage).waitForPageLoaded();
    }

    protected ProjectsPage initDiscProjectsPage() {
        openUrl(ProjectsPage.URI);
        return waitForFragmentVisible(projectsPage).waitForPageLoaded();
    }

    protected ProjectDetailPage initDiscProjectDetailPage() {
        openUrl(format(ProjectDetailPage.URI, testParams.getProjectId()));
        return waitForFragmentVisible(projectDetailPage);
    }

    protected ScheduleDetail initScheduleDetail(Schedule schedule) {
        return initDiscProjectDetailPage().getProcessById(schedule.getProcessId()).openSchedule(schedule.getName());
    }

    protected DataloadProcess createProcessWithBasicPackage(String processName) {
        return createProcess(processName, PackageFile.BASIC, ProcessType.CLOUD_CONNECT);
    }

    protected DataloadProcess createProcess(String processName, PackageFile packageFile, ProcessType type) {
        return createProcess(getGoodDataClient(), processName, packageFile, type);
    }

    protected DataloadProcess createProcess (GoodData goodDataClient, String processName,
            PackageFile packageFile, ProcessType type) {
        log.info("Create process: " + processName);
        return goodDataClient.getProcessService().createProcess(getProject(),
                new DataloadProcess(processName, type.getValue()), packageFile.loadFile());
    }

    protected Schedule createSchedule(DataloadProcess process, Executable executable, String crontimeExpression) {
        return createSchedule(getGoodDataClient(), process, executable, crontimeExpression);
    }

    protected Schedule createSchedule(GoodData goodDataClient, DataloadProcess process, Executable executable,
            String crontimeExpression) {
        return createScheduleWithTriggerType(goodDataClient, process, null, executable, crontimeExpression);
    }

    protected Schedule createSchedule(DataloadProcess process, Executable executable, Schedule triggeringSchedule) {
        return createSchedule(process, null, executable, triggeringSchedule);
    }

    protected Schedule createSchedule(DataloadProcess process, String name, Executable executable,
            Schedule triggeringSchedule) {
        return createScheduleWithTriggerType(getGoodDataClient(), process, name, executable, triggeringSchedule);
    }

    protected void deleteScheduleByName(DataloadProcess process, String scheduleName) {
        getProcessService().removeSchedule(getScheduleByName(process, scheduleName));
    }

    protected String getScheduleId(DataloadProcess process, String scheduleName) {
        return getScheduleByName(process, scheduleName).getId();
    }

    protected String parseTimeToCronExpression(LocalTime time) {
        return format("%d * * * *", time.getMinute());
    }

    protected String generateProcessName() {
        return "Process-" + generateHashString();
    }

    protected void executeScheduleWithSpecificTimes(ScheduleDetail scheduleDetail, int times) {
        for (int i = 1; i <= times; i++) {
            scheduleDetail.executeSchedule().waitForExecutionFinish();
        }
    }

    protected ProcessService getProcessService() {
        return getGoodDataClient().getProcessService();
    }

    private Schedule createScheduleWithTriggerType(GoodData goodDataClient, DataloadProcess process, String name,
            Executable executable, Object triggerType) {
        String expectedExecutable = process.getExecutables()
                .stream().filter(e -> e.contains(executable.getPath())).findFirst().get();

        Schedule schedule = null;

        if (triggerType instanceof String) {
            schedule = new Schedule(process, expectedExecutable, (String) triggerType);
        } else {
            schedule = new Schedule(process, expectedExecutable, (Schedule) triggerType);
        }

        if (nonNull(name)) schedule.setName(name);

        return goodDataClient.getProcessService().createSchedule(getProject(), schedule);
    }

    private Schedule getScheduleByName(DataloadProcess process, String scheduleName) {
        return getProcessService().listSchedules(getProject()).stream()
                .filter(s -> s.getProcessId().equals(process.getId()))
                .filter(s -> scheduleName.equals(s.getName()))
                .findFirst()
                .get();
    }
}
