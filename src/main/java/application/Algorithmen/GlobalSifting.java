/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.application.Algorithmen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.util.Pair;
import main.java.application.data.Block;
import main.java.application.data.BlockSet;
import main.java.application.data.Configuration;
import main.java.application.data.Direction;
import main.java.application.data.EdgeSugiyama;
import main.java.application.data.Layer;
import main.java.application.data.VertexSugiyama;

/**
 *
 * @author zeckzer
 */
public class GlobalSifting {

    /// Input
    private Map<Integer, Layer> indexLayerAssociation;
    private int maxLayer;
    /// also Output
    private List<BlockSet> blockSetList = new ArrayList<>();

    /// Local
    private Map<BlockSet, Integer> blockSetListPosition = new HashMap<>();

    public GlobalSifting(
        Map<Integer, Layer> indexLayerAssociation,
        int maxLayer
    ) {
        this.indexLayerAssociation = indexLayerAssociation;
        this.maxLayer = maxLayer;
    }

    /** Initialize BlockSet order
     *  preparation for Global Crossing Reduction
     */
    public void createInitialBlockSetList() {
        for (int index = 0; index <= maxLayer; index++) {
            Layer layer = indexLayerAssociation.get(index);
//            System.out.println("Layer: " + layer.getIndex());
//            String text = layer.getIndex() + ": ";
            for (VertexSugiyama vertex : layer.getVertexOrderList()) {
//                text += vertex.getId() + ", ";
//                System.out.println("Vertex: " + vertex.getId());
                BlockSet blockSet = vertex.getAssociatedBlockSet();
                if (!blockSetListPosition.containsKey(blockSet)) {

                    blockSet.sortIncludingBlocks();

                    blockSetList.add(blockSet);
                    blockSetListPosition.put(blockSet, blockSetList.size() - 1);
//                    blockSet.printBlockSetVertices();
                    for (Block block : blockSet.getIncludingBlocks()) {
                        block.setLevel();
//                        block.printBlockVertices();
                    }

                    //delete connected empty BlockSets
                    List<BlockSet> toDelete = new ArrayList<>();
                    for (BlockSet connectedBlockSet : blockSet.getConnectedBlockSets()) {
                        if (connectedBlockSet.getIncludingBlocks().isEmpty()) {
                            toDelete.add(connectedBlockSet);
                        }
                    }
                    blockSet.getConnectedBlockSets().removeAll(toDelete);
                }
            }
//            System.out.println(text);
        }
    }

    //***********************TEST***********************
    private void blockPositionEqualsBlockSetPosition() {
        int pos = 0;
        for (BlockSet bs : blockSetList) {
            for (Block b : bs.getIncludingBlocks()) {
                b.setPosition(pos);
            }
            pos++;
        }
    }
    //***********************TEST***********************

    /** Global k-level Crossing Reduction Algorithm from Bachmaier et al. --> adapted
     * And set BlockSet Position for drawing
     * @param siftingRounds number of sifting rounds
     * @return order list of block set with heuristically reduced amount of edge crossings
     */
    public List<BlockSet> globalSifting(
        int siftingRounds
    ) {

        //Global Crossing Reduction
        for (int round = 1; round <= siftingRounds; round++) {
            List<BlockSet> copyBlockSetList = new ArrayList<>(blockSetList);

            for (BlockSet blockSet : copyBlockSetList) {
                siftingStep(blockSet);
            }
        }

        return blockSetList;
    }

    /** Part of Global Crossing Reduction --> one Sifting Round
     *
     * @param siftingBlockSet
     */
    private void siftingStep(
        BlockSet siftingBlockSet
    ) {

//        System.out.println("SIFTING-BLOCK: " + siftingBlockSet.getBlockSetVertices());
        /// put the sifting block set on the first position
        blockSetList.remove((int) blockSetListPosition.get(siftingBlockSet));
        blockSetList.add(0, siftingBlockSet);
        blockSetListPosition.put(siftingBlockSet, 0);

        /// initializize block set and block positions
        initPositions();

        /// sort neighbor indices
        sortAdjacencies();

        /// perform sifting
        int currentCrossingNumber = 0;
        int bestCrossingNumber = 0;
        int bestSwapPosition = 0;
        for (int swapPosition = 1;
             swapPosition <= blockSetList.size() - 1;
             swapPosition++) {

            //***********************TEST***********************
//            System.out.println("sift <> next: " + siftingBlockSet.getBlockSetVertices() + " <> " + blockSetList.get(swapPosition).getBlockSetVertices());
            //***********************TEST***********************
            currentCrossingNumber += siftingSwap(siftingBlockSet, blockSetList.get(swapPosition));

            //***********************TEST***********************
//            System.out.println("current Crossing Number: " + currentCrossingNumber);
            //***********************TEST***********************

            if (currentCrossingNumber < bestCrossingNumber) {
                    bestCrossingNumber = currentCrossingNumber;
                    bestSwapPosition = swapPosition;
            }

        }

        /// apply best position for siftingBlockSet
        int currentPosition = blockSetListPosition.get(siftingBlockSet);
        blockSetList.remove(currentPosition);
        blockSetList.add(bestSwapPosition, siftingBlockSet);
        blockSetListPosition.put(siftingBlockSet, bestSwapPosition);

        /// update block set list positions for all blocks after the inserted block
        /// all other blocks did not change after the previous insertion
        for (int position = bestSwapPosition + 1; position < blockSetList.size(); position++) {
            blockSetListPosition.put(blockSetList.get(position), position);
        }
    }

