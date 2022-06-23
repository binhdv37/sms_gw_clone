package com.common;

public class ConstanceQuery {

    public static final String ROAMING_FIND_ALL_QUERY = "select distinct on (r.phone_number) r.phone_number as phoneNumber, " +
            "r.to_provider_id as toProviderId " +
            "from roaming r " +
            "where r.is_deleted = false " +
            "group by r.phone_number, r.to_provider_id, r.created_date " +
            "order by r.phone_number, r.created_date desc";

    public static final String BLACKLIST_FIND_ALL_QUERY = "select distinct on (bl.phone_number) bl.phone_number as phoneNumber " +
            "from blacklist bl " +
            "where bl.is_deleted = false " +
            "group by bl.phone_number, bl.created_date " +
            "order by bl.phone_number, bl.created_date desc";

    public static final String WHITELIST_FIND_ALL_QUERY = "select distinct on (wl.phone_number) wl.phone_number as phoneNumber " +
            "from whitelist wl " +
            "where wl.is_deleted = false " +
            "group by wl.phone_number, wl.created_date " +
            "order by wl.phone_number, wl.created_date desc";

    public static final String WHITELIST_ACTIVE = "select mm.active " +
            "from manage_module mm " +
            "where mm.module_name = 'WHITELIST' " +
            "and mm.is_deleted = false";

    public static final String QUOTA_ACTIVE = "select mm.active " +
            "from manage_module mm " +
            "where mm.module_name = 'QUOTA' " +
            "and mm.is_deleted = false";

    public static final String QUOTA_FIND_ALL_QUERY = "select q.start_valid_date, " +
            "q.end_valid_date, " +
            "q.total_quota, " +
            "q.warning_threshold, " +
            "q.quota_code " +
            "from quota q " +
            "where q.is_deleted = false";

    public static final String PREFIX_PHONE_NUMBER_FIND_ALL_QUERY = "select p.id as id, p.prefix_phone_number as prefixPhoneNumber " +
            "from provider p " +
            "where p.is_deleted = false and p.status = 1";

    public static final String MSG_TEMPLATE_FIND_ALL_QUERY = "select bn.alias, mt.template_message from message_template mt " +
            "left join messagetemplate_brandname mb on mt.id = mb.message_template_id " +
            "left join brand_name bn on bn.id = mb.brand_name_id " +
            "where mt.is_deleted = false and mt.status = 1";

/*    public static final String MSG_TEMPLATE_FIND_ALL_QUERY = "select mt.template_message " +
            "from message_template mt " +
            "where is_deleted = false " +
            "and status = 1";*/
}
