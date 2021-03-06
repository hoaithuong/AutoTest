package com.gooddata.qa.utils.lcm;

import com.gooddata.sdk.model.dataload.processes.DataloadProcess;
import com.gooddata.sdk.model.dataload.processes.ProcessExecution;
import com.gooddata.sdk.model.dataload.processes.ProcessExecutionDetail;
import com.gooddata.sdk.service.dataload.processes.ProcessService;
import com.gooddata.sdk.model.dataload.processes.ProcessType;
import com.gooddata.sdk.model.project.Project;
import com.gooddata.sdk.service.project.ProjectService;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile;
import com.gooddata.qa.graphene.entity.csvuploader.CsvFile.Column;
import com.gooddata.qa.graphene.entity.disc.Parameters;
import com.gooddata.qa.graphene.enums.process.Parameter;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.CommonRestRequest;
import com.gooddata.qa.utils.http.RestClient;
import com.gooddata.qa.utils.http.RestClient.RestProfile;
import com.gooddata.sdk.model.warehouse.Warehouse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import static java.util.stream.Collectors.joining;

/**
 * This class present a lcm service project, it means a project contains ruby processes to run bricks
 * This project has 4 dataload process, first process to load data to ads instance associated to project and
 * 3 process for running release brick, provision brick and rollout brick
 */
public final class LCMServiceProject {

    private static final Logger log = Logger.getLogger(LCMServiceProject.class.getName());
    private static final String UPDATE_ADS_TABLE_EXECUTABLE = "DLUI/graph/CreateAndCopyDataToADS.grf";
    //List of segments which used to be released by this service project, this intend for cleanup
    private final Set<String> associatedSegments = new HashSet<>();
    //id of this service project which contains 4 dataload process
    private String projectId;
    //this lcm project contains 3 ruby process which execute brick to release, provision and rollout
    private LcmProcess releaseProcess;
    private LcmProcess provisionProcess;
    private LcmProcess rolloutProcess;
    //ads associated to this project, used as lcm repository
    private Warehouse ads;
    //process to load data to associated ads
    private DataloadProcess updateAdsTableProcess;
    //default params to run ads data load process
    private Supplier<Parameters> defaultAdsParameters;

    private RestClient restClient;
    private boolean useK8sExecutor;

    /**
     * Create a service project that contains 3 ruby-run process: release, provision, rollout and one ads dataload process
     *
     * @param testParameters
     * @param useK8sExecutor create built-in LCM dataload processess if true otherwise create generic RUBY brick processess
     * @return LCMServiceProject
     */
    public static LCMServiceProject newWorkFlow(final TestParameters testParameters, boolean useK8sExecutor) {
        return new LCMServiceProject(testParameters, useK8sExecutor);
    }

    private LCMServiceProject() {
        //prevent default constructor
    }

    private LCMServiceProject(final TestParameters testParameters, boolean useK8sExecutor) {
        try {
            this.useK8sExecutor = useK8sExecutor;
            this.restClient = createDomainRestClient(testParameters);
            this.projectId = createNewEmptyProject(testParameters, "ATT Service Project");
            log.info("--->Created service project:" + this.projectId);
            log.info("--->useK8SExecutor:" + useK8sExecutor);

            initAdsInstance(testParameters);
            log.info("--->Created ads instance has uri:" + ads.getUri());
            createLCMProcesses(testParameters);
        } catch (Exception e) {
            if(projectId != null ) {
                deleteProject(projectId);
            }
            throw new RuntimeException("Cannot init lcm project", e);
        }
    }

