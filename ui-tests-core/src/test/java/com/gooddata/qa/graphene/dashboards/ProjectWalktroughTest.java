package com.gooddata.qa.graphene.dashboards;

import org.json.JSONException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractUITest;

public class ProjectWalktroughTest extends AbstractUITest {

    @BeforeClass
    public void initProperties() {
        testParams.setProjectId(testParams.loadProperty("projectId"));
    }

    @Test(groups = {"projectWalkthroughInit"})
    public void userLogin() throws JSONException {
        // sign in with demo user
        signInAtUI(testParams.getUser(), testParams.getPassword());
    }

    @Test(dependsOnGroups = {"projectWalkthroughInit"})
    public void verifyProject() {
        verifyProjectDashboardsAndTabs(false, null, true);
    }

}