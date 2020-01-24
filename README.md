# Blocks
Solves wooden puzzle block games

This provides the first solution it finds. If there is more than one solution only 1 is printed. If we want to check all
solutions we would add time to searching the whole problem space. 

## Run this as a java11 script
 ``` java Blocks.java ```
 
 Or you could always compile it and stuff
 
## Interpretation

 Goal is to get block 0 to the middle RHS 
 ```
 1 1 5 2 3 
_ _ 5 2 3 
_ _ 5 0 6 
4 4 _ _ 6 
_ 7 7 7 _ 
```
There are 7 blocks in the above output, Block 1 is in the top left and is 2 blocks wide. Block 7 is 3 blocks wide in the bottom row.

In the above example the following moves are possible:
* Block 1  can move Down
* Block 2  stuck
* Block 3  stuck
* Block 4  can move Up or right
* Block 5  can move Down
* Block 6  can move Down
* Block 7  can move Left or Right
* Block 0  can move Down
