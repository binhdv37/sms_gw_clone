package com.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.models.OutgoingEntity;

public interface OutgoingRepository extends JpaRepository<OutgoingEntity, Long> {

	@Query("SELECT COUNT(o) FROM OutgoingEntity o WHERE o.deleted = :deleted " 
			+ "AND o.sentToMbf = :sent "
			+ "AND (:startValidTime IS NULL OR o.createdDate >= :startValidTime) "
			+ "AND (:endValidTime IS NULL OR o.createdDate <= :endValidTime)")
	long countAllByDateRange(@Param("deleted") boolean deleted, @Param("sent") boolean sent,
			@Param("startValidTime") Long startValidTime, @Param("endValidTime") Long endValidTime);

	@Query("SELECT o FROM OutgoingEntity o WHERE o.uuid = :uuid")
	OutgoingEntity findByUUID(@Param("uuid") String uuid);
	
}
