package com.gooddata.qa.graphene.fragments.dashboards;

import com.gooddata.qa.graphene.fragments.AbstractFragment;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

import static com.gooddata.qa.graphene.common.CheckUtils.*;

public class DashboardsPage extends AbstractFragment {

    @FindBy(xpath = "//div[@id='abovePage']/div[contains(@class,'yui3-dashboardtabs-content')]")
    private DashboardTabs tabs;

    @FindBy(css = ".q-dashboardSwitcher")
    private WebElement dashboardSwitcherButton;

    @FindBy(css = ".menuArrow")
    private WebElement dashboardSwitcherArrowMenu;

    @FindBy(css = ".s-dashboards-menu")
    private WebElement dashboardsMenu;

    @FindBy(css = ".s-dashboards-menu-item")
    private List<WebElement> dashboardSelectors;

    @FindBy(xpath = "//button[@title='Edit, Embed or Export']")
    private WebElement editExportEmbedButton;

    @FindBy(css = ".s-edit")
    private WebElement editButton;

    @FindBy(css = ".s-export_to_pdf")
    private WebElement exportPdfButton;

    @FindBy(css = ".s-permissions")
    private WebElement permissionsButton;

    @FindBy(xpath = "//button[@title='Download PDF']")
    private WebElement printPdfButton;

    @FindBy(css = ".s-embed")
    private WebElement embedButton;

    @FindBy(css = ".s-add_dashboard")
    private WebElement addDashboardButton;

    @FindBy(xpath = "//button[@title='Add a new tab']")
    private WebElement addNewTabButton;

    @FindBy(xpath = "//div[contains(@class,'editTitleDialogView')]")
    private TabDialog newTabDialog;

    @FindBy(xpath = "//div[contains(@class,'s-dashboard-edit-bar')]")
    private DashboardEditBar editDashboardBar;

    @FindBy(xpath = "//div[contains(@class,'dashboardTitleEditBox')]/input")
    private WebElement newDashboardNameInput;

    @FindBy(xpath = "//div[contains(@class, 'c-confirmDeleteDialog')]")
    private WebElement dashboardTabDeleteDialog;

    @FindBy(xpath = "//div[contains(@class, 'c-confirmDeleteDialog')]//button[text()='Delete']")
    private WebElement dashboardTabDeleteConfirmButton;

    @FindBy(className = "yui3-c-filterdashboardwidget")
    private List<FilterWidget> filters;

    @FindBy(className = "yui3-c-projectdashboard-content")
    private DashboardContent content;

    @FindBy(xpath = "//div[contains(@class,'s-permissionSettingsDialog')]")
    private PermissionsDialog permissionsDialog;

    @FindBy(css = ".s-unlistedIcon")
    private WebElement unlistedIcon;

    @FindBy(css = ".s-lockIcon")
    private WebElement lockIcon;

    @FindBy (xpath = "//div[@class='yui3-d-embeddialog-content']")
    private DashboardEmbedDialog dashboardEmbedDialog;

    @FindBy(css = ".s-scheduleButton")
    private WebElement scheduleButton;

    @FindBy(xpath = "//div[contains(@class,'s-mailScheduleDialog')]")
    private DashboardScheduleDialog scheduleDialog;

    /**
     * Fragment represents link for saved view dialog on dashboard
     * when saved view mode is turned on
     *
     * @see SavedViewWidget
     */
    @FindBy(xpath = "//div[contains(@class,'savedFilters')]/button")
    private SavedViewWidget savedViewWidget;

    private By emptyTabPlaceholder = By.xpath("//div[contains(@class, 'yui3-c-projectdashboard-placeholder-visible')]");

    private static final By BY_DASHBOARD_SELECTOR_TITLE = By.xpath("a/span");
    private static final By BY_EXPORTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Exporting…']");
    private static final By BY_PRINTING_PANEL = By.xpath("//div[@class='box']//div[@class='rightContainer' and text()='Preparing printable PDF for download…']");
    private static final By BY_TAB_DROPDOWN_MENU = By.xpath("//div[contains(@class, 's-tab-menu')]");
    private static final By BY_TAB_DROPDOWN_DELETE_BUTTON = By.xpath("//li[contains(@class, 's-delete')]//a");

