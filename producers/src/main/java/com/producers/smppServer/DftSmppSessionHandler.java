package com.producers.smppServer;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.ByteUtil;
import com.models.MultipartMessage;
import com.producers.services.impl.MultiProcessSubmitSm;
import com.producers.services.impl.MultipartProcessSubmitSm;
import com.services.IMessageTemplateService;
import com.services.impl.MessageTemplateServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.producers.services.IMessageHandlingProducersService;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.producers.services.impl.MessageHandlingProducersServiceImpl.msgTemplate;

@Service
@Slf4j
public class DftSmppSessionHandler extends DefaultSmppSessionHandler {
    public static SmppSession smppSession;
    public static boolean isQueueAvailable = true;
    private SmppSession sessionRef;
    private final IMessageHandlingProducersService messageHandlingService;
    public static ConcurrentHashMap<String, SmppSession> sessionSubmitSm = new ConcurrentHashMap<>();
    private final ExecutorService executorNormalMessage = Executors.newFixedThreadPool(200);
    private final ExecutorService executorMultipart = Executors.newSingleThreadExecutor();
    public static ConcurrentHashMap<Integer, HashSet<MultipartMessage>> multiPartMsg = new ConcurrentHashMap<>();

    @Autowired
    public DftSmppSessionHandler(IMessageHandlingProducersService messageHandlingService) {
        this.messageHandlingService = messageHandlingService;
    }

    public void setSessionRef(SmppSession session) {
        this.sessionRef = session;
    }

    @Override
    public PduResponse firePduRequestReceived(PduRequest pduRequest) {
        PduResponse pduResponse = pduRequest.createResponse();

        if (!isQueueAvailable) {
            pduResponse.setResultMessage("Unable to send message");
            pduResponse.setCommandStatus(SmppConstants.STATUS_SUBMITFAIL);
            return pduResponse;
        }

        try {
            // Response of SubmitSm will be sent by sendSubmitSmResponsePdu() method
            if (pduRequest instanceof SubmitSm) {
                // Put sequence number of submitSm and session
                sessionSubmitSm.put(String.valueOf(pduRequest.getSequenceNumber()), sessionRef);
                boolean isUdh = isUdhEnabled(((SubmitSm) pduRequest).getEsmClass());
                if (!isUdh) {
                    log.info("Pdu request is submit sm without UDH of session: {}", sessionRef.toString());
                    // not udh
                    // Put to thread process normal message
                    MultiProcessSubmitSm processSubmitSm = new MultiProcessSubmitSm(messageHandlingService, pduRequest, System.currentTimeMillis(), false, false);
                    executorNormalMessage.submit(processSubmitSm);
                } else {
                    // is udh
                    log.info("Pdu request is submit sm with UDH of session: {}", sessionRef.toString());
//                    MultipartMessage multipartMessage = initMultipartMessage(((SubmitSm) pduRequest));
                    // Put to thread process multipart message
                    MultipartProcessSubmitSm multipartProcessSubmitSm = new MultipartProcessSubmitSm((SubmitSm) pduRequest, messageHandlingService);
                    executorMultipart.submit(multipartProcessSubmitSm);
                }
                return null;
            } else if (pduRequest instanceof EnquireLink) {
                log.info("Pdu request is enquire link");
            }
        } catch (Exception ex) {
            log.error("Error at function firePduRequestReceived in producers module with message: " + ex.getMessage());
            pduResponse.setResultMessage(ex.getMessage());
            pduResponse.setCommandStatus(SmppConstants.STATUS_UNKNOWNERR);
        }
        return pduResponse;
    }

    public void sendSubmitSmResponsePdu(String strSubmitSmResponse, long outQueueDate) {
        log.info("Received Submit SM response PDU: {}", strSubmitSmResponse);
        SubmitSmResp receiveSubmitSmResponse = messageHandlingService.receiveSubmitSmResponse(strSubmitSmResponse, outQueueDate);
        // Get session of submitSm by sequence number of submitSmResp
        SmppSession sessionOfMsg = sessionSubmitSm.get(String.valueOf(receiveSubmitSmResponse.getSequenceNumber()));
        if (sessionOfMsg != null && sessionOfMsg.isBound()) {
            try {
                sessionOfMsg.sendResponsePdu(receiveSubmitSmResponse);
                // Remove sequence number after sent
                sessionSubmitSm.remove(String.valueOf(receiveSubmitSmResponse.getSequenceNumber()));
            } catch (Exception e) {
                log.error("Error at function sendSubmitSmResponsePdu in producers module with message: " + e.getMessage());
            }
        }
    }

