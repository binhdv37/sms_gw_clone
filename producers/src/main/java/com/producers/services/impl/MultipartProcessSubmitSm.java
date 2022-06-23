package com.producers.services.impl;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.ByteUtil;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.models.MultipartMessage;
import com.producers.services.IMessageHandlingProducersService;
import com.services.IMessageTemplateService;
import com.services.impl.MessageTemplateServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import static com.producers.services.impl.MessageHandlingProducersServiceImpl.msgTemplate;
import static com.producers.smppServer.DftSmppSessionHandler.multiPartMsg;

@Slf4j
public class MultipartProcessSubmitSm implements Runnable{

//    private final MultipartMessage multipartMessage;
    private final SubmitSm submitSm;
    private final IMessageHandlingProducersService messageHandlingService;

    public MultipartProcessSubmitSm(SubmitSm submitSm, IMessageHandlingProducersService messageHandlingService) {
//        this.multipartMessage = multipartMessage;
        this.submitSm = submitSm;
        this.messageHandlingService = messageHandlingService;
    }

    @Override
    public void run() {
        MultipartMessage multipartMessage = initMultipartMessage(this.submitSm);
        // is udh
        synchronized (multiPartMsg) {
            if (multipartMessage.getSequenceNumber() >= 0) {
                log.info("Hash map multipart message before check: {} in thread {}", multiPartMsg, Thread.currentThread().getName());
                // Check key exist in hash map
                if (multiPartMsg.isEmpty() || !multiPartMsg.containsKey(multipartMessage.getSequenceNumber())) {
                    HashSet<MultipartMessage> multipartMessages = new HashSet<>();
                    multipartMessages.add(multipartMessage);
                    multiPartMsg.put(multipartMessage.getSequenceNumber(), multipartMessages);
                    log.info("New sequence number of multipart message: {}", multipartMessage);
                } else if (multiPartMsg.containsKey(multipartMessage.getSequenceNumber())) {
                    HashSet<MultipartMessage> multipartMessages = multiPartMsg.get(multipartMessage.getSequenceNumber());
                    if (multipartMessages.add(multipartMessage)) {
                        log.info("Add new multipartMessage to hash set: {}", multipartMessage);
                    } else {
                        log.info("Existed multipartMessage in hash set: {}", multipartMessage);
                    }
                    // Check end of multipart message
                    if (multipartMessage.getTotalMessages() == multipartMessages.size()) {
                        ArrayList<MultipartMessage> arrayListSorted = new ArrayList<>(multipartMessages);
                        arrayListSorted.sort(Comparator.comparing(MultipartMessage::getCurrentMessageNumber));
                        int index = 1;
                        boolean enough = false;
                        for (MultipartMessage mm : arrayListSorted) {
                            if (mm.getCurrentMessageNumber() == index) {
                                log.info("[index]/[currentMessageNumber] = [{}]/[{}]", index, mm.getCurrentMessageNumber());
                                if (index == mm.getTotalMessages()) {
                                    enough = true;
                                    break;
                                }
                                index = index + 1;
                            }
                        }
                        if (enough) {
                            log.info("Receive full multipart message with [total current message]/[total message] = [{}]/[{}],", multipartMessages.size(), multipartMessage.getTotalMessages());
                            StringBuilder builder = new StringBuilder();
                            for (MultipartMessage multipart : arrayListSorted) {
                                builder.append(multipart.getShortMessage());
                            }
                            if (builder.length() > 0) {
                                IMessageTemplateService messageTemplateService = new MessageTemplateServiceImpl();
                                boolean existed = messageTemplateService.existInMsgTemplate(multipartMessage.getSourceAddress(), builder.toString(), msgTemplate);
                                for (MultipartMessage message : multipartMessages) {
                                    MultiProcessSubmitSm processSubmitSm = new MultiProcessSubmitSm(messageHandlingService, message.getRawRequest(), System.currentTimeMillis(), true, existed);
                                    processSubmitSm.run();
                                }
                                // Remove after existed
                                multiPartMsg.remove(multipartMessage.getSequenceNumber());
                                log.info("Remove sequence number of multipart message in hash set is done. Hash set multipart message after remove: {}", multiPartMsg);
                            }
                        } else {
                            log.warn("Receive full multipart message but not correct or duplicated parts of the messages: {}", multiPartMsg);
                        }
                    } else {
                        log.info("Receive not full multipart message with [total current message]/[total message] = [{}]/[{}],", multipartMessages.size(), multipartMessage.getTotalMessages());
                    }
                }
            }
        }
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

}
