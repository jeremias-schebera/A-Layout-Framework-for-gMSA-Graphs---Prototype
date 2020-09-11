/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.application.Algorithmen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import main.java.application.data.*;

/**
 *
 * @author zeckzer
 */
public class EdgeRoutingOLD {

    // Input / Output
    private Map<Integer, Layer> indexLayerAssociation;
    private int maxLayer;

    // Output
    private double maximalHalfNeededEdgeSpaceOnVertex;
    private double maximalNeededEdgeSpaceBetweenLayers;
    private int spaceFactor;

    // Local
    private final double verticalSpaceGap = 5;
    private double edgeThicknessFactor = 0;
    double neededRightUpEdgeSpace = 0;
    double neededRightDownEdgeSpace = 0;
    double neededLeftUpEdgeSpace = 0;
    double neededLeftDownEdgeSpace = 0;
    VerticalEdgeDirection formerVerticalDirection;
    boolean specialDummyShiftMarker;


    public EdgeRoutingOLD(
        Configuration configuration,
        Map<Integer, Layer> indexLayerAssociation,
        int maxLayer
    ) {
        this.indexLayerAssociation = indexLayerAssociation;
        this.maxLayer = maxLayer;
        this.maximalHalfNeededEdgeSpaceOnVertex = 0;
        this.edgeThicknessFactor = configuration.getDrawingThicknessFactor();
        this.spaceFactor = configuration.getSpaceFactor();
    }

    /** Determine drawing positions of edges
     *
     */
    public void computeRouting() {
        /// Initialize anchor points and sort
        for (int layerIndex = 0; layerIndex <= maxLayer; layerIndex++) {
            Layer layer = indexLayerAssociation.get(layerIndex);
            layer.initializeMiddelPointXOrder();
            layer.sortOrderList();
            for (VertexSugiyama vertex : layer.getVertexOrderList()) {
                Collections.sort(vertex.getOutEdges(), new OutEdgeComparator());
                Collections.sort(vertex.getInEdges(), new InEdgeComparator());
            }
        }

        for (int layerIndex = 0; layerIndex < maxLayer; layerIndex++) {
            Layer layer = indexLayerAssociation.get(layerIndex);
            double neededSpace;

            Iterator iteratorLeftLayer = layer.getVertexOrderList().iterator();
            Iterator iteratorRightLayer = indexLayerAssociation.get(layerIndex + 1).getVertexOrderList().iterator();
            VertexSugiyama currentLeftVertex = getNextVertex(iteratorLeftLayer);
            VertexSugiyama currentRightVertex = getNextVertex(iteratorRightLayer);
            while (currentLeftVertex != null || currentRightVertex != null) {

                if (currentLeftVertex != null && currentRightVertex == null) {

                    neededSpace = placeEdgesOnVerticesRight(currentLeftVertex);
                    checkMaximalHalfNeededEdgeSpaceOnVertex(neededSpace);

                    currentLeftVertex = getNextVertex(iteratorLeftLayer);
                } else if (currentLeftVertex == null && currentRightVertex != null) {

                    neededSpace = placeEdgesOnVerticesLeft(currentRightVertex);
                    checkMaximalHalfNeededEdgeSpaceOnVertex(neededSpace);

                    currentRightVertex = getNextVertex(iteratorRightLayer);
                } else {
                    if (currentLeftVertex.getAssociatedBlock().getPosition() > currentRightVertex.getAssociatedBlock().getPosition()) {

                        neededSpace = placeEdgesOnVerticesLeft(currentRightVertex);
                        checkMaximalHalfNeededEdgeSpaceOnVertex(neededSpace);

                        currentRightVertex = getNextVertex(iteratorRightLayer);
                    } else if (currentLeftVertex.getAssociatedBlock().getPosition() < currentRightVertex.getAssociatedBlock().getPosition()) {

                        neededSpace = placeEdgesOnVerticesRight(currentLeftVertex);
                        checkMaximalHalfNeededEdgeSpaceOnVertex(neededSpace);

                        currentLeftVertex = getNextVertex(iteratorLeftLayer);
                    } else {

                        neededSpace = placeEdgesBetween(currentLeftVertex, currentRightVertex);
                        checkMaximalHalfNeededEdgeSpaceOnVertex(neededSpace);

                        currentRightVertex = getNextVertex(iteratorRightLayer);
                        currentLeftVertex = getNextVertex(iteratorLeftLayer);
                    }
                }
            }

            layer.setCurrentXMiddelPointPosition(verticalSpaceGap);

            neededSpace = verticalSpaceGap;

            for (EdgeSugiyama edge : layer.getMiddelPointXOrder(VerticalEdgeDirection.DOWN)) {
                neededSpace += placeEdgeMiddelPoints(edge, VerticalEdgeDirection.DOWN);
            }

            for (EdgeSugiyama edge : layer.getMiddelPointXOrder(VerticalEdgeDirection.UP)) {
                neededSpace += placeEdgeMiddelPoints(edge, VerticalEdgeDirection.UP);
            }

            neededSpace += verticalSpaceGap;
            maximalNeededEdgeSpaceBetweenLayers = Math.max(neededSpace, maximalNeededEdgeSpaceBetweenLayers);
        }
    }

