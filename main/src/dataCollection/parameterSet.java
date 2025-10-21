package dataCollection;

import java.util.HashMap;

public class parameterSet {
    public HashMap<String, Double> [] parameterMatrix;
    public static String[] variableNames = new String[]{"D_PIP", "ALPHA_ENZYME", "totalKinases", "totalPhosphatases",  "k_mkon", "k_koff", "K_mkcat", "k_mkm", "p_mkon", "p_koff", "p_mkcat", "p_mkm"};

    // Inclusive at both ends
    public static Range<Double> DPIPRange = new Range<>(2.0, 5.0, 1.0);
    public static Range<Double> ALPHAPRange = new Range<>(0.1, 2.0, 0.1);
    public static Range<Double> totalKinasesRange = new Range<>(100.0, 1090.0, 10.0);
    public static Range<Double> totalPhosphatasesRange = new Range<>(100.0, 1090.0, 10.0);
    public static Range<Double> k_mkonRange = new Range<>(0.1, 2.0, 0.1);
    public static Range<Double> k_koffRange = new Range<>(0.1, 2.0, 0.1);
    public static Range<Double> K_mkcatRange = new Range<>(0.1, 20.0, 1.0);
    public static Range<Double> k_mkmRange = new Range<>(1.0, 10.0, 1.0);
    public static Range<Double> p_mkonRange = new Range<>(0.01, 1.0, 0.01);
    public static Range<Double> p_koffRange = new Range<>(0.1, 2.0, 0.1);
    public static Range<Double> p_mkcatRange = new Range<>(1.0, 20.0, 1.0);
    public static Range<Double> p_mkmRange = new Range<>(0.1, 2.0, 0.1);
    public static Range<Double>[] Ranges = new Range[]{DPIPRange, ALPHAPRange,  totalKinasesRange, totalPhosphatasesRange, k_mkonRange, k_koffRange, K_mkcatRange, k_mkmRange, p_mkonRange, p_koffRange, p_mkcatRange, p_mkmRange};

    public static void main(String[] args){
        parameterSet pSet = new parameterSet();
        pSet.generateParamSet();
    }

    public parameterSet(){
        parameterMatrix = new HashMap[maxDimension(Ranges)];;
        generateParamSet();
    }

    public void generateParamSet() {
        for (int i = 0; i < maxDimension(Ranges); i++) {
            HashMap<String, Double> row = new HashMap<>();
            for (int j = 0; j < variableNames.length; j++) {
                row.put(variableNames[j], Ranges[j].gradientSpread(maxDimension(Ranges)).get(i));
            }
            parameterMatrix[i] = row;
//            System.out.println(row);
        }
    }

    public static int maxDimension(Range<Double>[] ranges) {
        int max = 0;
        for (Range<Double> r : ranges) {
            if (r.count() > max) max = r.count();
        }
        return max;
    }

    public static double[] getAllSpread(Range<Double>[] ranges, int length){
        double[] result = new double[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            result[i] = ranges[i].spread(length).size();
        }
        return result;
    }

    public static double[] getAllGradientSpread(Range<Double>[] ranges, int length){
        double[] result = new double[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            result[i] = ranges[i].gradientSpread(length).size();
        }
        return result;
    }

}
