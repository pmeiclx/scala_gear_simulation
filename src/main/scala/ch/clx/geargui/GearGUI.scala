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
import scala.swing.Swing._
import collection.mutable.ListBuffer
import event._
import actors.Actor._
import scala.actors.{Actor, Scheduler}
import actors.scheduler.{ForkJoinScheduler, ResizableThreadPoolScheduler}

object GearGUI extends SimpleSwingApplication {

  //Set manually if you want to have more Gears/Sliders
  private val nOfGears = 40

  //Needed to track the State of the simulation
  private var nOfSynchGears = 0

  //This coll serves as a // coll to the internal contents coll. Needed for access to the elements
  private val sliderCollection = new ListBuffer[GearSlider]

  //for simplicity reasons (easier sabotage) here and not in the GearController
  private var gearCollection = new ListBuffer[Gear]

  //Actors used for Communication
  private var gearController: GearController = null
  private var saboteur: Saboteur = null

  //ForkJoinScheduler is default
  var isForkJoinScheduler: Boolean = true
  var schedulerName: String = "forkJoinScheduler"

  /**
   * Setup all GUI components here
   * All are accessible from this application
   */
  object startButton extends Button {text = "Start"}
  object sabotageButton extends Button {text = "Sabotage"}
  object progressBar extends ProgressBar {labelPainted = true; max = nOfGears; value = 0}
  object calculatedSpeedLabel extends Label {text = "Calculated speed"}
  object calculatedSpeedTextField extends TextField {text = "0"; columns = 3}

  object radioButtonPanel extends BoxPanel(Orientation.Vertical) {
    border = CompoundBorder(TitledBorder(EtchedBorder, "Scheduler"), EmptyBorder(5, 5, 5, 10))
    val forkJoinScheduler = new RadioButton("ForkJoinScheduler")
    val resizableThreadPoolScheduler = new RadioButton("ResizableThreadPoolScheduler")
    val mutex = new ButtonGroup(forkJoinScheduler, resizableThreadPoolScheduler)
    mutex.select(forkJoinScheduler)
    contents ++= mutex.buttons
  }


