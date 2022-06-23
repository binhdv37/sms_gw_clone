package com.models;

import com.cloudhopper.commons.charset.Charset;
import lombok.Data;

@Data
public class EncodeType {
    boolean isCorrect = false;
    Charset type = null;
    String name = "";
}
