package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.fixture.utils.GoodSales.Metrics;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.entity.visualization.InsightMDConfiguration;
import com.gooddata.qa.graphene.entity.visualization.MeasureBucket;
import com.gooddata.qa.graphene.enums.ResourceDirectory;
import com.gooddata.qa.graphene.enums.indigo.ReportType;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.datasourcemgmt.*;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.IndigoDashboardsPage;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Insight;
import com.gooddata.qa.graphene.fragments.modeler.*;
import com.gooddata.qa.graphene.fragments.modeler.OverlayWrapper;
import com.gooddata.qa.graphene.fragments.modeler.datasource.*;
import com.gooddata.qa.utils.S3Utils;
import com.gooddata.qa.utils.cloudresources.*;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.indigo.IndigoRestRequest;
import com.gooddata.qa.utils.http.model.ModelRestRequest;
import com.gooddata.qa.utils.http.user.mgmt.UserManagementRestRequest;
import com.gooddata.qa.utils.lcm.LcmBrickFlowBuilder;
import com.gooddata.qa.utils.schedule.ScheduleUtils;
import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.model.project.Project;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.ParseException;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.browser.BrowserUtils.canAccessGreyPage;
import static com.gooddata.qa.graphene.AbstractTest.Profile.ADMIN;
import static com.gooddata.qa.graphene.AbstractTest.Profile.DOMAIN;
import static com.gooddata.qa.graphene.utils.ElementUtils.isElementVisible;
import static com.gooddata.qa.graphene.utils.Sleeper.sleepTightInSeconds;
import static com.gooddata.qa.utils.cloudresources.SnowflakeTableUtils.*;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static com.gooddata.qa.utils.io.ResourceUtils.getResourceAsString;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.*;
import static org.testng.Assert.assertFalse;

public class LDMRedshiftSegmentTest extends AbstractLDMPageTest {
    private final String DATASOURCE_NAME = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_NAME_OTHER = "Auto_datasource_" + generateHashString();
    private final String DATASOURCE_PREFIX = "PRE_";
    private final String DATABASE_NAME = "dev";
    private final String SCHEMA_NAME = "autoschema" + generateHashString() + "__" + getCurrentDate();
    private final String OTHER_SCHEMA_NAME = "autoschema" + generateHashString() + "__" + getCurrentDate();
    private final String CLIENT_ID_1 = "att_client_" + generateHashString();
    private final String CLIENT_ID_2 = "att_client_" + generateHashString();
    private final String CLIENT_PROJECT_TITLE_1 = "ATT_LCM Client project " + generateHashString();
    private final String CLIENT_PROJECT_TITLE_2 = "ATT_LCM Client project " + generateHashString();
    private final String SEGMENT_ID = "att_segment_" + generateHashString();
    private final String PERSON_TABLE = "person";
    private final String PRE_OTHER_TABLE = "pre_other";
    private final String PERSON_DATASET = "person";
    private final String CAR_TABLE = "car";
    private final String CAR_DATASET = "car";
    private final String TIMESTAMP_DATASET = "timestamp";
    private final String PRE_CAR_TABLE = "pre_car";
    private final String INVALID_NAME = "Invalid";
    private final String ID_ATTRIBUTE = "Id";
    private final String NAME_ATTRIBUTE = "Name";
    private final String AGE_FACT = "Age";
    private final String CITY_ATTRIBUTE = "City";
    private final String BIRTHDAY_DATE = "Birthday";
    private final String CLIENT_ID_ATTRIBUTE = "Clientid";
    private final String DELETED_ATTRIBUTE = "Deleted";
    private final String COLOR_ATTRIBUTE = "Color";
    private final String YEAR_DATE = "Year";
    private final String PRICE_FACT = "Price";
    private final String PERSON_REFERENCE = "Person";
    private final String DISTRIBUTED_LOAD = "Distributed Load";
    private final String INCREMENTAL_LOAD = "Incremental Load";
    private final String DELETED_ROWS = "Deleted rows";
    private final String INSIGHT_NAME = "AGE CHART";
    private final String INSIGHT_NAME_2 = "PRICE CHART";
    private final String DASHBOARD_NAME = "DASHBOARD TEST";
    private final String DEFAULT_S3_BUCKET_URI = "s3://msf-dev-grest/";

