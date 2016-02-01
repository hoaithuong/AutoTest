package com.gooddata.qa.utils.http.indigo;

import static com.gooddata.md.Restriction.title;
import static com.gooddata.qa.utils.http.RestUtils.CREATE_AND_GET_OBJ_LINK;
import static com.gooddata.qa.utils.http.RestUtils.deleteObject;
import static com.gooddata.qa.utils.http.RestUtils.executeRequest;
import static com.gooddata.qa.utils.http.RestUtils.getJsonObject;
import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import com.gooddata.GoodData;
import com.gooddata.md.Dimension;
import com.gooddata.md.MetadataService;
import com.gooddata.md.Metric;
import com.gooddata.project.Project;
import com.gooddata.qa.graphene.entity.kpi.KpiMDConfiguration;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonDirection;
import com.gooddata.qa.graphene.fragments.indigo.dashboards.Kpi.ComparisonType;
import com.gooddata.qa.utils.http.RestApiClient;

public class IndigoRestUtils {

    private static final String ANALYTICAL_DASHBOARD_BODY;
    private static final String KPI_WIDGET_BODY;

    private static final String AMOUNT = "Amount";
    private static final String LOST = "Lost";
    private static final String NUM_OF_ACTIVITIES = "# of Activities";
    private static final String DATE_DIM_CREATED = "Date dimension (Created)";

