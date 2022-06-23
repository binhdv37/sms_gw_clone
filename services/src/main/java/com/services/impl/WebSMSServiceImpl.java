package com.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.models.WebSMSEntity;
import com.repositories.WebSMSRepository;
import com.services.IWebSMSService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WebSMSServiceImpl implements IWebSMSService {

	private final WebSMSRepository webSMSRepository;

	@Autowired
	public WebSMSServiceImpl(WebSMSRepository webSMSRepository) {
		this.webSMSRepository = webSMSRepository;
	}

	@Override
	public List<WebSMSEntity> findAll() {
		try {
			return webSMSRepository.findAll();
		} catch (Exception e) {
			log.warn("Error at findAll function with message: {}", e.getMessage());
			return null;
		}
	}

	@Override
	public boolean clearAll() {
		try {
			webSMSRepository.deleteAll();
			return true;
		} catch (Exception e) {
			log.warn("Error at clearAll function with message: {}", e.getMessage());
			return false;
		}
	}

	@Override
	public boolean deleteById(long id) {
		try {
			webSMSRepository.deleteById(id);
			return true;
		} catch (Exception e) {
			log.warn("Error at deleteById function with message: {}", e.getMessage());
			return false;
		}
	}
}
