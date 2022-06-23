package com.services.impl;

import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.util.DeliveryReceipt;
import com.repositories.IncomingRepository;
import com.services.IIncomingService;
import com.models.IncomingEntity;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.common.ConstanceStatus.*;

import java.util.UUID;

@Service
@Slf4j
public class IncomingServiceImpl implements IIncomingService {

	private final IncomingRepository incomingRepository;

	@Autowired
	public IncomingServiceImpl(IncomingRepository incomingRepository) {
		this.incomingRepository = incomingRepository;
	}

	@Override
	public IncomingEntity save(IncomingEntity incoming) {
		IncomingEntity incomingEntitySaved = null;
		if (incoming != null) {
			try {
				incomingEntitySaved = incomingRepository.save(incoming);
				log.debug("Save IncomingEntity (DeliverSm) successful");
			} catch (Exception e) {
				log.warn("Error at save IncomingEntity with message: {}", e.getMessage());
			}
		}
		return incomingEntitySaved;
	}

	@Override
	public IncomingEntity initIncoming(DeliverSm deliverSm, long receivedDeliverSmDate) {
		log.trace("Go into initIncoming function");
		IncomingEntity incomingEntity = new IncomingEntity();
		DeliveryReceipt dlr;
		try {
			dlr = DeliveryReceipt.parseShortMessage(new String(deliverSm.getShortMessage()),
					DateTimeZone.forID("Asia/Ho_Chi_Minh"), false);
			incomingEntity.setCreatedDate(System.currentTimeMillis());
			incomingEntity.setUpdatedDate(System.currentTimeMillis());
			incomingEntity.setSourceAddress(deliverSm.getSourceAddress().getAddress());
			incomingEntity.setDestinationAddress(deliverSm.getDestAddress().getAddress());
			incomingEntity.setShortMessage(new String(deliverSm.getShortMessage()));
			incomingEntity.setSequenceNumber(deliverSm.getSequenceNumber());
			incomingEntity.setReceivedDeliverSmDate(receivedDeliverSmDate);
			incomingEntity.setMessageId(dlr.getMessageId());
			incomingEntity.setStatus(RECEIVE_DELIVER_SM_SUCCESS);
			incomingEntity.setDescription(DESCRIPTION.get(RECEIVE_DELIVER_SM_SUCCESS));
			incomingEntity.setDeleted(false);
			incomingEntity.setUuid(UUID.randomUUID().toString());
			log.debug("Initial IncomingEntity successful");
		} catch (Exception e) {
			log.warn("Error at initIncoming function with message: {}", e.getMessage());
		}
		log.trace("Exit initIncoming function");
		return incomingEntity;
	}

	@Override
	public IncomingEntity findByUUID(String uuid) {
		return incomingRepository.findByUUID(uuid);
	}
}
