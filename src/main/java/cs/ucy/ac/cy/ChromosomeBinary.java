package cs.ucy.ac.cy;

import java.io.IOException;
import java.util.*;

public class ChromosomeBinary implements Cloneable {

    // object parameters init
    private float fitness;
    private float exportedEnergy;
    private float importedEnergy;
    private float totalConsumption;
    private float totalProduction;
    private List<String[]> genes;

    // chromosome constructor with the consumptions
    public ChromosomeBinary(List<String[]> energyConsumptions, float[] averageConsumption, List<String[]> rules) throws IOException {

        // set genes using random allocation of appliances
        this.genes = Algorithms.randomBinary(1, energyConsumptions, averageConsumption, rules);
        this.fitness = 0.0F;
        this.exportedEnergy = 0.0F;
        this.importedEnergy = 0.0F;
        this.totalConsumption = 0.0F;
        this.totalProduction = 0.0F;

    }

    public ChromosomeBinary(List<String[]> list){
        this.genes = list;
        this.fitness = 0.0F;
        this.exportedEnergy = 0.0F;
        this.importedEnergy = 0.0F;
        this.totalConsumption = 0.0F;
        this.totalProduction = 0.0F;
    }

    // chromosome constructor with chromosome
    public ChromosomeBinary(ChromosomeBinary c){
        this.genes = c.getGenes();
        this.fitness = c.getFitness();
        this.exportedEnergy = c.getExportedEnergy();
        this.importedEnergy = c.getImportedEnergy();
        this.totalConsumption = c.getTotalConsumption();
        this.totalProduction = c.getTotalProduction();
    }

    // calculate fitness
    public void calcFitness(float[] averageConsumption) {

        float totalImported = 0.0F;
        float totalExported = 0.0F;
        float totalConsumption = 0.0F;
        float totalProduction = 0.0F;
        for (String[] gene : this.genes) {

            // calculate hour consumption
            float hourConsumption = 0.0F;
            for (int j = 0; j < gene.length - 2; j++) {
                hourConsumption += (Float.parseFloat(gene[j + 1]) * averageConsumption[j]);
            }

            totalConsumption += hourConsumption;
            totalProduction += Float.parseFloat(gene[20]);

            // if consumption is greater than production energy equals imported
            if (hourConsumption > Float.parseFloat(gene[20]))
                totalImported += (hourConsumption - Float.parseFloat(gene[20]));
            else
                totalExported += (Float.parseFloat(gene[20]) - hourConsumption);
        }

        // set values
        this.fitness = totalImported;
        this.importedEnergy = totalImported;
        this.exportedEnergy = totalExported;
        this.totalConsumption = totalConsumption;
        this.totalProduction = totalProduction;
    }

    public void fixConsumptions(int[][] dailyConsumptions){

        // iterate day per day
        for (int i = 0; i < this.genes.size(); i+=24){

            // find how many times devices are open in the day after gen. alg.
            int[] checkConsumptions = new int[19];
            for (int j = 1; j < this.genes.get(i).length - 1; j++){
                int counter = 0;
                for (int k = 0; k < 24; k++){

                    if (this.genes.get(i + k)[j].equals("1.0")){
                        counter++;
                    }
                }
                checkConsumptions[j - 1] = counter;
            }

            Integer[] idx = new Integer[24];
            for (int j = 0; j < 24; j++){
                idx[j] = j;
            }
            float[] productions = new float[24];
            for (int j = 0; j < 24; j++){
                productions[j] = Float.parseFloat(this.genes.get(i + j)[20]);
            }

            Arrays.sort(idx, (o1, o2) -> Float.compare(productions[o1], productions[o2]));

            // fix the consumption by iterate device per device
            for (int j = 0; j < checkConsumptions.length; j++){

                // if is greater than the original turn off devices
                if (checkConsumptions[j] > dailyConsumptions[i / 24][j]){
                    for (int k = 0; k < checkConsumptions[j] - dailyConsumptions[i / 24][j]; k++){
                        for (Integer integer : idx) {
                            if (this.genes.get(i + integer)[j + 1].equals("1.0")) {
                                this.genes.get(i + integer)[j + 1] = "0.0";
                                break;
                            }
                        }
                    }
                }

                // if is less than the original turn on devices
                else if (checkConsumptions[j] < dailyConsumptions[i / 24][j]){
                    for (int k = 0; k < dailyConsumptions[i / 24][j] - checkConsumptions[j]; k++){
                        for (int l = idx.length - 1; l >= 0; l--){
                            if (this.genes.get(i + idx[l])[j + 1].equals("0.0")) {
                                this.genes.get(i + idx[l])[j + 1] = "1.0";
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        ChromosomeBinary chromosome = (ChromosomeBinary)super.clone();
        chromosome.genes = new ArrayList<>();
        chromosome.genes.addAll(this.genes);
        chromosome.fitness = this.fitness;
        chromosome.importedEnergy = this.importedEnergy;
        chromosome.exportedEnergy = this.exportedEnergy;
        chromosome.totalConsumption = this.totalConsumption;
        chromosome.totalProduction = this.totalProduction;
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

}
