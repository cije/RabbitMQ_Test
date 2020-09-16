package com.ce.springbootrabbitmqconsumer.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 消息监听器
 *
 * @author c__e
 * @date Created in 2020/9/16 11:00
 */
@Component
public class MyListener {
    /**
     * 接收队列消息
     *
     * @param message 接收到的消息
     */
    @RabbitListener(queues = "item_queue")
    public void myListener(String message) {
        System.out.println("消费者接收到消息：" + message);
    }
}
