package com.repositories;

import com.models.IncomingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncomingRepository extends JpaRepository<IncomingEntity, Long> {

	@Query("SELECT i FROM IncomingEntity i WHERE i.uuid = :uuid")
	IncomingEntity findByUUID(@Param("uuid") String uuid);

}
