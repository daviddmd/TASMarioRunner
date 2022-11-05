package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Population {
    private int generation;
    private final List<Individual> individualList;
    private int totalFitness = -1;


    public Population(int populationSize, int numberInputs, int inputLength) {
        this.generation = 1;
        this.individualList = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            this.individualList.add(new Individual(numberInputs, inputLength));
        }
    }

    public Population(List<Individual> individualList, int generation) {
        this.generation = generation;
        this.individualList = individualList;
    }

    public Population(int populationSize, int generation) {
        this.generation = generation;
        this.individualList = new ArrayList<>(populationSize);
    }

    public Individual getIndividual(int offset) {
        return this.individualList.get(offset);
    }

    public void setIndividual(int offset, Individual individual) {
        this.individualList.add(offset, individual);
    }

    public List<Individual> getIndividualList() {
        return individualList;
    }

    public int getTotalFitness() {
        return totalFitness;
    }

    public void setTotalFitness(int totalFitness) {
        this.totalFitness = totalFitness;
    }

    public void sort() {
        this.individualList.sort(Comparator.comparing(Individual::getFitness).reversed());
    }

    public int getPopulationCount() {
        return this.individualList.size();
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }
}
