package ch.clx.geargui

/**
 * Created by IntelliJ IDEA.
 * User: pmei
 * Date: 11.02.2010
 * Time: 15:23:29
 * Package: ch.clx.geargui
 * Class: GearController
 */
import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer
import scala.math.{ max, min }


class GearController(nGears: ListBuffer[Gear], guiActor: Actor, aName: String) extends Actor {
  private var syncGears = new ListBuffer[Int]
  private var syncSpeed = 0

  val schedulerName = aName
  val rt = Runtime.getRuntime
  val startTime = System.nanoTime
  var maxThreads = totalThreads
  var minFree = rt.freeMemory
  var maxTotal = rt.totalMemory

  def act = {
    while (true) {
      receive {
        case StartSync => {
          println("[Controller] Send commands for syncing to gears!")
          var speeds = new ListBuffer[Int]
          nGears.foreach(e => speeds += e.speed)

          //Calc avg
          syncSpeed = (0 /: speeds)(_ + _) / speeds.length //Average over all gear speeds
          guiActor ! SetCalculatedSyncSpeed(syncSpeed)

          println("[Controller] calculated syncSpeed: " + syncSpeed)
          nGears.foreach {
            e =>
              e.start()
              e ! SyncGear(this, syncSpeed)
          }
          println("[Controller] started all gears")
        }
        case ReceivedSpeed(gearId: Int) => {
          println("[Controller] Syncspeed received by a gear (" + gearId + ")")
          syncGears += gearId

          /**
           * Notify the gui
           */
          guiActor ! ReceivedSpeed(gearId)
          guiActor ! Progress (syncGears.length)

          if (syncGears.length == nGears.length) {
            println("[Controller] all gears are back in town!")


        //a simple micro benchmark inspired by:
        //https://bitbucket.org/eengbrec/managedforkjoinpool/src/tip/src/main/scala/actorbench/TestHarness.scala
        maxThreads = max(maxThreads, totalThreads)
        minFree = min(minFree, rt.freeMemory)
        maxTotal = max(maxTotal, rt.totalMemory)
        printResults(startTime, System.nanoTime, maxThreads, minFree, maxTotal)
        }
        }

        case CurrentSpeed(gearId: Int, speed: Int) => {
          println("[GearController] gear("+gearId+") currentSpeed: "+speed)

          /**
           * Only forward the message to the gui
           * Controller isn't really interessed
           */
          guiActor ! CurrentSpeed(gearId, speed)
        }

        case ReportInterrupt(gearId: Int) => {
          var gear = nGears.filter {s => s.gearId == gearId}.first
          if (syncGears.contains(gearId)) {
            syncGears -= gearId;
            gear.start()
            gear ! SyncGear(this, syncSpeed)
          }

          /**
           * Notify the gui
           */
          guiActor ! GearProblem(gear.gearId)
          guiActor ! Progress (syncGears.length)
        }
        case _ => println("[Controller] No match :(")
      }
    }
  }

  protected def printResults(startTime: Long, endTime: Long, maxThreads: Int, minFree: Long, maxTotal: Long) {
    import scala.util.Properties
    def pf(s: String) = println(s)
    val props = System.getProperties
    def pp(name: String) = pf(name + ": " + props.getProperty(name))

    pf("****Microbenchmark****")
    pf("Scheduler: " + schedulerName)
    pf("# of Cores: " + rt.availableProcessors.toString)
    pp("java.vm.name")                      // e.g. Java HotSpot(TM) 64-Bit Server VM
    pp("java.vm.version")                   // e.g. 16.3-b01-279
    pp("java.version")                      // e.g. 1.6.0_20
    pf("Scala Version: " + Properties.scalaPropOrElse("version.number", "unknown")) // e.g. 2.8.0.final
    pp("os.name")                           // e.g. Mac OS X
    pp("os.version")                        // e.g. 10.5.8
    pp("os.arch")                           // e.g. x86_64
    val wct = (endTime - startTime).asInstanceOf[Double] / (1000.0 * 1000.0 * 1000.0)
    pf("Time: " + wct)
    def bytes2megs(bytes: Long) = bytes.asInstanceOf[Double] / (1024.0 * 1024.0)
    pf("Max Threads: " + maxThreads)
    pf("Min free Mem: " + bytes2megs(minFree))
    pf("Max total Mem: " + bytes2megs(maxTotal))
    pf("****")
  }

  def totalThreads = {
    def rec(tg: ThreadGroup): Int = if (tg.getParent eq null) tg.activeCount else rec(tg.getParent)
    rec(currentThread.getThreadGroup)
  }


}