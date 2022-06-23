package com.producers.smppServer;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.producers.services.IMessageHandlingProducersService;
import com.producers.services.impl.MessageHandlingProducersServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.common.ConstanceMessage.*;

@Service
@Slf4j
public class DftSmppServerHandler implements SmppServerHandler {

	private final IMessageHandlingProducersService messageHandlingService;

	@Autowired
	public DftSmppServerHandler(IMessageHandlingProducersService messageHandlingService) {
		this.messageHandlingService = messageHandlingService;
	}

	@Override
	public void sessionBindRequested(Long sessionId, SmppSessionConfiguration sessionConfiguration,
			BaseBind bindRequest) throws SmppProcessingException {
		String systemId;
		String textPlainPassword;
		String md5Password;
		try {
			systemId = bindRequest.getSystemId();
			textPlainPassword = bindRequest.getPassword();
			log.info("System id: {} and password: {}", systemId, textPlainPassword);
			log.info("Hash map SMPP user: {}", MessageHandlingProducersServiceImpl.hashMapSmppUser);
			if (MessageHandlingProducersServiceImpl.hashMapSmppUser.containsKey(systemId)) {
				log.debug("SystemId exist in database");
				md5Password = DigestUtils.md5Hex(textPlainPassword);
                if (md5Password.equals(MessageHandlingProducersServiceImpl.hashMapSmppUser.get(systemId))) {
					log.debug("Password is correct");
					sessionConfiguration.setName("Application.SMPP." + sessionConfiguration.getSystemId());
					log.info("SystemId and password is correct");
                } else {
                    log.warn("Password is incorrect");
                    throw new SmppProcessingException(SmppConstants.STATUS_BINDFAIL, "SystemId or password is incorrect");
                }
			} else {
				log.warn("SystemId is not exist in database");
				throw new SmppProcessingException(SmppConstants.STATUS_BINDFAIL, "SystemId or password is incorrect");
			}
		} catch (SmppProcessingException spe) {
			throw new SmppProcessingException(spe.getErrorCode(), spe.getMessage());
		} catch (Exception ex) {
			log.error("Error at sessionBindRequested function in DftSmppServerHandler with message: {}", ex.getMessage());
		}
	}

	@Override
	public void sessionCreated(Long sessionId, SmppServerSession session, BaseBindResp preparedBindResponse) {
		try {
			log.info("Session created: {}", session);
			DftSmppSessionHandler sessionHandler = new DftSmppSessionHandler(messageHandlingService);
			sessionHandler.setSessionRef(session);
			DftSmppSessionHandler.smppSession = session;
			session.serverReady(sessionHandler);
		} catch (Exception ex) {
			log.error("Error at sessionCreated function in DftSmppServerHandler with message: {}", ex.getMessage());
		}
	}

	@Override
	public void sessionDestroyed(Long sessionId, SmppServerSession session) {
		try {
			log.info("Session destroyed: {}", session);
			if (session.hasCounters()) {
				log.info("Final session rx-submitSM: {}", session.getCounters().getRxSubmitSM());
			}
			session.destroy();
		} catch (Exception ex) {
			log.error(SMSC_ERR_003 + ex.getMessage());
		}
	}
}
