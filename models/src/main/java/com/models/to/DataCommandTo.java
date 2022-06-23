package com.models.to;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.models.IncomingEntity;
import com.models.OutgoingEntity;
import com.models.Quota;

import lombok.Data;

@Data
public class DataCommandTo {

	public static enum DataCommandType {
		ADD_INCOMING, UPDATE_INCOMING, ADD_OUTGOING, UPDATE_OUTGOING, MASTERDATA_SMPP_USER, MASTERDATA_BLACK_LIST,
		MASTERDATA_WHITE_LIST, MASTERDATA_PROVIDER, MASTERDATA_MSG_TEMPLATE, MASTERDATA_WHITE_LIST_ACTIVE,
		MASTERDATA_QUOTA_ACTIVE, MASTERDATA_QUOTA, MASTERDATA_QUOTA_TOTAL_SENT
	}

	private DataCommandType commandType;

	private Object data;
	
	private int retriedTimes;

	@JsonIgnore
	public IncomingEntity getIncomingData() {
		if (this.data != null && this.data instanceof LinkedTreeMap) {
			String jsonStr = new Gson().toJson((LinkedTreeMap<String, String>) this.data);
			return new Gson().fromJson(jsonStr, IncomingEntity.class);
		}
		return null;
	}

	@JsonIgnore
	public OutgoingEntity getOutgoingData() {
		if (this.data != null && this.data instanceof LinkedTreeMap) {
			String jsonStr = new Gson().toJson((LinkedTreeMap<String, String>) this.data);
			return new Gson().fromJson(jsonStr, OutgoingEntity.class);
		}
		return null;
	}

	@JsonIgnore
	public ConcurrentHashMap<String, String> getSMPPUser() {
		if (this.data != null && this.data instanceof LinkedTreeMap) {
			String jsonStr = new Gson().toJson((LinkedTreeMap<String, String>) this.data);
			return new Gson().fromJson(jsonStr, ConcurrentHashMap.class);
		}
		return null;
	}

	@JsonIgnore
	public ArrayList<String> getBlacklist() {
		if (this.data != null && this.data instanceof ArrayList) {
			return (ArrayList<String>) this.data;
		}
		return null;
	}

	@JsonIgnore
	public ArrayList<String> getWhitelist() {
		if (this.data != null && this.data instanceof ArrayList) {
			return (ArrayList<String>) this.data;
		}
		return null;
	}

	@JsonIgnore
	public ConcurrentHashMap<String, String> getProvider() {
		if (this.data != null && this.data instanceof LinkedTreeMap) {
			String jsonStr = new Gson().toJson((LinkedTreeMap<String, String>) this.data);
			return new Gson().fromJson(jsonStr, ConcurrentHashMap.class);
		}
		return null;
	}

	@JsonIgnore
	public ConcurrentHashMap<String, List<String>> getMsgTemplate() {
		if (this.data != null && this.data instanceof LinkedTreeMap) {
			String jsonStr = new Gson().toJson((LinkedTreeMap<String, List<String>>) this.data);
			return new Gson().fromJson(jsonStr, ConcurrentHashMap.class);
		}
		return null;
	}

	@JsonIgnore
	public Boolean getWhitelistActive() {
		if (this.data != null && this.data instanceof Boolean) {
			return (boolean) this.data;
		}
		return null;
	}

	@JsonIgnore
	public Boolean getQuotaActive() {
		if (this.data != null && this.data instanceof Boolean) {
			return (boolean) this.data;
		}
		return null;
	}

	@JsonIgnore
	public ConcurrentHashMap<String, LinkedTreeMap<String, String>> getQuota() {
		if (this.data != null && this.data instanceof LinkedTreeMap) {
			String jsonStr = new Gson().toJson(this.data);
			return new Gson().fromJson(jsonStr, ConcurrentHashMap.class);
		}
		return null;
	}

	@JsonIgnore
	public ConcurrentHashMap<String, String> getTotalMsgSent() {
		if (this.data != null && this.data instanceof LinkedTreeMap) {
			String jsonStr = new Gson().toJson((LinkedTreeMap<String, String>) this.data);
			return new Gson().fromJson(jsonStr, ConcurrentHashMap.class);
		}
		return null;
	}
}
