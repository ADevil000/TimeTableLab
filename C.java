import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class C {

    public static class Tuple implements Comparable<Tuple> {
        int ind;
        long p, w;

        public Tuple(int ind, long p, long w) {
            this.ind = ind;
            this.p = p;
            this.w = w;
        }

        public void addParams(Tuple t) {
            this.p += t.p;
            this.w += t.w;
        }

        @Override
        public int compareTo(Tuple o) {
            if (w * o.p == o.w * p) {
                if (ind == o.ind) {
                    return 0;
                } else {
                    return ind > o.ind ? -1 : 1;
                }
            } else {
                return w * o.p > o.w * p ? -1 : 1;
            }
        }
    }

    public static class Union {
        int[] union;

        public Union(int n) {
            union = new int[n];
            for (int i = 0; i < n; i++) {
                union[i] = i;
            }
        }

        int findUnion(int p) {
            if (p == union[p]) {
                return p;
            }
            union[p] = findUnion(union[p]);
            return union[p];
        }

        void unit(int f, int s) {
            int fP = findUnion(f);
            int sP = findUnion(s);
            if (fP != sP) {
                union[sP] = fP;
            } else {
                throw new NullPointerException();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("p1outtreewc.in"))) {
            int n = Integer.parseInt(reader.readLine());
            long[] p = Arrays.stream(reader.readLine().split(" ")).mapToLong(Long::parseLong).toArray();
            long[] w = Arrays.stream(reader.readLine().split(" ")).mapToLong(Long::parseLong).toArray();
            Tuple[] values = new Tuple[n];
            int[] end = new int[n];
            int[] parents = new int[n];
            Union union = new Union(n);
            TreeSet<Tuple> priorityQueue = new TreeSet<>();
            for (int i = 0; i < n; i++) {
                values[i] = new Tuple(i, p[i], w[i]);
                parents[i] = -1;
                end[i] = i;
            }
            for (int i = 0; i < n - 1; i++) {
                int[] tmp = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                int son = tmp[0] - 1;
                int pnt = tmp[1] - 1;
                parents[son] = pnt;
            }
            int root = -1;
            for (int i = 0; i < n; i++) {
                if (parents[i] != -1) {
                    priorityQueue.add(values[i]);
                } else {
                    root = i;
                }
            }
            while (!priorityQueue.isEmpty()) { // union поддерживает инвариант, что СНМ объединения это номер действующего Tuple
                Tuple maxQ = priorityQueue.first();
                priorityQueue.remove(maxQ);
                int parent = union.findUnion(parents[maxQ.ind]);
                Tuple jobWithParent = values[parent];
                boolean notRoot = priorityQueue.remove(jobWithParent);
                jobWithParent.addParams(maxQ);
                parents[maxQ.ind] = end[parent];
                end[parent] = end[maxQ.ind];
                union.unit(parent, maxQ.ind);
                if (notRoot) priorityQueue.add(jobWithParent);
            }
            int[] order = new int[n];
            order[0] = root;
            for (int i = n - 1, cur = end[root]; i > 0; i--, cur = parents[cur]) {
                order[i] = cur;
            }
            long[] ans = new long[n];
            long cost = 0;
            for (int i = 1; i < n; i++) {
                ans[order[i]] = ans[order[i - 1]] + p[order[i - 1]];
                cost = cost + ans[order[i]] * w[order[i - 1]];
            }
            cost = cost + (ans[order[n - 1]] + p[order[n - 1]]) * w[order[n - 1]];
            try (BufferedWriter out = Files.newBufferedWriter(Path.of("p1outtreewc.out"))) {
                out.write(cost + "");
                out.newLine();
                out.write(Arrays.stream(ans).mapToObj(Objects::toString).collect(Collectors.joining(" ")));
            }
        }
    }
}