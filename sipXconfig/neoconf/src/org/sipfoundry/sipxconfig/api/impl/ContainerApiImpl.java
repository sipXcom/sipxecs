package org.sipfoundry.sipxconfig.api.impl;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.sipfoundry.sipxconfig.api.ContainerApi;
import org.sipfoundry.sipxconfig.api.SipxecsExceptionMapper;
import org.sipfoundry.sipxconfig.api.model.ContainerBean;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ContainerApiImpl implements ContainerApi {
    private static final Log LOG = LogFactory.getLog(ContainerApiImpl.class);
    ContainerApi m_service;
    
    public void init() {                                
        m_service = JAXRSClientFactory.create("http://nginx:81/docker", ContainerApi.class,
            Arrays.asList(new JacksonJsonProvider(), new JacksonJaxbJsonProvider(), new SipxecsExceptionMapper()));
    }

    @Override
    public ContainerBean getContainerBean(String containerName) {
        ContainerBean bean = null;
        try {
            bean = m_service.getContainerBean(containerName);
        }
        catch (Exception ex) {
            LOG.error("Cannot retrieve container data ", ex);
        }
        
        return bean;
    }

    @Override
    public Response getContainer(String containerName) {
        ContainerBean bean = getContainerBean(containerName);
        if (bean != null) {
            return Response.ok().entity(bean).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
}
