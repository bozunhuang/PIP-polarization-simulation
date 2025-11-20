package dataCollection;

import core.World;
import maps.Maps;
import tileengine.TETile;
import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static maps.Maps.NEURITE_MAP;

public class DataCollection {
    // Setup data
    private static final TETile[][] MAP = NEURITE_MAP;
    private static final int WIDTH = Maps.WIDTH;
    private static final int HEIGHT = Maps.HEIGHT;

    // Number of runs
    private static final int TOTAL_RUNS = 100;
    // Parameter set generation mode
    private static final int PERIMETERS = 0;
    private static final int GRADIENT = 1;
    private static final int FIXED_PARAM = 2;
    private static final int FIXED_AND_PERIMETERS = 3;

//    public static List<String[]> data = new ArrayList<>();
    public static List<String[]> data = Collections.synchronizedList(new ArrayList<>());

    private static String outputFileName = "";


    public static void main(String[] args) {

        // Set parameter set generation mode
        int MODE = FIXED_PARAM;

        outputFileName = "fix_param_test_1";

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
                            ArrayList<Double> params = Parameters.getPerimeters();
                            System.out.println("Starting run: " + i);
                            runSimulation(params, i);
                        } catch (Exception e) {
                            System.err.println("Error in run " + i + ": " + e.getMessage());
                        }
                    });
        }

        if  (MODE == GRADIENT) {
            ArrayList<ArrayList<ArrayList<Double>>> paramSet = Parameters.singleVariables(20);
            int i = 1;
            for (ArrayList<ArrayList<Double>> paramSubset : paramSet) {
                for (ArrayList<Double> params : paramSubset) {
                    System.out.println("Starting run: " + i);
                    runSimulation(params, i);
                    i++;
                }
            }
        }

        if   (MODE == FIXED_PARAM) {
            ArrayList<Double> params = Parameters.getFixedParameters();

            IntStream.range(0, TOTAL_RUNS)
                    .parallel()
                    .forEach(i -> {
                        try {
                            System.out.println("Starting run: " + i);
                            runSimulation(params, i);
                        } catch (Exception e) {
                            System.err.println("Error in run " + i + ": " + e.getMessage());
                        }
                    });
        }

        if (MODE == FIXED_AND_PERIMETERS) {
            int runs = 20;
            int reps = 30;
            ArrayList<ArrayList<Double>> paramSet = Parameters.getPerimeterFixedParameters(runs, reps);
            IntStream.range(0, runs * reps)
                    .parallel()
                    .forEach(i -> {
                        try {
                            System.out.println("Starting run: " + i);
                            runSimulation(paramSet.get(i), i);
                        } catch (Exception e) {
                            System.err.println("Error in run " + i + ": " + e.getMessage());
                        }
                    });
        }

        // Save data
        saveData();
    }

    private static void runSimulation(ArrayList<Double> params, int runNumber) {
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

        while (frameCount <= 5000) {
            // Update simulation status
            world.updateWorld();
            // Increase frame count
            frameCount++;
        }

        System.out.println("Completed simulation " + runNumber + "!");

        // Save data
        String[] row = new String[params.size() + 8];
        int i = 0;

        for (Double param : params) {
            row[i] = param.toString();
            i++;
        }

        row[i] = String.valueOf(world.getKinasesInSolution());
        row[i + 1] = String.valueOf(world.getPptasesInSolution());
        row[i + 2] = String.valueOf(world.getAvgBodyX());
        row[i + 3] = String.valueOf(world.getAvgOneNodeX(1));
        row[i + 4] = String.valueOf(world.getAvgOneNodeX(2));
        row[i + 5] = String.valueOf(world.getAvgOneNodeX(3));
        row[i + 6] = String.valueOf(world.getAvgOneNodeX(4));
        row[i + 7] = String.valueOf(world.getAvgAllNodeX());
        data.add(row);
    }

    private static void saveData() {
        String csvFilePath = "data/" + outputFileName + ".csv";
        String[] header = {"Total Kinase", "Total Phosphatase", "Timestep", "Patch Length", "dPIP",
                            "alphaEnzyme", "k_mkon", "k_koff", "p_mkon", "p_koff", "k_mkcat", "k_mKm",
                            "p_mkcat", "p_mKm", "Kinase in Solution", "Phosphatase in Solution",
                            "Average Body X", "Average Node 1 X","Average Node 2 X",
                            "Average Node 3 X","Average Node 4 X","Average All Nodes X"};

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
