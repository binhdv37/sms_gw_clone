package com.services.impl;

import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.google.gson.Gson;
import com.models.IncomingEntity;
import com.models.OutgoingEntity;
import com.models.to.DataCommandTo;
import com.services.IConvertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConvertServiceImpl implements IConvertService {

	@Override
	public String submitSmToJson(SubmitSm submitSm) {
		Gson gson = new Gson();
		String strJson = "";
		try {
			strJson = gson.toJson(submitSm);
		} catch (Exception e) {
			log.warn("Error while convert object submitSm to string json with message: " + e.getMessage());
		}
		return strJson;
	}

	@Override
	public String submitSmRespToJson(SubmitSmResp submitSmResp) {
		String strJson = "";
		try {
			strJson = new Gson().toJson(submitSmResp);
			log.debug("Convert from SubmitSmResp to json successful");
		} catch (Exception e) {
			log.warn("Error while convert object submitSmResp to string json with message: " + e.getMessage());
		}
		return strJson;
	}

	@Override
	public SubmitSm jsonToSubmitSm(String jsonSubmitSm) {
		Gson gson = new Gson();
		SubmitSm submitSm = null;
		try {
			submitSm = gson.fromJson(jsonSubmitSm, SubmitSm.class);
		} catch (Exception e) {
			log.warn("Error while convert string json to object submitSm with message: " + e.getMessage());
		}
		return submitSm;
	}

	@Override
	public SubmitSmResp jsonToSubmitSmResp(String jsonSubmitSmResp) {
		Gson gson = new Gson();
		SubmitSmResp submitSmResp = null;
		try {
			submitSmResp = gson.fromJson(jsonSubmitSmResp, SubmitSmResp.class);
			log.debug("Convert from json to SubmitSmResp successful");
		} catch (Exception e) {
			log.warn("Error while convert string json to object submitSmResp with message: " + e.getMessage());
		}
		return submitSmResp;
	}

	@Override
	public String outgoingToJson(OutgoingEntity outgoingEntity) {
		Gson gson = new Gson();
		String strJson = "";
		try {
			strJson = gson.toJson(outgoingEntity);
			log.debug("Convert from OutgoingEntity to json successful");
		} catch (Exception e) {
			log.warn("Error while convert object OutgoingEntity to string json with message: " + e.getMessage());
		}
		return strJson;
	}

	@Override
	public String incomingToJson(IncomingEntity incomingEntity) {
		String strJson = "";
		try {
			strJson = new Gson().toJson(incomingEntity);
			log.debug("Convert from IncomingEntity to json successful");
		} catch (Exception e) {
			log.warn("Error while convert object IncomingEntity to string json with message: " + e.getMessage());
		}
		return strJson;
	}

	@Override
	public OutgoingEntity castBytesArrayToOutgoing(String strBody) {
		OutgoingEntity currentOutgoing = null;
		try {
			currentOutgoing = new Gson().fromJson(strBody, OutgoingEntity.class);
			log.debug("Convert from bytes array to OutgoingEntity successful");
		} catch (Exception e) {
			log.warn("Error at castBytesArrayToOutgoing function with message: {}", e.getMessage());
		}
		return currentOutgoing;
	}

	@Override
	public IncomingEntity castBytesArrayToIncoming(String strBody) {
		IncomingEntity currentIncoming = null;
		try {
			currentIncoming = new Gson().fromJson(strBody, IncomingEntity.class);
			log.debug("Convert from bytes array to IncomingEntity successful");
		} catch (Exception e) {
			log.warn("Error at castBytesArrayToIncoming function with message: {}", e.getMessage());
		}
		return currentIncoming;
	}

	@Override
	public String deliverSmToJson(DeliverSm deliverSm) {
		Gson gson = new Gson();
		String strJson = "";
		try {
			strJson = gson.toJson(deliverSm);
		} catch (Exception e) {
			log.warn("Error while convert object deliverSm to string json with message: " + e.getMessage());
		}
		return strJson;
	}

	@Override
	public DeliverSm jsonToDeliverSm(String jsonDeliverSm) {
		Gson gson = new Gson();
		DeliverSm deliverSm = null;
		try {
			deliverSm = gson.fromJson(jsonDeliverSm, DeliverSm.class);
		} catch (Exception e) {
			log.warn("Error while convert string json to object deliverSm with message: " + e.getMessage());
		}
		return deliverSm;
	}

	@Override
	public String deliverSmRespToJson(DeliverSmResp deliverSmResp) {
		Gson gson = new Gson();
		String strJson = "";
		try {
			strJson = gson.toJson(deliverSmResp);
		} catch (Exception e) {
			log.warn("Error while convert object deliverSmResp to string json with message: " + e.getMessage());
		}
		return strJson;
	}

	@Override
	public DeliverSmResp jsonToDeliverSmResp(String jsonDeliverSmpResp) {
		DeliverSmResp deliverSmResp = null;
		try {
			deliverSmResp = new Gson().fromJson(jsonDeliverSmpResp, DeliverSmResp.class);
			log.debug("Convert from jsonDeliverSmpResp to DeliverSmResp successful");
		} catch (Exception e) {
			log.warn("Error while convert string json to object deliverSmResp with message: " + e.getMessage());
		}
		return deliverSmResp;
	}

	@Override
	public DataCommandTo castBytesArrayToDataCommandTo(String strBody) {
		DataCommandTo result = null;
		try {
			result = new Gson().fromJson(strBody, DataCommandTo.class);
			log.debug("Convert from castBytesArrayToDataCommandTo successful");
		} catch (Exception e) {
			log.warn("Error at castBytesArrayToDataCommandTo function with message: {}", e.getMessage());
		}
		return result;
	}

	@Override
	public String objectToJson(Object data) {
		String strJson = "";
		try {
			strJson = new Gson().toJson(data);
		} catch (Exception e) {
			log.warn("Error while convert object data to string json with message: " + e.getMessage());
		}
		return strJson;
	}
}
