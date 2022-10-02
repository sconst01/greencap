package cs.ucy.ac.cy;

import java.io.IOException;
import java.util.*;

public class ChromosomeComfort implements Cloneable {

    // object parameters init
    private float fitness;
    private float exportedEnergy;
    private float importedEnergy;
    private float totalConsumption;
    private float totalProduction;
    private float comfortCost;
    private List<String[]> genes;

    // chromosome constructor with the consumptions
    public ChromosomeComfort(List<String[]> energyConsumptions, float[] maxConsumption,
                             float[] averageConsumption, List<String[]> rules) throws IOException {

        // set genes using random allocation of appliances
        this.genes = Algorithms.random(1, energyConsumptions, maxConsumption, averageConsumption, rules);
        this.fitness = 0.0F;
        this.exportedEnergy = 0.0F;
        this.importedEnergy = 0.0F;
        this.totalConsumption = 0.0F;
        this.totalProduction = 0.0F;
        this.comfortCost = 0.0F;
    }

    public ChromosomeComfort(List<String[]> list) {
        this.genes = list;
        this.fitness = 0.0F;
        this.exportedEnergy = 0.0F;
        this.importedEnergy = 0.0F;
        this.totalConsumption = 0.0F;
        this.totalProduction = 0.0F;
        this.comfortCost = 0.0F;
    }

    // chromosome constructor with chromosome
    public ChromosomeComfort(ChromosomeComfort c) {
        this.genes = c.getGenes();
        this.fitness = c.getFitness();
        this.exportedEnergy = c.getExportedEnergy();
        this.importedEnergy = c.getImportedEnergy();
        this.totalConsumption = c.getTotalConsumption();
        this.totalProduction = c.getTotalProduction();
        this.comfortCost = c.getComfortCost();
    }

    // calculate fitness
    public void calcFitness(List<String[]> rules) {

        float totalImported = 0.0F;
        float totalExported = 0.0F;
        float totalConsumption = 0.0F;
        float totalProduction = 0.0F;
        float comfortCost = 0.0F;

        for (int i = 0; i < this.genes.size(); i++) {

            // calculate hour consumption
            float hourConsumption = 0.0F;
            for (int j = 0; j < this.genes.get(i).length - 2; j++) {
                float temp = Float.parseFloat(this.genes.get(i)[j + 1]);
                hourConsumption += (temp);
                for (int k = 0; k < rules.size(); k++) {
                    int appliance = Integer.parseInt(rules.get(k)[1]);
                    if (rules.get(k)[0].equals(this.genes.get(i)[0]) && appliance == j) {
                        if (this.genes.get(i)[appliance].equals("0.0")) {
                            comfortCost += 1000;
                        }
                    }
                }
            }

            totalConsumption += hourConsumption;
            totalProduction += Float.parseFloat(this.genes.get(i)[20]);

            // if consumption is greater than production energy equals imported
            if (hourConsumption > Float.parseFloat(this.genes.get(i)[20]))
                totalImported += (hourConsumption - Float.parseFloat(this.genes.get(i)[20]));
            else
                totalExported += (Float.parseFloat(this.genes.get(i)[20]) - hourConsumption);
        }

        // set values
//        this.fitness = totalImported;
        this.fitness = (float) (totalImported * 0.75) + (float) (0.25 * comfortCost);
        this.importedEnergy = totalImported;
        this.exportedEnergy = totalExported;
        this.totalConsumption = totalConsumption;
        this.totalProduction = totalProduction;
        this.comfortCost = comfortCost;
    }

