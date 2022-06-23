package com.services;

import java.util.concurrent.ConcurrentHashMap;

public interface IProviderService {

    ConcurrentHashMap<String, String> findAllPrefixPhoneNumber();

    int existInProvider(String destinationAddress, ConcurrentHashMap<String, String> prefixPhoneNumber);

}
