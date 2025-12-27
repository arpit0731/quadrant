# 🧭 Quadrant
### Distributed Consensus System based on Raft

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

---

## 🚀 Overview

**Quadrant** is a robust implementation of the **Raft Consensus Algorithm** built with Java and Spring Boot. It is designed to demonstrate leader election, log replication, and fault tolerance in a distributed system.

> "In search of consistency in a chaotic network."

## ✨ Features

- **👑 Leader Election**: Automatic failover and leader election using randomized timeouts.
- **📜 Log Replication**: Strong consistency log replication from Leader to Followers.
- **💓 Heartbeat Mechanism**: Periodic heartbeats to maintain authority and prevent unnecessary elections.
- **🔌 REST API**: Simple HTTP-based RPC mechanism for inter-node communication.
- **🛡️ Fault Tolerance**: Resilient to node failures and network partitions.

---

## 🛠️ Architecture

The system consists of multiple **Quadrant Nodes** communicating via HTTP. Each node operates in one of three states:

1.  **Follower**: Passive state, responds to requests.
2.  **Candidate**: Active state, soliciting votes to become leader.
3.  **Leader**: Active state, handles all client requests and replicates logs.

---

## 🏁 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**

### 📦 Installation

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/yourusername/quadrant.git
    cd quadrant
    ```

2.  **Build the project**:
    ```bash
    mvn clean install
    ```

---

## 🖥️ Running the Cluster

To simulate a distributed cluster locally, run multiple instances on different ports.

### Node 1 (Port 8081)
```bash
java -jar target/quadrant-0.0.1-SNAPSHOT.jar \
  --server.port=8081 \
  --raft.nodeId=node1 \
  --raft.peers=http://localhost:8082,http://localhost:8083
```

### Node 2 (Port 8082)
```bash
java -jar target/quadrant-0.0.1-SNAPSHOT.jar \
  --server.port=8082 \
  --raft.nodeId=node2 \
  --raft.peers=http://localhost:8081,http://localhost:8083
```

### Node 3 (Port 8083)
```bash
java -jar target/quadrant-0.0.1-SNAPSHOT.jar \
  --server.port=8083 \
  --raft.nodeId=node3 \
  --raft.peers=http://localhost:8081,http://localhost:8082
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/raft/requestVote` | Invoked by Candidates to gather votes. |
| `POST` | `/raft/appendEntries` | Invoked by Leader to replicate logs/heartbeats. |

---

## 🧪 Verification

Watch the logs! You will see:
- Nodes starting as **FOLLOWER**.
- Election timeouts triggering transition to **CANDIDATE**.
- One node receiving majority votes and becoming **LEADER**.
- **LEADER** sending periodic heartbeats.

---

Made with ❤️ by Arpit Yadav
