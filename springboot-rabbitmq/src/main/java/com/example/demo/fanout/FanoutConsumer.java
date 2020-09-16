package com.example.demo.fanout;

import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author c__e
 * @date Created in 2020/9/16 17:28
 */
@Component
public class FanoutConsumer {

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue, //创建临时队列
                    exchange = @Exchange(value = "logs", type = "fanout")  //绑定交换机
            )
    })
    public void receive1(String message) {
        System.out.println("message1 = " + message);
    }

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue, //创建临时队列
                    exchange = @Exchange(value = "logs", type = "fanout")  //绑定交换机
            )
    })
    public void receive2(String message) {
        System.out.println("message2 = " + message);
    }
}
