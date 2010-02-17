package ch.clx.geargui

/**
 * Created by IntelliJ IDEA.
 * User: pmei
 * Date: 12.02.2010
 * Time: 10:32:20
 * Package: ch.clx.geargui
 * Class: Saboteur
 */

import actors.Actor
import actors.Actor._
import collection.mutable.ListBuffer

class Saboteur extends Actor {

   def act = {
     while(true) {
       receive {
         case Sabotage(nGears: ListBuffer[Gear]) => {
           nGears.foreach{e =>
             e ! Interrupt(scala.util.Random.nextInt(1000))
             Thread.sleep(1000)
           }
         }
         case SabotageManual(gear: Gear, toSpeed: Int) => {
             gear ! Interrupt(toSpeed)
         }
       }
     }
   }
}