    private void checkMaximalHalfNeededEdgeSpaceOnVertex(double neededSpace) {
        if (neededSpace > maximalHalfNeededEdgeSpaceOnVertex) {
            maximalHalfNeededEdgeSpaceOnVertex = neededSpace;
        }
    }

    private Side switchSide(
            EdgeSugiyama currentOutEdge,
            EdgeSugiyama currentInEdge,
            Side side
    ) {
        if (currentOutEdge == null && currentInEdge != null) {
            side = Side.RIGHT;
        } else if (currentOutEdge != null && currentInEdge == null) {
            side = Side.LEFT;
        } else if (currentOutEdge == null && currentInEdge == null) {
            side = Side.UNDEFINED;
        } else {
            if (currentOutEdge.getBlockPositionDifference() < 0 && currentInEdge.getBlockPositionDifference() <= 0) {
                side = Side.LEFT;
            } else if (currentOutEdge.getBlockPositionDifference() >= 0 && currentInEdge.getBlockPositionDifference() > 0) {
                side = Side.RIGHT;
            } else {
                if (side.equals(Side.RIGHT)) {
                    side = Side.LEFT;
                } else if (side.equals(Side.LEFT)) {
                    side = Side.RIGHT;
                }

                if (currentOutEdge.getOutNode().isDummyNode()
                        && currentInEdge.getInNode().isDummyNode()
                        && (currentOutEdge.getBlockPositionDifference() < 0)
                        && (currentInEdge.getBlockPositionDifference() > 0)) {
                    specialDummyShiftMarker = true;
                }
            }
        }

        return side;
    }

    private double placeEdgesBetween(
        VertexSugiyama leftVertex,
        VertexSugiyama rightVertex
    ) {

        neededRightUpEdgeSpace = 0;
        neededRightDownEdgeSpace = 0;
        neededLeftUpEdgeSpace = 0;
        neededLeftDownEdgeSpace = 0;

        formerVerticalDirection = VerticalEdgeDirection.UNDEFINED;
        double yStartingpoint = spaceFactor * edgeThicknessFactor;

        Iterator outIterator = leftVertex.getOutEdges().iterator();
        Iterator inIterator = rightVertex.getInEdges().iterator();
        EdgeSugiyama currentOutEdge = getNextEdge(outIterator);
        EdgeSugiyama currentInEdge = getNextEdge(inIterator);

        Side side = Side.RIGHT;
        specialDummyShiftMarker = false;

        side = switchSide(currentOutEdge, currentInEdge, side);

        while (currentOutEdge != null || currentInEdge != null) {
            if (side.equals(Side.LEFT)) {
                yStartingpoint = chooseFreePositionOnRight(leftVertex, currentOutEdge, yStartingpoint);

                currentOutEdge = getNextEdge(outIterator);

                side = switchSide(currentOutEdge, currentInEdge, side);
            } else if (side.equals(Side.RIGHT)){
                yStartingpoint = chooseFreePositionOnLeft(rightVertex, currentInEdge, yStartingpoint, specialDummyShiftMarker);

                currentInEdge = getNextEdge(inIterator);

                side = switchSide(currentOutEdge, currentInEdge, side);
            }
        }

        return Collections.max(Arrays.asList(neededLeftDownEdgeSpace, neededLeftUpEdgeSpace, neededRightDownEdgeSpace, neededRightUpEdgeSpace));
    }

