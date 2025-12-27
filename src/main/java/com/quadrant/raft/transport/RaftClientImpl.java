package com.quadrant.raft.transport;

import com.quadrant.raft.model.AppendEntriesRequest;
import com.quadrant.raft.model.AppendEntriesResponse;
import com.quadrant.raft.model.RequestVoteRequest;
import com.quadrant.raft.model.RequestVoteResponse;
import com.quadrant.raft.service.RaftClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RaftClientImpl implements RaftClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public RequestVoteResponse sendRequestVote(String peer, RequestVoteRequest request) {
        String url = peer + "/raft/requestVote";
        try {
            return restTemplate.postForObject(url, request, RequestVoteResponse.class);
        } catch (Exception e) {
            // Log error and return a failure response or null
            return RequestVoteResponse.builder().term(0).voteGranted(false).build();
        }
    }

    @Override
    public AppendEntriesResponse sendAppendEntries(String peer, AppendEntriesRequest request) {
        String url = peer + "/raft/appendEntries";
        try {
            return restTemplate.postForObject(url, request, AppendEntriesResponse.class);
        } catch (Exception e) {
            // Log error and return a failure response or null
            return AppendEntriesResponse.builder().term(0).success(false).build();
        }
    }
}
