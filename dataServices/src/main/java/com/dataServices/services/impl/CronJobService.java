package com.dataServices.services.impl;

import com.dataServices.services.ICronJobService;
import com.google.gson.internal.LinkedTreeMap;
import com.models.SmppUserEntity;
import com.models.to.DataCommandTo;
import com.models.to.DataCommandTo.DataCommandType;
import com.services.*;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.services.impl.QuotaServiceImpl.QUOTA_END_VALID_DATE;
import static com.services.impl.QuotaServiceImpl.QUOTA_START_VALID_DATE;

@Service
@Slf4j
@EnableScheduling
public class CronJobService implements ICronJobService {

	@Value("${mbf.masterDataProducer.queueName}")
	private String masterDataProducerQueueName;

	@Value("${mbf.cron.time}")
	private static final int CRON_TIME = 60000;

	@Value("${mbf.cron.initDelay}")
	private static final int INIT_DELAY = 10000;

	private final IBlacklistService blacklistService;
//	private final IRoamingService roamingService;
	private final IWhitelistService whitelistService;
	private final IProviderService providerService;
	private final IMessageTemplateService messageTemplateService;
	private final IQuotaService quotaService;
	private final IOutgoingService outgoingService;
	private final IConvertService convertService;
	private final ISmppUserService smppUserService;
	private final RabbitTemplate rabbitTemplate;

	public static ConcurrentHashMap<String, String> hashMapSmppUser = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> prefixPhoneNumber = new ConcurrentHashMap<>();
	public static ArrayList<String> blacklists = new ArrayList<>();
	public static ArrayList<String> whitelists = new ArrayList<>();
	public static ConcurrentHashMap<String, List<String>> msgTemplate = new ConcurrentHashMap<>();
//	public static ConcurrentHashMap<String, List<List<String>>> msgTemplate = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, LinkedTreeMap<String, String>> quota = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> totalMsgSent = new ConcurrentHashMap<>();
	public static boolean whitelistActive;
	public static boolean quotaActive;

	@Autowired
	public CronJobService(IBlacklistService blacklistService, IRoamingService roamingService,
			IWhitelistService whitelistService, IProviderService providerService,
			IMessageTemplateService messageTemplateService, IQuotaService quotaService, RabbitTemplate rabbitTemplate,
			IOutgoingService outgoingService, IConvertService convertService, ISmppUserService smppUserService) {
		this.blacklistService = blacklistService;
//		this.roamingService = roamingService;
		this.whitelistService = whitelistService;
		this.providerService = providerService;
		this.messageTemplateService = messageTemplateService;
		this.quotaService = quotaService;
		this.outgoingService = outgoingService;
		this.convertService = convertService;
		this.smppUserService = smppUserService;

		this.rabbitTemplate = rabbitTemplate;
		this.rabbitTemplate.setUseDirectReplyToContainer(false);
	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY)
	public void cronSmppUser() {
		ArrayList<SmppUserEntity> smppUserEntities;
		ConcurrentHashMap<String, String> smppUserBackup = hashMapSmppUser;
		try {
			smppUserEntities = smppUserService.findAllSmppUser();
			hashMapSmppUser.clear();
			for (SmppUserEntity smppUser : smppUserEntities) {
				hashMapSmppUser.put(smppUser.getSystemId(), smppUser.getPassword());
			}
			log.info("Smpp user after cron: {}", hashMapSmppUser.toString());
			log.debug("Update hash map smpp user successful");
		} catch (Exception e) {
			log.error("Error at cronSmppUser in CronJobService with message: {}", e.getMessage());
			hashMapSmppUser.clear();
			hashMapSmppUser = smppUserBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_SMPP_USER, hashMapSmppUser);
	}

