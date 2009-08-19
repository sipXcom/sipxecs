/*
 *
 *
 * Copyright (C) 2008 Pingtel Corp., certain elements licensed under a Contributor Agreement.  
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 * 
 *
 */
package org.sipfoundry.callcontroller;

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
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

class SipListenerImpl implements SipListener {

    private static final Logger LOG = Logger.getLogger(SipListenerImpl.class);

    private SipStackBean m_stackBean;

    public SipListenerImpl(SipStackBean stackBean) {
        m_stackBean = stackBean;
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

        Dialog dialog = dialogTerminatedEvent.getDialog();
        DialogContext context = (DialogContext) dialog.getApplicationData();
        if (context != null) {
            context.remove();
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {

    }

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
                SubscriptionStateHeader subscriptionState = (SubscriptionStateHeader) request
                        .getHeader(SubscriptionStateHeader.NAME);
                Dialog dialog = requestEvent.getDialog();

                DialogContext dialogContext = (DialogContext) dialog.getApplicationData();
                if (request.getContentLength().getContentLength() != 0) {
                    String statusLine = new String(request.getRawContent());
                    LOG.debug("dialog = " + dialog);
                    LOG.debug("status line = " + statusLine);
                    if (!statusLine.equals("") && dialogContext != null) {
                        dialogContext.setStatus(statusLine);
                    }
                }

                if (subscriptionState.getState().equalsIgnoreCase(
                        SubscriptionStateHeader.TERMINATED)) {
                    m_stackBean.tearDownDialog(dialog);
                }
            }
        } catch (Exception ex) {
            LOG.error("Exception  " + requestEvent.getRequest(), ex);
            Dialog dialog = requestEvent.getDialog();
            m_stackBean.tearDownDialog(dialog);
        }
    }

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

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
    }
}
