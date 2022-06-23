package com.producers.services.impl;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.type.Address;
import com.google.gson.internal.LinkedTreeMap;
import com.models.IncomingEntity;
import com.models.OutgoingEntity;
import com.models.to.DataCommandTo;
import com.models.to.DataCommandTo.DataCommandType;
import com.producers.services.IMessageHandlingProducersService;
import com.services.*;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.common.ConstanceStatus.*;

@Service
@Slf4j
public class MessageHandlingProducersServiceImpl implements IMessageHandlingProducersService {

	@Value("${mbf.submitSm.queueName1}")
	private String mbfSubmitSmQueue1;

	@Value("${mbf.submitSm.queueName2}")
	private String mbfSubmitSmQueue2;

	@Value("${mbf.submitSm.queueName3}")
	private String mbfSubmitSmQueue3;

	@Value("${mbf.submitSm.queueName4}")
	private String mbfSubmitSmQueue4;

	@Value("${mbf.submitSm.queueName5}")
	private String mbfSubmitSmQueue5;

	@Value("${mbf.dataService.queueName}")
	private String mbfDataServiceQueue;

	@Value("${mbf.submitSmResp.queueName}")
	private String mbfSubmitSmRespQueue;

	@Value("${validate.template.allow}")
	private boolean allowValidateTemplate;

	private final IWhitelistService whitelistService;
	private final IBlacklistService blacklistService;
	private final IProviderService providerService;
	private final IOutgoingService outgoingService;
	private final IConvertService convertService;
	private final RabbitTemplate rabbitTemplate;
	private final IMessageTemplateService messageTemplateService;
	private final IQuotaService quotaService;

	public static ConcurrentHashMap<String, String> hashMapSmppUser = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> prefixPhoneNumber = new ConcurrentHashMap<>();
	public static ArrayList<String> blacklists = new ArrayList<>();
	public static ArrayList<String> whitelists = new ArrayList<>();
	public static ConcurrentHashMap<String, List<String>> msgTemplate = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, LinkedTreeMap<String, String>> quota = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> totalMsgSent = new ConcurrentHashMap<>();
	public static boolean whitelistActive;
	public static boolean quotaActive;

	private static int currentSubmitSmQueueNo = 0;

	@Autowired
	public MessageHandlingProducersServiceImpl(IWhitelistService whitelistService, IBlacklistService blacklistService,
			IProviderService providerService, IOutgoingService outgoingService, IConvertService convertService,
			RabbitTemplate rabbitTemplate, IMessageTemplateService messageTemplateService, IQuotaService quotaService) {
		this.whitelistService = whitelistService;
		this.blacklistService = blacklistService;
		this.providerService = providerService;
		this.outgoingService = outgoingService;
		this.convertService = convertService;
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitTemplate.setUseDirectReplyToContainer(false);
		this.messageTemplateService = messageTemplateService;
		this.quotaService = quotaService;
	}

