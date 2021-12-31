package com.isaveTemporal.HeartBeat;

import io.temporal.activity.Activity;
import java.util.Optional;

public class HeartBeatActivityImp implements HeartBeatActivity {

    @Override
    public boolean heartBeatActivity() {

        int start = 0;

        /**** Getting the heartbeats 'last heartbeat details' ******/
        Optional<Integer> lastHeartBeat = Activity.getExecutionContext().getHeartbeatDetails(Integer.class);

        if (lastHeartBeat.isPresent()) {
            System.out.println("Failure point: " + lastHeartBeat.get());
            System.out.println("Using HeartBeat to skip failure point on retry i.e. skip i = " + lastHeartBeat.get() + " on re-run");
            start = lastHeartBeat.get() + 1;
        }
        /**** Getting the heartbeats 'last heartbeat details' ******/


        for (int i = start; i < 10; i++) {
            try {
                Thread.sleep(2000);
                Activity.getExecutionContext().heartbeat(i);

                if (i == 3) {
                    throw new Exception("Forced Exception thrown when i = 3 to fail the activity");
                }

            } catch (Exception e) {
                throw Activity.wrap(e);
            }
        }
        return true;
    }
}
