package com.quadrant.raft.controller;

import com.quadrant.raft.model.AppendEntriesRequest;
import com.quadrant.raft.model.AppendEntriesResponse;
import com.quadrant.raft.model.RequestVoteRequest;
import com.quadrant.raft.model.RequestVoteResponse;
import com.quadrant.raft.service.ConsensusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/raft")
@RequiredArgsConstructor
public class RaftController {

    private final ConsensusService consensusService;

    @PostMapping("/requestVote")
    public ResponseEntity<RequestVoteResponse> requestVote(@RequestBody RequestVoteRequest request) {
        return ResponseEntity.ok(consensusService.requestVote(request));
    }

    @PostMapping("/appendEntries")
    public ResponseEntity<AppendEntriesResponse> appendEntries(@RequestBody AppendEntriesRequest request) {
        return ResponseEntity.ok(consensusService.appendEntries(request));
    }
}
