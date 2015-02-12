package com.standapp.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * Created by John on 2/4/2015.
 */
public class UserInfo {

    private Context context;
    private User user;
    // TODO add timestamp so we refresh only X minutes?

    public UserInfo(Context context) {
        this.context = context;
    }

    //FIXME handle multiple accounts
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
        return "john.sintal@gmail.com";
//        return gmail;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
