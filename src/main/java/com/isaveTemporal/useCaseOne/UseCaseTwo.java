package com.isaveTemporal.useCaseOne;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/*
    Demo of parallel activities
    A single workflow that triggers four asynchronous activities with:
        1. activity 1 runtime: 1 sec
        2. activity 2 runtime: 2 sec
        3. activity 3 runtime: 5 sec
        4. activity 4 runtime: 10 sec
    Worker waits for all activities to return before it completes.

*/

public class UseCaseTwo {

    private static String TASK_QUEUE = "TASK_QUEUE";

    @WorkflowInterface
    public interface parallelActivityWorkflow {

        @WorkflowMethod
        public Boolean initiateWorkFlow();
    }

    public static class parallelActivityWorkflowImp implements parallelActivityWorkflow {

        private final parallelActivities activities = Workflow.newActivityStub(parallelActivities.class, ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(20)).build());

        @Override
        public Boolean initiateWorkFlow() {
            System.out.println("Printing this from workflow before all parallel activities have completed");

            /* Parallel Activity Execution */
            List<Promise<Boolean>> promiseList = new ArrayList<>();

            promiseList.add(Async.function(activities::parallelActivityOne));
            promiseList.add(Async.function(activities::parallelActivityTwo));
            promiseList.add(Async.function(activities::parallelActivityThree));
            promiseList.add(Async.function(activities::parallelActivityFour));

            /* Wait for the above activities to complete*/
            Promise.allOf(promiseList).get();

            System.out.println("Printing this from workflow after all parallel activities have completed");
            return true;
        }
    }

    @ActivityInterface
    public interface parallelActivities {

        public Boolean parallelActivityOne();

        public Boolean parallelActivityTwo();

        public Boolean parallelActivityThree();

        public Boolean parallelActivityFour();

    }

    public static class parallelActivitiesImp implements parallelActivities {

        @Override
        public Boolean parallelActivityOne() {
            try {
                Thread.sleep(1000);
                System.out.println("Activity One running for 1000ms");
            } catch (Exception e) {
                throw Activity.wrap(e);
            }

            return true;
        }

        @Override
        public Boolean parallelActivityTwo() {
            try {
                Thread.sleep(2000);
                System.out.println("Activity Two running for 2000ms");
            } catch (Exception e) {
                throw Activity.wrap(e);
            }

            return true;
        }

        @Override
        public Boolean parallelActivityThree() {
            try {
                Thread.sleep(5000);
                System.out.println("Activity Three running for 5000ms");
            } catch (Exception e) {
                throw Activity.wrap(e);
            }

            return true;
        }

        @Override
        public Boolean parallelActivityFour() {
            try {
                Thread.sleep(10000);
                System.out.println("Activity Four running for 10000ms");
            } catch (Exception e) {
                throw Activity.wrap(e);
            }

            return true;
        }
    }

    public static void main(String[] args) {

        /* Worker code */
        WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
        WorkflowClient client = WorkflowClient.newInstance(service);
        WorkerFactory factory = WorkerFactory.newInstance(client);
        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(parallelActivityWorkflowImp.class);
        worker.registerActivitiesImplementations(new parallelActivitiesImp());
        factory.start();

        /* Workflow initiating code */
        WorkflowOptions options = WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build();
        parallelActivityWorkflow workflow = client.newWorkflowStub(parallelActivityWorkflow.class, options);

        WorkflowExecution we = WorkflowClient.start(workflow::initiateWorkFlow);
        // Boolean workFLowStatus = workflow.initiateWorkFlow();

    }
}
