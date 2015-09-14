/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sipfoundry.commons.userdb.profile.UserProfileService;

public class UserAvatarResource extends Resource {
    private static final Log LOG = LogFactory.getLog(UserAvatarResource.class);

    private String m_userName;
    private UserProfileService m_avatarService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_ALL));
        getVariants().add(new Variant(MediaType.APPLICATION_OCTET_STREAM));
        m_userName = (String) getRequest().getAttributes().get("user");
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        return new AvatarRepresentation(MediaType.IMAGE_PNG, m_avatarService.getAvatar(m_userName));
    }

    //POST
    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        if (entity != null) {
            if (MediaType.MULTIPART_FORM_DATA.equals(entity.getMediaType(), true)) {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(1000240);

                RestletFileUpload upload = new RestletFileUpload(factory);

                List<FileItem> fileList = null;
                try {
                    fileList = upload.parseRepresentation(entity);
                } catch (FileUploadException e) {
                    LOG.error("Cannot parse representation", e);
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
                }
                if (fileList == null || fileList.size() != 1) {
                    LOG.error("Wrong file list size. You have to send one file avatar to upload");
                    throw new ResourceException(Status.CLIENT_ERROR_EXPECTATION_FAILED,
                            "Zero or more than one avatar to upload");
                }

                try {
                    m_avatarService.saveAvatar(m_userName, fileList.get(0).getInputStream());
                } catch (Exception e) {
                    LOG.error("Cannot upload avatar", e);
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            }
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    static class AvatarRepresentation extends OutputRepresentation {

        private InputStream m_is;
        public AvatarRepresentation(MediaType mediaType, InputStream is) {
            super(mediaType);
            m_is = is;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            if (m_is != null) {
                IOUtils.copy(m_is, outputStream);
            }
            IOUtils.closeQuietly(m_is);
        }
    }

    public void setUserAvatarService(UserProfileService service) {
        m_avatarService = service;
    }

}
