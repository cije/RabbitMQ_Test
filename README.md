## 1. 消息队列概述

-   消息队列：
    -   消息队列是应用程序之间的通信方法
    -   无需即时返回且耗时的操作进行异步处理从而提高系统的吞吐量
    -   可以实现程序之间的解耦合
-   实现方式：AMQP、JMS
-   常见产品：`ActiveMQ`、`ZeroMQ`、`RabitMQ`、`RocketMQ`、`Kafka`

## 2. 安装`RabitMQ`

-   docker安装：
    -   拉取镜像
        -   `docker pull rabbitmq`
    -   创建并启动容器
        -   `docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 --hostname myRabbit -e RABBITMQ_DEFAULT_VHOST=my_vhost -
            e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin rabbitmq`
            -   -d 后台运行容器
            -   --name 指定容器名
            -   -p 指定服务运行的端口（5672：应用访问端口；15672：控制台Web端口号）
            -   -v 映射目录或文件
            -   --hostname 主机名（RabbitMQ的一个重要注意事项是它根据所谓的 “节点名称” 存储数据，默认为主机名）
            -   -e 指定环境变量
                -   RABBITMQ_DEFAULT_VHOST：默认虚拟机名
                -   RABBITMQ_DEFAULT_USER：默认的用户名
                -   RABBITMQ_DEFAULT_PASS：默认用户名的密码
    -   启动`rabbitmq_management`
        -   `docker exec -it rabbit rabbitmq-plugins enable rabbitmq_management`
    -   浏览器打开web管理端：http://ip:15672
-   windows安装
    1.  安装erlang
    2.  配置erlang环境变量 `ERLANG_HOME`  `Path`
    3.  安装`RabbitMQ`
    4.  配置插件
        -   `rabbitmq-plugins.bat enable rabbitmq_management`
    5.  浏览器打开web管理端：http://ip:15672
        -   默认 guest guest

## 3. RabbitMQ入门工程

1.  搭建工程

    idea新建maven工程，导入依赖：

    -   ```xml
        <dependency>
        	<groupId>com.rabbitmq</groupId>
        	<artifactId>amqp-client</artifactId>
        	<version>5.9.0</version>
        </dependency>
        ```

2.  生产者：

    -   编写消息生产者代码，发送消息到队列

    -   分析：

        -   生产者发送消息到RabbitMQ的队列（simple_queue）。
        -   消费者可以从队列中获取消息。
        -   可以使用RabbitMQ的简单队列模式（simple）。

    -   步骤：

        1.  创建连接工厂（设置RabbitMQ的连接参数）
        2.  创建连接
        3.  创建频道
        4.  声明队列
        5.  发送消息
        6.  关闭资源

    -   代码：

        ```java
        public class Producer {
        
            static final String QUEUE_NAME = "simple_queue";
        
            public static void main(String[] args) throws IOException, TimeoutException {
                // 1.  创建连接工厂（设置RabbitMQ的连接参数）
                ConnectionFactory connectionFactory = new ConnectionFactory();
                // 主机：默认localhost
                connectionFactory.setHost("localhost");
                // 连接端口：默认5672
                connectionFactory.setPort(5672);
                // 虚拟主机：默认/
                connectionFactory.setVirtualHost("my_vhost");
                // 用户名：默认guest
                connectionFactory.setUsername("admin");
                // 密码：默认guest
                connectionFactory.setPassword("admin");
        
                // 2.  创建连接
                Connection connection = connectionFactory.newConnection();
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
                // 5.  发送消息
                String message = "你好，RabbitMQ！";
        
                /*
                 * 参数1：交换机名称，如果没有则指定空字符串，表示使用默认的交换机
                 * 参数2：路由key，简单模式中可以使用队列名称
                 * 参数3：消息其他属性
                 * 参数4：消息内容
                 */
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
                System.out.println("已发送消息：" + message);
                // 6.  关闭资源
                channel.close();
                connection.close();
            }
        }
        ```

3.  消费者

    -   编写消息消费者代码，从队列中接受消息并消费

    -   分析：

        -   从RabbitMQ中队列（与生产者发送消息时的队列一致）接收消息

    -   步骤：

        1.  创建连接工厂
        2.  创建连接（抽取一个获取连接的工具类）
        3.  创建频道
        4.  声明队列
        5.  创建消费者（接收消息并处理消息）
        6.  监听队列（需要持续监听队列，所以不关闭资源）

    -   代码：

        ```xml
        public class Consumer {
        
            public static void main(String[] args) throws IOException, TimeoutException {
                // 1.  创建连接工厂
                // 2.  创建连接（抽取一个获取连接的工具类）
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
                channel.queueDeclare(Producer.QUEUE_NAME, true, false, false, null);
                // 5.  创建消费者（接收消息并处理消息）
                DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        // 路由key
                        System.out.println("路由key：" + envelope.getRoutingKey());
                        // 交换机
                        System.out.println("交换机：" + envelope.getExchange());
                        // 消息id
                        System.out.println("消息idy：" + envelope.getDeliveryTag());
                        // 接收到的消息
                        System.out.println("接收到的消息：" + new String(body, StandardCharsets.UTF_8));
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
                channel.basicConsume(Producer.QUEUE_NAME, true, defaultConsumer);
            }
        }
        ```

