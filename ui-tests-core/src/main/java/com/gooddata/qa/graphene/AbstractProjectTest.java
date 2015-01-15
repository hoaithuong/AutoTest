package com.gooddata.qa.graphene;

import com.gooddata.qa.graphene.enums.DWHDriver;
import com.gooddata.qa.graphene.enums.UserRoles;
import com.gooddata.qa.utils.graphene.Screenshots;
import org.json.JSONException;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static com.gooddata.qa.graphene.common.CheckUtils.*;

public abstract class AbstractProjectTest extends AbstractUITest {

    protected static final int DEFAULT_PROJECT_CHECK_LIMIT = 60; // 5 minutes

    protected String projectTitle = "simple-project";
    protected String projectTemplate = "";
    protected int projectCreateCheckIterations = DEFAULT_PROJECT_CHECK_LIMIT;

    protected boolean addUsersWithOtherRoles = false;

    @BeforeClass
    public void initStartPage() {
        startPage = "projects.html";
    }

    @Test(groups = {"projectInit"})
    public void init() throws JSONException {
        // sign in with admin user
        signIn(false, UserRoles.ADMIN);
    }

    @Test(dependsOnGroups = {"projectInit"})
    public void createProject() throws JSONException, InterruptedException, IOException {
        if (testParams.isReuseProject()) {
            if (testParams.getProjectId() != null && !testParams.getProjectId().isEmpty()) {
                System.out.println("Project will be re-used, id: " + testParams.getProjectId());
                return;
            } else {
                System.out.println("Project reuse is expected, but projectId is missing, new project will be created...");
            }
        }
        openUrl(PAGE_GDC_PROJECTS);
        waitForElementVisible(gpProject.getRoot());

        projectTitle += "-" + testParams.getDwhDriver().name();
        if (projectTemplate.isEmpty()) {
            testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null, testParams.getAuthorizationToken(), testParams.getDwhDriver(), projectCreateCheckIterations));
        } else {
            testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, projectTemplate, testParams.getAuthorizationToken(), DWHDriver.PG, projectCreateCheckIterations));

            if (testParams.getDwhDriver().equals(DWHDriver.VERTICA)) {
                String exportToken = exportProject(true, true, projectCreateCheckIterations * 5);
                deleteProject(testParams.getProjectId());

                openUrl(PAGE_GDC_PROJECTS);
                waitForElementVisible(gpProject.getRoot());
                testParams.setProjectId(gpProject.createProject(projectTitle, projectTitle, null, testParams.getAuthorizationToken2(), testParams.getDwhDriver(), projectCreateCheckIterations));
                importProject(exportToken, projectCreateCheckIterations * 5);
            }
        }
        Screenshots.takeScreenshot(browser, projectTitle + "-created", this.getClass());

        if (addUsersWithOtherRoles) addUsersWithOtherRolesToProject();
    }

    @AfterClass()
    public void validateProjectTearDown() throws JSONException {
        //it is necessary to login admin to validate project on afterClass
        logout();
        signIn(false, UserRoles.ADMIN);

        System.out.println("Going to validate project after tests...");
        // TODO remove when ATP-1520, ATP-1519, ATP-1822 are fixed
        String testName = this.getClass().getSimpleName();
        if (testName.contains("Coupa") || testName.contains("Pardot") || testName.contains("Zendesk4")) {
            System.out.println("Validations are skipped for Coupa, Pardot and Zendesk4 projects");
            return;
        }
        assertEquals(validateProject(), "OK");
    }

    @AfterClass(dependsOnMethods = {"validateProjectTearDown"}, alwaysRun = true)
    public void deleteProjectTearDown(ITestContext context) {
        if (testParams.isReuseProject()) {
            System.out.println("Project is being re-used and won't be deleted.");
            return;
        }
        System.out.println("Delete mode is set to " + testParams.getDeleteMode().toString());
        String projectId = testParams.getProjectId();
        if (projectId != null && projectId.length() > 0) {
            switch (testParams.getDeleteMode()) {
                case DELETE_ALWAYS:
                    System.out.println("Project will be deleted...");
                    deleteProject(projectId);
                    break;
                case DELETE_IF_SUCCESSFUL:
                    if (context.getFailedTests().size() == 0) {
                        System.out.println("Test was successful, project will be deleted...");
                        deleteProject(projectId);
                    } else {
                        System.out.println("Test wasn't successful, project won't be deleted...");
                    }
                    break;
                case DELETE_NEVER:
                    System.out.println("Delete mode set to NEVER, project won't be deleted...");
                    break;
            }
        } else {
            System.out.println("No project created -> no delete...");
        }
    }
}