    /** Part of Global Crossing Reduction --> sort Adjacency before every step
     *
     */
    private void initPositions() {
        /// Give each block set its own position
        /// Give each block its own position
        int position = 0;
        for (int blockSetIndex = 0; blockSetIndex <= blockSetList.size() - 1; blockSetIndex++) {
            BlockSet currentBlockSet = blockSetList.get(blockSetIndex);
            blockSetListPosition.put(currentBlockSet, blockSetIndex);
            for (Block currentBlock : currentBlockSet.getIncludingBlocks()) {
                currentBlock.setPosition(position);
                currentBlock.clearAllNeighbourAndIndicesLists();
                position++;
            }
        }
    }

    /** Part of Global Crossing Reduction --> sort Adjacency before every step
     *
     */
    private void sortAdjacencies() {
        ///
        Map<EdgeSugiyama, Integer> tempIndex = new HashMap<>();
        VertexSugiyama outVertex;
        Block outBlock;
        int outIndex;
        VertexSugiyama inVertex;
        Block inBlock;
        int inIndex;
        for (BlockSet blockSet : blockSetList) {
            for (Block block : blockSet.getIncludingBlocks()) {

                /// Handle incoming edges
                inVertex = block.getUpperVertex();
                for (EdgeSugiyama inEdge : inVertex.getInEdges()) {
                    outVertex = inEdge.getOutNode();
                    outBlock = outVertex.getAssociatedBlock();
                    outIndex = outBlock.getHighestNeighborOutIndex();

                    if (block.getPosition() < outBlock.getPosition()) {
                        tempIndex.put(inEdge, outIndex);
                    } else {
                        inIndex = tempIndex.get(inEdge);
                        outBlock.setNeighborOutIndexEntry(outIndex, inIndex);
                        block.setNeighborInIndexEntry(inIndex, outIndex);
                    }
                    outBlock.addNeighborOut(inVertex);
                }

                /// Handle outgoing edges
                outVertex = block.getLowerVertex();
                for (EdgeSugiyama outEdge : outVertex.getOutEdges()) {
                    inVertex = outEdge.getInNode();
                    inBlock = inVertex.getAssociatedBlock();
                    inIndex = inBlock.getHighestNeighborInIndex();

                    if (block.getPosition() < inBlock.getPosition()) {
                        tempIndex.put(outEdge, inIndex);
                    } else {
                        outIndex = tempIndex.get(outEdge);
                        inBlock.setNeighborInIndexEntry(inIndex, outIndex);
                        block.setNeighborOutIndexEntry(outIndex, inIndex);
                    }
                    inBlock.addNeighborIn(outVertex);
                }
            }
        }
    }

