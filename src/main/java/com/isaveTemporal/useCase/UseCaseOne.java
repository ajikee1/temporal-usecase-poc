package com.isaveTemporal.useCase;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

/*
    Demo of conditional child workflow execution
    Based on an input passed into the workflow by a signal method, run a different child workflow
        1: Initiate child One Workflow
        2: Initiate child Two Workflow
*/

public class UseCaseOne {

    private static String TASK_QUEUE = "TASK_QUEUE";

    // Parent workflow
    @WorkflowInterface
    public interface ParentWorkFlow {

        @WorkflowMethod
        Boolean initiateWorkFlow();

        @SignalMethod
        void signalChildWorkFlow(int childWorkFlowNum);

    }


    static boolean triggerChildOneWorkflow = false;
    static boolean triggerChildTwoWorkflow = false;

    public static class ParentWorkFlowImp implements ParentWorkFlow {

        @Override
        public Boolean initiateWorkFlow() {
            System.out.println("*** INSIDE PARENT WORKFLOW ***");

            // Block until the boolean that triggers either one of the workflow is set to TRUE by the signal
            Workflow.await(() -> triggerChildOneWorkflow || triggerChildTwoWorkflow);

            if (triggerChildOneWorkflow) {
                ChildOneWorkFlow childOneWorkFlow = Workflow.newChildWorkflowStub(ChildOneWorkFlow.class);
                childOneWorkFlow.initiateChildOneWorkFlow();

            } else if (triggerChildTwoWorkflow) {
                ChildTwoWorkFlow childTwoWorkFlow = Workflow.newChildWorkflowStub(ChildTwoWorkFlow.class);
                childTwoWorkFlow.initiateChildTwoWorkFlow();
            }

            return true;
        }


        @Override
        public void signalChildWorkFlow(int childWorkFlowNum) {
            if (childWorkFlowNum == 1) {
                triggerChildOneWorkflow = true;
            } else if (childWorkFlowNum == 2) {
                triggerChildTwoWorkflow = true;
            }
        }
    }


    // Child One workflow
    @WorkflowInterface
    public interface ChildOneWorkFlow {

        @WorkflowMethod
        Boolean initiateChildOneWorkFlow();
    }


    public static class ChildOneWorkFlowImp implements ChildOneWorkFlow {

        @Override
        public Boolean initiateChildOneWorkFlow() {
            System.out.println("*** INSIDE CHILD ONE WORKFLOW ***");
            return true;
        }
    }


    // Child Two workflow
    @WorkflowInterface
    public interface ChildTwoWorkFlow {

        @WorkflowMethod
        Boolean initiateChildTwoWorkFlow();
    }

    public static class ChildTwoWorkFlowImp implements ChildTwoWorkFlow {

        @Override
        public Boolean initiateChildTwoWorkFlow() {
            System.out.println("*** INSIDE CHILD TWO WORKFLOW ***");
            return true;
        }
    }


    public static void main(String[] args) {

        /* Worker code */
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);

        /* Register parent and all children with the worker */
        worker.registerWorkflowImplementationTypes(ParentWorkFlowImp.class, ChildOneWorkFlowImp.class, ChildTwoWorkFlowImp.class);
        factory.start();

        /* Workflow initiating code */
        WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build();
        ParentWorkFlow workflow = client.newWorkflowStub(ParentWorkFlow.class, options);

        /* Asynchronously initiate the parent workflow */
        WorkflowExecution we = WorkflowClient.start(workflow::initiateWorkFlow);

        workflow.signalChildWorkFlow(1);
    }

}
