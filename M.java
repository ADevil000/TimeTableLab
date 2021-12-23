import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class M {
    public static class Edge implements Comparable<Edge> {
        int from, to, c, f, cost, k, p, mInd, jInd, backNum;

        public Edge(int from, int to, int k, int p, int c, int jInd, int mInd, int backNum) {
            this.from = from;
            this.to = to;
            this.k = k;
            this.p = p;
            this.cost = k * p;
            this.c = c;
            this.jInd = jInd;
            this.mInd = mInd;
            this.backNum = backNum;
            this.f = 0;
        }



        @Override
        public String toString() {
            return "Edge{" +
                    "from=" + from +
                    ", to=" + to +
                    ", c=" + c +
                    ", f=" + f +
                    ", cost=" + cost +
                    ", k=" + k +
                    ", p=" + p +
                    ", mInd=" + mInd +
                    ", jInd=" + jInd +
                    ", backNum=" + backNum +
                    '}';
        }

        @Override
        public int compareTo(Edge o) {
            if (k == o.k) {
                if (jInd == o.jInd) {
                    return 0;
                } else {
                    return jInd < o.jInd ? -1 : 1;
                }
            } else {
                return k > o.k ? -1 : 1;
            }
        }
    }

    static List<Edge> edges = new ArrayList<>();
    static List<List<Integer>> ways = new ArrayList<>();

    static int s, t;

    public static void addEdge(Edge e) {
        ways.get(e.from).add(edges.size());
        edges.add(e);
    }

    public static void addEdgeAndBack(int from, int to, int k, int p, int jInd, int mInd) {
        addEdge(new Edge(from, to, k, p, 1, jInd, mInd, edges.size() + 1));
        addEdge(new Edge(to, from, -k, p, 0, jInd, mInd, edges.size() - 1));
    }

    public static int findFlowWithMinWay() {
        int n = ways.size();
        List<Integer> edgeToP = new ArrayList<>();
        List<Integer> foundQueue = new ArrayList<>();
        List<Integer> d = new ArrayList<>();
        Deque<Integer> queue = new ArrayDeque<>();
        queue.addLast(s);
        for (int i = 0; i < n; i++) {
            edgeToP.add(-1);
            d.add(Integer.MAX_VALUE - 5000000);
            foundQueue.add(2);
        }
        d.set(s, 0);
        foundQueue.set(s, 1);
        while (!queue.isEmpty()) {
            int from = queue.pollFirst();
            foundQueue.set(from, 0);
            for (Integer edgeNum : ways.get(from)) {
                Edge e = edges.get(edgeNum);
                if (e.c > e.f) {
                    if (d.get(from) + e.cost < d.get(e.to)) {
                        d.set(e.to, d.get(from) + e.cost);
                        edgeToP.set(e.to, edgeNum);
                        switch (foundQueue.get(e.to)) {
                            case 0:
                                queue.addFirst(e.to);
                                break;
                            case 2:
                                queue.addLast(e.to);
                                break;
                            default:
                                break;
                        }
                        foundQueue.set(e.to, 1);
                    }
                }
            }
        }
        if (d.get(t) == Integer.MAX_VALUE - 5000000) {
            return 0;
        }
        int minCapacity = Integer.MAX_VALUE - 5000000;
        int lastV = t;
        List<Integer> edgesForFlow = new ArrayList<>();
        while (lastV != s) {
            Edge e = edges.get(edgeToP.get(lastV));
            if (e.c - e.f < minCapacity) {
                minCapacity = e.c - e.f;
            }
            edgesForFlow.add(edgeToP.get(lastV));
            lastV = e.from;
        }
        for (Integer edgeNum : edgesForFlow) {
            Edge e = edges.get(edgeNum);
            e.f += minCapacity;
            edges.get(e.backNum).f -= minCapacity;
        }
        return minCapacity;
    }

    public static void findMinCostMaxFlow() {
        int add = 0;
        do {
            add = findFlowWithMinWay();
        } while(add > 0);
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("rsumc.in"))) {
            int[] tmp = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int n = tmp[0];
            int m = tmp[1];
            int[][] p = new int[n][];
            for (int i = 0; i < n; i++) {
                p[i] = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            }
            s = 0;
            ways.add(new ArrayList<>());
            t = 1;
            ways.add(new ArrayList<>());
            int startJob = ways.size();
            for (int i = 0; i < n; i++) {
                int from = ways.size();
                ways.add(new ArrayList<>());
                addEdgeAndBack(s, from, 0, 0, i, -1);
            }
            int startM = ways.size();
            for (int i = 0; i < m; i++) {
                for (int j = 1; j <= n; j++) {
                    int from = ways.size();
                    ways.add(new ArrayList<>());
                    addEdgeAndBack(from, t, 0, 0, -1, i);
                    for (int k = 0; k < n; k++) {
                        addEdgeAndBack(startJob + k, from, j, p[k][i], k, i);
                    }
                }
            }
            findMinCostMaxFlow();
            List<List<Edge>> used = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                used.add(new ArrayList<>());
            }
            int ans = 0;
            for (Edge e : edges) {
                if (e.f > 0 && e.mInd != -1 && e.jInd != -1) {
                    used.get(e.mInd).add(e);
                    ans += e.cost;
                }
            }
            try (BufferedWriter out = Files.newBufferedWriter(Path.of("rsumc.out"))) {
                out.write(ans + "");
                out.newLine();
                for (List<Edge> l : used) {
                    l.sort(Comparator.naturalOrder());
                    out.write(l.size() + " " + l.stream().mapToInt(j -> j.jInd + 1).mapToObj(Objects::toString).collect(Collectors.joining(" ")));
                    out.newLine();
                }
            }
        }
    }
}
