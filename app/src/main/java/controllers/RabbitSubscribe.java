package controllers;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Created by dmt on 01.09.2015.
 */
public class RabbitSubscribe implements Runnable {
    private String routingKey;
    private Handler handler;

    public RabbitSubscribe(String routingKey, Handler handler) {
        this.routingKey = routingKey;
        this.handler = handler;
    }

    @Override
    public void run() {
        while(true) {
            Channel ch = null;
            try {
                ch = RabbitConnection.getInstance().newChanel();
                ch.basicQos(1);
                AMQP.Queue.DeclareOk q = ch.queueDeclare();
                ch.queueBind(q.getQueue(), "amq.direct", routingKey);
                QueueingConsumer consumer = new QueueingConsumer(ch);
                ch.basicConsume(q.getQueue(), true, consumer);

                // Process deliveries
                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    String message = new String(delivery.getBody());

                    Message msg = handler.obtainMessage();
                    Bundle bundle = new Bundle();

                    bundle.putString("msg", message);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e1) {
                try {
                    Thread.sleep(4000); //sleep and then try again
                } catch (InterruptedException e) {
                    break;
                }
            }
            finally {
                RabbitConnection.getInstance().closeChanel(ch);
            }
        }
    }
}
