package com.producers.services.impl;

import static com.producers.services.impl.MessageHandlingProducersServiceImpl.blacklists;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.hashMapSmppUser;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.msgTemplate;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.prefixPhoneNumber;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.quota;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.quotaActive;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.totalMsgSent;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.whitelistActive;
import static com.producers.services.impl.MessageHandlingProducersServiceImpl.whitelists;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.models.to.DataCommandTo;
import com.producers.services.IMessageHandlingMasterDataChanged;
import com.services.IConvertService;

@Service
public class MessageHandlingMasterDataChangedImpl implements IMessageHandlingMasterDataChanged {

	private final IConvertService convertService;

	@Autowired
	public MessageHandlingMasterDataChangedImpl(IConvertService convertService) {
		this.convertService = convertService;
	}

	@Override
	public String handleMessage(String in, long outQueueTime) {
		if (!StringUtils.isBlank(in)) {
			DataCommandTo to = convertService.castBytesArrayToDataCommandTo(in);
			if (to != null && to.getData() != null && to.getCommandType() != null) {
				switch (to.getCommandType()) {
				case MASTERDATA_BLACK_LIST:
					blacklists = to.getBlacklist();
					break;
				case MASTERDATA_MSG_TEMPLATE:
					msgTemplate = to.getMsgTemplate();
					break;
				case MASTERDATA_PROVIDER:
					prefixPhoneNumber = to.getProvider();
					break;
				case MASTERDATA_QUOTA:
					quota = to.getQuota();
					break;
				case MASTERDATA_QUOTA_ACTIVE:
					quotaActive = to.getQuotaActive();
					break;
				case MASTERDATA_QUOTA_TOTAL_SENT:
					totalMsgSent = to.getTotalMsgSent();
					break;
				case MASTERDATA_SMPP_USER:
					hashMapSmppUser = to.getSMPPUser();
					break;
				case MASTERDATA_WHITE_LIST:
					whitelists = to.getWhitelist();
					break;
				case MASTERDATA_WHITE_LIST_ACTIVE:
					whitelistActive = to.getWhitelistActive();
					break;

				default:
					break;
				}
				return "true";
			} else {
				return "false";
			}
		} else {
			return "false";
		}
	}
}
