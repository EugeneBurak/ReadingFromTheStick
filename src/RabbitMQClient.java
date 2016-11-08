import com.rabbitmq.client.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeoutException;

class RabbitMQClient {

    private final static String QUEUE_COMMAND_NAME = "php";
    private final static String QUEUE_ANSWER_NAME = "java";

    private Channel channel;
    private Consumer consumer;

    private static RabbitMQClient rabbitMQClient = null;

    private RabbitMQClient() throws IOException, TimeoutException {

        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setHost("localhost");
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("java");
        connectionFactory.setPassword("fsrtKf4D5Algb");

        Connection connection = connectionFactory.newConnection();

        channel = connection.createChannel();
        channel.queueDeclare(QUEUE_COMMAND_NAME,true,false,false,null).getQueue();
        channel.queueDeclare(QUEUE_ANSWER_NAME,true,false,false,null).getQueue();

        channel.basicQos(1);

        getConsumer();

        channel.basicConsume(QUEUE_COMMAND_NAME, true, consumer);
    }

    private void getConsumer(){
        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                //switch case Commands list
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                JsonReader jsonReader = Json.createReader(new StringReader(message));
                JsonObject command = jsonReader.readObject();
                jsonReader.close();
                System.out.println(" [x] Received JSON '" + command.toString() + "'");
                try {
                    ReadingFromTheStick.receivingMessage(command);
                }
                catch (Exception e){
                    getConsumer();
                }
            }
        };
    }

    static RabbitMQClient getInstance() throws IOException, TimeoutException {
        if(rabbitMQClient==null){
            rabbitMQClient =  new RabbitMQClient();
            return rabbitMQClient;
        }
        else
            return rabbitMQClient;
    }

    void sendMessage(String message){            //work messages
        try {
            channel.basicPublish("", QUEUE_ANSWER_NAME, null, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(" [x] Sent '" + message + "'");
    }
}
