package controllers;

import android.os.Handler;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by dmt on 01.09.2015.
 * https://github.com/cloudamqp/android-example/blob/master/cloudAMQP/src/com/cloudamqp/androidexample/ActivityHome.java
 */
public class RabbitController {
    private BlockingDeque<String> queue = new LinkedBlockingDeque<>();

    private Thread subscribeThread;
    private Thread publishThread;

    public RabbitController() { }

    public void buildSubscribe(String chanel, Handler handler) {
        subscribeThread = new Thread(new RabbitSubscribe(chanel, handler));
        subscribeThread.start();
    }

    public void buildPublish(String chanel) {
        publishThread = new Thread(new RabbitPublish(chanel, queue));
        publishThread.start();
    }

    public void publish(String message) {
        //Adds a message to internal blocking queue
        try {
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void destroy() {
        if (subscribeThread != null) {
            subscribeThread.interrupt();
        }
        if(publishThread != null) {
            publishThread.interrupt();
        }
    }
}
