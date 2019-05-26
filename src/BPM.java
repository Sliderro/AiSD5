import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class BPM {


    private int k;
    private int i;
    private ArrayList<Integer>[] adjList;
    private int[][] capacity;
    private int[][] flow;
    private int[] P;
    private ArrayList<Integer> ints;
    private int source;
    private int sink;
    private int size;

    BPM(int k, int i) {
        this.k = k;
        this.i = i;
        size = (1 << k) * 2 + 2;
        source = 0;
        sink = size - 1;
        flow = new int[size][size];
        P = new int[size];
        ints = new ArrayList<>();
        for (int j = (1 << k) + 1; j <= (1 << k) * 2; j++) {
            ints.add(j);
        }
        capacity = new int[size][size];
        adjList = new ArrayList[size];
        for (int j = 0; j < size; j++) {
            adjList[j] = new ArrayList<>();
        }
        for (int j = 1; j <= 1 << k; j++) {
            adjList[0].add(j);
            adjList[j].add(0);
            capacity[0][j] = 1;
            Collections.shuffle(ints);
            for (int l = 0; l < i; l++) {
                adjList[j].add(ints.get(l));
                adjList[ints.get(l)].add(j);
                capacity[j][ints.get(l)] = 1;
            }
            adjList[(1 << k) + j].add(sink);
            adjList[sink].add((1 << k) + j);
            capacity[(1 << k) + j][sink] = 1;
        }
    }

    int maxFlow() {
        int f = 0;
        while (true) {
            int m = BFS();
            if (m == 0) break;
            f += m;
            int v = sink;
            while (v != source) {
                int u = P[v];
                flow[u][v] += m;
                flow[v][u] -= m;
                v = u;
            }
        }
        return f;
    }

    int BFS() {
        for (int j = 0; j < size; j++) {
            P[j] = -1;
        }
        //P[source] = -2;
        int[] M = new int[size];
        M[source] = Integer.MAX_VALUE;
        Queue<Integer> Q = new LinkedList<>();
        Q.add(source);
        while (Q.size() > 0) {
            int u = Q.poll();
            for (int j = 0; j < adjList[u].size(); j++) {
                int v = adjList[u].get(j);
                if (capacity[u][v] - flow[u][v] > 0 && P[v] == -1) {
                    P[v] = u;
                    M[v] = Math.min(M[u], capacity[u][v] - flow[u][v]);
                    if (v != sink)
                        Q.add(v);
                    else
                        return M[sink];
                }
            }
        }
        return 0;
    }

    void glpk() {
        File f = new File("maxmatch.mod");
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
                    "data;\n\n");
            pw.println();
            pw.printf("param n := %d;\n", (2 << k) + 2);
            pw.println();
            pw.println("param : E : a :=");
            for (int j = 0; j < adjList.length; j++) {
                for (Integer i : adjList[j]) {
                    pw.printf("  %d %d %d\n", j + 1, i + 1, 1);
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

    void printall() {
        for (int j = 0; j < adjList.length; j++) {
            for (int l = 0; l < adjList[j].size(); l++) {
                System.out.print(adjList[j].get(l) + " ");
            }
            System.out.println();
        }
    }

    void printA(int[] ints) {
        for (int i = 0; i < ints.length; i++) {
            System.out.print("[" + ints[i] + "]");
        }
        System.out.println();
    }

    void printflow() {
        for (int i = 0; i < flow.length; i++) {
            for (int j = 0; j < flow[i].length; j++) {
                System.out.print("[" + flow[i][j] + "]");
            }
            System.out.println();
        }
    }
}
