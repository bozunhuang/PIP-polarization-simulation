package dataCollection;

import java.util.ArrayList;
import java.util.Random;

public class parameters {
    // Reference parameters
    static double totalKinases = 500;
    static double totalPhosphatases = 1000;
    static double timestep = 0.01;
    static double patchLength = 0.5;
    static double dPIP = 3;
    static double alphaEnzyme = 0.2;
    static double k_mkon = 0.3;      // kinase on-rate
    static double k_koff = 0.5;      // kinase off-rate
    static double p_mkon = 0.05;      // phosphatase on-rate
    static double p_koff = 0.2;      // phosphatase off-rate
    static double k_mkcat = 10.0;     // kinase catalytic rate
    static double k_mKm = 2.0;       // kinase Michaelis constant
    static double p_mkcat = 15;     // phosphatase catalytic rate
    static double p_mKm = 0.5;

    static Random random = new Random(42);

    public static void main(String[] args) {
        System.out.println(String.valueOf(singleVariables(10)));
    }


    public static ArrayList<Double> getPerimeters() {
        ArrayList<Double> parameterSet = new ArrayList<>();
        parameterSet.add(totalKinases + random.nextDouble(-totalKinases / 5, totalKinases / 10));
        parameterSet.add(totalPhosphatases + random.nextDouble(-totalPhosphatases / 5, totalPhosphatases / 10));
        parameterSet.add(timestep);
        parameterSet.add(patchLength);
        parameterSet.add(dPIP + random.nextDouble(-1, 1));
        parameterSet.add(alphaEnzyme + random.nextDouble(-0.05, 0.05));
        parameterSet.add(k_mkon + random.nextDouble(-0.1, 0.1));
        parameterSet.add(k_koff + random.nextDouble(-0.1, 0.1));
        parameterSet.add(p_mkon + random.nextDouble(-0.01, 0.01));
        parameterSet.add(p_koff + random.nextDouble(-0.1, 0.1));
        parameterSet.add(k_mkcat + random.nextDouble(-3, 3));
        parameterSet.add(k_mKm + random.nextDouble(-0.2, 2.0));
        parameterSet.add(p_mkcat + random.nextDouble(-3, 3));
        parameterSet.add(p_mKm + random.nextDouble(-0.1, 0.1));
        return parameterSet;
    }

    public static ArrayList<ArrayList<ArrayList<Double>>> singleVariables(int steps) {
        ArrayList<ArrayList<ArrayList<Double>>> parameterSet = new ArrayList<>();
        ArrayList<Double> paramList = new ArrayList<>();

        paramList.add(totalKinases);
        paramList.add(totalPhosphatases);
        paramList.add(timestep);
        paramList.add(patchLength);
        paramList.add(dPIP);
        paramList.add(alphaEnzyme);
        paramList.add(k_mkon);
        paramList.add(k_koff);
        paramList.add(p_mkon);
        paramList.add(p_koff);
        paramList.add(k_mkcat);
        paramList.add(k_mKm);
        paramList.add(p_mkcat);
        paramList.add(p_mKm);

        for (double param : paramList) {
            ArrayList<ArrayList<Double>> set = new ArrayList<>();
            for (int i = 0; i < steps + 1; i++) {
                ArrayList<Double> row = new ArrayList<>();
                double inc = param / (steps * 10);

                for (int j = steps / 2; j > 0; j--) {
                    row.add(param - j * inc);
                }

                row.add(param);

                for (int j = 0; j < steps / 2; j++) {
                    row.add(param + j * inc);
                }
                set.add(row);
            }
            parameterSet.add(set);
        }

        for (int j = 0; j < steps + 1; j++) {
            ArrayList<Double> row = new ArrayList<>();
            for (int i = 0; i < paramList.size(); i++) {
                row.add(preset.get(i).get(j));
            }
            parameterSet.add(row);
        }

        if (parameterSet.size() != steps + 1 || parameterSet.get(0).size() != paramList.size() || parameterSet.get(0).isEmpty()) {
            throw new IllegalArgumentException("Failed building parameterSet");
        }

        return parameterSet;
    }
}
