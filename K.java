import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class K {

    public static class Pair implements Comparable<Pair> {
        int ind;
        long d;

        public Pair(int ind, long d) {
            this.ind = ind;
            this.d = d;
        }

        @Override
        public int compareTo(Pair o) {
            if (d == o.d) {
                if (ind == o.ind) {
                    return 0;
                } else {
                    return ind < o.ind ? -1 : 1;
                }
            } else {
                return d < o.d ? -1 : 1;
            }
        }
    }

    public static void trCl(int[][] depend) {
        int n = depend.length;
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (((depend[i][k /32] >> (k % 32)) & 1) == 1) {
                    for (int j = 0; j < n / 32 + 1; j++) {
                        depend[i][j] |= depend[k][j];
                    }
                }
            }
        }
    }

    public static TreeSet<Pair> relaxDeadlines(int[] d, int[][] depend) {
        int n = d.length;
        TreeSet<Pair> deadlineSorted = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            deadlineSorted.add(new Pair(i, d[i]));
        }
        for (Integer u : order) {
            Pair v = new Pair(u, d[u]);
            int count = 0;
            HashMap<Long, Integer> lowerD = new HashMap<>();
            for (Pair p : deadlineSorted) {
                if (((depend[u][p.ind / 32] >> (p.ind % 32)) & 1) == 1) {
                    count++;
                    lowerD.put(p.d, count);
                }
            }
            deadlineSorted.remove(v);
            for (Pair p : deadlineSorted) {
                if (((depend[u][p.ind / 32] >> (p.ind % 32)) & 1) == 1) {
                    int lower = lowerD.get(p.d);
                    v.d = Math.min(v.d, p.d - (lower / 2 + (lower % 2)));
                }
            }
            deadlineSorted.add(v);
        }
        return deadlineSorted;
    }

    public static void topsort(int u, int[][] depend) {
        was[u] = true;
        for (int i = 0; i < depend.length; i++) {
            if (!was[i] && (((depend[u][i / 32] >> (i % 32)) & 1) == 1)) {
                topsort(i, depend);
            }
        }
        order.add(u);
    }

    static boolean[] was;
    static ArrayList<Integer> order = new ArrayList<>();


    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("p2precp1lmax.in"))) {
            int n = Integer.parseInt(reader.readLine());
            was = new boolean[n];
            int[] d = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int[][] depend = new int[n][n / 32 + 1];
            for (int i = 0; i < n; i++) {
                int[] tmp = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                for (int j = 0; j < n; j++) {
                    if (tmp[j] == 1) {
                        depend[i][j / 32] |= 1 << (j % 32);
                    }
                }
            }
            trCl(depend);
            for (int i = 0; i < n; i++) {
                if (!was[i]) {
                    for (int j = 0; j < n; j++) {
                        if (((depend[i][j / 32] >> (j % 32)) & 1) == 1) {
                            topsort(i, depend);
                            break;
                        }
                    }
                }
            }
            TreeSet<Pair> sortedByDeadlines = relaxDeadlines(d, depend);
            ArrayList<Integer> first = new ArrayList<>();
            ArrayList<Integer> second = new ArrayList<>();
            int time = 0;
            long maxL = Long.MIN_VALUE;
            int[] waited = new int[n];
            for (int[] tmp : depend) {
                for (int i = 0; i < n; i++) {
                    if (((tmp[i / 32] >> (i % 32)) & 1) == 1) waited[i] += 1;
                }
            }
            while (!sortedByDeadlines.isEmpty()) {
                time++;
                Pair f = null, s = null;
                for (Pair p : sortedByDeadlines) {
                    if (f != null && waited[p.ind] == 0) {
                        s = p;
                        break;
                    }
                    if (f == null && waited[p.ind] == 0) {
                        f = p;
                    }
                }
                first.add(f.ind + 1);
                sortedByDeadlines.remove(f);
                for (int i = 0; i < n; i++) {
                    if (((depend[f.ind][i / 32] >> (i % 32)) & 1) == 1) waited[i]--;
                }
                maxL = Math.max(maxL, time - d[f.ind]);
                if (s != null) {
                    second.add(s.ind + 1);
                    sortedByDeadlines.remove(s);
                    maxL = Math.max(maxL, time - d[s.ind]);
                    for (int i = 0; i < n; i++) {
                        if (((depend[s.ind][i /32] >> (i % 32)) & 1) == 1) waited[i]--;
                    }
                } else {
                    second.add(-1);
                }
            }
            try (BufferedWriter out = Files.newBufferedWriter(Path.of("p2precp1lmax.out"))) {
                out.write(maxL + " " + time);
                out.newLine();
                out.write(first.stream().map(Objects::toString).collect(Collectors.joining(" ")));
                out.newLine();
                out.write(second.stream().map(Objects::toString).collect(Collectors.joining(" ")));
            }
        }
    }
}
