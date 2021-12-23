import java.io.BufferedReader;
        import java.io.BufferedWriter;
        import java.io.IOException;
        import java.nio.file.Files;
        import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
        import java.util.stream.IntStream;

public class H {
    public static class Job {
        int ind;
        long r, p, a, b, c;
        List<Long> times = new ArrayList<>();

        public Job(long r, long p, long a, long b, long c, int ind) {
            this.r = r;
            this.p = p;
            this.a = a;
            this.b = b;
            this.c = c;
            this.ind = ind;
        }

        public long evalF(long time) {
            return a * time * time + b * time + c;
        }

        @Override
        public String toString() {
            return "Job{" +
                    "ind=" + ind +
                    ", r=" + r +
                    ", p=" + p +
                    ", a=" + a +
                    ", b=" + b +
                    ", c=" + c +
                    '}';
        }
    }

    public static void dfs(int u, List<List<Integer>> ways, boolean[] was, List<Job> res, List<Job> jobs) {
        if (was[u]) {
            return;
        }
        was[u] = true;
        for (Integer v : ways.get(u)) {
            if (!was[v]) {
                dfs(v, ways, was, res, jobs);
            }
        }
        res.add(jobs.get(u));
    }

    public static List<Job> topsort(List<Job> jobs, List<List<Integer>> ways, List<List<Integer>> backWays) {
        List<Job> res = new ArrayList<>();
        boolean[] was = new boolean[jobs.size()];
        for (int i = 0; i < jobs.size(); i++) {
            if (!was[i] && backWays.get(i).isEmpty()) {
                dfs(i, ways, was, res, jobs);
            }
        }
        return res;
    }

    public static class Block {
        long start, time = 0, end = 0;
        List<Job> jobs = new ArrayList<>();

        public Block(long start) {
            this.start = start;
        }

        public void add(Job job) {
            jobs.add(job);
            time += job.p;
            end = start + time;
        }
    }

    public static List<Block> createBlocks(List<Job> jobs) {
        List<Block> blocks = new ArrayList<>();
        int blockInd = -1;
        for (Job job : jobs) {
            Block block;
            if (blockInd == -1) {
                block = new Block(job.r);
                blocks.add(block);
                blockInd++;
            } else {
                block = blocks.get(blockInd);
                if (block.end < job.r) {
                    block = new Block(job.r);
                    blocks.add(block);
                    blockInd++;
                }
            }
            block.add(job);
        }
        return blocks;
    }

    public static long decompose(Block block, List<List<Integer>> ways) {
        long e = block.end;
        long f = Long.MAX_VALUE;
        int minJobInd = -1;
        HashSet<Integer> was = new HashSet<>();
        for (int i = block.jobs.size() - 1; i >= 0; i--) {
            Job job = block.jobs.get(i);
            if (ways.get(job.ind).stream().noneMatch(was::contains) && f > job.evalF(e)) {
                minJobInd = i;
                f = job.evalF(e);
            }
            was.add(job.ind);
        }
        Job deleted = block.jobs.get(minJobInd);
        long ans = deleted.evalF(e);
        block.jobs.remove(minJobInd);
        List<Block> newBlocks = createBlocks(block.jobs);
        if (!newBlocks.isEmpty()) {
            if (block.start < newBlocks.get(0).start) {
                deleted.times.add(block.start);
                deleted.times.add(newBlocks.get(0).start);
            }
            for (int i = 1; i < newBlocks.size(); i++) {
                long start = newBlocks.get(i - 1).end;
                long end = newBlocks.get(i).start;
                if (start < end) {
                    deleted.times.add(start);
                    deleted.times.add(end);
                }
            }
            if (block.end > newBlocks.get(newBlocks.size() - 1).end) {
                deleted.times.add(newBlocks.get(newBlocks.size() - 1).end);
                deleted.times.add(block.end);
            }
            for (Block b : newBlocks) {
                ans = Math.max(ans, decompose(b, ways));
            }
        } else {
            deleted.times.add(block.start);
            deleted.times.add(block.end);
        }
        return ans;
    }


    public static void main(String[] args) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("p1precpmtnrifmax.in"))) {
            int n = Integer.parseInt(reader.readLine());
            int[] p = Arrays.stream(reader.readLine().trim().split(" ")).mapToInt(Integer::parseInt).toArray();
            int[] r = Arrays.stream(reader.readLine().trim().split(" ")).mapToInt(Integer::parseInt).toArray();
            int m = Integer.parseInt(reader.readLine());
            List<List<Integer>> ways = IntStream.range(0, n).mapToObj(i -> new ArrayList<Integer>()).collect(Collectors.toCollection(ArrayList::new));
            List<List<Integer>> backWays = IntStream.range(0, n).mapToObj(i -> new ArrayList<Integer>()).collect(Collectors.toCollection(ArrayList::new));
            for (int i = 0; i < m; i++) {
                int[] tmp = Arrays.stream(reader.readLine().trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                int from = tmp[0] - 1;
                int to = tmp[1] - 1;
                ways.get(from).add(to);
                backWays.get(to).add(from);
            }
            List<Job> jobs = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                int[] tmp = Arrays.stream(reader.readLine().trim().split(" ")).mapToInt(Integer::parseInt).toArray();
                jobs.add(new Job(r[i], p[i], tmp[0], tmp[1], tmp[2], i));
            }
            List<Job> topsorted = topsort(jobs, ways, backWays);
            for (int i = topsorted.size() - 1; i >= 0; i--) {
                Job cur = topsorted.get(i);
                for (Integer v : ways.get(cur.ind)) {
                    Job to = jobs.get(v);
                    to.r = Math.max(to.r, cur.r + cur.p);
                }
            }
            topsorted.sort(Comparator.comparingLong(j -> j.r));
            List<Block> blocks = createBlocks(topsorted);
            Long ans = Long.MIN_VALUE;
            for (Block block : blocks) {
                ans = Math.max(ans, decompose(block, ways));
            }
            try (BufferedWriter out = Files.newBufferedWriter(Path.of("p1precpmtnrifmax.out"))) {
                out.write(ans + "");
                out.newLine();
                for (Job job : jobs) {
                    out.write((job.times.size() / 2) + " ");
                    for (Long t : job.times) {
                        out.write(t + " ");
                    }
                    out.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}