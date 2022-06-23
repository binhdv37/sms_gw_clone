package com.services.impl;

import com.services.IMessageTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static com.common.ConstanceQuery.MSG_TEMPLATE_FIND_ALL_QUERY;

@Service
@Slf4j
public class MessageTemplateServiceImpl implements IMessageTemplateService {

    public static final String REGEX_CURLY_BRACES = "\\{([^}]*?)\\}";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentHashMap<String, List<List<String>>> findAllMessageTemplate() {
        ArrayList<String> msgTemplate;
        ConcurrentHashMap<String, List<List<String>>> msgTemplateHashmap = new ConcurrentHashMap<>();
        try {
            Query query = entityManager.createNativeQuery(MSG_TEMPLATE_FIND_ALL_QUERY);
            msgTemplate = (ArrayList<String>) query.getResultList();
            for (String s : msgTemplate) {
                // cắt template theo regex
                ArrayList<String> templateList = new ArrayList<>(Arrays.asList(s.split(REGEX_CURLY_BRACES)));
                List<List<String>> msgTemplateList = new ArrayList<>();
                msgTemplateList.add(templateList.subList(1, templateList.size()));
                if (!msgTemplateHashmap.containsKey(templateList.get(0))) {
                    msgTemplateHashmap.put(templateList.get(0), msgTemplateList);
                } else {
                    List<List<String>> currentValue = msgTemplateHashmap.get(templateList.get(0));
                    currentValue.add(templateList.subList(1, templateList.size()));
                    msgTemplateHashmap.put(templateList.get(0), currentValue);
                }
            }
            log.debug("Find all message template successful");
        } catch (Exception e) {
            log.warn("Error at findAllMessageTemplate function with message: {}", e.getMessage());
        }
        return msgTemplateHashmap;
    }

    @Override
    public boolean existInMsgTemplate(String sourceAddress, String shortMessage, ConcurrentHashMap<String, List<String>> msgTemplate) {
        List<String> arrRegex;
        int from = 0;
        int maxSize;
        int avgSize;
        int to;
        ExecutorService executorService;
        try {
            long startValidateTime = System.currentTimeMillis();
            executorService = Executors.newFixedThreadPool(10);
            if (msgTemplate.containsKey(sourceAddress.toLowerCase())) {
                arrRegex = msgTemplate.get(sourceAddress.toLowerCase());
                maxSize = arrRegex.size();
                if (maxSize > 100) {
                    avgSize = maxSize / 10;
                } else {
                    avgSize = maxSize;
                }
                to = avgSize;
                for (int i = 0; i < 10; i++) {
                    TemplateCallable templateCallable;
                    if (i < 9) {
                        templateCallable = new TemplateCallable(shortMessage, arrRegex.subList(from, to));
                    } else {
                        templateCallable = new TemplateCallable(shortMessage, arrRegex.subList(from, arrRegex.size()));
                    }
                    Future<Boolean> futureResp = executorService.submit(templateCallable);
                    if (futureResp.get()) {
                        log.debug("Total processing time validate short messages: {}", System.currentTimeMillis() - startValidateTime);
                        executorService.shutdownNow();
                        return true;
                    }
                    from = to;
                    to = to + avgSize;
                    if (to > maxSize) {
                        break;
                    }
                }
            }
            if (!executorService.isShutdown()) {
                executorService.shutdownNow();
            }
            log.debug("Total processing time validate short messages: {}", System.currentTimeMillis() - startValidateTime);
        } catch (Exception e) {
            log.warn("Error at existInMsgTemplate function with message: {}", e.getMessage());
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentHashMap<String, List<String>> findAllMessageTemplateAndBrandName() {
        ArrayList<Object[]> queryResult;
        ConcurrentHashMap<String, List<String>> msgTemplateHashmap = new ConcurrentHashMap<>();
        try {
            Query query = entityManager.createNativeQuery(MSG_TEMPLATE_FIND_ALL_QUERY);
            queryResult = (ArrayList<Object[]>) query.getResultList();
            for (Object[] obj : queryResult) {
                if (msgTemplateHashmap.containsKey(obj[0].toString().toLowerCase())) {
                    List<String> arrRegex = msgTemplateHashmap.get(obj[0].toString().toLowerCase());
                    arrRegex.add(obj[1].toString());
                } else {
                    ArrayList<String> arrRegex = new ArrayList<>();
                    arrRegex.add(obj[1].toString().toLowerCase());
                    msgTemplateHashmap.put(obj[0].toString().toLowerCase(), arrRegex);
                }
            }
            log.debug("Find all message template successful");
        } catch (Exception e) {
            log.warn("Error at findAllMessageTemplate function with message: {}", e.getMessage());
        }
        return msgTemplateHashmap;
    }

    @Override
    public boolean compareWithRegex(String shortMessages, List<String> regexList) {
        log.debug("Short message: {}", shortMessages);
        log.debug("Size of regex template: {}", regexList.size());
        log.debug("Regex template: {}", regexList);
        try {
            for (String regex : regexList) {
                if (Pattern.matches(regex, shortMessages)) {
                    log.debug(">>>Short message: {} matched with regex: {}", shortMessages, regex);
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("Error at compareWithRegex function with message: {}", e.getMessage());
        }
        return false;
    }

}
