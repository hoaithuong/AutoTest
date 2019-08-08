package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.md.Restriction.identifier;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetOnlyTimeStamp;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampClientId;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampDeleted;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TIMESTAMP_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.VARCHAR_TYPE;
import static com.gooddata.qa.utils.snowflake.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;
import static java.util.Arrays.asList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.qa.utils.snowflake.ConnectionInfo;
import com.gooddata.qa.utils.snowflake.DataSourceRestRequest;
import com.gooddata.qa.utils.snowflake.DataSourceUtils;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.ProcessExecutionDetail;
import com.gooddata.md.Attribute;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.AbstractADDProcessTest;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.model.Dataset;
import com.gooddata.qa.graphene.entity.model.LdmModel;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.snowflake.DatabaseColumn;
import com.gooddata.qa.utils.snowflake.ProcessUtils;
import com.gooddata.qa.utils.snowflake.SnowflakeUtils;

public class SegmentForceLoadTest extends AbstractADDProcessTest {

    private Project serviceProject;
    private String clientProjectId1;
    private String clientProjectId2;
    private Project project1;
    private Project project2;
    private String dataSourceId;
    private String serviceProjectId;
    private final String CLIENT_ID_1 = "att_client_" + generateHashString();
    private final String CLIENT_ID_2 = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE_1 = "ATT_LCM Client project " + generateHashString();
    private final String CLIENT_PROJECT_TITLE_2 = "ATT_LCM Client project " + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String DATA_SOURCE_NAME = "Auto_datasource" + generateHashString();
    private final String DATABASE_NAME = "ATT_DATABASE" + generateHashString();
    private final String PROCESS_NAME = "Process Test" + generateHashString();
    private SnowflakeUtils snowflakeUtils;
    private ProcessUtils domainProcessUtils;
    private DataloadProcess dataloadProcess;
    private LocalDateTime lastSuccessful;
    private String timeForceFullLoad;
    private String timeLoadFrom;
    private String timeLoadTo;
    private String timeOverRange;
    private DataSourceUtils dataSourceUtils;
    private DataSourceRestRequest dataSourceRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
    }

    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void initData() throws SQLException, IOException {
        createLCM();
        ConnectionInfo connectionInfo = dataSourceUtils.createDefaultConnectionInfo(DATABASE_NAME);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSourceUtils.createDataSource(DATA_SOURCE_NAME, connectionInfo);

        // setUp Model projects
        Dataset datasetCustomer = new Dataset(DATASET_CUSTOMERS).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerOnlyTimestamp =
                        new Dataset(DATASET_CUSTOMERS_ONLY_TIMESTAMP).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerTimestampClientId =
                        new Dataset(DATASET_CUSTOMERS_TIMESTAMP_CLIENTID).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerTimestampDeleted =
                        new Dataset(DATASET_CUSTOMERS_TIMESTAMP_DELETED).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE);

        // create MAQL
        setupMaql(new LdmModel().withDataset(datasetCustomer).withDataset(datasetCustomerOnlyTimestamp)
                .withDataset(datasetCustomerTimestampClientId).withDataset(datasetCustomerTimestampDeleted)
                .buildMaqlUsingPrimaryKey());

        // create Tables Snowflake
        DatabaseColumn custKeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY),
                nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE),
                ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE),
                timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE),
                deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE),
                clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn>
                listColumn1 = Arrays.asList(custKeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn, clientIdColumn),
                listColumnOnlyTimestamp = Arrays.asList(custKeyColumn, nameColumn, ageColumn, timestampColumn),
                listColumnTimestampClientId = Arrays.asList(custKeyColumn, nameColumn, ageColumn, timestampColumn, clientIdColumn),
                listColumnTimestampDeleted = Arrays.asList(custKeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn);

        snowflakeUtils.createTable(TABLE_CUSTOMERS, listColumn1)
                .createTable(TABLE_CUSTOMERS_ONLY_TIMESTAMP, listColumnOnlyTimestamp)
                .createTable(TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, listColumnTimestampClientId)
                .createTable(TABLE_CUSTOMERS_TIMESTAMP_DELETED, listColumnTimestampDeleted);

        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(
                serviceProject, PROCESS_NAME, dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        lastSuccessful = LocalDateTime.now().withNano(0);
        timeForceFullLoad = parseToTimeStampFormat(lastSuccessful);
        timeLoadFrom = parseToTimeStampFormat(LocalDateTime.now().plusSeconds(3));
        timeLoadTo = parseToTimeStampFormat(LocalDateTime.now().plusSeconds(5));
        timeOverRange = parseToTimeStampFormat(LocalDateTime.now().plusSeconds(7));
    }

    @DataProvider
    public Object[][] dataFirstLoadHasClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS, datasetNormal(), DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY,  
                        asList("CUS1", "User", "28", timeForceFullLoad, "0", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, datasetTimeStampClientId(), DATASET_CUSTOMERS_TIMESTAMP_CLIENTID,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS6", "Phong", "28", timeForceFullLoad, CLIENT_ID_1) } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkForceFullLoadHasClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                              List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeFullDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<>();
        ResultSet result = snowflakeUtils.getRecords(table, column);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId1, CLIENT_ID_1, dataset)));
        Attribute attributeCustKey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustKey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataFirstLoadNoClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS_ONLY_TIMESTAMP, datasetOnlyTimeStamp(), DATASET_CUSTOMERS_ONLY_TIMESTAMP, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS4", "Phong", "28", timeForceFullLoad) },
                { TABLE_CUSTOMERS_TIMESTAMP_DELETED, datasetTimeStampDeleted(), DATASET_CUSTOMERS_TIMESTAMP_DELETED,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS8", "Phong", "28", timeForceFullLoad, "0") } };
    }


    @Test(dependsOnMethods = { "checkForceFullLoadHasClientId" }, dataProvider = "dataFirstLoadNoClientId")
    public void checkForceFullLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                             List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeFullDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "FULL");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        ResultSet result = snowflakeUtils.getRecords(table, column);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId1, CLIENT_ID_1, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId2, CLIENT_ID_2, dataset)));
        Attribute attributeCustkeyClient2 = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataIncrementalLoadHasClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS, datasetNormal(), DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS1B", "User", "30", timeLoadFrom, "0", CLIENT_ID_1),
                        asList("CUS1C", "User", "30", timeLoadTo, "0", CLIENT_ID_1),
                        asList("CUS1D", "User", "30", timeOverRange, "0", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, datasetTimeStampClientId(), DATASET_CUSTOMERS_TIMESTAMP_CLIENTID,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS6B", "Phong", "30", timeLoadFrom, CLIENT_ID_1),
                        asList("CUS6C", "Phong", "30", timeLoadTo, CLIENT_ID_1),
                        asList("CUS6D", "Phong", "30", timeOverRange, CLIENT_ID_1) } };
    }

    @Test(dependsOnMethods = { "checkForceFullLoadNoClientId" }, dataProvider = "dataIncrementalLoadHasClientId")
    public void checkForceIncrementalLoadHasClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                                     List<String> dataFrom, List<String> dataTo, List<String> dataOverRange) throws SQLException, IOException {
        csvfile.rows(dataFrom).rows(dataTo).rows(dataOverRange);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeIncrementalDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custKeyValues = new ArrayList<>();
        ResultSet result = snowflakeUtils.getRecordsInRangeTimeStamp(table, column, parseToTimeStampFormat(lastSuccessful), timeLoadTo);
        while (result.next()) {
            custKeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                        clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        assertThat(executionLog, containsString("lastTimestamp=" + timeLoadTo));
        Attribute attributeCustKey = getMdService().getObj(project1, Attribute.class, identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustKey), containsInAnyOrder(custKeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataIncrementalLoadNoClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS_ONLY_TIMESTAMP, datasetOnlyTimeStamp(), DATASET_CUSTOMERS_ONLY_TIMESTAMP, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS4B", "Phong", "30", timeLoadFrom), asList("CUS4C", "Phong", "30", timeLoadTo),
                        asList("CUS4D", "Phong", "30", timeOverRange) },
                { TABLE_CUSTOMERS_TIMESTAMP_DELETED, datasetTimeStampDeleted(), DATASET_CUSTOMERS_TIMESTAMP_DELETED,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS8B", "Phong", "30", timeLoadFrom, "0"),
                        asList("CUS8C", "Phong", "30", timeLoadTo, "0"), asList("CUS8D", "Phong", "30", timeOverRange, "0") } };
    }

    @Test(dependsOnMethods = { "checkForceIncrementalLoadHasClientId" }, dataProvider = "dataIncrementalLoadNoClientId")
    public void checkForceIncrementalLoadNoClientId(String table, CsvFile csvfile, String dataset, String column,
                                                    String attribute, List<String> dataFrom, List<String> dataTo, List<String> dataOverRange)
            throws SQLException, IOException {
        csvfile.rows(dataFrom).rows(dataTo).rows(dataOverRange);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeIncrementalDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "INCREMENTAL")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_FROM", timeLoadFrom)
                .addParameter("GDC_DATALOAD_SINGLE_RUN_DATE_TO", timeLoadTo);
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custKeyValues = new ArrayList<>();
        ResultSet result = snowflakeUtils.getRecordsInRangeTimeStamp(table, column, parseToTimeStampFormat(lastSuccessful),
                timeLoadTo);
        while (result.next()) {
            custKeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                        clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        assertThat(executionLog, containsString("lastTimestamp=" + timeLoadTo));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class, identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custKeyValues.toArray()));
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                        clientProjectId2, CLIENT_ID_2, dataset, lastSuccessful)));
        Attribute attributeCustkeyClient2 =
                getMdService().getObj(project2, Attribute.class, identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(custKeyValues.toArray()));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        new ScheduleUtils(domainRestClient).getProcessService().removeProcess(dataloadProcess);
        lcmBrickFlowBuilder.destroy();
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        snowflakeUtils.dropDatabaseIfExists(DATABASE_NAME);
    }

    private void createLCM() throws IOException {
        serviceProjectId = lcmBrickFlowBuilder.getLCMServiceProject().getServiceProjectId();
        serviceProject = domainRestClient.getProjectService().getProjectById(serviceProjectId);
        String devProjectId = testParams.getProjectId();
        log.info("dev project : " + devProjectId);

        clientProjectId1 = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE_1);
        project1 = domainRestClient.getProjectService().getProjectById(clientProjectId1);
        log.info("client 1 : " + clientProjectId1);

        clientProjectId2 = createNewEmptyProject(domainRestClient, CLIENT_PROJECT_TITLE_2);
        project2 = domainRestClient.getProjectService().getProjectById(clientProjectId2);
        log.info("client 2 : " + clientProjectId2);

        lcmBrickFlowBuilder.setDevelopProject(devProjectId).setSegmentId(SEGMENT_ID).setClient(CLIENT_ID_1, clientProjectId1)
                .setClient(CLIENT_ID_2, clientProjectId2).buildLcmProjectParameters().runLcmFlow();
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId1);
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId2);
    }
}