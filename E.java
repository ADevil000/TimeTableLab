import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class E {
    public static class Tuple implements Comparable<Tuple> {
        int ind, w, d;

        public Tuple(int ind, int d, int w) {
            this.ind = ind;
            this.d = d;
            this.w = w;
        }

        @Override
        public int compareTo(Tuple o) {
            if (this.w == o.w) {
                if (this.d == o.d) {
                    if (this.ind == o.ind) {
                        return 0;
                    } else {
                        return this.ind < o.ind ? -1 : 1;
                    }
                } else {
                    return this.d < o.d ? -1 : 1;
                }
            } else {
                return this.w < o.w ? -1 : 1;
            }
        }
    }

    public static void main(String[] args) {
        try (BufferedReader in = new BufferedReader(new FileReader("p1sumwu.in"))) {
            int n = Integer.parseInt(in.readLine());
            List<Tuple> jobs = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                int[] tmp = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                jobs.add(new Tuple(i, tmp[0], tmp[1]));
            }
            int time = 0;
            jobs.sort(Comparator.comparingInt(t -> t.d));
            TreeSet<Tuple> possibleJobs = new TreeSet<>();
            List<Tuple> deadJobs = new ArrayList<>();
            long sumWU = 0;
            for (Tuple job : jobs) {
                possibleJobs.add(job);
                time += 1;
                if (time > job.d) {
                    Tuple del = possibleJobs.pollFirst();
                    sumWU = sumWU + (long) del.w;
                    deadJobs.add(del);
                    time -= 1;
                }
            }
            time = 0;
            int[] jobStart = new int[n];
            for (Tuple job : jobs) {
                if (possibleJobs.contains(job)) {
                    jobStart[job.ind] = time;
                    time += 1;
                }
            }
            for (Tuple job : deadJobs) {
                jobStart[job.ind] = time;
                time += 1;
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter("p1sumwu.out"))) {
                out.write("" + sumWU);
                out.newLine();
                out.write(Arrays.stream(jobStart).mapToObj(Long::toString).collect(Collectors.joining(" ")));
            } catch (IOException ignored) {}
        } catch (IOException ignored) {}
    }
}
