package com.gooddata.qa.graphene.fragments.csvuploader;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import com.gooddata.qa.graphene.fragments.indigo.Header;
import com.gooddata.qa.graphene.utils.ElementUtils;
import org.jboss.arquillian.graphene.Graphene;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementNotPresent;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForElementVisible;
import static com.gooddata.qa.graphene.utils.WaitUtils.waitForFragmentVisible;
import static com.gooddata.qa.utils.CssUtils.simplifyText;
import static org.openqa.selenium.By.className;
import static org.openqa.selenium.By.cssSelector;

public class DatasetsListPage extends AbstractFragment {

    private static final By BY_EMPTY_STATE = className("empty-state");
    private static final By BY_MY_DATASETS_EMPTY_STATE = className("my-datasets-empty-state");
    private String PROJECT_DROPDOWN_SELECTOR = ".gd-header-project";

    @FindBy(className = "s-datasets-list-header")
    private WebElement datasetsHeader;

    @FindBy(className = "s-add-data-button")
    private WebElement addDataButton;

    @FindBy(css = ".s-my-datasets-list table")
    private DatasetsTable myDatasetsTable;

    @FindBy(css = ".others-datasets table")
    private DatasetsTable othersDatasetsTable;

    public static DatasetsListPage getInstance(SearchContext context) {
        return Graphene.createPageFragment(DatasetsListPage.class,
                waitForElementVisible(className("s-datasets-list"), context));
    }

    public DatasetDetailPage openDatasetDetailPage(String datasetName) {
        return getMyDatasetsTable().getDataset(datasetName).openDetailPage();
    }

    public FileUploadDialog clickAddDataButton() {
        waitForAddDataButtonVisible().click();
        return FileUploadDialog.getInstane(browser);
    }

    public String getDatasetAnalyzeLink(String datasetName) {
        return getMyDatasetsTable().getDataset(datasetName).getAnalyzeLink();
    }

    public void openAnalyzePage(String datasetName) {
        getMyDatasetsTable().getDataset(datasetName).openAnalyzePage();
    }

    public WebElement waitForHeaderVisible() {
        return waitForElementVisible(datasetsHeader);
    }

    public WebElement waitForAddDataButtonVisible() {
        return waitForElementVisible(addDataButton);
    }

    public WebElement waitForEmptyStateLoaded() {
        return waitForElementVisible(BY_EMPTY_STATE, browser);
    }

    public DatasetsListPage waitForMyDatasetsEmptyStateLoaded() {
        waitForElementVisible(BY_MY_DATASETS_EMPTY_STATE, browser);
        return this;
    }

    public String getEmptyStateMessage() {
        return waitForEmptyStateLoaded().getText();
    }

    public DatasetsTable getMyDatasetsTable() {
        return waitForFragmentVisible(myDatasetsTable);
    }

    public boolean isMyDatasetsEmpty() {
        final int TIMEOUT_IN_SECONDS = 2;
        //Before loading finishing for data page which has information, the empty page is always reloaded
        //To make sure that data list isn't missed, should be handled waiting to load data content.
        try {
            waitForElementNotPresent(BY_EMPTY_STATE, TIMEOUT_IN_SECONDS);
        } catch (Exception e) {
            return true;
        }
        return false;
    }

    public int getMyDatasetsCount() {
        return isMyDatasetsEmpty() ? 0 : getMyDatasetsTable().getNumberOfDatasets();
    }

    public boolean isOtherDatasetsEmpty() {
        // this is used as indicator for datasets table is loaded
        waitForFragmentVisible(myDatasetsTable);

        return !ElementUtils.isElementPresent(By.cssSelector(".others-datasets"), getRoot());
    }

    public DatasetsTable getOthersDatasetsTable() {
        return waitForFragmentVisible(othersDatasetsTable);
    }

    public int getOtherDatasetsCount() {
        return isOtherDatasetsEmpty() ? 0 : waitForFragmentVisible(othersDatasetsTable).getNumberOfDatasets();
    }

    public DataPreviewPage uploadFile(String filePath) {
        waitForElementVisible(addDataButton).click();

        FileUploadDialog.getInstane(browser)
            .pickCsvFile(filePath)
            .clickUploadButton();

        return DataPreviewPage.getInstance(browser);
    }

    public FileUploadDialog tryUploadFile(String filePath) {
        waitForElementVisible(addDataButton).click();

        FileUploadDialog dialog = FileUploadDialog.getInstane(browser);
        dialog.pickCsvFile(filePath).clickUploadButton();

        return dialog;
    }

    public DataPreviewPage updateCsv(String datasetName, String filePath) {
        return updateCsv(getMyDatasetsTable().getDataset(datasetName), filePath);
    }

    public DataPreviewPage updateCsv(Dataset dataset, String filePath) {
        dataset.clickUpdateButton()
            .pickCsvFile(filePath)
            .clickUploadButton();

        return DataPreviewPage.getInstance(browser);
    }

    public DatasetsListPage switchProject(String name) {
        log.info("Switching to project: " + name);

        Graphene.createPageFragment(Header.class, waitForElementVisible(By.className("gd-header"), browser))
                .switchProject(name);

        return waitForFragmentVisible(this);
    }

    public DatasetsListPage switchProjectAndWaitProjectIsSelected(String name) {
        switchProject(name);
        By buttonTitle = cssSelector(PROJECT_DROPDOWN_SELECTOR + ".s-" + simplifyText(name));
        waitForElementVisible(buttonTitle, browser);
        return waitForFragmentVisible(this);
    }
}
