package com.ce.rabbitmq.topic;

import com.ce.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * 路由模式：消费者接收消息
 */
public class Consumer2 {

    public static void main(String[] args) throws IOException, TimeoutException {
        // 1.  创建连接（抽取一个获取连接的工具类）
        Connection connection = ConnectionUtil.getConnection();
        // 2.  创建频道
        Channel channel = connection.createChannel();
        // 3.声明交换机
        channel.exchangeDeclare(Producer.TOPIC_EXCHANGE, BuiltinExchangeType.TOPIC);

        // 4.  声明队列
        /*
         *  参数1： 队列名称
         *  参数2：是否定义持久化队列（消息会持久化保存在服务器上）
         *  参数3：是否独占本连接
         *  参数4：是否在不使用的时候队列自动删除
         *  参数5：其他参数
         */
        channel.queueDeclare(Producer.TOPIC_QUEUE_2, true, false, false, null);
        // 5.队列绑定在交换机上
        channel.queueBind(Producer.TOPIC_QUEUE_2, Producer.TOPIC_EXCHANGE, "item.*");
        // 5.  创建消费者（接收消息并处理消息）
        DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // 路由key
                System.out.println("路由key：" + envelope.getRoutingKey());
                // 交换机
                System.out.println("交换机：" + envelope.getExchange());
                // 消息id
                System.out.println("消息id：" + envelope.getDeliveryTag());
                // 接收到的消息
                System.out.println("消费者2接收到的消息：" + new String(body, StandardCharsets.UTF_8));
            }
        };
        // 6.  监听队列
        /*
         * 参数1：队列名
         * 参数2：是否自动确认，
         *      如true表示消息接收到自动向MQ回复接收到了，MQ会将消息从队列中删除
         *      false则需要手动确认
         * 参数3：消息的消费者
         */
        channel.basicConsume(Producer.TOPIC_QUEUE_2, true, defaultConsumer);
    }
}
