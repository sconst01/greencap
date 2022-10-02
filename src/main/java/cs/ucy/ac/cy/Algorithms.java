package cs.ucy.ac.cy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Algorithms {

    // init random
    static Random rn = new Random();

    // function to calculate total import/export energy
    public static float calculateImportedExported(float[][] dayConsumption, float[] energyGenerated,
                                                  float[] averageConsumption, int check, List<String[]> rules, String[] dates) {
        // variable to save total imported/exported energy of the day
        float totalImportedExported = 0.0F;
        int comfortError = 0;
        for (int i = 0; i < dayConsumption.length; i++) {

            // calculate hour consumption
            float hourConsumption = 0.0F;
            for (int j = 0; j < dayConsumption[i].length; j++) {
                if (check == 1){
                    hourConsumption += (dayConsumption[i][j]);
                }
                else {
                    hourConsumption += (dayConsumption[i][j] * averageConsumption[j]);
                }

                // Consider user comfort
                // if not comment out the following code until the next comment
                ////////////////////////////////////////////////////////////////////
                for (int k = 0; k < rules.size(); k++) {
                    int appliance = Integer.parseInt(rules.get(k)[1]);
                    if (rules.get(k)[0].equals(dates[i]) && appliance == (j + 1)) {
                        if (dayConsumption[i][j] == 0.0) {
                            comfortError += 10;
                        }
                    }
                }
                ////////////////////////////////////////////////////////////////////
            }

            // add the imported/exported value to total of the day
            //if (hourConsumption > energyGenerated[i])
            totalImportedExported += Math.abs(energyGenerated[i] - hourConsumption);
        }

        float retValue = (float) ((totalImportedExported * 0.75) + (comfortError * 0.25));
        // return the value
        return retValue;
    }

    // function to random allocate hourly appliance consumption per day
    public static List<String[]> randomBinary(int iterations, List<String[]> applianceConsumption,
                                              float[] averageConsumption, List<String[]> rules) throws IOException {

        // save appliance consumption to 2d array
        float[][] consumptions = new float[applianceConsumption.size()][19];
        for (int i = 0; i < applianceConsumption.size(); i++) {
            for (int j = 1; j < applianceConsumption.get(i).length - 1; j++) {
                consumptions[i][j - 1] = Float.parseFloat(applianceConsumption.get(i)[j]);
            }
        }

        // list for writing data (for output)
        List<float[]> energyConsumptions = new ArrayList<>();

        // iterate day per day (every 24 hours)
        for (int i = 0; i < consumptions.length; i += 24) {

            // get the daily energy generation
            float[] energyGenerated = new float[24];
            for (int j = 0; j < energyGenerated.length; j++) {
                energyGenerated[j] = Float.parseFloat(applianceConsumption.get(j + i)[20]);
            }

            // get the daily energy consumption per appliance (used for random operation) and set best as the current
            float[][] dayHourlyConsumption = new float[24][19];
            float[][] bestDayHourlyConsumption = new float[24][19];
            String[] dates = new String[24];
            for (int j = i; j < i + 24; j++) {
                dates[j%24] = applianceConsumption.get(j)[0];
                for (int k = 0; k < 19; k++) {
                    dayHourlyConsumption[j % 24][k] = consumptions[j][k];
                    bestDayHourlyConsumption[j % 24][k] = consumptions[j][k];
                }
            }

            // calculate current total import and exported energy
            float bestImportedExported = calculateImportedExported(dayHourlyConsumption,
                    energyGenerated, averageConsumption, 0, rules, dates);

            // for each day random allocate n times and get the lowest import/export
            for (int k = 0; k < iterations; k++) {

                // set an array with 0 (used to random allocate appliance consumption)
                dates = new String[24];
                dayHourlyConsumption = new float[24][19];
                for (float[] floats : dayHourlyConsumption) {
                    Arrays.fill(floats, 0.0F);
                }

                // counter to count 24 hours of the day
                int counter = 0;

                // iterate above every hour of the day
                for (int j = i; j < i + 24; j++) {
                    dates[j%24] = applianceConsumption.get(j)[0];
                    // hourly random allocation of devices in a day
                    for (int l = 0; l < 19; l++) {
                        if (bestDayHourlyConsumption[j % 24][l] != 0) {
                            int rnum = rn.nextInt(24);
                            if (dayHourlyConsumption[rnum][l] == 0) {
                                dayHourlyConsumption[rnum][l] = bestDayHourlyConsumption[j % 24][l];
                                continue;
                            }
                            for (; ; ) {
                                rnum = rn.nextInt(24);
                                if (dayHourlyConsumption[rnum][l] == 0) {
                                    dayHourlyConsumption[rnum][l] = bestDayHourlyConsumption[j % 24][l];
                                    break;
                                }
                            }
                        }
                    }

                    // if hour = 23 (last hour of the day) write results in list
                    if (counter == 23) {

                        // calculate total imported and exported energy
                        float totalImportedExported = calculateImportedExported(dayHourlyConsumption,
                                energyGenerated, averageConsumption, 0, rules, dates);

                        // if total import/export is less than the previous save the day hourly energy consumption
                        if (totalImportedExported < bestImportedExported) {

                        // save the new appliance consumption per hour
                        bestDayHourlyConsumption = new float[24][19];
                        for (int l = 0; l < dayHourlyConsumption.length; l++) {
                            System.arraycopy(dayHourlyConsumption[l], 0, bestDayHourlyConsumption[l],
                                    0, dayHourlyConsumption[l].length);
                        }

                        //set the new import/export energy
                        bestImportedExported = totalImportedExported;
                        }
                    }
                    // increase the counter
                    else {
                        counter++;
                    }
                }
            }

            // add 24 results (one per hour) to list
            energyConsumptions.addAll(Arrays.asList(bestDayHourlyConsumption));
        }

        List<String[]> output = new ArrayList<>();
        for (int i = 0; i < energyConsumptions.size(); i++) {
            String[] array = new String[21];
            array[0] = applianceConsumption.get(i)[0];
            for (int j = 0; j < energyConsumptions.get(i).length; j++) {
                array[j + 1] = String.valueOf(energyConsumptions.get(i)[j]);
            }
            array[20] = applianceConsumption.get(i)[20];
            output.add(array);
        }

        // write results in csv file
        File randomTotalFile = new File("RandomBinaryTotalConsumptionPerHour.csv");
        FileWriter totalWriter = new FileWriter(randomTotalFile);
        for (int i = 0; i < energyConsumptions.size(); i++) {
            float[] row = energyConsumptions.get(i);
            float total = 0.0F;
            for (int j = 0; j < row.length; j++) {
                total += (row[j] * averageConsumption[j]);
            }
            totalWriter.write(applianceConsumption.get(i)[0] + "," + total +
                    "," + applianceConsumption.get(i)[20] + "\n");
        }
        totalWriter.close();

        return output;

    }

    public static List<String[]> random(int iterations, List<String[]> applianceConsumption,
                                        float[] maxConsumption, float[] averageConsumption, List<String[]> rules) throws IOException {

        // save appliance consumption to 2d array
        float[][] consumptions = new float[applianceConsumption.size()][19];
        for (int i = 0; i < applianceConsumption.size(); i++) {
            for (int j = 1; j < applianceConsumption.get(i).length - 1; j++) {
                consumptions[i][j - 1] = Float.parseFloat(applianceConsumption.get(i)[j]);
            }
        }

        // list for writing data (for output)
        List<float[]> energyConsumptions = new ArrayList<>();

        // iterate day per day (every 24 hours)
        for (int i = 0; i < consumptions.length; i += 24) {

            // get the daily energy generation
            float[] energyGenerated = new float[24];
            String[] dates = new String[24];
            for (int j = 0; j < energyGenerated.length; j++) {
                dates[j%24] = applianceConsumption.get(j + i)[0];
                energyGenerated[j] = Float.parseFloat(applianceConsumption.get(j + i)[20]);
            }

            // get the daily energy consumption per appliance (used for random operation) and set best as the current
            float[][] dayHourlyConsumption = new float[24][19];
            float[][] bestDayHourlyConsumption = new float[24][19];
            for (int j = i; j < i + 24; j++) {
                for (int k = 0; k < 19; k++) {
                    dayHourlyConsumption[j % 24][k] = consumptions[j][k];
                    bestDayHourlyConsumption[j % 24][k] = consumptions[j][k];
                }
            }

            // calculate current total import and exported energy
            float bestImportedExported = calculateImportedExported(dayHourlyConsumption,
                    energyGenerated, averageConsumption, 1, rules, dates);

            // for each day random allocate n times and get the lowest import/export
            for (int k = 0; k < iterations; k++) {

                // set an array with 0 (used to random allocate appliance consumption)
                dates = new String[24];
                dayHourlyConsumption = new float[24][19];
                for (float[] floats : dayHourlyConsumption) {
                    Arrays.fill(floats, 0.0F);
                }

                // counter to count 24 hours of the day
                int counter = 0;

                // iterate above every hour of the day
                for (int j = i; j < i + 24; j++) {

                    dates[j%24] = applianceConsumption.get(j)[0];

                    // hourly random allocation of devices in a day
                    for (int l = 0; l < 19; l++) {
                        if (bestDayHourlyConsumption[j % 24][l] != 0) {
                            int rnum = rn.nextInt(24);
                            if (dayHourlyConsumption[rnum][l] + bestDayHourlyConsumption[j % 24][l]
                                    <= maxConsumption[l]){
                                dayHourlyConsumption[rnum][l] += bestDayHourlyConsumption[j % 24][l];
                            }
                            else{
                                for (;;){
                                    rnum = rn.nextInt(24);
                                    if (dayHourlyConsumption[rnum][l] + bestDayHourlyConsumption[j % 24][l]
                                            <= maxConsumption[l]){
                                        dayHourlyConsumption[rnum][l] += bestDayHourlyConsumption[j % 24][l];
                                        break;
                                    }
                                }
                            }


                        }
                    }

                    // if hour = 23 (last hour of the day) write results in list
                    if (counter == 23) {

                        // calculate total imported and exported energy
                        float totalImportedExported = calculateImportedExported(dayHourlyConsumption,
                                energyGenerated, averageConsumption, 1, rules, dates);

                        // if total import/export is less than the previous save the day hourly energy consumption
                        if (totalImportedExported < bestImportedExported) {

                        // save the new appliance consumption per hour
                        bestDayHourlyConsumption = new float[24][19];
                        for (int l = 0; l < dayHourlyConsumption.length; l++) {
                            System.arraycopy(dayHourlyConsumption[l], 0, bestDayHourlyConsumption[l],
                                    0, dayHourlyConsumption[l].length);
                        }

                        // set the new import/export energy
                        bestImportedExported = totalImportedExported;
                        }
                    }
                    // increase the counter
                    else {
                        counter++;
                    }
                }
            }

            // add 24 results (one per hour) to list
            energyConsumptions.addAll(Arrays.asList(bestDayHourlyConsumption));
        }

        List<String[]> output = new ArrayList<>();
        for (int i = 0; i < energyConsumptions.size(); i++) {
            String[] array = new String[21];
            array[0] = applianceConsumption.get(i)[0];
            for (int j = 0; j < energyConsumptions.get(i).length; j++) {
                array[j + 1] = String.valueOf(energyConsumptions.get(i)[j]);
            }
            array[20] = applianceConsumption.get(i)[20];
            output.add(array);
        }

        // write results in csv file
        File randomTotalFile = new File("RandomTotalConsumptionPerHour.csv");
        FileWriter totalWriter = new FileWriter(randomTotalFile);
        for (int i = 0; i < energyConsumptions.size(); i++) {
            float[] row = energyConsumptions.get(i);
            float total = 0.0F;
            for (int j = 0; j < row.length; j++) {
                total += (row[j] * averageConsumption[j]);
            }
            totalWriter.write(applianceConsumption.get(i)[0] + "," + total +
                    "," + applianceConsumption.get(i)[20] + "\n");
        }
        totalWriter.close();

        return output;

    }

    // function to create on/off dataset if energy consumption > 0
    public static void convertToBinary(ImportedData data) {

        List<String[]> hourlyEnergyConsumptions = data.getHourlyEnergyConsumption();

        List<String[]> hourlyBinaryData = new ArrayList<>();

        for (String[] hourlyEnergyConsumption : hourlyEnergyConsumptions) {
            String[] binaryHourConsumption = new String[21];
            binaryHourConsumption[0] = hourlyEnergyConsumption[0];
            for (int j = 1; j < hourlyEnergyConsumption.length - 1; j++) {
                if (!hourlyEnergyConsumption[j].equals("0.0")) {
                    binaryHourConsumption[j] = "1.0";
                } else {
                    binaryHourConsumption[j] = "0.0";
                }
            }
            binaryHourConsumption[20] = hourlyEnergyConsumption[20];
            hourlyBinaryData.add(binaryHourConsumption);
        }

        data.setBinaryEnergyConsumption(hourlyBinaryData);

    }

    public static List<String[]> bruteForceBinarySolution(List<String[]> applianceConsumption,
                                                          float[] averageConsumption) throws IOException {

        List<String[]> solution = new ArrayList<>();

        for (int i = 0; i < applianceConsumption.size(); i += 24) {

            // copy the consumptions of the day
            String[][] day = new String[24][21];
            String[][] tempDay = new String[24][21];
            for (int j = 0; j < day.length; j++) {
                for (int k = 0; k < day[j].length; k++) {
                    day[j][k] = applianceConsumption.get(j + i)[k];
                    tempDay[j][k] = applianceConsumption.get(j + i)[k];
                }
            }

            // brute force reallocate to find optimal solution
            for (int k = 1; k < day[0].length - 1; k++) {
                for (int pos = 0; pos < day.length; pos++) {
                    for (int j = 1; j < day.length - pos - 1; j++) {
                        String tmp = tempDay[pos][k];
                        tempDay[pos][k] = tempDay[pos + j][k];
                        tempDay[pos + j][k] = tmp;

                        if (checkRedGreenBinary(day, tempDay, averageConsumption)) {
                            String temp = day[pos][k];
                            day[pos][k] = day[pos + j][k];
                            day[pos + j][k] = temp;
                        } else {
                            String tmp1 = tempDay[pos + j][k];
                            tempDay[pos + j][k] = tempDay[pos][k];
                            tempDay[pos][k] = tmp1;
                        }
                    }
                }

                for (int pos = day.length - 1; pos > 0; pos--) {
                    for (int j = 1; j < pos - 1; j++) {
                        String tmp = tempDay[pos][k];
                        tempDay[pos][k] = tempDay[pos - j][k];
                        tempDay[pos - j][k] = tmp;

                        if (checkRedGreenBinary(day, tempDay, averageConsumption)) {
                            String temp = day[pos][k];
                            day[pos][k] = day[pos - j][k];
                            day[pos - j][k] = temp;
                        } else {
                            String tmp1 = tempDay[pos - j][k];
                            tempDay[pos - j][k] = tempDay[pos][k];
                            tempDay[pos][k] = tmp1;
                        }
                    }
                }

            }

            for (int k = day[0].length - 1; k > 0; k--) {
                for (int pos = 0; pos < day.length; pos++) {
                    for (int j = 1; j < day.length - pos - 1; j++) {
                        String tmp = tempDay[pos][k];
                        tempDay[pos][k] = tempDay[pos + j][k];
                        tempDay[pos + j][k] = tmp;

                        if (checkRedGreenBinary(day, tempDay, averageConsumption)) {
                            String temp = day[pos][k];
                            day[pos][k] = day[pos + j][k];
                            day[pos + j][k] = temp;
                        } else {
                            String tmp1 = tempDay[pos + j][k];
                            tempDay[pos + j][k] = tempDay[pos][k];
                            tempDay[pos][k] = tmp1;
                        }
                    }
                }

                for (int pos = day.length - 1; pos > 0; pos--) {
                    for (int j = 1; j < pos - 1; j++) {
                        String tmp = tempDay[pos][k];
                        tempDay[pos][k] = tempDay[pos - j][k];
                        tempDay[pos - j][k] = tmp;

                        if (checkRedGreenBinary(day, tempDay, averageConsumption)) {
                            String temp = day[pos][k];
                            day[pos][k] = day[pos - j][k];
                            day[pos - j][k] = temp;
                        } else {
                            String tmp1 = tempDay[pos - j][k];
                            tempDay[pos - j][k] = tempDay[pos][k];
                            tempDay[pos][k] = tmp1;
                        }
                    }
                }

            }

            // add results to solution list
            Collections.addAll(solution, day);
        }

        File bruteTotalFile = new File("BruteForceBinaryTotalConsumptionPerHour.csv");
        FileWriter bruteWriter = new FileWriter(bruteTotalFile);
        for (String[] row : solution) {

            for (int j = 0; j < row.length - 1; j++) {
                bruteWriter.write(row[j] + ",");
            }
            bruteWriter.write(row[20] + "\n");
        }
        bruteWriter.close();

        return solution;
    }

    // function to calculate total import/export energy
    public static boolean checkRedGreenBinary(String[][] day, String[][] tempDay, float[] averageConsumption) {
        // variable to save total imported/exported energy of the day
        float totalDayImportedExported = 0.0F, totalTempDayImportedExported = 0.0F;
        for (int i = 0; i < day.length; i++) {

            // calculate hour consumption
            float hourDay = 0.0F, hourTempDay = 0.0F;
            for (int j = 1; j < day[i].length - 1; j++) {
                hourDay += (Float.parseFloat(day[i][j]) * averageConsumption[j - 1]);
                hourTempDay += (Float.parseFloat(tempDay[i][j]) * averageConsumption[j - 1]);
            }

            // add the imported/exported value to total of the day
            totalDayImportedExported += Math.abs(Float.parseFloat(day[i][20]) - hourDay);
            totalTempDayImportedExported += Math.abs(Float.parseFloat(tempDay[i][20]) - hourTempDay);
        }

        return totalTempDayImportedExported < totalDayImportedExported;
    }

    public static List<String[]> bruteForceSolution(List<String[]> applianceConsumption,
                                                    float[] maxConsumption, List<String[]> rules) throws IOException {

        List<String[]> solution = new ArrayList<>();

        for (int i = 0; i < applianceConsumption.size(); i += 24) {

            // copy the consumptions of the day
            String[][] day = new String[24][21];
            for (int j = 0; j < day.length; j++) {
                System.arraycopy(applianceConsumption.get(j + i), 0, day[j], 0, day[j].length);
            }

            Stack<String[][]> stack = new Stack<>();
            stack.push(day);

            // brute force reallocate to find optimal solution
            for (int k = 1; k < day[0].length - 1; k++) {

                for (int pos = 0; pos < day.length; pos++) {
                    String[][] check = new String[24][21];
                    for (int c = 0; c < check.length; c++) {
                        check[c] = stack.peek()[c].clone();
                    }
                    String tmp = check[pos][k];
                    check[pos][k] = "0.0";
                    for (int j = 1; j < day.length - pos - 1; j++) {
                        String[][] checkTemp = new String[24][21];
                        for (int c = 0; c < checkTemp.length; c++) {
                            checkTemp[c] = check[c].clone();
                        }
                        if ((Float.parseFloat(checkTemp[pos + j][k]) + Float.parseFloat(tmp))
                                <= maxConsumption[k - 1]) {
                            checkTemp[pos + j][k] = String.valueOf((Float.parseFloat(checkTemp[pos + j][k])
                                    + Float.parseFloat(tmp)));
                            if (checkRedGreen(stack.peek(), checkTemp, rules)) {
                                stack.push(checkTemp);
                            }
                        }
                    }
                }

                for (int pos = day.length - 1; pos > 0; pos--) {
                    String[][] check = new String[24][21];
                    for (int c = 0; c < check.length; c++) {
                        check[c] = stack.peek()[c].clone();
                    }
                    String tmp = check[pos][k];
                    check[pos][k] = "0.0";
                    for (int j = 1; j < pos - 1; j++) {
                        String[][] checkTemp = new String[24][21];
                        for (int c = 0; c < checkTemp.length; c++) {
                            checkTemp[c] = check[c].clone();
                        }
                        if ((Float.parseFloat(checkTemp[pos - j][k]) + Float.parseFloat(tmp))
                                <= maxConsumption[k - 1]) {
                            checkTemp[pos - j][k] = String.valueOf((Float.parseFloat(checkTemp[pos - j][k])
                                    + Float.parseFloat(tmp)));
                            if (checkRedGreen(stack.peek(), checkTemp, rules)) {
                                stack.push(checkTemp);
                            }
                        }
                    }
                }
            }



            String[][] daySolution = stack.peek();

            // add results to solution list
            Collections.addAll(solution, daySolution);
        }

        File bruteTotalFile = new File("BruteForceTotalConsumptionPerHour.csv");
        //bruteTotalFile.createNewFile();
        FileWriter bruteWriter = new FileWriter(bruteTotalFile);
        for (String[] row : solution) {

            for (int j = 0; j < row.length - 1; j++) {
                bruteWriter.write(row[j] + ",");
            }
            bruteWriter.write(row[20] + "\n");
        }
        bruteWriter.close();

        return solution;
    }

    // function to calculate total import/export energy
    public static boolean checkRedGreen(String[][] day, String[][] tempDay, List<String[]> rules) {
        // variable to save total imported/exported energy of the day
        float totalImpExp = 0.0F, totalTempImpExp = 0.0F;
        int comfortErrorDay = 0, comfortErrorTempDay = 0;
        for (int i = 0; i < day.length; i++) {

            // calculate hour consumption
            float hourConsumption = 0.0F, hourTempConsumption = 0.0F;
            for (int j = 1; j < day[i].length -1; j++) {
                hourConsumption += Float.parseFloat(day[i][j]);
                hourTempConsumption += Float.parseFloat(tempDay[i][j]);

                // Consider user comfort
                // if not comment out the following code until the next comment
                ////////////////////////////////////////////////////////////////////
                for (int k = 0; k < rules.size(); k++) {
                    int appliance = Integer.parseInt(rules.get(k)[1]);
                    if (rules.get(k)[0].equals(day[i][0]) && appliance == j) {
                        if (day[i][appliance].equals("0.0")) {
                            comfortErrorDay += 10;
                        }
                        if (tempDay[i][appliance].equals("0.0")) {
                            comfortErrorTempDay += 10;
                        }
                    }
                }
                ///////////////////////////////////////////////////////////////////
            }

            // add the imported/exported value to total of the day
//            if (hourTempConsumption > energyGenerated[i])
            totalImpExp += Math.abs(Float.parseFloat(day[i][20]) - hourConsumption);
            totalTempImpExp += Math.abs(Float.parseFloat(day[i][20]) - hourTempConsumption);
        }

        float d = (float) ((totalImpExp * 75) + (comfortErrorDay * 0.25));
        float td = (float) ((totalTempImpExp * 75) + (comfortErrorTempDay * 0.25));

        // return the value
        return td < d;
    }
}
