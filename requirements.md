DistributedSystemsProjectProposal

COEN 317  
Distributed Systems – Project Proposal

Distributed Real-Time Voting and Polling System

Instructor - Prof. Ramin Moazzeni

Team Members:
Shiva Kumar Rangapuram (#07700005992)
Tomohiro Kanazawa (#1658062)
Venkat Dugasani(#07700005825)

Abstract:
As the demand for scalable and fault-tolerant voting platforms increases, real-time voting
systems must achieve high availability, low latency, and consistency. This project implements a
distributed real-time voting and polling system capable of maintaining reliability and speed in
vote processing. Using Bully Algorithm for leader election, Apache Kafka for real-time
messaging, and Lamport Timestamps for event ordering, the system efficiently aggregates votes
while ensuring fault tolerance and scalability. The system is deployed on AWS EC2 instances
and utilizes RESTful APIs for communication. This proposal describes the architecture,
methodology, and implementation approach of the system, along with key research contributions.

Motivation:
The growing dependence on digital tools for immediate polling and voting introduces additional
obstacles, such as speed, trust, and safety. The legacy voting systems face delays in results
processing due to bottlenecks, the risks of single points of failure, and also have limited
scalability of participation. These challenges are addressed by distributed architecture ensuring
that millions of votes can be processed concurrently, make seamless recovery from failures, and
real-time, accurate vote aggregation. The motivation of this project is based on creating a fair,
consistent, efficient, robust, and highly available real-time polling system with a secure voting
system.

Introduction:
Real-time voting systems are a key strategic asset in modern, democratic, participative
environments. Yet the traditional centralized approaches generally suffer the issues of bottleneck,
single point of failure, and scalability. These challenges can be alleviated through a distributed
approach by using leader election algorithms, fault-tolerant data streaming, and event-driven
architectures. This project aims to create: High available and fault-tolerant voting system by
fulfilling:

● Scalability – Handling thousands to millions of concurrent users.  
● Real-time Processing – Ensuring minimum latency in vote aggregation.  
● Fault Tolerance – Maintaining system stability during node failures.  
● Secure and Consistent Vote Handling – Avoiding data loss, duplication, and tampering.

Objectives:
1. Develop a scalable voting system capable of handling thousands of concurrent votes.
2. Ensure leader election for efficient vote aggregation and failure recovery using the Bully
Algorithm.
3. Implement Apache Kafka’s publish-subscribe model for asynchronous vote transmission.
4. Utilize Lamport Timestamps for consistent event ordering and processing.
5. Implement data replication and synchronization for fault tolerance.
6. Develop a Resource Discovery Protocol for dynamically registering system components.
7. Deploy the system on AWS EC2 instances for cloud scalability and performance.
8. Ensure secure vote processing with end-to-end encryption, RESTful API communication,
and access control mechanisms.

Methodology:
● System Design: The architecture consists of voter interfaces, Kafka message brokers,
and leader-elected result aggregation nodes.
● Leader Election: The Bully Algorithm will be used for leader election, ensuring rapid
leader selection upon failures.
● Resource Discovery: A protocol will enable dynamic discovery and registration of
brokers, publishers, and subscribers.
● Data Synchronization: The leader broker synchronizes data (topics, messages,
subscribers) with follower brokers.
● Logical Clocks: Brokers will maintain Lamport Timestamps to maintain event
consistency.
● Deployment: The system will be deployed on AWS EC2 instances with RESTful APIs
handling communication.
● Technology Stack: Backend – Java (Spring Boot), Frontend – HTML, CSS, Data
Streaming – Kafka, Cloud – AWS EC2.

System Architecture:
● Voter Interface: A distributed service enabling vote collection and processing.
● Message Broker (Apache Kafka): Handles vote transmission and event-driven
processing.
● Leader Election (Bully Algorithm): Ensures a single node finalizes vote aggregation.
● Resource Discovery: Enables dynamic component registration.
● Data Replication: Synchronizes brokers to ensure consistency and fault tolerance.
● Deployment: Hosted on AWS EC2 instances for scalability.

Implementation Plan:
1. System Design & Infrastructure Setup – Design the architecture and set up Kafka
clusters on AWS EC2 instances.
2. Development Phase – Implement backend and integration with Kafka.
3. Leader Election & Resource Discovery – Implement the Bully Algorithm and
component registration.
4. Logical Clocks & Data Synchronization – Implement Lamport Timestamps and broker
synchronization.
5. Testing & Optimization – Simulate large-scale voting scenarios and fine-tune
performance.
6. Deployment & Monitoring – Deploy the system on AWS EC2 instances and establish
real-time failure monitoring.

Expected Outcomes:
● A fully functional, real-time voting system handling large-scale participation.
● Improved user engagement with instant result updates.
● Enhanced system reliability using Bully Algorithm and Logical Clocks.
● Secure and consistent vote processing through encryption and RESTful APIs.

Project Timeline:
● Week 1: Finalize system design, set up infrastructure, define message broker strategy.
● Week 2: Develop backend logic, integrate Kafka.
● Week 3: Implement Bully Algorithm for leader election and resource discovery.
● Week 4: Integrate logical clocks, conduct testing, optimize performance, deploy system
on AWS EC2.

Conclusion:
This project aims to develop a scalable, fault-tolerant, and real-time distributed voting system.
By leveraging Bully Algorithm for leader election, Kafka for messaging, Logical Clocks for
event ordering, and data replication for synchronization, the system ensures efficient,
high-availability vote processing. Additionally, deploying on AWS EC2 and utilizing RESTful
APIs ensures scalability and accessibility, making it an ideal solution for real-world polling
applications.

