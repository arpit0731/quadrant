package com.quadrant.raft.service;

import com.quadrant.raft.model.LogEntry;
import com.quadrant.raft.model.RaftState;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Component
public class RaftNode {
    // Persistent state
    private long currentTerm = 0;
    private String votedFor = null;
    private List<LogEntry> log = Collections.synchronizedList(new ArrayList<>());

    // Volatile state
    private long commitIndex = 0;
    private long lastApplied = 0;
    private RaftState state = RaftState.FOLLOWER;
    private String leaderId = null;

    // Volatile state on leaders
    private Map<String, Long> nextIndex = new ConcurrentHashMap<>();
    private Map<String, Long> matchIndex = new ConcurrentHashMap<>();

    public long getLastLogIndex() {
        return log.size() - 1;
    }

    public long getLastLogTerm() {
        if (log.isEmpty()) {
            return 0;
        }
        return log.get(log.size() - 1).getTerm();
    }
    
    public long getTerm(long index) {
        if (index < 0 || index >= log.size()) {
            return 0;
        }
        return log.get((int) index).getTerm();
    }
}
