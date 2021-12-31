package com.isaveTemporal.HeartBeat;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface HeartBeatActivity {

    public boolean heartBeatActivity();
}

