package com.models;

import lombok.Data;

@Data
public class Quota {

    private String quotaCode;
    private long startValidDate;
    private long endValidDate;
    private long totalQuota;
    private long warningThreshold;

}
