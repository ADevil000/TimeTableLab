import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class B {
    private static void fillA(long[] timeA, List<Integer> left, List<Integer> right, long cMax, int x, int[] a) {
        long curT = 0;
        for (Integer ind : left) {
            timeA[ind] = curT;
            curT += a[ind];
        }
        curT = cMax - a[x];
        timeA[x] = curT;
        for (Integer ind : right) {
            curT -= a[ind];
            timeA[ind] = curT;
        }
    }

    private static void fillB(long[] timeB, List<Integer> left, List<Integer> right, long cMax, int x, int[] b) {
        timeB[x] = 0;
        long curT = b[x];
        for (Integer ind : left) {
            timeB[ind] = curT;
            curT += b[ind];
        }
        curT = cMax;
        for (Integer ind : right) {
            curT -= b[ind];
            timeB[ind] = curT;
        }
    }

    private static void solve(int[] a, int[] b, long cMax, boolean swap) {
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        long maxA = 0, maxB = 0;
        int indMaxA = -1;
        for (int i = 0; i < a.length; i++) {
            if (a[i] <= b[i]) {
                if (maxA <= a[i]) {
                    maxA = a[i];
                    indMaxA = left.size();
                }
                left.add(i);
            } else {
                right.add(i);
                maxB = Math.max(maxB, b[i]);
            }
        }

        if (maxA < maxB) {
            left.clear();
            right.clear();
            solve(b, a, cMax, true);
            return;
        }

        int x = left.remove(indMaxA);
        long[] timeA = new long[a.length];
        fillA(timeA, left, right, cMax, x, a);

        long[] timeB = new long[a.length];
        fillB(timeB, left, right, cMax, x, b);

        if (swap) {
            long[] tmp = timeA;
            timeA = timeB;
            timeB = tmp;
        }
        try (BufferedWriter out = new BufferedWriter(new FileWriter("o2cmax.out"))) {
            out.write("" + cMax);
            out.newLine();
            out.write(Arrays.stream(timeA).mapToObj(Long::toString).collect(Collectors.joining(" ")));
            out.newLine();
            out.write(Arrays.stream(timeB).mapToObj(Long::toString).collect(Collectors.joining(" ")));
        } catch (IOException ignored) {
        }
    }

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new FileReader("o2cmax.in"))){
            int n = Integer.parseInt(reader.readLine());
            int[] a = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int[] b = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            long aSum = 0, bSum = 0, maxPair = 0;
            for (int i = 0; i < n; i++) {
                aSum += a[i];
                maxPair = Math.max(maxPair, a[i] + b[i]);
                bSum += b[i];
            }
            long cMax = Math.max(aSum, Math.max(bSum, maxPair));
            solve(a, b, cMax, false);
        } catch (IOException ignored) {
        }
    }
}