4.  测试

    -   启动消费者和生产者，到RabbitMQ中查询队列并在消费者端IDEA控制台查看接收到的消息。
    -   分析：
        -   生产者：发送消息到RabbitMQ队列(simple_queue)
        -   消费者：接收RabbitMQ队列消息
    -   小结：
        -   简单模式：生产者发送消息到队列中，一个消费者从队列中接收消息。
        -   在RabbitMQ中消费者只能从队列接收消息。

## 4. Work queues工作队列模式

-   工作队列模式：在同一个队列中有多个消费者，消费者之间对于消息的接收是竞争关系。

-   一个消息只能被一个消费者接收

-   测试：

    -   生产者：发送30个消息
    -   消费者：创建两个消费者监听同一个队列，查看两个消费者的接收消息是否存在重复

-   代码：

    -   生产者：

        ```java
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
        ```

    -   消费者1

        ```java
        public class Consumer1 {
        
            public static void main(String[] args) throws IOException, TimeoutException {
                // 1.  创建连接工厂
                // 2.  创建连接（抽取一个获取连接的工具类）
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
                channel.queueDeclare(Producer.QUEUE_NAME, true, false, false, null);
        
                // 每次可以预取多少个消息
                channel.basicQos(1);
        
                // 5.  创建消费者（接收消息并处理消息）
                DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        try {
                            // 路由key
                            System.out.println("路由key：" + envelope.getRoutingKey());
                            // 交换机
                            System.out.println("交换机：" + envelope.getExchange());
                            // 消息id
                            System.out.println("消息idy：" + envelope.getDeliveryTag());
                            // 接收到的消息
                            System.out.println("Consumer1 接收到的消息：" + new String(body, StandardCharsets.UTF_8));
        
                            Thread.sleep(1000);
        
                            // 确认消息
                            /**
                             * 参数1：消息id
                             * 参数2：是否确认，false表示只有当前此条被处理
                             */
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                channel.basicConsume(Producer.QUEUE_NAME, true, defaultConsumer);
            }
        }
        ```

    -   消费者2

        ```java
        public class Consumer2 {
        
            public static void main(String[] args) throws IOException, TimeoutException {
                // 1.  创建连接工厂
                // 2.  创建连接（抽取一个获取连接的工具类）
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
                channel.queueDeclare(Producer.QUEUE_NAME, true, false, false, null);
        
                // 每次可以预取多少个消息
                channel.basicQos(1);
        
                // 5.  创建消费者（接收消息并处理消息）
                DefaultConsumer defaultConsumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        try {
                            // 路由key
                            System.out.println("路由key：" + envelope.getRoutingKey());
                            // 交换机
                            System.out.println("交换机：" + envelope.getExchange());
                            // 消息id
                            System.out.println("消息idy：" + envelope.getDeliveryTag());
                            // 接收到的消息
                            System.out.println("Consumer2 接收到的消息：" + new String(body, StandardCharsets.UTF_8));
        
                            Thread.sleep(1000);
        
                            // 确认消息
                            /**
                             * 参数1：消息id
                             * 参数2：是否确认，false表示只有当前此条被处理
                             */
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
                channel.basicConsume(Producer.QUEUE_NAME, true, defaultConsumer);
            }
        }
        ```

-   应用场景：可以在消费者端处理任务比较耗时的时候；添加对同一个队列的消费者来提高任务处理能力。

## 5. Exchange交换机：

-   接收生产者发送的消息并决定如何投递消息到其绑定的队列。
-   消息投递决定于交换机的类型：
    -   广播（fanout）
    -   定向（direct）
    -   通配符（topic）
-   交换机只做消息转发，自身不存储数据

## 6. Publish/Subscribe发布与订阅模式

-   特点：一个消息可以被多个消费者接收，其实是使用了订阅模式，交换机模式为（fanout）广播
-   生产者（发送10个消息）
    1.  创建连接
    2.  创建频道
    3.  声明交换机（fanout）
    4.  声明队列
    5.  队列绑定到交换机
    6.  发送消息
    7.  关闭资源
