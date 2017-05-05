package com.carlgira.oracle.bpel.flowchart;

/**
 * Created by cgiraldo on 05/05/2017.
 */
import java.awt.image.BufferedImage;

import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import com.carlgira.util.ServerConnection;
import oracle.bpel.services.bpm.common.IBPMContext;
import oracle.bpel.services.workflow.client.IWorkflowServiceClientConstants;
import oracle.bpel.services.workflow.client.WorkflowServiceClientFactory;

import oracle.bpm.client.BPMServiceClientFactory;
import oracle.bpm.services.common.exception.BPMException;
import oracle.bpm.services.instancequery.IInstanceQueryService;
import oracle.bpm.ui.Image;
import oracle.bpm.ui.utils.ImageExtension;
import oracle.bpm.ui.utils.ImageIOFacade;

public class AuditProcessImage {

    private ServerConnection serverConnection;

    public AuditProcessImage(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    /**
     * get BPMServiceClientFactory from property file
     * @return BPMServiceClientFactory
     */
    public BPMServiceClientFactory getBPMServiceClientFactory() {



        Map<IWorkflowServiceClientConstants.CONNECTION_PROPERTY, String> properties;
        properties = new EnumMap<>(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.class);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.CLIENT_TYPE,
                WorkflowServiceClientFactory.REMOTE_CLIENT);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_PROVIDER_URL, this.serverConnection.server);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_PRINCIPAL,
                this.serverConnection.adminUser);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_SECURITY_CREDENTIALS,
                this.serverConnection.adminPassword);
        properties.put(IWorkflowServiceClientConstants.CONNECTION_PROPERTY.EJB_INITIAL_CONTEXT_FACTORY,
                "weblogic.jndi.WLInitialContextFactory");
        return BPMServiceClientFactory.getInstance(properties, null, null);
    }

    static enum IMAGE_TYPE {
        PROCESS,
        AUDIT,
    };

    /**
     * Main method for test application.
     */
    public byte[] getBpmDiagrama(String instanceId, String type) {
        IMAGE_TYPE imageType;
        if (type.equalsIgnoreCase("Process")) {
            imageType = IMAGE_TYPE.PROCESS;
        } else if (type.equalsIgnoreCase("Audit")) {
            imageType = IMAGE_TYPE.AUDIT;
        } else {
            imageType = null;
            System.err.println("Specify \"Audit\", or \"Process\" as 3rd argument.");
            System.exit(1);
        }

        BPMServiceClientFactory bpmServiceClientFactory = getBPMServiceClientFactory();
        IBPMContext bpmContext;
        String Base64 = null;
        try {
            bpmContext = bpmServiceClientFactory.getBPMUserAuthenticationService().getBPMContextForAuthenticatedUser();
            IInstanceQueryService instanceQueryService =
                    bpmServiceClientFactory.getBPMServiceClient().getInstanceQueryService();
            if (imageType.equals(IMAGE_TYPE.PROCESS)) {
                Base64 = instanceQueryService.getProcessDiagram(bpmContext, instanceId, Locale.US);
            } else if (imageType.equals(IMAGE_TYPE.AUDIT)) {
                Base64 = instanceQueryService.getProcessAuditDiagram(bpmContext, instanceId, Locale.US);
            }
        } catch (BPMException e) {
            e.printStackTrace();
        }

        try {
            Image image = Image.createFromBase64(Base64);
            BufferedImage bufferedImage = (BufferedImage) image.asAwtImage();


            WritableRaster raster = bufferedImage .getRaster();
            DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

            return data.getData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}