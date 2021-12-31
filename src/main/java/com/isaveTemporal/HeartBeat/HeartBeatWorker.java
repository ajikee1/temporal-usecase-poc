package com.isaveTemporal.HeartBeat;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class HeartBeatWorker {

    private static String TASK_QUEUE = "TASK_QUEUE";

    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(HeartBeatWorkFlowImp.class);
        worker.registerActivitiesImplementations(new HeartBeatActivityImp());
        factory.start();
    }



}
