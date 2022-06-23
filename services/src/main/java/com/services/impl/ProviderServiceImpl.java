package com.services.impl;

import com.models.Provider;
import com.services.IProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.common.ConstanceQuery.PREFIX_PHONE_NUMBER_FIND_ALL_QUERY;
import static com.common.ConstanceStatus.DEST_ADDRESS_NOT_EXISTED_PROVIDER;

@Slf4j
@Service
public class ProviderServiceImpl implements IProviderService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentHashMap<String, String> findAllPrefixPhoneNumber() {
        ConcurrentHashMap<String, String> prefixPhoneNumber = new ConcurrentHashMap<>();
        List<Object[]> objectList;
        try {
            Query query = entityManager.createNativeQuery(PREFIX_PHONE_NUMBER_FIND_ALL_QUERY);
            objectList = query.getResultList();
            prefixPhoneNumber = convertListToHashMap(objectList);
        } catch (Exception e) {
            log.warn("Error at findAllPrefixPhoneNumber function with message: {}", e.getMessage());
        }
        return prefixPhoneNumber;
    }

    @Override
	public int existInProvider(String destinationAddress, ConcurrentHashMap<String, String> prefixPhoneNumber) {
		int existed = DEST_ADDRESS_NOT_EXISTED_PROVIDER;
		if (prefixPhoneNumber != null && prefixPhoneNumber.size() > 0) {
		    log.debug("Destination address: {}, prefix phone number {} and hash map prefix phone number before validate: {}", destinationAddress, destinationAddress.substring(0, 3), prefixPhoneNumber);
			if (prefixPhoneNumber.containsKey(destinationAddress.substring(0, 3))) {
				existed = Integer.parseInt(prefixPhoneNumber.get(destinationAddress.substring(0, 3)));
				log.info("Prefix phone number: {}, providerId: {}", destinationAddress.substring(0, 3),
						prefixPhoneNumber.get(destinationAddress.substring(0, 3)));
			} else {
				log.info("Prefix phone number not exist in hash map prefixPhoneNumber");
			}
		} else {
			log.info("Hash map prefixPhoneNumber is empty");
		}
		return existed;
	}

    private ConcurrentHashMap<String, String> convertListToHashMap(List<Object[]> objectList) {
        ConcurrentHashMap<String, String> prefixPhoneNumber = new ConcurrentHashMap<>();
        ArrayList<Provider> providers = new ArrayList<>();
        log.trace("Go into convertListToHashMap function");
        try {
            for (Object[] obj : objectList) {
                Provider p = new Provider();
                p.setId(obj[0].toString());
                p.setPrefixPhoneNumber(obj[1].toString());
                providers.add(p);
            }
            providers.forEach(x -> {
                List<String> prefix = Arrays.asList(x.getPrefixPhoneNumber().split(","));
                prefix.forEach(y -> {
                    prefixPhoneNumber.put(y, x.getId());
                });
            });
            log.info("Convert list object[] to hash map successful");
        } catch (Exception e) {
            log.warn("Error at convertListToHashMap function with message: {}", e.getMessage());
        }
        log.trace("Exit convertListToHashMap function");
        return prefixPhoneNumber;
    }
}
