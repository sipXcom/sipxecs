/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.sip;

import static org.apache.commons.lang.StringUtils.containsIgnoreCase;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipListener;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.header.ReasonHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class SipListenerImpl implements SipListener {

    private static final Log LOG = LogFactory.getLog(SipListenerImpl.class);

    private SipStackBean m_stackBean;

    public SipListenerImpl(SipStackBean stackBean) {
        m_stackBean = stackBean;
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {

    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        try {
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();
            if (serverTransaction == null) {
                LOG.debug("processRequest : NULL ServerTransaction -- dropping request");
                return;
            }
            LOG.debug("SipListenerImpl: processing incoming request "
                    + serverTransaction.getRequest().getMethod());
            Request request = requestEvent.getRequest();
            if (request.getMethod().equals(Request.BYE)) {
                Response response = m_stackBean.createResponse(request, Response.OK);
                serverTransaction.sendResponse(response);
            } else if (request.getMethod().equals(Request.NOTIFY)) {
                LOG.debug("got a NOTIFY");
                Response response = m_stackBean.createResponse(request, Response.OK);
                serverTransaction.sendResponse(response);
                SubscriptionStateHeader subscriptionState =
                        (SubscriptionStateHeader) request.getHeader(SubscriptionStateHeader.NAME);
                boolean terminated = subscriptionState.getState().equalsIgnoreCase(SubscriptionStateHeader.TERMINATED);
                String content = new String(request.getRawContent());
                boolean ringing = containsIgnoreCase(content, SipStackBean.RINGING_MESSAGE);
                boolean sessionProgress = containsIgnoreCase(content, SipStackBean.SESSION_PROGRESS);
                LOG.debug("Send BYE - Ringing " + ringing
                    + " Subscription state terminated " + terminated + " Session in progress " + sessionProgress);
                if (ringing || terminated || sessionProgress) {
                    Dialog dialog = requestEvent.getDialog();
                    ReasonHeader busyHeader = null;
                    if (containsIgnoreCase(content, SipStackBean.BUSY_MESSAGE)) {
                        busyHeader = m_stackBean.createBusyReasonHeader(request.getSIPVersion());
                    }
                    m_stackBean.tearDownDialog(dialog, busyHeader);
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception  " + requestEvent.getRequest(), ex);
            Dialog dialog = requestEvent.getDialog();
            m_stackBean.tearDownDialog(dialog);
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        try {
            ClientTransaction clientTransaction = responseEvent.getClientTransaction();
            if (clientTransaction == null) {
                LOG.debug("clientTransaction NULL -- dropping response ");
                return;
            }
            TransactionApplicationData tad = (TransactionApplicationData) clientTransaction
                    .getApplicationData();
            if (tad != null) {
                tad.response(responseEvent);
            } else {
                LOG.debug("transaction application data is null!");
            }
        } catch (Exception ex) {
            LOG.error("Error occured processing response", ex);
            Dialog dialog = responseEvent.getDialog();
            if (dialog != null) {
                m_stackBean.tearDownDialog(dialog);
            }
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        if (!timeoutEvent.isServerTransaction()) {
            ClientTransaction clientTransaction = timeoutEvent.getClientTransaction();

            TransactionApplicationData tad = (TransactionApplicationData) clientTransaction
                    .getApplicationData();
            if (tad != null) {
                tad.timeout(timeoutEvent);
            } else {
                Dialog dialog = clientTransaction.getDialog();
                if (dialog != null) {
                    dialog.delete();
                }

            }

        }
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
    }
}