    /** Part of Global Crossing Reduction --> sift two BlockSets
     *
     * @param siftingBlockSet block set tested
     * @param nextBlockSet block set at the position to be tested
     * @return change in number of intersections
     */
    private int siftingSwap(
        BlockSet siftingBlockSet,
        BlockSet nextBlockSet
    ) {
        int differenceOfIntersections = 0;

        List<Block> reverseSiftingBlocks = new ArrayList<>(siftingBlockSet.getIncludingBlocks());
        Collections.reverse(reverseSiftingBlocks);
        /* Currently unused
        List<Block> reverseNextBlocks = new ArrayList<>(nextBlockSet.getIncludingBlocks());
        Collections.reverse(reverseNextBlocks);
         */
        for (Block siftingBlock : reverseSiftingBlocks) {
            for (Block nextBlock : nextBlockSet.getIncludingBlocks()) {

                Set<Pair<Integer, Direction>> pairSchedule = new HashSet<>();
                if (nextBlock.getLevels().containsKey(siftingBlock.getUpperVertex().getAssociatedLayer().getIndex())) {
                    pairSchedule.add(new Pair<>(siftingBlock.getUpperVertex().getAssociatedLayer().getIndex(), Direction.MINUS));
                }
                if (nextBlock.getLevels().containsKey(siftingBlock.getLowerVertex().getAssociatedLayer().getIndex())) {
                    pairSchedule.add(new Pair<>(siftingBlock.getLowerVertex().getAssociatedLayer().getIndex(), Direction.PLUS));
                }
                if (siftingBlock.getLevels().containsKey(nextBlock.getUpperVertex().getAssociatedLayer().getIndex())) {
                    pairSchedule.add(new Pair<>(nextBlock.getUpperVertex().getAssociatedLayer().getIndex(), Direction.MINUS));
                }
                if (siftingBlock.getLevels().containsKey(nextBlock.getLowerVertex().getAssociatedLayer().getIndex())) {
                    pairSchedule.add(new Pair<>(nextBlock.getLowerVertex().getAssociatedLayer().getIndex(), Direction.PLUS));
                }

                VertexSugiyama[] siftingBlockNeighbors;
                VertexSugiyama[] nextBlockNeighbors;
                int[] siftingBlockIndices;
                int[] nextBlockIndices;
                for (Pair<Integer, Direction> pair : pairSchedule) {
                    int level = pair.getKey();
                    Direction direction = pair.getValue();
                    VertexSugiyama siftingBlockVertex = siftingBlock.getLevels().get(level);
                    VertexSugiyama nextBlockVertex = nextBlock.getLevels().get(level);
                    if (direction.equals(Direction.PLUS)) {
                        nextBlockNeighbors = getFittingNeighborListOut(nextBlockVertex, nextBlock);
                        siftingBlockNeighbors = getFittingNeighborListOut(siftingBlockVertex, siftingBlock);
                        nextBlockIndices = nextBlock.getIndicesNeighbourOut();
                        siftingBlockIndices = siftingBlock.getIndicesNeighbourOut();
                        int value = uswap(siftingBlockNeighbors, nextBlockNeighbors);
                        //***********************TEST***********************
//                        System.out.println("\t+ " + siftingBlock.getBlockVerticesNames() + " <-> " + nextBlock.getBlockVerticesNames() + " + " + value);
                        //***********************TEST***********************
                        differenceOfIntersections += value;
                        if (siftingBlockVertex == siftingBlock.getLowerVertex()
                            && nextBlockVertex == nextBlock.getLowerVertex()) {
                            updateAdjacency(direction,
                                            siftingBlockNeighbors,
                                            nextBlockNeighbors,
                                            siftingBlockIndices,
                                            nextBlockIndices);
                        }
                    } else {
                        nextBlockNeighbors = getFittingNeighborListIn(nextBlockVertex, nextBlock);
                        siftingBlockNeighbors = getFittingNeighborListIn(siftingBlockVertex, siftingBlock);
                        nextBlockIndices = nextBlock.getIndicesNeighbourIn();
                        siftingBlockIndices = siftingBlock.getIndicesNeighbourIn();
                        int value = uswap(siftingBlockNeighbors, nextBlockNeighbors);
                        //***********************TEST***********************
//                        System.out.println("\t- " + siftingBlock.getBlockVerticesNames() + " <-> " + nextBlock.getBlockVerticesNames() + " + " + value);
                        //***********************TEST***********************
                        differenceOfIntersections += value;
                        if (siftingBlockVertex == siftingBlock.getUpperVertex()
                            && nextBlockVertex == nextBlock.getUpperVertex()) {
                            updateAdjacency(direction,
                                            siftingBlockNeighbors,
                                            nextBlockNeighbors,
                                            siftingBlockIndices,
                                            nextBlockIndices);
                        }
                    }
                }

                if (siftingBlock.getPosition() + 1 != nextBlock.getPosition() || nextBlock.getPosition() - 1 != siftingBlock.getPosition()) {
                    throw new RuntimeException("Indice Error");
                }

                siftingBlock.setPosition(siftingBlock.getPosition() + 1);
                nextBlock.setPosition(nextBlock.getPosition() - 1);
            }
        }

        /// undo swapping
        int siftingBlockSetPosition = blockSetListPosition.get(siftingBlockSet);
        int nextBlockSetPosition = blockSetListPosition.get(nextBlockSet);
        blockSetList.set(siftingBlockSetPosition, nextBlockSet);
        blockSetList.set(nextBlockSetPosition, siftingBlockSet);
        blockSetListPosition.put(siftingBlockSet, nextBlockSetPosition);
        blockSetListPosition.put(nextBlockSet, siftingBlockSetPosition);

        return differenceOfIntersections;
    }

