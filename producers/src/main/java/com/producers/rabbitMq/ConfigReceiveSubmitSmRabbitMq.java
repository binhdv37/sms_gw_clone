package com.producers.rabbitMq;

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
public class ConfigReceiveSubmitSmRabbitMq {

	@Value("${mbf.submitSm.queueName1}")
	private String mbfSubmitSmQueue1;

	@Value("${mbf.submitSm.queueName2}")
	private String mbfSubmitSmQueue2;

	@Value("${mbf.submitSm.queueName3}")
	private String mbfSubmitSmQueue3;

	@Value("${mbf.submitSm.queueName4}")
	private String mbfSubmitSmQueue4;

	@Value("${mbf.submitSm.queueName5}")
	private String mbfSubmitSmQueue5;

	@Value("${mbf.dataService.queueName}")
	private String dataServiceQueue;

	@Value("${mbf.exchange}")
	private String mbfExchange;

	@Bean
	DirectExchange exchange() {
		return new DirectExchange(mbfExchange);
	}

	@Bean
	public Queue submitSmQueue1() {
		return new Queue(mbfSubmitSmQueue1, false);
	}

	@Bean
	public Queue submitSmQueue2() {
		return new Queue(mbfSubmitSmQueue2, false);
	}

	@Bean
	public Queue submitSmQueue3() {
		return new Queue(mbfSubmitSmQueue3, false);
	}

	@Bean
	public Queue submitSmQueue4() {
		return new Queue(mbfSubmitSmQueue4, false);
	}

	@Bean
	public Queue submitSmQueue5() {
		return new Queue(mbfSubmitSmQueue5, false);
	}

	@Bean
	public Queue dataServiceQueue() {
		return new Queue(dataServiceQueue, false);
	}

	@Bean
	List<Binding> bindings(DirectExchange exchange) {
		return Arrays.asList(BindingBuilder.bind(submitSmQueue1()).to(exchange).with("mbfSubmitSm1"),
				BindingBuilder.bind(submitSmQueue2()).to(exchange).with("mbfSubmitSm2"),
				BindingBuilder.bind(submitSmQueue3()).to(exchange).with("mbfSubmitSm3"),
				BindingBuilder.bind(submitSmQueue4()).to(exchange).with("mbfSubmitSm4"),
				BindingBuilder.bind(submitSmQueue5()).to(exchange).with("mbfSubmitSm5"),
				BindingBuilder.bind(dataServiceQueue()).to(exchange).with("dataService"));
	}
}
