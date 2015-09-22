/**
 * Copyright (C) 2015 sipXcom, certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 */
package org.sipxcom.sipxconfig.pojo;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Entity {
    @Id
    private String id;

    @Indexed
    private String uid;

    private String pntk;

    private String ident;

    private String pstk;

    @Indexed
    private List<String> phLines;

    @Indexed
    private String ent;

    private String mac;

    @Indexed
    private String model;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPntk() {
        return pntk;
    }

    public void setPntk(String pntk) {
        this.pntk = pntk;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getPstk() {
        return pstk;
    }

    public void setPstk(String pstk) {
        this.pstk = pstk;
    }

    public String getServerName() {
        return StringUtils.split(ident, '@')[1];
    }

    public List<String> getPhLines() {
        return phLines;
    }

    public void setPhLines(List<String> phLines) {
        this.phLines = phLines;
    }

    public String getEnt() {
        return ent;
    }

    public void setEnt(String ent) {
        this.ent = ent;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }
}
