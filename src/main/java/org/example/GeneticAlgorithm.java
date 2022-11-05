package org.example;

import luigi.GameMode;
import luigi.MarioUtils;
import luigi.Request;
import luigi.RunResult;

import java.io.PrintStream;
import java.util.Random;

enum ParentSelection {
    ROULETTE, TOURNAMENT, RANK, RANDOM;

    @Override
    public String toString() {
        return switch (this) {
            case ROULETTE -> "Roulette";
            case TOURNAMENT -> "Tournament";
            case RANK -> "Rank";
            case RANDOM -> "Random";
        };
    }

    public static ParentSelection fromString(String parentSelecion) {
        return switch (parentSelecion.toUpperCase()) {
            case "ROULETTE" -> ROULETTE;
            case "TOURNAMENT" -> TOURNAMENT;
            case "RANK" -> RANK;
            case "RANDOM" -> RANDOM;
            default -> throw new RuntimeException("Invalid Parent Selection Type");
        };
    }
}

enum CrossoverType {
    SINGLE_POINT, TWO_POINT, UNIFORM;

    @Override
    public String toString() {
        return switch (this) {
            case SINGLE_POINT -> "Single Point";
            case TWO_POINT -> "Two Point";
            case UNIFORM -> "Uniform";
        };
    }

    public static CrossoverType fromString(String crossoverType) {
        return switch (crossoverType.toUpperCase()) {
            case "SINGLE_POINT" -> SINGLE_POINT;
            case "TWO_POINT" -> TWO_POINT;
            case "UNIFORM" -> UNIFORM;
            default -> throw new RuntimeException("Invalid Crossover Type");
        };
    }
}

public class GeneticAlgorithm {
    private final int extraordinaryMutationRateOffset;
    private final double extraordinaryMutationRate;
    private final GameMode gameMode;
    private final int numberInputs;
    private final int inputLength;
    private final ParentSelection parentSelection;
    private final CrossoverType crossoverType;
    private final int elitismSize;
    private final double mutationRate;
    private final double crossoverRate;
    private final int tournamentSize;
    private final int populationSize;
    private final String ipAddress;
    private final String level;
    private final int port;
    private final PrintStream stdout;

    private final boolean renderInterface;

    public GeneticAlgorithm(GameMode gameMode, int numberInputs, int inputLength, ParentSelection parentSelection, CrossoverType crossoverType, int elitismSize, double mutationRate, double extraordinaryMutationRate, double crossoverRate, int tournamentSize, int populationSize, String ipAddress, String level, int port, int extraordinaryMutationRateOffset, PrintStream stdout, boolean renderInterface) {
        this.extraordinaryMutationRate = extraordinaryMutationRate;
        this.gameMode = gameMode;
        this.numberInputs = numberInputs;
        this.inputLength = inputLength;
        this.parentSelection = parentSelection;
        this.crossoverType = crossoverType;
        this.elitismSize = elitismSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.tournamentSize = tournamentSize;
        this.populationSize = populationSize;
        this.ipAddress = ipAddress;
        this.level = level;
        this.port = port;
        this.extraordinaryMutationRateOffset = extraordinaryMutationRateOffset;
        this.stdout = stdout;
        this.renderInterface = renderInterface;
    }

