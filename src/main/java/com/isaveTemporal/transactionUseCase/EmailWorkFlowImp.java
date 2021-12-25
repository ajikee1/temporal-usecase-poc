package com.isaveTemporal.transactionUseCase;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class EmailWorkFlowImp implements EmailWorkFlow{

    private final  EmailActivity activity = Workflow.newActivityStub(EmailActivity.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());

    @Override
    public void initiateWorkFlow() {

        String userName = "ajithkeerikkattil@gmail.com";
        String password = "Pulimood3";
        int num = 1;

        Promise<Boolean> emailPromise = Async.function(activity::sendEmail,userName,password, num);
        Boolean emailStatus = emailPromise.get();
        System.out.println(emailStatus);

    }
}