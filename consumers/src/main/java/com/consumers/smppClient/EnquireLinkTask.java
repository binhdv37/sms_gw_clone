package com.consumers.smppClient;

import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnquireLinkTask implements Runnable {

	private OutboundClient client;
	private Integer enquireLinkTimeout;

	public EnquireLinkTask(OutboundClient client, Integer enquireLinkTimeout) {
		this.client = client;
		this.enquireLinkTimeout = enquireLinkTimeout;
	}

	@Override
	public void run() {
		SmppSession smppSession = client.getSession();
		if (smppSession != null && smppSession.isBound()) {
			try {
				log.debug("Sending enquire_link");
				EnquireLinkResp enquireLinkResp = smppSession.enquireLink(new EnquireLink(), enquireLinkTimeout);
				log.debug("Enquire_link_resp: {}", enquireLinkResp);
			} catch (SmppTimeoutException | SmppChannelException e) {
				log.warn("Enquire link failed, executing reconnect; " + e);
				client.scheduleReconnect();
			} catch (InterruptedException e) {
				log.warn("Enquire link interrupted, probably killed by reconnecting");
			} catch (Exception e) {
				log.error("Enquire link failed, executing reconnect", e);
				client.scheduleReconnect();
			}
		} else {
			log.error("Enquire link running while session is not connected");
		}
	}
}
