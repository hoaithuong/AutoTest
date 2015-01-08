package com.gooddata.qa.graphene.project;

import com.gooddata.qa.graphene.AbstractProjectTest;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.CRC32;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = {"projectSimpleETL"}, description = "Tests for basic ETL functionality in GD platform")
public class SimpleProjectEtlTest extends AbstractProjectTest {

    protected int statusPollingCheckIterations = 60; // (60*5s)
    private static final boolean exportUsers = true;
    private static final boolean exportData = true;

    private class SLIManifestPart {

        private final String columnName;
        private final List<String> populateFields;

        public SLIManifestPart(String columnName, List<String> populateFields) {
            this.populateFields = populateFields;
            this.columnName = columnName;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result += this.columnName.hashCode();
            for (String s : this.populateFields) {
                result += s.hashCode();
            }
            return result;
        }
    }

    @Test(dependsOnMethods = {"createProject"})
    public void loadProject() throws JSONException, URISyntaxException, IOException, InterruptedException {
        URL maqlResource = getClass().getResource("/etl/maql-simple.txt");
        postMAQL(IOUtils.toString(maqlResource), statusPollingCheckIterations);

        URL csvResource = getClass().getResource("/etl/invoice.csv");
        String webdavURL = uploadFileToWebDav(csvResource, null);
        InputStream fileFromWebDav = getFileFromWebDav(webdavURL, csvResource);
        System.out.println("Checking local and remote CRC");
        assertEquals(getCRC(csvResource.openStream()), getCRC(fileFromWebDav), "Local and remote file CRC do not match");

        URL uploadInfoResource = getClass().getResource("/etl/upload_info.json");
        uploadFileToWebDav(uploadInfoResource, webdavURL);
        fileFromWebDav = getFileFromWebDav(webdavURL, uploadInfoResource);
        System.out.println("Checking local and remote CRC");
        assertEquals(getCRC(uploadInfoResource.openStream()), getCRC(fileFromWebDav), "Local and remote file CRC checksum do not match");

        postPullIntegration(webdavURL.substring(webdavURL.lastIndexOf("/") + 1, webdavURL.length()), statusPollingCheckIterations);
    }

    @Test(dependsOnMethods = {"loadProject"})
    public void sliManifestsCompare() throws JSONException, IOException, InterruptedException {
        HashSet<Object> sliParts = new HashSet<Object>();

        /** parse uploaded manifest first **/
        URL uploadInfoResource = this.getClass().getResource("/etl/upload_info.json");

        JSONObject dataSetSLIManifest = new JSONObject(IOUtils.toString(uploadInfoResource)).getJSONObject("dataSetSLIManifest");

        String uploadInfoFile = dataSetSLIManifest.getString("file");
        String uploadInfoDataset = dataSetSLIManifest.getString("dataSet");

        JSONArray jsonPartsArray = dataSetSLIManifest.getJSONArray("parts");
        for (int i = 0; i < jsonPartsArray.length(); i++) {
            String columnName = jsonPartsArray.getJSONObject(i).getString("columnName");
            ArrayList<String> populateFields = parsePopulatesFields(jsonPartsArray.getJSONObject(i).getJSONArray("populates"));
            sliParts.add(new SLIManifestPart(columnName, populateFields).hashCode());
        }

        /** check generated sli manifest **/
        dataSetSLIManifest = fetchSLIManifest(uploadInfoDataset).getJSONObject("dataSetSLIManifest");
        jsonPartsArray = dataSetSLIManifest.getJSONArray("parts");
        assertEquals(jsonPartsArray.length(), sliParts.size(), "SLIManifest parts count doesn't match");
        assertEquals(dataSetSLIManifest.getString("file"), "dataset.ds_" + uploadInfoFile, "SLIManifest file names doesn't match");
        assertEquals(dataSetSLIManifest.getString("dataSet"), uploadInfoDataset, "SLIManifest dataset names doesn't match");

        for (int i = 0; i < jsonPartsArray.length(); i++) {
            String columnName = jsonPartsArray.getJSONObject(i).getString("columnName");
            ArrayList<String> populateFields = parsePopulatesFields(jsonPartsArray.getJSONObject(i).getJSONArray("populates"));
            assertTrue(sliParts.contains(new SLIManifestPart(columnName, populateFields).hashCode()));
        }
    }

    @Test(dependsOnMethods = {"loadProject"})
    public void exportImportProject() throws JSONException, InterruptedException, IOException {
        String exportToken = exportProject(exportUsers, exportData, statusPollingCheckIterations);
        String parentProjectId = testParams.getProjectId();

        // New projectID is needed here. Load it from export, validate, delete and restore original one
        createProject();
        importProject(exportToken, statusPollingCheckIterations);
        validateProject();
        deleteProject(testParams.getProjectId());

        testParams.setProjectId(parentProjectId);
    }

    private ArrayList<String> parsePopulatesFields(JSONArray populates) throws JSONException {
        ArrayList<String> populateFields = new ArrayList<String>();
        for (int j = 0; j < populates.length(); j++) {
            populateFields.add(populates.getString(j));
        }
        return populateFields;
    }

    public static long getCRC(InputStream inputStreamn) throws IOException {
        CRC32 crc = new CRC32();
        int cnt;
        while ((cnt = inputStreamn.read()) != -1) {
            crc.update(cnt);
        }
        return crc.getValue();
    }
}