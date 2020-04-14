package com.gooddata.qa.graphene.modeler;

import com.gooddata.qa.graphene.AbstractDataIntegrationTest;
import com.gooddata.qa.graphene.fragments.modeler.LogicalDataModelPage;

public class AbstractLDMPageTest extends AbstractDataIntegrationTest {

    @Override
    protected void initProperties() {
        String domainUser = testParams.getDomainUser() != null ? testParams.getDomainUser() : testParams.getUser();
        testParams.setDomainUser(domainUser);
    }

    public LogicalDataModelPage initLogicalDataModelPage() {
        openUrl(LogicalDataModelPage.getUri(testParams.getProjectId()));
        return LogicalDataModelPage.getInstance(browser);
    }
}
