package ch.clx.geargui

import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer

/**
 * Created by IntelliJ IDEA.
 * User: pmei
 * Date: 11.02.2010
 * Time: 15:22:35
 * Package: ch.clx.geargui
 * Class: Gear
 */

class Gear(id: Int) extends Actor {
  private var mySpeed = scala.util.Random.nextInt(1000)
  private var myController: GearController = null

  def speed = mySpeed

  def gearId = id

  /* Constructor */
  println("[Gear (" + id + ")] created with speed: " + mySpeed)

  def act = {
    loop {
      react {
        case SyncGear(controller: GearController, syncSpeed: Int) => {
          Thread.sleep(50)
          //println("[Gear ("+id+")] activated, try to follow controller command (form mySpeed ("+mySpeed+") to syncspeed ("+syncSpeed+")")

          myController = controller          
          myController ! CurrentSpeed(gearId, mySpeed)
          adjustSpeedTo(syncSpeed)
        }
        case Interrupt(toSpeed: Int) => {
          println("[Gear ("+id+")] got interrupt: from "+mySpeed+" to " + toSpeed);
          mySpeed = toSpeed;
          myController ! ReportInterrupt(gearId)
        }
      }
    }
  }

  def adjustSpeedTo(targetSpeed: Int) = {
    if (targetSpeed > mySpeed) {
      mySpeed += 1
      self ! SyncGear(myController, targetSpeed)
    } else if (targetSpeed < mySpeed) {
      mySpeed -= 1
      self ! SyncGear(myController, targetSpeed)
    } else if (targetSpeed == mySpeed) {
      callController
    }
  }

  def callController = {
    println("[Gear (" + id + ")] has syncSpeed")
    myController ! ReceivedSpeed(gearId)
  }
}