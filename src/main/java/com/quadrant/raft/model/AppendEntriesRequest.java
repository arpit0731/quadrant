package com.quadrant.raft.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppendEntriesRequest {
    private long term;
    private String leaderId;
    private long prevLogIndex;
    private long prevLogTerm;
    private List<LogEntry> entries;
    private long leaderCommit;
}
