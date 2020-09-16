package com.ce.rabbitmq.topic;

import com.ce.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 通配符模式：发送消息
 */
public class Producer {
    /**
     * 交换机名称
     */
    static final String TOPIC_EXCHANGE = "topic_exchange";
    /**
     * 队列名称
     */
    static final String TOPIC_QUEUE_1 = "topic_queue_1";
    static final String TOPIC_QUEUE_2 = "topic_queue_2";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 1.  创建连接
        Connection connection = ConnectionUtil.getConnection();
        // 2.  创建频道
        Channel channel = connection.createChannel();
        // 3.  声明交换机  参数1：交换机名称  参数2：交换机类型 （fanout direct topic）
        channel.exchangeDeclare(TOPIC_EXCHANGE, BuiltinExchangeType.TOPIC);

        // 6.发送消息
        String message = "商品新增  通配符模式  routing key为 item.insert";

        channel.basicPublish(TOPIC_EXCHANGE, "item.insert", null, message.getBytes());
        System.out.println("已发送消息：" + message);

        message = "商品修改  通配符模式  routing key为 item.update";
        channel.basicPublish(TOPIC_EXCHANGE, "item.update", null, message.getBytes());
        System.out.println("已发送消息：" + message);

        message = "商品删除  通配符模式  routing key为 item.delete";
        channel.basicPublish(TOPIC_EXCHANGE, "item.delete", null, message.getBytes());
        System.out.println("已发送消息：" + message);

        // 6.  关闭资源
        channel.close();
        connection.close();
    }
}
