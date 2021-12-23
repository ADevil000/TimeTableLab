import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class O {
    public static class JobOnMachine implements Comparable<JobOnMachine> {
        int t, k, mInd;
        long start;
        Job job;
        int q;

        public JobOnMachine(int t, int k, int m) {
            this.t = t;
            this.k = k;
            this.mInd = m;
            this.q = k * t;
        }

        @Override
        public int compareTo(JobOnMachine o) {
            if (q == o.q) {
                if (mInd == o.mInd) {
                    return 0;
                } else {
                    return mInd < o.mInd ? -1 : 1;
                }
            } else {
                return q < o.q ? -1 : 1;
            }
        }
    }

    public static class Job implements Comparable<Job> {
        int p, ind;

        public Job(int p, int ind) {
            this.p = p;
            this.ind = ind;
        }

        @Override
        public int compareTo(Job o) {
            if (p == o.p) {
                if (ind == o.ind) {
                    return 0;
                } else {
                    return ind < o.ind ? -1 : 1;
                }
            } else {
                return p > o.p ? -1 : 1;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("qsumci.in"))) {
            int[] tmp = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int n = tmp[0];
            int m = tmp[1];
            int[] p = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            List<Job> jobs = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                jobs.add(new Job(p[i], i));
            }
            jobs.sort(Comparator.naturalOrder());
            int[] t = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            TreeSet<JobOnMachine> work = new TreeSet<>();
            List<JobOnMachine> used = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                work.add(new JobOnMachine(t[i], 1, i));
            }
            for (Job job : jobs) {
                JobOnMachine jm = work.pollFirst();
                jm.job = job;
                used.add(jm);
                if (jm.k < n) work.add(new JobOnMachine(jm.t, jm.k + 1, jm.mInd));
            }
            used.sort(Comparator.comparingInt(jm -> jm.k));
            long[] prevEnd = new long[m];
            long anotherAns = 0;
            for (int i = n - 1; i >= 0; i--) {
                JobOnMachine jm = used.get(i);
                jm.start = prevEnd[jm.mInd];
                prevEnd[jm.mInd] += (long) jm.t * (long) jm.job.p;
                anotherAns += prevEnd[jm.mInd];
            }
            try (BufferedWriter out = Files.newBufferedWriter(Path.of("qsumci.out"))) {
                out.write(anotherAns + "");
                out.newLine();
                used.sort(Comparator.comparingInt(jm -> jm.job.ind));
                for (JobOnMachine jm : used) {
                    out.write((jm.mInd + 1) + " " + jm.start);
                    out.newLine();
                }
            }
        }
    }
}
