package com.gooddata.qa.utils.webdav;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class WebDavClient {

    private Sardine sardine;

    String webDavStructure;

    public WebDavClient(String user, String password) {
        sardine = SardineFactory.begin(user, password);
    }

    public void setWebDavStructure(String webDavStructure) {
        this.webDavStructure = webDavStructure;
    }

    public String getWebDavStructure() {
        return webDavStructure;
    }

    public boolean createStructure(String userUploads){
        webDavStructure = userUploads+ "/" + UUID.randomUUID().toString();
        try {
            sardine.createDirectory(webDavStructure);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("Created WebDavClient structure on " + webDavStructure);
        return true;
    }

    public boolean uploadFile(File file) {
        try {
            InputStream fis = new FileInputStream(file);
            System.out.println("Using " + webDavStructure + " to upload " + file.getName());
            sardine.put(webDavStructure + "/" + file.getName(), IOUtils.toByteArray(fis));
            sardine.shutdown();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
