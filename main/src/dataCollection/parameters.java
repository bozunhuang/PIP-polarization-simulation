package dataCollection;

import java.util.ArrayList;
import java.util.Random;

public class parameters {
    // Reference parameters
    static double totalKinases = 100;
    static double totalPhosphatases = 100;
    static double timestep = 0.01;
    static double patchLength = 0.5;
    static double dPIP = 4;
    static double alphaEnzyme = 0.2;
    static double k_mkon = 0.1;      // kinase on-rate
    static double k_koff = 0.5;      // kinase off-rate
    static double p_mkon = 0.05;      // phosphatase on-rate
    static double p_koff = 0.2;      // phosphatase off-rate
    static double k_mkcat = 10.0;     // kinase catalytic rate
    static double k_mKm = 2.0;       // kinase Michaelis constant
    static double p_mkcat = 15;     // phosphatase catalytic rate
    static double p_mKm = 0.5;

    static Random random = new Random(42);

    public static void main(String[] args) {
        System.out.println(singleVariables(10).get(0));
    }


    public static ArrayList<Double> getPerimeters() {
        ArrayList<Double> parameterSet = new ArrayList<>();
        parameterSet.add(random.nextDouble( 10, totalKinases * 10));
        parameterSet.add(random.nextDouble(10, totalPhosphatases * 10));
        parameterSet.add(timestep);
        parameterSet.add(patchLength);
        parameterSet.add(dPIP + random.nextDouble(-1, 1));
        parameterSet.add(random.nextDouble(alphaEnzyme / 2, alphaEnzyme * 2));
        parameterSet.add(random.nextDouble(k_mkon / 2, k_mkon * 2));
        parameterSet.add(random.nextDouble(k_koff / 2, k_koff * 2));
        parameterSet.add(random.nextDouble(p_mkon / 2, p_mkon * 2));
        parameterSet.add(random.nextDouble(p_koff / 2, p_koff * 2));
        parameterSet.add(random.nextDouble(k_mkcat / 2, k_mkcat * 2));
        parameterSet.add(random.nextDouble(k_mKm / 2, k_mKm * 2));
        parameterSet.add(random.nextDouble(p_mkcat / 2, p_mkcat * 2));
        parameterSet.add(random.nextDouble(p_mKm / 2, p_mKm * 2));
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

        for (int i = 0; i < paramList.size(); i++) {
            ArrayList<ArrayList<Double>> set = new ArrayList<>();
            ArrayList<Double> gradient = new ArrayList<>();
            for (int k = 0; k < steps + 1; k++) {
                double inc = paramList.get(i) / (steps * 20);

                for (int j = steps / 2; j > 0; j--) {
                    gradient.add(paramList.get(i) - j * inc);
                }

                gradient.add(paramList.get(i));

                for (int j = 0; j < steps / 2; j++) {
                    gradient.add(paramList.get(i) + j * inc);
                }
            }
            for (int j = 0; j < steps + 1; j++) {
                ArrayList<Double> temp = new ArrayList<>(paramList);
                temp.set(i, gradient.get(j));
                set.add(temp);
            }
            parameterSet.add(set);
        }
        if (parameterSet.size() != paramList.size()|| parameterSet.getFirst().size() != steps + 1 || parameterSet.getFirst().getFirst().size() != paramList.size()) {
            throw new IllegalArgumentException("Failed building parameterSet");
        }

        return parameterSet;
    }
}
