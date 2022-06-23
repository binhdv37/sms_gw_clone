package com.consumers.rabbitMq;

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
public class ConfigReceiveDeliverSmRabbitMq {

	@Value("${mbf.deliverSm.queueName}")
	private String mbfDeliverSmQueue;

	@Value("${mbf.submitSm.queueName}")
	private String mbfSubmitSmQueue;

	@Value("${mbf.submitSmResp.queueName}")
	private String mbfSubmitSmRespQueue;

	@Value("${mbf.dataService.queueName}")
	private String dataServiceQueue;

	@Value("${mbf.exchange}")
	private String mbfExchange;

	@Bean
	DirectExchange exchange() {
		return new DirectExchange(mbfExchange);
	}

	@Bean
	public Queue mbfDeliverSmQueue() {
		return new Queue(mbfDeliverSmQueue, false);
	}

	@Bean
	public Queue submitSmQueue() {
		return new Queue(mbfSubmitSmQueue, false);
	}

	@Bean
	public Queue submitSmRespQueue() {
		return new Queue(mbfSubmitSmRespQueue, false);
	}

	@Bean
	public Queue dataServiceQueue() {
		return new Queue(dataServiceQueue, false);
	}

	@Bean
	List<Binding> bindings(DirectExchange exchange) {
		return Arrays.asList(BindingBuilder.bind(mbfDeliverSmQueue()).to(exchange).with("mbfDeliverSm"),
				BindingBuilder.bind(submitSmQueue()).to(exchange).with("mbfSubmitSm"),
				BindingBuilder.bind(submitSmRespQueue()).to(exchange).with("mbfSubmitSmResp"),
				BindingBuilder.bind(dataServiceQueue()).to(exchange).with("dataService"));
	}
}
