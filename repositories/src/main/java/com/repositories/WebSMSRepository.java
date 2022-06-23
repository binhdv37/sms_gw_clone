package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.models.WebSMSEntity;

public interface WebSMSRepository extends JpaRepository<WebSMSEntity, Long> {
}
