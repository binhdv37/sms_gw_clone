package com.consumers.services.impl;

import static com.common.ConstanceEncode.*;
import static com.common.ConstanceStatus.DESCRIPTION;
import static com.common.ConstanceStatus.FAIL;
import static com.common.ConstanceStatus.RECEIVE_FROM_WEB_SUCCESS;
import static com.common.ConstanceStatus.RECEIVE_SUBMIT_SM_RESP_FAIL;
import static com.common.ConstanceStatus.RECEIVE_SUBMIT_SM_RESP_SUCCESS;
import static com.common.ConstanceStatus.SUCCESS;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.consumers.services.IMessageHandlingConsumersService;
import com.models.IncomingEntity;
import com.models.OutgoingEntity;
import com.models.to.DataCommandTo;
import com.models.to.DataCommandTo.DataCommandType;
import com.services.IConvertService;
import com.services.IIncomingService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessageHandlingConsumersServiceImpl implements IMessageHandlingConsumersService {

	@Value("${mbf.deliverSm.queueName}")
	private String mbfDeliverSmQueue;

	@Value("${mbf.submitSm.queueName}")
	private String mbfSubmitSmQueue;

	@Value("${mbf.submitSmResp.queueName}")
	private String mbfSubmitSmRespQueue;

	@Value("${smpp.client.request.expiryTimeout}")
	private long SMPP_REQUEST_TIMEOUT;

	@Value("${smpp.client.message.maxRetryTimes}")
	private int SMPP_MESSAGE_MAX_RETRY_TIMES = 5;

	@Value("${mbf.dataService.queueName}")
	private String mbfDataServiceQueue;

	private final String KEY_ALREADY_EXITS_MESSAGE = "already exists in the window";

	private final IConvertService convertService;
	private final RabbitTemplate rabbitTemplate;
	private final IIncomingService incomingService;

	@Autowired
	public MessageHandlingConsumersServiceImpl(IConvertService convertService, RabbitTemplate rabbitTemplate,
			IIncomingService incomingService) {
		this.convertService = convertService;
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitTemplate.setUseDirectReplyToContainer(false);
		this.incomingService = incomingService;
	}

	@Override
	public boolean updateSmsStatus(OutgoingEntity outgoingEntity, int status, String description, long outQueueDate,
			long receivedSubmitSmRespDate, String messageId, String actionStatus, String actionFailureDetails,
			boolean sentToMbf) {
		boolean result = false;
		try {
			outgoingEntity.setStatus(status);
			if (!outgoingEntity.getDescription().contains(DESCRIPTION.get(RECEIVE_FROM_WEB_SUCCESS))) {
				outgoingEntity.setDescription(description);
			}

			outgoingEntity.setOutQueueDate(outQueueDate);
			outgoingEntity.setReceivedSubmitSmRespDate(receivedSubmitSmRespDate);
			outgoingEntity.setMessageId(messageId);
			outgoingEntity.setActionStatus(actionStatus);
			outgoingEntity.setActionFailureDetails(actionFailureDetails);
			outgoingEntity.setSentToMbf(sentToMbf);

			DataCommandTo dataToSend = new DataCommandTo();
			dataToSend.setCommandType(DataCommandType.UPDATE_OUTGOING);
			dataToSend.setData(outgoingEntity);
			rabbitTemplate.convertAndSend(mbfDataServiceQueue, convertService.objectToJson(dataToSend));
			log.info("Put message to Data Service queue successfully");

			result = true;
			log.info("Update status after receive submit sm resp successfully");
		} catch (Exception e) {
			log.error("Error at updateSmsStatus function with message: {}", e.getMessage());
		}
		return result;
	}

	@Override
	public SubmitSm composeSubmitSmMbf(OutgoingEntity outgoingEntity) {
		SubmitSm submitSm = null;
		try {
			submitSm = new SubmitSm();
			submitSm.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
			submitSm.setSourceAddress(new Address((byte) outgoingEntity.getSourceAddressTon(),
					(byte) outgoingEntity.getSourceAddressNpi(), outgoingEntity.getSourceAddress()));
			submitSm.setDestAddress(new Address((byte) outgoingEntity.getDestinationAddressTon(),
					(byte) outgoingEntity.getDestinationAddressNpi(), outgoingEntity.getDestinationAddress()));
			switch (outgoingEntity.getEncodeTypeName()) {
				case NAME_GSM:
					submitSm.setShortMessage(CharsetUtil.encode(outgoingEntity.getShortMessage(), CharsetUtil.CHARSET_GSM));
					break;
				case NAME_UCS2:
					submitSm.setShortMessage(CharsetUtil.encode(outgoingEntity.getShortMessage(), CharsetUtil.CHARSET_UCS_2));
					break;
				case NAME_UTF8:
					submitSm.setShortMessage(CharsetUtil.encode(outgoingEntity.getShortMessage(), CharsetUtil.CHARSET_UTF_8));
					break;
				default:
					break;
			}
			submitSm.setDataCoding((byte) outgoingEntity.getDataCoding());
			submitSm.setSequenceNumber(outgoingEntity.getSequenceNumber());
			submitSm.setEsmClass((byte) outgoingEntity.getEsmClass());
			submitSm.setRegisteredDelivery((byte) outgoingEntity.getRegisterDelivery());
			log.info("Compose SubmitSm successful");
		} catch (Exception e) {
			log.warn("Error at composeSubmitSmMbf function with message: {}", e.getMessage());
		}
		return submitSm;
	}

	@Override
	public String sendSubmitSmMbf(SmppSession session, String strBody, long outQueueDate) throws Exception {
		OutgoingEntity currentOutgoing = null;
		SubmitSm submitSm;
		SubmitSmResp submitSmResp = null;
		String jsonSubmitSmResp = "";
		long receiveSubmitSmRespDate = 0;
		boolean hasChannelException = false;
		try {
			currentOutgoing = convertService.castBytesArrayToOutgoing(strBody);
			submitSm = composeSubmitSmMbf(currentOutgoing);
			submitSmResp = session.submit(submitSm, SMPP_REQUEST_TIMEOUT);
			receiveSubmitSmRespDate = System.currentTimeMillis();
			jsonSubmitSmResp = convertService.submitSmRespToJson(submitSmResp);

			// - For Web SMS (SubmitSm from dataService), return submitSm as normally
			// - For SubmitSm from Producer: send responsePdu via mbfSubmitSmRespQueue
			if (currentOutgoing != null && !currentOutgoing.getDescription().isEmpty()
					&& !currentOutgoing.getDescription().contains(DESCRIPTION.get(RECEIVE_FROM_WEB_SUCCESS))) {
				sendSubmitSmResponse(jsonSubmitSmResp);
			}
		} catch (SmppChannelException e) {
			log.error("SmppChannelException at sendSubmitSmMbf function with message: {}", e.getMessage());
			hasChannelException = true;
			throw e;
		} catch (Exception e) {
			// SMPP server already received message but had been disconnected before sending
			// response.
			if (e.getMessage().contains(KEY_ALREADY_EXITS_MESSAGE)) {
				updateSmsStatus(currentOutgoing, RECEIVE_SUBMIT_SM_RESP_SUCCESS,
						DESCRIPTION.get(RECEIVE_SUBMIT_SM_RESP_SUCCESS), outQueueDate, receiveSubmitSmRespDate,
						submitSmResp.getMessageId(), SUCCESS, null, true);
			} else {
				log.error("Unknown exception at sendSubmitSmMbf function with message: {}", e.getMessage());
				if (submitSmResp != null) {
					updateSmsStatus(currentOutgoing, RECEIVE_SUBMIT_SM_RESP_SUCCESS,
							DESCRIPTION.get(RECEIVE_SUBMIT_SM_RESP_SUCCESS), outQueueDate, receiveSubmitSmRespDate,
							submitSmResp.getMessageId(), FAIL, e.getMessage(), true);
				}
			}
		} finally {
			if (!hasChannelException) {
				if (submitSmResp != null) {
					if (submitSmResp.getCommandStatus() == SmppConstants.STATUS_OK) {
						updateSmsStatus(currentOutgoing, RECEIVE_SUBMIT_SM_RESP_SUCCESS,
								DESCRIPTION.get(RECEIVE_SUBMIT_SM_RESP_SUCCESS), outQueueDate, receiveSubmitSmRespDate,
								submitSmResp.getMessageId(), SUCCESS, null, true);
					} else {
						updateSmsStatus(currentOutgoing, submitSmResp.getCommandStatus(),
								submitSmResp.getResultMessage(), outQueueDate, receiveSubmitSmRespDate,
								submitSmResp.getMessageId(), FAIL, submitSmResp.getResultMessage(), true);
					}
				} else {
					updateSmsStatus(currentOutgoing, RECEIVE_SUBMIT_SM_RESP_FAIL,
							DESCRIPTION.get(RECEIVE_SUBMIT_SM_RESP_FAIL), outQueueDate, receiveSubmitSmRespDate, "",
							FAIL, DESCRIPTION.get(RECEIVE_SUBMIT_SM_RESP_FAIL), false);
				}
			}
		}
		return jsonSubmitSmResp;
	}

	@Override
	public DeliverSmResp putDeliverSmAndReceive(DeliverSm deliverSm, long receiveDeliverSmDate) {
		log.trace("Go into putDeliverSmAndReceive function");
		String strJsonDeliverSmResp;
		DeliverSmResp deliverSmResp = null;
		IncomingEntity incoming;
		try {
			incoming = incomingService.initIncoming(deliverSm, receiveDeliverSmDate);
			incoming.setInQueueDate(System.currentTimeMillis());

			DataCommandTo dataToSend = new DataCommandTo();
			dataToSend.setCommandType(DataCommandType.UPDATE_INCOMING);
			dataToSend.setData(incoming);
			rabbitTemplate.convertAndSend(mbfDataServiceQueue, convertService.objectToJson(dataToSend));
			log.info("Put message to Data Service queue successfully");

			if (incoming != null) {
				strJsonDeliverSmResp = (String) rabbitTemplate.convertSendAndReceive(mbfDeliverSmQueue,
						convertService.incomingToJson(incoming));
				log.info("Put DeliverSm to queue and receive DeliverSmResp successful with response: {}",
						strJsonDeliverSmResp);
				deliverSmResp = convertService.jsonToDeliverSmResp(strJsonDeliverSmResp);
			}
		} catch (Exception e) {
			log.error("Error while put deliverSm to queue and receive deliverSmResp with message: {}", e.getMessage());
			deliverSmResp = new DeliverSmResp();
			deliverSmResp.setCommandStatus(SmppConstants.STATUS_SYSERR);
		}
		log.trace("Exit putDeliverSmAndReceive function");
		return deliverSmResp;
	}

	@Override
	public String sendSubmitSmBackToQueue(String strBody, long outQueueDate) throws Exception {
		String strJsonSubmitSmResp = null;
		OutgoingEntity currentOutgoing = convertService.castBytesArrayToOutgoing(strBody);
		if (currentOutgoing != null) {
			int retriedTimes = currentOutgoing.getRetriedTimes();
			if (retriedTimes < SMPP_MESSAGE_MAX_RETRY_TIMES) {
				currentOutgoing.setRetriedTimes(retriedTimes + 1);
				log.info("Retry time: {}/{}", retriedTimes, SMPP_MESSAGE_MAX_RETRY_TIMES);
				strJsonSubmitSmResp = (String) rabbitTemplate.convertSendAndReceive(getNextSubmitSmQueueName(),
						convertService.outgoingToJson(currentOutgoing));
			} else {
				updateSmsStatus(currentOutgoing, RECEIVE_SUBMIT_SM_RESP_FAIL,
						DESCRIPTION.get(RECEIVE_SUBMIT_SM_RESP_FAIL), outQueueDate, System.currentTimeMillis(), "",
						FAIL, DESCRIPTION.get(RECEIVE_SUBMIT_SM_RESP_FAIL), false);
			}
		}

		return strJsonSubmitSmResp;
	}

	private void sendSubmitSmResponse(String strBody) {
		try {
			rabbitTemplate.convertAndSend(mbfSubmitSmRespQueue, strBody);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getNextSubmitSmQueueName() {
		String result = "";

		if (!StringUtils.isBlank(mbfSubmitSmQueue)) {
			int numb = Integer.parseInt(mbfSubmitSmQueue.substring(mbfSubmitSmQueue.length() - 1));
			if (numb >= 5)
				numb = 1;
			else
				numb++;
			result = mbfSubmitSmQueue.substring(0, mbfSubmitSmQueue.length() - 1) + numb;
		}

		return result;
	}
}