    private String dataSourceId;
    private String serviceProjectId;
    private String clientProjectId1;
    private String clientProjectId2;
    private Project serviceProject;
    private Project project1;
    private Project project2;
    private LogicalDataModelPage ldmPage;
    private Modeler modeler;
    private Sidebar sidebar;
    private ToolBar toolbar;
    private Canvas canvas;
    private DataSourceUtils dataSourceUtils;
    private MainModelContent mainModelContent;
    private JSONObject modelView;
    private RestClient restClient;
    private DataSourceRestRequest dataSourceRestRequest;
    private IndigoRestRequest indigoRestRequest;
    private Project project;
    private String dataSourceOtherId;
    private RedshiftUtils redshiftUtils;
    private RedshiftUtils redshiftUtilOther;
    private DataSourceUtils dataSource;
    private ProcessUtils processUtils;
    private DataloadProcess dataloadProcess;
    private DataloadProcess processMaster;
    private OverlayWrapper wrapper;
    private DataSourcePanelContent datasourcePanel;
    private DataSourceDropDownBar dropDownBar;
    private DataSourceContent dataSourceContent;
    private DataSourceContentConnected dataSourceContentConnected;
    private DataSourceSchema datasourceSchema;
    private DataMappingUtils dataMappingProjectIdUtils;
    private String jsFile;
    private String projectId;
    private DataSourceManagementPage dataSourceManagementPage;
    private ContentWrapper contentWrapper;
    private DataSourceMenu dsMenu;
    private String DATASOURCE_URL;
    private String DATASOURCE_USERNAME;
    private String DATASOURCE_PASSWORD;
    private LcmBrickFlowBuilder lcmBrickFlowBuilder;
    private RestClient domainRestClient;
    private boolean useK8sExecutor = true;
    private String defaultS3AccessKey;
    private String defaultS3SecretKey;
    private DataSourceContentConnected connected;
    
    @Test(dependsOnGroups = { "createProject" }, groups = { "precondition" })
    public void initTest() throws Throwable {
        dataSourceUtils = new DataSourceUtils(testParams.getDomainUser());
        projectId = testParams.getProjectId();
        indigoRestRequest = new IndigoRestRequest(new RestClient(getProfile(Profile.ADMIN)), projectId);
        restClient = new RestClient(getProfile(ADMIN));
        domainRestClient = new RestClient(getProfile(DOMAIN));
        ConnectionInfo connectionInfo = dataSourceUtils.createRedshiftConnectionInfo(DATABASE_NAME, DatabaseType.REDSHIFT
                , SCHEMA_NAME);
        ConnectionInfo connectionOther = dataSourceUtils.createRedshiftConnectionInfo(DATABASE_NAME, DatabaseType.REDSHIFT
                ,OTHER_SCHEMA_NAME);
        redshiftUtils = new RedshiftUtils(connectionInfo);
        redshiftUtilOther = new RedshiftUtils(connectionOther);
        redshiftUtils.createSchema();
        redshiftUtilOther.createSchema();
        DATASOURCE_URL = testParams.getRedshiftJdbcUrl();
        DATASOURCE_USERNAME = testParams.getRedshiftUserName();
        DATASOURCE_PASSWORD = testParams.getRedshiftPassword();
        dataSourceManagementPage = initDatasourceManagementPage();
        contentWrapper = dataSourceManagementPage.getContentWrapper();
        dsMenu = dataSourceManagementPage.getMenuBar();
        defaultS3AccessKey = testParams.loadProperty("s3.accesskey");
        defaultS3SecretKey = testParams.loadProperty("s3.secretkey");
    }

    @Test(dependsOnMethods = "initTest")
    public void connectToWorkspaceTest() {
        createNewDatasource();
        ConnectWorkSpaceDialog connectWorkSpaceDialog = DatasourceHeading.getInstance(browser).clickConnectButton();
        connectWorkSpaceDialog.searchWorkspace(projectId);
        connectWorkSpaceDialog.selectedWorkspaceOnSearchList(projectId);
        ldmPage = connectWorkSpaceDialog.clickSelect();

        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();

        wrapper = OverlayWrapper.getInstance(browser);
        assertEquals(wrapper.getMessageConnectDatasource(), "Data Source connected. You can now use it in the model.");
        datasourcePanel = DataSourcePanelContent.getInstance(browser);
        dropDownBar = datasourcePanel.getDropdownDatasource();
        assertEquals(dropDownBar.getTextButton(), DATASOURCE_NAME);
        dataSourceContent = DataSourceContent.getInstance(browser);
        assertTrue(dataSourceContent.verifyConnectingMessage());
        connected = dataSourceContent.getDatasourceConnected();
        DataSourceSchema schema =  connected.getDatasourceSchema();
        assertEquals(schema.getTextNoTableInSchema(), "There are no tables in the schema");
        log.info("----Datasource verify----" + DATASOURCE_NAME);
    }

