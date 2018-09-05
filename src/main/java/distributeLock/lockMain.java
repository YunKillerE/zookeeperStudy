package distributeLock;

public class lockMain {

    public static void main(String[] args) {
        final int n = 500;
        Runnable runnable = new Runnable() {
            public void run() {
                DistributedLock lock = null;
                try {
                    lock = new DistributedLock("127.0.0.1:2181", "lock");
                    lock.lock();
                    System.out.println(n);
                    Thread.sleep(100);
                    System.out.println(Thread.currentThread().getName() + "正在运行");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (lock != null) {
                        lock.unlock();
                    }
                }
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }

    }
}
