# Instruction for VUW ECS Students

```diff
+ this will be highlighted in green
- this will be highlighted in red
```

***![#f03c15](NB: This is ONLY applicable to the students in the School of Engineering and Computer Science, Victoria University of Wellington to run experiments using the School's grid facility. Please ignore this folder if you are not a VUW ECS student.) `#f03c15`***

1. Read the ECS Grid Tech Notes **carefully**: https://ecs.victoria.ac.nz/Support/TechNoteEcsGrid. Apply your home folder on the Grid server following the instructions of the Tech Notes.
2. Compile the ```src/gputils/SimpleEvolve.java``` into a runnable jar file (for training, say ```SimpleEvolve.jar```).
3. Compile the ```src/gphhucarp/gp/GPTest.java``` into a runnable jar file (for testing, say ```GPTest.jar```).
4. Test your jar files to make sure they work, e.g. run ```$ java -jar SimpleEvolve.jar -file xxx/train.params``` in your command line.
5. Create a new folder in your home folder, e.g. ```gpucarp```.
6. Copy/Paste the jar files into ```yourgridhome/gpucarp/package```.
7. Copy/Paste the ```data``` into ```yourgridhome/gpucarp```.
8. Create a new folder ```yourgridhome/gpucarp/newalgorithm```.
9. Copy/Paste the ```train.params``` and ```test.params``` files of your algorithm into ```yourgridhome/gpucarp/newalgorithm/params```.
10. Copy/Paste **all** the ```.sh``` shell script files in this folder into ```yourgridhome/gpucarp/newalgorithm```.
11. Modify the ```.sh``` files, so that the path configurations are correct.
12. Run ```batch-traingrid.sh``` (or ```batch-traingrid-egl.sh```) for batch training on different UCARP instances. 30 independent runs will be done, and produce ```job.x.out.stat``` and ```job.x.stat.csv``` (```x = 0, ..., 29```) in the ```yourgridhome/gpucarp/newalgorithm/instance``` folder.
13. Make sure all the 30 runs are successful (the grid may terminate some jobs in the middle unexpectedly). If some jobs fail, retrain them.
14. Run ```batch-testgrid.sh``` (or ```batch-testgrid-egl.sh```) for batch testing. The testing will produce a ```.csv``` file in the ```yourgridhome/gpucarp/newalgorithm/instance``` folder.

**After all these steps, you can analyse the ```csv``` files using any analysis script such as R or Python. Simply read the ```.csv``` as data frame, and analyse them, e.g. draw table and convergence curves.**
