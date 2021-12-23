import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;

public class F {

    private static class Pair {
        int ind;
        BigInteger f;

        public Pair(int ind, BigInteger f) {
            this.ind = ind;
            this.f = f;
        }
    }

    private static BigInteger computeF(int[] f, BigInteger time) {
        BigInteger res = BigInteger.valueOf(f[f[0] + 1]);
        BigInteger x = time;
        for (int i = f[0]; i >= 1; i--) {
            res = res.add(x.multiply(BigInteger.valueOf(f[i])));
            x = x.multiply(time);
        }
        return res;
    }

    private static Pair findSmallestF(int[] countedWays, int[][] f, int time) {
        BigInteger minF = BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(Long.MAX_VALUE)).multiply(BigInteger.valueOf(Long.MAX_VALUE));
        int resInd = -1;
        for (int i = 0; i < countedWays.length; i++) {
            if (countedWays[i] == 0) {
                BigInteger fRes = computeF(f[i], BigInteger.valueOf(time));
                if (minF.compareTo(fRes) > 0) {
                    minF = fRes;
                    resInd = i;
                }
            }
        }
        return new Pair(resInd, minF);
    }

    public static void main(String[] args) {
        try (BufferedReader in = new BufferedReader(new FileReader("p1precfmax.in"))) {
            // input
            int n = Integer.parseInt(in.readLine());
            int[] p = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int[][] f = new int[n][];
            for (int i = 0; i < n; i++) {
                f[i] = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            }
            int d = Integer.parseInt(in.readLine());
            boolean[][] ways = new boolean[n][n];
            int[] countedWays = new int[n];
            for (int i = 0; i < d; i++) {
                int[] tmp = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                ways[tmp[1] - 1][tmp[0] - 1] = true;
                countedWays[tmp[0] - 1] += 1;
            }
            // solution
            int time = Arrays.stream(p).sum();
            int[] ans = new int[n];
            BigInteger fMax = BigInteger.ZERO;
            for (int i = n - 1; i >= 0; i--) {
                Pair job = findSmallestF(countedWays, f, time);
                int ind = job.ind;
                fMax = fMax.compareTo(job.f) < 0 ? job.f : fMax;
                time -= p[ind];
                ans[ind] = time;
                countedWays[ind] = -1;
                deleteEdges(ways, countedWays, ind);
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter("p1precfmax.out"))) {
                out.write(fMax.toString());
                out.newLine();
                out.write(Arrays.stream(ans).mapToObj(Integer::toString).collect(Collectors.joining(" ")));
            } catch (IOException ignored) {}
        } catch (IOException ignore) {}
    }

    private static void deleteEdges(boolean[][] ways, int[] countedWays, int ind) {
        for (int i = 0; i < countedWays.length; i++) {
            if (ways[ind][i]) {
                ways[ind][i] = false;
                countedWays[i] -= 1;
            }
        }
    }
}