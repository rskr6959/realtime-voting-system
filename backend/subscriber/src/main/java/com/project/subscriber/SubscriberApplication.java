package com.project.subscriber;

import com.project.subscriber.service.SubscriberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class SubscriberApplication {

	private static final Logger logger = LoggerFactory.getLogger(SubscriberApplication.class);

	@Value("${server.port}")
	private int port;

	public static void main(String[] args) {
		SpringApplication.run(SubscriberApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@EventListener
	public void onWebServerInitialized(WebServerInitializedEvent event) {
		this.port = event.getWebServer().getPort();
		logger.info("Started application on port: {}", port);
	}

	@Bean
	public SubscriberService subscriberService(RestTemplate restTemplate,
											 AdminClient adminClient,
											 KafkaListenerEndpointRegistry kafkaListenerRegistry) {
		SubscriberService subscriberService = new SubscriberService(restTemplate, adminClient, kafkaListenerRegistry);
		subscriberService.setPort(port);
		return subscriberService;
	}
} 