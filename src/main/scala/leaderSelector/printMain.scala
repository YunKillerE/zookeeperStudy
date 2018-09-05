package leaderSelector

class printMain {

  def pt(): Unit = {
    //while(true){
      println("I am the leader ============= "+System.currentTimeMillis/1000)
      Thread.sleep(3000)
    //}
  }

}


object printMain {

  def pt(): Unit = {
    //while(true){
    println("I am the leader ============= "+System.currentTimeMillis/1000)
    Thread.sleep(3000)
    //}
  }

}
