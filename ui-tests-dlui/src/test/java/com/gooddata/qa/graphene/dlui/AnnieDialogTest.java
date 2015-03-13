package com.gooddata.qa.graphene.dlui;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

import com.gooddata.qa.graphene.entity.dlui.ADSInstance;
import com.gooddata.qa.graphene.entity.dlui.DataSource;
import com.gooddata.qa.graphene.entity.dlui.Dataset;
import com.gooddata.qa.graphene.entity.dlui.Field;
import com.gooddata.qa.graphene.entity.dlui.Field.FieldTypes;
import com.gooddata.qa.graphene.entity.dlui.ProcessInfo;
import com.gooddata.qa.graphene.enums.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.dlui.ADSTables;
import com.gooddata.qa.graphene.enums.dlui.AdditionalDatasets;
import com.gooddata.qa.utils.http.RestUtils;
import com.google.common.collect.Lists;

public class AnnieDialogTest extends AbstractDLUITest {

    private static final String INITIAL_LDM_MAQL_FILE = "create-ldm.txt";

    private static final String DEFAULT_DATA_SOURCE_NAME = "Unknown data source";

    private static final String ADS_URL =
            "jdbc:gdc:datawarehouse://${host}/gdc/datawarehouse/instances/${adsId}";

    private ProcessInfo cloudconnectProcess;
    private ADSInstance adsInstance;

    @BeforeClass
    public void initProperties() {
        dluiZipFilePath =
                testParams.loadProperty("dluiZipFilePath") + testParams.getFolderSeparator();
        maqlFilePath = testParams.loadProperty("maqlFilePath") + testParams.getFolderSeparator();
        sqlFilePath = testParams.loadProperty("sqlFilePath") + testParams.getFolderSeparator();
        projectTitle = "Dlui-annie-dialog-test";
    }

    @Test(dependsOnMethods = "createProject")
    public void initialData() {
        try {
            RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                    ProjectFeatureFlags.ENABLE_DATA_EXPLORER);
        } catch (JSONException e) {
            throw new IllegalStateException("There is a problem when enable data explorer! ", e);
        }

        createModelForGDProject(maqlFilePath + INITIAL_LDM_MAQL_FILE);

        adsInstance =
                new ADSInstance().setName("ADS Instance for DLUI test").setAuthorizationToken(
                        testParams.loadProperty("dss.authorizationToken"));
        createADSInstance(adsInstance);

        setDefaultSchemaForOutputStage(testParams.getProjectId(), adsInstance.getId());
        assertTrue(dataloadProcessIsCreated(), "DATALOAD process is not created!");

        cloudconnectProcess =
                new ProcessInfo().setProjectId(testParams.getProjectId())
                        .setProcessName("Initial Data for ADS Instance").setProcessType("GRAPH");
        createCloudConnectProcess(cloudconnectProcess);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkEmptyStateInAnnieDialog() {
        createUpdateADSTable(ADSTables.WITHOUT_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkEmptyAnnieDialog();
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalFields() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalAttributes() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ATTRIBUTE);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalFacts() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.FACT);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAvailableAdditionalLabelHyperlink() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.LABLE_HYPERLINK);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkAdditionalDateField() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_DATE);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkEmptyStateWithDateFilter() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        openAnnieDialog();
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.DATE);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void checkSearchAllFields() {
        createUpdateADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        Field field = new Field().setNameAndType("Position", FieldTypes.ATTRIBUTE);
        Dataset dataset =
                new Dataset().setName(AdditionalDatasets.PERSON_WITH_NEW_FIELDS.getName())
                        .setFields(field);
        DataSource dataSource =
                new DataSource().setName(DEFAULT_DATA_SOURCE_NAME).setDatasets(dataset);

        openAnnieDialog();
        annieUIDialog.enterSearchKey("Pos");
        annieUIDialog.checkAvailableAdditionalFields(dataSource, FieldTypes.ALL);
    }

    @Test(dependsOnMethods = "initialData", groups = "annieDialogTest")
    public void selectAndDeselectFields() {
        DataSource dataSource = prepareADSTable(ADSTables.WITH_ADDITIONAL_FIELDS);

        Dataset selectedDataset1 = new Dataset(AdditionalDatasets.PERSON_WITH_NEW_FIELDS);
        Field selectedField1 = new Field("Position", FieldTypes.ATTRIBUTE);

        Dataset selectedDataset2 = new Dataset(AdditionalDatasets.OPPORTUNITY_WITH_NEW_FIELDS);
        Field selectedField2 = new Field("Title2", FieldTypes.ATTRIBUTE);

        openAnnieDialog();
        annieUIDialog.selectFields(dataSource, selectedDataset2, selectedField2);
        annieUIDialog.selectFields(dataSource, selectedDataset1, selectedField1);

        annieUIDialog.checkSelectionArea(Lists.newArrayList(selectedField2, selectedField1));

        annieUIDialog.deselectFieldsInSelectionArea(selectedField2);
        annieUIDialog.deselectFields(dataSource, selectedDataset1, selectedField1);
    }

    @Test(dependsOnGroups = "annieDialogTest", alwaysRun = true)
    public void cleanUp() {
        deleteADSInstance(adsInstance);
    }

    private DataSource prepareADSTable(ADSTables adsTable) {
        createUpdateADSTable(adsTable);
        DataSource dataSource = new DataSource(adsTable);

        return dataSource;
    }

    private void createUpdateADSTable(ADSTables adsTable) {
        executeProcess(
                cloudconnectProcess.getProcessId(),
                ADS_URL.replace("${host}", testParams.getHost()).replace("${adsId}",
                        adsInstance.getId()), sqlFilePath + adsTable.getCreateTableSqlFile(),
                sqlFilePath + adsTable.getCopyTableSqlFile());
    }
}
