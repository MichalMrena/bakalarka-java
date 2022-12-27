package main;

import datastructures.factory.BinaryHeapFactory;
import datastructures.factory.BrodalQueueFactory;
import datastructures.factory.FibonacciHeapFactory;
import datastructures.factory.IPrioQueueFactory;
import datastructures.factory.StrictFibonacciHeapFactory;
import graph.Graph;
import graph.Roads;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;
import pathfinding.Dijkstra;
import pathfinding.DijkstraData;
import pathfinding.DijkstraDataFactory;
import pathfinding.PathInfo;
import test.TesterPrioQueue;
import utils.ValueStabilizer;

public class Main {

    static void correctnessTest(IPrioQueueFactory factory) {
        TesterPrioQueue test = new TesterPrioQueue(2_000_000, 7);
        test.test(factory);
        System.out.println("Test successful !");
    }
    
    static void labelSetExperiment(IPrioQueueFactory factory) {
        String[] graphNames = {
            "USA-road-d.BAY",
            "USA-road-d.COL",
            "USA-road-d.FLA",
            "USA-road-d.NW",
            "USA-road-d.NE",
            "USA-road-d.CAL",
            "USA-road-d.LKS",
            "USA-road-d.E",
            "USA-road-d.W"
        };
        
        Random rng = new Random(911);
        try (PrintWriter pw = new PrintWriter(new File("results/" + factory.getClass().getSimpleName() + ".csv"))) {
        
            for (String graphName : graphNames) {
                Roads<DijkstraData> roads = new Roads<DijkstraData>();
                Graph<DijkstraData> graph = roads.load(graphName, new DijkstraDataFactory());
                Dijkstra pathfinder = new Dijkstra(graph);
                ValueStabilizer stabilizer = new ValueStabilizer(100);
                int replications = 0;
                
                while (!stabilizer.isStable()) {
                    int idSrc = rng.nextInt(graph.getVertexCount()) + 1;
                    System.out.println("src : " + idSrc);
                    PathInfo info = pathfinder.findPath(idSrc, factory);
                    stabilizer.addVal(info.getTimeTaken());
                    ++replications;
                }
                
                System.out.println("replicatins : " + replications);
                pw.println(graph.getVertexCount() + ";" + stabilizer.getLastAvg());
            }
        
        } catch (FileNotFoundException ex) {
            System.out.println("This sould not happen!!!");
        }
    }
    
    public static void main(String[] args) {
        correctnessTest(new BinaryHeapFactory());
        correctnessTest(new FibonacciHeapFactory());
        correctnessTest(new BrodalQueueFactory());
        correctnessTest(new StrictFibonacciHeapFactory());
        
        labelSetExperiment(new BinaryHeapFactory());
        labelSetExperiment(new FibonacciHeapFactory());
        labelSetExperiment(new BrodalQueueFactory());
        labelSetExperiment(new StrictFibonacciHeapFactory());
    }
    
}
