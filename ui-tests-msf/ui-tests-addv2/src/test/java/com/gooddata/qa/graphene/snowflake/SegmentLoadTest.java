package com.gooddata.qa.graphene.snowflake;

import static com.gooddata.md.Restriction.identifier;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.ATTR_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.BOOLEAN_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_NAME;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_CLIENT_ID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.COLUMN_X_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_DELETED_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_NO_SYSTEM;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.DATASET_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.FACT_AGE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.NUMERIC_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PK_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PKCOLUMN_CUSKEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.PRIMARY_KEY;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_DELETED_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_NO_SYSTEM;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_ONLY_TIMESTAMP;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_CLIENTID;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TABLE_CUSTOMERS_TIMESTAMP_DELETED;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.TIMESTAMP_TYPE;
import static com.gooddata.qa.utils.snowflake.SnowflakeTableUtils.VARCHAR_TYPE;
import static com.gooddata.qa.utils.snowflake.DataSourceUtils.LOCAL_STAGE;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNormal;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetDeletedClientId;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetNoSystem;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetOnlyClientId;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetOnlyDeleted;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetOnlyTimeStamp;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampClientId;
import static com.gooddata.qa.utils.snowflake.DatasetUtils.datasetTimeStampDeleted;
import static com.gooddata.qa.utils.DateTimeUtils.parseToTimeStampFormat;

import com.gooddata.qa.graphene.enums.project.DeleteMode;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.qa.utils.snowflake.DataSourceRestRequest;
import com.gooddata.qa.utils.snowflake.DataSourceUtils;
import org.apache.http.ParseException;
import org.json.JSONException;
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
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.snowflake.ConnectionInfo;
import com.gooddata.qa.utils.snowflake.DatabaseColumn;
import com.gooddata.qa.utils.snowflake.ProcessUtils;
import com.gooddata.qa.utils.snowflake.SnowflakeUtils;

public class SegmentLoadTest extends AbstractADDProcessTest {

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
    private final String PROCESS_NAME = "AutoProcess Test" + generateHashString();

    private DataloadProcess dataloadProcess;
    private LocalDateTime lastSuccessful;
    private String time;

    private String timeIncremental;

    private SnowflakeUtils snowflakeUtils;
    private DataSourceUtils dataSourceUtils;
    private ProcessUtils domainProcessUtils;
    private DataSourceRestRequest dataSourceRestRequest;

    @Override
    protected void customizeProject() throws Throwable {
        super.customizeProject();
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        dataSourceRestRequest = new DataSourceRestRequest(domainRestClient, testParams.getProjectId());
    }

    @Test(dependsOnGroups = { "createProject" })
    public void initData() throws JSONException, IOException, SQLException {
        createLCM();
        ConnectionInfo connectionInfo = dataSourceUtils.createDefaultConnectionInfo(DATABASE_NAME);
        snowflakeUtils = new SnowflakeUtils(connectionInfo);
        snowflakeUtils.createDatabase(DATABASE_NAME);
        dataSourceId = dataSourceUtils.createDataSource(DATA_SOURCE_NAME, connectionInfo);
        // setUp Model projects
        Dataset datasetCustomer =
                new Dataset(DATASET_CUSTOMERS).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerNoSystem =
                        new Dataset(DATASET_CUSTOMERS_NO_SYSTEM).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerOnlyClientId =
                        new Dataset(DATASET_CUSTOMERS_ONLY_CLIENTID).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerOnlyTimestamp =
                        new Dataset(DATASET_CUSTOMERS_ONLY_TIMESTAMP).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerOnlyDeleted =
                        new Dataset(DATASET_CUSTOMERS_ONLY_DELETED).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerTimestampClientId =
                        new Dataset(DATASET_CUSTOMERS_TIMESTAMP_CLIENTID).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerDeletedClientId =
                        new Dataset(DATASET_CUSTOMERS_DELETED_CLIENTID).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE),
                datasetCustomerTimestampDeleted =
                        new Dataset(DATASET_CUSTOMERS_TIMESTAMP_DELETED).withPrimaryKey(PK_CUSKEY).withAttributes(ATTR_NAME).withFacts(FACT_AGE);

