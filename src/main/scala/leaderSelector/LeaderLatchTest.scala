package leaderSelector

import java.util.UUID.randomUUID

import org.apache.curator.framework.recipes.leader.{LeaderLatch, LeaderLatchListener, LeaderSelector, LeaderSelectorListenerAdapter}
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry


class LeaderLatchTest(uuid: String, zookeeperUrl: String) {
  val client: CuratorFramework = CuratorFrameworkFactory
    .newClient(zookeeperUrl, new ExponentialBackoffRetry(1000,3))

  val PATH = "/dishbreak"

   val leaderLatch: LeaderLatch = new LeaderLatch(client, PATH, "client#")

  leaderLatch.addListener(new LeaderLatchListener {
    override def isLeader: Unit = {
      println("I am the leader ....... and begin doing the job........")
      while(true){printMain.pt()}
    }

    override def notLeader(): Unit = {
      println("I am not the leader.....")
    }
  })

  def start(): Unit = {

    client.start()
    client.create.creatingParentContainersIfNeeded.forPath("/dishbreak" + uuid, "init".getBytes())
    leaderLatch.start()

    println("Connected......!")
  }
}

object LeaderLatchTest {
  private val zookeeperUrl = "localhost:2181"//System.getProperty("zookeeper.url")

  def main(args: Array[String]): Unit = {
    val uniqueId = randomUUID().toString
    println(s"I am instance $uniqueId")
    val elector = new LeaderLatchTest(uniqueId, zookeeperUrl)
    elector.start()
    while (true) {
      Thread.sleep(1000)
    }
  }
}


