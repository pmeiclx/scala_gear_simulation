package ch.clx.geargui

/**
 * Created by IntelliJ IDEA.
 * User: pmei
 * Date: 11.02.2010
 * Time: 15:25:25
 * Package: ch.clx.geargui
 * Class: GearGUI
 */

import scala.swing._
import collection.mutable.ListBuffer
import event._
import actors.Actor
import actors.Actor._

object GearGUI extends SimpleSwingApplication {
  private val nOfGears = 40

  /**
   * Setup all GUI components here
   * All are accessable from this application
   */
  private val sliderCollection = new ListBuffer[GearSlider]
  private val gearCollection = new ListBuffer[Gear]
  private var gearController: GearController = null
  private val saboteur = new Saboteur

  object startButton extends Button {text = "Start"}
  object sabotageButton extends Button {text = "Sabotage"}
  object progressBar extends ProgressBar {labelPainted = true; max = nOfGears; value = 0}
  object calculatedSpeedLabel extends Label {text = "Calculated speed"}
  object calculatedSpeedTextField extends TextField {text = "0"; columns = 3}


  /**
   * Constructor
   * do some general things
   */
  saboteur.start()

  def top = new MainFrame {
    /**
     *   Set global vars for some components
     */
    var allSilders: ListBuffer[GearSlider] = new ListBuffer()

    /**
     * Set properties for mainframe
     */
    title = "Gear Swing Simulation"
    preferredSize = new java.awt.Dimension(800, 600)

    menuBar = new MenuBar {
      contents += new Menu("File") {
        contents += new MenuItem(Action("Quit") {
          System.exit(0)
        })
      }
      contents += new Menu("Control") {
        contents += new MenuItem(Action("Start") {
          startSimulation
        })
        contents += new MenuItem(Action("Random sabotage") {
          //Do a total random sabotage (random gear-selection, and random sabotage-value)
          doSabotage(scala.util.Random.nextInt(gearCollection.length)/2)
        })
      }
    }

    /**
     * Start with setting up the gui
     */
    contents = new SplitPane {

      /**
       * Initial properties for SplitPane
       */
      dividerLocation = 250
      dividerSize = 8
      oneTouchExpandable = true
      orientation = Orientation.Vertical;

      /**
       *  Contains two buttons and a label
       */
      val buttonPanel = new FlowPanel {
        preferredSize = new java.awt.Dimension(200, 0)
        contents += startButton
        contents += sabotageButton
        contents += calculatedSpeedLabel
        contents += calculatedSpeedTextField
        contents += progressBar
      }

      /**
       * Contains n of Slider
       * Each slider represents a gear
       */
      val gearPanel = new FlowPanel {
        preferredSize = new java.awt.Dimension(600, 0)
        for (i <- 0 to nOfGears - 1) {
          object slider extends GearSlider {
            min = 0
            value = 0
            max = 1000
            majorTickSpacing = 100
            sliderId = i
          }
          contents += slider
          sliderCollection += slider
        }
      }


      /**
       * Register both components with the parent SplitPane
       */
      leftComponent = buttonPanel
      rightComponent = gearPanel

    }

    /**
     * Definies listener and patterns for GUI-eventmatching
     */
    listenTo(startButton)
    listenTo(sabotageButton)
    sliderCollection.foreach(s => listenTo(s))
    sliderCollection.foreach(s => listenTo(s.mouse.clicks))
    reactions += {
      case ButtonClicked(`startButton`) =>
        println("[GearGUI] Startbutton")
        startSimulation
      case ButtonClicked(`sabotageButton`) =>
        println("[GearGUI] Sabotage")
        doSabotage(1);
      case e: MouseReleased =>
        println("[GearGUI] Mouse clicked at " + e.source.asInstanceOf[GearSlider].value)
        doSabotage(e.source.asInstanceOf[GearSlider].sliderId, e.source.asInstanceOf[GearSlider].value)
      case _ =>
      //println("AnyEvent")
    }
  }

  def startSimulation = {
    //TODO check that only one simulation  is running
    println("[App] start with creating gears")
    for (i <- 0 until nOfGears) {
      gearCollection += new Gear(i)
    }
    gearController = new GearController(gearCollection, receiver)

    println(sliderCollection.length)
    gearController.start()
    Actor.actor {
      gearController ! StartSync
    }
  }

  /**
   * Do a random sabotage
   */
  def doSabotage(nOfGears: Int) = {
    val sabotageList = new ListBuffer[Gear]()
    for (identity <- 0 until nOfGears) {
      sabotageList += gearCollection.apply(scala.util.Random.nextInt(gearCollection.length))
    }
    saboteur ! Sabotage(sabotageList)
  }

  def doSabotage(gearId: Int, toSpeed: Int) = {
    saboteur ! SabotageManual(gearCollection.apply(gearId), toSpeed)
  }

  /**
   * Define the actor/API for this GUI
   */
  val receiver: Actor = actor {
    while (true) {
      receive {
        case CurrentSpeed(gearId: Int, speed: Int) =>
          //println("[GearGUI] (" + gearId + ")] SetSpeed to newSpeed: " + speed)
          sliderCollection.filter {s => s.sliderId == gearId}.first.value = speed
          if(sliderCollection.filter {s => s.sliderId == gearId}.first.background != java.awt.Color.RED) {
            sliderCollection.filter {s => s.sliderId == gearId}.first.background = java.awt.Color.YELLOW
          }
        case GearProblem(gearId: Int) =>
          println("[GearGUI] Attention there is a gear problem!")
          sliderCollection.filter {s => s.sliderId == gearId}.first.background = java.awt.Color.RED
        case Progress(numberOfSyncGears: Int) =>
          println("[GearGUI] Progress: " + numberOfSyncGears)
          progressBar.value = numberOfSyncGears
        case ReceivedSpeed(gearId: Int) =>
          println("[GearGUI] ReceivedSpeed gearId: " + gearId)
          sliderCollection.filter {s => s.sliderId == gearId}.first.background = java.awt.Color.GREEN
        case SetCalculatedSyncSpeed(syncSpeed: Int) =>
          println("[GearGUI] SetCalculatedSyncSpeed syncSpeed: " + syncSpeed)
          calculatedSpeedTextField.text = syncSpeed.toString

        case _ => println("[GearGUI] Message could not be evaluated!")

      }
    }
  }
}

class GearSlider extends Slider {
  var sliderId = -1;
}