        // create MAQL
        setupMaql(new LdmModel().withDataset(datasetCustomer).withDataset(datasetCustomerNoSystem)
                .withDataset(datasetCustomerOnlyClientId).withDataset(datasetCustomerOnlyTimestamp)
                .withDataset(datasetCustomerOnlyDeleted).withDataset(datasetCustomerTimestampClientId)
                .withDataset(datasetCustomerDeletedClientId).withDataset(datasetCustomerTimestampDeleted)
                .buildMaqlUsingPrimaryKey());

        // create Tables Snowflake
        DatabaseColumn
                custKeyColumn = new DatabaseColumn(PKCOLUMN_CUSKEY, VARCHAR_TYPE, PRIMARY_KEY),
                nameColumn = new DatabaseColumn(COLUMN_NAME, VARCHAR_TYPE),
                ageColumn = new DatabaseColumn(COLUMN_AGE, NUMERIC_TYPE),
                timestampColumn = new DatabaseColumn(COLUMN_X_TIMESTAMP, TIMESTAMP_TYPE),
                deletedColumn = new DatabaseColumn(COLUMN_X_DELETED, BOOLEAN_TYPE),
                clientIdColumn = new DatabaseColumn(COLUMN_X_CLIENT_ID, VARCHAR_TYPE);

        List<DatabaseColumn> listColumn1 = asList(custKeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn, clientIdColumn),
                listColumnNoSystem = asList(custKeyColumn, nameColumn, ageColumn),
                listColumnOnlyClientId = asList(custKeyColumn, nameColumn, ageColumn, clientIdColumn),
                listColumnOnlyTimestamp = asList(custKeyColumn, nameColumn, ageColumn, timestampColumn),
                listColumnOnlyDeleted = asList(custKeyColumn, nameColumn, ageColumn, deletedColumn),
                listColumnTimestampClientId = asList(custKeyColumn, nameColumn, ageColumn, timestampColumn, clientIdColumn),
                listColumnDeletedClientId = asList(custKeyColumn, nameColumn, ageColumn, deletedColumn, clientIdColumn),
                listColumnTimestampDeleted = asList(custKeyColumn, nameColumn, ageColumn, timestampColumn, deletedColumn);

        snowflakeUtils.createTable(TABLE_CUSTOMERS, listColumn1)
                .createTable(TABLE_CUSTOMERS_NO_SYSTEM, listColumnNoSystem)
                .createTable(TABLE_CUSTOMERS_ONLY_CLIENTID, listColumnOnlyClientId)
                .createTable(TABLE_CUSTOMERS_ONLY_TIMESTAMP, listColumnOnlyTimestamp)
                .createTable(TABLE_CUSTOMERS_ONLY_DELETED, listColumnOnlyDeleted)
                .createTable(TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, listColumnTimestampClientId)
                .createTable(TABLE_CUSTOMERS_DELETED_CLIENTID, listColumnDeletedClientId)
                .createTable(TABLE_CUSTOMERS_TIMESTAMP_DELETED, listColumnTimestampDeleted);

        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(
                serviceProject, PROCESS_NAME, dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        domainProcessUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        lcmBrickFlowBuilder.deleteMasterProject();
        lcmBrickFlowBuilder.runLcmFlow();
        lastSuccessful = LocalDateTime.now().withNano(0);
        time = parseToTimeStampFormat(lastSuccessful);
        timeIncremental = parseToTimeStampFormat(LocalDateTime.now().plusSeconds(3));

    }

    @DataProvider
    public Object[][] dataFirstLoadHasClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS, datasetNormal(), DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS1", "User", "28", time, "0", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_ONLY_CLIENTID, datasetOnlyClientId(), DATASET_CUSTOMERS_ONLY_CLIENTID, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS3", "User", "28", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, datasetTimeStampClientId(), DATASET_CUSTOMERS_TIMESTAMP_CLIENTID,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS6", "Phong", "28", time, CLIENT_ID_1) },
                { TABLE_CUSTOMERS_DELETED_CLIENTID, datasetDeletedClientId(), DATASET_CUSTOMERS_DELETED_CLIENTID, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS7", "Phong", "28", "0", CLIENT_ID_1) } };
    }

