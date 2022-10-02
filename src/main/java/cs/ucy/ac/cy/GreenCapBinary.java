package cs.ucy.ac.cy;

import java.io.IOException;
import java.util.*;

public class GreenCapBinary {

    // object parameters init
    private final PopulationBinary population;
    private ChromosomeBinary firstSelected;
    private ChromosomeBinary secondSelected;
    private ChromosomeBinary best;
    private int generationCount;
    Random rn = new Random();

    // GreenCap object constructor
    public GreenCapBinary(List<String[]> energyConsumptions, int popSize, float[] averageConsumption, List<String[]> rules) throws IOException {
        this.population = new PopulationBinary(popSize, energyConsumptions, averageConsumption, rules);
        this.generationCount = 0;
    }

    // function to implement allocation of appliances using genetic algorithm
    public static List<String[]> GeneticAlgorithmBinary(List<String[]> energyConsumptions, List<String[]> peakHours, float[] averageConsumptions, List<String[]> rules) throws IOException {

        // parameters init
        final int popSize = 50;
        final int maxGeneration = 20;

        // init GreenCap object - Population
        GreenCapBinary greenCapBinary = new GreenCapBinary(energyConsumptions, popSize, averageConsumptions, rules);

        // calculate how many times a device is open in a day for all days of the year
        int[][] dailyConsumption = greenCapBinary.calcConsumptions(energyConsumptions);

        System.out.println("\nGreenCapBinary:");
        System.out.println("Population of " + greenCapBinary.population.getPopSize() + " chromosome(s).");

        //Calculate fitness of each individual
        greenCapBinary.population.calculateFitness(averageConsumptions);

        // print current generation and fitness
        System.out.println("Generation: " + greenCapBinary.generationCount + " Fittest Score: " + greenCapBinary.population.getFittestScore() + " Exported Energy: " + greenCapBinary.population.getFittestExported() + " kWh. Imported Energy: " + greenCapBinary.population.getFittestImported() + " kWh. Total Production: " + greenCapBinary.population.getFittestProduction() + " kWh. Total Consumption: " + greenCapBinary.population.getFittestConsumption() + " kWh.");

        // while current generation is less than max number of generations
        while (greenCapBinary.generationCount < maxGeneration) {

            int iteration = 0;

            // increase the current generation
            ++greenCapBinary.generationCount;

            // while iteration is less than the number of individuals in population
            while (iteration < popSize) {

                // do selection (random select two individuals)
                greenCapBinary.selection();

                // do crossover function
//                if (greenCap.rn.nextInt(100) < 90)
                greenCapBinary.crossover();


                // do mutation function
//                if (greenCap.rn.nextInt(100) == 99)
                greenCapBinary.mutation();

                // fix the consumptions to match the first consumption
                greenCapBinary.firstSelected.fixConsumptions(dailyConsumption);
                greenCapBinary.secondSelected.fixConsumptions(dailyConsumption);

                // update fitness values of offspring and clone best offspring
                greenCapBinary.firstSelected.calcFitness(averageConsumptions);
                greenCapBinary.secondSelected.calcFitness(averageConsumptions);
                try {
                    if (greenCapBinary.firstSelected.getFitness() < greenCapBinary.secondSelected.getFitness())
                        greenCapBinary.best = (ChromosomeBinary) greenCapBinary.firstSelected.clone();
                    else
                        greenCapBinary.best = (ChromosomeBinary) greenCapBinary.secondSelected.clone();
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }

                // reallocate appliances from peak to non-peak hour
                greenCapBinary.peakHoursReallocation(peakHours, averageConsumptions);

                // add the fittest offspring to population
                greenCapBinary.addFittestOffspring(averageConsumptions);

                // increase iteration
                ++iteration;
            }

            // find the fittest chromosome
            greenCapBinary.population.getFittest();

            // print current generation and fitness
            System.out.println("Generation: " + greenCapBinary.generationCount + " Fittest Score: " + greenCapBinary.population.getFittestScore() + " Exported Energy: " + greenCapBinary.population.getFittestExported() + " kWh. Imported Energy: " + greenCapBinary.population.getFittestImported() + " kWh. Total Production: " + greenCapBinary.population.getFittestProduction() + " kWh. Total Consumption: " + greenCapBinary.population.getFittestConsumption() + " kWh.");

        }

        System.out.println("Fitness: " + greenCapBinary.population.getFittestScore());

        return greenCapBinary.population.getChromosomes()[greenCapBinary.population.getFittestIndex()].getGenes();

    }

