package tools.descartes.coffee.controller.monitoring.reporter;

public class ReporterUtils {

    private ReporterUtils() {

    }

    /**
     * mean value
     * 
     * @param a
     * @return
     */
    public static double mean(double[] a) {
        double sum = 0.0;
        for (double v : a) {
            sum = sum + v;
        }
        return sum / a.length;
    }

    public static double mean(long[] a) {
        double sum = 0.0;
        for (long l : a) {
            sum = sum + l;
        }
        return sum / a.length;
    }

    /**
     * variance
     * 
     * @param a
     * @return
     */
    public static double var(double[] a) {
        double m = mean(a);
        double sum = 0.0;
        for (double v : a) {
            sum = sum + (v - m) * (v - m);
        }
        return sum / (a.length - 1);
    }

    public static double var(long[] a) {
        double m = mean(a);
        double sum = 0.0;
        for (long l : a) {
            sum = sum + (l - m) * (l - m);
        }
        return sum / (a.length - 1);
    }

    public static double var(double[] a, double mean) {
        double sum = 0.0;
        for (double v : a) {
            sum = sum + (v - mean) * (v - mean);
        }
        return sum / (a.length - 1);
    }

    public static double var(long[] a, double mean) {
        double sum = 0.0;
        for (long l : a) {
            sum = sum + (l - mean) * (l - mean);
        }
        return sum / (a.length - 1);
    }

    /**
     * standard deviation
     * 
     * @param a
     * @return
     */
    public static double stdDev(double[] a) {
        return Math.sqrt(var(a));
    }

    public static double stdDev(long[] a) {
        return Math.sqrt(var(a));
    }

    public static double stdDev(double var) {
        return Math.sqrt(var);
    }

}
