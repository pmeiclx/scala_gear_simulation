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


class GearController(nGears: ListBuffer[Gear], guiActor: Actor) extends Actor {
  private var syncGears = new ListBuffer[Int]
  private var syncSpeed = 0

  guiActor.start()
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
          if (syncGears.length == nGears.length) {
            println("[Controller] all gears are back in town!")
            //System.exit(0)
          }

          /**
           * Notify the gui
           */
          guiActor ! ReceivedSpeed(gearId)
          guiActor ! Progress (syncGears.length)
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
}