package com.models;

import com.cloudhopper.smpp.pdu.SubmitSm;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"rawRequest"})
public class MultipartMessage {

    private int sequenceNumber = -1;
    private int totalMessages;
    private int currentMessageNumber;
    private String shortMessage;
    private int esmClass;
    private int dcs;
    private String sourceAddress;
    private String destinationAddress;
    SubmitSm rawRequest;

}
