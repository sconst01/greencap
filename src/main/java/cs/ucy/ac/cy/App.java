package cs.ucy.ac.cy;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class App {

    public static void start(ImportedData data, int method) throws IOException, InterruptedException {

        // make minor changes to dataset from peak to non-peak hour
        //data.reallocationToNonProduction();

        long startTime = System.nanoTime();
        // get total/appliance consumption per hour
        System.out.println("Hourly total/per appliance energy consumption are written in files");
        Statistics.ConsumptionPerHour(data);

        // calculate average consumption for each appliance per hour
        float[] averageConsumptions = Statistics.applianceAverageConsumptionPerHour(data.getHourlyEnergyConsumption());

        System.out.println();
        System.out.println("Average consumption per appliance:");
        System.out.println(Arrays.toString(averageConsumptions));

        // find the max consumption a device can consume in an hour
        float[] maxConsumptions = Statistics.applianceMaxConsumption(data.getHourlyEnergyConsumption());

        System.out.println();
        System.out.println("Max consumption per appliance:");
        System.out.println(Arrays.toString(maxConsumptions));

        if (method == 1) {

            System.out.println("\n*********************************************************************************");

            // get red/green energy per hour

            System.out.println();
            System.out.println("Statistics:");
            Statistics.redGreenEnergyPerDay(data.getHourlyEnergyConsumption(), averageConsumptions,
                    1, data.getRules(), startTime, "Standard");

            startTime = System.nanoTime();
            int iterations = 5;
            List<String[]> randomData = Algorithms.random(iterations, data.getHourlyEnergyConsumption(),
                    maxConsumptions, averageConsumptions, data.getRules());

            // get red/green energy per hour after random
            System.out.println();
            System.out.println("Statistics after random allocation(Iterations = " + iterations + "):");
            Statistics.redGreenEnergyPerDay(randomData, averageConsumptions, 1,
                    data.getRules(), startTime, "Random");

            // call brute force allocation
            startTime = System.nanoTime();
            List<String[]> bruteForceData = Algorithms.bruteForceSolution(data.getHourlyEnergyConsumption(),
                    maxConsumptions, data.getRules());

            // get red/green energy per hour after random
            System.out.println();
            System.out.println("Statistics after brute force allocation(optimal schedule with stacking consumptions):");
            Statistics.redGreenEnergyPerDay(bruteForceData, averageConsumptions, 1,
                    data.getRules(), startTime, "BruteForce");

            // call greencap
            startTime = System.nanoTime();
            List<String[]> genAlgData = GreenCap.GeneticAlgorithm(data.getHourlyEnergyConsumption(),
                    data.getPeakHours(), maxConsumptions, averageConsumptions, data.getRules());


            System.out.println();
            System.out.println("Statistics after GreenCap Algorithm(Genetic Algorithm + " +
                    "FixConsumption Heuristic + Peak Hours Reallocation Heuristic):");
            Statistics.redGreenEnergyPerDay(genAlgData, averageConsumptions, 1,
                    data.getRules(), startTime, "GreenCap");

            // call greencap comfort
            startTime = System.nanoTime();
            List<String[]> genAlgComfortData =
                    GreenCapComfort.GeneticAlgorithmComfort(data.getHourlyEnergyConsumption(),
                            data.getPeakHours(), maxConsumptions, averageConsumptions, data.getRules());

            System.out.println();
            System.out.println("Statistics after GreenCap Algorithm Comfort(Genetic Algorithm + " +
                    "FixConsumption Heuristic + Peak Hours Reallocation Heuristic + " +
                    "Considering Comfort in Fitness Function):");
            Statistics.redGreenEnergyPerDay(genAlgComfortData, averageConsumptions, 1,
                    data.getRules(), startTime, "GreenCapComfort");
        }

        else {

            System.out.println("\n**********************************************************************************");

            // convert data to binary
            Algorithms.convertToBinary(data);

            // get red/green energy per hour
            startTime = System.nanoTime();
            System.out.println();
            System.out.println("Statistics(Binary):");
            Statistics.redGreenEnergyPerDay(data.getBinaryEnergyConsumption(), averageConsumptions, 0, data.getRules(), startTime, "Standard");

            // call random algorithm (n the number of times to random allocate appliances consumption
            startTime = System.nanoTime();
            List<String[]> randomDataBinary = Algorithms.randomBinary(5, data.getBinaryEnergyConsumption(), averageConsumptions, data.getRules());

            // get red/green energy per hour after random
            System.out.println();
            System.out.println("Statistics after random allocation(Binary):");
            Statistics.redGreenEnergyPerDay(randomDataBinary, averageConsumptions, 0, data.getRules(), startTime, "Random");

            // call brute force allocation
            startTime = System.nanoTime();
            List<String[]> bruteForceBinaryData = Algorithms.bruteForceBinarySolution(data.getBinaryEnergyConsumption(), averageConsumptions);

            // get red/green energy per hour after random
            System.out.println();
            System.out.println("Statistics after brute force allocation(optimal schedule based on 0/1 devices):");
            Statistics.redGreenEnergyPerDay(bruteForceBinaryData, averageConsumptions, 0, data.getRules(), startTime, "BruteForce");

            // call genetic algorithm
            startTime = System.nanoTime();
            List<String[]> genAlgBinData = GreenCapBinary.GeneticAlgorithmBinary(data.getBinaryEnergyConsumption(), data.getPeakHours(), averageConsumptions, data.getRules());

            System.out.println();
            System.out.println("Statistics after GreenCap(Binary) Algorithm(Genetic Algorithm + FixConsumption Heuristic + Peak Hours Reallocation Heuristic):");
            Statistics.redGreenEnergyPerDay(genAlgBinData, averageConsumptions, 0, data.getRules(), startTime, "GreenCap");

        }

        System.out.println("\n**********************************************************************************************\n");

    }

}