-   消费者（至少两个消费者）
    1.  创建链接
    2.  创建频道
    3.  声明交换机
    4.  声明队列
    5.  队列绑定到交换机
    6.  创建消费者
    7.  监听队列

## 7. Routing路由模式

-   队列绑定到交换机的时候指定路由key
-   消息发送时需要携带路由key
-   只有消息的路由key与队列路由key完全一致时才能让该队列接收到消息。
-   分析：
    -   生产者：发送两条消息（路由key分别为insert和update）
    -   消费者：创建两个消费者，监听的队列分别绑定路由key为：insert、update
        -   消息中路由key为insert的会被绑定路由key为insert的队列接收并被其监听的消费者接收、处理
        -   消息中路由key为update的会被绑定路由key为update的队列接收并被其监听的消费者接收、处理

## 8. Topic通配符模式

-   可以根据路由key将消息传递到对应路由key的队列
-   队列绑定到交换机的路由可以有多个
-   通配符模式中路由key可以使用 `*`和`#`
    -   `*`：匹配一个单词
    -   `#`：匹配多个单词

-   分析：
    -   生产者：发送包含有`item.insert`、`item.update`、`item.delete`的3中路由key消息
    -   消费者1：监听的队列绑定到交换机的路由key为`item.update`、`item.delete`
    -   消费者2：监听的队列绑定到交换机的路由key为`item.*`

## 9. RabbitMQ模式总结

-   不使用Exchange交换机（默认交换机）
    1.  simple简单模式：一个生产者生产一个消息到一个队列被一个消费者接收
    2.  work工作队列模式：生产者发送消息到一个队列中，然后可以被多个消费者监听该队列，一个消息只能被一个消费者接收，消费者之间是竞争关系
-   使用Exchange交换机：订阅模式（交换机：广播fanout、定向direct、通配符topic）
    1.  发布与订阅模式：使用广播fanout类型的交换机，可以将一个消息发送到所有绑定到该该交换机的队列
    2.  路由模式：使用direct定向类型的交换机，消息会携带路由key，交换机根据消息的路由key与队列的路由key进行对比，一致的话那么该队列可以接收到消息。
    3.  通配符模式：使用topic通配符类型的交换机，消息会携带路由key（`*`，`#`），交换机根据消息的路由key与队列的路由key进行对比，一致的话那么该队列可以接收到消息。

## 10. `SpringBoot`整合RabbitMQ

-   `SpringBoot`提供了对于AMQP的整合`spring-boot-starter-amqp`；可以使用`RabbitTemplat`e发送消息；可以使用`@RabbitListener`注解接收消息。

-   生产者工程`springboot-rabbitmq-producer`：发送消息

    -   使用通配符模式：将队列绑定到交换机（topic）时需要指定路由key（item.#)

        1.  配置RabbitMQ的连接参数：主机、连接参数、虚拟主机、用户名、密码；

            -   配置`application.yml`文件

                ```yml
                spring:
                  rabbitmq:
                    host: localhost
                    port: 5672
                    virtual-host: my_vhost
                    username: admin
                    password: admin
                ```

        2.  声明交换机、队列并将队列绑定到交换机，指定路由key（item.#)

            -   配置类

                ```xml
                import org.springframework.amqp.core.*;
                import org.springframework.beans.factory.annotation.Qualifier;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                
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
                ```

                

-   消费者工程`springboot-rabbitmq-consumer`：接收消息

    1.  配置`application.yml`文件，设置RabbitMQ的连接参数

    2.  编写消息监听器接收队列（item_queue）消息;可以使用注解`@RabbitListener`接收队列消息

        -   ```java
            import org.springframework.amqp.rabbit.annotation.RabbitListener;
            import org.springframework.stereotype.Component;
            
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
            ```

-   测试消息发送和接收

    -   生产者：编写测试类`RabbitMQTest`，利用`RabbitTemplate`发送3条消息，路由key分别为`item.insert`、`item.update`、`item.delete`

    ```java
        @SpringBootTest
        @ExtendWith(SpringExtension.class)
        public class RabbitMQTest {
            @Autowired
            private RabbitTemplate rabbitTemplate;
        
            @Test
            public void test() {
                rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE,
                        "item.insert", "商品新增，路由key为 item.insert");
                rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE,
                        "item.update", "商品修改，路由key为 item.update");
                rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE,
                        "item.delete", "商品删除，路由key为 item.delete");
                rabbitTemplate.convertAndSend(RabbitMQConfig.ITEM_TOPIC_EXCHANGE,
                        "a.item.delete", "商品删除，路由key为 a.item.delete");
            }
        
        }
    ```

    -   消费者：在IDEA控制台查看是否能接收到符合路由key的消息

    
