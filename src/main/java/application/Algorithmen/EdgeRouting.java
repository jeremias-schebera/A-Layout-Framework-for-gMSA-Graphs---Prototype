/*******************************************************************************
 * Copyright (c) 2020 Jeremias Schebera, Dirk Zeckzer, Daniel Wiegreffe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
import main.java.application.data.ChromosomePaths;
import main.java.application.data.Configuration;
import main.java.application.data.EdgeDirection;
import main.java.application.data.EdgeDirectionAtVertex;
import main.java.application.data.EdgeSugiyama;
import main.java.application.data.EdgesClassifiedByVerticalDirection;
import main.java.application.data.InEdgeComparator;
import main.java.application.data.Layer;
import main.java.application.data.OutEdgeComparator;
import main.java.application.data.VertexSugiyama;
import main.java.application.data.VerticalEdgeDirection;

/**
 *
 * @author zeckzer
 */
public class EdgeRouting {

    private final static double VERTICAL_SPACE_GAP = 10;

    // Input
    private int maxLayer;
    private int spaceFactor;
    private double edgeThicknessFactor;

    // Input / Output
    private Map<Integer, Layer> indexLayerAssociation;
    // also relative coordinates of Edges

    // Local
    //Max Needed Space Variables
    private double neededSpaceForUpAndDownEdges = 0;
    private double neededSpaceForStraightEdges = 0;
    private double neededInterLayerSpace = 0;

    public EdgeRouting(
        Configuration configuration,
        Map<Integer, Layer> indexLayerAssociation,
        int maxLayer
    ) {
        this.indexLayerAssociation = indexLayerAssociation;
        this.maxLayer = maxLayer;
        this.edgeThicknessFactor = configuration.getDrawingThicknessFactor();
        this.spaceFactor = configuration.getSpaceFactor();
    }

    /** Determine drawing positions of edges
     *
     */
    public void computeRouting() {
        //Phase I
        //layer-loop --> vertex-sort, vertex-loop --> edge-sorting out-edge-loop, in-edge-loop, sort 4 edge classes
        preProcessing();

        //Phase II
        //quasi two time layer-loop --> vertex-loop --> quasi edge-loop
        placeStartAndEndPoints();

        //Phase III
        //layer-loop --> vertex-loop
        calculateVerticalPartOrder();
        //layer-loop --> loop over all out-edges of layer
        setVerticalEdgePositions();

        //Phase IV
        //layer-loop
        setSpaceVariables();

    }

    private void setSpaceVariables() {
        //set maximum of needed inter layer space
        for (int layerIndex = 0; layerIndex <= maxLayer; layerIndex++) {
            Layer layer = indexLayerAssociation.get(layerIndex);
            layer.setNeededInterLayerSpace(neededInterLayerSpace);
            layer.setNeededVertexHeight(2 * neededSpaceForUpAndDownEdges);
        }
    }

