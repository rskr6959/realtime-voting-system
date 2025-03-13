# Real-Time Voting and Polling Frontend

This is a minimal frontend application for testing the real-time voting and polling backend system.

## Overview

This frontend application provides a simple user interface to:

1. View available polls
2. Cast votes on polls
3. Subscribe to Election Results
4. View Election Results in real-time

## Prerequisites

Before using this frontend, make sure the backend services are running:

- Publisher service (default port: 8081)
- Subscriber service (default port: 8082)
- Broker service
- Coordinator service

## How to Use

1. Open the `index.html` file in a web browser.
2. The application will display the list of candidates.
3. To cast a vote, select a candidate from the dropdown, enter your vote option, and click "Submit Vote".
4. To view results, first subscribe to a poll in the "Subscribe to Election Results" section, then select the same poll in the "Election Results" section and click "Get Results".

## API Integration

This frontend integrates with the backend using the following API endpoints:

### Publisher API (port 8081)

- `POST /api/publish?topic={topic}&timestamp={timestamp}` - Publish a message (cast a vote)

### Subscriber API (port 8082)

- `POST /api/subscribe?timestamp={timestamp}` - Subscribe to a topic/poll
- `GET /api/messages/{topic}?timestamp={timestamp}` - Get all messages for a topic/poll

## Troubleshooting

If you encounter issues:

1. Check that all backend services are running
2. Verify the correct ports are configured in the JavaScript (publisherBaseUrl and subscriberBaseUrl)
3. Check the browser console for error messages
4. Ensure CORS is properly configured on the backend to allow requests from the frontend

## Notes

This is a minimal implementation for testing purposes. In a production environment, you would want to:

1. Add proper error handling and user feedback
2. Implement authentication and authorization
3. Add real-time updates using WebSockets or Server-Sent Events
4. Improve the UI/UX design
5. Add form validation and security measures 