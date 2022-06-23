package com.services;

import java.util.List;

import com.models.WebSMSEntity;

public interface IWebSMSService {

	List<WebSMSEntity> findAll();

	boolean deleteById(long id);
	
	boolean clearAll();

}
