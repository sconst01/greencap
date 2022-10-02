package cs.ucy.ac.cy;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.SECONDS;

// object to save the data
public class ImportedData {

    // object variables
    private final List<String[]> peakHours;
    private List<String[]> energyConsumption;
    private final List<String[]> solarGen;
    private List<String[]> hourlyEnergyConsumption;
    private List<String[]> binaryEnergyConsumption;
    private List<String[]> rules;

    // object constructor
    public ImportedData() {
        this.peakHours = null;
        this.energyConsumption = null;
        this.solarGen = null;
        this.hourlyEnergyConsumption = null;
        this.binaryEnergyConsumption = null;
        this.rules = null;
    }

    // object constructor
    public ImportedData(List<String[]> peakHours, List<String[]> energyConsumption, List<String[]> solarGen) {
        this.peakHours = peakHours;
        this.energyConsumption = energyConsumption;
        this.solarGen = solarGen;
        this.hourlyEnergyConsumption = null;
        this.binaryEnergyConsumption = null;
        this.rules = null;
    }

    // get peak hours list
    public List<String[]> getPeakHours() {
        return peakHours;
    }

    // get energy consumption list
    public List<String[]> getEnergyConsumption() {
        return energyConsumption;
    }

    // get solar generation list
    public List<String[]> getSolarGen() {
        return solarGen;
    }

    // get appliance energy consumption per hour
    public List<String[]> getHourlyEnergyConsumption() {
        return hourlyEnergyConsumption;
    }

    // set appliance energy consumption per hour
    public void setHourlyEnergyConsumption(List<String[]> hourlyEnergyConsumption) {
        this.hourlyEnergyConsumption = hourlyEnergyConsumption;
    }

    // get appliance energy consumption per hour (on/off)
    public List<String[]> getBinaryEnergyConsumption() {
        return binaryEnergyConsumption;
    }

    // set appliance binary energy consumption per hour (on/off)
    public void setBinaryEnergyConsumption(List<String[]> binaryEnergyConsumption) {
        this.binaryEnergyConsumption = binaryEnergyConsumption;
    }

    public List<String[]> getRules() {
        return rules;
    }

    public static ImportedData importData(String env) {

        // paths for the data according to the environment
        String peakHoursPath = env.equals("dev")
                ? "/Users/nicolaspolycarpou/Documents/GreenCap/Dataset/US_Peak_Hours/EIA930_BALANCE_2016_NYIS.csv"
                : env.equals("prod")
                ? "/home/user/Desktop/GreenCapContent/Data/EIA930_BALANCE_2016_NYIS.csv"
                : ".";
        String energyConsumptionPath = env.equals("dev")
                ? "/Users/nicolaspolycarpou/Documents/GreenCap/Dataset/Home_Data_fixed/HomeEnergyConsumption2016.csv"
                : env.equals("prod")
                ? "/home/user/Desktop/GreenCapContent/Data/HomeEnergyConsumption2016.csv" //normal dataset
                //? "/home/user/Desktop/GreenCapContent/Data/HomeEnergyConsumption2016-1week.csv" //weekly dataset
                : ".";
        String solarGenPath = env.equals("dev")
                ? "/Users/nicolaspolycarpou/Documents/GreenCap/Dataset/Home_Solar_Gen/Solar_Data.csv"
                : env.equals("prod")
                ? "/home/user/Desktop/GreenCapContent/Data/Solar_Data.csv"
                : ".";

        ImportedData data = new ImportedData();

        try {

            // filereader to read the data
            FileReader peakHoursFilereader = new FileReader(peakHoursPath);
            FileReader energyConsumptionFilereader = new FileReader(energyConsumptionPath);
            FileReader solarGenFilereader = new FileReader(solarGenPath);

            // use csvreader to read the csv files
            CSVReader peakHoursCsvReader = new CSVReaderBuilder(peakHoursFilereader)
                    .withSkipLines(1)
                    .build();
            CSVReader energyConsumptionCsvReader = new CSVReaderBuilder(energyConsumptionFilereader)
                    .withSkipLines(1)
                    .build();
            CSVReader solarGenCsvReader = new CSVReaderBuilder(solarGenFilereader)
                    .withSkipLines(1)
                    .build();

            // create the data object
            data = new ImportedData(peakHoursCsvReader.readAll(), energyConsumptionCsvReader.readAll(), solarGenCsvReader.readAll());

        } catch (Exception e) {
            System.out.println("File not found!!!");
        }

        return data;
    }

