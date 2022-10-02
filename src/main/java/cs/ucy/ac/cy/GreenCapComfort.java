package cs.ucy.ac.cy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GreenCapComfort {

    // object parameters init
    private final PopulationComfort population;
    private ChromosomeComfort firstSelected;
    private ChromosomeComfort secondSelected;
    private ChromosomeComfort best;
    private int generationCount;
    Random rn = new Random();

    // GreenCap object constructor
    public GreenCapComfort(List<String[]> energyConsumptions, int popSize,
                           float[] maxConsumption, float[] averageConsumption, List<String[]> rules) throws IOException {
        this.population = new PopulationComfort(popSize, energyConsumptions, maxConsumption, averageConsumption, rules);
        this.generationCount = 0;
    }

    // function to implement allocation of appliances using genetic algorithm
    public static List<String[]> GeneticAlgorithmComfort(List<String[]> energyConsumptions, List<String[]> peakHours,
                                                         float[] maxConsumption, float[] averageConsumption,
                                                         List<String[]> rules) throws IOException {

        // parameters init
        final int popSize = 30;
        final int maxGeneration = 10;

        // init GreenCap object - Population
        GreenCapComfort greenCapComfort = new GreenCapComfort(energyConsumptions, popSize,
                maxConsumption, averageConsumption, rules);

        // calculate how total consumption of a device in a day for all days of the year
        float[][] dailyConsumption = greenCapComfort.calcConsumptions(energyConsumptions);

        System.out.println("\nGreenCapComfort:");
        System.out.println("Population of " + greenCapComfort.population.getPopSize() + " chromosome(s).");

        //Calculate fitness of each individual
        greenCapComfort.population.calculateFitness(rules);

        // print current generation and fitness
        System.out.println("Generation: " + greenCapComfort.generationCount + " Fittest Score: " +
                greenCapComfort.population.getFittestScore() + " Exported Energy: " +
                greenCapComfort.population.getFittestExported() + " kWh. Imported Energy: " +
                greenCapComfort.population.getFittestImported() + " kWh. Total Production: " +
                greenCapComfort.population.getFittestProduction() + " kWh. Total Consumption: " +
                greenCapComfort.population.getFittestConsumption() + " kWh. Comfort Cost: " +
                greenCapComfort.population.getFittestComfortCost());

        // while current generation is less than max number of generations
        while (greenCapComfort.generationCount < maxGeneration) {

            int iteration = 0;

            // increase the current generation
            ++greenCapComfort.generationCount;

            // while iteration is less than the number of individuals in population
            while (iteration < popSize) {

                // do selection (random select two individuals)
                greenCapComfort.selection();

                // do crossover function
                greenCapComfort.crossover();

                // do mutation function
                greenCapComfort.mutation();

                // fix the consumptions to match the first consumption
                greenCapComfort.firstSelected.fixConsumptions(dailyConsumption, maxConsumption);
                greenCapComfort.secondSelected.fixConsumptions(dailyConsumption, maxConsumption);

                // update fitness values of offspring and clone best offspring
                greenCapComfort.firstSelected.calcFitness(rules);
                greenCapComfort.secondSelected.calcFitness(rules);
                try {
                    if (greenCapComfort.firstSelected.getFitness() < greenCapComfort.secondSelected.getFitness())
                        greenCapComfort.best = (ChromosomeComfort) greenCapComfort.firstSelected.clone();
                    else
                        greenCapComfort.best = (ChromosomeComfort) greenCapComfort.secondSelected.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                // reallocate appliances from peak to non-peak hour
                greenCapComfort.peakHoursReallocation(peakHours);

                // add the fittest offspring to population
                greenCapComfort.addFittestOffspring(rules);

                // increase iteration
                ++iteration;
            }

            // find the fittest chromosome
            greenCapComfort.population.getFittest();

            // print current generation and fitness
            System.out.println("Generation: " + greenCapComfort.generationCount + " Fittest Score: " +
                    greenCapComfort.population.getFittestScore() + " Exported Energy: " +
                    greenCapComfort.population.getFittestExported() + " kWh. Imported Energy: " +
                    greenCapComfort.population.getFittestImported() + " kWh. Total Production: " +
                    greenCapComfort.population.getFittestProduction() + " kWh. Total Consumption: " +
                    greenCapComfort.population.getFittestConsumption() + " kWh. Comfort Cost: " +
                    greenCapComfort.population.getFittestComfortCost());

        }

        System.out.println("Fitness: " + greenCapComfort.population.getFittestScore());

        // fix consumption for every chromosome
        for (ChromosomeComfort c : greenCapComfort.population.getChromosomes())
            c.fixConsumptions(dailyConsumption, maxConsumption);

        greenCapComfort.population.calculateFitness(rules);
        greenCapComfort.population.getFittest();

        return greenCapComfort.population.getChromosomes()[greenCapComfort.population.getFittestIndex()].getGenes();

    }

    // selection function
    public void selection() {

        try {
            // random select the first individual
            int firstRandom = rn.nextInt(population.getChromosomes().length);
            firstSelected = (ChromosomeComfort) population.getChromosomes()[firstRandom].clone();

            // random select the second individual (make sure second != first)
            int secondRandom = rn.nextInt(population.getChromosomes().length);
            while (secondRandom == firstRandom) {
                secondRandom = rn.nextInt(population.getChromosomes().length);
            }
            secondSelected = (ChromosomeComfort) population.getChromosomes()[secondRandom].clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

    }

    // crossover function
    public void crossover() {

        //int crossOverPoint = rn.nextInt(24);

        // lists to save the results for crossover
        List<String[]> first = new ArrayList<>();
        List<String[]> second = new ArrayList<>();

        // iterate day per day
        for (int i = 0; i < firstSelected.getGenes().size(); i += 24) {

            // with possibility 90% swap values between the two offsprings at crossover point
            if (rn.nextInt(100) <= 90) {

                // select a random crossover point in the day
                int crossOverPoint = rn.nextInt(24);

                // swap consumption until the crossover point between the two offsprings
                for (int k = i; k < i + crossOverPoint; k++) {
                    first.add(secondSelected.getGenes().get(k));
                    second.add(firstSelected.getGenes().get(k));
                }

                // fill the list with the remaining hours of the day
                for (int k = i + crossOverPoint; k < i + 24; k++) {
                    first.add(firstSelected.getGenes().get(k));
                    second.add(secondSelected.getGenes().get(k));
                }
            }

            // else just fill with the current consumptions
            else {
                for (int k = i; k < i + 24; k++) {
                    first.add(firstSelected.getGenes().get(k));
                    second.add(secondSelected.getGenes().get(k));
                }
            }
        }

        // save results
        firstSelected = new ChromosomeComfort(first);
        secondSelected = new ChromosomeComfort(second);

    }

    // mutation function
    public void mutation() {

        // select a random mutation point in the day for the first selected
        //int mutationPoint = rn.nextInt(24);

        // lists to save the results for mutation
        List<String[]> first = new ArrayList<>();
        List<String[]> second = new ArrayList<>();

        //iterate day per day
        for (int i = 0; i < firstSelected.getGenes().size(); i += 24) {

            // with possibility 1% turn on/off appliances at the first selected offspring at mutation point
            if (rn.nextInt(100) == 99) {

                // select a random mutation point in the day for the first selected
                int mutationPoint = rn.nextInt(24);

                // add to list until the mutation point
                for (int k = i; k < i + mutationPoint; k++) {
                    first.add(firstSelected.getGenes().get(k));
                }

                // at mutation point flip values(turn on/off) for all the appliances at that hour
                String[] res = new String[21];
                res[0] = firstSelected.getGenes().get(i + mutationPoint)[0];
                for (int k = 1; k < res.length - 1; k++) {
                    res[k] = "0.0";
                }
                res[20] = firstSelected.getGenes().get(i + mutationPoint)[20];
                first.add(res);

                // add to list the remaining hours of the day
                for (int k = i + mutationPoint + 1; k < i + 24; k++) {
                    first.add(firstSelected.getGenes().get(k));
                }

            }

            // else add the consumptions of the day
            else {
                for (int k = i; k < i + 24; k++)
                    first.add(firstSelected.getGenes().get(k));
            }
        }

        //iterate day per day
        for (int i = 0; i < secondSelected.getGenes().size(); i += 24) {

            // with possibility 1% turn on/off appliances at the first selected offspring at mutation point
            if (rn.nextInt(100) == 99) {

                // select a random mutation point in the day for the first selected
                int mutationPoint = rn.nextInt(24);

                // add to list until the mutation point
                for (int k = i; k < i + mutationPoint; k++) {
                    second.add(secondSelected.getGenes().get(k));
                }

                // at mutation point flip values(turn on/off) for all the appliances at that hour
                String[] res = new String[21];
                res[0] = secondSelected.getGenes().get(i + mutationPoint)[0];
                for (int k = 1; k < res.length - 1; k++) {
                    res[k] = "0.0";
                }
                res[20] = secondSelected.getGenes().get(i + mutationPoint)[20];
                second.add(res);

                // add to list the remaining hours of the day
                for (int k = i + mutationPoint + 1; k < i + 24; k++) {
                    second.add(secondSelected.getGenes().get(k));
                }

            }

            // else add the consumptions of the day
            else {
                for (int k = i; k < i + 24; k++)
                    second.add(secondSelected.getGenes().get(k));
            }
        }

        // save results
        firstSelected = new ChromosomeComfort(first);
        secondSelected = new ChromosomeComfort(second);


    }

    // get the fittest offspring (offspring with the least fitness value)
    public ChromosomeComfort getFittestOffspring() {
        if ((best.getFitness() <= firstSelected.getFitness()) && (best.getFitness() <= secondSelected.getFitness())) {
            return best;
        } else if ((firstSelected.getFitness() <= secondSelected.getFitness())
                && (firstSelected.getFitness() <= best.getFitness())) {
            return firstSelected;
        } else {
            return secondSelected;
        }
    }

    // replace the least fit individual from the fittest offspring
    public void addFittestOffspring(List<String[]> rules) {

        // calculate fitness of the chromosomes
        firstSelected.calcFitness(rules);
        secondSelected.calcFitness(rules);
        best.calcFitness(rules);

        // get index of the least fit individual
        int leastFittestIndex = population.getLeastFittestIndex();

        // replace the least fit individual from the fittest offspring
        population.getChromosomes()[leastFittestIndex] = new ChromosomeComfort(getFittestOffspring());

        firstSelected = null;
        secondSelected = null;
        best = null;
    }

    // function to reallocate consumptions from peak hour to non-peak hour
    public void peakHoursReallocation(List<String[]> peakHours) {

        // init variables
        int firstPeakHour, secondPeakHour, thirdPeakHour;
        float firstPeakHourConsumption, secondPeakHourConsumption, thirdPeakHourConsumption;
        int firstNonPeakHour, secondNonPeakHour, thirdNonPeakHour;
        float firstNonPeakHourConsumption, secondNonPeakHourConsumption, thirdNonPeakHourConsumption;
        int firstPeakGenerationHour, secondPeakGenerationHour, thirdPeakGenerationHour;
        float firstPeakGeneration, secondPeakGeneration, thirdPeakGeneration;
        String[][] checkSelected, checkBest;    // arrays for check the daily fitness after reallocation
        List<String[]> reallocatedData = new ArrayList<>();

        // iterate day by day
        for (int i = 0; i < firstSelected.getGenes().size(); i += 24) {

            // give the correct value to variables
            firstPeakHour = secondPeakHour = thirdPeakHour = 0;
            firstPeakHourConsumption = secondPeakHourConsumption = thirdPeakHourConsumption = Float.MIN_VALUE;
            firstNonPeakHour = secondNonPeakHour = thirdNonPeakHour = 0;
            firstNonPeakHourConsumption = secondNonPeakHourConsumption = thirdNonPeakHourConsumption = Float.MAX_VALUE;
            firstPeakGenerationHour = secondPeakGenerationHour = thirdPeakGenerationHour = 0;
            firstPeakGeneration = secondPeakGeneration = thirdPeakGeneration = Float.MIN_VALUE;
            checkBest = new String[24][21];
            checkSelected = new String[24][21];


            // find three peak hours and the three hours with the least consumption
            for (int j = i; j < i + 24; j++) {

                // copy values to the two arrays for check if result is better after reallocation
                for (int k = 0; k < best.getGenes().get(j).length; k++) {
                    checkSelected[j % 24][k] = best.getGenes().get(j)[k];
                    checkBest[j % 24][k] = best.getGenes().get(j)[k];
                }

                // replace comma with empty char
                String val = (peakHours.get(j)[2]).replaceAll(",", "");

                // find three peak hours
                if (Float.parseFloat(val) > firstPeakHourConsumption) {
                    thirdPeakHour = secondPeakHour;
                    thirdPeakHourConsumption = secondPeakHourConsumption;
                    secondPeakHour = firstPeakHour;
                    secondPeakHourConsumption = firstPeakHourConsumption;
                    firstPeakHour = j % 24;     // mod 24 to find the specific hour of the day
                    firstPeakHourConsumption = Float.parseFloat(val);
                } else if (Float.parseFloat(val) > secondPeakHourConsumption) {
                    thirdPeakHour = secondPeakHour;
                    thirdPeakHourConsumption = secondPeakHourConsumption;
                    secondPeakHour = j % 24;    // mod 24 to find the specific hour of the day
                    secondPeakHourConsumption = Float.parseFloat(val);
                } else if (Float.parseFloat(val) > thirdPeakHourConsumption) {
                    thirdPeakHour = j % 24;     // mod 24 to find the specific hour of the day
                    thirdPeakHourConsumption = Float.parseFloat(val);
                }

                // find three non-peak hours
                if (Float.parseFloat(val) < firstNonPeakHourConsumption) {
                    thirdNonPeakHour = secondNonPeakHour;
                    thirdNonPeakHourConsumption = secondNonPeakHourConsumption;
                    secondNonPeakHour = firstNonPeakHour;
                    secondNonPeakHourConsumption = firstNonPeakHourConsumption;
                    firstNonPeakHour = j % 24;
                    firstNonPeakHourConsumption = Float.parseFloat(val);
                } else if (Float.parseFloat(val) < secondNonPeakHourConsumption) {
                    thirdNonPeakHour = secondNonPeakHour;
                    thirdNonPeakHourConsumption = secondNonPeakHourConsumption;
                    secondNonPeakHour = j % 24;
                    secondNonPeakHourConsumption = Float.parseFloat(val);
                } else if (Float.parseFloat(val) < thirdNonPeakHourConsumption) {
                    thirdNonPeakHour = j % 24;
                    thirdNonPeakHourConsumption = Float.parseFloat(val);
                }

                // find peak generation hours in the day
                if (Float.parseFloat(best.getGenes().get(j)[20]) > firstPeakGeneration) {
                    thirdPeakGenerationHour = secondPeakGenerationHour;
                    thirdPeakGeneration = secondPeakGeneration;
                    secondPeakGenerationHour = firstPeakGenerationHour;
                    secondPeakGeneration = firstPeakGeneration;
                    firstPeakGenerationHour = j % 24;
                    firstPeakGeneration = Float.parseFloat(best.getGenes().get(j)[20]);
                } else if (Float.parseFloat(best.getGenes().get(j)[20]) > secondPeakGeneration) {
                    thirdPeakGenerationHour = secondPeakGenerationHour;
                    thirdPeakGeneration = secondPeakGeneration;
                    secondPeakGenerationHour = j % 24;
                    secondPeakGeneration = Float.parseFloat(best.getGenes().get(j)[20]);
                } else if (Float.parseFloat(best.getGenes().get(j)[20]) > thirdPeakGeneration) {
                    thirdPeakGenerationHour = j % 24;
                    thirdPeakGeneration = Float.parseFloat(best.getGenes().get(j)[20]);
                }
            }

            // reallocate/swap appliances consumption in array for check
            for (int j = 0; j < checkBest[j % 24].length - 2; j++) {
                if ((firstPeakGenerationHour != firstPeakHour) && (secondPeakGenerationHour != firstPeakHour)
                        && (thirdPeakGenerationHour != firstPeakHour)) {
                    String temp1 = checkBest[firstPeakHour][j + 1];
                    checkBest[firstPeakHour][j + 1] = checkBest[firstNonPeakHour][j + 1];
                    checkBest[firstNonPeakHour][j + 1] = temp1;
                }
                if ((firstPeakGenerationHour != secondPeakHour) && (secondPeakGenerationHour != secondPeakHour)
                        && (thirdPeakGenerationHour != secondPeakHour)) {
                    String temp2 = checkBest[secondPeakHour][j + 1];
                    checkBest[secondPeakHour][j + 1] = checkBest[secondNonPeakHour][j + 1];
                    checkBest[secondNonPeakHour][j + 1] = temp2;
                }
                if ((firstPeakGenerationHour != thirdPeakHour) && (secondPeakGenerationHour != thirdPeakHour)
                        && (thirdPeakGenerationHour != thirdPeakHour)) {
                    String temp3 = checkBest[thirdPeakHour][j + 1];
                    checkBest[thirdPeakHour][j + 1] = checkBest[thirdNonPeakHour][j + 1];
                    checkBest[thirdNonPeakHour][j + 1] = temp3;
                }
            }

            // compare the two arrays and find the best
            float checkSelectedResult = calculateImportedExported(checkSelected);
            float checkBestResult = calculateImportedExported(checkBest);

            // if best have better results reallocate
            if (checkBestResult < checkSelectedResult) {
                // if best after reallocation save the best results
                reallocatedData.addAll(Arrays.asList(checkBest).subList(0, 24));
            }

            // else save the previous results
            else {
                reallocatedData.addAll(Arrays.asList(checkSelected).subList(0, 24));
            }

        }

        // save the results
        best = new ChromosomeComfort(reallocatedData);

    }

    // function to compare the two consumptions
    public float calculateImportedExported(String[][] check) {

        float Imp = 0.0F;
        float Exp = 0.0F;

        for (String[] hour : check) {

            // calculate hour consumption
            float hourConsumption = 0.0F;
            for (int j = 0; j < hour.length - 2; j++) {
                hourConsumption += (Float.parseFloat(hour[j + 1]));
            }

            // calculate imported/exported
            if (hourConsumption > Float.parseFloat(hour[20]))
                Imp += Math.abs(Float.parseFloat(hour[20]) - hourConsumption);
            else
                Exp += Math.abs(Float.parseFloat(hour[20]) - hourConsumption);

        }

        // return imported/exported based on their weight
        return Imp;
    }

    public float[][] calcConsumptions(List<String[]> genes) {
        float[][] consumptions = new float[366][19];

        // iterate above all data day per day
        for (int i = 0; i < genes.size(); i += 24) {

            // iterate per device in a day
            for (int j = 1; j < genes.get(i).length - 1; j++) {

                // count how many hours is open
                float counter = 0;
                for (int k = 0; k < 24; k++) {
                    counter += Float.parseFloat(genes.get(i + k)[j]);
                }

                // save the total consumption of the device
                consumptions[i / 24][j - 1] = counter;
            }
        }
        return consumptions;
    }

}
