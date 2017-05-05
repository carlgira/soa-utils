package com.carlgira.oracle.bpel.flowchart.managers;

/**
 * Created by cgiraldo on 04/05/2017.
 */
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.*;

import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;

import oracle.bpm.client.BPMServiceClientFactory;
import oracle.bpm.services.common.exception.BPMException;
import oracle.bpm.services.instancequery.IAuditInstance;
import oracle.bpm.services.instancequery.IInstanceQueryService;
import oracle.bpm.services.internal.processmodel.model.IProcessModelPackage;
import oracle.bpm.ui.Image;
import oracle.bpm.ui.utils.ImageExtension;
import oracle.bpm.ui.utils.ImageIOFacade;

public class AuditProcessImage1 {
    public AuditProcessImage1() {
        super();
    }

    /**
     * get BPMServiceClientFactory from property file
     * @return BPMServiceClientFactory
     */
    public static BPMServiceClientFactory getBPMServiceClientFactory() {

        Properties prop = new Properties();
        try {
            ClassLoader classLoader = AuditProcessImage1.class.getClassLoader();
            prop.load(classLoader.getResource("bpel-flowchart-preview.properties").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String soaURL = "t3://localhost:7003";
        String user = "weblogic";
        String password = "WELCOME1";

        Map<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String> properties =
                new HashMap<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String>();
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.CLIENT_TYPE,
                WorkflowServiceClientFactory.REMOTE_CLIENT);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_PROVIDER_URL, soaURL);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_PRINCIPAL,
                user);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_CREDENTIALS,
                password);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_INITIAL_CONTEXT_FACTORY,
                "weblogic.jndi.WLInitialContextFactory");
        return BPMServiceClientFactory.getInstance(properties, null, null);
    }

    /**
     * Main method for test application.
     */
    public static void main(String[] args) {
        // Argument check
        // [0] Instance ID [1] Output File Path (In case of Windows, seperator of "\\" is required) [2] Audit|Process

        String instanceId = "10011";
        String outputPath = "d://file.jpg";

        try {
            // Connect to BPM Server
            // get BPMContext and create Utility Class instance
            // Locale is fixed to ja_JP in this sample.
            BPMServiceClientFactory bpmServiceClientFactory = getBPMServiceClientFactory();
            IBPMContext bpmContext =
                    bpmServiceClientFactory.getBPMUserAuthenticationService().getBPMContextForAuthenticatedUser();
            IInstanceQueryService instanceQueryService =
                    bpmServiceClientFactory.getBPMServiceClient().getInstanceQueryService();


            String r = instanceQueryService.getAuditInstancePayloadXML(bpmContext, 2L);
            System.out.println("AAAAA " + r);


                // get a list of the audit events that have occurred in this instance
                List<IAuditInstance> auditInstances =
                        bpmServiceClientFactory.getBPMServiceClient().getInstanceQueryService().queryAuditInstanceByProcessId(bpmContext,
                                instanceId);
                for (IAuditInstance a1 : auditInstances) {
                    System.out.println(a1.getActivityName());
                    System.out.println(a1.getCompositeInstanceId());
                    System.out.println(a1.getLabel());
                    System.out.println(a1.getCreateTime().getTime());
                    System.out.println(a1.toString());
                    System.out.println("---------------------------");
                }

/*
            String Base64 = instanceQueryService.getProcessAuditDiagram(bpmContext, instanceId, Locale.JAPAN);

            Image image = Image.createFromBase64(Base64);
            BufferedImage bufferedImage = (BufferedImage) image.asAwtImage();

            try( ByteArrayOutputStream auditImageOutputStream = new ByteArrayOutputStream()) {
                ImageIOFacade.writeImage(bufferedImage, ImageExtension.PNG, auditImageOutputStream);
                try (InputStream diagram = new ByteArrayInputStream(auditImageOutputStream.toByteArray())) {
                    writeToFile(diagram, outputPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
*/
        } catch (BPMException e) {
            e.printStackTrace();
        }

    }

    private static void writeToFile(InputStream istream, String outputFilePath) throws IOException {
        try (OutputStream out = new FileOutputStream(outputFilePath)) {
            // output file
            byte[] buf = new byte[1024];
            int len;
            while ((len = istream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}