    @Test(dependsOnMethods = { "initData" }, dataProvider = "dataFirstLoadHasClientId")
    public void checkFirstLoadHasClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                          List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());

        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        List<String> custKeyValues = new ArrayList<>();
        ResultSet result = snowflakeUtils.getRecords(table, column);
        while (result.next()) {
            custKeyValues.add(result.getString(column));
        }
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId1, CLIENT_ID_1, dataset)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custKeyValues.toArray()));
    }

    @DataProvider
    public Object[][] dataFirstLoadNoClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS_NO_SYSTEM, datasetNoSystem(), DATASET_CUSTOMERS_NO_SYSTEM, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS2", "User", "28") },
                { TABLE_CUSTOMERS_ONLY_TIMESTAMP, datasetOnlyTimeStamp(), DATASET_CUSTOMERS_ONLY_TIMESTAMP, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS4", "Phong", "28", time) },
                { TABLE_CUSTOMERS_ONLY_DELETED, datasetOnlyDeleted(), DATASET_CUSTOMERS_ONLY_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS5", "Phong", "28", "0") },
                { TABLE_CUSTOMERS_TIMESTAMP_DELETED, datasetTimeStampDeleted(), DATASET_CUSTOMERS_TIMESTAMP_DELETED,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS8", "Phong", "28", time, "0") } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadHasClientId" }, dataProvider = "dataFirstLoadNoClientId")
    public void checkFirstLoadNoClientId(String table, CsvFile csvfile, String dataset, String column, String attribute,
                                         List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());

        // CHECK RESULT
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        List<String> custkeyValues = new ArrayList<>();
        ResultSet result = snowflakeUtils.getRecords(table, column);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog,
                containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
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
    public Object[][] incrementalHasTimeStampAndClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS, datasetNormal(), DATASET_CUSTOMERS, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS1B", "User", "28", timeIncremental, "0", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_TIMESTAMP_CLIENTID, datasetTimeStampClientId(), DATASET_CUSTOMERS_TIMESTAMP_CLIENTID,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS6B", "Phong", "28", timeIncremental, CLIENT_ID_1) } };
    }

    @Test(dependsOnMethods = { "checkFirstLoadNoClientId" }, dataProvider = "incrementalHasTimeStampAndClientId")
    public void checkIncrementalLoadHasTimeStampAndClientId(String table, CsvFile csvfile, String dataset, String column,
                                                            String attribute, List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        ResultSet result = snowflakeUtils.getRecords(table, column);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] incrementalHasTimeStampAndNoClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS_ONLY_TIMESTAMP, datasetOnlyTimeStamp(), DATASET_CUSTOMERS_ONLY_TIMESTAMP, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS4B", "Phong", "28", timeIncremental) },
                { TABLE_CUSTOMERS_TIMESTAMP_DELETED, datasetTimeStampDeleted(), DATASET_CUSTOMERS_TIMESTAMP_DELETED,
                        PKCOLUMN_CUSKEY, PK_CUSKEY, asList("CUS8B", "Phong", "28", timeIncremental, "0") } };
    }

    @Test(dependsOnMethods = {"checkIncrementalLoadHasTimeStampAndClientId" }, dataProvider = "incrementalHasTimeStampAndNoClientId")
    public void checkIncrementalLoadHasTimeStampAndNoClientId(String table, CsvFile csvfile, String dataset, String column,
                                                              String attribute, List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail = domainProcessUtils.execute(parameters);
        String executionLog = domainProcessUtils.getExecutionLog(detail.getLogUri(), serviceProjectId);
        // GET RESULT FROM Snowflake
        List<String> custkeyValues = new ArrayList<String>();
        ResultSet result = snowflakeUtils.getRecords(table, column);
        while (result.next()) {
            custkeyValues.add(result.getString(column));
        }
        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId1, CLIENT_ID_1, dataset, lastSuccessful)));
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));

        assertThat(executionLog,
                containsString(
                        String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, incremental, loadDataFrom=%s}]",
                                clientProjectId2, CLIENT_ID_2, dataset, lastSuccessful)));
        Attribute attributeCustkeyClient2 = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(custkeyValues.toArray()));
    }

    @DataProvider
    public Object[][] incrementalNoTimeStampAndHasClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS_ONLY_CLIENTID, datasetOnlyClientId(), DATASET_CUSTOMERS_ONLY_CLIENTID, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS3B", "User", "28", CLIENT_ID_1) },
                { TABLE_CUSTOMERS_DELETED_CLIENTID, datasetDeletedClientId(), DATASET_CUSTOMERS_DELETED_CLIENTID, PKCOLUMN_CUSKEY,
                        PK_CUSKEY, asList("CUS7B", "Phong", "28", "0", CLIENT_ID_1) } };
    }

    @Test(dependsOnMethods = { "checkIncrementalLoadHasTimeStampAndNoClientId" }, dataProvider = "incrementalNoTimeStampAndHasClientId")
    public void checkIncrementalLoadNoTimeStampAndHasClientId(String table, CsvFile csvfile, String dataset, String column,
                                                              String attribute, List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
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
    }

    @DataProvider
    public Object[][] incrementalNoTimeStampAndNoClientId() {
        return new Object[][] {
                { TABLE_CUSTOMERS_NO_SYSTEM, datasetNoSystem(), DATASET_CUSTOMERS_NO_SYSTEM, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS2B", "User", "28") },
                { TABLE_CUSTOMERS_ONLY_DELETED, datasetOnlyDeleted(), DATASET_CUSTOMERS_ONLY_DELETED, PKCOLUMN_CUSKEY, PK_CUSKEY,
                        asList("CUS5B", "Phong", "28", "0") } };
    }

    @Test(dependsOnMethods = { "checkIncrementalLoadNoTimeStampAndHasClientId" }, dataProvider = "incrementalNoTimeStampAndNoClientId")
    public void checkIncrementalLoadNoTimeStampAndNoClientId(String table, CsvFile csvfile, String dataset, String column,
                                                             String attribute, List<String> data) throws SQLException, IOException {
        csvfile.rows(data);
        csvfile.saveToDisc(testParams.getCsvFolder());
        log.info("This is path of CSV File :" + csvfile.getFilePath());
        snowflakeUtils.uploadCsv2Snowflake(LOCAL_STAGE, "", table, csvfile.getFilePath());
        // RUN SCHEDULE
        JSONObject jsonDataset = domainProcessUtils.setModeDefaultDataset(dataset);
        String valueParam = domainProcessUtils.getDataset(jsonDataset);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
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
        Attribute attributeCustkey = getMdService().getObj(project1, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkey), containsInAnyOrder(custkeyValues.toArray()));
        assertThat(executionLog, containsString(String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}]",
                clientProjectId2, CLIENT_ID_2, dataset)));
        Attribute attributeCustkeyClient2 = getMdService().getObj(project2, Attribute.class,
                identifier("attr." + dataset + "." + attribute));
        assertThat(getAttributeValues(attributeCustkeyClient2), containsInAnyOrder(custkeyValues.toArray()));
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws SQLException {
        testParams.setProjectId(testParams.getProjectId());
        if (testParams.getDeleteMode() == DeleteMode.DELETE_NEVER) {
            return;
        }
        domainRestClient.getProcessService().removeProcess(dataloadProcess);
        lcmBrickFlowBuilder.destroy();
        dataSourceRestRequest.deleteDataSource(dataSourceId);
        snowflakeUtils.dropDatabaseIfExists(DATABASE_NAME);
    }

    private void createLCM() throws ParseException, IOException {
        lcmBrickFlowBuilder = new LcmBrickFlowBuilder(testParams, useK8sExecutor);
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
                .setClient(CLIENT_ID_2, clientProjectId2).buildLcmProjectParameters();
        lcmBrickFlowBuilder.runLcmFlow();
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId1);
        addUserToSpecificProject(testParams.getUser(), UserRoles.ADMIN, clientProjectId2);
    }
}