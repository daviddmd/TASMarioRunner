# TAS Mario Runner

This program was made for the Artificial Intelligence subject of the Informatics Engineering Degree.

This program makes use of the [Gym Super Mario Bros](https://github.com/Kautenja/gym-super-mario-bros) by OpenAI in
order to complete Super Mario Bros. levels with an Artificial Intelligence Genetic Algorithm.

Its genetic algorithm parameters such as crossover/mutation rates, parent selection/crossover types may be configured on
the [Configuration File](src/main/resources/config.properties), as well as the output generation/statistics files. The
[Generation File](files/generation.txt) contains the genome of each member of the current population of the generation (
with the generation number on the first line), the genome consisting of the player inputs. On each generation the
generation file is written with the current population genomes, so the program may be safely be stopped and the
evolution resumed at a later point.

The [Statistics File](files/statistics.csv) contains the evolution of the population across generations, consisting of
the generation number, the fitness of the best individual of the population, total population fitness and the average
fitness score of the population, across generations.

The fitness calculation changes depending on the game mode, which may be speedrun (
completing the level in the less time possible), score (archieving the highest score) or coins (getting the maximum
number of coins), in which the evolutionary weights change to accomodate for the current game mode, in order to select
the best individuals and performing crossover of their genomes.

The program expects a localhost/virtual machine running the Gym Super Mario Bros server. The rendering quality may be
adjusted, as well as if the interface is displayed (to improve its performance across a population with many
individuals). The included library abstracts the POST process of the genome to the [Mario Server](ext/mario-server.py)
and POSTing the score to the university leaderboard (run with `python mario-server.py PORT_NUMBER`).