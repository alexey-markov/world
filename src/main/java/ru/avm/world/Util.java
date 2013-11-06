package ru.avm.world;

/**
 * <p>Static methods for doing useful math</p>
 */
public class Util {

    /**
     * Variance: the square of the standard deviation. A measure of the degree
     * of spread among a set of values; a measure of the tendency of individual
     * values to vary from the mean value.
     *
     * @param values the values to calculate
     * @return variance of the values
     */
    public static double variance(double[] values) {
        if ((values == null) || (values.length == 0)) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }
        return variance(values, 0, values.length);
    }

    /**
     * Variance: the square of the standard deviation. A measure of the degree
     * of spread among a set of values; a measure of the tendency of individual
     * values to vary from the mean value.
     *
     * @param values the values to calculate
     * @param from   the initial index of the range, inclusive
     * @param to     the final index of the range, exclusive.
     * @return variance of the values
     */
    public static double variance(double[] values, int from, int to) {
        double std = deviation(values, from, to);
        return std * std;
    }

    /**
     * Standard deviation is a statistical measure of spread or variability.The
     * standard deviation is the root mean square (RMS) deviation of the values
     * from their arithmetic mean.
     * <p/>
     * <b>deviation</b> normalizes values by (N-1), where N is the sample size.  This is the
     * sqrt of an unbiased estimator of the variance of the population from
     * which X is drawn, as long as X consists of independent, identically
     * distributed samples.
     *
     * @param values the values to calculate
     * @return standard deviation of the values
     */
    public static strictfp double deviation(double[] values) {
        if ((values == null) || (values.length == 0)) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }
        return deviation(values, 0, values.length);
    }

    /**
     * Standard deviation is a statistical measure of spread or variability.The
     * standard deviation is the root mean square (RMS) deviation of the values
     * from their arithmetic mean.
     * <p/>
     * <b>deviation</b> normalizes values by (N-1), where N is the sample size.  This is the
     * sqrt of an unbiased estimator of the variance of the population from
     * which X is drawn, as long as X consists of independent, identically
     * distributed samples.
     *
     * @param values the values to calculate
     * @param from   the initial index of the range, inclusive
     * @param to     the final index of the range, exclusive.
     * @return standard deviation of the values
     */
    public static strictfp double deviation(double[] values, int from, int to) {
        double mean = mean(values, from, to);
        double dv = 0D;
        for (int i = from; i < to; i++) {
            double dm = values[i] - mean;
            dv += dm * dm;
        }
        return Math.sqrt(dv / (to - from));
    }

    /**
     * Calculate the mean of an array of values
     *
     * @param values the values to calculate
     * @return The mean of the values
     */
    public static strictfp double mean(double[] values) {
        if ((values == null) || (values.length == 0)) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }
        return mean(values, 0, values.length);
    }

    /**
     * Calculate the mean of an array of values
     *
     * @param values the values to calculate
     * @param from   the initial index of the range, inclusive
     * @param to     the final index of the range, exclusive.
     * @return The mean of the values
     */
    public static strictfp double mean(double[] values, int from, int to) {
        return sum(values, from, to) / (to - from);
    }

    /**
     * Sum up all the values in an array
     *
     * @param values the values to calculate
     * @return The sum of the values
     */
    public static strictfp double sum(double[] values) {
        if ((values == null) || (values.length == 0)) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }
        return sum(values, 0, values.length);
    }

    /**
     * Sum up all the values in an array
     *
     * @param values the values to calculate
     * @param from   the initial index of the range, inclusive
     * @param to     the final index of the range, exclusive.
     * @return The sum of the values
     */
    public static strictfp double sum(double[] values, int from, int to) {
        if ((values == null) || (values.length < to - 1) || (to < from) || (from < 0)) {
            throw new IllegalArgumentException("The data array either is null or does not contain any data.");
        }
        double sum = 0;
        for (int i = from; i < to; i++) {
            sum += values[i];
        }
        return sum;
    }
}