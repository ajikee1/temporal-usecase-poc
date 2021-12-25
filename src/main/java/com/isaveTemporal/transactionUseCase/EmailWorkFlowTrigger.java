package com.isaveTemporal.transactionUseCase;

import com.isaveTemporal.useCase.UseCaseTwo;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class EmailWorkFlowTrigger {

    private static String TASK_QUEUE = "EMAIL_TASK_QUEUE";


    public static void main(String[] args) {

        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);

        WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build();
        EmailWorkFlow workflow = client.newWorkflowStub(EmailWorkFlow.class, options);

        WorkflowExecution we = WorkflowClient.start(workflow::initiateWorkFlow);
    }
}
