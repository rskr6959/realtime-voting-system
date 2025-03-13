# Distributed Real-Time Voting and Polling System

A scalable, fault-tolerant, real-time voting and polling system built with Java Spring Boot and Next.js.

## Project Overview

This project implements a distributed voting system that can handle thousands of concurrent votes with minimal latency. It uses a publish-subscribe architecture with leader election, logical clocks, and data replication to ensure fault tolerance and consistency.

### Key Features

- **Real-time vote processing**: Votes are processed and results are updated in real-time
- **Fault tolerance**: The system can recover from node failures without losing data
- **Scalability**: Designed to handle thousands of concurrent users
- **Leader election**: Uses the Bully Algorithm for leader election
- **Logical clocks**: Implements Lamport Timestamps for event ordering
- **Data replication**: Ensures consistency across all nodes

## How This Project Meets Requirements

This project satisfies the distributed systems final project requirements by implementing:

### 1. Distributed System Principles
- **Scalability**: The system uses a distributed architecture with separate components (coordinator, broker, publisher, subscriber) that can scale independently.
- **Fault Tolerance**: The system can recover from node failures through leader election and data replication.
- **Consistency**: Lamport Timestamps ensure consistent event ordering across the system.

### 2. Distributed Algorithms (4 implemented)
- **Leader Election**: The Bully Algorithm is used to elect a leader broker when failures occur.
- **Timestamps**: Lamport Logical Clocks maintain causal ordering of events across all components.
- **Replication and Consistency Protocols**: Leader-follower replication ensures data consistency across brokers.
- **Gossip Protocols**: Heartbeat mechanisms and state synchronization implement a form of gossip protocol.

### 3. Architecture
- **Publish-Subscribe Model**: Decouples publishers (voters) from subscribers (result viewers).
- **Coordinator-Broker Architecture**: Provides centralized discovery with distributed processing.
- **RESTful APIs**: Enables easy integration and communication between components.

## System Architecture

The system consists of the following components:

- **Coordinator**: Manages broker registration and leader election
- **Broker**: Integrates with Kafka for message routing and storage
- **Publisher**: Creates polls and publishes votes to Kafka topics
- **Subscriber**: Receives real-time Election Results from Kafka topics
- **Frontend**: Provides a user interface for creating polls and voting
- **Kafka**: Provides the messaging backbone for the entire system

### Kafka Integration

The system uses Apache Kafka as the messaging backbone:

- **Topics**: Each poll is represented as a Kafka topic
- **Publishers**: Send votes as messages to Kafka topics
- **Subscribers**: Consume messages from Kafka topics to display results
- **Brokers**: Manage Kafka topics and handle message routing

This architecture provides:
- **Scalability**: Kafka can handle high throughput of messages
- **Durability**: Messages are persisted and replicated
- **Fault Tolerance**: Kafka's distributed nature ensures the system can recover from failures
- **Real-time Processing**: Messages are delivered with low latency

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Apache Kafka 3.x (or Docker and Docker Compose)
- Web browser (Chrome, Firefox, Safari, or Edge)

### Running the Application Locally

#### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/real-time-voting-polling.git
cd real-time-voting-polling
```

#### 2. Start Kafka

You can start Kafka using either Docker Compose (recommended) or manually.

**Option 1: Using Docker Compose (Recommended)**

```bash
# Start Kafka and Zookeeper using Docker Compose
docker-compose up -d

# You can access the Kafka UI at http://localhost:8090
```

**Option 2: Manual Kafka Setup**

If you prefer to run Kafka manually:

```bash
# Start Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

# In a new terminal, start Kafka
bin/kafka-server-start.sh config/server.properties
```

#### 3. Start the Coordinator Service

```bash
cd backend/coordinator
mvn spring-boot:run
```

The coordinator will start on port 8080. You can verify it's running by visiting http://localhost:8080/api/health

#### 3. Start Multiple Broker Services

Open new terminal windows to start multiple broker instances on different ports:

```bash
cd backend/broker
# Start the first broker on port 8081
mvn spring-boot:run

# Start additional brokers on different ports
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8085
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8086
```

You can verify each broker is running by visiting their respective health endpoints (e.g., http://localhost:8081/api/health)

#### 4. Start the Publisher Service

Open a new terminal window:

```bash
cd backend/publisher
mvn spring-boot:run
```

The publisher will start on port 8082. You can verify it's running by visiting http://localhost:8082/api/health

#### 5. Start the Subscriber Service

Open a new terminal window:

```bash
cd backend/subscriber
mvn spring-boot:run
```

The subscriber will start on port 8083. You can verify it's running by visiting http://localhost:8083/api/health

#### 6. Start the Frontend

Open a new terminal window:

```bash
cd frontend
# Start a simple HTTP server on port 3000
python -m http.server 3000
```

Then open your browser and navigate to http://localhost:3000 to access the application.

### Testing the System

1. Create a new poll using the "Create a New Poll" section
2. Cast votes using the "Cast Your Vote" section
3. Subscribe to Election Results using the "Subscribe to Election Results" section
4. View results using the "Election Results" section

### Testing Fault Tolerance

To test fault tolerance:

1. Start all services as described above (coordinator, multiple brokers, publisher, subscriber, and frontend)
2. Create a poll and cast some votes
3. Kill one of the broker services (Ctrl+C in its terminal)
4. Observe in the coordinator logs that it detects the broker failure and elects a new leader from the remaining brokers
5. Verify that the system continues to function correctly without interruption
6. You can restart the killed broker to see it rejoin the system

## Distributed Algorithms Implementation

### 1. Leader Election (Bully Algorithm)

The coordinator service implements the Bully Algorithm for leader election:

- When a broker joins, it registers with the coordinator
- The coordinator designates one broker as the leader
- If the leader fails (detected through missed heartbeats), the coordinator elects a new leader
- All components regularly check with the coordinator to identify the current leader

### 2. Timestamps (Lamport Logical Clocks)

All components implement Lamport Timestamps:

- Each component maintains its own logical clock
- The clock is incremented with each operation
- When receiving a message with a timestamp, the local clock is updated to max(local, received) + 1
- Timestamps are passed with every API call to maintain causal ordering

### 3. Replication and Consistency

The system implements data replication through Apache Kafka:

- **Topic Replication**: Kafka topics can be configured with a replication factor to ensure data is replicated across multiple brokers
- **Partition Leadership**: Each Kafka partition has a leader that handles all reads and writes, with followers that replicate the data
- **Consistency Guarantees**: Kafka provides at-least-once delivery semantics, ensuring messages are not lost
- **Offset Management**: Consumers track their position in each partition using offsets, allowing them to resume from where they left off
- **Producer Acknowledgments**: Producers can be configured to wait for acknowledgments from the leader or all replicas before considering a write successful

This approach ensures:
- High availability of data
- Fault tolerance against broker failures
- Consistent message ordering within partitions
- Scalable throughput by adding more partitions

### 4. Gossip Protocol

The system implements aspects of a gossip protocol through:

- Regular heartbeat messages from brokers to the coordinator
- Periodic synchronization of state between components
- Information dissemination through the leader to all followers

## Troubleshooting

- **Port conflicts**: If any service fails to start due to port conflicts, you can modify the port in the `application.properties` file in each service's resources directory
- **Connection issues**: Ensure all services are running before using the frontend
- **Data inconsistency**: If you notice inconsistent results, wait a few seconds for synchronization to complete

## Contributors

- Shiva Kumar Rangapuram
- Tomohiro Kanazawa
- Venkat Dugasani

