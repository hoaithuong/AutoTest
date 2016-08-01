package com.gooddata.qa.graphene;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.gooddata.project.Project;
import com.gooddata.project.ProjectDriver;
import com.gooddata.project.ProjectService;
import com.gooddata.project.ProjectValidationResults;
import org.apache.http.HttpHost;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.testng.Arquillian;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

import com.gooddata.GoodData;
import com.gooddata.qa.graphene.common.StartPageContext;
import com.gooddata.qa.graphene.common.TestParameters;
import com.gooddata.qa.utils.ads.AdsHelper;
import com.gooddata.qa.utils.http.RestApiClient;
import com.gooddata.qa.utils.testng.listener.AuxiliaryFailureScreenshotListener;
import com.gooddata.qa.utils.testng.listener.ConsoleStatusListener;
import com.gooddata.qa.utils.testng.listener.FailureLoggingListener;

@Listeners({ConsoleStatusListener.class, FailureLoggingListener.class, AuxiliaryFailureScreenshotListener.class})
public abstract class AbstractTest extends Arquillian {

    protected Properties testVariables;
    private String propertiesPath;

    protected TestParameters testParams;

    @Drone
    protected WebDriver browser;

    protected String imapHost;
    protected String imapUser;
    protected String imapPassword;

    protected GoodData goodDataClient = null;
    protected RestApiClient restApiClient = null;
    protected AdsHelper adsHelper = null;

    protected StartPageContext startPageContext = null;

    protected static final Logger log = Logger.getLogger(AbstractTest.class.getName());

    @BeforeClass(alwaysRun = true)
    public void loadProperties() {
        propertiesPath = System.getProperty("propertiesPath", System.getProperty("user.dir") +
                "/ui-tests-core/src/test/resources/variables-env-test.properties".replace("/",
                        System.getProperty("file.separator")));
        System.out.println("User properties: " + propertiesPath);

        testVariables = new Properties();
        try {
            FileInputStream in = new FileInputStream(propertiesPath);
            testVariables.load(in);
        } catch (IOException e) {
            throw new IllegalArgumentException("Properties weren't loaded from path: " + propertiesPath);
        }

        testParams = new TestParameters(testVariables);
    }

    @BeforeMethod(alwaysRun = true)
    public void loadPlatformPageBeforeTestMethod() {
        if (startPageContext == null) {
            return;
        }
        openUrl(startPageContext.getStartPage());
        startPageContext.waitForStartPageLoaded();
    }

    public void openUrl(String url) {
        String currentUrl = browser.getCurrentUrl();
        String pageURL = getRootUrl() + url.replaceAll("^/", "");
        System.out.println("Loading page ... " + pageURL);

        // Request Selenium to load a URL. If current URL is the one we want to load, page is NOT reloaded.
        browser.get(pageURL);

        // We need to call browser#get(String) before refreshing page to make sure the last request to browser has
        // method is GET unless we will get an alert about re-sending information to browser 
        if (pageURL.equals(currentUrl)) {
            browser.navigate().refresh();
        }
    }

    public String getRootUrl() {
        return "https://" + testParams.getHost() + "/";
    }

    public String getBasicRootUrl() {
        String rootUrl = getRootUrl();
        return getRootUrl().substring(0, rootUrl.length() - 1);
    }

    public RestApiClient getDomainUserRestApiClient() {
        return getRestApiClient(testParams.getDomainUser(), testParams.getPassword());
    }

    /**
     * Create {@link com.gooddata.qa.utils.http.RestApiClient} for admin user and save it to the test context.
     * @return {@link com.gooddata.qa.utils.http.RestApiClient} client for admin user
     */
    public RestApiClient getRestApiClient() {
        if (restApiClient == null) {
            restApiClient = getRestApiClient(testParams.getUser(), testParams.getPassword());
        }
        return restApiClient;
    }

    /**
     * Create {@link com.gooddata.qa.utils.http.RestApiClient} for specific user. It doesn't save it to the context!
     *
     * @return {@link com.gooddata.qa.utils.http.RestApiClient} for specific user.
     */
    public RestApiClient getRestApiClient(final String userLogin, final String userPassword) {
        return new RestApiClient(testParams.getHost(), userLogin, userPassword, true, false);
    }

    public GoodData getGoodDataClient() {
        if (goodDataClient == null) {
            goodDataClient = getGoodDataClient(testParams.getUser(), testParams.getPassword());
        }
        return goodDataClient;
    }

    public GoodData getGoodDataClient(final String userLogin, final String userPassword) {
        final HttpHost httpHost = RestApiClient.parseHost(testParams.getHost());
        return new GoodData(httpHost.getHostName(), userLogin, userPassword, httpHost.getPort());
    }

    public AdsHelper getAdsHelper() {
        if (adsHelper == null)
            adsHelper = new AdsHelper(getGoodDataClient(), getRestApiClient());
        return adsHelper;
    }

    public String generateEmail(String email) {
        return email.replace("@", "+" + UUID.randomUUID().toString().substring(0, 5) + "@");
    }

    public String generateDynamicUser(String user) {
        return user.replace("@", "+dynamic+" + UUID.randomUUID().toString().substring(0, 5) + "@");
    }

    public ProjectValidationResults validateProject() {
        final int timeout = testParams.getProjectDriver() == ProjectDriver.VERTICA ?
                testParams.getExtendedTimeout() : testParams.getDefaultTimeout();
        final ProjectService service = getGoodDataClient().getProjectService();
        final Project project = service.getProjectById(testParams.getProjectId());
        final ProjectValidationResults results = service.validateProject(project).get(timeout, TimeUnit.SECONDS);
        System.out.println("Project valid: " + results.isValid());
        return results;
    }
}
