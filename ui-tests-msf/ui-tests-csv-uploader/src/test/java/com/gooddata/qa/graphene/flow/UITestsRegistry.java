package com.gooddata.qa.graphene.flow;

import java.util.HashMap;
import java.util.Map;

import com.gooddata.qa.graphene.csvuploader.DataPreviewAfterUploadTest;
import com.gooddata.qa.graphene.csvuploader.DatasetDetailTest;
import com.gooddata.qa.graphene.csvuploader.DeleteDatasetTest;
import com.gooddata.qa.graphene.csvuploader.EmptyStateTest;
import com.gooddata.qa.graphene.csvuploader.ProjectSwitchTest;
import com.gooddata.qa.graphene.csvuploader.TooltipValidationTest;
import com.gooddata.qa.graphene.csvuploader.UploadDateTest;
import com.gooddata.qa.graphene.csvuploader.UploadErrorTest;
import com.gooddata.qa.graphene.csvuploader.UploadTest;
import com.gooddata.qa.utils.flow.TestsRegistry;

public class UITestsRegistry {

    public static void main(String[] args) throws Throwable {
        Map<String, Object[]> suites = new HashMap<>();

        suites.put("all", new Object[] {
            DatasetDetailTest.class,
            DataPreviewAfterUploadTest.class,
            DeleteDatasetTest.class,
            EmptyStateTest.class,
            UploadErrorTest.class,
            UploadTest.class,
            UploadDateTest.class,
            ProjectSwitchTest.class,
            TooltipValidationTest.class,
            "testng-csv-permissions-DataOfOtherUsers.xml",
            "testng-csv-permissions-RefreshTest.xml",
            "testng-csv-permissions-NavigationError.xml",
            "testng-csv-permissions-UploadHistoryInfo.xml",
            "testng-csv-imap-Notification.xml"
        });

        TestsRegistry.getInstance()
            .register(args, suites)
            .toTextFile();
    }
}
