import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class N {

    public static class Job implements Comparable<Job> {
        int ind;
        long p1, p2, pMin;

        public Job(int ind, long p1, long p2) {
            this.ind = ind;
            this.p1 = p1;
            this.p2 = p2;
            this.pMin = Math.min(p1, p2);
        }

        @Override
        public int compareTo(Job o) {
            if (pMin == o.pMin) {
                if (ind == o.ind) {
                    return 0;
                } else {
                    return ind < o.ind ? -1 : 1;
                }
            } else {
                return pMin < o.pMin ? -1 : 1;
            }
        }

        @Override
        public String toString() {
            return "Job{" +
                    "ind=" + ind +
                    ", p1=" + p1 +
                    ", p2=" + p2 +
                    '}';
        }
    }

    public static void main(String[] args) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get("f2cmax.in"))) {
            int n = Integer.parseInt(reader.readLine().strip());
            long[] p1 = Arrays.stream(reader.readLine().strip().split("\\s+")).mapToLong(Long::parseLong).toArray();
            long[] p2 = Arrays.stream(reader.readLine().strip().split("\\s+")).mapToLong(Long::parseLong).toArray();
            List<Job> jobs = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                jobs.add(new Job(i, p1[i], p2[i]));
            }
            jobs.sort(Comparator.naturalOrder());
            ArrayList<Job> left = new ArrayList<>();
            LinkedList<Job> right = new LinkedList<>();
            for (int i = 0; i < n; i++) {
                Job job = jobs.get(i);
                if (job.p1 <= job.p2) {
                    left.add(job);
                } else {
                    right.addFirst(job);
                }
            }
            long[] time1 = new long[n];
            long ans = 0;
            for (int i = 0; i < left.size(); i++) {
                long prevTime = i == 0 ? 0 : time1[i - 1];
                time1[i] = prevTime + left.get(i).p1;
                if (ans < time1[i]) {
                    ans = time1[i] + left.get(i).p2;
                } else {
                    ans += left.get(i).p2;
                }
            }
            int i = left.size();
            for (Job job : right) {
                long prevTime = i == 0 ? 0 : time1[i - 1];
                time1[i] = prevTime + job.p1;
                if (ans < time1[i]) {
                    ans = time1[i] + job.p2;
                } else {
                    ans += job.p2;
                }
                i++;
            }
            try (BufferedWriter out = Files.newBufferedWriter(Paths.get("f2cmax.out"))) {
                out.write(ans + "");
                out.newLine();
                String order = Stream.concat(left.stream(), right.stream()).mapToInt(j -> j.ind + 1).mapToObj(Objects::toString).collect(Collectors.joining(" "));
                out.write(order);
                out.newLine();
                out.write(order);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}