    public void reallocationToNonProduction() throws IOException {

        // list to save output
        List<String[]> output = new ArrayList<>();

        // iterate day by day
        for (int i = 0; i < this.energyConsumption.size(); i+=(24*60)){

            String[][] day = new String[24*60][20];
            for (int j = i; j < i + (10*60); j++){
                System.arraycopy(this.energyConsumption.get(j), 0, day[j % (24 * 60)], 0, day[j % (24 * 60)].length);
            }
            for (int j = i + (10*60); j < (i + 15*60); j++){
                day[j % (24*60)][0] = this.energyConsumption.get(j)[0];
                for (int k = 1; k < day[j % (24*60)].length; k++){
                    if (Float.parseFloat(this.energyConsumption.get(j)[k]) < Float.parseFloat(this.energyConsumption.get(j + (7*60))[k]))
                        day[j % (24*60)][k] = this.energyConsumption.get(j)[k];
                    else
                        day[j % (24*60)][k] = this.energyConsumption.get(j + (7*60))[k];
                }
            }
            for (int j = i + (15*60); j < (i + 17*60); j++){
                System.arraycopy(this.energyConsumption.get(j), 0, day[j % (24 * 60)], 0, day[j % (24 * 60)].length);
            }
            for (int j = i + (17*60); j < (i + 22*60); j++){
                day[j % (24*60)][0] = this.energyConsumption.get(j)[0];
                for (int k = 1; k < day[j % (24*60)].length; k++){
                    if (Float.parseFloat(this.energyConsumption.get(j)[k]) > Float.parseFloat(this.energyConsumption.get(j - (7*60))[k]))
                        day[j % (24*60)][k] = this.energyConsumption.get(j)[k];
                    else
                        day[j % (24*60)][k] = this.energyConsumption.get(j - (7*60))[k];
                }
            }
            for (int j = i + (22*60); j < (i + 24*60); j++){
                System.arraycopy(this.energyConsumption.get(j), 0, day[j % (24 * 60)], 0, day[j % (24 * 60)].length);
            }

            Collections.addAll(output, day);
        }

        // write results in csv file
        File applianceFile = new File("HomeEnergyConsumption2016.csv");
        //applianceFile.createNewFile();
        FileWriter applianceWriter = new FileWriter(applianceFile);
        for (String[] strings : output) {
            for (int j = 0; j < strings.length - 1; j++) {
                applianceWriter.write(strings[j] + ",");
            }
            applianceWriter.write(strings[strings.length - 1] + "\n");
        }
        applianceWriter.close();

        this.energyConsumption = output;
    }

    public void retrieveRules(int getFromAPI) throws IOException, InterruptedException, CsvException {

        if (getFromAPI == 1){

            // get rules from the api
            this.rules = ApiCalls.getRulesFromAPI();

            // write rules to csv file
            File rulesFile = new File("rules.csv");
            FileWriter rulesWriter = new FileWriter(rulesFile);
            rulesWriter.write("Date & Time,Appliance\n");
            for (String[] rule : rules) {
                rulesWriter.write(rule[0] +  "," + rule[1] + "\n");
            }
            rulesWriter.close();
        }

        // read rules from file
        else {
            //String rulesPath = "C:\\Users\\CPS\\Desktop\\GreenCap\\Data\\rules.txt"; //for normal dataset
        	String rulesPath = "/home/user/Desktop/GreenCapContent/Data/rules-1week.txt"; //for weekly dataset(this is only used if you pass the 0 parameter in this function)
            FileReader rulesFilereader = new FileReader(rulesPath);
            CSVReader rulesCsvReader = new CSVReaderBuilder(rulesFilereader)
                .withSkipLines(1)
                .build();
            this.rules = rulesCsvReader.readAll();
        }
    }
}
