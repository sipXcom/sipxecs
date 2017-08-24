package org.sipfoundry.sipxconfig.api.impl;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.sipfoundry.sipxconfig.api.ContainerApi;
import org.sipfoundry.sipxconfig.api.model.ContainerBean;

public class ContainerApiImpl implements ContainerApi {
    ContainerApi m_service;

    public void init() {
        m_service = JAXRSClientFactory.create("http://docker", ContainerApi.class);
    }

    @Override
    public ContainerBean getContainer(String containerName) {
        return m_service.getContainer("sipxproxy");
    }
}
