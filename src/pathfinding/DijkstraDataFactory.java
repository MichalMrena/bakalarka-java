package pathfinding;

import graph.IDataFactory;

public class DijkstraDataFactory implements IDataFactory<DijkstraData> {

    @Override
    public DijkstraData makeData() {
        return new DijkstraData();
    }

}
