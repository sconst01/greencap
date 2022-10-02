package cs.ucy.ac.cy;

import java.io.IOException;
import java.util.List;

public class Population {

    // object parameters init
    private final int popSize;
    private Chromosome[] chromosomes;
    private float fittestScore = 0.0F;
    private float fittestExported = 0.0F;
    private float fittestImported = 0.0F;
    private float fittestConsumption = 0.0F;
    private float fittestProduction = 0.0F;
    private int fittestIndex = -1;

    // population object constructor
    public Population(int popSize, List<String[]> energyConsumptions, float[] maxConsumption,
                      float[] averageConsumption, List<String[]> rules) throws IOException {
        super();
        this.popSize = popSize;
        this.chromosomes = new Chromosome[popSize];

        // create a first population pool
        for (int i = 0; i < popSize; i++) {
            chromosomes[i] = new Chromosome(energyConsumptions, maxConsumption, averageConsumption, rules);
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
    public void calculateFitness() {

        for (Chromosome chromosome : chromosomes) {
            chromosome.calcFitness();
        }
        getFittest();
    }

    public int getPopSize() {
        return popSize;
    }

    // individuals getter
    public Chromosome[] getChromosomes() {
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

    public int getFittestIndex() {
        return fittestIndex;
    }
}
