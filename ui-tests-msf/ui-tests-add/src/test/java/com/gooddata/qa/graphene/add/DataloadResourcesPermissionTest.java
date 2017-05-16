package com.gooddata.qa.graphene.add;

import static com.gooddata.qa.utils.ads.AdsHelper.OUTPUT_STAGE_METADATA_URI;
import static com.gooddata.qa.utils.ads.AdsHelper.OUTPUT_STAGE_URI;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static com.gooddata.qa.utils.http.model.ModelRestUtils.getProductionProjectModelView;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractDataloadProcessTest;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.RestApiClient;

public class DataloadResourcesPermissionTest extends AbstractDataloadProcessTest {

    private static final String INTERNAL_OUTPUT_STAGE_URI = "/gdc/dataload/internal/projects/%s/outputStage/";
    private static final String OUTPUT_STAGE_MAPPING_URI = INTERNAL_OUTPUT_STAGE_URI + "mapping";
    private static final String OUTPUT_STAGE_MODEL_URI = INTERNAL_OUTPUT_STAGE_URI + "model";

    private RestApiClient editorRestApiClient;
    private RestApiClient viewerRestApiClient;

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);

        editorRestApiClient = getRestApiClient(testParams.getEditorUser(), testParams.getPassword());
        viewerRestApiClient = getRestApiClient(testParams.getViewerUser(), testParams.getPassword());
    }

    @DataProvider(name = "accessDataloadResourceProvider")
    public Object[][] getAccessDataloadResourceProvider() throws ParseException, JSONException, IOException {
        String otherUser = createDynamicUserFrom(testParams.getUser());

        return new Object[][] {
            {viewerRestApiClient},
            {getRestApiClient(otherUser, testParams.getPassword())}
        };
    }

    @Test(dependsOnGroups = {"initDataload"}, dataProvider = "accessDataloadResourceProvider")
    public void cannotAccessDataloadResource(RestApiClient restApiClient) throws IOException, JSONException {
        final String errorMessage = "User does not have required permission ('canEnrichData')";

        JSONObject outputStageMapping = getJsonObject(restApiClient, format(OUTPUT_STAGE_MAPPING_URI,
                testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputStageMapping.getJSONObject("error").getString("message"), errorMessage);

        JSONObject outputStageModel = getJsonObject(restApiClient, format(OUTPUT_STAGE_MODEL_URI,
                testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputStageModel.getJSONObject("error").getString("message"), errorMessage);

        JSONObject outputState = getJsonObject(restApiClient, format(AdsHelper.OUTPUT_STAGE_URI,
                testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputState.getJSONObject("error").getString("message"), errorMessage);

        JSONObject outputStageMetadata = getJsonObject(restApiClient,
                format(AdsHelper.OUTPUT_STAGE_METADATA_URI, testParams.getProjectId()), HttpStatus.FORBIDDEN);
        assertEquals(outputStageMetadata.getJSONObject("error").getString("message"), errorMessage);
    }

    @Test(dependsOnGroups = {"initDataload"})
    public void editorCanAccessDataloadResources() throws ParseException, JSONException, IOException {
        JSONObject outputStageMapping = getJsonObject(editorRestApiClient, format(OUTPUT_STAGE_MAPPING_URI,
                testParams.getProjectId()));
        assertTrue(outputStageMapping.has("mappingDefinition"), "Output stage mapping show wrong!");

        JSONObject outputStageModel = getJsonObject(editorRestApiClient,
                format(OUTPUT_STAGE_MODEL_URI, testParams.getProjectId()));
        assertTrue(outputStageModel.has("outputStageModel"), "Output stage model show wrong!");

        JSONObject outputState = getJsonObject(editorRestApiClient, format(OUTPUT_STAGE_URI,
                testParams.getProjectId()));
        assertTrue(outputState.has("outputStage"), "Output stage show wrong!");

        JSONObject outputStageMetadata = getJsonObject(editorRestApiClient,
                format(OUTPUT_STAGE_METADATA_URI, testParams.getProjectId()));
        assertTrue(outputStageMetadata.has("outputStageMetadata"), "Output stage metadata show wrong!");
    }

    @Test(dependsOnGroups = {"initDataload"})
    public void canAccessToProjectModelView() throws ParseException, JSONException, IOException {
        JSONObject adminModelView = getProductionProjectModelView(getRestApiClient(),
                testParams.getProjectId(), false);
        assertTrue(adminModelView.has("projectModelView"), "Model view structure shows wrong!");

        JSONObject editorModelView = getProductionProjectModelView(editorRestApiClient,
                testParams.getProjectId(), false);
        assertTrue(editorModelView.has("projectModelView"), "Model view structure shows wrong!");

        JSONObject viewerModelView = getProductionProjectModelView(viewerRestApiClient,
                testParams.getProjectId(), false);
        assertTrue(viewerModelView.has("projectModelView"), "Model view structure shows wrong!");
    }
}
