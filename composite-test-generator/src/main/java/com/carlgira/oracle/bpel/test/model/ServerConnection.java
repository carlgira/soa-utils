package com.carlgira.oracle.bpel.test.model;

/**
 * Created by emateo on 08/03/2016.
 */
public class ServerConnection {

    public String server;
    public String adminUser;
    public String adminPassword;
    public String realm;

    public ServerConnection(String serverURL, String adminUser, String adminPassword, String realm) {
        this.server = serverURL;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.realm = realm;
    }
}
