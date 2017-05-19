package com.gooddata.qa.graphene.common;

import static java.util.Objects.nonNull;

import com.gooddata.GoodData;
import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.AbstractDataIntegrationTest;
import com.gooddata.qa.graphene.enums.disc.schedule.Executable;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.PackageFile;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;

public class AbstractProcessTest extends AbstractDataIntegrationTest {

    protected DataloadProcess createProcess(String processName, PackageFile packageFile, ProcessType type) {
        return createProcess(getGoodDataClient(), processName, packageFile, type);
    }

    protected DataloadProcess createProcess (GoodData goodDataClient, String processName,
            PackageFile packageFile, ProcessType type) {
        log.info("Create process: " + processName);
        return goodDataClient.getProcessService().createProcess(getProject(),
                new DataloadProcess(processName, type.getValue()), packageFile.loadFile());
    }

    protected DataloadProcess createProcessWithBasicPackage(String processName) {
        return createProcess(processName, PackageFile.BASIC, ProcessType.CLOUD_CONNECT);
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
}