	@Override
	public void sendSubmitSm(PduRequest pduRequest, long receivedSubmitSmDate, boolean udh, boolean matchedRegexUdh) {
		int statusInWhitelist = 0;
		int statusInBlacklist = 0;
		int providerId;
		boolean matchedTemplate;
		boolean allowSentMsg;
		OutgoingEntity outgoing;
		try {
			// initial entity outgoing
			outgoing = outgoingService.initOutgoing((SubmitSm) pduRequest, receivedSubmitSmDate, udh);
			// find providerId
			providerId = providerService.existInProvider(outgoing.getDestinationAddress(), prefixPhoneNumber);
			outgoing.setProviderId((long) providerId);
			// check mode whitelist
			if (!whitelistActive) {
				log.info("Whitelist mode is OFF");
				// validate blacklist
				statusInBlacklist = blacklistService.existInBlacklist(outgoing.getDestinationAddress(), blacklists);
				if (statusInBlacklist == DEST_ADDRESS_EXISTED_BLACKLIST) {
					outgoing.setStatus(DEST_ADDRESS_EXISTED_BLACKLIST);
					outgoing.setDescription(DESCRIPTION.get(DEST_ADDRESS_EXISTED_BLACKLIST));
					outgoing.setActionFailureDetails(AFD.get(DEST_ADDRESS_EXISTED_BLACKLIST));
				}
			} else {
				log.info("Whitelist mode is ON");
				// validate whitelist
				statusInWhitelist = whitelistService.existInWhitelist(outgoing.getDestinationAddress(), whitelists);
				// whitelist empty
				if (statusInWhitelist == WHITELIST_EMPTY) {
					outgoing.setStatus(WHITELIST_EMPTY);
					outgoing.setDescription(DESCRIPTION.get(WHITELIST_EMPTY));
					outgoing.setActionFailureDetails(AFD.get(DEST_ADDRESS_NOT_EXISTED_WHITELIST));
					// whitelist not empty but destination not exist in whitelist
				} else if (statusInWhitelist == DEST_ADDRESS_NOT_EXISTED_WHITELIST) {
					outgoing.setStatus(DEST_ADDRESS_NOT_EXISTED_WHITELIST);
					outgoing.setDescription(DESCRIPTION.get(DEST_ADDRESS_NOT_EXISTED_WHITELIST));
					outgoing.setActionFailureDetails(AFD.get(DEST_ADDRESS_NOT_EXISTED_WHITELIST));
				}
			}
			// violations blacklist or whitelist -> check template message
			if (statusInBlacklist == DEST_ADDRESS_NOT_EXISTED_BLACKLIST || statusInWhitelist == DEST_ADDRESS_EXISTED_WHITELIST) {
				// validate template
				if (allowValidateTemplate) {
					log.info("Validate template is ON");
					if (!udh) {
						matchedTemplate = messageTemplateService.existInMsgTemplate(outgoing.getSourceAddress(), outgoing.getShortMessage(), msgTemplate);
					} else {
						// multipart message with UDH
						matchedTemplate = matchedRegexUdh;
					}
					if (!matchedTemplate) {
						outgoing.setStatus(SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE);
						outgoing.setDescription(DESCRIPTION.get(SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE));
						outgoing.setActionFailureDetails(AFD.get(SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE));
						log.info("The short message: {} does not match the template in the system", outgoing.getShortMessage());
					}
				} else {
					log.info("Validate template is OFF");
				}
				// check mode quota
				if (quotaActive) {
					log.info("Quota mode is ON");
					// validate quota
					allowSentMsg = quotaService.allowSendMessage(outgoing.getCreatedDate(), quota, totalMsgSent);
					if (!allowSentMsg) {
						outgoing.setStatus(QUOTA_IS_OVER);
						outgoing.setDescription(DESCRIPTION.get(QUOTA_IS_OVER));
						outgoing.setActionFailureDetails(AFD.get(QUOTA_IS_OVER));
						log.info("Quota is over so cannot to send");
					} else {
						log.info("Allowed to send");
					}
				} else {
					log.info("Quota mode is OFF");
				}
			}
			outgoing.setInQueueDate(System.currentTimeMillis());
			DataCommandTo dataToSend = new DataCommandTo();
			dataToSend.setCommandType(DataCommandType.ADD_OUTGOING);
			dataToSend.setData(outgoing);
			rabbitTemplate.convertAndSend(mbfDataServiceQueue, convertService.objectToJson(dataToSend));
			log.info("Put message to mbfDataServiceQueue successfully");

			if (outgoing.getStatus() == RECEIVE_FROM_FB_SUCCESS) {
				if (!udh) {
					String currentQueue = getSubmitSmQueueName();
					rabbitTemplate.convertAndSend(currentQueue, convertService.outgoingToJson(outgoing));
					log.info("Put message without UDH to {} successfully", currentQueue);
				} else {
					rabbitTemplate.convertAndSend(mbfSubmitSmQueue1, convertService.outgoingToJson(outgoing));
					log.info("Put message with UDH to default queue: {} successfully", mbfSubmitSmQueue1);
				}
			} else if (outgoing.getStatus() == DEST_ADDRESS_NOT_EXISTED_PROVIDER
					|| outgoing.getStatus() == DEST_ADDRESS_NOT_EXISTED_WHITELIST
					|| outgoing.getStatus() == DEST_ADDRESS_EXISTED_BLACKLIST
					|| outgoing.getStatus() == SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE
					|| outgoing.getStatus() == WHITELIST_EMPTY || outgoing.getStatus() == QUOTA_IS_OVER) {
				SubmitSmResp submitSmResp = new SubmitSmResp();
				submitSmResp.setSequenceNumber(outgoing.getSequenceNumber());
				submitSmResp.setCommandStatus(339); // read file ADA Error Code list.pdf
				String jsonSubmitSmResp = convertService.submitSmRespToJson(submitSmResp);
				rabbitTemplate.convertAndSend(mbfSubmitSmRespQueue, jsonSubmitSmResp);
			}
		} catch (Exception ex) {
			log.error("Error at putSubmitSmAndReceive function with message: {}", ex.getMessage());
		}
	}

	@Override
	public SubmitSmResp receiveSubmitSmResponse(String strSubmitSmResponse, long receivedSubmitSmDate) {
		SubmitSmResp submitSmResp = null;
		try {
			if (StringUtils.isBlank(strSubmitSmResponse)) {
				submitSmResp = new SubmitSmResp();
				submitSmResp.setCommandStatus(SmppConstants.STATUS_SUBMITFAIL);
			} else {
				submitSmResp = convertService.jsonToSubmitSmResp(strSubmitSmResponse);
			}
		} catch (Exception e) {
			log.warn("Error at receiveSubmitSmResponse function with message: {}", e.getMessage());
		}
		return submitSmResp;
	}