    public void fixConsumptions(float[][] dailyConsumptions, float[] maxConsumption) {

        // threshold to check if two floats are equal
        final float THRESHOLD = (float) .00001;

        List<String[]> output = new ArrayList<>();

        // iterate day per day
        for (int i = 0; i < this.genes.size(); i += 24) {

            // copy in array the current day
            String[][] day = new String[24][21];
            for (int j = i; j < i + 24; j++) {
                System.arraycopy(this.genes.get(j), 0, day[j % 24], 0, 21);
            }

           
            //find how much is the consumption for each device daily
            float[] checkConsumptions = new float[19];
            for (int j = 1; j < this.genes.get(i).length - 1; j++) {
                float counter = 0;
                for (int k = 0; k < 24; k++) {
                    counter += Float.parseFloat(day[k][j]);
                }
                checkConsumptions[j - 1] = counter;
            }

            Integer[] idx = new Integer[24];
            for (int j = 0; j < 24; j++) {
                idx[j] = j;
            }
            float[] productions = new float[24];
            for (int j = 0; j < 24; j++) {
                productions[j] = Float.parseFloat(day[j][20]);
            }

            Arrays.sort(idx, (o1, o2) -> Float.compare(productions[o1], productions[o2]));

            // fix the consumption by iterate device per device
            for (int j = 0; j < checkConsumptions.length; j++) {

                float consumptionDiff;

                if (Math.abs(checkConsumptions[j] - dailyConsumptions[i / 24][j]) < THRESHOLD) continue;
                    // if is greater than the original turn off devices
                else if (checkConsumptions[j] > dailyConsumptions[i / 24][j]) {
                    consumptionDiff = checkConsumptions[j] - dailyConsumptions[i / 24][j];
                    for (Integer integer : idx) {

                        if (day[integer][j + 1].equals("0.0")) continue;
                        else {
                            if (consumptionDiff <= 0)
                                break;

                            float currCons = Float.parseFloat(day[integer][j + 1]);
                            if (currCons <= consumptionDiff) {
                                this.genes.get(i + integer)[j + 1] = "0.0";
                                consumptionDiff -= currCons;
                            } else {
                                float tmp = currCons - consumptionDiff;
                                day[integer][j + 1] = String.valueOf(tmp);
                                break;
                            }
                        }
                    }
                }

                // if is less than the original turn on devices
                else if (checkConsumptions[j] < dailyConsumptions[i / 24][j]) {
                    consumptionDiff = dailyConsumptions[i / 24][j] - checkConsumptions[j];
                    for (int l = idx.length - 1; l >= 0; l--) {

                        if (consumptionDiff <= 0)
                            break;

                        float currCons = Float.parseFloat(day[idx[l]][j + 1]);
                        if ((currCons + consumptionDiff) >= maxConsumption[j]) {
                            day[idx[l]][j + 1] = String.valueOf(maxConsumption[j]);
                            consumptionDiff -= (maxConsumption[j] - currCons);
                        } else {
                            float tmp = currCons + consumptionDiff;
                            day[idx[l]][j + 1] = String.valueOf(tmp);
                            break;
                        }
                    }
                }
            }

            Collections.addAll(output, day);
        }

        this.genes = new ArrayList<>(output);

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ChromosomeComfort chromosome = (ChromosomeComfort) super.clone();
        chromosome.genes = new ArrayList<>();
        chromosome.genes.addAll(this.genes);
        chromosome.fitness = this.fitness;
        chromosome.importedEnergy = this.importedEnergy;
        chromosome.exportedEnergy = this.exportedEnergy;
        chromosome.totalConsumption = this.totalConsumption;
        chromosome.totalProduction = this.totalProduction;
        chromosome.comfortCost = this.comfortCost;
        return chromosome;
    }

    // fitness getter
    public float getFitness() {
        return fitness;
    }

    // exported energy getter
    public float getExportedEnergy() {
        return exportedEnergy;
    }

    // imported energy getter
    public float getImportedEnergy() {
        return importedEnergy;
    }

    // genes getter
    public List<String[]> getGenes() {
        return genes;
    }

    // total consumption getter
    public float getTotalConsumption() {
        return totalConsumption;
    }

    // total production getter
    public float getTotalProduction() {
        return totalProduction;
    }

    // total comfort cost getter
    public float getComfortCost() {
        return comfortCost;
    }
}
