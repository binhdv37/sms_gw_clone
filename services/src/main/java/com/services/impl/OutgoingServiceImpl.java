package com.services.impl;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.ByteUtil;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.models.EncodeType;
import com.models.OutgoingEntity;
import com.models.WebSMSEntity;
import com.repositories.OutgoingRepository;
import com.services.EncodeTypeService;
import com.services.IOutgoingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.common.ConstanceEncode.*;
import static com.common.ConstanceStatus.*;

import java.util.UUID;

@Service
@Slf4j
public class OutgoingServiceImpl implements IOutgoingService {

	private final OutgoingRepository outgoingRepository;
	private final EncodeTypeService encodeTypeService;

	@Autowired
	public OutgoingServiceImpl(OutgoingRepository outgoingRepository, EncodeTypeService encodeTypeService) {
		this.outgoingRepository = outgoingRepository;
		this.encodeTypeService = encodeTypeService;
	}

	@Override
	public OutgoingEntity save(OutgoingEntity outgoingEntity) {
		if (outgoingEntity != null) {
			return outgoingRepository.saveAndFlush(outgoingEntity);
		} else {
			return null;
		}
	}

	@Override
	public OutgoingEntity initOutgoing(SubmitSm submitSm, long receivedSubmitSmDate, boolean udh) {
		OutgoingEntity outgoing = new OutgoingEntity();
		try {
			if (submitSm.getDataCoding() == (byte) 0x00) {
				outgoing.setShortMessage(CharsetUtil.decode(submitSm.getShortMessage(), CharsetUtil.CHARSET_GSM));
				outgoing.setEncodeTypeName(NAME_GSM);
				log.info("The short messages are encrypted with GSM");
			} else if (submitSm.getDataCoding() == (byte) 0x08){
				outgoing.setShortMessage(CharsetUtil.decode(submitSm.getShortMessage(), CharsetUtil.CHARSET_UCS_2));
				outgoing.setEncodeTypeName(NAME_UCS2);
				log.info("The short messages are encrypted with UCS2");
			} else {
				// don't know what kind of dcs
				outgoing.setShortMessage(CharsetUtil.decode(submitSm.getShortMessage(), CharsetUtil.CHARSET_UCS_2));
				outgoing.setEncodeTypeName(NAME_UCS2);
				log.info("Don't know what kind of dcs with dcs = {}", submitSm.getDataCoding());
			}

			outgoing.setDataCoding(submitSm.getDataCoding() & 0x0ff);
			outgoing.setCreatedDate(System.currentTimeMillis());
			outgoing.setUpdatedDate(System.currentTimeMillis());
			outgoing.setSourceAddress(submitSm.getSourceAddress().getAddress());
			outgoing.setSourceAddressNpi(submitSm.getSourceAddress().getNpi() & 0x0ff);
			outgoing.setSourceAddressTon(submitSm.getSourceAddress().getTon() & 0x0ff);
			outgoing.setDestinationAddress(submitSm.getDestAddress().getAddress());
			outgoing.setDestinationAddressNpi(submitSm.getDestAddress().getNpi() & 0x0ff);
			outgoing.setDestinationAddressTon(submitSm.getDestAddress().getTon() & 0x0ff);
			outgoing.setSequenceNumber(submitSm.getSequenceNumber());
			outgoing.setReceivedSubmitSmDate(receivedSubmitSmDate);
			outgoing.setStatus(RECEIVE_FROM_FB_SUCCESS);
			outgoing.setDescription(DESCRIPTION.get(RECEIVE_FROM_FB_SUCCESS));
			outgoing.setDeleted(false);
			outgoing.setActionStatus(FAIL);
			outgoing.setSentToMbf(false);
			outgoing.setUuid(UUID.randomUUID().toString());
			outgoing.setEsmClass(submitSm.getEsmClass() & 0x0ff);
			outgoing.setRegisterDelivery(submitSm.getRegisteredDelivery() & 0x0ff);
			outgoing.setUdh(udh);
			outgoing.setRawRequest(submitSm.toString());
			if (udh) {
				byte[] udhBytes = getShortMessageUserDataHeader(submitSm.getShortMessage());
				outgoing.setMultipartId(udhBytes[3] & 0x0ff);
				outgoing.setTotalMessages(udhBytes[4] & 0x0ff);
				outgoing.setCurrentMessageNumber(udhBytes[5] & 0x0ff);
			}
		} catch (Exception e) {
			log.warn("Error at initOutgoing function with message: {}", e.getMessage());
		}
		return outgoing;
	}

	@Override
	public OutgoingEntity initOutgoing(WebSMSEntity webSMSEntity, long receivedSubmitSmDate) {
		OutgoingEntity outgoing = new OutgoingEntity();
		try {
			outgoing.setCreatedDate(System.currentTimeMillis());
			outgoing.setUpdatedDate(System.currentTimeMillis());
			outgoing.setSourceAddress(webSMSEntity.getSourceAddress());
			outgoing.setSourceAddressNpi(0);
			outgoing.setSourceAddressTon(5);
			outgoing.setDestinationAddress(webSMSEntity.getDestinationAddress());
			outgoing.setDestinationAddressNpi(1);
			outgoing.setDestinationAddressTon(1);
			outgoing.setDataCoding(0);
			outgoing.setEncodeTypeName(NAME_GSM);
			outgoing.setShortMessage(webSMSEntity.getShortMessage());
			outgoing.setSequenceNumber(1);
			outgoing.setReceivedSubmitSmDate(receivedSubmitSmDate);
			outgoing.setStatus(RECEIVE_FROM_WEB_SUCCESS);
			outgoing.setDescription(DESCRIPTION.get(RECEIVE_FROM_WEB_SUCCESS) + "_ID=" + webSMSEntity.getId());
			outgoing.setDeleted(false);
			outgoing.setActionStatus(FAIL);
			outgoing.setSentToMbf(false);
		} catch (Exception e) {
			log.warn("Error at initOutgoing function with message: {}", e.getMessage());
		}
		return outgoing;
	}

	@Override
	public String countAllByDateRange(Long startValidTime, Long endValidTime) {
		long total = 0L;
		try {
			total = outgoingRepository.countAllByDateRange(false, true, startValidTime, endValidTime);
		} catch (Exception e) {
			log.warn("Error at countAllByDateRange function with message: {}", e.getMessage());
		}
		return Long.toString(total);
	}

	@Override
	public OutgoingEntity findByUUID(String uuid) {
		return outgoingRepository.findByUUID(uuid);
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
