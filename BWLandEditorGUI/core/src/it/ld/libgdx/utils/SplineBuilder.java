package it.ld.libgdx.utils;

import java.util.*;
import java.util.Map.Entry;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public abstract class SplineBuilder {
	private SplineBuilder() {}
	
    /**Builds a natural cubic spline y(x) passing through cPoints, filling target array.
     */
    public static void buildSplineMap(List<Vector2> points, int[] target) {
        if (target == null || target.length == 0) throw new IllegalArgumentException("Invalid target");

        int size = target.length;
        int maxIndex = size - 1;

        if (points == null || points.isEmpty()) {
            for (int i = 0; i < size; i++) target[i] = i;
            return;
        }

        // sort and remove duplicates x
        TreeMap<Integer, Double> pts = new TreeMap<>();
        for (Vector2 p : points) {
            int xi = MathUtils.clamp(Math.round(p.x), 0, maxIndex);
            double yi = MathUtils.clamp(p.y, 0, maxIndex);
            pts.put(xi, yi);
        }

        if (pts.size() == 1) {
            int y = MathUtils.clamp((int)Math.round(pts.firstEntry().getValue()), 0, maxIndex);
            Arrays.fill(target, y);
            return;
        }

        int n = pts.size();
        double[] x = new double[n];
        double[] y = new double[n];

        int i = 0;
        for (Entry<Integer, Double> e : pts.entrySet()) {
            x[i] = e.getKey();
            y[i] = e.getValue();
            i++;
        }

        double[] y2 = computeNaturalSplineSecondDerivatives(x, y);

        for (int xi = 0; xi < size; xi++) {
            double yi;

            if (xi <= x[0]) yi = y[0];
            else if (xi >= x[n - 1]) yi = y[n - 1];
            else yi = evalSpline(x, y, y2, xi);

            target[xi] = MathUtils.clamp((int)Math.round(yi), 0, maxIndex);
        }
    }

    private static double[] computeNaturalSplineSecondDerivatives(double[] x, double[] y) {
        int n = x.length;
        double[] y2 = new double[n];
        double[] u = new double[n-1];

        y2[0] = 0;
        u[0] = 0;

        for (int i = 1; i < n-1; i++) {
            double hPrev = x[i] - x[i-1];
            double hNext = x[i+1] - x[i];

            double sig = hPrev / (hPrev + hNext);
            double p = sig * y2[i-1] + 2.0;

            y2[i] = (sig - 1.0) / p;

            double d1 = (y[i+1] - y[i]) / hNext;
            double d0 = (y[i] - y[i-1]) / hPrev;

            u[i] = (6.0 * (d1 - d0) / (hPrev + hNext) - sig * u[i-1]) / p;
        }

        y2[n-1] = 0;

        for (int k = n-2; k >= 0; k--)
            y2[k] = y2[k] * y2[k+1] + u[k];

        return y2;
    }

    private static double evalSpline(double[] x, double[] y, double[] y2, double xq) {
        int klo = 0;
        int khi = x.length - 1;

        while (khi - klo > 1) {
            int k = (khi + klo) >>> 1;
            if (x[k] > xq) khi = k;
            else klo = k;
        }

        double h = x[khi] - x[klo];

        double a = (x[khi] - xq) / h;
        double b = (xq - x[klo]) / h;

        return a * y[klo] + b * y[khi] +
               ((a*a*a - a) * y2[klo] + (b*b*b - b) * y2[khi]) * (h*h) / 6.0;
    }
}