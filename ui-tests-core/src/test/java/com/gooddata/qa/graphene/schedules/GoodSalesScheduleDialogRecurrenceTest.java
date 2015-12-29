/**
 * Copyright (C) 2007-2014, GoodData(R) Corporation. All rights reserved.
 */
package com.gooddata.qa.graphene.schedules;

import static org.testng.Assert.assertEquals;

import java.io.IOException;

import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.json.JSONException;
import org.testng.annotations.Test;

import com.gooddata.qa.graphene.entity.dashboard.scheduledialog.AbstractRecurrenceTestCase;
import com.gooddata.qa.graphene.entity.dashboard.scheduledialog.MonthlyDayOfMonthTestCase;
import com.gooddata.qa.graphene.entity.dashboard.scheduledialog.MonthlyDayOfWeekTestCase;
import com.gooddata.qa.graphene.entity.dashboard.scheduledialog.WeeklyTestCase;
import com.gooddata.qa.graphene.enums.project.ProjectFeatureFlags;
import com.gooddata.qa.graphene.enums.user.UserRoles;
import com.gooddata.qa.graphene.fragments.dashboards.DashboardScheduleDialog;
import com.gooddata.qa.utils.graphene.Screenshots;
import com.gooddata.qa.utils.http.RestUtils;

@Test(groups = {"GoodSalesShareDashboard"}, description = "Tests for GoodSales project - schedule dashboard")
public class GoodSalesScheduleDialogRecurrenceTest extends AbstractGoodSalesEmailSchedulesTest {
    private final String SCHEDULE_INFO = "This dashboard will be sent %s %s to %s as a PDF attachment.";
    private DashboardScheduleDialog dashboardScheduleDialog;
    private AbstractRecurrenceTestCase[] CASES = new AbstractRecurrenceTestCase[]{
            new WeeklyTestCase(
                    "weekly on Monday, Tuesday at 12:30 AM",
                    AbstractRecurrenceTestCase.DayTime.AM_12_30,
                    new AbstractRecurrenceTestCase.DayOfWeek[]{
                            AbstractRecurrenceTestCase.DayOfWeek.MONDAY,
                            AbstractRecurrenceTestCase.DayOfWeek.TUESDAY
                    },
                    AbstractRecurrenceTestCase.EveryXWeeks.ONE
            ),

            new WeeklyTestCase(
                    "every 2 weeks on Wednesday at 12:30 AM",
                    AbstractRecurrenceTestCase.DayTime.AM_12_30,
                    new AbstractRecurrenceTestCase.DayOfWeek[]{
                            AbstractRecurrenceTestCase.DayOfWeek.WEDNESDAY
                    },
                    AbstractRecurrenceTestCase.EveryXWeeks.TWO
            ),

            new MonthlyDayOfMonthTestCase(
                    "monthly on day 2 at 12:30 AM",
                    AbstractRecurrenceTestCase.DayTime.AM_12_30,
                    AbstractRecurrenceTestCase.MONTH_DAYS[2]
            ),

            new MonthlyDayOfWeekTestCase(
                    "monthly on the first Tuesday at 12:30 AM",
                    AbstractRecurrenceTestCase.DayTime.AM_12_30,
                    AbstractRecurrenceTestCase.DayOfWeek.TUESDAY,
                    AbstractRecurrenceTestCase.WeekOfMonth.FIRST
            ),

            new MonthlyDayOfWeekTestCase(
                    "monthly on the third Sunday at 12:30 AM",
                    AbstractRecurrenceTestCase.DayTime.AM_12_30,
                    AbstractRecurrenceTestCase.DayOfWeek.SUNDAY,
                    AbstractRecurrenceTestCase.WeekOfMonth.THIRD
            )
    };

    private String getTimezoneShortName() {
        return DateTimeZone.getDefault().getShortName(DateTimeUtils.currentTimeMillis());
    }

    @Test(dependsOnMethods = {"verifyEmptySchedules"}, groups = {"schedules"})
    public void setFeatureFlags() throws JSONException, IOException {
        RestUtils.enableFeatureFlagInProject(getRestApiClient(), testParams.getProjectId(),
                ProjectFeatureFlags.DASHBOARD_SCHEDULE_RECIPIENTS);
    }

    @Test(dependsOnGroups = {"schedules"})
    public void testRecurrences() throws IllegalArgumentException, JSONException {
        logout();
        signIn(true, UserRoles.ADMIN); // login with gray pages to reload application and have feature flag set
        initDashboardsPage();
        dashboardScheduleDialog = dashboardsPage.showDashboardScheduleDialog();

        for (AbstractRecurrenceTestCase testCase : CASES) {
            testCase.setDialogConfiguration(dashboardScheduleDialog);
            String scheduleInfo = testCase.getMessage();
            String infoText = dashboardScheduleDialog.getInfoText();
            String fullText = String.format(SCHEDULE_INFO, scheduleInfo, getTimezoneShortName(), testParams.getUser());
            Screenshots.takeScreenshot(browser, "Goodsales-schedules-dashboard-dialog-recurrence-weekly-" + scheduleInfo, this.getClass());

            assertEquals(infoText, fullText, "Custom time is in info message");
        }
    }
}
