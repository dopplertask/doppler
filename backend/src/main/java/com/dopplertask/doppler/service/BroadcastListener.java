package com.dopplertask.doppler.service;

import com.dopplertask.doppler.domain.OutputType;

public interface BroadcastListener {
    void run(String output, OutputType outputType);
}
