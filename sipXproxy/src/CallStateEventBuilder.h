//
// Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
// Contributors retain copyright to elements licensed under a Contributor Agreement.
// Licensed to the User under the LGPL license.
//
//////////////////////////////////////////////////////////////////////////////

#ifndef _CallStateEventBuilder_h_
#define _CallStateEventBuilder_h_

// SYSTEM INCLUDES

class UtlString;
class OsTime;

// APPLICATION INCLUDES
// DEFINES
// MACROS
// EXTERNAL FUNCTIONS
// EXTERNAL VARIABLES
// CONSTANTS
// STRUCTS
// TYPEDEFS
// FORWARD DECLARATIONS

/**
 * The base class for any means of building a record of a call state event.
 * Classes derived from this one may build events in different representations for
 * different purposes.
 *
 * Before any call events are generated, an observer event indicating the start
 * of a new sequence event should be called:
 *  - observerEvent(0, universalEpochTime, ObserverReset, "AuthProxy Restart");
 *
 * Call Events are generated by a sequence of calls, begining with one of the call*Event
 * methods and ending with callEventComplete.  The specific calls required for each
 * event type are documented with the method that begins the sequence.
 *
 * This base class provides the
 */
class CallStateEventBuilder
{
/* //////////////////////////// PUBLIC //////////////////////////////////// */
  public:

/* ============================ CREATORS ================================== */

   /// Instantiate an event builder and set the observer name for its events
   CallStateEventBuilder(const char* observerDnsName ///< the DNS name to be recorded in all events
                         );

   /// Destructor
   virtual ~CallStateEventBuilder();


   /// Meta-events about the observer itself
   typedef enum
      {
         ObserverReset = 101 ///< starts a new sequence number stream
      } ObserverEvent;

   /**
    * Generate a metadata event.
    * This method generates a complete event - it does not require that the callEventComplete method be called.
    */
   virtual void observerEvent(int sequenceNumber, ///< for ObserverReset, this should be zero
                              const OsTime& timestamp,      ///< obtain using getCurTime(OsTime)
                              ObserverEvent eventCode,
                              const char* eventMsg ///< for human consumption
                              );

   /// Begin a Call Request Event - an INVITE without a to tag has been observed
   /**
    * Requires:
    *   - callRequestEvent
    *   - addCallData (the toTag in the addCallRequest will be a null string)
    *   - addEventVia (at least for via index zero)
    *   - completeCallEvent
    */
   virtual void callRequestEvent(int sequenceNumber,
                                 const OsTime& timestamp,      ///< obtain using getCurTime(OsTime)
                                 const UtlString& requestUri,
                                 const UtlString& contact,
                                 const UtlString& references,
                                 const UtlString& branch_id,
                                 int              via_count,
                                 const bool callerInternal
                                 );

   /// Begin a Call Setup Event - a 2xx response to an INVITE has been observed
   /**
    * Requires:
    *   - callSetupEvent
    *   - addCallData
    *   - addEventVia (at least for via index zero)
    *   - completeCallEvent
    */
   virtual void callSetupEvent(int sequenceNumber,
                               const OsTime& timestamp,      ///< obtain using getCurTime(OsTime)
                               const UtlString& contact,
                               const UtlString& calleeRoute,
                               const UtlString& branch_id,
                               int              via_count 
                               );

   /// Begin a Call Failure Event - an error response to an INVITE has been observed
   /**
    * Requires:
    *   - callFailureEvent
    *   - addCallData
    *   - addEventVia (at least for via index zero)
    *   - completeCallEvent
    */
   virtual void callFailureEvent(int sequenceNumber,
                                 const OsTime& timestamp,      ///< obtain using getCurTime(OsTime)
                                 const UtlString& branch_id,
                                 int via_count,
                                 int statusCode,
                                 const UtlString& statusMsg
                                 );

   /// Begin a Call End Event - a BYE request has been observed
   /**
    * Requires:
    *   - callEndEvent
    *   - addCallData
    *   - addEventVia (at least for via index zero)
    *   - completeCallEvent
    */
   virtual void callEndEvent(const int sequenceNumber,
                             const OsTime& timestamp      ///< obtain using getCurTime(OsTime)
                             );

   /// Begin a Call Transfer Event - a REFER request has been observed
   /**
    * Requires:
    *   - callEndEvent
    *   - addCallData
    *   - completeCallEvent
    */
   virtual void callTransferEvent(const int sequenceNumber,
                                  const OsTime& timestamp,   ///< obtain using getCurTime(OsTime)
                                  const UtlString& contact,
                                  const UtlString& refer_to,
                                  const UtlString& referred_by,
                                  const UtlString& request_uri
                                  );

   /// Add the dialog and call information for the event being built.
   virtual void addCallData(const int cseqNumber,
                            const UtlString& callId,
                            const UtlString& fromTag,  /// may be a null string
                            const UtlString& toTag,    /// may be a null string
                            const UtlString& fromField,
                            const UtlString& toField
                            );

   /// Add a via element for the event
   /**
    * Record a Via from the message for this event
    * Calls to this routine are in reverse cronological order - the last
    * call for an event should be the via added by the message originator
    */
   virtual void addEventVia(const UtlString& via
                            );

   /// Indicates that all information for the current call event has been added.
   virtual void completeCallEvent();

   virtual bool finishElement(UtlString& event) = 0;

/* //////////////////////////// PROTECTED ///////////////////////////////// */
  protected:
   const char* observerName;

   /**
    * Input events for the builderStateOk FSM.
    * Each of these events is passed to the state machine from the
    * correspondingly named public builder method.
    */
   typedef enum
      {
         InvalidEvent = -1,
         BuilderReset,    ///< for observerEvent(ObserverReset)
         CallRequestEvent,
         CallSetupEvent,
         CallFailureEvent,
         CallEndEvent,
         CallTransferEvent,
         AddCallData,
         AddVia,
         CompleteCallEvent
      } BuilderMethod;

   /// Event validity checking state machine
   bool builderStateIsOk(BuilderMethod method);
   /**<
    * Each of the public builder methods calls this finite state machine to determine
    * whether or not the call is valid.  This allows all builders to share the same
    * rules for what calls are allowed and required when constructing each event type
    * (for this reason, this should not be redefined by derived classes).
    *
    * In the initial state, the only valid call is builderStateOk(BuilderReset)
    *
    * @returns
    *  - true if method is valid now
    *  - false if method is not valid now
    */

   /// Reset the validity checking finite state machine to the initial state.
   bool builderStateReset();

/* //////////////////////////// PRIVATE /////////////////////////////////// */
  private:
   int  buildState; // used by builderStateIsOk

   /// no copy constructor
   CallStateEventBuilder(const CallStateEventBuilder&);

   /// no assignment operator
   CallStateEventBuilder& operator=(const CallStateEventBuilder&);

} ;

/* ============================ INLINE METHODS ============================ */

#endif    // _CallStateEventBuilder_h_
