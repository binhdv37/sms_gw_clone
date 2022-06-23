package com.producers.services.impl;

import com.cloudhopper.smpp.pdu.PduRequest;
import com.producers.services.IMessageHandlingProducersService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiProcessSubmitSm implements Runnable {

    private final IMessageHandlingProducersService messageHandlingService;
    private final PduRequest pduRequest;
    private final long receiveSubmitSmDate;
    private final boolean udh;
    private final boolean matchedRegex;

    public MultiProcessSubmitSm(IMessageHandlingProducersService messageHandlingService, PduRequest pduRequest, long receiveSubmitSmDate, boolean udh, boolean matchedRegex) {
        this.messageHandlingService = messageHandlingService;
        this.pduRequest = pduRequest;
        this.receiveSubmitSmDate = receiveSubmitSmDate;
        this.udh = udh;
        this.matchedRegex = matchedRegex;
    }

    @Override
    public void run() {
        try {
            log.debug("Thread receive submit_sm {}", Thread.currentThread().getName());
            this.messageHandlingService.sendSubmitSm(this.pduRequest, this.receiveSubmitSmDate, this.udh, this.matchedRegex);
        } catch (Exception e) {
            log.error("Error at run function in MultiProcessSubmitSm function with message: {}", e.getMessage());
        }
    }
}