	/*
	 * @Override
	 * 
	 * @EventListener(ApplicationReadyEvent.class)
	 * 
	 * @Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY) public void
	 * cronRoaming() { ConcurrentHashMap<String, Long> roamingArrayList;
	 * ConcurrentHashMap<String, Long> roamingBackup = hashMapRoaming; try {
	 * roamingArrayList = roamingService.findAllRoaming(); hashMapRoaming.clear();
	 * hashMapRoaming = roamingArrayList; log.info("Roaming after cron: {}",
	 * hashMapRoaming.toString()); log.debug("Update hash map roaming successful");
	 * } catch (Exception e) {
	 * log.warn("Error at cronRoaming in CronJobService with message: {}",
	 * e.getMessage()); hashMapRoaming.clear(); hashMapRoaming = roamingBackup; } }
	 */

	@Override
	public void cronRoaming() {

	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 200)
	public void cronBlacklist() {
		ArrayList<String> blacklistArrL;
		ArrayList<String> blacklistBackup = blacklists;
		try {
			blacklistArrL = blacklistService.findAllBlacklist();
			blacklists.clear();
			blacklists = blacklistArrL;
			log.info("Blacklist after cron: {}", blacklists.toString());
			log.debug("Update array list blacklist successful");
		} catch (Exception e) {
			log.error("Error at cronBlacklist in CroJobService with message: {}", e.getMessage());
			blacklists.clear();
			blacklists = blacklistBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_BLACK_LIST, blacklists);
	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 400)
	public void cronWhitelist() {
		ArrayList<String> whitelistArrL;
		ArrayList<String> whitelistBackup = whitelists;
		try {
			whitelistArrL = whitelistService.findAllWhitelist();
			whitelists.clear();
			whitelists = whitelistArrL;
			log.info("Whitelist after cron: {}", whitelists.toString());
			log.debug("Update array list whitelist successful");
		} catch (Exception e) {
			log.error("Error at cronWhitelist in CroJobService with message: {}", e.getMessage());
			whitelists.clear();
			whitelists = whitelistBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_WHITE_LIST, whitelists);
	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 600)
	public void cronProvider() {
		ConcurrentHashMap<String, String> prefixPhoneNumberArrL;
		ConcurrentHashMap<String, String> prefixPhoneNumberBackup = prefixPhoneNumber;
		try {
			prefixPhoneNumberArrL = providerService.findAllPrefixPhoneNumber();
			prefixPhoneNumber.clear();
			prefixPhoneNumber = prefixPhoneNumberArrL;
			log.info("Prefix phone number after cron: {}", prefixPhoneNumber.toString());
			log.debug("Update array list Prefix phone number successful");
		} catch (Exception e) {
			log.error("Error at cronProvider in CroJobService with message: {}", e.getMessage());
			prefixPhoneNumber.clear();
			prefixPhoneNumber = prefixPhoneNumberBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_PROVIDER, prefixPhoneNumber);
	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 800)
	public void cronMsgTemplate() {
		ConcurrentHashMap<String, List<String>> msgTemplateHashmap;
		ConcurrentHashMap<String, List<String>> msgTemplateBackup = msgTemplate;
		try {
			msgTemplateHashmap = messageTemplateService.findAllMessageTemplateAndBrandName();
			msgTemplate.clear();
			msgTemplate = msgTemplateHashmap;
			log.info("Message template after cron: {}", msgTemplate.toString());
			log.debug("Update array list msgTemplate successful");
		} catch (Exception e) {
			log.error("Error at cronMsgTemplate in CroJobService with message: {}", e.getMessage());
			msgTemplate.clear();
			msgTemplate = msgTemplateBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_MSG_TEMPLATE, msgTemplate);
	}

