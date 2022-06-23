package com.services.impl;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import com.models.EncodeType;
import com.services.EncodeTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.common.ConstanceEncode.*;


@Service
@Slf4j
public class EncodeTypeServiceImpl implements EncodeTypeService {

    @Override
    public EncodeType determineEncodeType(byte dataCoding, byte[] shortMessages) {
        EncodeType encodeType = new EncodeType();
        InputStream is;
        BufferedInputStream bis;
        CharsetDetector charsetDetector;
        CharsetMatch charsetMatch;
        String typeOfEncode;
        try {
            is = new ByteArrayInputStream(shortMessages);
            bis = new BufferedInputStream(is);
            charsetDetector = new CharsetDetector();
            charsetDetector.setText(bis);
            charsetMatch = charsetDetector.detect();
            typeOfEncode = charsetMatch.getName();
            switch (dataCoding) {
                case DATA_CODING_GSM:
                    if (typeOfEncode.equals(NAME_GSM) || typeOfEncode.equals(NAME_ISO_8859_1) || typeOfEncode.equals(NAME_UTF8)) {
                        encodeType.setCorrect(true);
                        encodeType.setType(CharsetUtil.CHARSET_GSM);
                        encodeType.setName(NAME_GSM);
                        log.info("The short messages are encrypted with GSM");
                    }
                    break;
                case DATA_CODING_UCS2:
                    if (typeOfEncode.equals(NAME_UCS2) || typeOfEncode.equals(NAME_UTF16_BE)) {
                        encodeType.setCorrect(true);
                        encodeType.setType(CharsetUtil.CHARSET_UCS_2);
                        encodeType.setName(NAME_UCS2);
                        log.info("The short messages are encrypted with UCS2");
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Error at determineEncodeType function with message: {}", e.getMessage());
        }
        return encodeType;
    }
}
