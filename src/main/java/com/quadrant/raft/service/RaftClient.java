package com.quadrant.raft.service;

import com.quadrant.raft.model.AppendEntriesRequest;
import com.quadrant.raft.model.AppendEntriesResponse;
import com.quadrant.raft.model.RequestVoteRequest;
import com.quadrant.raft.model.RequestVoteResponse;

public interface RaftClient {
    RequestVoteResponse sendRequestVote(String peer, RequestVoteRequest request);
    AppendEntriesResponse sendAppendEntries(String peer, AppendEntriesRequest request);
}
