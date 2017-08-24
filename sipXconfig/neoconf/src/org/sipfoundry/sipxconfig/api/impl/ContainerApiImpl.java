package org.sipfoundry.sipxconfig.api.impl;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.sipfoundry.sipxconfig.api.ContainerApi;
import org.sipfoundry.sipxconfig.api.model.ContainerBean;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ContainerApiImpl implements ContainerApi {
    ContainerApi m_service;

    public void init() {
        m_service = JAXRSClientFactory.create("http://nginx/docker", ContainerApi.class);
    }

    @Override
    public ContainerBean getContainerBean(String containerName) {
        return m_service.getContainerBean(containerName);
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