  def top = new MainFrame {


    /**
     * Set properties for mainframe
     */
    title = "A simulation of gears using actors and scala-swing in Scala 2.8"
    preferredSize = new java.awt.Dimension(1200, 500)

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
        contents += new MenuItem(Action("Random sabotage n Gears") {
          if(isSimulationRunning){
          doSabotage(scala.util.Random.nextInt(gearCollection.length) / 2)
          }
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
       *  Contains controls for the simulation
       */
      val buttonPanel = new FlowPanel {
        preferredSize = new java.awt.Dimension(200, 0)
        contents += startButton
        contents += sabotageButton
        contents += calculatedSpeedLabel
        contents += calculatedSpeedTextField
        contents += progressBar
        contents += radioButtonPanel
      }

      /**
       * Contains n singleton instances of GearSlider
       * Each slider represents a Gear
       */
      val gearPanel = new FlowPanel {
        preferredSize = new java.awt.Dimension(600, 0)
        for (i <- 0 to nOfGears - 1) {
          object slider extends GearSlider {
            min = 0
            value = 0
            max = 1000
            majorTickSpacing = 100
            //must be set to true, otherwise the background color does not show
            opaque = true
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
     * Define listener and patterns for GUI-eventmatching
     */
    listenTo(startButton)
    listenTo(sabotageButton)
    val forkJoinScheduler = radioButtonPanel.forkJoinScheduler
    val resizableThreadPoolScheduler = radioButtonPanel.resizableThreadPoolScheduler
    listenTo(forkJoinScheduler)
    listenTo(resizableThreadPoolScheduler)
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
      case ButtonClicked(`forkJoinScheduler`) =>
        println("[GearGUI] ToggleButton forkJoinScheduler clicked")
        isForkJoinScheduler = true
        schedulerName = "forkJoinScheduler"
      case ButtonClicked(`resizableThreadPoolScheduler`) =>
        println("[GearGUI] ToggleButton resizableThreadPoolScheduler clicked")
        isForkJoinScheduler = false
        schedulerName = "resizableThreadPoolScheduler"
      case _ =>
      //println("AnyEvent: ")
    }
  }

  def startSimulation = {
    println("[GearGUI] starting new simulation")

    cleanup()
    handleSchedulerType()

    saboteur = new Saboteur()
    saboteur.start()

    for (i <- 0 until nOfGears) {
      gearCollection += new Gear(i)
    }
    gearController = new GearController(gearCollection, receiver, schedulerName)
    receiver.start()
    gearController.start()
    Actor.actor {
      gearController ! StartSync
    }
    startButton.enabled = false
  }

  def cleanup() = {
    //needed if the simulation is started n times
    gearCollection = new ListBuffer[Gear]
    gearController = null
    saboteur = null
    progressBar.value = 0
    nOfSynchGears = 0
  }


  def handleSchedulerType() = {
    //Doc ResizableThreadPoolScheduler see 
    //http://www.scala-lang.org/docu/files/api/scala/actors/scheduler/ResizableThreadPoolScheduler.html
    //http://scala-programming-language.1934581.n4.nabble.com/Increase-actor-thread-pool-td1936329.html
    if (isForkJoinScheduler) {
      Scheduler.impl = {
        val s = new ForkJoinScheduler(true);
        s.start()
        s
      }
    } else {
          Scheduler.impl = {
        val s = new ResizableThreadPoolScheduler(true);
        s.start()
        s
    }
    }
  }

  def isSimulationRunning = nOfSynchGears > 0 && nOfSynchGears < nOfGears

  def handleStartButton() = {
      if (isSimulationRunning) {
        startButton.enabled = false
      } else {
        startButton.enabled = true
      }
    }


  /**
   * Do a total random sabotage (random gear-selection, and random sabotage-value)
   */
  def doSabotage(nOfGears: Int) = {
    if (isSimulationRunning) {
      val sabotageList = new ListBuffer[Gear]()
      for (identity <- 0 until nOfGears) {
        sabotageList += gearCollection(scala.util.Random.nextInt(gearCollection.length))
      }
      saboteur ! Sabotage(sabotageList)
    }
  }

  /**
   * Do sabotage one Gear (choosen via the Slider)
   */
  def doSabotage(gearId: Int, toSpeed: Int) = {
    if (isSimulationRunning) {
      saboteur ! SabotageManual(gearCollection(gearId), toSpeed)
    }
  }

  /**
   * Define the actor/API for this GUI
   */
  val receiver: Actor = actor {
    while (true) {
      receive {
        case CurrentSpeed(gearId: Int, speed: Int) =>
          //println("[GearGUI] (" + gearId + ")] SetSpeed to newSpeed: " + speed)
          findSlider(gearId).value = speed
          if (findSlider(gearId).background != java.awt.Color.RED) {
            findSlider(gearId).background = java.awt.Color.YELLOW
          }
        case GearProblem(gearId: Int) =>
          println("[GearGUI] Recieved gear problem - due to Sabotage!")
          findSlider(gearId).background = java.awt.Color.RED
        case Progress(numberOfSyncGears: Int) =>
          println("[GearGUI] Progress: " + numberOfSyncGears)
          progressBar.value = numberOfSyncGears
          nOfSynchGears = numberOfSyncGears
          handleStartButton()

        case ReceivedSpeed(gearId: Int) =>
          println("[GearGUI] ReceivedSpeed gearId: " + gearId)
          findSlider(gearId).background = java.awt.Color.GREEN
        case SetCalculatedSyncSpeed(syncSpeed: Int) =>
          println("[GearGUI] SetCalculatedSyncSpeed syncSpeed: " + syncSpeed)
          calculatedSpeedTextField.text = syncSpeed.toString

        case _ => println("[GearGUI] Message could not be evaluated!")

      }
    }

    def findSlider(gearId: Int) = {
      sliderCollection.find(_.sliderId == gearId).get
    }

  }
}


class GearSlider extends Slider {
  var sliderId = -1;
}