    /** Part of Global Crossing Reduction --> update Adjacency after every sifting swap if necessary
     * Not adapted for BlockSet sifting
     *
     * @param direction 
     * @param siftingBlockVertexNeighbors 
     * @param nextBlockVertexNeighbors
     * @param siftingBlockVertexIndices 
     * @param nextBlockVertexIndices 
     */
    private void updateAdjacency(
        Direction direction,
        VertexSugiyama[] siftingBlockVertexNeighbors,
        VertexSugiyama[] nextBlockVertexNeighbors,
        int[] siftingBlockVertexIndices,
        int[] nextBlockVertexIndices
    ) {
        int positionSifting = 0;
        int positionNext = 0;
        int lengthSifting = siftingBlockVertexNeighbors.length;
        int lengthNext = nextBlockVertexNeighbors.length;
        while (positionSifting < lengthSifting
               && positionNext < lengthNext) {
            int siftingNeighborPosition = siftingBlockVertexNeighbors[positionSifting].getAssociatedBlock().getPosition();
            int nextNeighborPosition = nextBlockVertexNeighbors[positionNext].getAssociatedBlock().getPosition();
            if (siftingNeighborPosition < nextNeighborPosition) {
                positionSifting++;
            } else if (siftingNeighborPosition > nextNeighborPosition) {
                positionNext++;
            } else {
                VertexSugiyama siftingNeighbor = siftingBlockVertexNeighbors[positionSifting];
                int siftingIndex = siftingBlockVertexIndices[positionSifting];
                int nextIndex = nextBlockVertexIndices[positionNext];

                siftingNeighbor.getAssociatedBlock().swapEntries(direction, siftingIndex, nextIndex);

                ++(siftingBlockVertexIndices[positionSifting]);
                --(nextBlockVertexIndices[positionNext]);
                positionSifting++;
                positionNext++;
            }
        }
    }

    /** Part of Global Crossing Reduction
     *  Not adapted for BlockSet sifting
     *
     *  @param siftingBlockVertexNeighbors neighbors of sifting block
     *  @param nextBlockVertexNeighbors neighbors of next block
     */
    private int uswap(
        VertexSugiyama[] siftingBlockVertexNeighbors,
        VertexSugiyama[] nextBlockVertexNeighbors
    ) {
        int cuts = 0;
        int positionSifting = 0;
        int positionNext = 0;
        int lengthSifting = siftingBlockVertexNeighbors.length;
        int lengthNext = nextBlockVertexNeighbors.length;
        while (positionSifting < lengthSifting
               && positionNext < lengthNext) {
            int siftingNeighborPosition = siftingBlockVertexNeighbors[positionSifting].getAssociatedBlock().getPosition();
            int nextNeighborPosition = nextBlockVertexNeighbors[positionNext].getAssociatedBlock().getPosition();
            if (siftingNeighborPosition < nextNeighborPosition) {
                cuts += (lengthNext - positionNext);
                positionSifting++;
            } else if (siftingNeighborPosition > nextNeighborPosition) {
                cuts -= (lengthSifting - positionSifting);
                positionNext++;
            } else {
                cuts += (lengthNext - positionNext) - (lengthSifting - positionSifting);
                positionSifting++;
                positionNext++;
            }
        }
        return cuts;
    }

    /** Part of Global Crossing Reduction
     * @param blockVertex
     * @param block
     * @return 
     */
    private VertexSugiyama[] getFittingNeighborListOut(
        VertexSugiyama blockVertex,
        Block block
    ) {
        VertexSugiyama[] blockNeighborsOut;
        if (blockVertex == block.getLowerVertex()) {
            blockNeighborsOut = block.getNeighborsOut();
        } else {
            blockNeighborsOut = new VertexSugiyama[]{blockVertex.getOutEdges().get(0).getInNode()};
            //this is between two dummy vertices --> there should be never more then one edge
            if (blockVertex.getOutEdges().size() > 1) {
                System.out.println("Mist, wir haben ein dickes PROBLEM!!!!");
                System.out.println(blockVertex.getId() + ": " + blockVertex.getOutEdges().size());
            }
        }
        return blockNeighborsOut;
    }

    /** Part of Global Crossing Reduction
     *  @param blockVertex
     *  @param block
     *  @return 
     */
    private VertexSugiyama[] getFittingNeighborListIn(
        VertexSugiyama blockVertex,
        Block block
    ) {
        VertexSugiyama[] blockNeighborsIn;
        if (blockVertex == block.getUpperVertex()) {
            blockNeighborsIn = block.getNeighborsIn();
        } else {
            blockNeighborsIn = new VertexSugiyama[]{blockVertex.getInEdges().get(0).getOutNode()};
            //this is between two dummy vertices --> there should be never more then one edge
            if (blockVertex.getInEdges().size() > 1) {
                System.out.println("Mist, wir haben ein dickes PROBLEM!!!!");
                System.out.println(blockVertex.getId() + ": " + blockVertex.getOutEdges().size());
            }
        }
        return blockNeighborsIn;
    }
}
