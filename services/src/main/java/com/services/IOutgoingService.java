package com.services;

import com.cloudhopper.smpp.pdu.SubmitSm;
import com.models.OutgoingEntity;
import com.models.WebSMSEntity;

public interface IOutgoingService {

	OutgoingEntity save(OutgoingEntity outgoingEntity);

	OutgoingEntity initOutgoing(SubmitSm submitSm, long receivedSubmitSmDate, boolean udh);

	OutgoingEntity initOutgoing(WebSMSEntity webSMSEntity, long receivedSubmitSmDate);

	String countAllByDateRange(Long startValidTime, Long endValidTime);

	OutgoingEntity findByUUID(String uuid);
	
}
