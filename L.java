import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class L {
    public static class Tuple implements Comparable<Tuple> {
        int ind, d, dNew;
        List<Integer> edgesFrom = new ArrayList<>();
        int edgesTo = -1;

        public Tuple(int ind, int d) {
            this.ind = ind;
            this.d = d;
        }

        @Override
        public int compareTo(Tuple o) {
            if (this.dNew == o.dNew) {
                if (this.ind == o.ind) {
                    return 0;
                } else {
                    return this.ind < o.ind ? -1 : 1;
                }
            } else {
                return this.dNew < o.dNew ? -1 : 1;
            }
        }
    }

    public static void relaxDeadlines(Tuple job, List<Tuple> jobs, int deadline) {
        job.dNew = Math.min(job.d, deadline - 1);
        for (Integer ind : job.edgesFrom) {
            relaxDeadlines(jobs.get(ind), jobs, job.dNew);
        }
    }

    public static void main(String[] args) {
        try (BufferedReader in = new BufferedReader(new FileReader("pintreep1l.in"))) {
            // input
            int[] tmp = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int n = tmp[0];
            int m = tmp[1];
            int[] d = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            List<Tuple> jobs = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                jobs.add(new Tuple(i, d[i]));
            }
            for (int i = 0; i < n - 1; i++) {
                tmp = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                int from = tmp[0] - 1;
                int to = tmp[1] - 1;
                jobs.get(from).edgesTo = to;
                jobs.get(to).edgesFrom.add(from);
            }
            int headOfTree = -1;
            for (int i = 0; i < n; i++) {
                if (jobs.get(i).edgesTo == -1) {
                    headOfTree = i;
                    break;
                }
            }
            // solution
            relaxDeadlines(jobs.get(headOfTree), jobs, Integer.MAX_VALUE);
            jobs.sort(Comparator.comparingInt(t -> t.dNew));
            int machinePossibleTime = 0;
            Map<Integer, Integer> numberOfJobsAtTime = new HashMap<>();
            int[] jobStart = new int[n];
            for (int i = 0; i < n; i++) {
                Tuple job = jobs.get(i);
                int maxEndTimeOfParent = job.edgesFrom.stream().mapToInt(ind -> jobStart[ind] + 1).max().orElse(0);
                int time = Math.max(machinePossibleTime, maxEndTimeOfParent);
                jobStart[job.ind] = time;
                if (numberOfJobsAtTime.merge(time, 1, Integer::sum) == m) machinePossibleTime++;
            }
            for (int i = 0; i < n; i++) {
                d[i] = jobStart[i] + 1 - d[i];
            }
            int late = Arrays.stream(d).max().getAsInt();
            // outputa
            try (BufferedWriter out = new BufferedWriter(new FileWriter("pintreep1l.out"))) {
                out.write(late + "");
                out.newLine();
                out.write(Arrays.stream(jobStart).mapToObj(Objects::toString).collect(Collectors.joining(" ")));
            } catch (IOException ignored) {}
        } catch (IOException ignore) {}
    }
}
