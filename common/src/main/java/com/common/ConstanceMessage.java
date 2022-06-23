package com.common;

public class ConstanceMessage {

//    Log rabbitMq message
    public final static String RM_MSG_001 = "Create connection to rabbitMq successful";
    public final static String RM_MSG_002 = "Message has entered queue";
    public final static String RM_MSG_003 = "Get message from queue with body: ";
    public final static String RM_MSG_004 = "Sent message to method message handling successful";
    public final static String RM_MSG_005 = "Put message to queue successful";

//    Log rabbitMq error
    public final static String RM_ERR_001 = "Error while initial connection to rabbitMq with message: ";
    public final static String RM_ERR_002 = "Error while connect to rabbitMq or add message to queue with message: ";
    public final static String RM_ERR_003 = "Error while get message from queue or sent message to another method with message: ";
    public final static String RM_ERR_004 = "Error while put message to queue with message: ";
    public final static String RM_ERR_005 = "Error while get message from queue with message: ";
//    Log smsc message


//    Log smsc error
    public static final String SMSC_ERR_001 = "Error while start SMSC with message: ";
    public static final String SMSC_ERR_002 = "Error while session bind request with message: ";
    public static final String SMSC_ERR_003 = "Error while destroy session with message: ";
    public static final String SMSC_ERR_004 = "Error while create session with message: ";
    public static final String SMSC_ERR_005 = "PduRequest is not SubmitSm";

//    Log service message

//    Log service error
    public static final String SMS_SV_ERR_001 = "Error while save log to database with message: ";
    public static final String SMS_SV_ERR_002 = "Error while find log by id from database with message: ";
    public static final String SMS_SV_ERR_003 = "Error while cast request to object SmsEntity with message: ";
    public static final String SMS_SV_ERR_004 = "Error while cast from byte array to object SmsEntity with message: ";
    public static final String SMS_SV_ERR_005 = "Error while cast pdu request to sms history entity with message: ";
    public static final String SMS_SV_ERR_006 = "Error while compose object submit sm with message: ";
    public static final String SMS_SV_ERR_007 = "Error while handling message with message: ";
    public static final String SMS_SV_ERR_008 = "Violation keyword is empty";
    public static final String SMS_SV_ERR_009 = "Error while cron violating keyword with message: ";
    public static final String SMS_SV_ERR_010 = "Fail while update Sms to database with message: ";

}
