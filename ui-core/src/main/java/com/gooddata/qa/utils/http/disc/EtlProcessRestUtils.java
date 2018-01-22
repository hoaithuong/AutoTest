package com.gooddata.qa.utils.http.disc;

import com.gooddata.qa.graphene.fragments.disc.process.DeployProcessForm;
import com.gooddata.qa.utils.http.RestApiClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;

/**
 * Copyright (C) 2007-2018, GoodData(R) Corporation. All rights reserved.
 */
public class EtlProcessRestUtils {

    public static final String ETL_PROCESS_TYPE = "ETL";
    public static final String ETL_PROCESS_TYPE_LABEL = "DATA LOADING";

    private static final String COMPONENT_VERSION_URI = "/gdc/projects/%s/internal/pipeline/components";
    private static final String COMPONENT_PROCESS_CREATE_URI = "/gdc/projects/%s/dataload/processes";
    private static final String COMPONENT_PROCESS_SCHEDULE_CREATE_URI = "/gdc/projects/%s/schedules";

    /**
     * Create ETL process in specified project.
     *
     * @param restApiClient rest API client
     * @param projectId id of project where process will be created
     * @param processName name of process will be created
     * @param processType type of process will be created
     * @param s3ConfigurationPath configuration path will be used when run process
     * @param s3AccessKey access key will be used when run process
     * @param s3SecretKey secret key will be used when run process
     */
    public static void createEtlProcess(RestApiClient restApiClient,
                                    String projectId,
                                    String processName,
                                    DeployProcessForm.ProcessType processType,
                                    String s3ConfigurationPath,
                                    String s3AccessKey,
                                    String s3SecretKey) {
        String version = getEtlProcessTypeVersion(restApiClient, projectId, processType);
        if (version == null) {
            throw new IllegalStateException(format("Does not support process type='%s'", processType));
        }
        String jsonStr = buildEtlProcessJson(processName,
                processType,
                version,
                s3ConfigurationPath,
                s3AccessKey,
                s3SecretKey);

        executeRequest(restApiClient,
                restApiClient.newPostMethod(format(COMPONENT_PROCESS_CREATE_URI, projectId), jsonStr),
                HttpStatus.CREATED);
    }

    /**
     * Create schedule for ETL process in specified project.
     *
     * @param restApiClient rest API client
     * @param projectId id of project where schedule will be created
     * @param processId id of process that schedule will be created in
     * @param scheduleName name of schedule will be created
     * @param cron cron of schedule will be created
     */
    public static void createEtlProcessSchedule(RestApiClient restApiClient,
                                                String projectId,
                                                String processId,
                                                String scheduleName, String cron) {
        String json = buildEtlProcessScheduleJson(processId, scheduleName, cron);
        executeRequest(restApiClient,
                restApiClient.newPostMethod(format(COMPONENT_PROCESS_SCHEDULE_CREATE_URI, projectId), json),
                HttpStatus.CREATED);
    }

    private static String getEtlProcessTypeVersion(RestApiClient restApiClient,
                                                   String projectId,
                                                   DeployProcessForm.ProcessType processType) {
        String componentVersionUri = format(COMPONENT_VERSION_URI, projectId);
        try {
            final JSONObject json = getJsonObject(restApiClient, restApiClient.newGetMethod(componentVersionUri));
            JSONArray items = json.getJSONObject("pipelineComponents").getJSONArray("items");
            for(int i=0; i<items.length(); i++) {
                JSONObject item = items.getJSONObject(i).getJSONObject("pipelineComponent");
                if (processType.getValue().equals(item.getString("name"))) {
                    String version = item.getString("version");
                    // Return only major version
                    return version.substring(0, version.indexOf("."));
                }
            }
        } catch (JSONException | IOException e) {
            throw new IllegalStateException("Error during get ETL component version", e);
        }
        return null;
    }

    private static String buildEtlProcessJson(String processName,
                                         DeployProcessForm.ProcessType processType,
                                         String version,
                                         String s3ConfigurationPath,
                                         String s3AccessKey,
                                         String s3SecretKey) {
        String jsonStr;
        try {
            jsonStr = new JSONObject() {{
                put("process", new JSONObject() {{
                    put("type", ETL_PROCESS_TYPE);
                    put("name", processName);
                    put("component", new JSONObject() {{
                        put("name", processType.getValue());
                        put("version", version);
                        put("configLocation", new JSONObject() {{
                            put("s3", new JSONObject() {{
                                put("path", s3ConfigurationPath);
                                put("accessKey", s3AccessKey);
                                put("secretKey", s3SecretKey);
                            }});
                        }});
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during build create component process JSON", e);
        }
        return jsonStr;
    }

    private static String buildEtlProcessScheduleJson(String processId, String scheduleName, String cron) {
        String jsonStr;
        try {
            jsonStr = new JSONObject() {{
                put("schedule", new JSONObject() {{
                    put("name", scheduleName);
                    put("type", "MSETL");
                    put("cron", cron);
                    put("timezone", "UTC");
                    put("params", new JSONObject() {{
                        put("PROCESS_ID", processId);

                    }});
                }});
            }}.toString();

        } catch (JSONException e) {
            throw new IllegalStateException(
                    "There is an exception during build create component process schedule JSON",
                    e);
        }
        return jsonStr;
    }

}