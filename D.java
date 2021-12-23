import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class D {
    public static class Tuple implements Comparable<Tuple> {
        int ind, p, d;

        public Tuple(int ind, int p, int d) {
            this.ind = ind;
            this.p = p;
            this.d = d;
        }

        @Override
        public int compareTo(Tuple o) {
            if (this.p == o.p) {
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
                return this.p < o.p ? -1 : 1;
            }
        }
    }

    public static void main(String[] args) {
        try (BufferedReader in = new BufferedReader(new FileReader("p1sumu.in"))) {
            int n = Integer.parseInt(in.readLine());
            List<Tuple> jobs = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                int[] tmp = Arrays.stream(in.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
                jobs.add(new Tuple(i, tmp[0], tmp[1]));
            }
            long time = 0;
            jobs.sort(Comparator.comparingInt(t -> t.d));
            TreeSet<Tuple> possibleJobs = new TreeSet<>();
            for (Tuple job : jobs) {
                possibleJobs.add(job);
                time = time + (long) job.p;
                if (time > job.d) {
                    Tuple del = possibleJobs.pollLast();
                    time = time - (long) del.p;
                }
            }
            time = 0;
            long[] jobStart = new long[n];
            for (Tuple job : jobs) {
                if (possibleJobs.contains(job)) {
                    jobStart[job.ind] = time;
                    time = time + (long) job.p;
                } else {
                    jobStart[job.ind] = -1;
                }
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter("p1sumu.out"))) {
                out.write("" + possibleJobs.size());
                out.newLine();
                out.write(Arrays.stream(jobStart).mapToObj(Long::toString).collect(Collectors.joining(" ")));
            } catch (IOException ignored) {}
        } catch (IOException ignored) {}
    }
}
