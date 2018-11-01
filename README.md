# Genetic Programming Hyper-Heuristic for Uncertain Capacitated Arc Routing Problem

The library contains a range of alogrithms for solving Uncertain Capacitated Arc Routing Problem (UCARP).

For experimentation, it includes three static (deterministic) datasets (gdb, val and egl in the data/ directory). The UCARP version is generated based on each static instance, where the variables (e.g. demand and deadheading cost) are transformed from a deterministic value to a random variable. The static value given in the file is treated as the mean of the random distribution.

Genetic Programming Hyper-Heuristic (GPHH) is a major category of algorithms in this library. It evolves a so-called **routing policy** which could then be used for making real-time decisions such as telling a vehicle where to go next.

The library is based on the ECJ Java library. If you are not familiar with ECJ, you are highly suggested to learn more from https://cs.gmu.edu/~eclab/projects/ecj/. Following ECJ's style, all the algorithms designed in this library are essentially a ```.params``` file.

***Your must fully understand the following Java files as a starting point.***
- ```src/gputils/SimpleEvolve.java```
- ```src/gphhucarp/gp/GPTest.java```
- The ```.params``` file that represents the algorithm you are running.
- All the Java files used in the ```.params``` file.

The pipleline of an examperiment is as follows.
1. **Training process**: Run a GPHH with a set of training samples for training a routing policy. The main program for training is the ```src/gputils/SimpleEvolve.java``` file. A standard GPHH training algorithm is ```src/gphhucarp/algorithm/simplereactivegp/train.params```. To run the simple GPHH algorithm for UCARP, you could run  ```SimpleEvolve.java``` with the argument ```-file src/gphhucarp/algorithm/simplereactivegp/train.params```. This will output a file like ```job.0.out.stat```, with the best routing policy in each generation.
2. **Test process**: Test the routing policies obtained in each generation of the GPHH training process (or some manually designed routing policies) using an unseen test set. The main program for testing is ```src/gphhucarp/gp/GPTest.java```. To test the routing policies trained by the simple GPHH algorithm, you could run ```src/gphhucarp/gp/GPTest.java``` with the argument ```-file src/gphhucarp/algorithm/simplereactivegp/test.params```. This will create a ```test``` folder beside ```src```, and produce a file ```xxx.csv``` with a variety of statistics such as the training and test performance of each routing policy obtained in each generation of the training.

All the other algorithms are in the ```src/gphhucarp/algorithm/``` folder for you to try.
