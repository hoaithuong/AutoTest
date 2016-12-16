package com.gooddata.qa.graphene.disc;

import static com.gooddata.qa.graphene.utils.WaitUtils.waitForProjectsPageLoaded;
import static com.gooddata.qa.utils.graphene.Screenshots.takeScreenshot;
import static com.gooddata.qa.utils.http.process.ProcessRestUtils.deteleProcess;
import static com.gooddata.qa.utils.http.project.ProjectRestUtils.createBlankProject;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.http.ParseException;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.gooddata.dataload.processes.DataloadProcess;
import com.gooddata.dataload.processes.Schedule;
import com.gooddata.qa.graphene.enums.disc.__Executable;
import com.gooddata.qa.graphene.enums.disc.__ScheduleCronTime;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.disc.projects.__ProjectsPage.FilterOption;
import com.gooddata.qa.graphene.fragments.disc.schedule.__ScheduleDetailFragment;
import com.gooddata.qa.utils.http.project.ProjectRestUtils;

public class __ProjectsPageTest extends __AbstractDISCTest {

    private static final String BASIC_PROCESS = "basic-process";
    private static final String EMPTY_STATE_MESSAGE = "No %sprojects matching \"%s\"";

    @Override
    protected void addUsersWithOtherRolesToProject() throws ParseException, JSONException, IOException {
        createAndAddUserToProject(UserRoles.EDITOR);
        createAndAddUserToProject(UserRoles.VIEWER);
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkFilterOption() {
        __initDiscProjectsPage();

        assertEquals(projectsPage.getSelectedFilterOption(), FilterOption.ALL.toString());
        assertEquals(projectsPage.getFilterOptions(), asList("all", "failed", "running",
                "scheduled", "successful", "unscheduled", "disabled"));
    }

    @DataProvider(name = "filterProvider")
    public Object[][] getFilterProvider() {
        return new Object[][] {
            {FilterOption.SUCCESSFUL, __Executable.SUCCESSFUL_GRAPH},
            {FilterOption.FAILED, __Executable.ERROR_GRAPH},
            {FilterOption.RUNNING, __Executable.LONG_TIME_RUNNING_GRAPH},
            {FilterOption.DISABLED, __Executable.SUCCESSFUL_GRAPH}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "filterProvider")
    public void checkFilterWorkCorrectly(FilterOption filterOption, __Executable executable) {
        DataloadProcess process = createProcessWithBasicPackage(BASIC_PROCESS);

        try {
            Schedule schedule = createSchedule(process, executable, __ScheduleCronTime.EVERY_30_MINUTE.getExpression());

            __ScheduleDetailFragment scheduleDetail = initScheduleDetail(schedule);

            if(filterOption == FilterOption.DISABLED) {
                scheduleDetail.disableSchedule();

            } else {
                scheduleDetail.executeSchedule();
                if (filterOption != FilterOption.RUNNING) {
                    scheduleDetail.waitForExecutionFinish();
                }
            }

            __initDiscProjectsPage().selectFilterOption(filterOption);
            takeScreenshot(browser, "Filter-" + filterOption + "-work-correctly", getClass());
            assertTrue(projectsPage.hasProject(projectTitle), "Filter " + filterOption + " not work properly");

            projectsPage.selectFilterOption(FilterOption.UNSCHEDULED);

            if (filterOption == FilterOption.DISABLED) {
                assertTrue(projectsPage.hasProject(projectTitle), "Filter " + filterOption + " not work properly");
            } else {
                assertFalse(projectsPage.hasProject(projectTitle), "Filter " + filterOption + " not work properly");
            }

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProcessesInfoOnProject() {
        DataloadProcess process = createProcessWithBasicPackage(BASIC_PROCESS);

        try {
            createSchedule(process, __Executable.SUCCESSFUL_GRAPH, __ScheduleCronTime.EVERY_30_MINUTE.getExpression());

            __initDiscProjectsPage();
            takeScreenshot(browser, "Data-loading-processes-shows-correctly", getClass());
            assertEquals(projectsPage.getProcessesInfoFrom(projectTitle), "1 processes, 1 schedules");

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLastSuccessfulExecution() {
        DataloadProcess process = createProcessWithBasicPackage(BASIC_PROCESS);

        try {
            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    __ScheduleCronTime.EVERY_30_MINUTE.getExpression());

            String executionDateTime = initScheduleDetail(schedule)
                    .executeSchedule()
                    .waitForExecutionFinish()
                    .getLastExecutionDateTime();

            __initDiscProjectsPage();
            takeScreenshot(browser, "Last-successful-execution-shows-correctly", getClass());
            assertEquals(projectsPage.getLastSuccessfulExecutionFrom(projectTitle), executionDateTime);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkLastSuccessfulAutoExecution() {
        DataloadProcess process = createProcessWithBasicPackage(BASIC_PROCESS);

        try {
            DateTime autoExecutionStartTime = DateTime.now().plusMinutes(2);

            Schedule schedule = createSchedule(process, __Executable.SUCCESSFUL_GRAPH,
                    parseDateToCronExpression(autoExecutionStartTime));

            String executionDateTime = initScheduleDetail(schedule)
                    .waitForAutoExecute(autoExecutionStartTime)
                    .waitForExecutionFinish()
                    .getLastExecutionDateTime();

            __initDiscProjectsPage();
            takeScreenshot(browser, "Last-successful-auto-execution-shows-correctly", getClass());
            assertEquals(projectsPage.getLastSuccessfulExecutionFrom(projectTitle), executionDateTime);

        } finally {
            deteleProcess(getGoodDataClient(), process);
        }
    }

    @DataProvider(name = "nonAdminUserProvider")
    public Object[][] getNonAdminUserProvider() {
        return new Object[][] {
            {UserRoles.EDITOR},
            {UserRoles.VIEWER}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "nonAdminUserProvider")
    public void checkProjectDisabledWithNonAdminRole(UserRoles role) throws JSONException {
        logoutAndLoginAs(true, role);

        try {
            __initDiscProjectsPage();

            takeScreenshot(browser, "Project-is-disabled-with-" + role + "-role", getClass());
            assertTrue(projectsPage.isProjectDisabled(projectTitle), "Project is not disabled");

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkPagingOptions() {
        __initDiscProjectsPage();

        assertEquals(projectsPage.getPagingOptions(), asList("20", "50", "100", "200", "500", "1000", "2000", "5000"));
        assertEquals(projectsPage.getSelectedPagingOption(), "20");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkPagingOptionWorkCorrectly() {
        final List<String> additionalProjects = prepareProjectsForPagingTest();
        final int totalProjects = getTotalProjectNumber();

        try {
            __initDiscProjectsPage();

            takeScreenshot(browser, "First-page-shows-corectly", getClass());
            assertEquals(projectsPage.getProjectNumber(), 20);
            assertEquals(projectsPage.getPagingDescription(), format("Showing 1-20 of %d projects", totalProjects));
            assertTrue(projectsPage.isOnPage(1), "Paging does not stay on 1");
            assertFalse(projectsPage.hasPreviousPage(), "Previous page button still enabled from beginning page");

            projectsPage.goToNextPage();
            assertTrue(projectsPage.isOnPage(2), "Paging does not stay on 2");

            projectsPage.goToPreviousPage();
            assertTrue(projectsPage.isOnPage(1), "Paging does not stay on 1");

            projectsPage.goToLastPage();
            takeScreenshot(browser, "Last-page-shows-corectly", getClass());
            assertEquals(projectsPage.getProjectNumber(), getProjectNumberOnLastPage());
            assertEquals(projectsPage.getPagingDescription(),
                    format("Showing %d-%d of %d projects", getProjectBeginIndexOnLastPage(), totalProjects, totalProjects));
            assertFalse(projectsPage.hasNextPage(), "Next page button still enabled from last page");

        } finally {
            if (!additionalProjects.isEmpty()) {
                additionalProjects.stream().forEach(p -> ProjectRestUtils.deleteProject(getGoodDataClient(), p));
            }
        }
    }

    @DataProvider(name = "emptySearchProvider")
    public Object[][] getEmptySearchProvider() {
        return new Object[][] {
            {FilterOption.FAILED},
            {FilterOption.RUNNING},
            {FilterOption.SCHEDULED},
            {FilterOption.SUCCESSFUL},
            {FilterOption.UNSCHEDULED},
            {FilterOption.DISABLED}
        };
    }

    @Test(dependsOnGroups = {"createProject"}, dataProvider = "emptySearchProvider")
    public void checkEmptySearchResult(FilterOption filterOption) {
        String searchKey = "!@#$%^";

        __initDiscProjectsPage()
                .selectFilterOption(filterOption)
                .searchProject(searchKey);
        assertEquals(projectsPage.getEmptyStateMessage(), format(EMPTY_STATE_MESSAGE, filterOption + " ", searchKey));

        projectsPage.clickSearchInAllProjectsLink();
        assertEquals(projectsPage.getEmptyStateMessage(), format(EMPTY_STATE_MESSAGE, "", searchKey));
        assertEquals(projectsPage.getSelectedFilterOption(), FilterOption.ALL.toString());
    }

    @Test(dependsOnGroups = {"createProject"})
    public void searchProject() {
        __initDiscProjectsPage().searchProject(projectTitle);
        assertTrue(projectsPage.hasProject(projectTitle), "Project not found");

        __initDiscProjectsPage().searchProject(testParams.getProjectId());
        assertTrue(projectsPage.hasProject(projectTitle), "Project not found");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void searchProjectWithUnicodeName() {
        String newProjectTitle = "Tiếng Việt ພາສາລາວ résumé";
        String newProjectId = createBlankProject(getGoodDataClient(), newProjectTitle,
                testParams.getAuthorizationToken(), testParams.getProjectDriver(), testParams.getProjectEnvironment());

        try {
            __initDiscProjectsPage().searchProject(newProjectTitle);
            takeScreenshot(browser, "Project-with-unicode-name-shows-correctly", getClass());
            assertTrue(projectsPage.hasProject(newProjectTitle), "Project not found");

        } finally {
            ProjectRestUtils.deleteProject(getGoodDataClient(), newProjectId);
        }
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkSearchField() {
        __initDiscProjectsPage();
        assertTrue(projectsPage.getSearchFieldValue().isEmpty(), "Default search field is not empty!");
        assertEquals(projectsPage.getSearchFieldHint(), "Search in project names and ids ...");

        projectsPage.inputSearchField("!@#$%").deleteSearchValue();
        assertTrue(projectsPage.getSearchFieldValue().isEmpty(), "Default search field is not empty!");
        assertEquals(projectsPage.getSearchFieldHint(), "Search in project names and ids ...");
    }

    @Test(dependsOnGroups = {"createProject"})
    public void checkProjectListAfterLeaveAProject() throws ParseException, JSONException, IOException {
        String newAdminUser = createAndAddUserToProject(UserRoles.ADMIN);

        try {
            logout();
            signInAtGreyPages(newAdminUser, testParams.getPassword());

            initProjectsAndUsersPage().leaveProject();
            waitForProjectsPageLoaded(browser);

            __initDiscProjectsPage();
            takeScreenshot(browser, "Leave-project-does-not-appear-on-projects-list", getClass());
            assertFalse(projectsPage.hasProject(projectTitle), "Leave project still appears on projects list");

        } finally {
            logoutAndLoginAs(true, UserRoles.ADMIN);
        }
    }

    private List<String> prepareProjectsForPagingTest() {
        int totalProjects = getTotalProjectNumber();

        if (totalProjects > 20) {
            return emptyList();
        }

        return IntStream.rangeClosed(1, 21 - totalProjects)
                .mapToObj(i -> createBlankProject(getGoodDataClient(), "Test-paging" + i, testParams.getAuthorizationToken(),
                        testParams.getProjectDriver(), testParams.getProjectEnvironment()))
                .collect(toList());
    }

    private int getProjectNumberOnLastPage() {
        int remainder = getTotalProjectNumber() % 20;
        return remainder == 0 ? 20 : remainder; 
    }

    private int getProjectBeginIndexOnLastPage() {
        return getTotalProjectNumber() - getProjectNumberOnLastPage() + 1;
    }

    private int getTotalProjectNumber() {
        return getGoodDataClient().getProjectService().getProjects().size();
    }
}
