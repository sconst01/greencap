package cs.ucy.ac.cy;

import java.io.IOException;
import java.util.List;

public class PopulationComfort {

    // object parameters init
    private final int popSize;
    private ChromosomeComfort[] chromosomes;
    private float fittestScore = 0.0F;
    private float fittestExported = 0.0F;
    private float fittestImported = 0.0F;
    private float fittestConsumption = 0.0F;
    private float fittestProduction = 0.0F;
    private float fittestComfortCost = 0.0F;
    private int fittestIndex = -1;

    // population object constructor
    public PopulationComfort(int popSize, List<String[]> energyConsumptions,
                             float[] maxConsumption, float[] averageConsumption, List<String[]> rules) throws IOException {
        super();
        this.popSize = popSize;
        this.chromosomes = new ChromosomeComfort[popSize];

        // create a first population pool
        for (int i = 0; i < popSize; i++) {
            chromosomes[i] = new ChromosomeComfort(energyConsumptions, maxConsumption, averageConsumption, rules);
        }
    }

    // get the fittest individual (individual with the lowest fitness)
    public void getFittest() {
        float maxFit = Float.MAX_VALUE;
        int maxFitIndex = 0;
        for (int i = 0; i < chromosomes.length; i++) {
            if (chromosomes[i].getFitness() < maxFit) {
                maxFit = chromosomes[i].getFitness();
                maxFitIndex = i;
            }
        }
        fittestScore = chromosomes[maxFitIndex].getFitness();
        fittestImported = chromosomes[maxFitIndex].getImportedEnergy();
        fittestExported = chromosomes[maxFitIndex].getExportedEnergy();
        fittestConsumption = chromosomes[maxFitIndex].getTotalConsumption();
        fittestProduction = chromosomes[maxFitIndex].getTotalProduction();
        fittestComfortCost = chromosomes[maxFitIndex].getComfortCost();
        fittestIndex = maxFitIndex;

    }

    // get index of the least fit individual
    public int getLeastFittestIndex() {
        float minFitVal = Float.MIN_VALUE;
        int minFitIndex = 0;
        for (int i = 0; i < chromosomes.length; i++) {
            if (chromosomes[i].getFitness() > minFitVal) {
                minFitVal = chromosomes[i].getFitness();
                minFitIndex = i;
            }
        }
        return minFitIndex;
    }

    // calculate fitness of each individual
    public void calculateFitness(List<String[]> rules) {

        for (ChromosomeComfort chromosome : chromosomes) {
            chromosome.calcFitness(rules);
        }
        getFittest();
    }

    // population size getter
    public int getPopSize() {
        return popSize;
    }

    // individuals getter
    public ChromosomeComfort[] getChromosomes() {
        return chromosomes;
    }

    // fittest score getter
    public float getFittestScore() {
        return fittestScore;
    }

    public float getFittestExported() {
        return fittestExported;
    }

    public float getFittestImported() {
        return fittestImported;
    }

    public float getFittestConsumption() {
        return fittestConsumption;
    }

    public float getFittestProduction() {
        return fittestProduction;
    }

    public float getFittestComfortCost() {
        return fittestComfortCost;
    }

    public int getFittestIndex() {
        return fittestIndex;
    }
}
