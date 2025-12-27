package dataCollection;

import java.util.ArrayList;
import java.util.Random;

public class Parameters {
    // Reference parameters
<<<<<<< HEAD
    static double totalKinases = 50;
    static double totalPhosphatases = 50;
    static double timestep = 0.01;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
=======
    static double totalKinases = 100;
    static double totalPhosphatases = 100;
    static double timestep = 0.01;
>>>>>>> 985c16162676e91fe1a44ba03f60243433266432
    static double patchLength = 0.5;
    static double dPIP = 3;
    static double alphaEnzyme = 0.2;
    static double k_mkon = 0.01;      // kinase on-rate
    static double k_koff = 0.05;      // kinase off-rate
    static double p_mkon = 0.05;      // phosphatase on-rate
    static double p_koff = 0.01;      // phosphatase off-rate
    static double k_mkcat = 10.0;     // kinase catalytic rate
    static double k_mKm = 0.5;       // kinase Michaelis constant
    static double p_mkcat = 15;     // phosphatase catalytic rate
    static double p_mKm = 0.5;

    static Random random = new Random();

//    public static void main(String[] args) {
//        System.out.println(singleVariables(10).get(0));
//    }

    /// We are fixing most parameters and only allowing a few to change
    public static ArrayList<Double> getPerimeters() {
        ArrayList<Double> parameterSet = new ArrayList<>();
        int perturbFactor = 10;
        parameterSet.add(random.nextDouble( 0, 1000));
        parameterSet.add(random.nextDouble(0, 1000));
        parameterSet.add(timestep);
        parameterSet.add(patchLength);
//        parameterSet.add(dPIP + random.nextDouble(-1, 1));
        parameterSet.add(dPIP);
//        parameterSet.add(random.nextDouble(alphaEnzyme / perturbFactor, alphaEnzyme * perturbFactor));
        parameterSet.add(alphaEnzyme);
        parameterSet.add(random.nextDouble(k_mkon / perturbFactor, k_mkon * perturbFactor));
        parameterSet.add(random.nextDouble(k_koff / perturbFactor, k_koff * perturbFactor));
        parameterSet.add(random.nextDouble(p_mkon / perturbFactor, p_mkon * perturbFactor));
        parameterSet.add(random.nextDouble(p_koff / perturbFactor, p_koff * perturbFactor));
//        parameterSet.add(random.nextDouble(k_mkcat / perturbFactor, k_mkcat * perturbFactor));
//        parameterSet.add(random.nextDouble(k_mKm / perturbFactor, k_mKm * perturbFactor));
//        parameterSet.add(random.nextDouble(p_mkcat / perturbFactor, p_mkcat * perturbFactor));
//        parameterSet.add(random.nextDouble(p_mKm / perturbFactor, p_mKm * perturbFactor));
        parameterSet.add(k_mkcat);
        parameterSet.add(k_mKm);
        parameterSet.add(p_mkcat);
        parameterSet.add(p_mKm);
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

    public static ArrayList<Double> getFixedParameters() {
        ArrayList<Double> parameterSet = new ArrayList<>();
        double totalKinases = 50;
        double totalPhosphatases = 50;
        double timestep = 0.01;
        double patchLength = 0.5;
        double dPIP = 2.0;
        double alphaEnzyme = 0.2;
        double k_mkon = 0.01;      // kinase on-rate
        double k_koff = 0.05;      // kinase off-rate
        double p_mkon = 0.05;      // phosphatase on-rate
        double p_koff = 0.01;      // phosphatase off-rate
        double k_mkcat = 10;     // kinase catalytic rate
        double k_mKm = 0.5;       // kinase Michaelis constant
        double p_mkcat = 15;     // phosphatase catalytic rate
        double p_mKm = 0.5;
        parameterSet.add(totalKinases);
        parameterSet.add(totalPhosphatases);
        parameterSet.add(timestep);
        parameterSet.add(patchLength);
        parameterSet.add(dPIP);
        parameterSet.add(alphaEnzyme);
        parameterSet.add(k_mkon);
        parameterSet.add(k_koff);
        parameterSet.add(p_mkon);
        parameterSet.add(p_koff);
        parameterSet.add(k_mkcat);
        parameterSet.add(k_mKm);
        parameterSet.add(p_mkcat);
        parameterSet.add(p_mKm);

        return parameterSet;
    }

    public static ArrayList<ArrayList<Double>> getPerimeterFixedParameters(int runs, int reps) {
        ArrayList<ArrayList<Double>> parameterSet = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            ArrayList<Double> temp = getPerimeters();
            for (int j = 0; j < reps; j++) {
                parameterSet.add(temp);
            }
        }
        return  parameterSet;
    }
}