    public String getLevel() {
        return level;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public int getNumberInputs() {
        return numberInputs;
    }

    public int getInputLength() {
        return inputLength;
    }

    public Population generatePopulation() {
        return new Population(populationSize, this.numberInputs, this.inputLength);
    }

    public ParentSelection getParentSelection() {
        return parentSelection;
    }

    public CrossoverType getCrossoverType() {
        return crossoverType;
    }


    public int getElitismSize() {
        return elitismSize;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public double getCrossoverRate() {
        return crossoverRate;
    }

    public int getTournamentSize() {
        return tournamentSize;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public Individual getIndividualRandom(Population population) {
        Random r = new Random();
        int position = r.nextInt(population.getPopulationCount());
        return population.getIndividual(position);
    }

    public Individual getIndividualRoulette(Population population) {
        double sumFitness = 0;
        for (Individual individual : population.getIndividualList()) {
            sumFitness += individual.getFitness();
        }
        Random r = new Random();
        double randomValue = r.nextDouble(sumFitness);
        double partialSum = 0;
        for (Individual individual : population.getIndividualList()) {
            partialSum += individual.getFitness();
            if (partialSum >= randomValue) {
                return individual;
            }
        }
        return null;
    }

    public Individual getIndividualTournament(Population population) {
        Random r = new Random();

        Population individualList = new Population(this.tournamentSize, population.getGeneration());
        for (int i = 0; i < tournamentSize; i++) {
            int pos = r.nextInt(population.getPopulationCount());
            individualList.setIndividual(i, population.getIndividual(pos));
        }
        individualList.sort();
        return individualList.getIndividual(0);
    }

    public void evaluatePopulation(Population population) {
        int totalFitness = 0;
        for (int i = 0; i < population.getPopulationCount(); i++) {
            totalFitness += calculateFitness(population.getIndividual(i));
        }
        population.setTotalFitness(totalFitness);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int calculateFitness(Individual individual) {
        MarioUtils mu = new MarioUtils(this.ipAddress);
        RunResult res = mu.goMarioGo(new Request(individual.getChromosome(), this.level, Boolean.toString(this.renderInterface).toLowerCase()), this.port);
        int fitness = 0;
        int diedAtCommand = res.getCommands_used() / individual.getInputLength();
        individual.setDiedAtCommand(diedAtCommand);
        fitness += res.getX_pos();
        if (res.getReason_finish().equals("win")) {
            fitness += 1000;
        }
        switch (this.gameMode) {
            /*
            No caso do speedrun, a bonificação do tempo é apenas atribuída caso o nível tenha sido completo
            Irá haver uma discrepância significativa entre vários elementos da população em termos de fitness, portanto
            esta bonificação irá servir unicamente para distinguir entre membros que passaram o nível em termos de
            tempo restante
             */
            case SPEEDRUN -> {
                if (res.getReason_finish().equals("win")) {
                    fitness += res.getTime_left() * 300;
                }
            }
            case COIN -> {
                fitness += res.getCoins() * 500;
            }
            case SCORE -> {
                fitness += res.getScore() * 10;
            }
        }
        if (res.getReason_finish().equals("no_more_commands") || res.getTime_left() == 0) {
            individual.resetChromosome();
            fitness = 0;
        }
        individual.setFitness(fitness);
        return fitness;
    }

    public Population crossoverPopulation(Population population) {
        Random r = new Random();

        Population newPopulation = new Population(population.getPopulationCount(), population.getGeneration());

        for (int i = 0; i < this.elitismSize; i++) {
            newPopulation.setIndividual(i, population.getIndividual(i));
        }
        for (int i = this.elitismSize; i < population.getPopulationCount(); i++) {

            Individual parent1 = population.getIndividual(i);
            Individual parent2;
            if (this.crossoverRate > Math.random()) {
                Individual crossoverOutput = new Individual(this.numberInputs, this.inputLength);
                if (this.parentSelection == ParentSelection.TOURNAMENT) {
                    parent2 = getIndividualTournament(population);
                }
                else if (this.parentSelection == ParentSelection.ROULETTE) {
                    parent2 = getIndividualRoulette(population);
                }
                else {
                    parent2 = getIndividualRandom(population);
                }
                int crossoverSinglePoint = parent1.getDiedAtCommand();
                for (int geneIndex = 0; geneIndex < this.numberInputs; geneIndex++) {
                    if (crossoverType == CrossoverType.UNIFORM) {
                        if (Math.random() > 0.5) {
                            crossoverOutput.setGene(geneIndex, parent1.getGene(geneIndex));
                        }
                        else {
                            crossoverOutput.setGene(geneIndex, parent2.getGene(geneIndex));
                        }
                    }
                    else if (crossoverType == CrossoverType.SINGLE_POINT) {
                        if (geneIndex < crossoverSinglePoint) {
                            crossoverOutput.setGene(geneIndex, parent1.getGene(geneIndex));
                        }
                        else {
                            crossoverOutput.setGene(geneIndex, parent2.getGene(geneIndex));
                        }
                    }
                }
                newPopulation.setIndividual(i, crossoverOutput);
            }
            else {
                newPopulation.setIndividual(i, parent1);
            }
        }
        return newPopulation;
    }

    public static int generateNumber(int extraordinaryMutationRateOffset) {
        Random r = new Random();
        int low = -50;
        int high = 50;
        int randomNumber = r.nextInt(high - low) + low;
        double randomPercentage = randomNumber / 100d;
        return (int) (randomPercentage * extraordinaryMutationRateOffset) + extraordinaryMutationRateOffset;
    }

    public Population mutatePopulation(Population population) {
        Population newPopulation = new Population(population.getPopulationCount(), population.getGeneration());
        for (int i = 0; i < population.getPopulationCount(); i++) {
            Individual individual = population.getIndividual(i);
            for (int f = 0; f < individual.getChromosomeSimplified().length; f++) {
                int newExtraordinaryMutationRateOffset;
                if (i >= this.elitismSize) {
                    newExtraordinaryMutationRateOffset = generateNumber(this.extraordinaryMutationRateOffset);
                    if (f >= individual.getDiedAtCommand() - newExtraordinaryMutationRateOffset && f <= individual.getDiedAtCommand() + newExtraordinaryMutationRateOffset) {
                        if (this.extraordinaryMutationRate > Math.random()) {
                            individual.mutateGene(f);
                        }
                    }
                    else if (f >= individual.getDiedAtCommand() + newExtraordinaryMutationRateOffset && this.mutationRate > Math.random()) {
                        individual.mutateGene(f);
                    }
                }
            }
            newPopulation.setIndividual(i, individual);
        }
        return newPopulation;
    }
}