    // selection function
    public void selection() {

        try {
            // random select the first individual
            int firstRandom = rn.nextInt(population.getChromosomes().length);
            firstSelected = (ChromosomeBinary) population.getChromosomes()[firstRandom].clone();

            // random select the second individual (make sure second != first)
            int secondRandom = rn.nextInt(population.getChromosomes().length);
            while (secondRandom == firstRandom) {
                secondRandom = rn.nextInt(population.getChromosomes().length);
            }
            secondSelected = (ChromosomeBinary) population.getChromosomes()[secondRandom].clone();
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
        firstSelected = new ChromosomeBinary(first);
        secondSelected = new ChromosomeBinary(second);

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
                    if (firstSelected.getGenes().get(i + mutationPoint)[k].equals("1.0"))
                        res[k] = "0.0";
                    else
                        res[k] = "1.0";
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
                    if (secondSelected.getGenes().get(i + mutationPoint)[k].equals("1.0"))
                        res[k] = "0.0";
                    else
                        res[k] = "1.0";
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
        firstSelected = new ChromosomeBinary(first);
        secondSelected = new ChromosomeBinary(second);


    }

    // get the fittest offspring (offspring with the least fitness value)
    public ChromosomeBinary getFittestOffspring() {
        if ((best.getFitness() <= firstSelected.getFitness()) && (best.getFitness() <= secondSelected.getFitness()))
            return best;
        else if ((firstSelected.getFitness() <= secondSelected.getFitness()) && (firstSelected.getFitness() <= best.getFitness())) {
            return firstSelected;
        } else {
            return secondSelected;
        }
    }

    // replace the least fit individual from the fittest offspring
    public void addFittestOffspring(float[] averageConsumption) {

        // calculate fitness of the chromosomes
        firstSelected.calcFitness(averageConsumption);
        secondSelected.calcFitness(averageConsumption);
        best.calcFitness(averageConsumption);

        // get index of the least fit individual
        int leastFittestIndex = population.getLeastFittestIndex();

        // replace the least fit individual from the fittest offspring
        population.getChromosomes()[leastFittestIndex] = new ChromosomeBinary(getFittestOffspring());

        firstSelected = null;
        secondSelected = null;
        best = null;
    }

    // function to reallocate consumptions from peak hour to non-peak hour
    public void peakHoursReallocation(List<String[]> peakHours, float[] averageConsumption) {

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
                if ((firstPeakGenerationHour != firstPeakHour) && (secondPeakGenerationHour != firstPeakHour) && (thirdPeakGenerationHour != firstPeakHour)) {
                    String temp1 = checkBest[firstPeakHour][j + 1];
                    checkBest[firstPeakHour][j + 1] = checkBest[firstNonPeakHour][j + 1];
                    checkBest[firstNonPeakHour][j + 1] = temp1;
                }
                if ((firstPeakGenerationHour != secondPeakHour) && (secondPeakGenerationHour != secondPeakHour) && (thirdPeakGenerationHour != secondPeakHour)) {
                    String temp2 = checkBest[secondPeakHour][j + 1];
                    checkBest[secondPeakHour][j + 1] = checkBest[secondNonPeakHour][j + 1];
                    checkBest[secondNonPeakHour][j + 1] = temp2;
                }
                if ((firstPeakGenerationHour != thirdPeakHour) && (secondPeakGenerationHour != thirdPeakHour) && (thirdPeakGenerationHour != thirdPeakHour)) {
                    String temp3 = checkBest[thirdPeakHour][j + 1];
                    checkBest[thirdPeakHour][j + 1] = checkBest[thirdNonPeakHour][j + 1];
                    checkBest[thirdNonPeakHour][j + 1] = temp3;
                }
            }

            // compare the two arrays and find the best
            float checkSelectedResult = calculateImportedExported(checkSelected, averageConsumption);
            float checkBestResult = calculateImportedExported(checkBest, averageConsumption);

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
        best = new ChromosomeBinary(reallocatedData);

    }

    // function to compare the two consumptions
    public float calculateImportedExported(String[][] check, float[] averageConsumption) {

        float Imp = 0.0F;
        float Exp = 0.0F;

        for (String[] hour : check) {

            // calculate hour consumption
            float hourConsumption = 0.0F;
            for (int j = 0; j < hour.length - 2; j++) {
                hourConsumption += (Float.parseFloat(hour[j + 1]) * averageConsumption[j]);
            }

            // calculate imported/exported
            if (hourConsumption > Float.parseFloat(hour[20]))
                Imp += Math.abs(Float.parseFloat(hour[20]) - hourConsumption);
            else
                Exp += Math.abs(Float.parseFloat(hour[20]) - hourConsumption);

        }

        // return imported/exported based on their weight
        return Imp;
//        return Exp;
    }

    public int[][] calcConsumptions(List<String[]> genes) {
        int[][] consumptions = new int[366][19];

        // iterate above all data day per day
        for (int i = 0; i < genes.size(); i += 24) {

            // iterate per device in a day
            for (int j = 1; j < genes.get(i).length - 1; j++) {

                // count how many hours is open
                int counter = 0;
                for (int k = 0; k < 24; k++) {

                    if (genes.get(i + k)[j].equals("1.0")) {
                        counter++;
                    }
                }

                // save the counted number
                consumptions[i / 24][j - 1] = counter;
            }
        }
        return consumptions;
    }

}