    public DashboardTabs getTabs() {
        return tabs;
    }

    public List<FilterWidget> getFilters() {
        return filters;
    }

    public DashboardContent getContent() {
        return content;
    }

    public DashboardEditBar getDashboardEditBar() {
        return editDashboardBar;
    }

    public WebElement getEditExportEmbedButton() {
        return editExportEmbedButton;
    }

    public PermissionsDialog getPermissionsDialog() {
        return permissionsDialog;
    }

    public SavedViewWidget getSavedViewWidget() {
        return savedViewWidget;
    }

    public String getDashboardName() {
        waitForElementVisible(dashboardSwitcherButton);
        String name = dashboardSwitcherButton.getText();
        if (getDashboardsCount() > 1) {
            return name.substring(0, name.length() - 1);
        }
        return name;
    }

    public boolean selectDashboard(String dashboardName) throws InterruptedException {
        if (getDashboardName().contains(dashboardName)) {
            System.out.println("Dashboard '" + dashboardName + "'already selected");
            return true;
        }
        waitForElementVisible(dashboardSwitcherButton).click();
        if (dashboardSelectors != null && dashboardSelectors.size() > 0) {
            for (WebElement elem : dashboardSelectors) {
                if (elem.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title").equals(dashboardName)) {
                    elem.findElement(BY_LINK).click();
                    Thread.sleep(3000);
                    waitForDashboardPageLoaded(browser);
                    return true;
                }
            }
        }
        System.out.println("Dashboard not selected because it's not present!!!!");
        return false;
    }

    public boolean selectDashboard(int dashboardIndex) throws InterruptedException {
        waitForElementVisible(dashboardSwitcherButton).click();
        Thread.sleep(3000);
        if (dashboardSelectors != null && dashboardSelectors.size() > 0) {
            for (WebElement elem : dashboardSelectors) {
                if (Integer.valueOf(elem.getAttribute("gdc:index")) == dashboardIndex) {
                    elem.findElement(BY_LINK).click();
                    Thread.sleep(3000);
                    waitForDashboardPageLoaded(browser);
                    return true;
                }
            }
        }
        System.out.println("Dashboard not selected because it's not present!!!!");
        return false;
    }

    public List<String> getDashboardsNames() throws InterruptedException {
        List<String> dashboardsNames = new ArrayList<String>();
        if (dashboardSwitcherArrowMenu.isDisplayed()) {
            dashboardSwitcherArrowMenu.click();
            waitForElementVisible(dashboardsMenu);
            if (dashboardSelectors != null && dashboardSelectors.size() > 0) {
                for (WebElement elem : dashboardSelectors) {
                    dashboardsNames.add(elem.findElement(BY_DASHBOARD_SELECTOR_TITLE).getAttribute("title"));
                }
            }
            dashboardSwitcherArrowMenu.click();
        } else if (dashboardSwitcherButton.isDisplayed()) {
            dashboardsNames.add(getDashboardName());
        }
        return dashboardsNames;
    }

    public int getDashboardsCount() {
        if (dashboardSwitcherArrowMenu.isDisplayed()) {
            dashboardSwitcherArrowMenu.click();
            int dashboardsCount = dashboardSelectors.size();
            dashboardSwitcherArrowMenu.click();
            return dashboardsCount;
        } else if (dashboardSwitcherButton.isDisplayed()) {
            return 1;
        }
        return 0;
    }

    public void editDashboard() {
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(editExportEmbedButton).click();
        waitForElementVisible(editButton).click();
        waitForElementPresent(editDashboardBar.getRoot());
    }

    public String exportDashboardTab(int tabIndex) throws InterruptedException {
        tabs.openTab(tabIndex);
        waitForDashboardPageLoaded(browser);
        String tabName = tabs.getTabLabel(0);
        waitForElementVisible(editExportEmbedButton).click();
        waitForElementVisible(exportPdfButton).click();
        waitForElementVisible(BY_EXPORTING_PANEL, browser);
        Thread.sleep(3000);
        waitForElementNotPresent(BY_EXPORTING_PANEL);
        Thread.sleep(3000);
        System.out.println("Dashboard " + tabName + " exported to PDF...");
        return tabName;
    }

