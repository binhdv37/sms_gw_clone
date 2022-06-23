package com.dataServices.services.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.dataServices.services.IMessageHandlingDataService;
import com.models.IncomingEntity;
import com.models.OutgoingEntity;
import com.models.to.DataCommandTo;
import com.services.IConvertService;
import com.services.IIncomingService;
import com.services.IOutgoingService;
import com.services.IWebSMSService;

import lombok.extern.slf4j.Slf4j;
import static com.common.ConstanceStatus.*;

@Service
@Slf4j
public class MessageHandlingDataServiceImp implements IMessageHandlingDataService {

	@Value("${mbf.dataService.queueName}")
	private String mbfDataServiceQueue;

	private int MAX_RETRY_TIMES = 10;

	private final IOutgoingService outgoingService;
	private final IConvertService convertService;
	private final IIncomingService incomingService;
	private final IWebSMSService webSMSService;
	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public MessageHandlingDataServiceImp(IOutgoingService outgoingService, IConvertService convertService,
			RabbitTemplate rabbitTemplate, IWebSMSService webSMSService, IIncomingService incomingService) {
		this.outgoingService = outgoingService;
		this.convertService = convertService;
		this.incomingService = incomingService;
		this.webSMSService = webSMSService;
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitTemplate.setUseDirectReplyToContainer(false);
	}

	@Override
	public void handleDataCommand(String in, long outQueueDate) {
		DataCommandTo data = this.convertService.castBytesArrayToDataCommandTo(in);
		if (data != null) {
			log.info("Received data command with type: {}", data.getCommandType().name());
			IncomingEntity incomingEntity = null;
			OutgoingEntity outgoingEntity = null;
			switch (data.getCommandType()) {
			case ADD_INCOMING:
				incomingEntity = this.incomingService.save(data.getIncomingData());
				break;
			case UPDATE_INCOMING:
				incomingEntity = updateIncomming(data.getIncomingData());
				if (incomingEntity != null) {
					this.incomingService.save(incomingEntity);
				} else {
					int retriedTimes = data.getRetriedTimes();
					if (retriedTimes < MAX_RETRY_TIMES) {
						data.setRetriedTimes(retriedTimes + 1);
						// Requeue in case incomming message has not been created in db
						this.rabbitTemplate.convertAndSend(mbfDataServiceQueue, convertService.objectToJson(data));
					} else {
						// After 10 retried times, insert a new record
						// In case the ADD_OUTGOING message was lost from queue
						this.incomingService.save(data.getIncomingData());
					}
				}
				break;

			case ADD_OUTGOING:
				outgoingEntity = this.outgoingService.save(data.getOutgoingData());
				break;
			case UPDATE_OUTGOING:
				outgoingEntity = updateOutgoing(data.getOutgoingData());
				if (outgoingEntity != null) {
					outgoingEntity = this.outgoingService.save(outgoingEntity);
					removeWebSMSRecord(outgoingEntity);
				} else {
					int retriedTimes = data.getRetriedTimes();
					if (retriedTimes < MAX_RETRY_TIMES) {
						System.out.println("RETRIED: " + retriedTimes);
						data.setRetriedTimes(retriedTimes + 1);
						// Requeue in case outgoing message has not been created in db
						this.rabbitTemplate.convertAndSend(mbfDataServiceQueue, convertService.objectToJson(data));
					} else {
						// After 5 retried time, insert a new record
						// In case the ADD_OUTGOING message was lost from queue
						outgoingEntity = this.outgoingService.save(data.getOutgoingData());
						removeWebSMSRecord(outgoingEntity);
					}
				}
				break;

			default:
				break;
			}
		}
	}

	private void removeWebSMSRecord(OutgoingEntity outgoingEntity) {
		if (outgoingEntity != null && !outgoingEntity.getDescription().isEmpty()
				&& outgoingEntity.getDescription().contains(DESCRIPTION.get(RECEIVE_FROM_WEB_SUCCESS))) {
			String[] tempArr = outgoingEntity.getDescription().split("=");
			if (tempArr.length == 2) {
				this.webSMSService.deleteById(Long.valueOf(tempArr[1]));
			} else {
				tempArr = outgoingEntity.getDescription().split("\\u003d");
				if (tempArr.length == 2) {
					this.webSMSService.deleteById(Long.valueOf(tempArr[1]));
				}
			}
		}
	}

	private IncomingEntity updateIncomming(IncomingEntity data) {
		IncomingEntity entity = null;
		if (data != null) {
			entity = this.incomingService.findByUUID(data.getUuid());
			if (entity != null) {
				entity.setCreatedDate(data.getCreatedDate());
				entity.setDeleted(data.isDeleted());
				entity.setDescription(data.getDescription());
				entity.setDestinationAddress(data.getDestinationAddress());
				entity.setInQueueDate(data.getInQueueDate());
				entity.setMessageId(data.getMessageId());
				entity.setOutQueueDate(data.getOutQueueDate());
				entity.setReceivedDeliverSmDate(data.getReceivedDeliverSmDate());
				entity.setReceivedDeliverSmRespDate(data.getReceivedDeliverSmRespDate());
				entity.setSequenceNumber(data.getSequenceNumber());
				entity.setShortMessage(data.getShortMessage());
				entity.setSourceAddress(data.getSourceAddress());
				entity.setStatus(data.getStatus());
				entity.setUpdatedDate(data.getUpdatedDate());
			}
		}
		return entity;
	}

	private OutgoingEntity updateOutgoing(OutgoingEntity data) {
		OutgoingEntity entity = null;
		if (data != null) {
			entity = this.outgoingService.findByUUID(data.getUuid());
			if (entity != null) {
				entity.setActionFailureDetails(data.getActionFailureDetails());
				entity.setActionStatus(data.getActionStatus());
				entity.setCreatedDate(data.getCreatedDate());
				entity.setDeleted(data.isDeleted());
				entity.setDescription(data.getDescription());
				entity.setDestinationAddress(data.getDestinationAddress());
				entity.setInQueueDate(data.getInQueueDate());
				entity.setMessageId(data.getMessageId());
				entity.setOutQueueDate(data.getOutQueueDate());
				entity.setProviderId(data.getProviderId());
				entity.setReceivedSubmitSmDate(data.getReceivedSubmitSmDate());
				entity.setReceivedSubmitSmRespDate(data.getReceivedSubmitSmRespDate());
				entity.setSentToMbf(data.isSentToMbf());
				entity.setSequenceNumber(data.getSequenceNumber());
				entity.setShortMessage(data.getShortMessage());
				entity.setSourceAddress(data.getSourceAddress());
				entity.setStatus(data.getStatus());
				entity.setUpdatedDate(data.getUpdatedDate());
				entity.setViolationKeyword(data.getViolationKeyword());
			}
		}
		return entity;
	}
}
