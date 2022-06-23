package com.producers.services;

public interface IMessageHandlingMasterDataChanged {
	String handleMessage(String in, long outQueueTime);
}
