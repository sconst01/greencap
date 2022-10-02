package cs.ucy.ac.cy;

import java.io.IOException;

import com.opencsv.exceptions.CsvException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, CsvException {

        // set the input variable as dev for development environment or as prod for production environment
        // and create data object
        ImportedData data = ImportedData.importData("prod");

        // if 1 it will get rules from API else it will get rules from file
        int getFromAPI = 1; 
        //int getFromAPI = 0; //perissotero gia eukolia mas sta peiramata tou weekly dataset
        data.retrieveRules(getFromAPI);

        // if method = 1 the functions will run with real consumption
        // else the functions will run with binary (on/off) data
        int method = 1;

        // start the program
        App.start(data, method);

    }
}
