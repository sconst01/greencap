package cs.ucy.ac.cy;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Statistics {

    // function to create csv file appliance and total energy consumption per hour
    public static void ConsumptionPerHour(ImportedData data) throws IOException {

        List<String[]> energyConsumptionData = data.getEnergyConsumption();
        List<String[]> solarGenData = data.getSolarGen();

        // date formatter to read from data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // lists to save the data needed
        List<LocalDateTime> hours = new ArrayList<>();
        List<float[]> energyConsumptions = new ArrayList<>();

        // variables to save appliance consumption and current hour
        int currentHour = 0;
        float[] consumptionPerHour = new float[19];

        // for each sample of data
        for (int i = 0; i < energyConsumptionData.size(); i++) {

            // get sample and read hour
            String[] metric = energyConsumptionData.get(i);
            LocalDateTime date = LocalDateTime.parse(metric[0], formatter);
            int readHour = date.getHour();

            // if it is different from previous read save the total consumption and the hour
            if (readHour != currentHour) {

                // save consumption for previous hour
                LocalDateTime oneHourBack = date.minusHours(1);
                hours.add(oneHourBack);
                energyConsumptions.add(consumptionPerHour);

                // set consumption to 0
                consumptionPerHour = new float[19];
                Arrays.fill(consumptionPerHour, 0.0F);

                // set current hour to the hour that is read
                currentHour = readHour;
            }

            // calculate the appliance consumption for this sample
            for (int j = 1; j < metric.length; j++) {
                consumptionPerHour[j - 1] = consumptionPerHour[j - 1] + Float.parseFloat(metric[j]);
            }

            // if is the last sample save the total consumption
            if (i == (energyConsumptionData.size() - 1)) {
                LocalDateTime oneHourBack = date.minusMinutes(59);
                hours.add(oneHourBack);
                energyConsumptions.add(consumptionPerHour);
            }
        }

        // get results in list of string[]
        List<String[]> applianceEnergyConsumptionsPerHour = new ArrayList<>();
        for (int i = 0; i < hours.size(); i++) {
            String[] array = new String[21];
            array[0] = hours.get(i).format(formatter);
            for (int j = 0; j < energyConsumptions.get(i).length; j++) {
                array[j + 1] = String.valueOf(energyConsumptions.get(i)[j]);
            }
            array[20] = solarGenData.get(i)[1];
            applianceEnergyConsumptionsPerHour.add(array);
        }

        // write result in data object
        data.setHourlyEnergyConsumption(applianceEnergyConsumptionsPerHour);

        // write results in txt file
        File applianceFile = new File("ApplianceConsumptionPerHour.csv");
        //applianceFile.createNewFile();
        FileWriter applianceWriter = new FileWriter(applianceFile);
        for (int i = 0; i < hours.size(); i++) {
            applianceWriter.write(hours.get(i).format(formatter) + ",");
            float[] row = energyConsumptions.get(i);
            for (float v : row) {
                applianceWriter.write(v + ",");
            }
            applianceWriter.write(solarGenData.get(i)[1] + "\n");
        }
        applianceWriter.close();

        // write results in csv file
        File totalFile = new File("TotalConsumptionPerHour.csv");
        //totalFile.createNewFile();
        FileWriter totalWriter = new FileWriter(totalFile);
        for (int i = 0; i < energyConsumptions.size(); i++) {
            float[] row = energyConsumptions.get(i);
            float total = 0.0F;
            for (float v : row) {
                total = total + v;
            }
            totalWriter.write(hours.get(i).format(formatter) + "," + total + "," + solarGenData.get(i)[1] + "\n");
        }
        totalWriter.close();
    }

    // function to calculate red/green energy per hour
    public static void redGreenEnergyPerDay(List<String[]> totalConsumption, float[] averageConsumption, int check, List<String[]> rules, long startTime, String function) throws IOException, InterruptedException {

        // variables to save total red/green energy
        float importedEnergy = 0.0F, exportedEnergy = 0.0F, redEnergy = 0.0F, greenEnergy = 0.0F, comfortPercentage;
        int comfortCost = 0, matchComfort = 0;
        int currentMonth = 1;
        float[] importedEnergyMonthly = new float[12];
        float[] exportedEnergyMonthly = new float[12];
        float[] redEnergyMonthly = new float[12];
        float[] greenEnergyMonthly = new float[12];
        float[] selfconsumption = new float[12];
        for (int i = 0; i < 12; i++){
            importedEnergyMonthly[i] = 0.0F;
            exportedEnergyMonthly[i] = 0.0F;
            redEnergyMonthly[i] = 0.0F;
            greenEnergyMonthly[i] = 0.0F;
            selfconsumption[i] = 0.0F;
        }
        // date formatter to read from data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // for each sample of data
        for (String[] hour : totalConsumption) {

            LocalDateTime currentDateTime = LocalDateTime.parse(hour[0], formatter);
            if (currentDateTime.getMonthValue() != currentMonth){
                selfconsumption[currentMonth - 1] = greenEnergyMonthly[currentMonth - 1] - exportedEnergyMonthly[currentMonth - 1];
                importedEnergyMonthly[currentMonth - 1] *= -1;
                ++currentMonth;
            }

            // read consumption and generation per hour from data
            float energyConsumption = 0.0F;
            for (int j = 1; j < hour.length - 1; j++) {
                float temp = Float.parseFloat(hour[j]);
                for (String[] rule : rules) {
                    if (rule[0].equals(hour[0]) && Integer.parseInt(rule[1]) == j) {
                        if (Float.parseFloat(hour[Integer.parseInt(rule[1])]) > 0.0) {
                            matchComfort += 1;
                        } else {
                            comfortCost += 1000;
                        }
                    }
                }
                if (check == 0)
                    energyConsumption += (temp * averageConsumption[j - 1]);
                else
                    energyConsumption += (temp);
            }
            float energyGenerated = Float.parseFloat(hour[hour.length - 1]);

            // increase red/green energy
            redEnergy += energyConsumption;
            greenEnergy += energyGenerated;
            redEnergyMonthly[currentMonth - 1] += energyConsumption;
            greenEnergyMonthly[currentMonth - 1] += energyGenerated;

            // calculate generate minus consumption
            float typeOfEnergy = energyGenerated - energyConsumption;

            // greater than zero equals green
            if (typeOfEnergy >= 0.0) {
                exportedEnergy += typeOfEnergy;
                exportedEnergyMonthly[currentMonth - 1] += typeOfEnergy;
            }

            // less than zero equals red
            else {
                importedEnergy += typeOfEnergy;
                importedEnergyMonthly[currentMonth - 1] += typeOfEnergy;
            }
        }

        importedEnergyMonthly[11] *= -1;
        selfconsumption[11] = greenEnergyMonthly[11] - exportedEnergyMonthly[11];

        comfortPercentage = (float) (matchComfort * 100.0) / rules.size();

        float co2emissionPerKWh = (float) 0.449;

        // print total red/green energy
        System.out.printf("Total Exported Energy(Production energy non-consumed): %.2f kWh\n", exportedEnergy);
        System.out.println("Monthly Exported Energy:");
        for (int i = 0; i < 12; i++) {
            System.out.printf("%.2f, ", exportedEnergyMonthly[i]);
        }
        float selfConsumptionEnergy = greenEnergy - exportedEnergy;
        System.out.printf("\nTotal Self-Consumed Energy: %.2f kWh\n", selfConsumptionEnergy);
        System.out.println("Monthly Self-Consumed Energy");
        for (int i = 0; i < 12; i++) {
            System.out.printf("%.2f, ", selfconsumption[i]);
        }
        System.out.printf("\nTotal Imported Energy(Red energy): %.2f kWh\n", (-1.0 * importedEnergy));
        System.out.println("Monthly Imported Energy:");
        for (int i = 0; i < 12; i++) {
            System.out.printf("%.2f, ", importedEnergyMonthly[i]);
        }
        System.out.printf("\nTotal Production Energy(Exported + Self-consumed): %.2f kWh\n", greenEnergy);
        System.out.println("Monthly Production Energy:");
        for (int i = 0; i < 12; i++) {
            System.out.printf("%.2f, ", greenEnergyMonthly[i]);
        }
        System.out.printf("\nTotal Consumption Energy(Imported + Self-consumed): %.2f kWh\n", redEnergy);
        System.out.println("Monthly Consumption Energy:");
        for (int i = 0; i < 12; i++) {
            System.out.printf("%.2f, ", redEnergyMonthly[i]);
        }
        float co2emissions = -1 * importedEnergy * co2emissionPerKWh;
        System.out.printf("\nTotal CO2 emissions(USA avg: 0.449 kgCO2/kWh): %.2f kgCO2\n", co2emissions);
        System.out.printf("Total Comfort Error Cost: %d\n", comfortCost);
        System.out.printf("Comfort: %.2f %%\n", comfortPercentage);
        long endTime = System.nanoTime();
        float execTime = (float) ((endTime - startTime) / 1000000000.0);
        System.out.printf("That took %.2f seconds\n", execTime);

        ApiCalls.sendToAPI(function, exportedEnergy, selfConsumptionEnergy, (float) (-1.0 * importedEnergy), greenEnergy, redEnergy, co2emissions, comfortCost, comfortPercentage, execTime);
    }

    public static float[] applianceAverageConsumptionPerHour(List<String[]> energyConsumption) {

        // init array
        float[] averageConsumptions = new float[19];

        // for each appliance calculate total consumption and how many times is greater than zero
        for (int i = 0; i < 19; i++) {
            int onCounter = 0;
            float totalConsumption = 0.0F;

            for (String[] row : energyConsumption) {
                if (Float.parseFloat(row[i + 1]) > 0.0) {
                    totalConsumption += Float.parseFloat(row[i + 1]);
                    onCounter++;
                }
            }

            // compute and save average of each appliance
            averageConsumptions[i] = totalConsumption / onCounter;

        }

        return averageConsumptions;

    }

    public static float[] applianceMaxConsumption(List<String[]> energyConsumption) {
        float[] maxConsumptions = new float[19];

        for (int i = 1; i < energyConsumption.get(0).length - 1; i++) {
            float maxConsTemp = 0.0F;
            for (String[] hour : energyConsumption) {
                if (Float.parseFloat(hour[i]) > maxConsTemp) {
                    maxConsTemp = Float.parseFloat(hour[i]);
                }
            }
            maxConsumptions[i - 1] = maxConsTemp;
        }

        return maxConsumptions;
    }

}