    private void calculateVerticalPartOrder() {
        for (int layerIndex = 0; layerIndex < maxLayer; layerIndex++) {
            Layer outLayer = indexLayerAssociation.get(layerIndex);
            outLayer.initializeMiddelPointXOrder();
            List<EdgeSugiyama> rightVerticalOrder = outLayer.getMiddelPointXOrder(VerticalEdgeDirection.UP);
            List<EdgeSugiyama> leftVerticalOrder = outLayer.getMiddelPointXOrder(VerticalEdgeDirection.DOWN);
            for (VertexSugiyama vertex : outLayer.getVertexOrderList()) {
                rightVerticalOrder.addAll(vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP));
                List<EdgeSugiyama> reversedOrderDownEdges = new ArrayList<>(vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.DOWN));
                Collections.reverse(reversedOrderDownEdges);
                leftVerticalOrder.addAll(0, reversedOrderDownEdges);
            }
        }
    }

    private void setVerticalEdgePositions() {
        for (int layerIndex = 0; layerIndex < maxLayer; layerIndex++) {
            Layer outLayer = indexLayerAssociation.get(layerIndex);
            double neededInterLayerSpace = VERTICAL_SPACE_GAP + edgeThicknessFactor * spaceFactor;
            for (EdgeSugiyama downEdge : outLayer.getMiddelPointXOrder(VerticalEdgeDirection.DOWN)) {
                neededInterLayerSpace = addVerticalEdgePositions(downEdge, VerticalEdgeDirection.DOWN, neededInterLayerSpace);
            }

//            neededInterLayerSpace += edgeThicknessFactor * spaceFactor;
            for (EdgeSugiyama upEdge : outLayer.getMiddelPointXOrder(VerticalEdgeDirection.UP)) {
                neededInterLayerSpace = addVerticalEdgePositions(upEdge, VerticalEdgeDirection.UP, neededInterLayerSpace);
            }

            neededInterLayerSpace += VERTICAL_SPACE_GAP;

            this.neededInterLayerSpace = Math.max(neededInterLayerSpace, this.neededInterLayerSpace);
        }
    }

    private double addVerticalEdgePositions(
        EdgeSugiyama edge,
        VerticalEdgeDirection verticalDirection,
        double neededInterLayerSpace
    ) {
        ChromosomePaths chromosomePaths = edge.getAssociatedChromosomes();
        if (verticalDirection.equals(VerticalEdgeDirection.DOWN)) {
            if (!chromosomePaths.isEmpty(EdgeDirection.FORWARD)
                && chromosomePaths.isEmpty(EdgeDirection.BACKWARD)) {
                //only Forward
                double forwardThickness = chromosomePaths.size(EdgeDirection.FORWARD) * edgeThicknessFactor;
                edge.setxMiddelPoint(EdgeDirection.FORWARD, neededInterLayerSpace);

                neededInterLayerSpace += forwardThickness;
            } else if (chromosomePaths.isEmpty(EdgeDirection.FORWARD)
                       && !chromosomePaths.isEmpty(EdgeDirection.BACKWARD)) {
                //only Backward
                double backwardThickness = chromosomePaths.size(EdgeDirection.BACKWARD) * edgeThicknessFactor;
                edge.setxMiddelPoint(EdgeDirection.BACKWARD, neededInterLayerSpace);

                neededInterLayerSpace += backwardThickness;
            } else {
                //Both directions
                double backwardThickness = chromosomePaths.size(EdgeDirection.BACKWARD) * edgeThicknessFactor;
                double forwardThickness = chromosomePaths.size(EdgeDirection.FORWARD) * edgeThicknessFactor;

                double backwardPosition = neededInterLayerSpace;
                double forwardPosition = neededInterLayerSpace + backwardThickness + edgeThicknessFactor * spaceFactor;

                edge.setxMiddelPoint(EdgeDirection.BACKWARD, backwardPosition);
                edge.setxMiddelPoint(EdgeDirection.FORWARD, forwardPosition);

                neededInterLayerSpace += backwardThickness + edgeThicknessFactor * spaceFactor + forwardThickness;
            }
        } else {
            if (!chromosomePaths.isEmpty(EdgeDirection.FORWARD)
                && chromosomePaths.isEmpty(EdgeDirection.BACKWARD)) {
                //only Forward
                double forwardThickness = chromosomePaths.size(EdgeDirection.FORWARD) * edgeThicknessFactor;
                edge.setxMiddelPoint(EdgeDirection.FORWARD, neededInterLayerSpace);

                neededInterLayerSpace += forwardThickness;
            } else if (chromosomePaths.isEmpty(EdgeDirection.FORWARD)
                       && !chromosomePaths.isEmpty(EdgeDirection.BACKWARD)) {
                //only Backward
                double backwardThickness = chromosomePaths.size(EdgeDirection.BACKWARD) * edgeThicknessFactor;
                edge.setxMiddelPoint(EdgeDirection.BACKWARD, neededInterLayerSpace);

                neededInterLayerSpace += backwardThickness;
            } else {
                //Both directions
                double forwardThickness = chromosomePaths.size(EdgeDirection.FORWARD) * edgeThicknessFactor;
                double backwardThickness = chromosomePaths.size(EdgeDirection.BACKWARD) * edgeThicknessFactor;

                double forwardPosition = neededInterLayerSpace;
                double backwardPosition = neededInterLayerSpace + forwardThickness + edgeThicknessFactor * spaceFactor;

                edge.setxMiddelPoint(EdgeDirection.BACKWARD, backwardPosition);
                edge.setxMiddelPoint(EdgeDirection.FORWARD, forwardPosition);

                neededInterLayerSpace += forwardThickness + edgeThicknessFactor * spaceFactor + backwardThickness;
            }
        }

        neededInterLayerSpace += edgeThicknessFactor + spaceFactor;

        return neededInterLayerSpace;
    }

    private void placeStartAndEndPoints() {
        for (int layerIndex = 0; layerIndex < maxLayer; layerIndex++) {
            Layer outLayer = indexLayerAssociation.get(layerIndex);
            Layer inLayer = indexLayerAssociation.get(layerIndex + 1);

            Iterator iteratorOutLayer = outLayer.getVertexOrderList().iterator();
            Iterator iteratorInLayer = inLayer.getVertexOrderList().iterator();

            VertexSugiyama currentOutVertex = getNextVertex(iteratorOutLayer);
            VertexSugiyama currentInVertex = getNextVertex(iteratorInLayer);
            while (currentOutVertex != null || currentInVertex != null) {
                if (currentOutVertex != null && currentInVertex == null) {
                    //only vertices in outLayer left
                    if (currentOutVertex.isDummyNode()) {
                        placeStraightOutEdgeStartingPositions(currentOutVertex.getOutEdges().get(0));
                    } else {
                        placeOutEdgesAtVertex(currentOutVertex);
                    }
                    currentOutVertex = getNextVertex(iteratorOutLayer);
                } else if (currentOutVertex == null && currentInVertex != null) {
                    //only vertices in inLayer left
                    if (currentInVertex.isDummyNode()) {
                        placeStraightInEdgeStartingPositions(currentInVertex.getInEdges().get(0));
                    } else {
                        placeInEdgesAtVertex(currentInVertex);
                    }
                    currentInVertex = getNextVertex(iteratorInLayer);
                } else {
                    //vertices in both layers left
                    if (currentOutVertex.getAssociatedBlockSet().getDrawingPosition() > currentInVertex.getAssociatedBlockSet().getDrawingPosition()) {
                        //process vertex from inLayer
                        if (currentInVertex.isDummyNode()) {
                            placeStraightInEdgeStartingPositions(currentInVertex.getInEdges().get(0));
                        } else {
                            placeInEdgesAtVertex(currentInVertex);
                        }
                        currentInVertex = getNextVertex(iteratorInLayer);
                    } else if (currentOutVertex.getAssociatedBlockSet().getDrawingPosition() < currentInVertex.getAssociatedBlockSet().getDrawingPosition()) {
                        //process vertex from outLayer
                        if (currentOutVertex.isDummyNode()) {
                            placeStraightOutEdgeStartingPositions(currentOutVertex.getOutEdges().get(0));
                        } else {
                            placeOutEdgesAtVertex(currentOutVertex);
                        }
                        currentOutVertex = getNextVertex(iteratorOutLayer);
                    } else {
                        //process both vertices from both layers simultaneously
                        if (currentOutVertex.isDummyNode() && !currentInVertex.isDummyNode()) {
                            //outVertex is dummy and inVertex is NOT dummy
                            placeStraightOutEdgeStartingPositions(currentOutVertex.getOutEdges().get(0));
                            placeInEdgesAtVertex(currentInVertex);
                        } else if (!currentOutVertex.isDummyNode() && currentInVertex.isDummyNode()) {
                            //outVertex is NOT dummy and inVertex is dummy
                            placeOutEdgesAtVertex(currentOutVertex);
                            placeStraightInEdgeStartingPositions(currentInVertex.getInEdges().get(0));
                        } else if (currentOutVertex.isDummyNode() && currentInVertex.isDummyNode()) {
                            //both are dummy
                            EdgeSugiyama outEdge = currentOutVertex.getOutEdges().get(0);
                            EdgeSugiyama inEdge = currentInVertex.getInEdges().get(0);

                            //special case: the vertical edge direction of both edges of the dummy vertices are up
                            if (!currentOutVertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP).isEmpty()
                                && !currentInVertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP).isEmpty()) {
                                placeEdgesBetweenBothDummyVertices(outEdge, inEdge);
                            } else {
                                placeStraightOutEdgeStartingPositions(outEdge);
                                placeStraightInEdgeStartingPositions(inEdge);
                            }
                        } else {
                            //both are NOT dummy
                            placeEdgesAtOppositeVertices(currentOutVertex, currentInVertex);
                        }

//                        TO-DO: die zu platzierenden edges sortieren nach
//                          1. y-position der enden
//                          2. nach Seite
                        currentInVertex = getNextVertex(iteratorInLayer);
                        currentOutVertex = getNextVertex(iteratorOutLayer);
                    }
                }
            }
        }
    }

    private void placeEdgesBetweenBothDummyVertices(
        EdgeSugiyama outEdge,
        EdgeSugiyama inEdge
    ) {
        //out Edge Part --> calculate needed height + standard placeholder space
        double neededOutEdgeHeight;
        if (!outEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
            && outEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Forward
            double forwardHeight = outEdge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            outEdge.setyStart(EdgeDirection.FORWARD, -0.5 * forwardHeight);

            neededOutEdgeHeight = 0.5 * forwardHeight + edgeThicknessFactor * spaceFactor;
        } else if (outEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
                   && !outEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Backward
            double backwardHeight = outEdge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;
            outEdge.setyStart(EdgeDirection.BACKWARD, -0.5 * backwardHeight);

            neededOutEdgeHeight = 0.5 * backwardHeight + edgeThicknessFactor * spaceFactor;
        } else {
            //Both directions
            double forwardHeight = outEdge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            double backwardHeight = outEdge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;

            double forwardPosition = 0.0;
            double backwardPosition = forwardHeight + edgeThicknessFactor * spaceFactor;
            double middelPointPosition = (forwardHeight + edgeThicknessFactor * spaceFactor + backwardHeight) * 0.5;

            outEdge.setyStart(EdgeDirection.FORWARD, forwardPosition - middelPointPosition);
            outEdge.setyStart(EdgeDirection.BACKWARD, backwardPosition - middelPointPosition);

            neededOutEdgeHeight = middelPointPosition + edgeThicknessFactor * spaceFactor;
        }

        //in Edge Part --> shift the Edge up to avoid crossings
        if (!inEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
            && inEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Forward
            double forwardHeight = inEdge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            inEdge.setyEnd(EdgeDirection.FORWARD, neededOutEdgeHeight + 0.5 * forwardHeight);
        } else if (inEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
                   && !inEdge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Backward
            double backwardHeight = inEdge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;
            inEdge.setyEnd(EdgeDirection.BACKWARD, neededOutEdgeHeight + 0.5 * backwardHeight);
        } else {
            //Both directions
            // double forwardHeight = inEdge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            double backwardHeight = inEdge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;

            double backwardPosition = neededOutEdgeHeight;
            double forwardPosition = neededOutEdgeHeight + backwardHeight + edgeThicknessFactor * spaceFactor;

            inEdge.setyEnd(EdgeDirection.FORWARD, forwardPosition);
            inEdge.setyEnd(EdgeDirection.BACKWARD, backwardPosition);
        }

        inEdge.setShiftedInVertexEndPoint(true);

    }

    private void placeStraightOutEdgeStartingPositions(
        EdgeSugiyama edge
    ) {
        if (!edge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
            && edge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Forward
            double forwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            edge.setyStart(EdgeDirection.FORWARD, -0.5 * forwardHeight);
        } else if (edge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
                   && !edge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Backward
            double backwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;
            edge.setyStart(EdgeDirection.BACKWARD, -0.5 * backwardHeight);
        } else {
            //Both directions
            double forwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            double backwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;

            double forwardPosition = 0.0;
            double backwardPosition = forwardHeight + edgeThicknessFactor * spaceFactor;
            double middelPointPosition = (forwardHeight + edgeThicknessFactor * spaceFactor + backwardHeight) * 0.5;

            edge.setyStart(EdgeDirection.FORWARD, forwardPosition - middelPointPosition);
            edge.setyStart(EdgeDirection.BACKWARD, backwardPosition - middelPointPosition);
        }
    }

    private void placeStraightInEdgeStartingPositions(
        EdgeSugiyama edge
    ) {
        if (!edge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
            && edge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Forward
            double forwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            edge.setyEnd(EdgeDirection.FORWARD, -0.5 * forwardHeight);
        } else if (edge.getAssociatedChromosomes().isEmpty(EdgeDirection.FORWARD)
                   && !edge.getAssociatedChromosomes().isEmpty(EdgeDirection.BACKWARD)) {
            //only Backward
            double backwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;
            edge.setyEnd(EdgeDirection.BACKWARD, -0.5 * backwardHeight);
        } else {
            //Both directions
            double forwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.FORWARD) * edgeThicknessFactor;
            double backwardHeight = edge.getAssociatedChromosomes().size(EdgeDirection.BACKWARD) * edgeThicknessFactor;

            double forwardPosition = 0.0;
            double backwardPosition = forwardHeight + edgeThicknessFactor * spaceFactor;
            double middelPointPosition = (forwardHeight + edgeThicknessFactor * spaceFactor + backwardHeight) * 0.5;

            edge.setyEnd(EdgeDirection.FORWARD, forwardPosition - middelPointPosition);
            edge.setyEnd(EdgeDirection.BACKWARD, backwardPosition - middelPointPosition);
        }
    }

    private void placeOutEdgesAtVertex(
        VertexSugiyama vertex
    ) {
        // Up Edges
        List<EdgeSugiyama> reverseSortedUpEdges = new ArrayList<>(vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP));
        Collections.reverse(reverseSortedUpEdges);
        double currentPosition = -0.5 * neededSpaceForStraightEdges;
        for (EdgeSugiyama edge : reverseSortedUpEdges) {
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.OUT);
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.OUT);
        }
        neededSpaceForUpAndDownEdges = Math.max(neededSpaceForUpAndDownEdges, Math.abs(currentPosition));

        // Straight Edges
        if (!vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).isEmpty()) {
            EdgeSugiyama straightOutEdge = vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).get(0);
            placeStraightOutEdgeStartingPositions(straightOutEdge);
        }

        // Down Edges
        currentPosition = 0.5 * neededSpaceForStraightEdges;
        for (EdgeSugiyama edge : vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.DOWN)) {
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.OUT);
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.OUT);
        }
        neededSpaceForUpAndDownEdges = Math.max(neededSpaceForUpAndDownEdges, currentPosition);
    }

    private void placeInEdgesAtVertex(
        VertexSugiyama vertex
    ) {
        // Up Edges
        List<EdgeSugiyama> reverseSortedUpEdges = new ArrayList<>(vertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP));
        Collections.reverse(reverseSortedUpEdges);
        double currentPosition = -0.5 * neededSpaceForStraightEdges;
        for (EdgeSugiyama edge : reverseSortedUpEdges) {
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.IN);
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.IN);
        }
        neededSpaceForUpAndDownEdges = Math.max(neededSpaceForUpAndDownEdges, Math.abs(currentPosition));

        // Straight Edges
        if (!vertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).isEmpty()) {
            EdgeSugiyama straightOutEdge = vertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).get(0);
            placeStraightInEdgeStartingPositions(straightOutEdge);
        }

        // Down Edges
        currentPosition = 0.5 * neededSpaceForStraightEdges;
        for (EdgeSugiyama edge : vertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.DOWN)) {
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.IN);
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.IN);
        }
        neededSpaceForUpAndDownEdges = Math.max(neededSpaceForUpAndDownEdges, currentPosition);
    }

    private void placeEdgesAtOppositeVertices(
        VertexSugiyama outVertex,
        VertexSugiyama inVertex
    ) {
        // Up Edges
        List<EdgeSugiyama> reverseSortedUpOutEdges = new ArrayList<>(outVertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP));
        Collections.reverse(reverseSortedUpOutEdges);
        double currentPosition = -0.5 * neededSpaceForStraightEdges;
        for (EdgeSugiyama edge : reverseSortedUpOutEdges) {
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.OUT);
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.OUT);
        }

        List<EdgeSugiyama> reverseSortedUpInEdges = new ArrayList<>(inVertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP));
        Collections.reverse(reverseSortedUpInEdges);
        for (EdgeSugiyama edge : reverseSortedUpInEdges) {
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.IN);
            currentPosition = placeUpEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.IN);
        }

        neededSpaceForUpAndDownEdges = Math.max(neededSpaceForUpAndDownEdges, Math.abs(currentPosition));

        // Straight Edges
        if (!outVertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).isEmpty()) {
            EdgeSugiyama straightOutEdge = outVertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).get(0);
            placeStraightOutEdgeStartingPositions(straightOutEdge);
        }

        if (!inVertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).isEmpty()) {
            EdgeSugiyama straightOutEdge = inVertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT).get(0);
            placeStraightInEdgeStartingPositions(straightOutEdge);
        }

        // Down Edges
        currentPosition = 0.5 * neededSpaceForStraightEdges;
        for (EdgeSugiyama edge : outVertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.DOWN)) {
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.OUT);
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.OUT);
        }

        for (EdgeSugiyama edge : inVertex.getInEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.DOWN)) {
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.FORWARD, EdgeDirectionAtVertex.IN);
            currentPosition = placeDownEdgeStartingPositionsDependingOnDirection(edge, currentPosition, EdgeDirection.BACKWARD, EdgeDirectionAtVertex.IN);
        }

        neededSpaceForUpAndDownEdges = Math.max(neededSpaceForUpAndDownEdges, currentPosition);
    }

    private double placeUpEdgeStartingPositionsDependingOnDirection(
        EdgeSugiyama edge,
        double currentPosition,
        EdgeDirection edgeDirection,
        EdgeDirectionAtVertex edgeDirectionAtVertex
    ) {
        if (!edge.getAssociatedChromosomes().isEmpty(edgeDirection)) {
            double edgeHeight = edge.getAssociatedChromosomes().size(edgeDirection) * edgeThicknessFactor;
            //set position
            currentPosition -= edgeHeight;
            if (edgeDirectionAtVertex == EdgeDirectionAtVertex.OUT) {
                //start position
                edge.setyStart(edgeDirection, currentPosition);
            } else {
                //end position
                edge.setyEnd(edgeDirection, currentPosition);
            }
            //increase current position
            currentPosition -= edgeThicknessFactor * spaceFactor;
        }

        return currentPosition;
    }

    private double placeDownEdgeStartingPositionsDependingOnDirection(
        EdgeSugiyama edge,
        double currentPosition,
        EdgeDirection edgeDirection,
        EdgeDirectionAtVertex edgeDirectionAtVertex
    ) {
        if (!edge.getAssociatedChromosomes().isEmpty(edgeDirection)) {
            double edgeHeight = edge.getAssociatedChromosomes().size(edgeDirection) * edgeThicknessFactor;
            //set position
            if (edgeDirectionAtVertex == EdgeDirectionAtVertex.OUT) {
                //start position
                edge.setyStart(edgeDirection, currentPosition);
            } else {
                //end position
                edge.setyEnd(edgeDirection, currentPosition);
            }
            //increase current position
            currentPosition += edgeHeight;
            currentPosition += edgeThicknessFactor * spaceFactor;
        }

        return currentPosition;
    }

    private VertexSugiyama getNextVertex(Iterator iterator) {
        if (iterator.hasNext()) {
            return (VertexSugiyama) iterator.next();
        } else {
            return null;
        }
    }

    private void preProcessing() {
        for (int layerIndex = 0; layerIndex <= maxLayer; layerIndex++) {
            Layer layer = indexLayerAssociation.get(layerIndex);
            layer.sortOrderList();
            for (VertexSugiyama vertex : layer.getVertexOrderList()) {
                classifyAndSortEdges(vertex);
            }
        }
    }

    private void classifyAndSortEdges(
        VertexSugiyama vertex
    ) {
        // classify and sort outgoing edges
        double neededStraightOutEdgeSpace = classifyAndSortOutgoingEdges(vertex);

        // classify and sort incoming edges
        double neededStraightInEdgeSpace = classifyAndSortIncomingEdges(vertex);

        // use maximum
        neededSpaceForStraightEdges = Collections.max(Arrays.asList(neededStraightOutEdgeSpace, neededStraightInEdgeSpace, neededSpaceForStraightEdges));
    }

    private double classifyAndSortIncomingEdges(
        VertexSugiyama vertex
    ) {
        vertex.initializeInEdgeClassifiedByVerticalDirection();
        EdgesClassifiedByVerticalDirection inEdgesClassifiedByVerticalDirection = vertex.getInEdgesClassifiedByVerticalDirection();
        for (EdgeSugiyama inEdge : vertex.getInEdges()) {
            VerticalEdgeDirection verticalDirection = getInEdgeVerticalDirection(inEdge);
            inEdgesClassifiedByVerticalDirection.add(verticalDirection, inEdge);
        }
        Collections.sort(inEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.UP), new InEdgeComparator());
        Collections.sort(inEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.DOWN), new InEdgeComparator());
        // find maximal needed space per layer for the straight drawing area
        if (!inEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.STRAIGHT).isEmpty()) {
            return calculateNeededStraightEdgeSpace(inEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.STRAIGHT).get(0).getAssociatedChromosomes());
        } else {
            return 0.0;
        }
    }

    private double classifyAndSortOutgoingEdges(
        VertexSugiyama vertex
    ) {
        vertex.initializeOutEdgeClassifiedByVerticalDirection();
        EdgesClassifiedByVerticalDirection outEdgesClassifiedByVerticalDirection = vertex.getOutEdgesClassifiedByVerticalDirection();
        for (EdgeSugiyama outEdge : vertex.getOutEdges()) {
            VerticalEdgeDirection verticalDirection = getOutEdgeVerticalDirection(outEdge);
            outEdgesClassifiedByVerticalDirection.add(verticalDirection, outEdge);
        }
        Collections.sort(outEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.UP), new OutEdgeComparator());
        Collections.sort(outEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.DOWN), new OutEdgeComparator());
        // find maximal needed space per layer for the straight drawing area
        if (!outEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.STRAIGHT).isEmpty()) {
            return calculateNeededStraightEdgeSpace(outEdgesClassifiedByVerticalDirection.get(VerticalEdgeDirection.STRAIGHT).get(0).getAssociatedChromosomes());
        } else {
            return 0.0;
        }
    }

    private VerticalEdgeDirection getOutEdgeVerticalDirection(
        EdgeSugiyama outEdge
    ) {
        int outVertexYPosition = outEdge.getOutNode().getAssociatedBlockSet().getDrawingPosition();
        int inVertexYPosition = outEdge.getInNode().getAssociatedBlockSet().getDrawingPosition();
        if (outVertexYPosition > inVertexYPosition) {
            return VerticalEdgeDirection.UP;
        } else if (outVertexYPosition == inVertexYPosition) {
            return VerticalEdgeDirection.STRAIGHT;
        } else {
            return VerticalEdgeDirection.DOWN;
        }
    }

    private VerticalEdgeDirection getInEdgeVerticalDirection(
        EdgeSugiyama inEdge
    ) {
        int outVertexYPosition = inEdge.getOutNode().getAssociatedBlockSet().getDrawingPosition();
        int inVertexYPosition = inEdge.getInNode().getAssociatedBlockSet().getDrawingPosition();
        if (outVertexYPosition < inVertexYPosition) {
            return VerticalEdgeDirection.UP;
        } else if (outVertexYPosition == inVertexYPosition) {
            return VerticalEdgeDirection.STRAIGHT;
        } else {
            return VerticalEdgeDirection.DOWN;
        }
    }

    private double calculateNeededStraightEdgeSpace(
        ChromosomePaths chromosomePaths
    ) {
        double neededSpace = edgeThicknessFactor * spaceFactor;

        neededSpace += calculateNeededStraightEdgeSpace(chromosomePaths, EdgeDirection.FORWARD);
        neededSpace += calculateNeededStraightEdgeSpace(chromosomePaths, EdgeDirection.BACKWARD);

        return neededSpace;
    }

    private double calculateNeededStraightEdgeSpace(
        ChromosomePaths chromosomePaths,
        EdgeDirection edgeDirection
    ) {
        double neededSpace = 0.0;
        if (!chromosomePaths.isEmpty(edgeDirection)) {
            neededSpace += edgeThicknessFactor * chromosomePaths.size(edgeDirection);
            neededSpace += edgeThicknessFactor * spaceFactor;
        }
        return neededSpace;
    }
}
