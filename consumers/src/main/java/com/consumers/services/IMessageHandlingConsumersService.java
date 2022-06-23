package com.consumers.services;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.models.OutgoingEntity;

public interface IMessageHandlingConsumersService {
    boolean updateSmsStatus(OutgoingEntity outgoingEntity, int status, String description, long outQueueDate, long receivedSubmitSmRespDate, String messageId, String actionStatus, String actionFailureDetails, boolean sentToMbf);

    SubmitSm composeSubmitSmMbf(OutgoingEntity outgoingEntity);

	String sendSubmitSmMbf(SmppSession session, String strBody, long outQueueDate) throws Exception;
    
    String sendSubmitSmBackToQueue(String strBody, long outQueueDate) throws Exception;

    DeliverSmResp putDeliverSmAndReceive(DeliverSm deliverSm, long receiveDeliverSmDate);
}
