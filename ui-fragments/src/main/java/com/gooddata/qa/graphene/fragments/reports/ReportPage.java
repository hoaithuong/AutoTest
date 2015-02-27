package com.gooddata.qa.graphene.fragments.reports;

import static com.gooddata.qa.graphene.common.CheckUtils.waitForAnalysisPageLoaded;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementNotVisible;
import static com.gooddata.qa.graphene.common.CheckUtils.waitForElementVisible;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gooddata.qa.graphene.entity.ReportDefinition;
import com.gooddata.qa.graphene.entity.filter.FilterItem;
import com.gooddata.qa.graphene.entity.filter.NumericRangeFilterItem;
import com.gooddata.qa.graphene.entity.filter.RankingFilterItem;
import com.gooddata.qa.graphene.entity.filter.SelectFromListValuesFilterItem;
import com.gooddata.qa.graphene.entity.filter.VariableFilterItem;
import com.gooddata.qa.graphene.enums.ExportFormat;
import com.gooddata.qa.graphene.enums.metrics.SimpleMetricTypes;
import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.google.common.base.Predicate;

public class ReportPage extends AbstractFragment {

    @FindBy(id = "analysisReportTitle")
    private WebElement reportName;

    @FindBy(xpath = "//input[@class='ipeEditor']")
    private WebElement reportNameInput;

    @FindBy(xpath = "//div[@class='c-ipeEditorControls']/button")
    private WebElement reportNameSaveButton;

    @FindBy(xpath = "//div[@id='reportSaveButtonContainer']/button")
    private WebElement createReportButton;

    @FindBy(xpath = "//div[contains(@class, 's-saveReportDialog')]//footer[@class='buttons']//button[contains(@class, 's-btn-create')]")
    private WebElement confirmDialogCreateButton;

    @FindBy(id = "reportVisualizer")
    private ReportVisualizer visualiser;

    @FindBy(xpath = "//button[contains(@class, 'exportButton')]")
    private WebElement exportButton;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-pdf')]//a")
    private WebElement exportToPDF;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-pdf__portrait_')]//a")
    private WebElement exportToPDFPortrait;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-pdf__landscape_')]//a")
    private WebElement exportToPDFLandscape;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-image__png_')]//a")
    private WebElement exportToPNG;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-excel_xls')]//a")
    private WebElement exportToXLS;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-excel_xlsx')]//a")
    private WebElement exportToXLSX;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-csv')]//a")
    private WebElement exportToCSV;

    @FindBy(xpath = "//div[contains(@class, 'yui3-m-export')]//li[contains(@class, 's-raw_data__csv_')]//a")
    private WebElement exportToRawCSV;

    @FindBy(css = "div.report")
    private List<WebElement> reportsList;

    @FindBy(css = ".s-btn-save")
    private WebElement saveReportButton;

    @FindBy(css = ".s-btn-saved")
    private WebElement alreadySavedButton;

    @FindBy(xpath = "//div[contains(@class,'c-dashboardUsageWarningDialog')]")
    private WebElement confirmSaveDialog;

    private String confirmSaveDialogLocator = "//div[contains(@class,'c-dashboardUsageWarningDialog')]";

    @FindBy(xpath = "//span[2]/button[3]")
    private WebElement confirmSaveButton;

    @FindBy(xpath = "//div[contains(@class, 'reportEditorFilterArea')]/button[not (@disabled)]")
    private WebElement filterButton;

    @FindBy(xpath = "//div[@id='filtersContainer']")
    private ReportFilter reportFilter;

    @FindBy(id = "p-analysisPage")
    private TableReport tableReport;

    public TableReport getTableReport() {
        return tableReport;
    }

    public ReportVisualizer getVisualiser() {
        return visualiser;
    }

    public void setReportName(String reportName) {
        waitForElementVisible(this.reportName).click();
        waitForElementVisible(reportNameInput).clear();
        reportNameInput.sendKeys(reportName);
        waitForElementVisible(reportNameSaveButton).click();
        waitForElementNotVisible(reportNameInput);
        assertEquals(this.reportName.getText(), reportName, "Report name wasn't updated");
    }

    public String getReportName() {
        return reportName.getAttribute("title");
    }

