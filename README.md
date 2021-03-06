A gear simulation in Scala
========

## Introduction
A simulation of gears using actors and scala-swing with Scala 2.8.final.
To get familiar with the scala actors I wrote a small simulation:

- The ActorApplication creates 40 gears with random speed.
- The controller calculates a synchronization speed and starts the gear-actors.
- The gears synchronize to this given speed step by step (1+ or 1-).
- The simulation is finished when all gears have reached the synchronization speed.
- It's possible to manipulate the working gears with a sabotage-function.

### Interesting side effects
The simulation runs well on Java JRE 1.5: all gears are working simultaneously.
If you change to Java JRE 1.6 only 2-3 gears are working simultaneously instead of 40.
see http://stackoverflow.com/questions/2288723/scala-actors-different-behavior-on-jre-1-5-and-1-6

## Installation

### Easy start in IDEA IntelliJ
If you want use IDEA IntelliJ just open the project and switch the run configuration

#### Debug configuration for IDEA IntelliJ
1.  Create new Run-Configuration (select Application)
2.  Use ch.clx.geargui.GearGUI as mainclass
3.  disable "make" before launch
4.  enable "maven goal" before launch: compile
5.  Use debug-button in IDEA

### Easy start with maven
1.  Open the setEnv.cmd in the project directory in an editor. Change this line: <i>set JAVA_HOME=</i>
    <br />(JAVA_HOME should point to JDK 1.5 or JDK 1.6)
2.  Open a command line (cmd) and change to the project directory
3.  run the following command: 
        setEnv.cmd
4.  run the following command (The application will start): 
        mvn
5.  for changing the JDK just edit the setEnv.cmd and start at <b>step 2</b> again.

### Full configuration for windows
An alternative configuration is to change the PATH environment variable:

1.  define a new variable for JDK 1.5 i.E.
        Variable name: JAVA_HOME_15
        Variable value: C:\Java\jdk1.5.0_21
2.  define a new variable for JDK 1.6 i.E.
        Variable name: JAVA_HOME_16
        Variable value: C:\Java\jdk1.6.0_16
3.  use the normal JAVA_HOME variable for switching i.E.
        Variable name: JAVA_HOME
        Variable value: %JAVA_HOME_15%
    change the value in <b>%JAVA_HOME%</b> to <i>%JAVA_HOME_16%</i> for using JDK/JRE 1.6
4.  make sure that the normal %JAVA_HOME% is in your path-var at first position. For more informations: http://forums.sun.com/thread.jspa?messageID=9633247#9633247
5.  you can now run the project with maven: mvn 
    <br />(default goal in pom.xml is set to scala:run)

## Recent changes
- Added option ResizableThreadPoolScheduler and minor enhancements and bugfixes in GearGUI.scala

## TODOs
- Replace all println with a logging util