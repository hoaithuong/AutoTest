package com.gooddata.qa.graphene.manage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.enums.ReportTypes;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardEditBar;
import com.gooddata.qa.graphene.fragments.greypages.sfdccredentials.ConfigureSFDCCredentials;
import com.gooddata.qa.graphene.fragments.reports.ReportWithImage;
import com.gooddata.qa.graphene.fragments.reports.TableReport;
import com.gooddata.qa.graphene.fragments.upload.UploadColumns.OptionDataType;

@Test(groups = {"projectSimpleAttribute"}, description = "Tests for configuration of attribute labels functionality on simple project in GD platform")
public class AttributeLabelsTest extends AbstractProjectTest {

    private String csvFilePath;
    private List<String> attributesList;
    private String hyperlinkAttr;
    private String hyperlinkReport;

    @FindBy(tagName = "form")
    protected ConfigureSFDCCredentials sfdc;

    @BeforeClass
    public void setProjectTitle() {
        csvFilePath = loadProperty("csvFilePath");
        projectTitle = "Attribute-labels-test";
    }

    @Test(dependsOnMethods = {"createProject"}, groups = {"tests"})
    public void initialize() throws InterruptedException, JSONException {
        hyperlinkAttr = "Hyperlink";
        hyperlinkReport = "Hyperlink Report";
        attributesList = Arrays.asList("Geo_pushpin", "AUS_State_Name",
                "AUS_State_ISO", "StateName", "StateID", "StateCode",
                "CountyID", "CountryName", "Country_ISO2", "Country_ISO3",
                "CZ_District_Name", "CZ_District_NO_Diacritics",
                "CZ_District_NUTS4", "CZ_District_KNOK");
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"tests"})
    public void initDataTest() throws InterruptedException {
        Map<Integer, OptionDataType> columnIndexAndType = new HashMap<Integer, OptionDataType>();
        columnIndexAndType.put(12, OptionDataType.TEXT);
        uploadCSV(csvFilePath + "attribute_labels.csv", columnIndexAndType, "attribute-labels");
    }

    @Test(dependsOnMethods = {"initialize"}, groups = {"tests"})
    public void setSFDCCredentialsTest() throws InterruptedException,
            JSONException {
        openUrl(PAGE_GDC_PROJECTS + "/" + projectId + "/credentials/sfdc");
        waitForElementVisible(sfdc.getRoot());
        sfdc.setSFDCCredentials(loadProperty("sfdc.email"),
                loadProperty("sfdc.password") + loadProperty("sfdc.securityToken"));
    }

    @Test(dependsOnMethods = {"initDataTest"}, groups = {"tests"})
    public void changeAttributeToImageTest() throws InterruptedException {
        changeAttributeLabel("Image", AttributeLabelTypes.IMAGE);
        changeAttributeLabel("Image_SFDC", AttributeLabelTypes.IMAGE);
    }

    @Test(dependsOnMethods = {"changeAttributeToImageTest"}, groups = {
            "tests"})
    public void verifyReportWithImageTest() throws InterruptedException {
        initReport("Image Top 5");
        ReportWithImage report = Graphene.createPageFragment(
                ReportWithImage.class,
                browser.findElement(By.id("gridContainerTab")));
        report.verifyImageOnReport();
    }

    @Test(dependsOnMethods = {"setSFDCCredentialsTest",
            "changeAttributeToImageTest"}, groups = {"tests"})
    public void verifyReportWithImageSFDCTest() throws InterruptedException {
        initReport("Image_SFDC Top 5");
        ReportWithImage report = Graphene.createPageFragment(
                ReportWithImage.class,
                browser.findElement(By.id("gridContainerTab")));
        report.verifyIfImageSFDCOnReport();

    }

    @Test(dependsOnMethods = {"initDataTest"}, groups = {"tests"})
    public void changeAttributeToHyperlinkTest() throws InterruptedException {
        changeAttributeLabel(hyperlinkAttr, AttributeLabelTypes.HYPERLINK);
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
        attributePage.verifyHyperLink(hyperlinkAttr);
    }

    @Test(dependsOnMethods = {"changeAttributeToHyperlinkTest"}, groups = {
            "tests"})
    public void configDrillToExternalPageTest() throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
        attributePage.configureDrillToExternalPage(hyperlinkAttr);
    }

    @Test(dependsOnMethods = {"configDrillToExternalPageTest"}, groups = {
            "tests"})
    public void createReportWithHyperlinkTest() throws InterruptedException {
        List<String> what = Arrays.asList("Count of Image");
        List<String> how = Arrays.asList(hyperlinkAttr);
        createReport(hyperlinkReport, ReportTypes.TABLE, what, how, "Simple hyperlink report");
    }

    @Test(dependsOnMethods = {"createReportWithHyperlinkTest"}, groups = {
            "tests"})
    public void verifyReportWithHyperlinkTest() throws InterruptedException {
        initReport(hyperlinkReport);
        TableReport report = Graphene.createPageFragment(TableReport.class,
                browser.findElement(By.id("gridContainerTab")));
        report.verifyAttributeIsHyperlinkInReport();
    }

    @Test(dependsOnMethods = {"initDataTest"}, groups = {"tests"})
    public void changeAttributeToGeoStateTest() throws InterruptedException {
        int i = 0;
        for (AttributeLabelTypes type : getGeoLabels()) {
            changeAttributeLabel(attributesList.get(i), type);
            i++;
        }
    }

    @Test(dependsOnMethods = {"changeAttributeToGeoStateTest"}, groups = {"tests"})
    public void verifyGeoLayersTest() throws InterruptedException {
        initDashboardsPage();
        DashboardEditBar dashboardEditBar = dashboardsPage.getDashboardEditBar();
        dashboardsPage.addNewDashboard("Test");
        dashboardsPage.editDashboard();
        dashboardEditBar.verifyGeoLayersList("Sum of Amount", attributesList);
    }

    @Test(dependsOnGroups = {"tests"})
    public void finalTest() {
        successfulTest = true;
    }

    private void initReport(String reportName) {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|domainPage|");
        waitForReportsPageLoaded();
        reportsPage.getReportsList().openReport(reportName);
        waitForAnalysisPageLoaded();
    }

    private void changeAttributeLabel(String attribute, AttributeLabelTypes label)
            throws InterruptedException {
        openUrl(PAGE_UI_PROJECT_PREFIX + projectId + "|dataPage|attributes");
        attributePage.configureAttributeLabel(attribute, label);
    }

    public List<AttributeLabelTypes> getGeoLabels() {
        List<AttributeLabelTypes> list = new ArrayList<AttributeLabelTypes>();
        for (AttributeLabelTypes label : AttributeLabelTypes.values()) {
            if (label.isGeoLabel()) {
                list.add(label);
            }
        }
        return list;
    }
}