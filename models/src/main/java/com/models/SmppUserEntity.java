package com.models;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "smpp_user", schema = "public", catalog = "sms_gateway")
public class SmppUserEntity {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "system_id", nullable = false, length = 30)
    private String systemId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "submit_sm_vinaphone_source_addr", length = 30)
    private String submitSmVnpSourceAddress;

    @Column(name = "submit_sm_mobifone_source_addr", length = 30)
    private String submitSmMbfSourceAddress;

    @Column(name = "submit_sm_vietnam_mobile_source_addr", length = 30)
    private String submitSmVnmSourceAddress;

    @Column(name = "deliver_sm_vinaphone_source_addr", length = 30)
    private String deliverSmVnpSourceAddress;

    @Column(name = "deliver_sm_mobifone_source_addr", length = 30)
    private String deliverSmMbfSourceAddress;

    @Column(name = "deliver_sm_vietnam_mobile_source_addr", length = 30)
    private String deliverSmVnmSourceAddress;

    @Column(name = "status", nullable = false, length = 10)
    private int status;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

}