    private double placeEdgesOnVerticesLeft(
        VertexSugiyama vertex
    ) {

        neededLeftUpEdgeSpace = 0;
        neededLeftDownEdgeSpace = 0;

        formerVerticalDirection = VerticalEdgeDirection.UNDEFINED;
        double leftYStartingpoint = spaceFactor * edgeThicknessFactor;

        for (EdgeSugiyama inEdge : vertex.getInEdges()) {

            leftYStartingpoint = chooseFreePositionOnLeft(vertex, inEdge, leftYStartingpoint, false);
        }

        return Math.max(neededLeftUpEdgeSpace, neededLeftDownEdgeSpace);
    }

    private double shiftStartDrawingPoint(
        EdgeSugiyama edge,
        double startingPoint
    ) {
        startingPoint = shiftStartDrawingPoint(edge, EdgeDirection.FORWARD, startingPoint);
        startingPoint = shiftStartDrawingPoint(edge, EdgeDirection.BACKWARD, startingPoint);

        return startingPoint;
    }

    private double shiftStartDrawingPoint(
        EdgeSugiyama edge,
        EdgeDirection direction,
        double startingPoint
    ) {
        if (!edge.getAssociatedChromosomes().isEmpty(direction)) {
            edge.setyStart(direction, startingPoint);
            startingPoint += edge.getDrawingThicknesByDirection(direction) + edge.getDrawingThicknesFactor() * spaceFactor;
        }
        return startingPoint;
    }

    private double shiftStartDrawingPointDummy(
        EdgeSugiyama edge
    ) {
        List<EdgeDirection> directions = new ArrayList<>();
        directions.add(EdgeDirection.FORWARD);
        directions.add(EdgeDirection.BACKWARD);

        EdgeSugiyama formerEdge = edge.getOutNode().getInEdges().get(0);
        double position = 0;
        EdgeDirection lastDirection = EdgeDirection.FORWARD;

        for (EdgeDirection direction : directions) {
            if (!edge.getAssociatedChromosomes().isEmpty(direction)) {

//                startingPoint += 0.5 * edge.getDrawingThicknesByDircetion(direction);
                position = formerEdge.getyEnd(direction);
                edge.setyStart(direction, position);
                lastDirection = direction;
            }
        }
        position += edge.getDrawingThicknesByDirection(lastDirection) + edge.getDrawingThicknesFactor() * spaceFactor;

        return position;
    }

    private double shiftEndDrawingPointDummy(
        EdgeSugiyama edge
    ) {
        List<EdgeDirection> directions = new ArrayList<>();
        directions.add(EdgeDirection.FORWARD);
        directions.add(EdgeDirection.BACKWARD);

        EdgeSugiyama formerEdge = edge.getOutNode().getInEdges().get(0);
        double position = 0;
        EdgeDirection lastDirection = EdgeDirection.FORWARD;

        for (EdgeDirection direction : directions) {
            if (!edge.getAssociatedChromosomes().isEmpty(direction)) {

//                startingPoint += 0.5 * edge.getDrawingThicknesByDircetion(direction);
                position = formerEdge.getyEnd(direction);
                edge.setyEnd(direction, position);
                lastDirection = direction;
            }
        }
        position += edge.getDrawingThicknesByDirection(lastDirection) + edge.getDrawingThicknesFactor() * spaceFactor;

        return position;
    }

    private double shiftEndDrawingPoint(
        EdgeSugiyama edge,
        double startingPoint
    ) {
        startingPoint = shiftEndDrawingPoints(edge, EdgeDirection.FORWARD, startingPoint);
        startingPoint = shiftEndDrawingPoints(edge, EdgeDirection.BACKWARD, startingPoint);

        return startingPoint;
    }

