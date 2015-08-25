## Purpose of the GameOfLiveDemo: 
The Game of Live Demo uses an implementation of Conway's game-of-life for the purpose of integration testing, performance testing and showcasing how Eve agents can be used. At this moment it's mostly used for the performance testing of the RPC call path in various situations.

The demo will create a grid of cells that will update their state based on its neighboring cells states. It will try to run as many rounds of updates as possible within a specified runtime. After the run it will stop updating and provide a ASCII visualisation of the results of each round. Above this visualisation it will also report some statistics on the run.

This implementation of the Game-of-life is not meant to be an optimal solution for the game-of-life as such (an memory stable array based solution can easily outperform this implementation) but is meant to measure and optimized the inter-agent communication of Eve.

## How to Compile:
`mvn clean install`
This will compile the sources (in src/main/java/...) and build a jar-file containing the resulting binary and all it's associated dependencies. This jar-file ends up in the target folder, with a name like GameOfLiveDemo-*version_number*.jar. (an example version_number is: 3.2.0-SNAPSHOT)

## How to Run:
`java -jar target/GameOfLiveDemo-3.2.0-SNAPSHOT.jar eve.yaml < 55blink.txt`

This starts Java with the given jar-file as executable code, please check if the version number is still correct. There are several eveX.yaml files available in the root-folder, for various tests we run. Most importantly for running the demo, the eve.yaml contains the number of rows and columns the cell grid will contain. This has to match the given input file (in the example above the 55blink.txt file). There are also several inputfiles with possible start grids for the various runs. (among others some 30x30 grids)

## Configuration:
The eve.yaml file contains all the configuration of the demo. Besides the normal Eve configuration of the cell agents, there are several demo specific parameters:
*gol>rows*: Number of rows in the input file (needs to match the input file)
*gol>columns*: Number of columns in the input file (needs to match the input file)
*gol>runTime*: Runtime of the demo
*gol>reportOnly*: If set to true, the visualisation will be skipped
*gol>random*: If set to true, no input file is needed as the initial state of each cell will be randomly chosen
*gol>OddUrl*: Base URL for agents at odd cell locations
*gol>EvenUrl*: Base URL for agents at even cell locations
*gol>nofCores*: A hint for Eve how many CPU cores are available (in case the detection goes wrong)
