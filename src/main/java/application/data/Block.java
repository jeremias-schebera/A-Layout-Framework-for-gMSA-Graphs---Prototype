package main.java.application.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Block {

    private VertexSugiyama upperVertex;
    private VertexSugiyama lowerVertex;
    private List<VertexSugiyama> includingVertices;
    private VertexSugiyama[] neighborsOut;
    private VertexSugiyama[] neighborsIn;
    private int highestNeighborOutIndex;
    private int highestNeighborInIndex;
    private int[] indicesNeighbourOut;
    private int[] indicesNeighbourIn;
    private int position;
    private Map<Integer, VertexSugiyama> levels;
    private BlockSet associatedBlockSet = null;

    public Block(
        List<VertexSugiyama> includingVertices,
        int position
    ) {
        this.includingVertices = includingVertices;
        lowerVertex = includingVertices.get(0);
        upperVertex = includingVertices.get(includingVertices.size() - 1);
        this.position = position;

        initializeArrays();

        levels = new HashMap<>();
        for (VertexSugiyama vertex : includingVertices) {
            vertex.setAssociatedBlock(this);
            levels.put(vertex.getAssociatedLayer().getIndex(), vertex);
        }
    }

    public Block(
        VertexSugiyama vertexBlock,
        int position
    ) {
        this.includingVertices = new ArrayList<>();
        includingVertices.add(vertexBlock);
        lowerVertex = vertexBlock;
        upperVertex = vertexBlock;
        this.position = position;

        initializeArrays();

        levels = new HashMap<>();
        vertexBlock.setAssociatedBlock(this);
        levels.put(vertexBlock.getAssociatedLayer().getIndex(), vertexBlock);
    }

    private void initializeArrays() {
        neighborsIn = new VertexSugiyama[upperVertex.getInEdges().size()];
        neighborsOut = new VertexSugiyama[lowerVertex.getOutEdges().size()];
        indicesNeighbourIn = new int[upperVertex.getInEdges().size()];
        indicesNeighbourOut = new int[lowerVertex.getOutEdges().size()];
        highestNeighborInIndex = 0;
        highestNeighborOutIndex = 0;
    }

    public BlockSet getAssociatedBlockSet() {
        return associatedBlockSet;
    }

    public void setAssociatedBlockSet(
        BlockSet associatedBlockSet
    ) {
        this.associatedBlockSet = associatedBlockSet;
    }

    public void setLevel() {
        for (VertexSugiyama vertex : includingVertices) {
            levels.put(vertex.getAssociatedLayer().getIndex(), vertex);
        }
    }

    public List<VertexSugiyama> getIncludingVertices() {
        return includingVertices;
    }

    public Map<Integer, VertexSugiyama> getLevels() {
        return levels;
    }

    public void swapEntries(
        Direction direction,
        int siftingIndex,
        int nextIndex
    ) {
        if (direction.equals(Direction.PLUS)) {
            VertexSugiyama tempSwapVertex = neighborsIn[siftingIndex];
            neighborsIn[siftingIndex] = neighborsIn[nextIndex];
            neighborsIn[nextIndex] = tempSwapVertex;

            int tempSwapIndex = indicesNeighbourIn[siftingIndex];
            indicesNeighbourIn[siftingIndex] = indicesNeighbourIn[nextIndex];
            indicesNeighbourIn[nextIndex] = tempSwapIndex;
        } else {
            VertexSugiyama tempSwapVertex = neighborsOut[siftingIndex];
            neighborsOut[siftingIndex] = neighborsOut[nextIndex];
            neighborsOut[nextIndex] = tempSwapVertex;

            int tempSwapIndex = indicesNeighbourOut[siftingIndex];
            indicesNeighbourOut[siftingIndex] = indicesNeighbourOut[nextIndex];
            indicesNeighbourOut[nextIndex] = tempSwapIndex;
        }
    }

    public int[] getIndicesNeighbourOut() {
        return indicesNeighbourOut;
    }

    public int[] getIndicesNeighbourIn() {
        return indicesNeighbourIn;
    }

    public VertexSugiyama[] getNeighborsOut() {
        return neighborsOut;
    }

    public VertexSugiyama[] getNeighborsIn() {
        return neighborsIn;
    }

    public void setNeighborOutIndexEntry(
        int neighborOutIndex,
        int neighborOutIndexEntry
    ) {
        indicesNeighbourOut[neighborOutIndex] = neighborOutIndexEntry;
    }

    public void setNeighborInIndexEntry(
        int neighborInIndex,
        int neighborInIndexEntry
    ) {
        indicesNeighbourIn[neighborInIndex] = neighborInIndexEntry;
    }

    public int getHighestNeighborOutIndex() {
        return highestNeighborOutIndex;
    }

    public int getHighestNeighborInIndex() {
        return highestNeighborInIndex;
    }

    public void addNeighborOut(
        VertexSugiyama inVertex
    ) {
        neighborsOut[highestNeighborOutIndex] = inVertex;
        highestNeighborOutIndex++;
    }

    public void addNeighborIn(
        VertexSugiyama outVertex
    ) {
        neighborsIn[highestNeighborInIndex] = outVertex;
        highestNeighborInIndex++;
    }

    public VertexSugiyama getUpperVertex() {
        return upperVertex;
    }

    public VertexSugiyama getLowerVertex() {
        return lowerVertex;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getBlockVerticesNames() {
        String text = "";
        for (VertexSugiyama v : includingVertices) {
            text += v.getId() + ", ";
        }
        return text;
    }

    public void printBlockVertices() {
        String text = "Block: ";
        for (VertexSugiyama v : includingVertices) {
            text += v.getId() + ", ";
        }
        System.out.println(text);
    }

    public void printBlockArrays() {
        String text = "N+: ";
        for (VertexSugiyama v : neighborsOut) {
            text += v.getId() + ", ";
        }
        text += "\nN-: ";
        for (VertexSugiyama v : neighborsIn) {
            text += v.getId() + ", ";
        }
        text += "\nI+: ";
        for (int index : indicesNeighbourOut) {
            text += index + ", ";
        }
        text += "\nI-: ";
        for (int index : indicesNeighbourIn) {
            text += index + ", ";
        }
        System.out.println(text);
    }

    public void clearAllNeighbourAndIndicesLists() {
        initializeArrays();
    }
}