    /**
     * Delete devs/masters project, clientIds, segments which have involved in this lcm model
     */
    public void cleanUp(final String domain) {
        try {
            System.out.println("Removing ads instance");
            AdsHelper adsHelper = new AdsHelper(restClient, this.projectId);
            adsHelper.removeAds(ads);
            System.out.println("Removing associated segments");
            LcmRestUtils.deleteSegments(this.restClient, domain, this.associatedSegments);
            System.out.println("Removing service project");
            deleteProject(this.projectId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getServiceProjectId() {
        return projectId;
    }

    public String getExecutionLog(final String logUri) {
        try {
            return new CommonRestRequest(restClient, projectId)
                    .getResource(logUri, HttpStatus.OK);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Run release process
     *
     * @param segments
     */
    public ProcessExecutionDetail release(final JSONArray segments) {
        addReleasedSegmentsToAssociatedList(segments);
        Parameters releaseTemplate = getReleaseParamsTemplate();
        JSONObject encodedParamObj = new JSONObject(releaseTemplate.getParameters().get("gd_encoded_params"));
        encodedParamObj.put("segments", segments);
        String encodesString = encodedParamObj.toString();
        log.info(encodesString);
        releaseTemplate.getParameters().put("gd_encoded_params", encodesString);
        return releaseProcess.execute(releaseTemplate);
    }

    /**
     * Run provision project
     * @param segments segments filter
     * @param inputSource
     */
    public ProcessExecutionDetail provision(final JSONArray segments, final JSONObject inputSource) {
        Parameters provisionTemplate = getProvisionParamsTemplate();
        JSONObject encodedParamObj = new JSONObject(provisionTemplate.getParameters().get("gd_encoded_params"));
        encodedParamObj.put("SEGMENTS_FILTER", segments);
        encodedParamObj.put("input_source", inputSource);
        String encodesString = encodedParamObj.toString();
        log.info(encodesString);
        provisionTemplate.getParameters().put("gd_encoded_params", encodesString);

        return provisionProcess.execute(provisionTemplate);
    }

    /**
     * Run rollout process
     *
     * @param segments
     */
    public ProcessExecutionDetail rollout(final JSONArray segments) {
        Parameters rolloutTemplate = getRolloutParamsTemplate();
        JSONObject encodedParamObj = new JSONObject(rolloutTemplate.getParameters().get("gd_encoded_params"));
        encodedParamObj.put("SEGMENTS_FILTER", segments);
        String encodesString = encodedParamObj.toString();
        log.info(encodesString);
        rolloutTemplate.getParameters().put("gd_encoded_params", encodesString);

        return rolloutProcess.execute(rolloutTemplate);
    }

    /**
     * @param segmentId
     * @param clientId
     * @param clientProjectIds
     * @return
     */
    public JSONObject createProvisionDatasource(final String segmentId, final String clientId, String... clientProjectIds) {
        CsvFile csvFile = new CsvFile("clients")
                .columns(new Column("segment_id"), new Column("client_id"), new Column("project_id"));
        Arrays.stream(clientProjectIds).forEach(
                clientProjectId -> csvFile.rows(segmentId, clientId, String.format("/gdc/projects/%s", clientProjectId)));

        createAdsTableFromFile(csvFile);

        return new JSONObject() {{
            put("type", "ads");
            put("query", "SELECT segment_id, client_id, project_id FROM clients;");
        }};
    }

    public JSONObject createProvisionDatasource(final String segmentId, final Map<String,String> clients) {
        CsvFile csvFile = new CsvFile("clients")
        .columns(new Column("segment_id"), new Column("client_id"), new Column("project_id"));
        for(String clientId : clients.keySet()) {
            csvFile.rows(segmentId, clientId, String.format("/gdc/projects/%s", clients.get(clientId)));
        }
        createAdsTableFromFile(csvFile);
        return new JSONObject() {{
        put("type", "ads");
        put("query", "SELECT segment_id, client_id, project_id FROM clients;");
        }};
    }


    public Parameters getReleaseParamsTemplate() {
        return releaseProcess.getDefaultParameters();
    }

    public Parameters getProvisionParamsTemplate() {
        return provisionProcess.getDefaultParameters();
    }

    public Parameters getRolloutParamsTemplate() {
        return rolloutProcess.getDefaultParameters();
    }

    /**
     * Create an ads table which associacated with LCM service project, ruby process use this table as input to run
     * provision
     *
     * @param file
     * @return this
     */
    private LCMServiceProject createAdsTableFromFile(final CsvFile file) {
        final String sql = buildSql(file);
        log.info(sql);
        executeProcess(updateAdsTableProcess, UPDATE_ADS_TABLE_EXECUTABLE,
                defaultAdsParameters.get().addParameter(Parameter.SQL_QUERY, sql));
        return this;
    }

    /**
     * Create ads instance where become a repository for ruby process
     *
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    private LCMServiceProject initAdsInstance(final TestParameters testParameters)
            throws IOException {
        AdsHelper adsHelper = new AdsHelper(createDomainRestClient(testParameters), this.projectId);
        ads = adsHelper.createAds("att-ads-" + generateHashString(),
                testParameters.loadProperty("dss.authorizationToken"));

        adsHelper.associateAdsWithProject(ads, projectId, "", "");
        updateAdsTableProcess = getProcessService().createProcess(getProject(),
                new DataloadProcess(generateProcessName(), ProcessType.GRAPH),
                getResourceAsFile("/zip-file/adsTable.zip"));

        defaultAdsParameters = () -> new Parameters()
                .addParameter(Parameter.ADS_URL, ads.getConnectionUrl())
                .addParameter(Parameter.ADS_USER, testParameters.getDomainUser())
                .addSecureParameter(Parameter.ADS_PASSWORD, testParameters.getPassword());
        return this;
    }

    private LCMServiceProject createLCMProcesses(final TestParameters testParameters) {
        this.releaseProcess = LcmProcess.ofRelease(testParameters, ads.getConnectionUrl(), this.projectId, useK8sExecutor);
        this.provisionProcess = LcmProcess.ofProvision(testParameters, ads.getConnectionUrl(), this.projectId, useK8sExecutor);
        this.rolloutProcess = LcmProcess.ofRollout(testParameters, ads.getConnectionUrl(), this.projectId, useK8sExecutor);
        return this;
    }

    /**
     * Get file instance of resource, use this to create a input File of
     * a dataload process to use for creating ads instance
     *
     * @param resourcePath path of adsTable
     * @return file
     */
    private File getResourceAsFile(final String resourcePath) {
        try {
            InputStream in = getClass().getResourceAsStream(resourcePath);
            if (in == null) {
                throw new RuntimeException("Cannot read resource: " + resourcePath);
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildSql(final CsvFile file) {
        return new StringBuilder()
                .append(createTable(file))
                .append(insertData(file))
                .toString();
    }

    private String createTable(final CsvFile file) {
        return new StringBuilder()
                .append("DROP TABLE IF EXISTS ${table};")
                .append("CREATE TABLE ${table}(")
                .append(file.getColumnNames().stream().map(name -> name.replace(name, name + " VARCHAR(128)"))
                        .collect(joining(", ")))
                .append(");")
                .toString()
                .replace("${table}", file.getName());
    }

    private String insertData(final CsvFile file) {
        return file.getDataRows().stream()
                .map(row -> "INSERT into ${table} values (" + row.stream().map(value -> "'" + value + "'")
                        .collect(joining(", ")) + ");")
                .collect(joining())
                .replace("${table}", file.getName());
    }

    /**
     * execute dataload process to put data to associated ads
     *
     * @param process
     * @param executable
     * @param parameters
     * @return
     */
    private ProcessExecutionDetail executeProcess(final DataloadProcess process, final String executable,
                                                  final Parameters parameters) {
        return restClient.getProcessService()
                .executeProcess(new ProcessExecution(process, executable,
                        parameters.getParameters(), parameters.getSecureParameters()))
                .get();
    }

    private String generateHashString() {
        return UUID.randomUUID().toString().substring(0, 5);
    }

    private ProcessService getProcessService() {
        return restClient.getProcessService();
    }

    private Project getProject() {
        return restClient.getProjectService().getProjectById(projectId);
    }

    private String generateProcessName() {
        return "Ads dataload process-" + generateHashString();
    }

    /**
     * Add segments input of release process to associated list, later used this list to clean up
     *
     * @param segments
     */
    private void addReleasedSegmentsToAssociatedList(final JSONArray segments) {
        segments.forEach(segment -> {
            JSONObject obj = (JSONObject) segment;
            this.associatedSegments.add(obj.getString("segment_id"));
        });
    }

    private String createNewEmptyProject(final TestParameters testParameters, final String projectTitle) {
        final Project project = new Project(projectTitle, testParameters.getAuthorizationToken());
        project.setDriver(testParameters.getProjectDriver());
        project.setEnvironment(testParameters.getProjectEnvironment());

        return restClient.getProjectService().createProject(project).get(testParameters.getCreateProjectTimeout(), TimeUnit.MINUTES).getId();
    }

    private RestClient createDomainRestClient(final TestParameters testParameters) {
        return new RestClient(
                new RestProfile(testParameters.getHost(), testParameters.getDomainUser(),
                        testParameters.getPassword(), true));
    }

    public void deleteProject(final String projectId) {
        final ProjectService service = restClient.getProjectService();
        service.removeProject(service.getProjectById(projectId));
    }

    public String getMasterProject(final String domain, final String segmentId) {
        return LcmRestUtils.getMasterProjectId(this.restClient,domain , segmentId);
    }
}
