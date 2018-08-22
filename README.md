# Genetic Programming Hyper-Heuristic for Uncertain Capacitated Arc Routing Problem

The library contains a range of alogrithms for solving Uncertain Capacitated Arc Routing Problem (UCARP).

For experimentation, it includes three static (deterministic) datasets (gdb, val and egl in the data/ directory). The UCARP version is generated based on each static instance, where the variables (e.g. demand and deadheading cost) are transformed from a deterministic value to a random variable. The static value given in the file is treated as the mean of the random distribution.

Genetic Programming Hyper-Heuristic (GPHH) is a major category of algorithms in this library. It evolves a so-called **routing policy** which could then be used for making real-time decisions such as telling a vehicle where to go next.

The library is based on the ECJ Java library. If you are not familiar with ECJ, you are highly suggested to learn more from https://cs.gmu.edu/~eclab/projects/ecj/. Following ECJ's style, all the algorithms designed in this library are essentially a ```.params``` file.

The main program is the ```SimpleEvolve.java``` file in the ```src/gputils``` directory. To run a simple GPHH algorithm for UCARP, you could configure the argument of ```SimpleEvolve.java``` to ```-file src/gphhucarp/algorithm/simplereactivegp/train.params```, and possibly with other parameters such as seed number.

All the other algorithms are in the ```src/gphhucarp/algorithm/``` folder for you to try.
