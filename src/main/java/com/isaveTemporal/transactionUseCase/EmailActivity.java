package com.isaveTemporal.transactionUseCase;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface EmailActivity {

    public boolean sendEmail(String userName, String password, int number);
}

