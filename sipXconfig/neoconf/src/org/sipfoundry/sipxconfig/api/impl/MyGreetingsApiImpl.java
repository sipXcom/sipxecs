/**
 * Copyright (c) 2017 eZuce, Inc. All rights reserved.
 * Contributed to sipXcom under a Contributor Agreement
 *
 * This software is free software; you can redistribute it and/or modify it under
 * the terms of the Affero General Public License (AGPL) as published by the
 * Free Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 */
package org.sipfoundry.sipxconfig.api.impl;

import static java.lang.String.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.sipfoundry.sipxconfig.api.MyGreetingsApi;
import org.sipfoundry.sipxconfig.common.SimpleCommandRunner;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.MessageSource;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;

public class MyGreetingsApiImpl extends PromptsApiImpl implements MyGreetingsApi {

    private static final String TEMP = "tmp";
    private static final String SIPXCOM = "sipXcom";
    private static final String EXIT_CODE = "Exit code ";
    private static final String END_LINE = ".\n";
    private static final String DOT = ".";
    private static final String PROPERTIES_EXT = "properties";
    private static final String FILENAME_PROP = "filename";
    private static final String USER_QUERY = "metadata.user";

    private String m_mailstorePath;
    private String m_commandReplace;
    private String m_commandReplaceWithFilename;
    private String m_commandGetMigratedFilename;
    private String m_commandDelete;
    private int m_commandTimeout = 5000;
    private MessageSource m_messages;
    private MongoTemplate m_vmdbTemplate;

    @Override
    public Response uploadGreeting(String name, String extension, Attachment attachment, HttpServletRequest request) {
        return uploadGreeting(name, extension, attachment, request, false);
    }

    @Override
    public Response uploadGreetingSetNewFilename(String name, String extension, Attachment attachment,
        HttpServletRequest request) {
        return uploadGreeting(name, extension, attachment, request, true);
    }

    @Override
    public Response removeGreeting(String name, String extension, HttpServletRequest request) {
        if (!isSipxcom(request)) {
            //remove uploaded greeting
            SimpleCommandRunner commandRunner = new SimpleCommandRunner();
            String command = format(m_commandDelete,
                extension, name, getCurrentUser().getUserName());
            commandRunner.setRunParameters(command, m_commandTimeout);
            commandRunner.run();
            Integer exitCode = commandRunner.getExitCode();
            if (exitCode != 0) {
                StringBuilder msg = new StringBuilder();
                msg.append(EXIT_CODE).append(exitCode).append(END_LINE);
                msg.append(commandRunner.getStderr());
                return Response.serverError().entity(msg.toString()).build();
            }

            return Response.ok().build();
        } else {
            String absoluteFilePath = getAbsoluteFilePath(name, extension);
            try {
                FileUtils.deleteQuietly(new File(absoluteFilePath));
                FileUtils.deleteQuietly(new File(getAbsoluteFilePath(name, PROPERTIES_EXT)));
                return Response.ok().build();
            } catch (Exception ex) {
                return Response.serverError().entity(ex.getMessage()).build();
            }
        }
    }

    @Override
    public Response streamGreeting(String name, String extension, HttpServletRequest request) {
        if (!isSipxcom(request)) {
            DB vmDb = m_vmdbTemplate.getDb();
            GridFS vmFS = new GridFS(vmDb);
            String fileName = (new StringBuilder().append(name).append(DOT).append(extension)).toString();
            DBObject query = QueryBuilder.start(USER_QUERY).is(getCurrentUser().getUserName()).
                and(FILENAME_PROP).is(fileName).get();
            GridFSDBFile promptFile = vmFS.findOne(query);

            if (promptFile != null) {
                return ResponseUtils.buildStreamFileResponse(promptFile.getInputStream(),
                    promptFile.getLength(), promptFile.getContentType());
            } else {
                return Response.serverError().entity("File not found").build();
            }

        } else {
            File greetingFile = new File(getAbsoluteFilePath(name, extension));
            setPath(greetingFile.getParent());
            return streamPrompt(greetingFile.getName());
        }
    }

    @Override
    public Response isCustomGreeting(String name, String extension, HttpServletRequest request) {
        boolean exists = false;
        if (!isSipxcom(request)) {
            DB vmDb = m_vmdbTemplate.getDb();
            GridFS vmFS = new GridFS(vmDb);
            String fileName = (new StringBuilder().append(name).append(DOT).append(extension)).toString();
            DBObject query = QueryBuilder.start(USER_QUERY).is(getCurrentUser().getUserName()).
                and(FILENAME_PROP).is(fileName).get();
            GridFSDBFile promptFile = vmFS.findOne(query);
            exists = promptFile != null ? true : false;
        } else {
            File greetingFile = new File(getAbsoluteFilePath(name, extension));
            exists = greetingFile.exists() ? true : false;
        }
        return exists ? Response.ok().entity("{\"exists\":true}").build()
            : Response.ok().entity("{\"exists\":false}").build();
    }

