package com.services.impl;

import com.google.gson.internal.LinkedTreeMap;
import com.services.IQuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.common.ConstanceQuery.QUOTA_ACTIVE;
import static com.common.ConstanceQuery.QUOTA_FIND_ALL_QUERY;

@Service
@Slf4j
public class QuotaServiceImpl implements IQuotaService {

    public static final String QUOTA_START_VALID_DATE = "startValidDate";
    public static final String QUOTA_END_VALID_DATE = "endValidDate";
    public static final String QUOTA_TOTAL = "totalQuota";
    public static final String QUOTA_WARNING_THRESHOLD = "warningThreshold";
    public static final String QUOTA_CODE = "quotaCode";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean quotaActive() {
        boolean active = false;
        try {
            Query query = entityManager.createNativeQuery(QUOTA_ACTIVE);
            active = (boolean) query.getSingleResult();
        } catch (Exception e) {
            log.warn("Error at quotaActive function with message: {}", e.getMessage());
        }
        return active;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentHashMap<String, LinkedTreeMap<String, String>> findAllQuota() {
        ConcurrentHashMap<String, LinkedTreeMap<String, String>> quotaHashmap = new ConcurrentHashMap<>();
        List<Object[]> quotaArrL;
        Query query = entityManager.createNativeQuery(QUOTA_FIND_ALL_QUERY);
        quotaArrL = query.getResultList();
        for (Object[] o : quotaArrL) {
            LinkedTreeMap<String, String> quota = new LinkedTreeMap<>();
            quota.put(QUOTA_START_VALID_DATE, o[0].toString());
            quota.put(QUOTA_END_VALID_DATE, o[1].toString());
            quota.put(QUOTA_TOTAL, o[2].toString());
            quota.put(QUOTA_WARNING_THRESHOLD, o[3].toString());
            quota.put(QUOTA_CODE, o[4].toString());
            quotaHashmap.put(quota.get(QUOTA_CODE), quota);
        }
        return quotaHashmap;
    }

    @Override
    public boolean allowSendMessage(long receiveMsgDate, ConcurrentHashMap<String, LinkedTreeMap<String, String>> quota, ConcurrentHashMap<String, String> totalMsgSent) {
        AtomicBoolean allowSend = new AtomicBoolean(false);
        AtomicLong remainingMsg = new AtomicLong();
        quota.forEach((quotaCode, objQuota) -> {
            if (receiveMsgDate >= Long.parseLong(objQuota.get(QUOTA_START_VALID_DATE)) &&
                    receiveMsgDate <= Long.parseLong(objQuota.get(QUOTA_END_VALID_DATE))) {
                remainingMsg.set(Long.parseLong(objQuota.get(QUOTA_TOTAL)) - Long.parseLong(totalMsgSent.get(quotaCode)));
                if (remainingMsg.get() > 0) {
                    allowSend.set(true);
                    totalMsgSent.replace(quotaCode, totalMsgSent.get(quotaCode) + 1);
                }
            }
        });
        return allowSend.get();
    }
}
