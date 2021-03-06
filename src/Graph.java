import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

class Graph {
    public int maxFlow;
    public int paths;
    public double time;
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
        int p = 0;
        while (true) {
            int m = BFS();
            if (m == 0) break;
            p++;
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
        this.maxFlow = f;
        this.paths = p;
        this.time = (stop - start) / 1000000000.0;
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

    void glpk() {
        File f = new File("maxflow.mod");
        try {
            FileWriter fileWriter = new FileWriter(f);
            PrintWriter pw = new PrintWriter(fileWriter);
            pw.print("param n, integer, >= 2;\n" +
                    "/* number of nodes */\n" +
                    "\n" +
                    "set V, default {1..n};\n" +
                    "/* set of nodes */\n" +
                    "\n" +
                    "set E, within V cross V;\n" +
                    "/* set of arcs */\n" +
                    "\n" +
                    "param a{(i,j) in E}, > 0;\n" +
                    "/* a[i,j] is capacity of arc (i,j) */\n" +
                    "\n" +
                    "param s, symbolic, in V, default 1;\n" +
                    "/* source node */\n" +
                    "\n" +
                    "param t, symbolic, in V, != s, default n;\n" +
                    "/* sink node */\n" +
                    "\n" +
                    "var x{(i,j) in E}, >= 0, <= a[i,j];\n" +
                    "/* x[i,j] is elementary flow through arc (i,j) to be found */\n" +
                    "\n" +
                    "var flow, >= 0;\n" +
                    "/* total flow from s to t */\n" +
                    "\n" +
                    "s.t. node{i in V}:\n" +
                    "/* node[i] is conservation constraint for node i */\n" +
                    "\n" +
                    "   sum{(j,i) in E} x[j,i] + (if i = s then flow)\n" +
                    "   /* summary flow into node i through all ingoing arcs */\n" +
                    "\n" +
                    "   = /* must be equal to */\n" +
                    "\n" +
                    "   sum{(i,j) in E} x[i,j] + (if i = t then flow);\n" +
                    "   /* summary flow from node i through all outgoing arcs */\n" +
                    "\n" +
                    "maximize obj: flow;\n" +
                    "/* objective is to maximize the total flow through the network */\n" +
                    "\n" +
                    "solve;\n" +
                    "\n" +
                    "printf{1..56} \"=\"; printf \"\\n\";\n" +
                    "printf \"Maximum flow from node %s to node %s is %g\\n\\n\", s, t, flow;\n" +
                    "\n" +
                    "data;\n");
            pw.println();
            pw.printf("param n := %d;\n", 1 << k);
            pw.println();
            pw.println("param : E : a :=");
            for (int i = 0; i < (1 << k); i++) {
                for (int j = 0; j < k; j++) {
                    if (capacity[i][j] != 0) {
                        int s = i + (1 << j) + 1;
                        pw.printf("  %d %d %d\n", i + 1, s, capacity[i][j]);
                    }
                }
            }
            pw.println(";\n");
            pw.print("end;");
            pw.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void debug(){
        for(int[] ints: flow){
            for(int i: ints){
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }

    void printflow() {
        for (int i = 0; i < capacity.length; i++) {
            for (int j = 0; j < capacity[i].length; j++) {
                System.out.print("["+capacity[i][j]+"]");
            }
            System.out.println();
        }
    }
}
