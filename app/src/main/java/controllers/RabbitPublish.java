package controllers;

import com.rabbitmq.client.Channel;

import java.util.concurrent.BlockingDeque;

/**
 * Created by dmt on 01.09.2015.
 */
public class RabbitPublish implements Runnable {

    private BlockingDeque<String> queue;
    private String routingKey;

    public RabbitPublish(String routingKey, BlockingDeque<String> queue) {
        this.queue = queue;
        this.routingKey = routingKey;
    }

    @Override
    public void run() {
        while(true) {
            Channel ch = null;
            try {
                ch = RabbitConnection.getInstance().newChanel();
                ch.confirmSelect();

                while (true) {
                    String message = queue.takeFirst();
                    try{
                        ch.basicPublish("amq.direct", routingKey, null, message.getBytes());
                        ch.waitForConfirmsOrDie();
                    } catch (Exception e){
                        queue.putFirst(message);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(5000); //sleep and then try again
                } catch (InterruptedException e1) {
                    break;
                }
            }
            finally {
                RabbitConnection.getInstance().closeChanel(ch);
            }
        }
    }
}