    private double shiftEndDrawingPoints(
        EdgeSugiyama edge,
        EdgeDirection direction,
        double startingPoint
    ) {
        if (!edge.getAssociatedChromosomes().isEmpty(direction)) {
            edge.setyEnd(direction, startingPoint);
            startingPoint += edge.getDrawingThicknesByDirection(direction) + edge.getDrawingThicknesFactor() * spaceFactor;
        }
        return startingPoint;
    }

    private double placeEdgesOnVerticesRight(
        VertexSugiyama vertex
    ) {

        neededRightUpEdgeSpace = 0;
        neededRightDownEdgeSpace = 0;

        formerVerticalDirection = VerticalEdgeDirection.UNDEFINED;
        double rightYStartingpoint = spaceFactor * edgeThicknessFactor;

        for(EdgeSugiyama outEdge : vertex.getOutEdges()) {
            rightYStartingpoint = chooseFreePositionOnRight(vertex, outEdge, rightYStartingpoint);
        }

        return Math.max(neededRightUpEdgeSpace, neededRightDownEdgeSpace);
    }

    private double chooseFreePositionOnRight(
            VertexSugiyama vertex,
            EdgeSugiyama outEdge,
            double rightYStartingpoint
    ) {
        List<EdgeSugiyama> leftOutEdgeMiddelPointXOrder = vertex.getAssociatedLayer().getMiddelPointXOrder(VerticalEdgeDirection.DOWN);
        List<EdgeSugiyama> rightOutEdgeMiddelPointXOrder = vertex.getAssociatedLayer().getMiddelPointXOrder(VerticalEdgeDirection.UP);
        List<EdgeSugiyama> straightEdges = vertex.getAssociatedLayer().getMiddelPointXOrder(VerticalEdgeDirection.STRAIGHT);

        if (outEdge.getBlockPositionDifference() < 0) {
            if (vertex.isDummyNode()) {
                rightYStartingpoint = -0.5 * spaceFactor * edgeThicknessFactor - outEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD);
            }

            rightOutEdgeMiddelPointXOrder.add(outEdge);
            formerVerticalDirection = VerticalEdgeDirection.UP;
            rightYStartingpoint = shiftStartDrawingPoint(outEdge, rightYStartingpoint);
            neededRightUpEdgeSpace = rightYStartingpoint;
        } else if (outEdge.getBlockPositionDifference() == 0) {

            neededRightUpEdgeSpace += spaceFactor * edgeThicknessFactor + outEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD) + 0.5 * spaceFactor * edgeThicknessFactor;