	/*@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 800)
	public void cronMsgTemplate() {
		ConcurrentHashMap<String, List<List<String>>> msgTemplateHashmap;
		ConcurrentHashMap<String, List<List<String>>> msgTemplateBackup = msgTemplate;
		try {
*//*			ArrayList<Double> max = new ArrayList<>();
			for (String t : result) {
				double maxLength = Double.max("Your OTP code on Facebook is 123456".length(), t.length());
				double maxValue = (maxLength - StringUtils.getLevenshteinDistance("Your OTP code on Facebook is 123456", t)) / maxLength;
				max.add(maxValue);
			}
			double maxValue = Collections.max(max);*//*
			msgTemplateHashmap = messageTemplateService.findAllMessageTemplate();
			msgTemplate.clear();
			msgTemplate = msgTemplateHashmap;
			log.info("Message template after cron: {}", msgTemplate.toString());
			log.debug("Update array list msgTemplate successful");
		} catch (Exception e) {
			log.error("Error at cronMsgTemplate in CroJobService with message: {}", e.getMessage());
			msgTemplate.clear();
			msgTemplate = msgTemplateBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_MSG_TEMPLATE, msgTemplate);
	}*/

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 1000)
	public void cronWhitelistActive() {
		boolean active;
		boolean activeBackup = whitelistActive;
		try {
			active = whitelistService.whitelistActive();
			whitelistActive = active;
			log.info("Mode whitelist after cron: {}", whitelistActive);
			log.debug("Update mode whitelist successful");
		} catch (Exception e) {
			log.error("Error at cronWhitelistActive in CronJobService with message: {}", e.getMessage());
			whitelistActive = activeBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_WHITE_LIST_ACTIVE, whitelistActive);
	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 1100)
	public void cronQuotaActive() {
		boolean active;
		boolean activeBackup = quotaActive;
		try {
			active = quotaService.quotaActive();
			quotaActive = active;
			log.info("Mode quota after cron: {}", quotaActive);
			log.debug("Update mode quota successful");
		} catch (Exception e) {
			log.error("Error at cronQuotaActive in CronJobService with message: {}", e.getMessage());
			quotaActive = activeBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_QUOTA_ACTIVE, quotaActive);
	}

	@Override
	@EventListener(ApplicationReadyEvent.class)
	@Scheduled(fixedRate = CRON_TIME, initialDelay = INIT_DELAY + 1200)
	public void cronQuotaAndTotalMsgSent() {
		ConcurrentHashMap<String, LinkedTreeMap<String, String>> quotaHashmap;
		ConcurrentHashMap<String, LinkedTreeMap<String, String>> quotaHashmapBackup = quota;
		ConcurrentHashMap<String, String> totalMsgSentHashmap = new ConcurrentHashMap<>();
		ConcurrentHashMap<String, String> totalMsgSentHashmapBackup = totalMsgSent;
		try {
			// quota
			quotaHashmap = quotaService.findAllQuota();
			quota.clear();
			quota = quotaHashmap;
			log.info("Quota after cron: {}", quota.toString());
			log.debug("Update hash map quota successful");

			// total message sent
			quotaHashmap.forEach((code, objQuota) -> {
				String totalMsgSent = outgoingService.countAllByDateRange(Long.parseLong(objQuota.get(QUOTA_START_VALID_DATE)),
						Long.parseLong(objQuota.get(QUOTA_END_VALID_DATE)));
				totalMsgSentHashmap.put(code, totalMsgSent);
			});
			totalMsgSent.clear();
			totalMsgSent = totalMsgSentHashmap;
			log.info("Total message sent after cron: {}", totalMsgSent);
			log.debug("Update hash map total message sent successful");
		} catch (Exception e) {
			log.error("Error at cronQuotaAndTotalMsgSent in CroJobService with message: {}", e.getMessage());
			quota.clear();
			quota = quotaHashmapBackup;
			totalMsgSent.clear();
			totalMsgSent = totalMsgSentHashmapBackup;
		}

		sendToQueues(DataCommandType.MASTERDATA_QUOTA, quota);
		sendToQueues(DataCommandType.MASTERDATA_QUOTA_TOTAL_SENT, totalMsgSent);
	}

	private void sendToQueues(DataCommandType type, Object data) {
		DataCommandTo command = new DataCommandTo();
		command.setCommandType(type);
		command.setData(data);

		String jsonStr = convertService.objectToJson(command);

		String producerResponse = (String) rabbitTemplate.convertSendAndReceive(masterDataProducerQueueName, jsonStr);
		log.info("Put message to queue successfully and producerResponse is: {}", producerResponse);
	}
}
