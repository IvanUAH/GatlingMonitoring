package com.gmonitor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RunStatusView {

    private boolean running;
    private String runId;
}
