package org.flossboss.notificationservice;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class NotificationServiceApplication {

	@Autowired
	private EmailSenderService senderService;

	public static void main(String[] args) {

		SpringApplication.run(NotificationServiceApplication.class, args);

	}

	@EventListener(ApplicationReadyEvent.class)
	public void sendMail() throws MqttException {


		//MQTT Broker/Subscriber instance and EmailService as dependency injection
		playSubscriber subscriber = new playSubscriber(senderService);

	}
}
