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

import javafx.util.Pair;
import main.java.application.data.DummyPathEdgeSugiyama;
import main.java.application.data.Block;
import main.java.application.data.EdgeSugiyama;
import main.java.application.data.Layer;
import main.java.application.data.VertexSugiyama;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zeckzer
 */
public class LayerAssignment {

    /// Input
    private List<VertexSugiyama> vertexList;
    private List<EdgeSugiyama> edgeList;
    private double drawingThicknessFactor;

    /// Local
    private int maxLayer = Integer.MIN_VALUE;
    private int dummyVertexCount = 1;
    private List<DummyPathEdgeSugiyama> dummyPathEdgesReplacedByDummyEdges = new ArrayList<>();
    //Test!!!
    private int longSpanEdgeCount = 0;
    private int blockCount;
    private int edgeCountDif = 0;
    //Test!!!

    /// Output
    private Map<Integer, Layer> indexLayerAssociation = new HashMap<>();
    private int longestAlignmentBlock;
    private int shortestAlignmentBlock;

    LayerAssignment(
        List<VertexSugiyama> vertexList,
        List<EdgeSugiyama> edgeList,
        double drawingThicknessFactor
    ) {
        this.vertexList = vertexList;
        this.edgeList = edgeList;
        this.drawingThicknessFactor = drawingThicknessFactor;
    }

    public List<DummyPathEdgeSugiyama> getDummyPathEdgesReplacedByDummyEdges() {
        return dummyPathEdgesReplacedByDummyEdges;
    }

    /** The Longest-Path Algo. - Handbook of Graph Drawing and Visualization, Page 421, Figure 13.6 --> adapted
     */
    void assignLayerWithLongestPathAndImprovement() {
        Map<VertexSugiyama, Integer> layering = assignLayer();

//        layering = vertexPromotion(layering);
        createLayersAndBlocks(layering);
        createDummyNodesAndBlocks();

        System.out.println("Draw and Dummy Vertex-Number: " + vertexList.size());

//        for (int index : indexLayerAssociation.keySet()) {
//            for (NodeSugiyama v : indexLayerAssociation.get(index).getVerticesInLayer()) {
//                System.out.println(index + " - " + v.getId());
//            }
//        }
    }

    private Map<VertexSugiyama, Integer> assignLayer() {
        Map<VertexSugiyama, Integer> layering = new HashMap<>();

        List<VertexSugiyama> currentSchedule = new ArrayList<>();
        Map<VertexSugiyama, Integer> outEdgesPerVertex = new HashMap<>();

        int layerIndex = 0;
        for (VertexSugiyama vertex : vertexList) {
            int numberOutEdges = vertex.getOutDegree();
            if (numberOutEdges == 0) {
                currentSchedule.add(vertex);
                layering.put(vertex, layerIndex);
            } else {
                outEdgesPerVertex.put(vertex, numberOutEdges);
            }
        }

        layerIndex = 1;
        while (outEdgesPerVertex.size() > 0) {
//            Layer layer = new Layer(layerIndex);
            List<VertexSugiyama> nextSchedule = new ArrayList<>();
            for (VertexSugiyama vertex : currentSchedule) {
                for (EdgeSugiyama backEdge : vertex.getInEdges()) {
                    VertexSugiyama currentVertex = backEdge.getOutNode();
                    int newOutEdgeNumber = outEdgesPerVertex.get(currentVertex) - 1;
                    if (newOutEdgeNumber == 0) {
                        outEdgesPerVertex.remove(currentVertex);
                        nextSchedule.add(currentVertex);
                        layering.put(currentVertex, layerIndex);
//                        indexLayerAssociation.put(layerIndex, layer);
//                        layer.addToVerticesInLayer(currentVertex);
//                        currentVertex.setAssociatedLayer(layer);
                    } else {
                        outEdgesPerVertex.put(currentVertex, newOutEdgeNumber);
                    }
                }
            }
            currentSchedule = nextSchedule;
            layerIndex++;
        }

        return layering;
    }

