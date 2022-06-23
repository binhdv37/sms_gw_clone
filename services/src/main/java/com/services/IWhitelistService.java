package com.services;

import java.util.ArrayList;

public interface IWhitelistService {

    ArrayList<String> findAllWhitelist();

    int existInWhitelist(String destinationAddress, ArrayList<String> whitelist);

    boolean whitelistActive();

}