    public void createReport(ReportDefinition reportDefinition) throws InterruptedException {
        // Wait to avoid red bar randomly
        // Red bar message: An error occurred while performing this operation.
        Thread.sleep(3000);

        setReportName(reportDefinition.getName());

        if (reportDefinition.shouldAddWhatToReport())
            visualiser.selectWhatArea(reportDefinition.getWhats());

        if (reportDefinition.shouldAddHowToReport())
            visualiser.selectHowArea(reportDefinition.getHows());

        visualiser.finishReportChanges();

        if (reportDefinition.shouldAddFilterToReport()) {
            for (FilterItem filter : reportDefinition.getFilters()) {
                addFilter(filter);
            }
        }

        visualiser.selectReportVisualisation(reportDefinition.getType());
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(createReportButton).click();
        waitForElementVisible(confirmDialogCreateButton).click();
        waitForElementNotVisible(confirmDialogCreateButton);

        Graphene.waitGui().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return "Saved".equals(createReportButton.getText().trim());
            }
        });
        // When Create button change its name to Saving, and then Saved, the create report process is not finished.
        // Report have to refresh some parts, e.g. the What button have to enable, then disable, then enable.
        // If we navigate to another url when create report is not finished, unsaved change dialog will appear.
        // Use sleep here to make sure that process is finished
        Thread.sleep(1000);
    }

    public void createSimpleMetric(SimpleMetricTypes metricOperation, String metricOnFact, String metricName, boolean addToGlobal){
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(this.getRoot());
        visualiser.addSimpleMetric(metricOperation, metricOnFact, metricName, addToGlobal);
    }

    public String exportReport(ExportFormat format) throws InterruptedException {
        // Wait to avoid red bar randomly
        // Red bar message: An error occurred while performing this operation.
        Thread.sleep(3000);

        String reportName = getReportName();
        waitForElementVisible(exportButton).click();
        WebElement currentExportLink = null;
        switch (format) {
            case PDF:
                currentExportLink = exportToPDF;
                break;
            case PDF_PORTRAIT:
                currentExportLink = exportToPDFPortrait;
                break;
            case PDF_LANDSCAPE:
                currentExportLink = exportToPDFLandscape;
                break;
            case IMAGE_PNG:
                currentExportLink = exportToPNG;
                break;
            case EXCEL_XLS:
                currentExportLink = exportToXLS;
                break;
            case EXCEL_XLSX:
                currentExportLink = exportToXLSX;
                break;
            case CSV:
                currentExportLink = exportToCSV;
                break;
            case RAW_CSV:
                currentExportLink = exportToRawCSV;
                break;
            default:
                break;
        }
        waitForElementVisible(currentExportLink).click();
        Thread.sleep(5000);
        // waitForElementVisible(BY_EXPORTING_STATUS); //this waiting is causing
        // some unexpected issues in tests when the export (xls/csv) is too fast
        waitForElementVisible(exportButton);
        Thread.sleep(3000);
        System.out.println("Report " + reportName + " exported to "
                + format.getName());
        return reportName;
    }

    public void addFilter(FilterItem filterItem) throws InterruptedException {
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(filterButton).click();
        waitForElementVisible(reportFilter.getRoot());
        String textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountBefore = getNumber(textOnFilterButton);

        if (filterItem instanceof SelectFromListValuesFilterItem) {
            reportFilter.addFilterSelectList((SelectFromListValuesFilterItem) filterItem);

        } else if (filterItem instanceof RankingFilterItem) {
            reportFilter.addRankFilter((RankingFilterItem) filterItem);

        } else if (filterItem instanceof NumericRangeFilterItem) {
            reportFilter.addRangeFilter((NumericRangeFilterItem) filterItem);

        } else if (filterItem instanceof VariableFilterItem) {
            reportFilter.addPromtFiter((VariableFilterItem) filterItem);

        } else {
            throw new IllegalArgumentException("Unknow filter item: " + filterItem);
        }

        textOnFilterButton = waitForElementVisible(filterButton).getText();
        float filterCountAfter = getNumber(textOnFilterButton);
        assertEquals(filterCountAfter, filterCountBefore + 1, "Filter wasn't added");
        Thread.sleep(2000);
    }

    public void saveReport() throws InterruptedException {
        waitForAnalysisPageLoaded(browser);
        waitForElementVisible(createReportButton).click();
        if (browser.findElements(By.xpath(confirmSaveDialogLocator)).size() > 0) {
            waitForElementVisible(confirmSaveButton).click();
        }
        Thread.sleep(3000);
        assertEquals(createReportButton.getText(), "Saved", "Report wasn't saved");
    }

    public static float getNumber(String text) {
        String tmp = "";
        float number = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) > 44 && text.charAt(i) < 58 && text.charAt(i) != 47) {
                tmp += text.charAt(i);
            }
        }
        if (tmp.length() > 0) {
            number = Float.parseFloat(tmp);
        }
        return number;
    }
}
