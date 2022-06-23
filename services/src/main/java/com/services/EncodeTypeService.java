package com.services;

import com.models.EncodeType;

public interface EncodeTypeService {
    EncodeType determineEncodeType(byte dataCoding, byte[] shortMessages);
}
