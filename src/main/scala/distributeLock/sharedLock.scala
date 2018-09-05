package distributeLock

import java.util
import java.util.concurrent.TimeUnit

import org.apache.commons.lang.math.RandomUtils
import org.apache.curator.framework.recipes.locks.{InterProcessLock, InterProcessMutex, InterProcessReadWriteLock}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.RetryNTimes

import scala.collection.mutable.ArrayBuffer

class sharedLock {

  var client: CuratorFramework = null
  val connectionTimeoutMs = 5000
  val connectString = "localhost:2181"
  val defaultData = "默认数据".getBytes
  val maxCloseWaitMs = 5000
  val namespace = "yclock"


  /**
    * 获取zookeeper连接
    *
    * @return
    */
  private def getClient(): CuratorFramework = {
    if (client == null) {

      val retryPolicy = new RetryNTimes(Integer.MAX_VALUE, 5000)

      val clientinit = CuratorFrameworkFactory.builder.connectionTimeoutMs(connectionTimeoutMs)
        .connectString(connectString).defaultData(defaultData).maxCloseWaitMs(maxCloseWaitMs)
        .namespace(namespace).retryPolicy(retryPolicy).build

      clientinit.start()
      client = clientinit
    }
    return client
  }


  /**
    *
    * @param sharedlock
    */
  def sharedLock(sharedlock: String = "/shlock") = {
    val client = getClient();
    val sharedlock = new InterProcessMutex(client, "/sharedlock")

    try {
      if (sharedlock.acquire(10, TimeUnit.SECONDS)) {

        System.out.println(Thread.currentThread().getName() + "  is  get the shared lock")

        for (i <- 1 to 5) {
          leaderSelector.printMain.pt()
          Thread.sleep(10)
          System.out.println(Thread.currentThread.getName + "  operator " + i)
        }

        System.out.println(Thread.currentThread.getName + "  is  release the shared lock")

      }

    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    } finally {
      try {
        System.out.println(Thread.currentThread.getName + "  the flag is " + sharedlock.isAcquiredInThisProcess)
        if (sharedlock.isAcquiredInThisProcess) { //判断是否持有锁 进而进行锁是否释放的操作
          sharedlock.release()
        }
      } catch {
        case e: Exception => {
          e.printStackTrace()
        }
      }
    }

  }

  /**
    * @return void
    *
    * 测试读写锁
    */
  private def ReadWriterLock(): InterProcessReadWriteLock = { //创建多线程 循环进行锁的操作
    val client: CuratorFramework = getClient
    val readWriteLock: InterProcessReadWriteLock = new InterProcessReadWriteLock(client, "/readwriter")
    return readWriteLock

  }

  def readWriterLock(lock: InterProcessLock) {
    System.out.println(Thread.currentThread().getName() + "   进入任务 " + System.currentTimeMillis())
    try {

      if (lock.acquire(1, TimeUnit.SECONDS)) { //System.err.println(Thread.currentThread().getName() + " 等待超时  无法获取到锁");
        //执行任务 --读取 或者写入
        val time = RandomUtils.nextInt(1500)
        System.out.println(Thread.currentThread.getName + "   执行任务开始")
        Thread.sleep(time)
        System.out.println(Thread.currentThread.getName + "   执行任务完毕")
      }
      else
        System.err.println(Thread.currentThread.getName + " 等待超时  无法获取到锁")

    } catch {
      case e: Exception => {
        e.printStackTrace()
      }

    } finally {

      try {
        if (lock.isAcquiredInThisProcess) lock.release()
      } catch {
        case e: Exception => {

        }
      }

    }


  }


}

object sharedLock {

  def main(args: Array[String]): Unit = {
    val s = new sharedLock()


    //共享锁测试
    /*

        val runnable = new Runnable() {
          override def run(): Unit = {
            try {
              s.sharedLock()
              System.out.println(10000)
              Thread.sleep(1000)
              System.out.println(Thread.currentThread.getName + "正在运行")
            } catch {
              case e: InterruptedException =>
                e.printStackTrace()
            }
          }
        }

        for(i<-1 to 20){
          val t = new Thread(runnable)
          t.start()
        }*/

    //共享读写锁测试

    val readLock = s.ReadWriterLock().readLock
    val writeLock = s.ReadWriterLock().writeLock

    var jobs = new ArrayBuffer[Thread]()

    for (i <- 1 to 20) {
      val t = new Thread("wirite lock " + i) {
        override def run(): Unit = {
          s.readWriterLock(writeLock)
        }
      }
      jobs.+=(t)
    }

    for (i <- 1 to 20) {
      val t: Thread = new Thread("read lock " + i) {
        override def run(): Unit = {
          s.readWriterLock(readLock)
        }
      }
      jobs.+=(t)
    }


    for (thread <- jobs) {
      thread.start()
    }
  }

}
