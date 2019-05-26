public class Main {
    public static void main(String[] args){
        /*System.out.printf("%2s%11s%9s%10s\n","k","Flow","Paths","Time");
        for(int i=1; i<=0; i++){
            int flow = 0;
            int paths = 0;
            double time = 0;
            for(int j=0; j<1; j++){
                Graph g = new Graph(i);
                g.maxFlow();
                flow += g.maxFlow;
                paths += g.paths;
                time += g.time;
            }
            System.out.printf("%2d%11.2f%9.2f%10f\n",i,flow/100.0,paths/100.0,time/100.0);
        }*/
        BPM bpm = new BPM(9,2);
        System.out.println(bpm.maxFlow());
        bpm.glpk();
    }
}
