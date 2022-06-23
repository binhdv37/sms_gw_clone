package com.services;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public interface IMessageTemplateService {

    ConcurrentHashMap<String, List<List<String>>> findAllMessageTemplate();

    boolean existInMsgTemplate(String sourceAddress, String shortMessage, ConcurrentHashMap<String, List<String>> msgTemplate);

    ConcurrentHashMap<String, List<String>> findAllMessageTemplateAndBrandName();

    boolean compareWithRegex(String shortMessages, List<String> regexList);

}