    public String sendDeliverSmAndReceive(String strDeliverSm, long outQueueDate) {
        return messageHandlingService.sendDeliverSm(smppSession, strDeliverSm, outQueueDate);
    }

    private boolean isUdhEnabled(byte esmClass) {
        return ((esmClass & 67) == 67) || ((esmClass & 64) == 64);
    }

    private byte[] getShortMessageUserData(byte[] shortMessage) throws IllegalArgumentException {
        if (shortMessage == null) {
            return null;
        }
        if (shortMessage.length == 0) {
            return shortMessage;
        }
        int userDataHeaderLength = ByteUtil.decodeUnsigned(shortMessage[0]) + 1;
        if (userDataHeaderLength > shortMessage.length) {
            throw new IllegalArgumentException("User data header length exceeds short message length [shortMessageLength=" + shortMessage.length + ", userDataHeaderLength=" + userDataHeaderLength + "]");
        }
        int newShortMessageLength = shortMessage.length - userDataHeaderLength;
        byte[] newShortMessage = new byte[newShortMessageLength];
        System.arraycopy(shortMessage, userDataHeaderLength, newShortMessage, 0, newShortMessageLength);
        return newShortMessage;
    }

    private byte[] getShortMessageUserDataHeader(byte[] shortMessage) throws IllegalArgumentException{
        if (shortMessage == null) {
            return null;
        }
        if (shortMessage.length == 0) {
            return shortMessage;
        }
        int userDataHeaderLength = ByteUtil.decodeUnsigned(shortMessage[0]) + 1;
        if (userDataHeaderLength > shortMessage.length) {
            throw new IllegalArgumentException("User data header length exceeds short message length [shortMessageLength=" + shortMessage.length + ", userDataHeaderLength=" + userDataHeaderLength + "]");
        }
        if (userDataHeaderLength == shortMessage.length) {
            return shortMessage;
        }
        byte[] userDataHeader = new byte[userDataHeaderLength];
        System.arraycopy(shortMessage, 0, userDataHeader, 0, userDataHeaderLength);

        return userDataHeader;
    }

    private MultipartMessage initMultipartMessage(SubmitSm submitSm) {
        MultipartMessage multipartMessage = new MultipartMessage();
        String strMsg;

        try {
            byte[] udh = getShortMessageUserDataHeader(submitSm.getShortMessage());
            int messageId = udh[3] & 0x0ff;
            int totalMessages = udh[4] & 0x0ff;
            int currentMessageNum = udh[5] & 0x0ff;
            byte[] shortMessage = getShortMessageUserData(submitSm.getShortMessage());
            byte dcs = submitSm.getDataCoding();
            byte esmClass = submitSm.getEsmClass();
            String sourceAddress = submitSm.getSourceAddress().getAddress();
            String destinationAddress = submitSm.getDestAddress().getAddress();

            if (dcs == (byte) 0x00) {
                strMsg = CharsetUtil.decode(shortMessage, CharsetUtil.CHARSET_GSM);
                log.info("The short messages are encrypted with GSM");
            } else if (dcs == (byte) 0x08) {
                strMsg = CharsetUtil.decode(shortMessage, CharsetUtil.CHARSET_UCS_2);
                log.info("The short messages are encrypted with UCS2");
            } else {
                // don't know what kind of dcs
                strMsg = CharsetUtil.decode(shortMessage, CharsetUtil.CHARSET_UCS_2);
                log.info("Don't know what kind of dcs with dcs = {}", submitSm.getDataCoding());
            }

            multipartMessage.setSequenceNumber(messageId);
            multipartMessage.setTotalMessages(totalMessages);
            multipartMessage.setCurrentMessageNumber(currentMessageNum);
            multipartMessage.setShortMessage(strMsg);
            multipartMessage.setEsmClass(esmClass & 0xff);
            multipartMessage.setDcs(dcs & 0xff);
            multipartMessage.setSourceAddress(sourceAddress);
            multipartMessage.setDestinationAddress(destinationAddress);
            multipartMessage.setRawRequest(submitSm);
        } catch (Exception e) {
            log.warn("Error at initMultipartMessage function with message: {}", e.getMessage());
        }

        return multipartMessage;
    }
}
