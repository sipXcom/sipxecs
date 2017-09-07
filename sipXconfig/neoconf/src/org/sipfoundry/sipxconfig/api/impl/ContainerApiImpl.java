package org.sipfoundry.sipxconfig.api.impl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.sipfoundry.sipxconfig.api.ContainerApi;
import org.sipfoundry.sipxconfig.api.SipxecsExceptionMapper;
import org.sipfoundry.sipxconfig.api.model.ContainerBean;
import org.sipfoundry.sipxconfig.api.model.ContainersBean;

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

    @Override
    public List<ContainersBean> getContainersBeans(int all) {
        List<ContainersBean> beans = null;
        try {
            beans = m_service.getContainersBeans(all);
        } catch (Exception ex) {
            LOG.error("Cannot retrieve containers ", ex);
        }
        return beans;
    }

    @Override
    public Response getContainers() {
        List<ContainersBean> beans = getContainersBeans(0);
        if (beans != null) {
            return Response.ok().entity(beans).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
    @Override
    public Response getAllContainers() {
        List<ContainersBean> beans = getContainersBeans(1);
        if (beans != null) {
            return Response.ok().entity(beans).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }    

    @Override
    public Response restartContainer(String containerName) {        
        return m_service.restartContainer(containerName);
    }
}
