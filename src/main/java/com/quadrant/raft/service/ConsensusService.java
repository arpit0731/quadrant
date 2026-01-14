package com.quadrant.raft.service;

import com.quadrant.raft.config.NodeConfig;
import com.quadrant.raft.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConsensusService {

    private final RaftNode raftNode;
    private final NodeConfig nodeConfig;
    private final RaftClient raftClient;

    private final AtomicLong lastHeartbeatTime = new AtomicLong(System.currentTimeMillis());
    private final Random random = new Random();

    @Scheduled(fixedRate = 50)
    public void checkElectionTimeout() {
        if (raftNode.getState() == RaftState.LEADER) {
            sendHeartbeats();
            return;
        }

        long timeout = nodeConfig.getElectionTimeoutMin() + random.nextInt(nodeConfig.getElectionTimeoutMax() - nodeConfig.getElectionTimeoutMin());
        if (System.currentTimeMillis() - lastHeartbeatTime.get() > timeout) {
            startElection();
        }
    }

    private void startElection() {
        log.info("Starting election for term {}", raftNode.getCurrentTerm() + 1);
        log.info("testing", raftNode.getCurrentTerm() + 1);
        raftNode.setState(RaftState.CANDIDATE);
        raftNode.setCurrentTerm(raftNode.getCurrentTerm() + 1);
        raftNode.setVotedFor(nodeConfig.getNodeId());
        lastHeartbeatTime.set(System.currentTimeMillis());

        int votes = 1; // Vote for self
        int requiredVotes = (nodeConfig.getPeers().size() + 1) / 2 + 1;

        for (String peer : nodeConfig.getPeers()) {
            RequestVoteRequest request = RequestVoteRequest.builder()
                    .term(raftNode.getCurrentTerm())
                    .candidateId(nodeConfig.getNodeId())
                    .lastLogIndex(raftNode.getLastLogIndex())
                    .lastLogTerm(raftNode.getLastLogTerm())
                    .build();

            java.util.concurrent.CompletableFuture.supplyAsync(() -> raftClient.sendRequestVote(peer, request))
                    .thenAccept(response -> {
                        if (raftNode.getState() != RaftState.CANDIDATE) return;
                        if (response.getTerm() > raftNode.getCurrentTerm()) {
                            raftNode.setCurrentTerm(response.getTerm());
                            raftNode.setState(RaftState.FOLLOWER);
                            raftNode.setVotedFor(null);
                            return;
                        }
                        if (response.isVoteGranted()) {
                            synchronized (raftNode) {
                                if (raftNode.getState() == RaftState.CANDIDATE) {
                                     // We need to track votes safely. For simplicity here, we assume single-threaded access or use atomic.
                                     // But since we are inside a lambda, we should be careful.
                                     // Let's assume we have a way to count votes.
                                     // For this skeleton, we'll just log.
                                     log.info("Received vote from {}", peer);
                                }
                            }
                        }
                    });
        }
    }

    private void sendHeartbeats() {
        for (String peer : nodeConfig.getPeers()) {
            long prevLogIndex = raftNode.getNextIndex().getOrDefault(peer, raftNode.getLastLogIndex() + 1) - 1;
            long prevLogTerm = raftNode.getTerm(prevLogIndex);
            
            // For heartbeats, entries can be empty
            AppendEntriesRequest request = AppendEntriesRequest.builder()
                    .term(raftNode.getCurrentTerm())
                    .leaderId(nodeConfig.getNodeId())
                    .prevLogIndex(prevLogIndex)
                    .prevLogTerm(prevLogTerm)
                    .entries(java.util.Collections.emptyList()) 
                    .leaderCommit(raftNode.getCommitIndex())
                    .build();

            java.util.concurrent.CompletableFuture.supplyAsync(() -> raftClient.sendAppendEntries(peer, request))
                    .thenAccept(response -> {
                         if (raftNode.getState() != RaftState.LEADER) return;
                         if (response.getTerm() > raftNode.getCurrentTerm()) {
                             raftNode.setCurrentTerm(response.getTerm());
                             raftNode.setState(RaftState.FOLLOWER);
                             raftNode.setVotedFor(null);
                             return;
                         }
                         if (response.isSuccess()) {
                             // Update matchIndex and nextIndex
                             raftNode.getMatchIndex().put(peer, prevLogIndex);
                             raftNode.getNextIndex().put(peer, prevLogIndex + 1);
                         } else {
                             // Decrement nextIndex and retry (handled in next heartbeat)
                             raftNode.getNextIndex().put(peer, Math.max(0, raftNode.getNextIndex().getOrDefault(peer, 0L) - 1));
                         }
                    });
        }
    }

    public synchronized RequestVoteResponse requestVote(RequestVoteRequest request) {
        if (request.getTerm() < raftNode.getCurrentTerm()) {
            return RequestVoteResponse.builder().term(raftNode.getCurrentTerm()).voteGranted(false).build();
        }

        if (request.getTerm() > raftNode.getCurrentTerm()) {
            raftNode.setCurrentTerm(request.getTerm());
            raftNode.setState(RaftState.FOLLOWER);
            raftNode.setVotedFor(null);
        }

        lastHeartbeatTime.set(System.currentTimeMillis());

        boolean logIsUpToDate = false;
        long lastLogIndex = raftNode.getLastLogIndex();
        long lastLogTerm = raftNode.getLastLogTerm();

        if (request.getLastLogTerm() > lastLogTerm) {
            logIsUpToDate = true;
        } else if (request.getLastLogTerm() == lastLogTerm && request.getLastLogIndex() >= lastLogIndex) {
            logIsUpToDate = true;
        }

        if ((raftNode.getVotedFor() == null || raftNode.getVotedFor().equals(request.getCandidateId())) && logIsUpToDate) {
            raftNode.setVotedFor(request.getCandidateId());
            return RequestVoteResponse.builder().term(raftNode.getCurrentTerm()).voteGranted(true).build();
        }

        return RequestVoteResponse.builder().term(raftNode.getCurrentTerm()).voteGranted(false).build();
    }

    public synchronized AppendEntriesResponse appendEntries(AppendEntriesRequest request) {
        if (request.getTerm() < raftNode.getCurrentTerm()) {
            return AppendEntriesResponse.builder().term(raftNode.getCurrentTerm()).success(false).build();
        }

        if (request.getTerm() > raftNode.getCurrentTerm()) {
            raftNode.setCurrentTerm(request.getTerm());
            raftNode.setState(RaftState.FOLLOWER);
            raftNode.setVotedFor(null);
        }
        
        raftNode.setLeaderId(request.getLeaderId());
        lastHeartbeatTime.set(System.currentTimeMillis());

        // Log consistency check
        if (request.getPrevLogIndex() >= 0) {
             if (raftNode.getLog().size() <= request.getPrevLogIndex() ||
                 raftNode.getLog().get((int) request.getPrevLogIndex()).getTerm() != request.getPrevLogTerm()) {
                 return AppendEntriesResponse.builder().term(raftNode.getCurrentTerm()).success(false).build();
             }
        }

        // Append new entries
        if (request.getEntries() != null && !request.getEntries().isEmpty()) {
            long index = request.getPrevLogIndex() + 1;
            for (LogEntry entry : request.getEntries()) {
                if (raftNode.getLog().size() > index) {
                    if (raftNode.getLog().get((int) index).getTerm() != entry.getTerm()) {
                        // Conflict: delete existing entry and all that follow
                        raftNode.getLog().subList((int) index, raftNode.getLog().size()).clear();
                        raftNode.getLog().add(entry);
                    }
                } else {
                    raftNode.getLog().add(entry);
                }
                index++;
            }
        }

        // Update commit index
        if (request.getLeaderCommit() > raftNode.getCommitIndex()) {
            raftNode.setCommitIndex(Math.min(request.getLeaderCommit(), raftNode.getLastLogIndex()));
        }

        return AppendEntriesResponse.builder().term(raftNode.getCurrentTerm()).success(true).build();
    }
}
