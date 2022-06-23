package com.dataServices.rabbitMq;

import java.util.Arrays;
import java.util.List;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class ConfigMasterDataRabbitMq {

	@Value("${mbf.masterDataProducer.queueName}")
	private String masterDataProducer;

	@Value("${mbf.dataService.queueName}")
	private String dataServiceQueue;

	@Value("${mbf.submitSmWeb.queueName}")
	private String submitSmWebQueue;

	@Value("${mbf.exchange}")
	private String mbfExchange;

	@Bean
	public Queue masterDataProducerQueue() {
		return new Queue(masterDataProducer, false);
	}

	@Bean
	public Queue dataServiceQueue() {
		return new Queue(dataServiceQueue, false);
	}

	@Bean
	public Queue submitSmWebQueue() {
		return new Queue(submitSmWebQueue, false);
	}

	@Bean
	DirectExchange exchange() {
		return new DirectExchange(mbfExchange);
	}

	@Bean
	List<Binding> bindings(DirectExchange exchange) {
		return Arrays.asList(BindingBuilder.bind(masterDataProducerQueue()).to(exchange).with("mbfMasterDataProducer"),
				BindingBuilder.bind(dataServiceQueue()).to(exchange).with("dataService"),
				BindingBuilder.bind(submitSmWebQueue()).to(exchange).with("submitSmWebQueue"));
	}
}
