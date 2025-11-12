package dataCollection;

import core.World;
import tileengine.TERenderer;
import tileengine.TETile;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static maps.maps.NEURITE_MAP;

public class dataCollection {
    // Setup data
    private static final TETile[][] MAP = NEURITE_MAP;
    private static final int WIDTH = 101;
    private static final int HEIGHT = 101;

    // Number of runs
    private static final int TOTAL_RUNS = 1000;
    // Parameter set generation mode
    private static final int PERIMETERS = 0;
    private static final int ONE_VARIABLE = 1;

//    public static List<String[]> data = new ArrayList<>();
    public static List<String[]> data = Collections.synchronizedList(new ArrayList<>());

    private static String outputFileName = "";


    public static void main(String[] args) {

        // Set parameter set generation mode
        int MODE = PERIMETERS;

        outputFileName = "rand_run_1000_1";

//        if (MODE == PERIMETERS) {
//            for (int i = 0; i < runs; i++) {
//                // Load parameters
//                ArrayList<Double> params = parameters.getPerimeters();
//                System.out.println("Starting run: " + i);
//                runSimulations(params);
//                System.out.println("Done run: " + i);
//            }
//        }
        if (MODE == PERIMETERS) {
            IntStream.range(0, TOTAL_RUNS)
                    .parallel()
                    .forEach(i -> {
                        try {
                            ArrayList<Double> params = parameters.getPerimeters();
                            System.out.println("Starting run: " + i);
                            runSimulations(params, i);
                        } catch (Exception e) {
                            System.err.println("Error in run " + i + ": " + e.getMessage());
                        }
                    });

            saveData();
        }

        if  (MODE == ONE_VARIABLE) {
            ArrayList<ArrayList<ArrayList<Double>>> paramSet = parameters.singleVariables(20);
            int i = 1;
            for (ArrayList<ArrayList<Double>> paramSubset : paramSet) {
                for (ArrayList<Double> params : paramSubset) {
                    System.out.println("Starting run: " + i);
                    runSimulations(params, i);
                    i++;
                }
            }
        }

        // Save data
        saveData();
    }

    private static void runSimulations(ArrayList<Double> params, int runNumber) {
        // Calculate alphaPIP for stability check
        double timestep = params.get(2);
        double patchLength = params.get(3);
        double dPIP = params.get(4);
        double alphaPIP = dPIP * timestep / (patchLength * patchLength);

        // Check for stability
        if (alphaPIP >= 0.25) {
            System.err.println("ERROR: Diffusion will be unstable!");
            System.err.println("Reduce TIMESTEP or increase PATCH_LENGTH");
            return;
        }

        // Setup world
        World world = new World(MAP, WIDTH, HEIGHT, alphaPIP, params);

        long frameCount = 0;

        while (frameCount <= 3000){

            // Update simulation status
            world.upDateWorld();

            // Increase frame count
            frameCount++;
        }

        System.out.println("Done simulation " + runNumber + "!");

        // Save data
        String[] row = new String[params.size() + 5];
        int i = 0;

        for (Double param : params) {
            row[i] = param.toString();
            i++;
        }

        row[i] = String.valueOf(world.getKinasesInSolution());
        row[i + 1] = String.valueOf(world.getPhosphatasesInSolution());
        row[i + 2] = String.valueOf(world.getAvgSystemX());
        row[i + 3] = String.valueOf(world.getAvgDendriteX());
        row[i + 4] = String.valueOf(world.polarizationIdx());
        data.add(row);
    }

    private static void saveData() {
        String csvFilePath = "data/" + outputFileName + ".csv";
        String[] header = {"Total Kinase", "Total Phosphatase", "Timestep", "Patch Length", "dPIP",
                            "alphaEnzyme", "k_mkon", "k_koff", "p_mkon", "p_koff", "k_mkcat", "k_mKm",
                            "p_mkcat", "p_mKm", "Kinase in solution", "Phosphatase in solution",
                            "Average system X", "Average dendrite X", "Polarization index"};

        try (CSVWriter writer = new CSVWriter(new FileWriter(csvFilePath))) {
            writer.writeNext(header); // Write the header row
            writer.writeAll(data);
            System.out.println("CSV file written successfully.");
            System.out.println("Total rows written: " + data.size());
        } catch (IOException e) {
            System.err.println("Error writing CSV file: " + e.getMessage());
        }
    }
}
