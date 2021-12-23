import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class G {

    private static int getTime(int[] a, int ind) {
        return ind < 0 ? Integer.MAX_VALUE - 1_000_000 : a[ind];
    }

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of("r2cmax.in"))) {
            int n = Integer.parseInt(reader.readLine());
            int[] p1 = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int[] p2 = Arrays.stream(reader.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
            int maxTime = Arrays.stream(p2).sum();
            int[] prev = new int[maxTime + 1];
            int[] cur = new int[maxTime + 1];
            Arrays.fill(prev, Integer.MAX_VALUE - 1_000_000);
            Arrays.fill(cur, Integer.MAX_VALUE - 1_000_000);
            prev[0] = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j <= maxTime; j++) {
                    cur[j] = Math.min(prev[j] + p1[i], getTime(prev, j - p2[i]));
                }
                int[] tmp = prev;
                prev = cur;
                cur = tmp;
            }
            int ans = Integer.MAX_VALUE;
            for (int i = 0; i <= maxTime; i++) {
                ans = Math.min(ans, Math.max(i, prev[i]));
            }
            try (BufferedWriter out = Files.newBufferedWriter(Path.of("r2cmax.out"))) {
                out.write(ans + "");
            }
        }
    }
}
