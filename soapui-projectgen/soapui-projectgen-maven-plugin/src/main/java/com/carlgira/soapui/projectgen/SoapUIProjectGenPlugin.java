package com.carlgira.soapui.projectgen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Maven plugin for the automatic generation of soapUI projects, using WSDLs to create TestSuites and MockServices 
 * @author Carlos Giraldo. carlgira@gmail.com
 */
@Mojo(name = "generateSoapUIProject", defaultPhase = LifecyclePhase.GENERATE_SOURCES, executionStrategy = "run")
@Execute(goal = "run", phase = LifecyclePhase.GENERATE_SOURCES)
public class SoapUIProjectGenPlugin extends AbstractMojo 
{
    /**
     * Directory of WSDLs for create TestSuites
     */
    @Parameter(property = "testWSDLDir")
    private String testWSDLDir;
    
    /**
     * Directory of WSDLs for create MockServices
     */
    @Parameter(property = "mockWSDLDir")
    private String mockWSDLDir;
    
    /**
     * Absolute path for the soapUI project
     */
    @Parameter(property = "soapUIProjectFileName")
    private String soapUIProjectFileName;

    /**
     * Project Name of the soapUI project
     */
    @Parameter(property = "projectName", defaultValue = "${project.artifactId}")
    private String projectName;

    /**
     * WSDL list of files/list of URLs to create TestSuites
     */
    @Parameter(property = "testWSDLFiles")
    private String[] testWSDLFiles;

    /**
     * WSDL list of files to create MockServices
     */
    @Parameter(property = "mockWSDLFiles")
    private String[] mockWSDLFiles;

    /**
     * Absolute path for the properties file (Endpoints of the services)
     */
    @Parameter(property = "testPropertiesFile")
    private String testPropertiesFile;

    /**
     * Initial port for the mock services
     */
    @Parameter(property = "initialMockPort", defaultValue = "63000")
    private String initialMockPort;

    public SoapUIProjectGenPlugin() 
    {
    }

    @Override
    public void execute() throws MojoExecutionException 
    {
        try 
        {
            SoapUIProjectGen generator = new SoapUIProjectGen(testWSDLDir, mockWSDLDir, soapUIProjectFileName, this.projectName, this.testWSDLFiles, this.mockWSDLFiles, Integer.parseInt(initialMockPort), this.testPropertiesFile);
            boolean projectGeneratedFlag = generator.createSoapUIProject();
            if (projectGeneratedFlag) 
            {
                getLog().info("SoapUIProject created");
            } 
            else 
            {
                getLog().info("No files founded");
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
            throw new MojoExecutionException(e.getMessage());
        }
    }

    public String getClientWSDLDir() {
        return testWSDLDir;
    }

    public void setClientWSDLDir(String clientWSDLDir) {
        this.testWSDLDir = clientWSDLDir;
    }

    public String getMockWSDLDir() {
        return mockWSDLDir;
    }

    public void setMockWSDLDir(String mockWSDLDir) {
        this.mockWSDLDir = mockWSDLDir;
    }

    public String getSoapUIProjectFileName() {
        return soapUIProjectFileName;
    }

    public void setSoapUIProjectFileName(String soapUIProjectFileName) {
        this.soapUIProjectFileName = soapUIProjectFileName;
    }

    public String getProjectArtifacId() {
        return projectName;
    }

    public void setProjectArtifacId(String projectArtifacId) {
        this.projectName = projectArtifacId;
    }

    public String[] getClientWSDLFiles() {
        return testWSDLFiles;
    }

    public void setClientWSDLFiles(String[] clientWSDLFiles) {
        this.testWSDLFiles = clientWSDLFiles;
    }

    public String[] getMockWSDLFiles() {
        return mockWSDLFiles;
    }

    public void setMockWSDLFiles(String[] mockWSDLFiles) {
        this.mockWSDLFiles = mockWSDLFiles;
    }

    public String getInitialMockPort() {
        return initialMockPort;
    }

    public void setInitialMockPort(String initialMockPort) {
        this.initialMockPort = initialMockPort;
    }

    public String getTestPropertiesFile() {
        return testPropertiesFile;
    }

    public void setTestPropertiesFile(String testPropertiesFile) {
        this.testPropertiesFile = testPropertiesFile;
    }
}
