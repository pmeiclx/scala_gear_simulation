A gear simulation in Scala
========

## Introduction
A simulation of gears using actors and scala-swing in Scala 2.8-Snapshot. 
To get familiar with the scala actors I wrote a small simulation with some gears and one controller: 
- The ActorApplication creates 40 gears with random speed. 
- The controller calculates a synchronization speed and starts the gear-actors. 
- The gears synchronize to this given speed step by step (1+ or 1-). 
- The simulation is finished when all gears have reached the synchronization speed.
- It's possible to manipulate the working gears with a sabotage-function.

### Interessting side effects
The simulation runs well on Java JRE 1.5: all gears are working simultaneously.
If you change to Java JRE 1.6 only 2-3 gears are working simultaneously instead of 40.

## Installation

### Easy start in IDEA IntelliJ
If you want use IDEA IntelliJ just open the project and switch the run configuration

### Easy start with maven
  1.  Open the setEnv.cmd in the project directory in an editor. Change this line: set JAVA_HOME=
    JAVA_HOME should point to JDK 1.5 or JDK 1.6
  2.  Open a commandline (cmd) and change to the project directory
  3.  run the following command: setEnv.cmd
  4.  run the following command: mvn
    The application will start
  5.  for changing the JDK just edit the setEnv.cmd and start at step 2 again

### Full configuration for windows
For running from console you have to change the path environement variable:
  1.  define a new variable for JDK 1.5 i.E.
Variable name: JAVA_HOME_15
Variable value: C:\Java\jdk1.5.0_21
  2.  define a new variable for JDK 1.6 i.E.
Variable name: JAVA_HOME_16
Variable value: C:\Java\jdk1.6.0_16
  3.  use the normal JAVA_HOME variable for switching i.E.
Variable name: JAVA_HOME
Variable value: %JAVA_HOME_15%
change the value to %JAVA_HOME_16% for using JDK/JRE 1.6
  4.  make sure that the normal %JAVA_HOME% is in your path-var at first position. For more informations: http://forums.sun.com/thread.jspa?messageID=9633247#9633247
  5.  you can now run the project with maven: mvn 
(default goal in pom.xml is set to scala:run)
    
## TODO
- Replace all println with a logging util.
- Fix TODOs listed in code.