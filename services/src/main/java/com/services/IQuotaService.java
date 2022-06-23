package com.services;

import com.google.gson.internal.LinkedTreeMap;

import java.util.concurrent.ConcurrentHashMap;

public interface IQuotaService {

    boolean quotaActive();

    ConcurrentHashMap<String, LinkedTreeMap<String, String>> findAllQuota();

    boolean allowSendMessage(long receiveMsgDate, ConcurrentHashMap<String, LinkedTreeMap<String, String>> quota, ConcurrentHashMap<String, String> totalMsgSent);

}
