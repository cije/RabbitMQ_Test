package com.example.demo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = RabbitMQSpringbootApplication.class)
@ExtendWith(SpringExtension.class)
public class TestRabbitMQ {
    // 注入RabbitTemplate
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // fanout 广播
    @Test
    public void testFanout() {
        rabbitTemplate.convertAndSend("logs", "", "fanout模型发送的消息");
    }

    // topic 通配符模式 动态路由
    @Test
    public void testTopic() {
        rabbitTemplate.convertAndSend("topics", "user.save", "user.save 路由消息");
        rabbitTemplate.convertAndSend("topics", "order.save", "order.save 路由消息");
        rabbitTemplate.convertAndSend("topics", "product.test.test", "product.test.test 路由消息");
    }

    // route 路由模式
    @Test
    public void testRoute() {
        rabbitTemplate.convertAndSend("directs", "info", "发送info的key的路由信息");
        rabbitTemplate.convertAndSend("directs", "error", "发送error的key的路由信息");
    }

    // 简单模型
    @Test
    public void testHello() {
        rabbitTemplate.convertAndSend("hello", "hello-world");
    }

    // work
    @Test
    public void testWork() {
        for (int i = 0; i < 10; i++) {
            rabbitTemplate.convertAndSend("work", "work模型-" + i);
        }
    }
}
