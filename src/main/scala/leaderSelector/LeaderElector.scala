package leaderSelector

import java.util.UUID.randomUUID

import org.apache.curator.framework.recipes.leader.{LeaderSelector, LeaderSelectorListenerAdapter}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry

class LeaderElectorListener extends LeaderSelectorListenerAdapter {
  var running: Boolean = true

  val x = new printMain()

  override def takeLeadership(curatorFramework: CuratorFramework): Unit = {

    while (running) {
      Thread.sleep(1000)
      //这里放你的主函数
      x.pt()
    }
  }

  def close(): Unit = {
    println("Closing leader listener")
    this.running = false
  }
}

class LeaderElector(uuid: String, zookeeperUrl: String) {
  val curatorFramework: CuratorFramework = CuratorFrameworkFactory
    .newClient(zookeeperUrl, new ExponentialBackoffRetry(1000,3))

  val mutexPath = "/dishbreak"

  val listener: LeaderElectorListener = new LeaderElectorListener()

  val leaderSelector: LeaderSelector = new LeaderSelector(
    curatorFramework, mutexPath, listener
  )

  def start(): Unit = {

    curatorFramework.start()
    curatorFramework.create.creatingParentContainersIfNeeded.forPath("/dishbreak" + uuid, "init".getBytes())
    leaderSelector.start()

    println("Connected......!")
  }
}

object LeaderElector {
  private val zookeeperUrl = "localhost:2181"//System.getProperty("zookeeper.url")

  def main(args: Array[String]): Unit = {
    val uniqueId = randomUUID().toString
    println(s"I am instance $uniqueId")
    val elector = new LeaderElector(uniqueId, zookeeperUrl)
    elector.start()
    while (true) {
      Thread.sleep(1000)
    }
  }
}