    public String printDashboardTab(int tabIndex) throws InterruptedException {
        tabs.openTab(tabIndex);
        waitForDashboardPageLoaded(browser);
        String tabName = tabs.getTabLabel(0);
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(printPdfButton).click();
        waitForElementVisible(BY_PRINTING_PANEL,browser);
        Thread.sleep(3000);
        waitForElementNotPresent(BY_PRINTING_PANEL);
        Thread.sleep(3000);
        System.out.println("Dashboard " + tabName + " printed to Pdf");
        return tabName;
    }

    public DashboardEmbedDialog embedDashboard() throws InterruptedException {
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(editExportEmbedButton).click();
        waitForElementVisible(embedButton).click();
        waitForElementVisible(dashboardEmbedDialog.getRoot());
        return dashboardEmbedDialog;
    }

    public void addNewTab(String tabName) {
        waitForElementVisible(addNewTabButton).click();
        waitForElementVisible(newTabDialog.getRoot());
        newTabDialog.createTab(tabName);
    }

    public void deleteDashboardTab(int tabIndex) throws InterruptedException {
        tabs.openTab(tabIndex);
        editDashboard();
        boolean nonEmptyTab = browser.findElements(emptyTabPlaceholder).size() == 0;
        tabs.selectDropDownMenu(tabIndex);
        waitForElementVisible(BY_TAB_DROPDOWN_MENU, browser).findElement(BY_TAB_DROPDOWN_DELETE_BUTTON).click();
        if (nonEmptyTab) {
            waitForElementVisible(dashboardTabDeleteDialog);
            waitForElementVisible(dashboardTabDeleteConfirmButton).click();
            waitForElementNotPresent(dashboardTabDeleteDialog);
        }
        editDashboardBar.saveDashboard();
        waitForElementNotPresent(editDashboardBar.getRoot());
        waitForDashboardPageLoaded(browser);
    }

    public void addNewDashboard(String dashboardName) throws InterruptedException {
        waitForElementVisible(editExportEmbedButton).click();
        waitForElementVisible(addDashboardButton).click();
        waitForElementVisible(newDashboardNameInput).clear();
        newDashboardNameInput.click(); //sleep wasn't necessary, getting focus on the input field helps
        newDashboardNameInput.sendKeys(dashboardName);
        System.out.println(newDashboardNameInput.getText());
        Thread.sleep(2000);
        editDashboardBar.saveDashboard();
    }

    public void deleteDashboard() throws InterruptedException {
        editDashboard();
        editDashboardBar.deleteDashboard();
    }

    public void openPermissionsDialog() {
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(editExportEmbedButton).click();
        waitForElementVisible(permissionsButton).click();
        waitForElementVisible(permissionsDialog.getRoot());
    }

    public void publishDashboard(boolean listed) {
        openPermissionsDialog();
        permissionsDialog.publish(listed);
        permissionsDialog.submit();
    }

    public void lockDashboard(boolean locked) {
        openPermissionsDialog();
        if (locked) {
            permissionsDialog.lock();
        } else {
            permissionsDialog.unlock();
        }
        permissionsDialog.submit();
    }

    public void lockIconClick() {
        waitForElementVisible(lockIcon).click();
    }

    public void unlistedIconClick() {
        waitForElementVisible(unlistedIcon).click();
    }

    public boolean isLocked() {
        return lockIcon.isDisplayed();
    }

    public boolean isUnlisted() {
        return unlistedIcon.isDisplayed();
    }

    public boolean isEditButtonPresent() {
        try {
            return editButton.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public DashboardScheduleDialog scheduleDashboard() {
        waitForDashboardPageLoaded(browser);
        waitForElementVisible(scheduleButton).click();
        waitForElementVisible(scheduleDialog.getRoot());
        return scheduleDialog;
    }
}
