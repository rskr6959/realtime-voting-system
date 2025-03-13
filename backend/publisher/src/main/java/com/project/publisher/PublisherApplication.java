package com.project.publisher;

import com.project.publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.apache.kafka.clients.admin.AdminClient;

@SpringBootApplication
@EnableScheduling
public class PublisherApplication {

	@Value("${server.port}")
	private int port;

	public static void main(String[] args) {
		SpringApplication.run(PublisherApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@EventListener
	public void onWebServerInitialized(WebServerInitializedEvent event) {
		this.port = event.getWebServer().getPort();
		System.out.println("Started application on port: " + port);
	}

	@Bean
	public PublisherService publisherService(RestTemplate restTemplate, 
										   KafkaTemplate<String, String> kafkaTemplate,
										   AdminClient adminClient) {
		PublisherService publisherService = new PublisherService(restTemplate, kafkaTemplate, adminClient);
		publisherService.setPort(port);
		return publisherService;
	}
} 