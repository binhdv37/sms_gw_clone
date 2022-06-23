package com.services;

import java.util.concurrent.ConcurrentHashMap;

public interface IRoamingService {

    ConcurrentHashMap<String, Long> findAllRoaming();

    int existInRoaming(String destinationAddress, ConcurrentHashMap<String, Long> roaming);
}
