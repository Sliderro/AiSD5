import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class Graph {
    private Random r = new Random();
    private int k;
    private int[][] capacity;
    private int[][] flow;
    private int[] P;

    Graph(int k) {
        this.k = k;
        capacity = new int[1 << k][k];
        for (int i = 0; i < (1 << k); i++) {
            for (int j = 0; j < k; j++) {
                capacity[i][j] = ((i & (1 << j)) == 0) ? randomize(i, j) : 0;
            }
        }
        flow = new int[1 << k][k];
        P = new int[1 << k];
    }

    private int randomize(int number, int index) {
        return r.nextInt(1 << (Math.max(maxHZ(number), maxHZ(number + 1 << index)))) + 1;
    }

    private int maxHZ(int i) {
        i = i - ((i >> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >> 2) & 0x33333333);
        i = (((i + (i >> 4)) & 0x0F0F0F0F) * 0x01010101) >> 24;
        return Math.max(i, k - i);
    }

    int maxFlow() {
        long start = System.nanoTime();
        int f = 0;
        while (true) {
            int m = BFS();
            if (m == 0) break;
            f += m;
            int v = (1 << k) - 1;
            while (v != 0) {
                int u = P[v];
                int diff = binlog(u ^ v);
                flow[u][diff] += m;
                flow[v][diff] -= m;
                v = u;
            }
        }
        long stop = System.nanoTime();
        System.out.println(TimeUnit.MILLISECONDS.convert(stop - start,TimeUnit.NANOSECONDS));
        return f;
    }

    int BFS() {
        for (int i = 0; i < (1 << k); i++) {
            P[i] = -1;
        }
        P[0] = -2;
        int[] M = new int[1 << k];
        M[0] = Integer.MAX_VALUE;
        Queue<Integer> Q = new LinkedList<>();
        Q.add(0);
        while (!Q.isEmpty()) {
            int u = Q.poll();
            for (int v = 0; v < k; v++) {
                int p = (u & (1 << v)) == 0 ? u + (1 << v) : u - (1 << v);
                if (capacity[u][v] > flow[u][v] && P[p] == -1) {
                    P[p] = u;
                    M[p] = Math.min(M[u], capacity[u][v] - flow[u][v]);
                    if (p != ((1 << k) - 1)) Q.add(p);
                    else return M[(1 << k) - 1];
                }
            }
        }
        return 0;
    }

    int binlog(int bits) {
        int log = 0;
        if ((bits & 0xffff0000) != 0) {
            bits >>>= 16;
            log = 16;
        }
        if (bits >= 256) {
            bits >>>= 8;
            log += 8;
        }
        if (bits >= 16) {
            bits >>>= 4;
            log += 4;
        }
        if (bits >= 4) {
            bits >>>= 2;
            log += 2;
        }
        return log + (bits >>> 1);
    }
}
