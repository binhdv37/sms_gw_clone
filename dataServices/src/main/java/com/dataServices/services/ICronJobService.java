package com.dataServices.services;

public interface ICronJobService {

    void cronSmppUser();

    void cronRoaming();

    void cronBlacklist();

    void cronWhitelist();

    void cronProvider();

    void cronMsgTemplate();

    void cronWhitelistActive();

    void cronQuotaActive();

    void cronQuotaAndTotalMsgSent();
}
