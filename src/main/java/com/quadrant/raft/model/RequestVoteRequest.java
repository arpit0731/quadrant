package com.quadrant.raft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestVoteRequest {
    private long term;
    private String candidateId;
    private long lastLogIndex;
    private long lastLogTerm;
}
