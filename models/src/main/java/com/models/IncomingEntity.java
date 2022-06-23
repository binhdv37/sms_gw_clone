package com.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "incoming", schema = "public", catalog = "sms_gateway")
public class IncomingEntity {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "uuid", length = 36)
	private String uuid;
	
	@Column(name = "created_date", nullable = false)
	private long createdDate;

	@Column(name = "updated_date", nullable = false)
	private long updatedDate;

	@Column(name = "description", length = 1000000)
	private String description;

	@Column(name = "source_address", nullable = false)
	private String sourceAddress;

	@Column(name = "destination_address", nullable = false)
	private String destinationAddress;

	@Column(name = "short_message", length = 1000000, nullable = false)
	private String shortMessage;

	@Column(name = "message_id")
	private String messageId;

	@Column(name = "sequence_number", nullable = false)
	private int sequenceNumber;

	@Column(name = "received_deliver_sm_date", nullable = false)
	private long receivedDeliverSmDate;

	@Column(name = "received_deliver_sm_resp_date", nullable = false)
	private long receivedDeliverSmRespDate;

	@Column(name = "in_queue_date", nullable = false)
	private long inQueueDate;

	@Column(name = "out_queue_date", nullable = false)
	private long outQueueDate;

	@Column(name = "status", nullable = false, length = 10)
	private int status;

	@Column(name = "is_deleted", nullable = false)
	private boolean deleted;
}
