package com.gooddata.qa.graphene.disc.schedule;

import com.gooddata.qa.graphene.common.AbstractEtlProcessTest;
import com.gooddata.qa.graphene.enums.disc.schedule.ScheduleStatus;
import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm.ProcessType;
import com.gooddata.qa.graphene.fragments.disc.schedule.CreateScheduleForm;
import com.gooddata.qa.graphene.fragments.disc.schedule.ScheduleDetail;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class EtlProcessScheduleDetailTest extends AbstractEtlProcessTest {

    @Test(dependsOnGroups = {"createProject"})
    public void executionCSVDownloaderAndCheckSuccess() {
        String processName = generateProcessName();
        deployEtlProcessFromDiscWithDefaultConfig(processName, ProcessType.CSV_DOWNLOADER);

        Map<String, String> params = new HashMap<>();
        Map<String, String> secureParams = new HashMap<>();

        params.put("GDC_VERIFY_SSL", "false");
        params.put("ID", "csv_downloader_1");
        params.put("bds_path", DEFAULT_S3_CONFIGURATION_PATH);

        secureParams.put("bds_access_key", defaultS3AccessKey);
        secureParams.put("bds_secret_key", defaultS3SecretKey);
        secureParams.put("csv|options|secret_key", defaultS3SecretKey);
        try {
            // Create schedule
            String scheduleName = generateScheduleName();
            CreateScheduleForm scheduleForm = projectDetailPage.openCreateScheduleForm()
                    .selectProcess(processName)
                    .enterScheduleName(scheduleName);
            scheduleForm.selectRunTimeByEveryWeek(DayOfWeek.MONDAY, 1, 1);
            scheduleForm.addParameters(params);
            scheduleForm.addSecureParameters(secureParams);
            scheduleForm.schedule();

            // Execute schedule
            ScheduleDetail scheduleDetail = ScheduleDetail.getInstance(browser);
            scheduleDetail.executeSchedule().waitForExecutionFinish();

            // Check result success
            takeScreenshot(browser, "Execute-schedule-from-process-" + processName, getClass());
            assertEquals(scheduleDetail.getExecutionHistoryItemNumber(), 1);
            assertEquals(scheduleDetail.getLastExecutionHistoryItem().getStatusDescription(),
                    ScheduleStatus.OK.toString());

            // Check execution log
            final CommonRestRequest restRequest = new CommonRestRequest(
                    new RestClient(getProfile(Profile.ADMIN)), testParams.getProjectId());
            assertNotNull(restRequest.getResource(scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK));
        } catch (IOException e) {
            // Ignore
        } finally {
            initDiscProjectDetailPage().deleteProcess(processName);
        }
    }

}