    @Override
    public Response getGreetingNewFilename(String name, String extension, HttpServletRequest request) {
        if (!isSipxcom(request)) {
            SimpleCommandRunner commandRunner = new SimpleCommandRunner();

            String command = format(m_commandGetMigratedFilename, name, extension, getCurrentUser().getUserName());
            commandRunner.setRunParameters(command, m_commandTimeout);
            commandRunner.run();
            Integer exitCode = commandRunner.getExitCode();
            if (exitCode != 0) {
                StringBuilder msg = new StringBuilder();
                msg.append(EXIT_CODE).append(exitCode).append(END_LINE);
                msg.append(commandRunner.getStderr());
                return Response.serverError().entity(msg.toString()).build();
            }

            return Response.ok().entity(commandRunner.getStdout()).build();
        } else {
            try {
                Properties props = new Properties();
                File greetingFileName = new File(getAbsoluteFilePath(name, PROPERTIES_EXT));
                props.load(new FileInputStream(greetingFileName));
                return Response.ok().entity(props.get(FILENAME_PROP)).build();
            } catch (Exception ex) {
                return Response.ok().entity("null").build();
            }
        }
    }

    private Response uploadGreeting(String name, String extension, Attachment attachment,
        HttpServletRequest request, boolean setNewFilename) {
        //make sure to clear temp path in case previous upload failed
        FileUtils.deleteQuietly(new File(getGreetingTempPath()));

        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(attachment);
        setPath(getGreetingTempPath());
        String uploadedFileName = getFileNameFromContentDisposition(attachment
            .getHeader(ResponseUtils.CONTENT_DISPOSITION));
        String absoluteUploadedFilePath = new StringBuilder().
            append(getPath()).
            append(File.separator).
            append(uploadedFileName).toString();
        Response response = super.uploadPrompts(attachments, request);
        if (response.getStatus() == 200) {
            if (!isSipxcom(request)) {
                //replicate to mongo GridFS if available

                SimpleCommandRunner commandRunner = null;
                String command = null;

                commandRunner = new SimpleCommandRunner();
                String commandName = setNewFilename ? m_commandReplaceWithFilename : m_commandReplace;
                command = format(commandName, absoluteUploadedFilePath, extension,
                    name, getCurrentUser().getUserName());
                commandRunner.setRunParameters(command, m_commandTimeout);
                commandRunner.run();
                Integer exitCode = commandRunner.getExitCode();
                if (exitCode != 0) {
                    StringBuilder msg = new StringBuilder();
                    msg.append(EXIT_CODE).append(exitCode).append(END_LINE);
                    msg.append(commandRunner.getStderr());
                    return Response.serverError().entity(msg.toString()).build();
                }
                FileUtils.deleteQuietly(new File(getPath()));

                return Response.ok().build();
            } else {
                String absoluteFilePath = getAbsoluteFilePath(name, extension);
                try {
                    FileUtils.deleteQuietly(new File(absoluteFilePath));
                    File uploadedFile = new File(absoluteUploadedFilePath);
                    FileUtils.moveFile(uploadedFile, new File(absoluteFilePath));
                    if (setNewFilename) {
                        Properties newFileProp = new Properties();
                        newFileProp.setProperty(FILENAME_PROP, uploadedFile.getName());
                        FileOutputStream out = new FileOutputStream(getGreetingPath()
                            + File.separator + name + DOT + PROPERTIES_EXT);
                        newFileProp.store(out, null);
                        out.close();
                    }
                    return Response.ok().build();
                } catch (Exception ex) {
                    return Response.serverError().entity(ex.getMessage()).build();
                }
            }
        }
        return response;
    }

    private String getGreetingTempPath() {
        String basePath = m_mailstorePath;
        StringBuilder builder = new StringBuilder();
        builder.append(basePath)
               .append(File.separator)
               .append(getCurrentUser().getUserName())
               .append(File.separator)
               .append(TEMP);
        String greetingPath = builder.toString();
        File f = new File(greetingPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        return builder.toString();
    }

    private String getGreetingPath() {
        String basePath = m_mailstorePath;
        StringBuilder builder = new StringBuilder();
        builder.append(basePath)
               .append(File.separator)
               .append(getCurrentUser().getUserName());
        String greetingPath = builder.toString();
        File f = new File(greetingPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        return builder.toString();
    }

    private String getAbsoluteFilePath(String name, String extension) {
        StringBuilder absoluteOrigFilePath = new StringBuilder().
            append(getGreetingPath()).
            append(File.separator).
            append(name).
            append(DOT).
            append(extension);
        return absoluteOrigFilePath.toString();
    }

    private boolean isSipxcom(HttpServletRequest request) {
        return m_messages.getMessage("product.name", null, request.getLocale()).equalsIgnoreCase(SIPXCOM);
    }

    @Required
    public void setMailstorePath(String mailstorePath) {
        m_mailstorePath = mailstorePath;
    }

    @Required
    public void setCommandReplace(String commandReplace) {
        m_commandReplace = commandReplace;
    }

    @Required
    public void setCommandReplaceWithFilename(String commandReplaceWithFilename) {
        m_commandReplaceWithFilename = commandReplaceWithFilename;
    }

    @Required
    public void setCommandDelete(String commandDelete) {
        m_commandDelete = commandDelete;
    }

    @Required
    public void setMessages(MessageSource messages) {
        m_messages = messages;
    }

    @Required
    public void setCommandGetMigratedFilename(String commandGetMigratedFilename) {
        m_commandGetMigratedFilename = commandGetMigratedFilename;
    }

    public void setVmdbTemplate(MongoTemplate vmdbTemplate) {
        m_vmdbTemplate = vmdbTemplate;
    }
}
