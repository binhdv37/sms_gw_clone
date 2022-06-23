package com.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "temp_message", schema = "public", catalog = "sms_gateway")
public class WebSMSEntity {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "created_date", nullable = false)
	private long createdDate;

	@Column(name = "destination_address", nullable = false)
	private String destinationAddress;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "quota_id", nullable = false)
	private Long quotaId;

	@Column(name = "short_message", length = 1000000, nullable = false)
	private String shortMessage;

	@Column(name = "source_address", nullable = false)
	private String sourceAddress;

	@Column(name = "status", nullable = false, length = 10)
	private int status;

	@Column(name = "ticket_id ", nullable = false)
	private Long ticketId;

}
