package ch.clx.geargui


/**
 * Created by IntelliJ IDEA.
 * User: pmei
 * Date: 11.02.2010
 * Time: 15:20:44
 * Package: ch.clx.geargui
 * Class: Message
 */
// test commit

import actors.Actor
import collection.mutable.ListBuffer

abstract class Message

//gear API
case object StartSync extends Message
case class SyncGear(controller: GearController, syncSpeed: Int) extends Message
case class Interrupt(toSpeed: Int) extends Message

//controller API
case class CurrentSpeed(gearId: Int, speed: Int) extends Message  //Used in GUI too
case class ReceivedSpeed(gearId: Int) extends Message //Used in GUI too
case class ReportInterrupt(gearId: Int) extends Message

//GUI API
case class Progress(numberOfSyncGears: Int) extends Message
case class SetCalculatedSyncSpeed(syncSpeed: Int) extends Message
case class GearProblem(gearId: Int) extends Message

//saboteur API
case class Sabotage(nGears: ListBuffer[Gear]) extends Message
case class SabotageManual(gear: Gear, toSpeed: Int) extends Message