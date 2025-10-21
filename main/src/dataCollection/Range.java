package dataCollection;

import java.util.ArrayList;

public class Range<T extends Number> {
    public T start;
    public T end;
    public T increment;

    public Range(T start, T end, T inc) {
        this.start = start;
        this.end = end;
        this.increment = inc;
    }

    // Inclusive at both ends
    public int count() {
        double s = start.doubleValue();
        double e = end.doubleValue();
        double inc = increment.doubleValue();
        double c = (e - s + inc) / inc;
        return (int) Math.floor(c);
    }

    public ArrayList<Double> spread(int length) {
        ArrayList<Double> returnList = new ArrayList<>();

        if (length < count()) {
            throw new IllegalArgumentException("length must be larger than count");
        }

        if ((double) length / count() != Math.floor((double) length / count())) {
            throw new IllegalArgumentException("length must be whole divisible by count. The incompatible range is: " + start + ", " + end);
        }

        int d = length / count();
        double s = start.doubleValue();
        double inc = increment.doubleValue();

        for (int i = 0; i < count(); i++) {
            double curr = s + (i * inc);
            for (int j = 0; j < d; j++) {
                returnList.add(curr);
            }
        }
        return returnList;
    }

    public ArrayList<Double> gradientSpread(int length) {
        ArrayList<Double> returnList = new ArrayList<>();

        double s = start.doubleValue();
        double e = end.doubleValue();
        double inc = increment.doubleValue();
        int count = count();

        while ((double) length / count != Math.floor((double) length / count)) {
            if (inc < 0.0001) {
                throw new IllegalArgumentException("Increment too small. Reset range.");
            }
            inc *= 0.1;
            double c = (e - s + inc) / inc;
            count = (int) Math.floor(c);
        }

        int d = length / count();

        for (int i = 0; i < count(); i++) {
            double curr = s + (i * inc);
            for (int j = 0; j < d; j++) {
                returnList.add(curr);
            }
        }
        return returnList;
    }
}