            straightEdges.add(outEdge);
            formerVerticalDirection = VerticalEdgeDirection.STRAIGHT;
            rightYStartingpoint = -0.5 * spaceFactor * edgeThicknessFactor - outEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD);
            rightYStartingpoint = shiftStartDrawingPoint(outEdge, rightYStartingpoint);
        } else {
            leftOutEdgeMiddelPointXOrder.add(0, outEdge);
            if (formerVerticalDirection.equals(VerticalEdgeDirection.UP) || formerVerticalDirection.equals(VerticalEdgeDirection.UNDEFINED)) {
                rightYStartingpoint = 0.5 * spaceFactor * edgeThicknessFactor;
            }

            if (vertex.isDummyNode()) {
                rightYStartingpoint = -0.5 * spaceFactor * edgeThicknessFactor - outEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD);
            }

            formerVerticalDirection = VerticalEdgeDirection.DOWN;
            rightYStartingpoint = shiftStartDrawingPoint(outEdge, rightYStartingpoint);
            neededRightDownEdgeSpace = rightYStartingpoint;
        }

        return rightYStartingpoint;
    }

    private double chooseFreePositionOnLeft(
            VertexSugiyama vertex,
            EdgeSugiyama inEdge,
            double leftYStartingpoint,
            boolean specialDummyShiftMarker
    ) {
        if (inEdge.getBlockPositionDifference() > 0) {
            if (vertex.isDummyNode() && !specialDummyShiftMarker) {
                leftYStartingpoint = -0.5 * spaceFactor * edgeThicknessFactor - inEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD);
            }

            formerVerticalDirection = VerticalEdgeDirection.UP;
            leftYStartingpoint = shiftEndDrawingPoint(inEdge, leftYStartingpoint);
            neededLeftUpEdgeSpace = leftYStartingpoint;
        } else if (inEdge.getBlockPositionDifference() == 0) {

            neededLeftUpEdgeSpace += spaceFactor * edgeThicknessFactor + inEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD) + 0.5 * spaceFactor * edgeThicknessFactor;

            formerVerticalDirection = VerticalEdgeDirection.STRAIGHT;
            leftYStartingpoint = -0.5 * spaceFactor * edgeThicknessFactor - inEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD);
            leftYStartingpoint = shiftEndDrawingPoint(inEdge, leftYStartingpoint);
        } else {
            if (formerVerticalDirection.equals(VerticalEdgeDirection.UP) || formerVerticalDirection.equals(VerticalEdgeDirection.UNDEFINED)) {
                leftYStartingpoint = 0.5 * spaceFactor * edgeThicknessFactor;
            }

            if (vertex.isDummyNode()) {
                leftYStartingpoint = -0.5 * spaceFactor * edgeThicknessFactor - inEdge.getDrawingThicknesByDirection(EdgeDirection.FORWARD);
            }

            formerVerticalDirection = VerticalEdgeDirection.DOWN;
            leftYStartingpoint = shiftEndDrawingPoint(inEdge, leftYStartingpoint);
            neededLeftDownEdgeSpace = leftYStartingpoint;
        }

        return leftYStartingpoint;
    }

    private double placeEdgeMiddelPoints(
        EdgeSugiyama edge,
        VerticalEdgeDirection verticalDirection
    ) {
        if (verticalDirection.equals(VerticalEdgeDirection.DOWN)) {
            placeEdgeMiddelPoints(edge, EdgeDirection.BACKWARD, verticalDirection);
            placeEdgeMiddelPoints(edge, EdgeDirection.FORWARD, verticalDirection);
        } else {
            placeEdgeMiddelPoints(edge, EdgeDirection.FORWARD, verticalDirection);
            placeEdgeMiddelPoints(edge, EdgeDirection.BACKWARD, verticalDirection);
        }

        return edge.getOutNode().getAssociatedLayer().getCurrentXMiddelPointPosition();
    }

    private void placeEdgeMiddelPoints(
        EdgeSugiyama edge,
        EdgeDirection direction,
        VerticalEdgeDirection verticalDirection
    ) {
        if (!edge.getAssociatedChromosomes().isEmpty(direction)) {
            double edgeThickness = edge.getDrawingThicknesFactor() * edge.getAssociatedChromosomes().size(direction);

            if (verticalDirection.equals(VerticalEdgeDirection.UP) || verticalDirection.equals(VerticalEdgeDirection.STRAIGHT)) {
                edge.setxMiddelPoint(direction, edge.getOutNode().getAssociatedLayer().getCurrentXMiddelPointPosition());
            } else {
                edge.setxMiddelPoint(direction, edge.getOutNode().getAssociatedLayer().getCurrentXMiddelPointPosition() + edgeThickness);
            }

            edge.getOutNode().getAssociatedLayer().setCurrentXMiddelPointPosition(edge.getOutNode().getAssociatedLayer().getCurrentXMiddelPointPosition() + edgeThickness + spaceFactor * edge.getDrawingThicknesFactor());
        }
    }

    private VertexSugiyama getNextVertex(Iterator iterator) {
        if (iterator.hasNext()) {
            return (VertexSugiyama) iterator.next();
        } else {
            return null;
        }
    }

    private EdgeSugiyama getNextEdge(Iterator iterator) {
        if (iterator.hasNext()) {
            return (EdgeSugiyama) iterator.next();
        } else {
            return null;
        }
    }

    public double getMaximalHalfNeededEdgeSpaceOnVertex() {
        return maximalHalfNeededEdgeSpaceOnVertex;
    }

    public double getMaximalNeededEdgeSpaceBetweenLayers() {
        return maximalNeededEdgeSpaceBetweenLayers;
    }
}
