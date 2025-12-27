package com.quadrant.raft.service;

import com.quadrant.raft.config.NodeConfig;
import com.quadrant.raft.model.RequestVoteRequest;
import com.quadrant.raft.model.RequestVoteResponse;
import com.quadrant.raft.model.RaftState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsensusServiceTest {

    @Mock
    private NodeConfig nodeConfig;

    @Mock
    private RaftClient raftClient;

    private RaftNode raftNode;
    private ConsensusService consensusService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        raftNode = new RaftNode();
        consensusService = new ConsensusService(raftNode, nodeConfig, raftClient);
    }

    @Test
    void testRequestVote_GrantVote() {
        raftNode.setCurrentTerm(0);
        raftNode.setVotedFor(null);

        RequestVoteRequest request = RequestVoteRequest.builder()
                .term(1)
                .candidateId("node2")
                .lastLogIndex(0)
                .lastLogTerm(0)
                .build();

        RequestVoteResponse response = consensusService.requestVote(request);

        assertTrue(response.isVoteGranted());
        assertEquals(1, raftNode.getCurrentTerm());
        assertEquals("node2", raftNode.getVotedFor());
        assertEquals(RaftState.FOLLOWER, raftNode.getState());
    }

    @Test
    void testRequestVote_DenyVote_LowerTerm() {
        raftNode.setCurrentTerm(2);

        RequestVoteRequest request = RequestVoteRequest.builder()
                .term(1)
                .candidateId("node2")
                .lastLogIndex(0)
                .lastLogTerm(0)
                .build();

        RequestVoteResponse response = consensusService.requestVote(request);

        assertFalse(response.isVoteGranted());
        assertEquals(2, raftNode.getCurrentTerm());
    }
}