    static {
        try {
            ANALYTICAL_DASHBOARD_BODY = new JSONObject() {{
                put("analyticalDashboard", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "title");
                    }});
                    put("content", new JSONObject() {{
                        put("widgets", new JSONArray());
                        put("filters", new JSONArray());
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    static {
        try {
            KPI_WIDGET_BODY = new JSONObject() {{
                put("kpi", new JSONObject() {{
                    put("meta", new JSONObject() {{
                        put("title", "${title}");
                    }});
                    put("content", new JSONObject() {{
                        put("comparisonType", "${comparisonType}");
                        put("metric", "${metric}");
                        put("dateDimension", "${dateDimension}");
                    }});
                }});
            }}.toString();
        } catch (JSONException e) {
            throw new IllegalStateException("There is an exception during json object initialization! ", e);
        }
    }

    public static List<String> getAnalyticalDashboards(final RestApiClient restApiClient, final String projectId)
            throws JSONException, IOException {
        final String analyticalDashboardsUri = "/gdc/md/" + projectId + "/query/analyticaldashboard";
        final JSONArray entries = getJsonObject(restApiClient, analyticalDashboardsUri)
                .getJSONObject("query")
                .getJSONArray("entries");
        final List<String> dashboardLinks = new ArrayList<>();
        for (int i = 0, n = entries.length(); i < n; i++) {
            dashboardLinks.add(entries.getJSONObject(i).getString("link"));
        }

        return dashboardLinks;
    }

    public static String createKpiWidget(final RestApiClient restApiClient, final String projectId,
            final KpiMDConfiguration kpiConfig) throws JSONException, IOException {
        String content = KPI_WIDGET_BODY
                .replace("${title}", kpiConfig.getTitle())
                .replace("${metric}", kpiConfig.getMetric())
                .replace("${dateDimension}", kpiConfig.getDateDimension())
                .replace("${comparisonType}", kpiConfig.getComparisonType().getJsonKey());

        if (kpiConfig.hasComparison()) {
            final JSONObject contentJson = new JSONObject(content);

            contentJson.getJSONObject("kpi")
                .getJSONObject("content")
                .put("comparisonDirection", kpiConfig.getComparisonDirection().toString());

            content = contentJson.toString();
        }

        if (kpiConfig.hasDrillTo()) {
            final JSONObject contentJson = new JSONObject(content);

            contentJson.getJSONObject("kpi")
                .getJSONObject("content")
                .put("drillTo", new JSONObject() {{
                    put("projectDashboard", kpiConfig.getDrillToDashboard());
                    put("projectDashboardTab", kpiConfig.getDrillToDashboardTab());
                }});

            content = contentJson.toString();
        }

        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("kpi")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    public static void addKpiWidgetToAnalyticalDashboard(final RestApiClient restApiClient, final String projectId,
            final String dashboardUri, final String widgetUri) throws JSONException, IOException {
        final JSONObject dashboard = getJsonObject(restApiClient, dashboardUri);
        dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .getJSONArray("widgets")
            .put(widgetUri);

        executeRequest(restApiClient, restApiClient.newPutMethod(dashboardUri, dashboard.toString()), HttpStatus.OK);
    }

    public static void deleteKpiWidgetFromAnalyticalDashboard(final RestApiClient restApiClient, final String projectId,
            final String dashboardUri, final String widgetUri) throws JSONException, IOException {
        final JSONObject dashboard = getJsonObject(restApiClient, dashboardUri);
        final JSONArray widgets = dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .getJSONArray("widgets");
        final JSONArray newWidgets = new JSONArray();
        for (int i = 0, n = widgets.length(); i < n; i++) {
            final String uri = widgets.getString(i);
            if (!widgetUri.equals(uri)) {
                newWidgets.put(uri);
            }
        }
        dashboard.getJSONObject("analyticalDashboard")
            .getJSONObject("content")
            .put("widgets", newWidgets);

        executeRequest(restApiClient, restApiClient.newPutMethod(dashboardUri, dashboard.toString()), HttpStatus.OK);
    }

    public static String createAnalyticalDashboard(final RestApiClient restApiClient, final String projectId,
            final Collection<String> widgetUris) throws JSONException, IOException {

        // TODO: consider better with .put() and have clever template
        final String widgets = new JSONArray(widgetUris).toString();
        final String content = ANALYTICAL_DASHBOARD_BODY.replace("\"widgets\":[]", "\"widgets\":" + widgets);

        return getJsonObject(restApiClient,
                restApiClient.newPostMethod(format(CREATE_AND_GET_OBJ_LINK, projectId), content))
                    .getJSONObject("analyticalDashboard")
                    .getJSONObject("meta")
                    .getString("uri");
    }

    public static void prepareAnalyticalDashboardTemplate(final RestApiClient restApiClient,
            final GoodData goodData, final String projectId) throws JSONException, IOException {
        // delete all dashboards, if some exist
        for (String dashboardLink: getAnalyticalDashboards(restApiClient, projectId)) {
            deleteObject(restApiClient, dashboardLink);
        }

        final Project project = goodData.getProjectService().getProjectById(projectId);
        final MetadataService service = goodData.getMetadataService();
        final String amountMetricUri = service.getObjUri(project, Metric.class, title(AMOUNT));
        final String lostMetricUri = service.getObjUri(project, Metric.class, title(LOST));
        final String numOfActivitiesUri = service.getObjUri(project, Metric.class, title(NUM_OF_ACTIVITIES));
        final String dateDimensionUri = getDateDimensionCreatedUri(goodData, projectId);

        final String amountWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title(AMOUNT)
                .metric(amountMetricUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .build());
        final String lostWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title(LOST)
                .metric(lostMetricUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(Kpi.ComparisonType.LAST_YEAR)
                .comparisonDirection(Kpi.ComparisonDirection.BAD)
                .build());
        final String numOfActivitiesWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title(NUM_OF_ACTIVITIES)
                .metric(numOfActivitiesUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(Kpi.ComparisonType.PREVIOUS_PERIOD)
                .comparisonDirection(Kpi.ComparisonDirection.GOOD)
                .build());
        final String drillToWidget = createKpiWidget(restApiClient, projectId, new KpiMDConfiguration.Builder()
                .title("DrillTo")
                .metric(amountMetricUri)
                .dateDimension(dateDimensionUri)
                .comparisonType(ComparisonType.NO_COMPARISON)
                .comparisonDirection(ComparisonDirection.NONE)
                .drillToDashboard("/gdc/md/p8aqohkx4htbrau1wpk6k68crltlojig/obj/916")
                .drillToDashboardTab("adzD7xEmdhTx")
                .build());

        final List<String> widgetUris = asList(amountWidget, lostWidget, numOfActivitiesWidget, drillToWidget);
        createAnalyticalDashboard(restApiClient, projectId, widgetUris);
    }

    public static String getDateDimensionCreatedUri(final GoodData goodData, final String projectId) {
        return goodData.getMetadataService()
            .getObjUri(goodData.getProjectService().getProjectById(projectId),
                    Dimension.class,
                    title(DATE_DIM_CREATED));
    }
}
