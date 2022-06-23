package com.dataServices.services;

public interface IMessageHandlingDataService {

    void handleDataCommand(String in,  long outQueueDate);
    
}