    private Map<VertexSugiyama, Integer> vertexPromotion(
        Map<VertexSugiyama, Integer> layering
    ) {
        //Vertex Promotion --> improvement
        /*
        HashMap<VertexSugiyama, Integer> layeringBackUp = new HashMap<>(layering);
        int promotion;
        do {
            promotion = 0;
            for (VertexSugiyama vertex : layering.keySet()) {
                if (vertex.getInDegree() > 0) {
                    if (promoteNode(vertex, layering) < 0) {
                        promotion++;
                        layeringBackUp = new HashMap<>(layering);
                    } else {
                        layering = new HashMap<>(layeringBackUp);
                    }
                }
            }
        } while (promotion != 0);
         */
        return layering;
    }

    private void createLayersAndBlocks(
        Map<VertexSugiyama, Integer> layering
    ) {
        // create Layer-Objects and create Blocks
        for (Map.Entry<VertexSugiyama, Integer> layeringEntry : layering.entrySet()) {
            VertexSugiyama vertex = layeringEntry.getKey();
            int index = layeringEntry.getValue();
            if (index > maxLayer) {
                maxLayer = index;
            }
            Layer currentLayer;
            if (indexLayerAssociation.containsKey(index)) {
                currentLayer = indexLayerAssociation.get(index);
            } else {
                currentLayer = new Layer(index);
                indexLayerAssociation.put(index, currentLayer);
            }

            if (vertex.getSequenceLength() > currentLayer.getLongestAlignmentBlockInLayer()) {
                currentLayer.setLongestAlignmentBlockInLayer(vertex.getSequenceLength());
                if (vertex.getSequenceLength() > longestAlignmentBlock) {
                    longestAlignmentBlock = vertex.getSequenceLength();
                }
            }
            if (vertex.getSequenceLength() < shortestAlignmentBlock) {
                shortestAlignmentBlock = vertex.getSequenceLength();
            }

            vertex.setAssociatedLayer(currentLayer);
            currentLayer.addToVerticesInLayer(vertex);

            Block block = new Block(vertex, 0);
            vertex.getAssociatedBlockSet().addBlock(block);
            //Test!!!
            blockCount++;
            //Test!!!
        }
    }

    /** create Dummy Vertices and the new Edges between them and delete the old Edge
     * Post Processing of Layering
     */
    private void createDummyNodesAndBlocks() {
        List<EdgeSugiyama> newEdges = new ArrayList<>();
        List<EdgeSugiyama> deleteEdges = new ArrayList<>();
        for (EdgeSugiyama outGoingEdge : edgeList) {
            int dummyNumber = outGoingEdge.layerDifference() - 1;
            if (dummyNumber > 0) {
                //Test!!!
                longSpanEdgeCount++;
                //Test!!!
                int firstIndex = outGoingEdge.getOutNode().getAssociatedLayer().getIndex();
                List<List<EdgeSugiyama>> dummyEdges = createDummyEdgesAndBlocks(outGoingEdge, dummyNumber, firstIndex);
                newEdges.addAll(dummyEdges.get(0));
                deleteEdges.addAll(dummyEdges.get(1));
            }
        }

        for (EdgeSugiyama edge : deleteEdges) {
            edge.getOutNode().removeOutEdge(edge);
            edge.getInNode().removeInEdge(edge);
            edgeList.remove(edge);
        }

        for (EdgeSugiyama edge : newEdges) {
            edgeList.add(edge);
        }
    }

