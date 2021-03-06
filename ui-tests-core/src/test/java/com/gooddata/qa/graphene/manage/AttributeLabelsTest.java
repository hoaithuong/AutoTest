package com.gooddata.qa.graphene.manage;

import com.gooddata.sdk.model.md.Fact;
import com.gooddata.qa.graphene.AbstractProjectTest;
import com.gooddata.qa.graphene.entity.report.UiReportDefinition;
import com.gooddata.qa.graphene.enums.AttributeLabelTypes;
import com.gooddata.qa.graphene.fragments.greypages.sfdc.ConfigureSFDCCredentials;
import com.gooddata.qa.graphene.fragments.reports.report.ReportWithImage;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport;
import com.gooddata.qa.graphene.fragments.reports.report.TableReport.CellType;
import org.jboss.arquillian.graphene.Graphene;
import org.json.JSONException;
import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;
import org.testng.annotations.Test;

import static com.gooddata.sdk.model.md.Restriction.title;
import static com.gooddata.qa.graphene.enums.ResourceDirectory.PAYROLL_CSV;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.io.ResourceUtils.getFilePathFromResource;
import static java.lang.String.format;
import static org.testng.Assert.assertTrue;

public class AttributeLabelsTest extends AbstractProjectTest {

    private static final String IMAGE_SFDC_ATTRIBUTE = "Image Sfdc";
    private static final String IMAGE_ATTRIBUTE = "Image";
    private static final String HYPERLINK_ATTRIBUTE = "Hyperlink";
    private static final String HYPERLINK_REPORT = "Hyperlink Report";
    private static final String IMAGE_REPORT = "Image Report";
    private static final String IMAGE_SFDC_REPORT = "Image SFDC Report";
    private static final String SUM_OF_AMOUNT_METRIC = "Sum of Amount";
    private static final String AMOUNT_FACT = "Amount";
    private static final String DEFAULT_METRIC_FORMAT = "#,##0";

    @FindBy(tagName = "form")
    protected ConfigureSFDCCredentials sfdc;

    @Override
    protected void initProperties() {
        // use empty project
        projectTitle = "SimpleProject-test-attribute-labels";
    }

    @Override
    protected void customizeProject() throws Throwable {
        uploadCSV(getFilePathFromResource("/" + PAYROLL_CSV + "/attribute_labels.csv"));
        takeScreenshot(browser, "uploaded-attribute_labels", getClass());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeAttributeToImageTest() {
        changeAttributeLabel(IMAGE_ATTRIBUTE, AttributeLabelTypes.IMAGE);
        changeAttributeLabel(IMAGE_SFDC_ATTRIBUTE, AttributeLabelTypes.IMAGE);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void createTestMetric() {
        createMetric(SUM_OF_AMOUNT_METRIC,
                format("SELECT SUM([%s])", getMdService().getObjUri(getProject(), Fact.class, title(AMOUNT_FACT))),
                DEFAULT_METRIC_FORMAT);
    }

    @Test(dependsOnMethods = {"createTestMetric", "changeAttributeToImageTest"})
    public void createImageReports() {
        createReport(new UiReportDefinition()
                        .withName(IMAGE_REPORT)
                        .withWhats(SUM_OF_AMOUNT_METRIC)
                        .withHows(IMAGE_ATTRIBUTE),
                        IMAGE_REPORT);
        createReport(new UiReportDefinition()
                        .withName(IMAGE_SFDC_REPORT)
                        .withWhats(SUM_OF_AMOUNT_METRIC)
                        .withHows(IMAGE_SFDC_ATTRIBUTE),
                        IMAGE_SFDC_REPORT);
    }

    @Test(dependsOnMethods = {"createImageReports"})
    public void verifyReportWithImageTest() {
        initReportsPage().openReport(IMAGE_REPORT);
        ReportWithImage report = Graphene.createPageFragment(
                ReportWithImage.class,
                browser.findElement(By.id("gridContainerTab")));
        report.verifyImageOnReport();
    }

    @Test(dependsOnGroups = {"createProject"})
    public void changeAttributeToHyperlinkTest() {
        changeAttributeLabel(HYPERLINK_ATTRIBUTE, AttributeLabelTypes.HYPERLINK);
        initAttributePage().verifyHyperLink(HYPERLINK_ATTRIBUTE);
    }

    @Test(dependsOnMethods = {"changeAttributeToHyperlinkTest"})
    public void configDrillToExternalPageTest() {
        initAttributePage().configureDrillToExternalPage(HYPERLINK_ATTRIBUTE);
    }

    @Test(dependsOnMethods = {"createTestMetric", "configDrillToExternalPageTest"})
    public void createReportWithHyperlinkTest() {
        createReport(new UiReportDefinition().withName(HYPERLINK_REPORT)
                                           .withWhats(SUM_OF_AMOUNT_METRIC)
                                           .withHows(HYPERLINK_ATTRIBUTE),
                     "Simple hyperlink report");
    }

    @Test(dependsOnMethods = {"createReportWithHyperlinkTest"})
    public void verifyReportWithHyperlinkTest() {
        TableReport report = initReportsPage().openReport(HYPERLINK_REPORT).getTableReport();
        assertTrue(report.isDrillableToExternalPage(CellType.ATTRIBUTE_VALUE), "cannot drill report to external page");
    }

    private void changeAttributeLabel(String attribute, AttributeLabelTypes label) {
        initAttributePage().configureAttributeLabel(attribute, label);
    }

}
