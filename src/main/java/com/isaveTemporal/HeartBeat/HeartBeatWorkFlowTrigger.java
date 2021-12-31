package com.isaveTemporal.HeartBeat;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

/*
Use case:
    Demonstrates how heartbeats can be used to notify temporal server of activity progress.

    Demonstrates how heartbeats 'last heartbeat details' can be used to handle activity failures on re-try.
    1. In activity 'heartBeatActivity' method, the code loops with 'i' 0 to 10, with the value of 'i' send back to temporal server as the heartbeat.
    2. When i = 3, the code is forced to throw an exception, demonstrating an activity failure.
    - This sets the 'last heartbeat detail' to '3' (the latest heartbeat value when the failure happened)
    - This 'last heartbeat detail' is available through Activity.getExecutionContext().getHeartbeatDetails()
    3. Temporal will retry the activity by default
    - On retry, the value of 'Activity.getExecutionContext().getHeartbeatDetails()' (3 in this case) is used to set the loop starting point to '4'
      and continue with 'i' 4 to 10.
    - Thereby, using heartbeat to skip the failure point on activity re-try.

 */

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
