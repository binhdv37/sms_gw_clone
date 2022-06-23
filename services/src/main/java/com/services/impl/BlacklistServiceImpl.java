package com.services.impl;

import com.services.IBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;

import static com.common.ConstanceQuery.BLACKLIST_FIND_ALL_QUERY;
import static com.common.ConstanceStatus.DEST_ADDRESS_EXISTED_BLACKLIST;
import static com.common.ConstanceStatus.DEST_ADDRESS_NOT_EXISTED_BLACKLIST;

@Service
@Slf4j
public class BlacklistServiceImpl implements IBlacklistService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public ArrayList<String> findAllBlacklist() {
        ArrayList<String> blacklists = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(BLACKLIST_FIND_ALL_QUERY);
            blacklists = (ArrayList<String>) query.getResultList();
        } catch (Exception e) {
            log.warn("Error at findAllBlacklist function with message: {}", e.getMessage());
        }
        return blacklists;
    }

    @Override
	public int existInBlacklist(String destinationAddress, ArrayList<String> blacklist) {
		int existed = DEST_ADDRESS_NOT_EXISTED_BLACKLIST;
		log.debug("Destination address: {} and blacklist before check: {}", destinationAddress, blacklist.toString());
		try {
			if (blacklist != null && blacklist.size() > 0) {
				if (blacklist.contains(destinationAddress)) {
					existed = DEST_ADDRESS_EXISTED_BLACKLIST;
					log.info("Destination address existed in blacklist");
				} else {
					log.info("Destination address not exist in blacklist");
				}
			} else {
				log.info("Blacklist is empty");
			}
		} catch (Exception e) {
			log.warn("Error at existInBlacklist function with message: {}", e.getMessage());
		}
		return existed;
	}
}
