package com.services.impl;

import com.services.IWhitelistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;

import static com.common.ConstanceQuery.WHITELIST_ACTIVE;
import static com.common.ConstanceQuery.WHITELIST_FIND_ALL_QUERY;
import static com.common.ConstanceStatus.*;

@Service
@Slf4j
public class WhitelistServiceImpl implements IWhitelistService {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@SuppressWarnings("unchecked")
	public ArrayList<String> findAllWhitelist() {
		ArrayList<String> whitelist = new ArrayList<>();
		try {
			Query query = entityManager.createNativeQuery(WHITELIST_FIND_ALL_QUERY);
			whitelist = (ArrayList<String>) query.getResultList();
		} catch (Exception e) {
			log.warn("Error at findAllWhitelist function with message: {}", e.getMessage());
		}
		return whitelist;
	}

	@Override
	public int existInWhitelist(String destinationAddress, ArrayList<String> whitelist) {
		int existed = WHITELIST_EMPTY;
		log.info("Destination address: {} and whitelist before check: {}", destinationAddress, whitelist.toString());
		try {
			if (whitelist != null && whitelist.size() > 0) {
				if (whitelist.contains(destinationAddress)) {
					existed = DEST_ADDRESS_EXISTED_WHITELIST;
					log.info("Destination existed in whitelist");
				} else {
					existed = DEST_ADDRESS_NOT_EXISTED_WHITELIST;
					log.info("Destination not exist in whitelist");
				}
			} else {
				log.info("Whitelist is empty");
			}
		} catch (Exception e) {
			log.warn("Error at existInWhitelist function with message: {}", e.getMessage());
		}
		return existed;
	}

	@Override
	public boolean whitelistActive() {
		boolean active = false;
		try {
			Query query = entityManager.createNativeQuery(WHITELIST_ACTIVE);
			active = (boolean) query.getSingleResult();
		} catch (Exception e) {
			log.warn("Error at whitelistActive function with message: {}", e.getMessage());
		}
		return active;
	}

}
