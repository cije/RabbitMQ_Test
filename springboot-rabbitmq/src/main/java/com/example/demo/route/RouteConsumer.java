package com.example.demo.route;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author c__e
 * @date Created in 2020/9/16 17:59
 */
@Component
public class RouteConsumer {

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue, //临时队列
                    exchange = @Exchange(value = "directs", type = "direct"), //指定交换机
                    key = {"info", "error", "warning"}
            )
    })
    public void receiver1(String message) {
        System.out.println("message1 = " + message);
    }

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue, //临时队列
                    exchange = @Exchange(value = "directs", type = "direct"), //指定交换机
                    key = {"error"}
            )
    })
    public void receiver2(String message) {
        System.out.println("message2 = " + message);
    }

}
