package com.quadrant.raft.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "raft")
public class NodeConfig {
    private String nodeId;
    private List<String> peers; // List of peer URLs e.g., http://localhost:8081
    private int electionTimeoutMin = 150;
    private int electionTimeoutMax = 300;
    private int heartbeatInterval = 50;
}
