package com.services.impl;

import com.models.SmppUserEntity;
import com.repositories.SmppUserRepository;
import com.services.ISmppUserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
public class SmppUserServiceImpl implements ISmppUserService {

	private final SmppUserRepository smppUserRepository;

	@Autowired
	public SmppUserServiceImpl(SmppUserRepository smppUserRepository) {
		this.smppUserRepository = smppUserRepository;
	}

	@Override
	public ArrayList<SmppUserEntity> findAllSmppUser() {
		ArrayList<SmppUserEntity> smppUserEntities = null;
		try {
			smppUserEntities = smppUserRepository.findAllByDeleted(false);
		} catch (Exception e) {
			log.warn("Error at findAllSmppUser function with message: {}", e.getMessage());
		}
		return smppUserEntities;
	}
}
