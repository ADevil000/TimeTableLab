import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Double.min;

public class A {

    public static class Edge {
        int from, to, backNum;
        double c, f;

        public Edge(int from, int to, double c, int backNum) {
            this.from = from;
            this.to = to;
            this.c = c;
            this.backNum = backNum;
            this.f = 0;
        }
    }

    static int completedJobs = 0;
    static int n, m;
    static int s, t, startTimeIntervalInd, startJobsInd, startExtraIntervalInd;
    static List<Edge> edges = new ArrayList<>();
    static int[] machines;
    static boolean[] was;

    private static void addEdge(List<List<Integer>> graph, int from, int to, double c) {
        graph.get(from).add(edges.size());
        edges.add(new Edge(from, to, c, edges.size() + 1));
        graph.get(to).add(edges.size());
        edges.add(new Edge(to, from, 0, edges.size() - 1));
    }

    private static List<List<Integer>> createGraph(int[][] jobs, double[][] borders) {
        edges.clear();
        double[] times = new double[borders.length * 2];
        for (int i = 0; i < borders.length; i++) {
            times[i] = borders[i][0];
            times[i + borders.length] = borders[i][1];
        }
        times = Arrays.stream(times).sorted().distinct().toArray();
        List<List<Integer>> graph = new ArrayList<>();
        // add start
        graph.add(new ArrayList<>());
        s = 0;
        // add finish
        graph.add(new ArrayList<>());
        t = 1;
        // add time intervals
        startTimeIntervalInd = graph.size();
        int powerOfM = Arrays.stream(machines).sum();
        for (int i = 1; i < times.length; i++) {
            graph.add(new ArrayList<>());
            addEdge(graph,startTimeIntervalInd + (i - 1), 1, powerOfM * times[i] - times[i - 1]);
        }
        // add ExtraIntervals
        startExtraIntervalInd = graph.size();
        for (int i = 1; i < times.length; i++) {
            for (int j = 0; j < m; j++) {
                graph.add(new ArrayList<>());
                int from =  startExtraIntervalInd + j + (i - 1) * m;
                int to = startTimeIntervalInd + (i - 1);
                double c;
                if (j == m - 1) {
                    c = (j + 1) * (machines[j]) * (times[i] - times[i - 1]);
                } else {
                    c = (j + 1) * (machines[j] - machines[j + 1]) * (times[i] - times[i - 1]);
                }
                addEdge(graph, from, to, c);
            }
        }
        // add Jobs
        startJobsInd = graph.size();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
            int from = startJobsInd + i;
            addEdge(graph, s, from, jobs[i][0]);
            int timeInd = 0;
            while (borders[i][0] > times[timeInd]) {
                timeInd++;
            }
            timeInd++;
            while (timeInd < times.length && borders[i][1] >= times[timeInd]) {
                for (int j = 0; j < m; j++) {
                    int to = startExtraIntervalInd + (timeInd - 1) * m + j;
                    double c;
                    if (j == m - 1) {
                        c = (machines[j]) * (times[timeInd] - times[timeInd - 1]);
                    } else {
                        c = (machines[j] - machines[j + 1]) * (times[timeInd] - times[timeInd - 1]);
                    }
                    addEdge(graph, from, to, c);
                }
                timeInd++;
            }
        }
        // add Edges from start
        return graph;
    }

    private static double dfs(int from, double flow, List<List<Integer>> ways) {
        if (from == t) {
            return flow;
        }
        was[from] = true;
        for (Integer edgeNum : ways.get(from)) {
            Edge e = edges.get(edgeNum);
            if (!was[e.to] && e.f < e.c) {
                double added = dfs(e.to, min(flow, e.c - e.f), ways);
                if (added > 0) {
                    e.f += added;
                    edges.get(e.backNum).f -= added;
                    return added;
                }
            }
        }
        return 0;
    }

    private static boolean solve(int[][] jobs, double[][] borders) {
        List<List<Integer>> ways = createGraph(jobs, borders);
        double added, flow = 0;
        do {
            was = new boolean[ways.size()];
            added = dfs(s, completedJobs, ways);
            flow += added;
        } while (added > 0);
//        for (Edge e : edges) {
//            System.out.println(e.from + " -> " + e.to + " | " + e.c + " >= " + e.f);
//        }
//        System.out.println(flow);
        return flow == (double) completedJobs;
    }

    private static double[][] changeBorders(int[][] jobs, double extra) {
        double[][] res = new double[jobs.length][2];
        for (int i = 0; i < jobs.length; i++) {
            res[i][0] = jobs[i][1];
            res[i][1] = jobs[i][2] + extra;
        }
        return res;
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("cheese.in"))) {
            // input
            int[] tmp = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            n = tmp[0];
            m = tmp[1];
            // Job : p, r, d;
            int[][] jobs = new int[n][];
            for (int i = 0; i < n; i++) {
                jobs[i] = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                completedJobs += jobs[i][0];
            }
            // sorted machines
            machines = new int[m];
            for (int i = 0; i < m; i++) {
                machines[i] = Integer.parseInt(reader.readLine());
            }
            Arrays.sort(machines);
            tmp = new int[m];
            for (int i = 0; i < m; i++) {
                tmp[i] = machines[(m - 1) - i];
            }
            machines = tmp;
            double left = 0, right = completedJobs;
            for (int i = 0; i < 1000; i++) {
                double mid = (right + left) / 2;
                double[][] borders = changeBorders(jobs, mid);
                if (solve(jobs, borders)) {
                    right = mid;
                } else {
                    left = mid;
                }
            }
            try (BufferedWriter out = Files.newBufferedWriter(Path.of("cheese.out"))) {
                out.write(String.format("%.10f", (right + left) / 2));
            }
        }
    }
}