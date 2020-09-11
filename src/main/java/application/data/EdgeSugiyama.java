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
package main.java.application.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EdgeSugiyama {

    private VertexSugiyama inNode;
    private VertexSugiyama outNode;
    private EdgeDirection direction;
    private BlockSet associatedBlockSet;
    private ChromosomePaths associatedChromosomes;
    private double drawingThicknesFactor;
    private Map<EdgeDirection, Double> yStart;
    private Map<EdgeDirection, Double> yEnd;
    private Map<EdgeDirection, Double> xVerticalPosition;

    private boolean shiftedInVertexEndPoint;

    public EdgeSugiyama(
        VertexSugiyama outNode,
        VertexSugiyama inNode,
        double drawingThicknesFactor
    ) {
        init(outNode, inNode, drawingThicknesFactor);
    }

    public EdgeSugiyama(
        VertexSugiyama outNode,
        VertexSugiyama inNode,
        ChromosomePaths associatedChromosomes,
        double drawingThicknesFactor
    ) {
        init(outNode, inNode, drawingThicknesFactor);

        this.associatedChromosomes = associatedChromosomes;

        initRoutingPoints();
    }

    private void init(
        VertexSugiyama outNode,
        VertexSugiyama inNode,
        double drawingThicknesFactor
    ) {
        this.inNode = inNode;
        this.outNode = outNode;

        inNode.addInEdge(this);
        outNode.addOutEdge(this);

        this.direction = EdgeDirection.FORWARD;
        this.drawingThicknesFactor = drawingThicknesFactor;
        this.shiftedInVertexEndPoint = false;
    }

    public void createChromosomePaths(Chromosome chromosome) {
        associatedChromosomes = new ChromosomePaths();
        associatedChromosomes.add(direction, chromosome);

        initRoutingPoints();
    }

    private void initRoutingPoints() {
        yStart = new HashMap<>();
        yStart.put(EdgeDirection.BACKWARD, 0.0);
        yStart.put(EdgeDirection.FORWARD, 0.0);

        yEnd = new HashMap<>();
        yEnd.put(EdgeDirection.BACKWARD, 0.0);
        yEnd.put(EdgeDirection.FORWARD, 0.0);

        xVerticalPosition = new HashMap<>();
        xVerticalPosition.put(EdgeDirection.BACKWARD, 0.0);
        xVerticalPosition.put(EdgeDirection.FORWARD, 0.0);
    }

    public void addChromosomePath(Chromosome chromosome, EdgeDirection direction) {
        associatedChromosomes.add(direction, chromosome);
    }

    public double getDrawingThicknes(int spaceFactor) {
        if (associatedChromosomes.isEmpty(EdgeDirection.BACKWARD)) {
            return associatedChromosomes.size(EdgeDirection.FORWARD) * drawingThicknesFactor;
        } else if (associatedChromosomes.isEmpty(EdgeDirection.FORWARD)) {
            return associatedChromosomes.size(EdgeDirection.BACKWARD) * drawingThicknesFactor;
        } else {
            return associatedChromosomes.size(EdgeDirection.FORWARD) * drawingThicknesFactor
                   + spaceFactor * drawingThicknesFactor
                   + associatedChromosomes.size(EdgeDirection.BACKWARD) * drawingThicknesFactor;
        }
    }

    public double getDrawingThicknesByDirection(EdgeDirection direction) {
        return associatedChromosomes.size(direction) * drawingThicknesFactor;
    }

    public double getDrawingThicknesFactor() {
        return drawingThicknesFactor;
    }

    public ChromosomePaths getAssociatedChromosomes() {
        return associatedChromosomes;
    }

    public void setAssociatedBlockSet(BlockSet associatedBlockSet) {
        this.associatedBlockSet = associatedBlockSet;
    }

    public void addVerticesAndBlocks(
        List<VertexSugiyama> blockVertices,
        Block block
    ) {
        associatedBlockSet.addVerticesAndBlocks(blockVertices, block);
    }

    public void increaseYStart(
        EdgeDirection direction,
        double increase
    ) {
        yStart.put(direction, yStart.get(direction) + increase);
    }

    public double getyStart(
        EdgeDirection direction
    ) {
        return yStart.get(direction);
    }

    public void setyStart(
        EdgeDirection direction,
        double position
    ) {
        yStart.put(direction, position);
    }

    public void increaseyEnd(
        EdgeDirection direction,
        double increase
    ) {
        yEnd.put(direction, yEnd.get(direction) + increase);
    }

    public double getyEnd(
        EdgeDirection direction
    ) {
        return yEnd.get(direction);
    }

    public void setyEnd(
        EdgeDirection direction,
        double position
    ) {
        yEnd.put(direction, position);
    }

    public boolean isShiftedInVertexEndPoint() {
        return shiftedInVertexEndPoint;
    }

    public void setShiftedInVertexEndPoint(boolean shiftedInVertexEndPoint) {
        this.shiftedInVertexEndPoint = shiftedInVertexEndPoint;
    }

    public double getxMiddelPoint(
        EdgeDirection direction
    ) {
        return xVerticalPosition.get(direction);
    }

    public void setxMiddelPoint(
        EdgeDirection direction,
        double value
    ) {
        xVerticalPosition.put(direction, value);
    }

    public int getBlockPositionDifference() {
        return inNode.getAssociatedBlock().getPosition() - outNode.getAssociatedBlock().getPosition();
    }

    //    public boolean isRemovedForAcyclic() {
//        return isRemovedForAcyclic;
//    }
//    public void setRemovedForAcyclic(boolean removedForAcyclic) {
//        isRemovedForAcyclic = removedForAcyclic;
//    }
    public void printNodes() {
        System.out.println(outNode.getId() + "-->" + inNode.getId());
    }

//    public Path getPathIdentifier() {
//        return pathIdentifier;
//    }
    public VertexSugiyama getInNode() {
        return inNode;
    }

    public VertexSugiyama getOutNode() {
        return outNode;
    }

    public EdgeDirection getDirection() {
        return direction;
    }

    public void setDirection(EdgeDirection direction) {
        this.direction = direction;
    }

    public int layerDifference() {
        return Math.abs(outNode.getAssociatedLayer().getIndex() - inNode.getAssociatedLayer().getIndex());
    }
}
