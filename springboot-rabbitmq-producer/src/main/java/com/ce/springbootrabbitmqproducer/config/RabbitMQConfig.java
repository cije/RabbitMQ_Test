package com.ce.springbootrabbitmqproducer.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author c__e
 * @date Created in 2020/9/16 10:35
 */
@Configuration
public class RabbitMQConfig {
    /**
     * 交换机名称
     */
    public static final String ITEM_TOPIC_EXCHANGE = "item_topic_exchange";
    /**
     * 队列名称
     */
    public static final String ITEM_QUEUE = "item_queue";
    /**
     * 路由key
     */
    public static final String ROUTING_KEY = "item.#";

    /**
     * 声明交换机
     */
    @Bean("itemTopicExchange")
    public Exchange topicExchange() {
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE).durable(true).build();
    }

    /**
     * 声明队列
     */
    @Bean("itemQueue")
    public Queue itemQueue() {
        return QueueBuilder.durable(ITEM_QUEUE).build();
    }

    /**
     * 将队列绑定到交换机
     */
    @Bean
    public Binding itemQueueExchange(@Qualifier("itemQueue") Queue queue, @Qualifier("itemTopicExchange") Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY).noargs();
    }
}
