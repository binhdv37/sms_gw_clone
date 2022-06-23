package com.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "outgoing", schema = "public", catalog = "sms_gateway")
public class OutgoingEntity {

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

	@Column(name = "source_address_npi")
	private int sourceAddressNpi;

	@Column(name = "source_address_ton")
	private int sourceAddressTon;

	@Column(name = "destination_address", nullable = false)
	private String destinationAddress;

	@Column(name = "destination_address_npi")
	private int destinationAddressNpi;

	@Column(name = "destination_address_ton")
	private int destinationAddressTon;

	@Column(name = "provider_id", nullable = false)
	private Long providerId;

	@Column(name = "message_id")
	private String messageId;

	@Column(name = "action_status")
	private String actionStatus;

	@Column(name = "action_failure_details", length = 1000000)
	private String actionFailureDetails;

	@Column(name = "short_message", length = 1000000, nullable = false)
	private String shortMessage;

	@Column(name = "data_coding")
	private int dataCoding;

	@Column(name = "encode_type_name")
	private String encodeTypeName;

	@Column(name = "violation_keyword")
	private String violationKeyword;

	@Column(name = "sequence_number", nullable = false)
	private int sequenceNumber;

	@Column(name = "received_submit_sm_date")
	private long receivedSubmitSmDate;

	@Column(name = "received_submit_sm_resp_date")
	private long receivedSubmitSmRespDate;

	@Column(name = "in_queue_date", nullable = false)
	private long inQueueDate;

	@Column(name = "out_queue_date", nullable = false)
	private long outQueueDate;

	@Column(name = "sent_to_mbf", nullable = false)
	private boolean sentToMbf;

	@Column(name = "status", nullable = false, length = 10)
	private int status;

	@Column(name = "is_deleted", nullable = false)
	private boolean deleted;

	@Column(name = "esm_class")
	private int esmClass;

	@Column(name = "udh")
	private boolean udh;

	@Column(name = "multipart_id")
	private int multipartId;

	@Column(name = "total_messages")
	private int totalMessages;

	@Column(name = "current_message_number")
	private int currentMessageNumber;

	@Column(name = "register_delivery")
	private int registerDelivery;

	@Column(name = "raw_request", length = 1000000)
	private String rawRequest;
	
	@Transient
	private int retriedTimes;
}
