package com.isaveTemporal.transactionUseCase;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface EmailWorkFlow {

    @WorkflowMethod
    public void initiateWorkFlow();

}