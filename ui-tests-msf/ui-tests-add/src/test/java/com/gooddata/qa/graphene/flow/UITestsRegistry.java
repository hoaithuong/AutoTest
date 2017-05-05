package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.add.SqlDiffTest;
import com.gooddata.qa.graphene.add.schedule.CreateScheduleTest;
import com.gooddata.qa.graphene.add.schedule.DatasetDetailTest;
import com.gooddata.qa.graphene.add.schedule.LoadDatasetTest;
import com.gooddata.qa.graphene.add.schedule.ScheduleDetailTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object> suites = new HashMap<>();

        suites.put("all", new Object[] {
            CreateScheduleTest.class,
            ScheduleDetailTest.class,
            DatasetDetailTest.class,
            LoadDatasetTest.class,
            SqlDiffTest.class,
            "testng-imap-notification.xml"
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
