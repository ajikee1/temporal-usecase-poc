package com.isaveTemporal.HeartBeat;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HeartBeatWorkFlow {

    @WorkflowMethod
    public void initiateWorkFlow();

}