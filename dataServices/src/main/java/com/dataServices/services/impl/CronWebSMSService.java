package com.dataServices.services.impl;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dataServices.services.ICronWebSMSService;
import com.models.OutgoingEntity;
import com.models.WebSMSEntity;
import com.services.IConvertService;
import com.services.IOutgoingService;
import com.services.IProviderService;
import com.services.IWebSMSService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@EnableScheduling
public class CronWebSMSService implements ICronWebSMSService {
	
	@Value("${mbf.submitSmWeb.queueName}")
	private String submitSmWebQueue;

	@Value("${mbf.cron.time}")
	private static final int CRON_TIME = 60000;

	@Value("${mbf.cron.initDelay}")
	private static final int INIT_DELAY = 10000;

	private final IConvertService convertService;
	private final IWebSMSService webSMSService;
	private final IOutgoingService outgoingService;
	private final IProviderService providerService;

	private final RabbitTemplate rabbitTemplate;

	@Autowired
	public CronWebSMSService(IConvertService convertService, RabbitTemplate rabbitTemplate,
			IOutgoingService outgoingService, IWebSMSService webSMSService, IProviderService providerService) {
		this.convertService = convertService;
		this.webSMSService = webSMSService;
		this.outgoingService = outgoingService;
		this.providerService = providerService;
		this.rabbitTemplate = rabbitTemplate;
		this.rabbitTemplate.setUseDirectReplyToContainer(false);
	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY)
	public void cronHandleWebSMS() {
		List<WebSMSEntity> entities = webSMSService.findAll();
		log.info("Temp message after cron: {}", entities);
		if (entities != null && !entities.isEmpty()) {
			for (WebSMSEntity entity : entities) {
				try {
					OutgoingEntity outgoing = outgoingService.initOutgoing(entity, System.currentTimeMillis());
					long providerId = providerService.existInProvider(outgoing.getDestinationAddress(),
							CronJobService.prefixPhoneNumber);
					outgoing.setProviderId(providerId);

					outgoingService.save(outgoing);

					String jsonStr = (String) rabbitTemplate.convertSendAndReceive(submitSmWebQueue,
							convertService.outgoingToJson(outgoing));
					log.info("Put message to queue successfully and submitSmResp is: {}", jsonStr);
					// Low priority, so sleep 500ms before sending each message to queue
					Thread.sleep(500);
				} catch (InterruptedException e) {
					log.error("Error at putSubmitSmAndReceive function with message: {}", e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					log.error("Error at putSubmitSmAndReceive function with message: {}", e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
}
