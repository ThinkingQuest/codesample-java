package net.thinkingquest.future;

import java.util.Random;
import java.util.concurrent.*;

/**
 *
 * Future和FutureTask用于执行异步任务。
 * FutureTask 也实现了 Future 接口。
 * 常见两种用法如test1()和test2()。
 *
 * Created by libei on 2017/5/24.
 */
public class FutureTest {

    public static void main(String[] args) {
//        test1();
        test2();
    }

    private static void test1() {
        FutureTask<Integer> future = new FutureTask(new Callable() {
            public Object call() throws Exception {
                return doStaff();
            }
        });

        new Thread(future).start();
        doOtherStaff();

        try {
            int result = future.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void test2() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        Future<Integer> future = threadPool.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return doStaff();
            }
        });

        doOtherStaff();

        try {
            int result = future.get();
            System.out.println(result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static int doStaff() {
        try {
            Thread.sleep(3000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new Random().nextInt();
    }

    private static void doOtherStaff() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Other staff done.");
    }
}
