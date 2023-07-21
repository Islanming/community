package com.example.community;


import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingTest {
    public static void main(String[] args) {
        //阻塞队列长度为10 ArrayBlockingQueue:用数组实现的阻塞队列
        BlockingQueue queue = new ArrayBlockingQueue(10);
        new Thread(new Product(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}

class Product implements Runnable{
    private BlockingQueue<Integer> queue;

    public Product(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(20);
                queue.put(i);
                System.out.println(Thread.currentThread().getName()+"生产："+queue.size());

            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}

class Consumer implements Runnable{
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue){
        this.queue = queue;
    }

    @Override
    public void run() {
        try{
            for (int i = 0; i < 100; i++) {
                Thread.sleep(new Random().nextInt(1000));
                queue.take();
                System.out.println(Thread.currentThread().getName()+"消费："+queue.size());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}