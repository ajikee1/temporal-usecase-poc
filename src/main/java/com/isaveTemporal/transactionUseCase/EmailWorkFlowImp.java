package com.isaveTemporal.transactionUseCase;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EmailWorkFlowImp implements EmailWorkFlow {

    private final EmailActivity activity = Workflow.newActivityStub(EmailActivity.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());

    @Override
    public void initiateWorkFlow(String userName, String password, int num) {

        List<Promise<Boolean>> promiseList = new ArrayList<>();

        for (int i = 0; i < num; i++) {
            Workflow.sleep(Duration.ofSeconds(30));
            promiseList.add(Async.function(activity::sendEmail, userName, password, i));
        }

        Promise.allOf(promiseList).get();

        /*
            Promise<Boolean> emailPromise = Async.function(activity::sendEmail,userName,password, num);
            Boolean emailStatus = emailPromise.get();
            System.out.println(emailStatus);
        */

    }
}