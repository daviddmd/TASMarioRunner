package org.example;

import luigi.GameMode;
import luigi.MarioUtils;
import luigi.Request;
import luigi.RunResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static luigi.GameMode.*;

public class Main {

    public static GameMode gameModeFromString(String gameMode) {
        return switch (gameMode.toUpperCase()) {
            case "SPEEDRUN" -> SPEEDRUN;
            case "COIN" -> COIN;
            case "SCORE" -> SCORE;
            default -> throw new RuntimeException("Invalid Game Mode");
        };
    }

    public static void writeFile(String fileName, Population population) throws IOException {
        List<String> lines = new ArrayList<>();
        //primeira linha é a geração, próximas linhas são os indivíduos
        lines.add(String.valueOf(population.getGeneration()));
        for (Individual individual : population.getIndividualList()) {
            lines.add(IntStream.of(Arrays.stream(individual.getChromosomeSimplified()).mapToInt(Integer::intValue).toArray()).mapToObj(Integer::toString).collect(Collectors.joining("")));
        }
        Path file = Paths.get(fileName);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public static void writeFileStats(String fileName, Population population) throws IOException {
        Path file = Paths.get(fileName);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        int total = population.getTotalFitness();
        int best = population.getIndividualList().get(0).getFitness();
        int average = total / population.getPopulationCount();
        char separator = ',';
        String line = String.valueOf(population.getGeneration()) + separator + best + separator + total + separator + average + "\r\n";
        Files.writeString(file, line, StandardOpenOption.APPEND);
    }

    public static Population loadFile(String fileName, int numberInputs, int inputLength) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        int generation = Integer.parseInt(lines.get(0));
        List<Individual> individualList = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            Integer[] individualInputArray = Stream.of(lines.get(i).split("")).mapToInt(Integer::parseInt).boxed().toArray(Integer[]::new);
            individualList.add(new Individual(individualInputArray, numberInputs, inputLength));
        }
        return new Population(individualList, generation);
    }

    public static void printGenes(Population population, PrintStream stdout) {
        stdout.println("----------------------");
        for (int individualCount = 0; individualCount < population.getPopulationCount() / 2; individualCount++) {
            stdout.printf("(%d)\tIndividual {%d}\t%s\tFitness (%d)\t%s%n",
                    population.getGeneration(),
                    individualCount,
                    population.getIndividual(individualCount).hashCode(),
                    population.getIndividual(individualCount).getFitness(),
                    Arrays.toString(population.getIndividual(individualCount).getChromosomeSimplified()
                    )
            );
        }
        stdout.println("----------------------");
    }

    public static void clearFiles(String generationFileName, String statisticsFileName) throws IOException {
        Path generationFile = Paths.get(generationFileName);
        Path statisticsFile = Paths.get(statisticsFileName);

        if (!Files.exists(generationFile)) {
            Files.createFile(generationFile);
        }
        if (!Files.exists(statisticsFile)) {
            Files.createFile(statisticsFile);
        }
        Files.writeString(generationFile, "");
        Files.writeString(statisticsFile, "");

    }

    public static void main(String[] args) {
        Properties prop = new Properties();
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            prop.load(input);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        boolean loadFromFile = Boolean.parseBoolean(prop.getProperty("app.load_from_file"));
        String fileName = prop.getProperty("app.file_name");
        String statisticsFileName = prop.getProperty("app.statistics_file_name");
        PrintStream stdout = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        String level = prop.getProperty("app.level");
        String ip = prop.getProperty("app.ip_address");
        int port = Integer.parseInt(prop.getProperty("app.port"));
        int elitism = Integer.parseInt(prop.getProperty("app.elitism_rate"));
        int populationSize = Integer.parseInt(prop.getProperty("app.population_size"));
        double mutationRate = Double.parseDouble(prop.getProperty("app.mutation_rate"));
        double extraordinaryMutationRate = Double.parseDouble(prop.getProperty("app.extraordinary_mutation_rate"));
        int extraordinaryMutationRateOffset = Integer.parseInt(prop.getProperty("app.extraordinary_mutation_rate_offset"));
        double crossoverRate = Double.parseDouble(prop.getProperty("app.crossover_rate"));
        int tournamentSize = populationSize / Integer.parseInt(prop.getProperty("app.tournament_size_factor"));
        int inputLength = Integer.parseInt(prop.getProperty("app.input_length"));
        int numberInputs = Integer.parseInt(prop.getProperty("app.number_inputs"));
        int numberGenerations = Integer.parseInt(prop.getProperty("app.number_generations"));
        boolean doCrossOver = Boolean.parseBoolean(prop.getProperty("app.do_crossover"));
        GameMode gameMode = gameModeFromString(prop.getProperty("app.game_mode"));
        ParentSelection parentSelection = ParentSelection.fromString(prop.getProperty("app.parent_selection"));
        CrossoverType crossoverType = CrossoverType.fromString(prop.getProperty("app.crossover_type"));
        boolean submitToLeaderboard = Boolean.parseBoolean(prop.getProperty("app.submit_to_leaderboard"));
        String teamName = prop.getProperty("team_name");
        boolean renderInterface = Boolean.parseBoolean(prop.getProperty("render_interface"));
        boolean renderInterfaceSubmit = Boolean.parseBoolean(prop.getProperty("render_interface_submit"));
        GeneticAlgorithm ga = new GeneticAlgorithm(gameMode, numberInputs, inputLength, parentSelection, crossoverType, elitism, mutationRate, extraordinaryMutationRate, crossoverRate, tournamentSize, populationSize, ip, level, port, extraordinaryMutationRateOffset, stdout, renderInterface);
        Population population;
        if (!loadFromFile) {
            population = ga.generatePopulation();
            try {
                clearFiles(fileName, statisticsFileName);
            }
            catch (IOException e) {
                stdout.println("Error while clearing files: " + e.getMessage());
                throw new RuntimeException(e);
            }
            stdout.println("Created new Population.");
        }
        else {
            try {
                population = loadFile(fileName, numberInputs, inputLength);
                stdout.printf("Loaded population at generation %d from file.%n", population.getGeneration());
            }
            catch (IOException e) {
                stdout.println("Error while loading population " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        ga.evaluatePopulation(population);
        while (population.getGeneration() < numberGenerations) {
            population.sort();
            try {
                writeFile(fileName, population);
                writeFileStats(statisticsFileName, population);
            }
            catch (IOException e) {
                stdout.println("Error while saving file: " + e.getMessage());
                throw new RuntimeException(e);
            }
            printGenes(population, stdout);
            stdout.printf("Generation (%d) Population Fitness: %d%n", population.getGeneration(), population.getTotalFitness());
            Individual bestIndividual = population.getIndividual(0);
            stdout.printf("Best Solution [%d] - %s%n", bestIndividual.getFitness(), Arrays.toString(bestIndividual.getChromosomeSimplified()));
            MarioUtils mu = new MarioUtils(ip);
            RunResult result = mu.goMarioGo(new Request(bestIndividual.getChromosome(), level, Boolean.toString(renderInterfaceSubmit).toLowerCase()), port);
            if (
                    (((gameMode == SPEEDRUN || gameMode == SCORE) && result.getReason_finish().equals("win")) ||
                            (gameMode == COIN && result.getCoins() >= 4 && result.getReason_finish().equals("win")) && submitToLeaderboard)
            ) {
                try {
                    mu.submitToLeaderboard(result, teamName, gameMode);
                    stdout.println("Submitted to leaderboard");
                }
                catch (Exception e) {
                    stdout.println("Exception " + e.getMessage());
                    //throw new RuntimeException(e);
                }
            }
            if (doCrossOver) {
                population = ga.crossoverPopulation(population);
            }
            population = ga.mutatePopulation(population);
            ga.evaluatePopulation(population);
            population.setGeneration(population.getGeneration() + 1);
        }
        stdout.println("End of " + numberGenerations + " generations");
    }
}