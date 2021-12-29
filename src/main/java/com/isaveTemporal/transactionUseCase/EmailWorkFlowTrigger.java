package com.isaveTemporal.transactionUseCase;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

/*
    Demo the ability of temporal to resume execution without restart on recovery
    For input 'n', the workflow will send 'n' emails to the email address 'ajith@yopmail.com' every 30 seconds.

    1. Run 'EmailWorker'
    2. Run 'EmailWorkFlowTrigger': Workflow in 'Running' status
    3. Stop the 'EmailWorker' after 30 seconds
    - Check 'ajith@yopmail.com': It receives 1 email
    - Check 'ajith@yopmail.com' again in 2 minutes: It receives no additional emails
    4. Resume the 'EmailWorker' after 2 minutes and wait for the workflow to be in 'Completed' status
    - Check 'ajith@yopmail.com': It received 'n' number of emails. (temporal retains the state of the workflow i.e. '1'
      email was sent before the worker failed and on worker re-start continues fom that point with no additional emails sent
*/

public class EmailWorkFlowTrigger {

    private static String TASK_QUEUE = "EMAIL_TASK_QUEUE";


    public static void main(String[] args) {

        /* Point to remote host */
        WorkflowServiceStubsOptions wfOptions = WorkflowServiceStubsOptions.newBuilder().setTarget("XX.XX.XX.XX:7233").build();

        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance(wfOptions);
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build();
        EmailWorkFlow workflow = client.newWorkflowStub(EmailWorkFlow.class, options);

        String userName = "ajithkeerikkattil@gmail.com";
        String password = "Pulimood3";
        int num = 5;

        WorkflowExecution we = WorkflowClient.start(workflow::initiateWorkFlow, userName, password, num);
    }
}
