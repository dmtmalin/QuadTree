package controllers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dmt on 04.09.2015.
 */
public class RabbitConnection {
    private static RabbitConnection ourInstance = new RabbitConnection();

    public static RabbitConnection getInstance() {
        return ourInstance;
    }

    private Connection connection;

    private RabbitConnection() {
        String uri = "amqp://root:root@178.62.233.195/%2f";
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setAutomaticRecoveryEnabled(false);
            factory.setUri(uri);
            connection = factory.newConnection();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Channel newChanel() throws Exception{
        return connection.createChannel();
    }

    public void closeChanel(Channel ch) {
        try {
            if(ch != null)
                ch.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        try{
            //release resources here
        }catch(Throwable t){
            throw t;
        }finally{
            if (connection != null && connection.isOpen())
                connection.close();
            super.finalize();
        }
    }

}
