package com.services;

import com.models.SmppUserEntity;

import java.util.ArrayList;

public interface ISmppUserService {

    ArrayList<SmppUserEntity> findAllSmppUser();
}
