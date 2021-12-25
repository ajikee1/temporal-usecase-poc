package com.isaveTemporal.transactionUseCase;

import com.isaveTemporal.useCase.UseCaseTwo;
import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;


public class EmailWorker {

    private static String TASK_QUEUE = "EMAIL_TASK_QUEUE";

    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(EmailWorkFlowImp.class);
        worker.registerActivitiesImplementations(new EmailActivityImp());
        factory.start();
    }



}
