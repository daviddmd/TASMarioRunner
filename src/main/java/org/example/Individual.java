package org.example;

import java.util.Random;

enum Buttons {
    NOOP, RIGHT, RIGHT_A, RIGHT_B, RIGHT_A_B, A, LEFT, LEFT_A, LEFT_B, LEFT_A_B, DOWN, UP
}

public class Individual {
    private final int numberInputs;
    private final int inputLength;
    private Integer[] chromosome;
    private int fitness;

    private int diedAtCommand;
    private final int[] currentChoices = {Buttons.RIGHT_A_B.ordinal(), Buttons.RIGHT_A_B.ordinal(), Buttons.RIGHT_A_B.ordinal(), Buttons.RIGHT.ordinal(), Buttons.NOOP.ordinal(), Buttons.A.ordinal(), Buttons.LEFT_A_B.ordinal()};

    public Individual(int numberInputs, int inputLength) {
        this.diedAtCommand = -1;
        this.numberInputs = numberInputs;
        this.inputLength = inputLength;
        this.chromosome = new Integer[numberInputs];
        for (int i = 0; i < numberInputs; i++) {
            this.chromosome[i] = generateGene();
        }
        this.fitness = -1;
    }

    public Individual(Integer[] chromosome, int numberInputs, int inputLength) {
        this.diedAtCommand = -1;
        this.chromosome = chromosome;
        this.numberInputs = numberInputs;
        this.inputLength = inputLength;
        this.fitness = -1;
    }

    public int getDiedAtCommand() {
        return diedAtCommand;
    }

    public void setDiedAtCommand(int diedAtCommand) {
        this.diedAtCommand = diedAtCommand;
    }

    public int getFitness() {
        return fitness;
    }

    public int getGene(int geneIndex) {
        return this.chromosome[geneIndex];
    }

    public Integer[] getChromosomeSimplified() {
        return this.chromosome;
    }

    public Integer[] getChromosome() {
        Integer[] chromosomeReturn = new Integer[this.numberInputs * this.inputLength];
        for (int i = 0; i < this.numberInputs; i++) {
            for (int j = 0; j < this.inputLength; j++) {
                chromosomeReturn[i * this.inputLength + j] = this.chromosome[i];
            }
        }
        return chromosomeReturn;
    }

    public void setChromosome(Integer[] chromosome) {
        this.chromosome = chromosome;
    }

    public void setFitness(int fitness) {
        this.fitness = fitness;
    }

    public void mutateGene(int geneIndex) {
        this.chromosome[geneIndex] = generateGene();
    }

    public void setGene(int geneIndex, int gene) {
        this.chromosome[geneIndex] = gene;
    }

    public int generateGene() {
        Random r = new Random();
        return currentChoices[r.nextInt(currentChoices.length)];
    }
    public void resetChromosome(){
        for (int i = 0; i < numberInputs; i++) {
            this.chromosome[i] = generateGene();
        }
    }

    public int getNumberInputs() {
        return numberInputs;
    }

    public int getInputLength() {
        return inputLength;
    }
}
