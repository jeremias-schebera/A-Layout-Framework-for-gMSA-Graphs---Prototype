package main.java.application.data;

import java.util.*;

public class Layer {

    private HashMap<VertexSugiyama, Integer> associationVertexPosition;
    private List<VertexSugiyama> vertexOrderList;
    private int index;
    private int longestAlignmentBlockInLayer;
    private double currentXMiddelPointPosition;
    private double xPosition;
    private EdgesClassifiedByVerticalDirection middelPointXOrder;

    private double neededVertexHeight;
    private double neededInterLayerSpace;

    public Layer(int index) {
        this.index = index;
        this.associationVertexPosition = new HashMap<>();
        this.vertexOrderList = new LinkedList<>();
        this.longestAlignmentBlockInLayer = 0;
        this.currentXMiddelPointPosition = 0;
        this.neededVertexHeight = 0.0;
        this.neededInterLayerSpace = 0.0;
        this.xPosition = 0.0;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getXPosition() {
        return xPosition;
    }

    public void setXPosition(double xPosition) {
        this.xPosition = xPosition;
    }

    public double getCurrentXMiddelPointPosition() {
        return currentXMiddelPointPosition;
    }

    public void setCurrentXMiddelPointPosition(double currentXMiddelPointPosition) {
        this.currentXMiddelPointPosition = currentXMiddelPointPosition;
    }

    public List<VertexSugiyama> getVertexOrderList() {
        return vertexOrderList;
    }

    public void initializeMiddelPointXOrder() {
        middelPointXOrder = new EdgesClassifiedByVerticalDirection();
    }

    public double getNeededVertexHeight() {
        return neededVertexHeight;
    }

    public void setNeededVertexHeight(double neededVertexHeight) {
        this.neededVertexHeight = neededVertexHeight;
    }

    public List<EdgeSugiyama> getMiddelPointXOrder(
        VerticalEdgeDirection verticalEdgeDirection
    ) {
        return middelPointXOrder.get(verticalEdgeDirection);
    }

    public double getNeededInterLayerSpace() {
        return neededInterLayerSpace;
    }

    public void setNeededInterLayerSpace(double neededInterLayerSpace) {
        this.neededInterLayerSpace = neededInterLayerSpace;
    }



    public void sortOrderList() {
        Collections.sort(vertexOrderList, new VertexLayerIndexComparator());
    }

    public void addToVerticesInLayer(VertexSugiyama vertex) {
        vertexOrderList.add(vertex);
        associationVertexPosition.put(vertex, vertexOrderList.size() - 1);
    }

    public int getLongestAlignmentBlockInLayer() {
        return longestAlignmentBlockInLayer;
    }

    public void setLongestAlignmentBlockInLayer(int longestAlignmentBlockInLayer) {
        this.longestAlignmentBlockInLayer = longestAlignmentBlockInLayer;
    }
}
