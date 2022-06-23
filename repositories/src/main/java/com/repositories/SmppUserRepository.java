package com.repositories;

import com.models.SmppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface SmppUserRepository extends JpaRepository<SmppUserEntity, Long> {

    ArrayList<SmppUserEntity> findAllByDeleted(boolean deleted);
}
