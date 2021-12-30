package com.isaveTemporal.useCase;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.common.v1.WorkflowType;
import io.temporal.api.enums.v1.WorkflowExecutionStatus;
import io.temporal.api.enums.v1.WorkflowIdReusePolicy;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.client.WorkflowStub;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/*
    A Workflow is uniquely identified by its Namespace, Workflow Id, and Run Id.

    Workflow ID: Custom Workflow Id to a Workflow. Example: Customer ID, Order ID etc.
        - WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).setWorkflowId({WORKFLOW_ID}).build();

    Cannot re-use workflow ID (except based on reuse policy):
        - Set re-use policy: WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).setWorkflowId(randomString).setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY).build();

    1. Allow duplicate failed only policy: Allow a workflow with duplicate ID to run if the prev workflow with that ID failed.
        setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY)
    2. Allow duplicate policy: Default
        setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE)
    3. Reject duplicate policy:
        setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_REJECT_DUPLICATE)

 */

public class SearchWorkFlow {

    private static String TASK_QUEUE = "TASK_QUEUE";

    @WorkflowInterface
    public interface searchWorkflow {

        @WorkflowMethod
        public Boolean initiateWorkFlow();
    }

    public static class searchWorkflowImp implements searchWorkflow {

        private final testActivity activities = Workflow.newActivityStub(testActivity.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());

        @Override
        public Boolean initiateWorkFlow() {
            /* Use side-effect
                *  Workflow code that includes arbitrary side effects (for example getting a random number or generating a random UUID, etc), can cause unpredictable results during replay.
                *  Being able to add some non-deterministic code inside your Workflow is in some cases important, and you can do that using Workflow.sideEffect.

                String randomString = Workflow.sideEffect(String.class, () -> generateRandom());
            */

            Promise<Boolean> p = Async.function(activities::activityOne);
            Boolean response = p.get();

            return true;
        }
    }

    @ActivityInterface
    public interface testActivity {

        public Boolean activityOne();


    }

    public static class testActivityImp implements testActivity {

        @Override
        public Boolean activityOne() {
            try {
                Thread.sleep(5000);
                System.out.println("Activity  running for 5000ms");
            } catch (Exception e) {
                throw Activity.wrap(e);
            }

            return true;
        }

    }

    public static void main(String[] args) {

        /* returns a string to be used as the WORKFLOW ID */
        String randomString = generateRandom();

        /* Worker code */
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(searchWorkflowImp.class);
        worker.registerActivitiesImplementations(new testActivityImp());
        factory.start();

        /* Set Workflow ID to the randomString */
        WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).setWorkflowId(randomString).setWorkflowIdReusePolicy(WorkflowIdReusePolicy.WORKFLOW_ID_REUSE_POLICY_ALLOW_DUPLICATE_FAILED_ONLY).build();
        searchWorkflow workflow = client.newWorkflowStub(searchWorkflow.class, options);

        /*
            Trigger the workflow async but wait for it to complete
            In this case, since the workflow is async, the below code will make the workflow to complete
         */
        WorkflowExecution we = WorkflowClient.start(workflow::initiateWorkFlow);
        WorkflowStub untyped = WorkflowStub.fromTyped(workflow);
        Boolean workFLowStatus = untyped.getResult(Boolean.class);
        System.out.println("    WORKFLOW RESPONSE: " + workFLowStatus);

        /* Get the Workflow ID and Run ID */
        String workFlowId = we.getWorkflowId();
        String runId = we.getRunId();
        System.out.println("    WORKFLOW ID: " + workFlowId + " RUN ID: " + runId);

        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }

        /* Search for workFlows by the workFlowId and get meta-data */
        String query = "WorkflowId='" + workFlowId + "'";
        ListWorkflowExecutionsRequest wfeRequest = ListWorkflowExecutionsRequest.newBuilder().setQuery(query).setNamespace("default").build();
        ListWorkflowExecutionsResponse wfeResponse = service.blockingStub().listWorkflowExecutions(wfeRequest);
        List<WorkflowExecutionInfo> workFlowExecutionList = wfeResponse.getExecutionsList();

        for (WorkflowExecutionInfo workFlowExecution : workFlowExecutionList) {
            String workFlowType = workFlowExecution.getType().getName();
            String workFlowStatus = workFlowExecution.getStatus().toString();
            System.out.println("    WORKFLOW ID: " + workFlowId + " WORKFLOW STATUS: " + workFlowStatus);
        }

    }


    /* Generic function that returns a random string */
    public static String generateRandom() {

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        int length = 7;

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
