package com.services;

import com.cloudhopper.smpp.pdu.DeliverSm;
import com.models.IncomingEntity;

public interface IIncomingService {

	IncomingEntity save(IncomingEntity incoming);

	IncomingEntity initIncoming(DeliverSm deliverSm, long receivedDeliverSmDate);

	IncomingEntity findByUUID(String uuid);

}
