package com.gooddata.qa.utils.http.project;

import com.gooddata.featureflag.FeatureFlagService;
import com.gooddata.featureflag.ProjectFeatureFlag;
import com.gooddata.project.Project;
import com.gooddata.project.ProjectService;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestRequest;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import com.jayway.awaitility.pollinterval.IterativePollInterval;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static com.gooddata.qa.utils.http.RestRequest.initPutRequest;
import static java.lang.String.format;

/**
 * REST request for project task
 */
public final class ProjectRestRequest extends CommonRestRequest {

    private static final Logger log = Logger.getLogger(ProjectRestRequest.class.getName());
    private static final String PROJECT_LINK = "/gdc/projects/%s";
    private static final String PROJECT_CONFIGURATION_LINK = "/gdc/projects/%s/config";

    public ProjectRestRequest(final RestClient restClient, final String projectId) {
        super(restClient, projectId);
    }

    private static final Supplier<String> UPDATE_PROJECT_TITLE_BODY = () -> {
        try {
            return new JSONObject() {{
                put("project", new JSONObject() {{
                    put("content", new JSONObject() {{
                        put("guidedNavigation", "1");
                        put("environment", "${environment}");
                    }});
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    };

    public void updateProjectTitle(final String newProjectTitle) {
        final Project project = getProject();
        final String uri = String.format(PROJECT_LINK, project.getId());
        final String content = UPDATE_PROJECT_TITLE_BODY.get()
                .replace("${title}", newProjectTitle)
                .replace("${environment}", project.getEnvironment());

        executeRequest(RestRequest.initPostRequest(uri, content), HttpStatus.NO_CONTENT);
    }

    /**
     * Delete project
     */
    public void deleteProject() {
        final ProjectService service = restClient.getProjectService();
        service.removeProject(service.getProjectById(projectId));
    }

    /**
     * Turn on/off project feature flag
     *
     * @param featureFlag
     * @param enabled
     */
    public void setFeatureFlagInProject(final ProjectFeatureFlags featureFlag, final boolean enabled) {
        final FeatureFlagService service = restClient.getFeatureFlagService();
        service.createProjectFeatureFlag(restClient.getProjectService().getProjectById(projectId),
                new ProjectFeatureFlag(featureFlag.getFlagName(), enabled));
    }

    /**
     * Turn on/off project feature flag and check the feature flag's status before exit
     * Because the cache in C3 is 10-20s so if checking the feature flag is not correct, we should wait for sometime
     * Waiting time is 10s++ for each loop
     *
     * @param featureFlag
     * @param enabled
     */
    public void setFeatureFlagInProjectAndCheckResult(final ProjectFeatureFlags featureFlag, final boolean enabled) {
        log.info(String.format("Set feature flag %s for project %s %s", featureFlag.getFlagName(), projectId,
                enabled));
        final FeatureFlagService service = restClient.getFeatureFlagService();
        final Project project = restClient.getProjectService().getProjectById(projectId);

        service.createProjectFeatureFlag(project, new ProjectFeatureFlag(featureFlag.getFlagName(), enabled));

        Awaitility.await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(IterativePollInterval.iterative(duration -> duration.plus(10, TimeUnit.SECONDS))
                        .startDuration(Duration.TEN_SECONDS))
                .conditionEvaluationListener(condition -> service.createProjectFeatureFlag(project,
                        new ProjectFeatureFlag(featureFlag.getFlagName(), enabled)))
                .until(() -> service.getProjectFeatureFlag(project, featureFlag.getFlagName()).isEnabled() == enabled);
    }

    public void updateProjectConfiguration(final String key, final String value) throws IOException {
        String url = format(PROJECT_CONFIGURATION_LINK, projectId) + "/" + key;
        JSONObject settingItem = getJsonObject(url);
        settingItem.getJSONObject("settingItem").put("value", value);
        executeRequest(initPutRequest(url, settingItem.toString()), HttpStatus.NO_CONTENT);
    }

    public int setXaeVersionProject(int xaeVersion) throws IOException {
        updateProjectConfiguration("xae_version", String.valueOf(xaeVersion));
        return xaeVersion;
    }

    public int getXaeVersionProject() throws IOException {
        int xaeVersionProject = 3;
        JSONArray items = this.getJsonObject(format(PROJECT_CONFIGURATION_LINK, projectId))
                .getJSONObject("settings").getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject jsonObject = items.getJSONObject(i).getJSONObject("settingItem");
            if ("xae_version".equals(jsonObject.getString("key"))) {
                xaeVersionProject = Integer.valueOf(jsonObject.getString("value"));
            }
        }
        log.info("Current XAE version of Project is: " + xaeVersionProject);
        return xaeVersionProject;
    }
}
