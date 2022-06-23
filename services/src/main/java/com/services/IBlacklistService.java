package com.services;

import java.util.ArrayList;

public interface IBlacklistService {

    ArrayList<String> findAllBlacklist();

    int existInBlacklist(String destinationAddress, ArrayList<String> blacklist);

}
