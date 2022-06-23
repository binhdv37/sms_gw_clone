package com.producers.services;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.models.IncomingEntity;

public interface IMessageHandlingProducersService {

    void sendSubmitSm(PduRequest pduRequest, long receivedSubmitSmDate, boolean udh, boolean matchedRegexUdh);
    
    SubmitSmResp receiveSubmitSmResponse(String strSubmitSmResponse, long receivedSubmitSmDate);

    String sendDeliverSm(SmppSession session, String strIncoming, long outQueueDate);

    DeliverSm composeDeliverSm(IncomingEntity incoming);

    IncomingEntity updateStatusIncoming(IncomingEntity currentIncoming, int status, String description, long outQueueDate, long receivedDeliverSmRespDate);

}
