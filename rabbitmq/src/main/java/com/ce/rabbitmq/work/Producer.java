package com.ce.rabbitmq.work;

import com.ce.rabbitmq.util.ConnectionUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * work工作模式：发送消息
 */
public class Producer {

    static final String QUEUE_NAME = "work_queue";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 1.  创建连接工厂（设置RabbitMQ的连接参数）
        // 2.  创建连接
        Connection connection = ConnectionUtil.getConnection();
        // 3.  创建频道
        Channel channel = connection.createChannel();
        // 4.  声明队列
        /*
         *  参数1： 队列名称
         *  参数2：是否定义持久化队列（消息会持久化保存在服务器上）
         *  参数3：是否独占本连接
         *  参数4：是否在不使用的时候队列自动删除
         *  参数5：其他参数
         */
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        for (int i = 0; i < 30; i++) {
            // 5.  发送消息
            String message = "你好，RabbitMQ！work模式-" + i;

            /*
             * 参数1：交换机名称，如果没有则指定空字符串，表示使用默认的交换机
             * 参数2：路由key，简单模式中可以使用队列名称
             * 参数3：消息其他属性
             * 参数4：消息内容
             */
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
            System.out.println("已发送消息：" + message);
        }
        // 6.  关闭资源
        channel.close();
        connection.close();
    }
}