	@Override
	public String sendDeliverSm(SmppSession session, String strIncoming, long outQueueDate) {
		DeliverSmResp deliverSmResp = null;
		DeliverSm deliverSm;
		String jsonDeliverSmResp = "";
		IncomingEntity incoming = null;
		long receiveDeliverSmRespDate = 0L;
		try {
			incoming = convertService.castBytesArrayToIncoming(strIncoming);
			deliverSm = composeDeliverSm(incoming);
			if (session != null) {
				WindowFuture<Integer, PduRequest, PduResponse> future = session.sendRequestPdu(deliverSm, 10000, false);
				if (!future.await()) {
					log.warn("Failed to receive deliver_sm_resp within specified time");
				} else if (future.isSuccess()) {
					receiveDeliverSmRespDate = System.currentTimeMillis();
					deliverSmResp = (DeliverSmResp) future.getResponse();
					jsonDeliverSmResp = convertService.deliverSmRespToJson(deliverSmResp);
					log.info("deliver_sm_resp: commandStatus [" + deliverSmResp.getCommandStatus() + "="
							+ deliverSmResp.getResultMessage() + "]");
				} else {
					log.warn("Failed to properly receive deliver_sm_resp: " + future.getCause());
				}
			} else {
				log.warn("Session smpp server is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Error while send deliverSm and receive deliverSmResp with message: " + e.getMessage());
			if (deliverSmResp != null) {
				updateStatusIncoming(incoming, RECEIVE_DELIVER_SM_RESP_SUCCESS, e.getMessage(), outQueueDate,
						receiveDeliverSmRespDate);
			}
		} finally {
			if (deliverSmResp != null) {
				if (deliverSmResp.getCommandStatus() == SmppConstants.STATUS_OK) {
					updateStatusIncoming(incoming, RECEIVE_DELIVER_SM_RESP_SUCCESS, deliverSmResp.getResultMessage(),
							outQueueDate, receiveDeliverSmRespDate);
				} else {
					updateStatusIncoming(incoming, deliverSmResp.getCommandStatus(), deliverSmResp.getResultMessage(),
							outQueueDate, receiveDeliverSmRespDate);
				}
			} else {
				updateStatusIncoming(incoming, RECEIVE_DELIVER_SM_RESP_FAIL,
						DESCRIPTION.get(RECEIVE_DELIVER_SM_RESP_FAIL), outQueueDate, receiveDeliverSmRespDate);
			}
		}
		return jsonDeliverSmResp;
	}

	@Override
	public DeliverSm composeDeliverSm(IncomingEntity incoming) {
		DeliverSm deliverSm = null;
		try {
			deliverSm = new DeliverSm();
//            deliverSm.setRegisteredDelivery(SmppConstants.REGISTERED_DELIVERY_SMSC_RECEIPT_REQUESTED);
			deliverSm.setSourceAddress(new Address((byte) 0x05, (byte) 0x00, incoming.getSourceAddress()));
			deliverSm.setDestAddress(new Address((byte) 0x01, (byte) 0x01, incoming.getDestinationAddress()));
			deliverSm.setShortMessage(CharsetUtil.encode(incoming.getShortMessage(), CharsetUtil.CHARSET_UTF_8));
			deliverSm.setSequenceNumber(incoming.getSequenceNumber());
			log.debug("Compose DeliverSm successful");
		} catch (Exception e) {
			log.error("Error at composeDeliverSm function with message: {}", e.getMessage());
		}
		return deliverSm;
	}

	@Override
	public IncomingEntity updateStatusIncoming(IncomingEntity currentIncoming, int status, String description,
			long outQueueDate, long receivedDeliverSmRespDate) {
		try {
			currentIncoming.setStatus(status);
			currentIncoming.setDescription(description);
			currentIncoming.setOutQueueDate(outQueueDate);
			currentIncoming.setReceivedDeliverSmRespDate(receivedDeliverSmRespDate);

			DataCommandTo dataToSend = new DataCommandTo();
			dataToSend.setCommandType(DataCommandType.UPDATE_INCOMING);
			dataToSend.setData(currentIncoming);
			rabbitTemplate.convertAndSend(mbfDataServiceQueue, convertService.objectToJson(dataToSend));
			log.info("Put message to Data Service queue successfully");

			log.info("Update status after receive deliver sm resp resp successul");
		} catch (Exception e) {
			log.error("Error at updateStatusIncoming function with message: {}", e.getMessage());
		}
		return currentIncoming;
	}

	private String getSubmitSmQueueName() {
		String result = mbfSubmitSmQueue1;
		currentSubmitSmQueueNo++;
		if (currentSubmitSmQueueNo > 5) {
			currentSubmitSmQueueNo = 1;
		}
		switch (currentSubmitSmQueueNo) {
		case 1:
			result = mbfSubmitSmQueue1;
			break;
		case 2:
			result = mbfSubmitSmQueue2;
			break;
		case 3:
			result = mbfSubmitSmQueue3;
			break;
		case 4:
			result = mbfSubmitSmQueue4;
			break;
		case 5:
			result = mbfSubmitSmQueue5;
			break;
		default:
			break;
		}
		return result;
	}
}
