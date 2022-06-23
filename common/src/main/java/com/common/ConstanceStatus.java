package com.common;

import java.util.HashMap;

public class ConstanceStatus {

    public static final HashMap<Integer, String> DESCRIPTION = new HashMap<Integer, String>() {{
        put(RECEIVE_FROM_FB_SUCCESS, "RECEIVE_SUBMIT_SM_FB");
        put(RECEIVE_FROM_WEB_SUCCESS, "RECEIVE_FROM_WEB_SUCCESS");
        put(DEST_ADDRESS_EXISTED_BLACKLIST, "BLACKLIST");
        put(DEST_ADDRESS_EXISTED_WHITELIST, "WHITELIST");
        put(DEST_ADDRESS_NOT_EXISTED_WHITELIST, "NOT_IN_WHITELIST");
        put(RECEIVE_SUBMIT_SM_RESP_SUCCESS, "RECEIVE_SUBMIT_SM_RESP_MBF_SUCCESS");
        put(RECEIVE_SUBMIT_SM_RESP_FAIL, "RECEIVE_SUBMIT_SM_RESP_MBF_FAIL");
        put(RECEIVE_DELIVER_SM_SUCCESS, "RECEIVE_DELIVER_SM_MBF_SUCCESS");
        put(RECEIVE_DELIVER_SM_RESP_SUCCESS, "RECEIVE_DELIVER_SM_RESP_FB_SUCCESS");
        put(RECEIVE_DELIVER_SM_RESP_FAIL, "RECEIVE_DELIVER_SM_RESP_FB_FAIL");
        put(SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE, "SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE");
        put(WHITELIST_EMPTY, "WHITELIST_EMPTY");
        put(QUOTA_IS_OVER, "QUOTA_IS_OVER");        
    }};

    public static final HashMap<Integer, String> AFD = new HashMap<Integer, String>() {{
        put(DEST_ADDRESS_EXISTED_BLACKLIST, "Blacklist");
        put(DEST_ADDRESS_NOT_EXISTED_WHITELIST, "Whitelist");
        put(SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE, "Template tin nhắn");
        put(QUOTA_IS_OVER, "Hết sản lượng quota");
    }};

    //    Success
    public static final int RECEIVE_FROM_FB_SUCCESS = 0;
    public static final int RECEIVE_FROM_WEB_SUCCESS = 11;
    public static final int RECEIVE_SUBMIT_SM_RESP_SUCCESS = 1;
    public static final int RECEIVE_DELIVER_SM_SUCCESS = 7;
    public static final int RECEIVE_DELIVER_SM_RESP_SUCCESS = 8;
    public static final String SUCCESS = "SUCCESS";

    //    Failed
    public static final int RECEIVE_SUBMIT_SM_RESP_FAIL = -1;
    public static final int RECEIVE_DELIVER_SM_RESP_FAIL = -8;
    public static final String FAIL = "FAIL";

    public static final int WHITELIST_EMPTY = 2;
    public static final int DEST_ADDRESS_EXISTED_WHITELIST = 3;
    public static final int DEST_ADDRESS_NOT_EXISTED_WHITELIST = -2;

    public static final int DEST_ADDRESS_NOT_EXISTED_BLACKLIST = 4;
    public static final int DEST_ADDRESS_EXISTED_BLACKLIST = -4;

    public static final int DEST_ADDRESS_NOT_EXISTED_PROVIDER = -5;

    public static final int DEST_ADDRESS_NOT_EXISTED_ROAMING = -6;

    public static final int SHORT_MSG_NOT_EXISTED_MSG_TEMPLATE = -9;

    public static final int QUOTA_IS_OVER = -10;

}
