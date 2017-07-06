package com.gooddata.qa.graphene.add.schedule;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.http.RestUtils.getResource;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.executeProcess;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.Schedule;
import com.gooddata.md.Attribute;
import com.gooddata.qa.graphene.common.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.entity.add.SyncDatasets;
import com.gooddata.qa.graphene.entity.ads.SqlBuilder;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DataloadScheduleDetail;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.DatasetDropdown;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog;
import com.gooddata.qa.graphene.fragments.disc.schedule.add.RunOneOffDialog.LoadMode;

public class RunOneOffDialogTest extends AbstractDataloadProcessTest {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final String FULL_LOAD = "Dataset with id: dataset.%s will be loaded in full mode.";
    private static final String INCREMENTAL_LOAD = "Dataset with id: dataset.%s will be loaded in incremental mode.";

    private static final String LOAD_NOTHING = "Dataset with id: dataset.%s won't be loaded since the "
            + "maximum timestamp is null or it is lower or equal than LSLTS, which is not allowed";

    private CsvFile opportunity;
    private CsvFile person;

    private String opportunityLSLTS;
    private String personLSLTS;

    @Test(dependsOnGroups = {"initDataload"}, groups = {"precondition"})
    public void initData() throws JSONException, IOException {
        setupMaql(new LdmModel()
                .withDataset(new Dataset(DATASET_OPPORTUNITY)
                        .withAttributes(ATTR_OPPORTUNITY)
                        .withFacts(FACT_PRICE))
                .withDataset(new Dataset(DATASET_PERSON)
                        .withAttributes(ATTR_PERSON)
                        .withFacts(FACT_AGE))
                .buildMaql());

        opportunityLSLTS = parseDateTime(LocalDateTime.now(), DATE_PATTERN);
        opportunity = new CsvFile(DATASET_OPPORTUNITY)
                .columns(new CsvFile.Column(ATTR_OPPORTUNITY), new CsvFile.Column(FACT_PRICE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("OP1", "100", opportunityLSLTS);

        personLSLTS = parseDateTime(LocalDateTime.now().plusSeconds(2), DATE_PATTERN);
        person = new CsvFile(DATASET_PERSON)
                .columns(new CsvFile.Column(ATTR_PERSON), new CsvFile.Column(FACT_AGE),
                        new CsvFile.Column(X_TIMESTAMP_COLUMN))
                .rows("P1", "18", personLSLTS);

        Parameters parameters = getDefaultParameters().addParameter(Parameter.SQL_QUERY,
                SqlBuilder.build(opportunity, person));
        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());
    }

    @Test(dependsOnGroups = {"precondition"})
    public void checkDefaultLoadSetAsDefault() {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            RunOneOffDialog dialog = initScheduleDetail(schedule).triggerRunOneOffDialog();
            assertThat(dialog.getModes(), hasItems(LoadMode.values()));
            assertEquals(dialog.getSelectedMode(), LoadMode.DEFAULT);

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"precondition"})
    public void editSyncDatasetInRunOneOffDialog() {
        Schedule schedule1 = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));
        Schedule schedule2 = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            DatasetDropdown dropdown = initScheduleDetail(schedule1).triggerRunOneOffDialog()
                    .getDatasetDropdown().expand();
            assertEquals(dropdown.getButtonText(), "1 of 2 datasets");
            assertEquals(dropdown.getSelectedDatasets(), singletonList(DATASET_PERSON));

            dropdown.selectAllDatasets().submit();
            assertEquals(dropdown.getButtonText(), "2 of 2 datasets");

            dropdown = initScheduleDetail(schedule2).triggerRunOneOffDialog().getDatasetDropdown().expand();
            assertEquals(dropdown.getButtonText(), "2 of 2 datasets");
            assertThat(dropdown.getSelectedDatasets(), hasItems(DATASET_PERSON, DATASET_OPPORTUNITY));

            dropdown.clearAllSelected();
            assertFalse(dropdown.isSaveButtonEnabled(),
                    "Save button is not disabled when there is no selected dataset");

        } finally {
            getProcessService().removeSchedule(schedule1);
            getProcessService().removeSchedule(schedule2);
        }
    }

    @Test(dependsOnGroups = {"precondition"}, groups = {"fullLoad"})
    public void checkDefaultLoadWorkAsFullLoad() throws ParseException, IOException {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            initScheduleDetail(schedule).executeSchedule(LoadMode.DEFAULT).waitForExecutionFinish();

            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            DatasetDropdown dropdown = scheduleDetail.selectCustomDatasetsOption()
                    .getDatasetDropdown().expand();
            assertEquals(dropdown.getLSLTSOf(DATASET_OPPORTUNITY), opportunityLSLTS);
            assertEquals(dropdown.getLSLTSOf(DATASET_PERSON), personLSLTS);

            String executionLog = getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK);
            assertThat(executionLog, containsString(format(FULL_LOAD, DATASET_OPPORTUNITY)));
            assertThat(executionLog, containsString(format(FULL_LOAD, DATASET_PERSON)));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"fullLoad"})
    public void checkDefaultLoadWorkAsIncrementalLoad() throws ParseException, IOException {
        personLSLTS = parseDateTime(LocalDateTime.now(), DATE_PATTERN);
        person.rows("P2", "20", personLSLTS);

        Parameters parameters = getDefaultParameters()
                .addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person));
        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(LoadMode.DEFAULT).waitForExecutionFinish();

            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            assertEquals(scheduleDetail.getDatasetDropdown().expand().getLSLTSOf(DATASET_PERSON), personLSLTS);

            String executionLog = getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK);
            assertThat(executionLog, containsString(format(INCREMENTAL_LOAD, DATASET_PERSON)));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"fullLoad"})
    public void checkDefaultLoadDoNothing() throws ParseException, IOException {
        person.rows("P3", "20", personLSLTS);

        Parameters parameters = getDefaultParameters()
                .addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person));
        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(LoadMode.DEFAULT).waitForExecutionFinish();

            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);
            assertEquals(scheduleDetail.getDatasetDropdown().expand().getLSLTSOf(DATASET_PERSON), personLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), not(hasItem("P3")));

            String executionLog = getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK);
            assertThat(executionLog, containsString(format(LOAD_NOTHING, DATASET_PERSON)));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"fullLoad"})
    public void checkDefaultLoadInCombinationCase() throws ParseException, IOException {
        opportunity.rows("OP2", "100", opportunityLSLTS);

        personLSLTS = parseDateTime(LocalDateTime.now(), DATE_PATTERN);
        person.rows("P4", "20", personLSLTS);

        Parameters parameters = getDefaultParameters()
                .addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person));
        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            initScheduleDetail(schedule).executeSchedule(LoadMode.DEFAULT).waitForExecutionFinish();

            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            DatasetDropdown dropdown = scheduleDetail.selectCustomDatasetsOption().getDatasetDropdown().expand();
            assertEquals(dropdown.getLSLTSOf(DATASET_OPPORTUNITY), opportunityLSLTS);
            assertEquals(dropdown.getLSLTSOf(DATASET_PERSON), personLSLTS);

            String executionLog = getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK);
            assertThat(executionLog, containsString(format(INCREMENTAL_LOAD, DATASET_PERSON)));
            assertThat(executionLog, containsString(format(LOAD_NOTHING, DATASET_OPPORTUNITY)));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"fullLoad"})
    public void checkForceFullLoad() throws ParseException, IOException {
        person.rows("P5", "20", personLSLTS);

        Parameters parameters = getDefaultParameters()
                .addParameter(Parameter.SQL_QUERY, SqlBuilder.build(opportunity, person));
        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON));

        try {
            initScheduleDetail(schedule).executeSchedule(LoadMode.FULL).waitForExecutionFinish();

            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            DatasetDropdown dropdown = scheduleDetail.getDatasetDropdown().expand();
            assertEquals(dropdown.getLSLTSOf(DATASET_PERSON), personLSLTS);

            Attribute personAttr = getMdService().getObj(getProject(), Attribute.class, title(ATTR_PERSON));
            assertThat(getAttributeValues(personAttr), hasItem("P5"));

            String executionLog = getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK);
            assertThat(executionLog, containsString(format(FULL_LOAD, DATASET_PERSON)));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"fullLoad"})
    public void checkDefaultLoadUsedForAutoTrigger() throws ParseException, IOException {
        personLSLTS = parseDateTime(LocalDateTime.now(), DATE_PATTERN);
        person.rows("P6", "20", personLSLTS);

        Parameters parameters = getDefaultParameters()
                .addParameter(Parameter.SQL_QUERY, SqlBuilder.build(person));
        executeProcess(getGoodDataClient(), updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                parameters.getParameters(), parameters.getSecureParameters());

        LocalTime autoStartTime = LocalTime.now().plusMinutes(2);
        Schedule schedule = createSchedule(generateScheduleName(), SyncDatasets.custom(DATASET_PERSON),
                parseTimeToCronExpression(autoStartTime));

        try {
            initScheduleDetail(schedule).waitForAutoExecute(autoStartTime).waitForExecutionFinish();

            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule);

            DatasetDropdown dropdown = scheduleDetail.getDatasetDropdown().expand();
            assertEquals(dropdown.getLSLTSOf(DATASET_PERSON), personLSLTS);

            String executionLog = getResource(getRestApiClient(), scheduleDetail.getLastExecutionLogUri(), HttpStatus.OK);
            assertThat(executionLog, containsString(format(INCREMENTAL_LOAD, DATASET_PERSON)));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }

    @Test(dependsOnGroups = {"fullLoad"})
    public void checkRunOneOffDialogInitializeAsSameWay() {
        Schedule schedule = createScheduleForManualTrigger(generateScheduleName(), SyncDatasets.ALL);

        try {
            DataloadScheduleDetail scheduleDetail = initScheduleDetail(schedule)
                    .executeSchedule(LoadMode.FULL, SyncDatasets.custom(DATASET_PERSON));
            scheduleDetail.waitForExecutionFinish();

            RunOneOffDialog dialog = scheduleDetail.triggerRunOneOffDialog();
            assertEquals(dialog.getSelectedMode(), LoadMode.DEFAULT);
            assertThat(dialog.getDatasetDropdown().expand().getSelectedDatasets(),
                    hasItems(DATASET_PERSON, DATASET_OPPORTUNITY));

        } finally {
            getProcessService().removeSchedule(schedule);
        }
    }
}
