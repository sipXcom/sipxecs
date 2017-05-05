package org.sipfoundry.sipxconfig.cdr;

/**
 * Maps Active call retrieved from callresolver active calls REST service
 */
public class ActiveCallREST {
    private final String m_from;
    private final String m_to;
    private final String m_recipient;
    private final long m_startTime;
    private final long m_duration;

    public ActiveCallREST(String from, String to, String recipient, long startTime, long duration) {
        m_from = from;
        m_to = to;
        m_recipient = recipient;
        m_startTime = startTime;
        m_duration = duration;
    }

    public String getFrom() {
        return m_from;
    }

    public String getTo() {
        return m_to;
    }

    public String getRecipient() {
        return m_recipient;
    }

    public long getStartTime() {
        return m_startTime;
    }

    public long getDuration() {
        return m_duration;
    }
}
