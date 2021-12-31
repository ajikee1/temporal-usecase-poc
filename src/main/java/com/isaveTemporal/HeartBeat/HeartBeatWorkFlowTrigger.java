package com.isaveTemporal.HeartBeat;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class HeartBeatWorkFlowTrigger {

    private static String TASK_QUEUE = "TASK_QUEUE";

    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build();
        HeartBeatWorkFlow workflow = client.newWorkflowStub(HeartBeatWorkFlow.class, options);

        WorkflowExecution we = WorkflowClient.start(workflow::initiateWorkFlow);
    }
}
