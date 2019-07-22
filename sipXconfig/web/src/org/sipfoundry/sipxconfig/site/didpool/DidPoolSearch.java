package org.sipfoundry.sipxconfig.site.didpool;

public class DidPoolSearch {

    public enum Mode {
        NONE, EXTENSION, DIDEXTENSION
    }

    private Mode m_mode = Mode.NONE;
    private String[] m_term = new String[] {
        ""
    };
    private String m_order;
    private boolean m_ascending = true;

    public void setMode(Mode mode) {
        if (mode == null) {
            m_mode = Mode.NONE;
        } else {
            m_mode = mode;
        }
    }

    public Mode getMode() {
        return m_mode;
    }

    public void setTerm(String[] term) {
        m_term = term;
    }

    public String[] getTerm() {
        return m_term;
    }

    public void setOrder(String order, boolean ascending) {
        m_order = order;
        m_ascending = ascending;
    }

    public boolean isSearch() {
        return m_mode != Mode.NONE;
    }
}
