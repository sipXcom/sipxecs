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

public class MyGreetingsApiImpl extends PromptsApiImpl implements MyGreetingsApi {

    private static final String TEMP = "tmp";
    private static final String SIPXCOM = "sipXcom";
    private static final String EXIT_CODE = "Exit code ";
    private static final String END_LINE = ".\n";
    private static final String DOT = ".";
    private static final String PROPERTIES_EXT = "properties";
    private static final String FILENAME_PROP = "filename";

    private String m_mailstorePath;
    private String m_commandReplace;
    private String m_commandReplaceWithFilename;
    private String m_commandCopy;
    private String m_commandGetMigratedFilename;
    private int m_commandTimeout = 5000;
    private MessageSource m_messages;

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
        String absoluteCopyOrigFilePath = getAbsoluteCopyOrigFilePath(name, extension);
        File original = new File(absoluteCopyOrigFilePath);
        if (original.exists()) {
            if (!isSipxcom(request)) {
                //remove uploaded greeting (replace with the original one)
                SimpleCommandRunner commandRunner = new SimpleCommandRunner();
                String command = format(m_commandReplace, absoluteCopyOrigFilePath,
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
                    FileUtils.moveFile(new File(absoluteCopyOrigFilePath), new File(absoluteFilePath));
                    return Response.ok().build();
                } catch (Exception ex) {
                    return Response.serverError().entity(ex.getMessage()).build();
                }
            }
        } else {
            StringBuilder msg = new StringBuilder();
            msg.append("{\"error\":\"");
            msg.append("No initial greeting to replace with");
            msg.append("\"}");
            return Response.serverError().entity(msg.toString()).build();
            //return Response.serverError().entity("No initial greeting to replace with").build();
        }
    }

    @Override
    public Response streamGreeting(String name, String extension, HttpServletRequest request) {
        if (!isSipxcom(request)) {
            String absoluteActualFilePath = getAbsoluteActualFilePath(name, extension);
            SimpleCommandRunner commandRunner = new SimpleCommandRunner();
            String command = format(m_commandCopy, name, extension,
                getCurrentUser().getUserName(), absoluteActualFilePath);
            commandRunner.setRunParameters(command, m_commandTimeout);
            commandRunner.run();
            Integer exitCode = commandRunner.getExitCode();
            if (exitCode != 0) {
                StringBuilder msg = new StringBuilder();
                msg.append(EXIT_CODE).append(exitCode).append(END_LINE);
                msg.append(commandRunner.getStderr());
                return Response.serverError().entity(msg.toString()).build();
            }
            File greetingFile = new File(absoluteActualFilePath);
            setPath(greetingFile.getParent());
            return streamPrompt(greetingFile.getName());
        } else {
            File greetingFile = new File(getAbsoluteFilePath(name, extension));
            setPath(greetingFile.getParent());
            return streamPrompt(greetingFile.getName());
        }
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
                //backup the initial greeting if not already backup-ed
                File original = new File(getAbsoluteCopyOrigFilePath(name, extension));
                SimpleCommandRunner commandRunner = null;
                String command = null;
                if (!original.exists()) {
                    commandRunner = new SimpleCommandRunner();
                    command = format(m_commandCopy, name, extension, getCurrentUser().getUserName(), original);
                    commandRunner.setRunParameters(command, m_commandTimeout);
                    commandRunner.run();
                }

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
                String absoluteCopyOrigFilePath = getAbsoluteCopyOrigFilePath(name, extension);
                try {
                    File copyOrig = new File(absoluteCopyOrigFilePath);
                    File orig = new File(absoluteFilePath);
                    //backup the initial greeting before upload
                    if (!copyOrig.exists() && orig.exists()) {
                        FileUtils.moveFile(new File(absoluteFilePath), copyOrig);
                    }
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

    private String getAbsoluteCopyOrigFilePath(String name, String extension) {
        StringBuilder absoluteCopyOrigFilePath = new StringBuilder().
            append(getGreetingPath()).
            append(File.separator).
            append(name).
            append("_orig").
            append(DOT).
            append(extension);
        return absoluteCopyOrigFilePath.toString();
    }

    private String getAbsoluteActualFilePath(String name, String extension) {
        StringBuilder absoluteActualFilePath = new StringBuilder().
            append(getGreetingPath()).
            append(File.separator).
            append(name).
            append("_actual").
            append(DOT).
            append(extension);
        return absoluteActualFilePath.toString();
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
    public void setCommandCopy(String commandCopy) {
        m_commandCopy = commandCopy;
    }

    @Required
    public void setMessages(MessageSource messages) {
        m_messages = messages;
    }

    @Required
    public void setCommandGetMigratedFilename(String commandGetMigratedFilename) {
        m_commandGetMigratedFilename = commandGetMigratedFilename;
    }
}
