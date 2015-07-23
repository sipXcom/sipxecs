package org.sipfoundry.sipxconfig.api.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.activation.DataHandler;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.sipfoundry.sipxconfig.api.UserApi;
import org.sipfoundry.sipxconfig.common.CoreContext;
import org.sipfoundry.sipxconfig.common.User;
import org.sipfoundry.sipxconfig.setting.Setting;
import org.springframework.beans.factory.annotation.Required;

public class UserApiImpl implements UserApi {
    private static final String COMMA = ",";
    private static final Log LOG = LogFactory.getLog(UserApiImpl.class);
    private CoreContext m_coreContext;

    @Override
    public Response setUserSetting(String userName, String path, String value) {
        User user = m_coreContext.loadUserByUserName(userName);
        if (user != null) {
            user.setSettingValue(path, value);
            m_coreContext.saveUser(user);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response deleteUserSetting(String userName, String path) {
        User user = m_coreContext.loadUserByUserName(userName);
        if (user != null) {
            Setting setting = user.getSettings().getSetting(path);
            setting.setValue(setting.getDefaultValue());
            m_coreContext.saveUser(user);
            return Response.ok().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    @Required
    public void setCoreContext(CoreContext coreContext) {
        m_coreContext = coreContext;
    }

    @Override
    public Response setUsersSetting(List<Attachment> attachments, String path) {
        for (Attachment attachment : attachments) {
            DataHandler dataHandler = attachment.getDataHandler();
            InputStream inputStream = null;
            BufferedReader br = null;
            try{
                // parse csv file
                inputStream = dataHandler.getInputStream();
                br = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] userSetting = line.split(COMMA);
                    User user = m_coreContext.loadUserByUserName(userSetting[0]);
                    if (user != null) {
                        Setting setting = user.getSettings().getSetting(path);
                        setting.setValue(userSetting[1]);
                        LOG.debug("User : " + userSetting[0] + " " + userSetting[1]);
                        m_coreContext.saveUser(user);
                    }
                }
            }
            catch(Exception ex) {
                return Response.status(Status.EXPECTATION_FAILED).build();
            }
            finally {
                IOUtils.closeQuietly(br);
                IOUtils.closeQuietly(inputStream);
            }
        }
        return Response.ok("upload success").build();
    }
}
