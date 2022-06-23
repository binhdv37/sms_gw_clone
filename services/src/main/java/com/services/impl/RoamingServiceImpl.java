package com.services.impl;

import com.services.IRoamingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.common.ConstanceQuery.ROAMING_FIND_ALL_QUERY;
import static com.common.ConstanceStatus.DEST_ADDRESS_NOT_EXISTED_ROAMING;

@Service
@Slf4j
public class RoamingServiceImpl implements IRoamingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentHashMap<String, Long> findAllRoaming() {
        ConcurrentHashMap<String, Long> roaming = new ConcurrentHashMap<>();
        List<Object[]> objectList;
        try {
            Query query = entityManager.createNativeQuery(ROAMING_FIND_ALL_QUERY);
            objectList = query.getResultList();
            for (Object[] obj : objectList) {
                roaming.put(obj[0].toString(), Long.parseLong(obj[1].toString()));
            }
        } catch (Exception e) {
            log.warn("Error at findAllRoaming function with message: {}", e.getMessage());
        }
        return roaming;
    }

    @Override
    public int existInRoaming(String destinationAddress, ConcurrentHashMap<String, Long> roaming) {
        int existed = DEST_ADDRESS_NOT_EXISTED_ROAMING;
        log.trace("Go into existInRoaming function");
        log.info("Destination address: {}", destinationAddress);
        log.info("Hash map roaming: {}", roaming.toString());
        try {
            if (roaming.size() > 0) {
                if (roaming.containsKey(destinationAddress)) {
                    existed = Math.toIntExact(roaming.get(destinationAddress));
                    log.info("Destination address existed in roaming, toProviderId is: {}", existed);
                } else {
                    log.info("Destination address not exist in roaming");
                }
            } else {
                log.info("Hash map roaming is empty");
            }
        } catch (Exception e) {
            log.warn("Error at existInRoaming function with message: {}", e.getMessage());
        }
        log.trace("Exit existInRoaming function");
        return existed;
    }
}