    @Test(dependsOnMethods = "connectToWorkspaceTest")
    public void refeshSchemaTest() throws SQLException, FileNotFoundException {
        prepareTables();
        updateData();
        dataSourceContent = DataSourceContent.getInstance(browser);
        DataSourceSchema schema =  connected.getDatasourceSchema();
        schema.clickRefeshSchema();
        assertTrue(dataSourceContent.verifyConnectingMessage());
        DataSourceSchemaContent schemaContent = schema.getSchemaContent();
        assertTrue(schemaContent.isTableExisting(PERSON_TABLE));
        assertTrue(schemaContent.isTableExisting(CAR_TABLE));
        assertTrue(schemaContent.isTableExisting(PRE_CAR_TABLE));
        assertEquals(schema.getTextSchemaName(), SCHEMA_NAME);

        BubleContent popUpContentPerson = schemaContent.openPopUpTable(PERSON_TABLE);
        assertEquals(popUpContentPerson.getItemName(), PERSON_TABLE + " (table)");
        List<String> listColumnOfPerSon = asList("id", "name", "age", "city", "birthday", "clientid", "timestamp", "deleted");
        assertTrue(popUpContentPerson.isPopUpContainsList(listColumnOfPerSon));
    }

    @Test(dependsOnMethods = "refeshSchemaTest" )
    public void searchAndReviewDataTest() {
        connected = dataSourceContent.getDatasourceConnected();
        connected.searchTable(PERSON_TABLE);
        DataSourceSchema schema =  connected.getDatasourceSchema();
        DataSourceSchemaContent schemaContent = schema.getSchemaContent();
        WebElement modelContent = modeler.getLayout().getRoot();
        jsFile = getResourceAsString("/dragdrop.js");

        //Add table Person and verify preview Dialog
        schemaContent.dragdropTableToCanvas(PERSON_TABLE, jsFile);
        PreviewCSVDialog dialog = PreviewCSVDialog.getInstance(browser);
        GenericList dropdownRecommend = GenericList.getInstance(browser);
        dropdownRecommend.selectBasicItem(GenericList.DATA_TYPE_PICKER.PRIMARY_KEY.getClassName());
        assertEquals(dialog.getListHeaders(), asList("id", "name", "age", "city", "birthday", "clientid", "timestamp", "deleted"));
        assertTrue(dialog.isShowCorrectRow("6"));
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(ID_ATTRIBUTE), "Primary key");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(NAME_ATTRIBUTE), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(AGE_FACT), "Measure");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(CITY_ATTRIBUTE), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(BIRTHDAY_DATE), "Date");
        dialog.clickImportButton();

        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        Model modelPerson = mainModelContent.getModel(PERSON_TABLE);
        mainModelContent.focusOnDataset(PERSON_TABLE);
        modelPerson.isPrimaryKeyExistOnDataset(PERSON_TABLE, ID_ATTRIBUTE.toLowerCase());

        //Add table PRE_CAR and verify preview Dialog
        connected.clearSearchText();
        connected.searchTable(PRE_OTHER_TABLE);
        assertEquals(dataSourceContent.getDatasourceNoResultText(), "No results for\n" + "\""
                + PRE_OTHER_TABLE + "\"");
        connected.clearSearchText();
        schemaContent.dragdropTableToCanvas(PRE_CAR_TABLE, jsFile);
        PreviewCSVDialog dialogCar = PreviewCSVDialog.getInstance(browser);
        GenericList dropdownRecommendCar = GenericList.getInstance(browser);
        dropdownRecommendCar.selectBasicItem(GenericList.DATA_TYPE_PICKER.PRIMARY_KEY.getClassName());
        assertEquals(dialogCar.getListHeaders(), asList("cp__id", "a__color", "d__year", "f__price", "r__person"));
        assertTrue(dialogCar.isShowCorrectRow("6"));
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(ID_ATTRIBUTE), "Primary key");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(COLOR_ATTRIBUTE), "Attribute");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(YEAR_DATE), "Date");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(PRICE_FACT), "Measure");
        assertEquals(dialog.getEditDatasetZone().getTextDatatypeByName(PERSON_REFERENCE), "Reference");
        dialog.clickImportButton();

        modeler.getLayout().waitForLoading();
        mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
        assertTrue(isElementVisible(mainModelContent.getModel(PERSON_DATASET).getRoot()));
        assertTrue(isElementVisible(mainModelContent.getModel(CAR_DATASET).getRoot()));
    }

    @Test(dependsOnMethods = "searchAndReviewDataTest")
    public void updateModelAndRunLCMTest() throws IOException {
        if (testParams.isTestingEnvironment()) {
            //delete Attribute, delete Date
            mainModelContent = canvas.getPaperScrollerBackground().getMainModelContent();
            mainModelContent.focusOnDataset(PERSON_DATASET);

            Model modelPerson = mainModelContent.getModel(PERSON_DATASET);
            modelPerson.deleteAttributeOnDataset("clientid").deleteAttributeOnDataset("deleted");

            DateModel modelTimestamp = mainModelContent.getDateModel(TIMESTAMP_DATASET);
            mainModelContent.focusOnDateDataset(TIMESTAMP_DATASET);
            modelTimestamp.deleteDateModel();
            // there are 2 overlay wrapper on UI now, need provide index
            OverlayWrapper.getInstanceByIndex(browser, 1).getConfirmDeleteDatasetDialog().clickDeleteDataset();

            //add table from OTHER schema
            dropDownBar.selectDatasource(DATASOURCE_NAME_OTHER);
            connected = dataSourceContent.clickButtonConnect();
            DataSourceSchema schema = connected.getDatasourceSchema();
            DataSourceSchemaContent schemaContent = schema.getSchemaContent();
            jsFile = getResourceAsString("/dragdrop.js");
            schemaContent.dragdropTableToCanvas(PRE_OTHER_TABLE, jsFile);
            PreviewCSVDialog dialogOther = PreviewCSVDialog.getInstance(browser);
            dialogOther.clickImportButton();

            //publish model
            toolbar = modeler.getToolbar();
            toolbar.clickPublish();
            PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
            publishModelDialog.overwriteDataSwitchToEditMode();
            //setUpKPIs on Master workspace after publish
            setUpKPIs();
            setUpProcessOnMaster();
            createLCM();
        } else {
            throw new SkipException("Skip test LCM on Client demo !!");
        }
    }

    /**
     * Verify cover ticket TMA-1275 Skip cloning ADDv2 process into LCM master
     */
    @Test(dependsOnMethods = {"updateModelAndRunLCMTest"})
    public void checkADDv2NotSync() {
        assertFalse(initDISCIgnoreAlert(clientProjectId1).hasProcess("PROCESS_NAME"),
                "Process should not be sync");
    }

    @Test(dependsOnMethods = "checkADDv2NotSync" )
    public void editAndPublishModelTest() throws IOException {
        String masterProjectId = lcmBrickFlowBuilder.getLCMServiceProject().getMasterProject(testParams.getUserDomain(), SEGMENT_ID);
        logout();
        signInAtUI(testParams.getDomainUser(), testParams.getPassword());
        initModelerPageContent(masterProjectId);
        Model modelPerson = mainModelContent.getModel(PERSON_DATASET);
        // add mapping custom
        mainModelContent.focusOnDataset(PERSON_DATASET);
        modelPerson.openEditDialog();
        EditDatasetDialog dialog = EditDatasetDialog.getInstance(browser);
        DataMapping mappingTab = dialog.clickOnDataMappingTab();
        LdmControlLoad controlLoad = LdmControlLoad.getInstance(browser);
        // ON system fields
        controlLoad.toogleDistributedLoad().toogleIncrementalLoad().toogleDeletedRowsLoad();
        mappingTab.editSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName(),"id", false);
        mappingTab.editSourceColumnByName(NAME_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName(),"name", false);
        mappingTab.editSourceColumnByName(AGE_FACT, DataMapping.SOURCE_TYPE.FACT.getName(),"age", false);
        mappingTab.editSourceColumnByName(CITY_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName(),"city", false);
        mappingTab.editSourceColumnByName(BIRTHDAY_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName(),"birthday", false);
        mappingTab.editDistributedLoadMapping("clientid", true);
        mappingTab.editIncrementalLoadMapping("timestamp", true);
        mappingTab.editDeletedRowsMapping("deleted", true);
        dialog.saveChanges();

        //Verify Detail Dataset Person : Mapped to,Mapping Fields, Datatype,
        mainModelContent.focusOnDataset(PERSON_DATASET);
        // there are 2 overlay wrapper on UI now, need provide index
        modelPerson.openEditDialog();
        dialog.clickOnDataMappingTab();
        // ON system fields

        MappedTo mappedTo = MappedTo.getInstance(browser);
        assertEquals(mappedTo.getSourceName(), PERSON_TABLE);
        assertEquals(mappingTab.getSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "id");
        assertEquals(mappingTab.getSourceColumnByName(NAME_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "name");
        assertEquals(mappingTab.getSourceColumnByName(AGE_FACT, DataMapping.SOURCE_TYPE.FACT.getName()), "age");
        assertEquals(mappingTab.getSourceColumnByName(CITY_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "city");
        assertEquals(mappingTab.getSourceColumnByName(BIRTHDAY_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "birthday");
        assertEquals(mappingTab.getSourceColumnByName(DISTRIBUTED_LOAD, DataMapping.SOURCE_TYPE.DISTRIBUTED_LOAD.getName()), "clientid");
        assertEquals(mappingTab.getSourceColumnByName(INCREMENTAL_LOAD, DataMapping.SOURCE_TYPE.INCREMENTAL_LOAD.getName()), "timestamp");
        assertEquals(mappingTab.getSourceColumnByName(DELETED_ROWS, DataMapping.SOURCE_TYPE.DELETED_ROWS.getName()), "deleted");

        assertEquals(mappingTab.getSourceTypeByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName()), "text");
        assertEquals(mappingTab.getSourceTypeByName(AGE_FACT, DataMapping.SOURCE_TYPE.FACT.getName()), "number");
        assertEquals(mappingTab.getSourceTypeByName(BIRTHDAY_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName()), "date");
        assertEquals(mappingTab.getSourceTypeByName(DISTRIBUTED_LOAD, DataMapping.SOURCE_TYPE.DISTRIBUTED_LOAD.getName()), "String");
        assertEquals(mappingTab.getSourceTypeByName(INCREMENTAL_LOAD, DataMapping.SOURCE_TYPE.INCREMENTAL_LOAD.getName()), "Timestamp");
        assertEquals(mappingTab.getSourceTypeByName(DELETED_ROWS, DataMapping.SOURCE_TYPE.DELETED_ROWS.getName()), "Boolean");
        dialog.clickCancel();

        //Verify Detail Dataset Car :  Mapped to, Mapping Fields
        mainModelContent.focusOnDataset(CAR_DATASET);
        Model modelCar = mainModelContent.getModel(CAR_DATASET);
        modelCar.openEditDialog();
        DataMapping mappingTabCar = dialog.clickOnDataMappingTab();
        controlLoad.toogleDistributedLoad().toogleIncrementalLoad().toogleDeletedRowsLoad();
        mappingTabCar.editDistributedLoadMapping(INVALID_NAME, true);
        mappingTabCar.editIncrementalLoadMapping(INVALID_NAME, true);
        mappingTabCar.editDeletedRowsMapping(INVALID_NAME, true);
        mappingTabCar.editSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName(), INVALID_NAME, false);
        mappingTabCar.editSourceColumnByName(COLOR_ATTRIBUTE, DataMapping.SOURCE_TYPE.LABEL.getName(), INVALID_NAME, false);
        mappingTabCar.editSourceColumnByName(PRICE_FACT, DataMapping.SOURCE_TYPE.FACT.getName(), INVALID_NAME, false);
        mappingTabCar.editSourceColumnByName(YEAR_DATE, DataMapping.SOURCE_TYPE.REFERENCE.getName(), INVALID_NAME, false);
        mappingTabCar.editSourceColumnByName(ID_ATTRIBUTE, DataMapping.SOURCE_TYPE.REFERENCE.getName(), INVALID_NAME, false);
        dialog.saveChanges();

        //publish model
        toolbar = modeler.getToolbar();
        toolbar.clickPublish();
        PublishModelDialog publishModelDialog = PublishModelDialog.getInstance(browser);
        publishModelDialog.overwriteDataSwitchToEditMode();

        String sql = getResourceAsString("/model_redshift_current.txt");
        ModelRestRequest modelRestRequest = new ModelRestRequest(restClient, projectId);
        modelView = modelRestRequest.getProductionProjectModelView(false);
        //assertEquals(modelView .toString(), sql);
    }

    @Test(dependsOnMethods = "editAndPublishModelTest" )
    public void runADDAndVerifyKPIOnClientTest() throws IOException {
        setUpDataMapping();
        setUpProcess();
        verifyKPIsOnClient();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws ParseException, JSONException, IOException, SQLException {
        log.info("Clean up...............");
        if (dataloadProcess != null) {
            domainRestClient.getProcessService().removeProcess(dataloadProcess);
        }
        if (processMaster != null) {
            domainRestClient.getProcessService().removeProcess(processMaster);
        }
        if (dataMappingProjectIdUtils != null) {
            dataMappingProjectIdUtils.deleteClientIdDataMapping(CLIENT_ID_1);
        }
        try {
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        } catch (Exception handleAlert) {
            browser.navigate().refresh();
            browser.switchTo().alert().accept();
            browser.switchTo().defaultContent();
            logoutAndLoginAs(canAccessGreyPage(browser), UserRoles.ADMIN);
        }
        initDatasourceManagementPage();
        if (dsMenu.isDataSourceExist(DATASOURCE_NAME)) {
            deleteDatasource(DATASOURCE_NAME);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME), "Datasource " + DATASOURCE_NAME + " should be deleted");
        }

        if (dsMenu.isDataSourceExist(DATASOURCE_NAME_OTHER)) {
            deleteDatasource(DATASOURCE_NAME_OTHER);
            assertFalse(dsMenu.isDataSourceExist(DATASOURCE_NAME_OTHER), "Datasource " + DATASOURCE_NAME_OTHER + " should be deleted");
        }
        redshiftUtils.dropTables(PERSON_TABLE, CAR_TABLE, PRE_CAR_TABLE);
        redshiftUtils.dropSchemaIfExists();
        redshiftUtilOther.dropTables(PRE_OTHER_TABLE);
        redshiftUtilOther.dropSchemaIfExists();

        redshiftUtils.closeRedshiftConnection();
        redshiftUtilOther.closeRedshiftConnection();
    }

    private void setUpProcessOnMaster() {
        // Create New Process Schedule On Master
        log.info("Setup Process...............");
        processMaster = new ScheduleUtils(domainRestClient).createDataDistributionProcess(domainRestClient.getProjectService()
                        .getProjectById(projectId), "PROCESS_NAME", dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
    }

    private void setUpProcess() {
        // Create New Process Schedule
        log.info("Setup Process...............");
        project = restClient.getProjectService().getProjectById(projectId);
        dataloadProcess = new ScheduleUtils(domainRestClient).createDataDistributionProcess(serviceProject, "PROCESS_NAME",
                dataSourceId, SEGMENT_ID, "att_lcm_default_data_product", "1");
        log.info("dataloadProcess : " + dataloadProcess);
        processUtils = new ProcessUtils(domainRestClient, dataloadProcess);
        log.info("processUtils : " + processUtils);
        JSONObject jsonDatasetPerson = processUtils.setModeDefaultDataset(PERSON_DATASET);
        JSONObject jsonDatasetCar = processUtils.setModeDefaultDataset(CAR_DATASET);
        log.info("jsonDataset Person : " + jsonDatasetPerson);
        log.info("jsonDataset Car : " + jsonDatasetCar);
        List<JSONObject> listJson = new ArrayList<JSONObject>();
        listJson.add(jsonDatasetPerson);
        listJson.add(jsonDatasetCar);
        String valueParam = processUtils.getDatasets(listJson);
        log.info("valueParam : " + valueParam);
        Parameters parameters = new Parameters().addParameter("GDC_DATALOAD_DATASETS", "[" + valueParam + "]")
                .addParameter("GDC_DATALOAD_SINGLE_RUN_LOAD_MODE", "DEFAULT");
        ProcessExecutionDetail detail;
        log.info("Execute Process...............");
        try {
            detail = processUtils.execute(parameters);
        } catch (Exception e) {
            throw new RuntimeException("Cannot execute process" + e.getMessage());
        }
        String executionLog = processUtils.getExecutionLog(detail.getLogUri(), projectId);
        log.info("executionLog : " + executionLog);
        assertThat(executionLog, containsString(
                String.format("Project=\"%s\", client_id=\"%s\"; datasets=[{dataset.%s, full}, {dataset.%s, full}]",
                        clientProjectId1, "clientcustom1", CAR_DATASET, PERSON_DATASET)));
    }

    private void setUpKPIs() {
        log.info("Setup KPIs...............");
        getMetricCreator().createSumAgeMetricAdvance();
        getMetricCreator().createSumPriceMetricAdvance();
        createInsightHasOnlyMetric(INSIGHT_NAME, ReportType.COLUMN_CHART, asList(METRIC_AGE));
        createInsightHasOnlyMetric(INSIGHT_NAME_2, ReportType.COLUMN_CHART, asList(METRIC_PRICE));
        IndigoDashboardsPage indigoDashboardsPage = initIndigoDashboardsPage(10).waitForWidgetsLoading();
        log.info("Delete cookies..............");
        browser.manage().deleteAllCookies();
        sleepTightInSeconds(3);
        indigoDashboardsPage.addDashboard().addInsight(INSIGHT_NAME).addInsight(INSIGHT_NAME_2).selectDateFilterByName("All time")
                .waitForWidgetsLoading().changeDashboardTitle(DASHBOARD_NAME).saveEditModeWithWidgets();
    }

    private void verifyKPIsOnClient() {
        IndigoDashboardsPage indigoDashboardsPageClient1 = initIndigoDashboardsPageSpecificProject(clientProjectId1).waitForWidgetsLoading();
        List<String> listValueClient1 = indigoDashboardsPageClient1.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .getChartReport().getDataLabels();
        log.info("listValue : " + listValueClient1);
        List<String> listValuePriceClient1 = indigoDashboardsPageClient1.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME_2)
                .getChartReport().getDataLabels();
        log.info("listValuePrice : " + listValuePriceClient1);
        assertEquals(listValueClient1, singletonList("$80.00"), "Unconnected filter make impact to insight");
        assertEquals(listValuePriceClient1, singletonList("$12,000.00"), "Unconnected filter make impact to insight");

        IndigoDashboardsPage indigoDashboardsPageClient2 = initIndigoDashboardsPageSpecificProject(clientProjectId2).waitForWidgetsLoading();
        assertTrue(indigoDashboardsPageClient2.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME)
                .isEmptyValue());
        assertTrue(indigoDashboardsPageClient2.waitForWidgetsLoading().getWidgetByHeadline(Insight.class, INSIGHT_NAME_2)
                 .isEmptyValue());
    }
    protected Metrics getMetricCreator() {
        return new Metrics(restClient, projectId);
    }

    private String createInsightHasOnlyMetric(String insightTitle, ReportType reportType, List<String> metricsTitle) {
        return indigoRestRequest.createInsight(new InsightMDConfiguration(insightTitle, reportType).setMeasureBucket(metricsTitle
                .stream().map(metric -> MeasureBucket.createSimpleMeasureBucket(getMetricByTitle(metric))).collect(toList())));
    }

    private void updateData() throws FileNotFoundException, SQLException {
        CsvFile personCsv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/person.csv"));
        File personFile = new File(personCsv.getFilePath());
        CsvFile carCSV = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/car.csv"));
        File carFile = new File(carCSV.getFilePath());

        CsvFile precarCsv = CsvFile.loadFile(
                getFilePathFromResource("/" + ResourceDirectory.UPLOAD_CSV + "/pre_car.csv"));
        File precarFile = new File(precarCsv.getFilePath());

        // Upload data to S3
        String fileNamePerson = "persons_" + generateHashString() + ".csv";
        String fileNameCar = "cars_" + generateHashString() + ".csv";
        String fileNamePreCar = "precars_" + generateHashString() + ".csv";


        S3Utils.uploadFile(personFile, "ATT_Redshift/" + fileNamePerson, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(PERSON_TABLE, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileNamePerson, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileNamePerson, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);

        S3Utils.uploadFile(carFile, "ATT_Redshift/" + fileNameCar, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(CAR_TABLE, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileNameCar, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileNameCar, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);

        S3Utils.uploadFile(precarFile, "ATT_Redshift/" + fileNamePreCar, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
        // Load data from S3 to Schema
        redshiftUtils.loadDataFromS3ToDatabase(PRE_CAR_TABLE, DEFAULT_S3_BUCKET_URI + "ATT_Redshift/" + fileNamePreCar, defaultS3AccessKey,
                defaultS3SecretKey);
        S3Utils.deleteFile("ATT_Redshift/" + fileNamePreCar, defaultS3AccessKey, defaultS3SecretKey, DEFAULT_S3_BUCKET_URI);
    }

    private void createNewDatasource() {
        initDatasourceManagementPage();
        //create new other datasource for verifying add datasource from many sources
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configuration = container.getConnectionConfiguration();
        ContentDatasourceContainer containerOther = contentWrapper.getContentDatasourceContainer();
        ConnectionConfiguration configurationOther = containerOther.getConnectionConfiguration();
        container.addConnectionTitle(DATASOURCE_NAME_OTHER);
        configuration.addRedshiftBasicInfo(DATASOURCE_URL,DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATABASE_NAME, DATASOURCE_PREFIX, OTHER_SCHEMA_NAME);
        configurationOther.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        dataSourceOtherId = container.getDataSourceId();
        log.info("---ID Datasource Other:" + dataSourceOtherId);
        // create main datasource
        dsMenu.selectRedshiftResource();
        contentWrapper.waitLoadingManagePage();
        container.addConnectionTitle(DATASOURCE_NAME);
        configuration.addRedshiftBasicInfo(DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD,
                DATABASE_NAME, DATASOURCE_PREFIX, SCHEMA_NAME );
        configuration.clickValidateButton();
        assertEquals(configuration.getValidateMessage(), "Connection succeeded");
        container.clickSavebutton();
        contentWrapper.waitLoadingManagePage();
        ConnectionDetail redshiftDetail = container.getConnectionDetail();
        dataSourceId = container.getDataSourceId();
        log.info("---ID Datasource Main:" + dataSourceId);
    }

    private String getCurrentDate() {
        return DateTime.now().toString("YYYY_MM_dd_HH_mm_ss");
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

    private void prepareTables() throws SQLException {
        redshiftUtils.executeCommandsForSpecificWarehouse();
        redshiftUtils.executeSql("CREATE TABLE person (id varchar(128) primary key, name varchar(255), age integer," +
                "city varchar(255), birthday date, clientid varchar(255), timestamp TIMESTAMP, deleted boolean)");
        redshiftUtils.executeSql("CREATE TABLE car(id varchar(128), color varchar(255), year date, price integer, " +
                "x__client_id varchar(255), x__timestamp TIMESTAMP , x__deleted boolean, owner varchar(128) " +
                "REFERENCES person(id))");
        redshiftUtils.executeSql("CREATE TABLE pre_car(cp__id varchar(128), a__color varchar(255), d__year date, f__price integer, " +
                "x__client_id varchar(255), x__timestamp TIMESTAMP , x__deleted boolean, r__person varchar(128) " +
                "REFERENCES person(id))");
        redshiftUtilOther.executeCommandsForSpecificWarehouse();
        redshiftUtilOther.executeSql("CREATE TABLE pre_other(cp__id varchar(128), a__color varchar(255), d__year date, f__priceother integer, " +
                "x__client_id varchar(255), x__timestamp TIMESTAMP , x__deleted boolean)");
    }

    private void addUserToSpecificProject(String email, UserRoles userRole, String projectId) throws IOException {
        final String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        final UserManagementRestRequest userManagementRestRequest = new UserManagementRestRequest(new RestClient(
                new RestClient.RestProfile(testParams.getHost(), domainUser, testParams.getPassword(), true)),
                projectId);
        userManagementRestRequest.addUserToProject(email, userRole);
    }

    private void setUpDataMapping() {
        List<Pair<String, String>> listClientIdMapping = new ArrayList<>();
        listClientIdMapping.add(Pair.of(CLIENT_ID_1, "clientcustom1"));
        dataMappingProjectIdUtils = new DataMappingUtils(testParams.getDomainUser(), asList(), listClientIdMapping, dataSourceId,
                testParams.getProjectId());
        dataMappingProjectIdUtils.createDataMapping();
    }

    private void initModelerPageContent(String pid) {
        //workaround modeler loading
        ldmPage = initLogicalDataModelPageByPID(pid);
        modeler = ldmPage.getDataContent().getModeler();
        sidebar = modeler.getSidebar();
        toolbar = modeler.getToolbar();
        modeler.getLayout().waitForLoading();
        canvas = modeler.getLayout().getCanvas();
    }

    private void deleteDatasource(String datasourceName) {
        log.info("Delete Datasource...............");
        dsMenu.selectDataSource(datasourceName);
        contentWrapper.waitLoadingManagePage();
        ContentDatasourceContainer container = contentWrapper.getContentDatasourceContainer();
        DatasourceHeading heading = container.getDatasourceHeading();
        DeleteDatasourceDialog deleteDialog = heading.clickDeleteButton();
        deleteDialog.clickDelete();
        contentWrapper.waitLoadingManagePage();
        dsMenu.waitForDatasourceNotVisible(datasourceName);
    }
}
