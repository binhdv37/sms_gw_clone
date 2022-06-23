package com.services;

import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.models.IncomingEntity;
import com.models.OutgoingEntity;
import com.models.to.DataCommandTo;

public interface IConvertService {

    String submitSmToJson(SubmitSm submitSm);

    String submitSmRespToJson(SubmitSmResp submitSmResp);

    SubmitSm jsonToSubmitSm(String jsonSubmitSm);

    SubmitSmResp jsonToSubmitSmResp(String jsonSubmitSmResp);

    String outgoingToJson(OutgoingEntity outgoingEntity);

    String incomingToJson(IncomingEntity incomingEntity);

    OutgoingEntity castBytesArrayToOutgoing(String strBody);

    IncomingEntity castBytesArrayToIncoming(String strBody);

    String deliverSmToJson(DeliverSm deliverSm);

    DeliverSm jsonToDeliverSm(String jsonDeliverSm);

    String deliverSmRespToJson(DeliverSmResp deliverSmResp);

    DeliverSmResp jsonToDeliverSmResp(String jsonDeliverSmpResp);
    
    DataCommandTo castBytesArrayToDataCommandTo(String strBody);
    
    String objectToJson(Object data);
}
