package com.atspring.springpro.search.Thread;

import org.elasticsearch.common.recycler.Recycler;

import java.util.concurrent.*;

public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main---start--");
//        CompletableFuture.runAsync(()->{
//
//        },executor);
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果" + i);
//            return i;
//        }, executor);
//        Integer integer = future.get();
//        System.out.println("main---end---");

        /*
        线程串行化 A->B：A完成以后才能做B
        1) thenRun; 不能获取到上一步的执行结果，无返回值
        2)thenAcceptAsync能接受上一步结果，但是无返回值
        3)thenApplyAsyncn能接受上一步结果，有返回值
         */
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, executor).thenApplyAsync(res->{

            return res;
        },executor);

        /*
        两任务组合，都完成 A,B都完成再来执行C
        runAfterBothAsync不能感知前两个线程的返回结果
        thenAcceptBothAsync可以感知之前两个任务的返回结果
        thenCombineAsync既能感知前两个任务的返回值，也能自己返回当前结果
         */
        CompletableFuture<Integer> future01 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, executor);

        CompletableFuture<Integer> future02 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果" + i);
            return i;
        }, executor);

//        future01.runAfterBothAsync(future02,()->{
//            System.out.println("任务3开始");
//        },executor);
        future01.thenCombineAsync(future02,(f1,f2)->{
            return f1+":"+f2;
        },executor);

        /*
        两个任务，只要有一个完成，我们就执行任务3
        1）runAfterEitherAsync
        2)acceptEitherAsync 感知结果，自己没有返回值
        3)applyToEitherAsync 感知结果，自己有返回值
         */

        future01.acceptEitherAsync(future02,(res)->{
            System.out.println("h");
        },executor);

        /*
        多任务组合
        allOf：所有任务都完成
        anyOf，只要有一个完成

         */
    }

    public void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main---start--");
//        Thread01 thread = new Thread01();
//        thread.start();
//
//        Runnable01 runnable01 = new Runnable01();
//        new Thread(runnable01).start();

//        FutureTask<Integer> futureTask = new FutureTask<>(new Callbale01());
//        new Thread(futureTask).start();
//
//        //阻塞等待整个线程执行完成，获取返回结果
//        Integer integer = futureTask.get();
        /*
        七大参数
        corePoolSize:[5] 核心线程数（一直存在，除非allowCoreThreadTimeOut）；线程池创建好以后就准备就绪的线程数量，就等待接受异步任务去执行
        maximumPoolSize 最大线程数量，控制资源
        keppAliveTime:存活时间.如果当前正在运行的线程数量大于核心数量，释放空闲的线程(maxPoolsize-corePoolSize)，主要线程空闲大于指定的keepAliveTime
        unit；时间单位
        BlockingQueue<Runnable> workQueue；阻塞队列，如果任务有很多，就会将目前多的任务放在队列里面。只要有线程空闲，就会去队列里面取出新的任务继续执行
        threadFactory:线程创建的工厂
        RejectExecuteionHandler handler:如果队列满了，按照我们指定的拒绝策略拒绝执行任务
         */
        //service.execute(new Runnable01());

        //new LinkedBlockingDeque<>(), 默认是Integer的最大值，但可能导致内存不够
        new ThreadPoolExecutor(5,200,10,TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(10000), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());


        System.out.println("main---end---");
    }

    public static class Thread01 extends Thread{
        @Override
        public void run(){
            System.out.println("当前线程"+Thread.currentThread().getId());
            int i= 10 /2;
            System.out.println("运行结果"+i);
        }
    }

    public static class Runnable01 implements Runnable{

        @Override
        public void run() {
            System.out.println("当前线程"+Thread.currentThread().getId());
            int i= 10 /2;
            System.out.println("运行结果"+i);
        }
    }

    public static class Callbale01 implements Callable<Integer>{

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程"+Thread.currentThread().getId());
            int i= 10 /2;
            System.out.println("运行结果"+i);
            return i;
        }
    }
}