    /** create Dummy Vertices and the new Edges between them and mark the original edge to delete
     * Post Processing of Layering
     */
    private List<List<EdgeSugiyama>> createDummyEdgesAndBlocks(
        EdgeSugiyama edgeToReplace,
        int dummyNumber,
        int firstIndex
    ) {
        List<List<EdgeSugiyama>> result = new ArrayList<>();

        List<EdgeSugiyama> newEdges = new ArrayList<>();
        List<EdgeSugiyama> deleteEdges = new ArrayList<>();

        VertexSugiyama oldOutNode = edgeToReplace.getOutNode();
        VertexSugiyama oldInNode = edgeToReplace.getInNode();
        VertexSugiyama newOutNode = oldOutNode;
        VertexSugiyama newInNode;
        List<EdgeSugiyama> dummyEdges = new ArrayList<>();
        List<VertexSugiyama> blockVertices = new ArrayList<>();
        int layerIndex = firstIndex;
        for (int dummyCount = 0; dummyCount < dummyNumber; dummyCount++) {
            VertexSugiyama dummyVertex = new VertexSugiyama(true, dummyVertexCount);
            dummyVertexCount++;
            blockVertices.add(0, dummyVertex);

            layerIndex--;
            Layer layer = indexLayerAssociation.get(layerIndex);
            layer.addToVerticesInLayer(dummyVertex);
            dummyVertex.setAssociatedLayer(layer);
            vertexList.add(dummyVertex);

            newInNode = dummyVertex;

            createDummyEdge(newOutNode, newInNode, edgeToReplace, dummyEdges, newEdges);
            newOutNode = newInNode;
        }

        createDummyEdge(newOutNode, oldInNode, edgeToReplace, dummyEdges, newEdges);
        deleteEdges.add(edgeToReplace);

        Block block = new Block(blockVertices, 0);
        //Test!!!
        blockCount++;
        //Test!!!

        edgeToReplace.addVerticesAndBlocks(blockVertices, block);
        DummyPathEdgeSugiyama dummyPathEdgeSugiyama = new DummyPathEdgeSugiyama(edgeToReplace, dummyEdges);
        dummyPathEdgesReplacedByDummyEdges.add(dummyPathEdgeSugiyama);

//        blockList.add(block);
//        blockListPosition.put(block, blockList.size() - 1);
        result.add(newEdges);
        result.add(deleteEdges);
        return result;
    }

    private void createDummyEdge(
        VertexSugiyama newOutNode,
        VertexSugiyama newInNode,
        EdgeSugiyama edgeToReplace,
        List<EdgeSugiyama> dummyEdges,
        List<EdgeSugiyama> newEdges
    ) {
        EdgeSugiyama dummyEdge = new EdgeSugiyama(newOutNode,
                                                  newInNode,
                                                  edgeToReplace.getAssociatedChromosomes(),
                                                  drawingThicknessFactor);
        dummyEdges.add(dummyEdge);
        dummyEdge.setDirection(edgeToReplace.getDirection());
        newEdges.add(dummyEdge);
    }

    /** Post-Processing
     *
     */
    int deleteDummyEdgesAndCreateDrawingEdges(int vertexNumber) {
        for (DummyPathEdgeSugiyama dummyPathEdgeSugiyama : dummyPathEdgesReplacedByDummyEdges) {
            Pair<Integer, Integer> values = dummyPathEdgeSugiyama.deleteDummyEdgesAndCreateDrawingEdges(edgeList, vertexNumber);
            edgeCountDif += values.getKey();
            vertexNumber = values.getValue();
        }
        return vertexNumber;
    }

    Map<Integer, Layer> getIndexLayerAssociation() {
        return indexLayerAssociation;
    }

    int getMaxLayer() {
        return maxLayer;
    }

    int getLongestAlignmentBlock() {
        return longestAlignmentBlock;
    }

    int getShortestAlignmentBlock() {
        return shortestAlignmentBlock;
    }

    //Test!!!
    public StringBuffer csvLine(StringBuffer sb) {
        sb.append((maxLayer+1) + ";" + longSpanEdgeCount + ";" + (dummyVertexCount-1) + ";" + blockCount + ";");
        return sb;
    }

    public StringBuffer csvLine2(StringBuffer sb) {
        sb.append(edgeCountDif + ";");
        return sb;
    }
    //Test!!!
}
