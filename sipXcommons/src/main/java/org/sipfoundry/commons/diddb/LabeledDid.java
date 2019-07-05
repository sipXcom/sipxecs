package org.sipfoundry.commons.diddb;

public class LabeledDid extends Did {

    private static final long serialVersionUID = 1L;
    
    private String m_typeLabel;
    
    private String m_description;

    public LabeledDid(String type, String typeId, String value, String poolId) {
        super(type, typeId, value, poolId);
    }

    public void setTypeLabel(String typeLabel) {
        m_typeLabel = typeLabel;
    }

    public String getTypeLabel() {
        return m_typeLabel;
    }

    public String getDescription() {
        return m_description;
    }

    public void setDescription(String description) {
        m_description = description;
    }
}
