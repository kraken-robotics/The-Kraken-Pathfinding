<img align="right" src="https://raw.githubusercontent.com/PFGimenez/The-Kraken-Pathfinding/master/resources/logo.png">

# The Kraken Pathfinding

A tentacular pathfinding library for nonholonomic robotic vehicles 

## What is Kraken ?

**Kraken isn't usable yet. Please come back in Fall 2017 in you are interested.**

A presentation is available here (in french !) : https://intechsenpai.github.io/moon-rover/.

Kraken finds a trajectory followable by a car-like vehicle in the form of a list of points.

### Why Java ?

For legacy reasons, mainly.

## Getting Kraken

### Downloading the last stable version

_There is no stable version yet._

### Getting the source

If you want the latest stable version, clone this repository :

_(no stable version yet)_

If you want the latest **experimental** version, clone this repository :

    $ git clone https://github.com/PFGimenez/The-Kraken-Pathfinding.git --depth 1


### Compiling

You will need a JDK and `ant` (package `ant` or `apache-ant`) :
    
    $ cd The-Kraken-Pathfinding/core
    $ ant
    

The file ```kraken.jar```, containing the compiled code .class and the sources .java along with the dependencies .jar, will be created.

Examples are available in the directory ```examples```.

### Unit testing

You can easily run the tests :

    $ cd The-Kraken-Pathfinding/tests
    $ ant

## Great, I have a trajectory. How do I follow it ?

Getting the trajectory is only half of the work, because you won't go far if you can't follow it. Different control algorithm exists ; I will expand soonly (?) this question.

## Contributing

### How to help ?

Feel free to contribute to Kraken ! There are two easy ways of helping me :

- submit an issue containing your ideas ;
- create a pull request.

### Bug report

Bug report are done with issues. Please be careful to respect those few points :

- check if the bug hasn't be reported yet ;
- indicate the version of Kraken you use ;
- describe your problem as best as possible. If you can post a minimal code that triggers the bug, it will be much easier for me ;
- attach any relevant file.
