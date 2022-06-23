package com.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "message_log", schema = "public", catalog = "sms_gateway")
public class MessageLogEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_date", nullable = false, unique = true)
    private long createdDate;

    @Column(name = "updated_date", nullable = false)
    private long updatedDate;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "modified_by", nullable = false)
    private String modifiedBy;

    @Column(name = "provider_id", nullable = false)
    private long providerId;

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "source_address", nullable = false)
    private String sourceAddress;

    @Column(name = "destination_address", nullable = false)
    private String destinationAddress;

    @Column(name = "short_message", length = 1000000)
    private String shortMessage;

    @Column(name = "violation_keyword")
    private String violationKeyword;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "received_submit_sm_date")
    private long receivedSubmitSmDate;

    @Column(name = "received_submit_sm_resp_date")
    private long receivedSubmitSmRespDate;

    @Column(name = "received_deliver_sm_date")
    private long receivedDeliverSmDate;

    @Column(name = "received_deliver_sm_resp_date")
    private long receivedDeliverSmRespDate;

    @Column(name = "in_queue_date")
    private long inQueueDate;

    @Column(name = "out_queue_date")
    private long outQueueDate;

    @Column(name = "action_data", length = 1000000)
    private String actionData;

    @Column(name = "action_status", nullable = false)
    private String actionStatus;

    @Column(name = "action_failure_details", length = 1000000)
    private String actionFailureDetails;

    @Column(name = "status", nullable = false)
    private int status;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

}
