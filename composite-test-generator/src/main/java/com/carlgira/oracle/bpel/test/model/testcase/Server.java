package com.carlgira.oracle.bpel.test.model.testcase;

/**
 * Created by emateo on 04/03/2016.
 */
public class Server {
    public String adminUser;
    public String adminPassword;
    public String serverUrl;
    public String realm;

    public Server(){

    }

    public Server(String adminUser, String adminPassword, String serverUrl, String realm) {
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.serverUrl = serverUrl;
        this.realm = realm;
    }
}
