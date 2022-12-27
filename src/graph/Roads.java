package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Roads<T extends Comparable<T>> {

    public Graph<T> load(String name, IDataFactory<T> dataFactory) {
        try (BufferedReader sc = new BufferedReader(new FileReader("roads/" + name + ".gr"))) {
            
            Graph<T> graph = new Graph<>(0, Integer.MAX_VALUE / 2);
            
            sc.readLine();
            sc.readLine();
            sc.readLine();
            sc.readLine();
            
            String[] countsline = sc.readLine().split("[ ]");
            int vertexCount = Integer.parseInt(countsline[2]);
            int edgeCount   = Integer.parseInt(countsline[3]);
            
            sc.readLine();
            sc.readLine();
            
            for (int i = 1; i <= vertexCount; i++) {
                graph.addVertex(i, dataFactory.makeData());
            }
            
            for (int i = 0; i < edgeCount; i++) {
                String[] line = sc.readLine().split("[ ]");
                
                int idsrc = Integer.parseInt(line[1]);
                int iddst = Integer.parseInt(line[2]);
                int cost  = Integer.parseInt(line[3]);
                
                graph.addEdge(idsrc, iddst, cost);
            }
            
            System.out.println("Roads :: graph loaded " + name + "\n");
            
            return graph;
            
        } catch (IOException ex) {
            throw new RuntimeException("File not found: " + ex.getMessage());
        }
    }
    
}
