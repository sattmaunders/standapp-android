package com.standapp.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Created by John on 2/4/2015.
 */
public class UserInfo {


    private Context context;

    public UserInfo(Context context) {
        this.context = context;
    }

    public String getUserEmail(){
        AccountManager manager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] list = manager.getAccounts();
        String gmail = null;

        for(Account account: list)
        {
            if(account.type.equalsIgnoreCase("com.google"))
            {
                gmail = account.name;
                break;
            }
        }

        return gmail;
    }
}