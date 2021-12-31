package com.isaveTemporal.HeartBeat;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;

import java.time.Duration;

public class HeartBeatWorkFlowImp implements HeartBeatWorkFlow {
    private final HeartBeatActivity activity = Workflow.newActivityStub(HeartBeatActivity.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(200)).build());

    @Override
    public void initiateWorkFlow() {
        Promise<Boolean> promise = Async.function(activity::heartBeatActivity);
        Boolean status = promise.get();
    }
}