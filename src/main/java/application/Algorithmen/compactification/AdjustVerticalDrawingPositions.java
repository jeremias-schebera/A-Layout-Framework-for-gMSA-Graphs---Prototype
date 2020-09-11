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
package main.java.application.Algorithmen.compactification;

import main.java.application.data.Block;
import main.java.application.data.BlockSet;
import main.java.application.data.VertexSugiyama;

import java.util.ArrayList;
import java.util.List;

public class AdjustVerticalDrawingPositions {

    /// Input
    private int maxLayer;
    private CompactificationSide side;

    ///Local
    private int[] countVerticesLayer;
    //Test!!!
    private int maxSpecialShiftSteps = 0;
    public int getMaxSpecialShiftSteps() {
        return maxSpecialShiftSteps;
    }
    //Test!!!

    private List<VertexSugiyama[]> matrix = new ArrayList<>();
    private List<List<BlockSet>> blockSets = new ArrayList<>();
    private int depth = 0;

    AdjustVerticalDrawingPositions(
        int maxLayer,
        CompactificationSide side
    ) {
        this.maxLayer = maxLayer;
        this.side = side;

        initVertexCountOfLayers();
    }

    /** set vertexCount of the Layers to 0
     */
    private void initVertexCountOfLayers() {
        countVerticesLayer = new int[maxLayer + 1];
        for (int index = 0;
             index < countVerticesLayer.length;
             index++) {
            countVerticesLayer[index] = 0;
        }
    }

    /**
     *
     * @param blockSet
     */
    void shiftBlockSetsTowardsGuideSequence(
        BlockSet blockSet
    ) {
        /// Compute maximal (i.e., closest to the guide sequence) position
        int maxPosition = Integer.MIN_VALUE;
        for (Block block : blockSet.getIncludingBlocks()) {
            for (VertexSugiyama includedVertex : block.getIncludingVertices()) {
                int layerIndex = includedVertex.getAssociatedLayer().getIndex();
                if (countVerticesLayer[layerIndex] + 1 > maxPosition) {
                    maxPosition = countVerticesLayer[layerIndex] + 1;
                }

                if (matrix.size() < maxPosition) {
                    matrix.add(new VertexSugiyama[maxLayer + 1]);
                    blockSets.add(new ArrayList<>());
                }
            }
        }

        /// Save depth
        if (maxPosition > depth) {
            depth = maxPosition;
        }

        /// Set drawing position
        switch (side) {
            case LOWER:
                blockSet.setDrawingPosition(maxPosition);
                break;
            case UPPER:
                blockSet.setDrawingPosition(-maxPosition);
                break;
        }

        /// Fill matrix
        for (Block block : blockSet.getIncludingBlocks()) {
            for (VertexSugiyama includedVertex : block.getIncludingVertices()) {
                int layerIndex = includedVertex.getAssociatedLayer().getIndex();
                countVerticesLayer[layerIndex] = maxPosition;
                matrix.get(maxPosition - 1)[layerIndex] = includedVertex;
            }
        }

        /// Add block set to block-sets
        blockSets.get(maxPosition - 1).add(blockSet);
    }

    void specialCaseShifting() {
        /// Shifting of special cases
        for (int verticalPosition = depth - 2; verticalPosition >= 0; verticalPosition--) {
            for (BlockSet blockSet : blockSets.get(verticalPosition)) {
                /// compute number of free positions in the matrix
                int shiftableStepsInMatrix = 0;
                boolean shiftable = true;
                while (shiftable && verticalPosition + shiftableStepsInMatrix <= depth - 2) {
                    shiftable = isShiftable(blockSet, matrix.get(verticalPosition + shiftableStepsInMatrix + 1));
                    if (shiftable) {
                        shiftableStepsInMatrix++;
                    }
                }

                /// If at least one position is free in the matrix
                if (shiftableStepsInMatrix > 0) {
                    int shiftablePositions = 0;
                    int shiftableStepsDueConnection = Integer.MAX_VALUE;
                    for (BlockSet connectedBlockSet : blockSet.getConnectedBlockSets()) {
                        switch (side) {
                            case LOWER:
                                shiftablePositions = connectedBlockSet.getDrawingPosition() - blockSet.getDrawingPosition();
                                break;
                            case UPPER:
                                shiftablePositions = blockSet.getDrawingPosition() - connectedBlockSet.getDrawingPosition();
                                break;
                        }

                        if (shiftablePositions < shiftableStepsDueConnection) {
                            shiftableStepsDueConnection = shiftablePositions;
                        }
                        if (shiftablePositions <= 0) {
                            break;
                        }
                    }

                    if (shiftableStepsDueConnection > 0) {
                        int shiftSteps = Math.min(shiftableStepsInMatrix, shiftableStepsDueConnection);

                        if (shiftSteps > maxSpecialShiftSteps) {
                            maxSpecialShiftSteps = shiftSteps;
                        }

                        /// Set drawing position
                        switch (side) {
                            case LOWER:
                                System.out.println("shift down " + shiftSteps + " steps: " + blockSet.getBlockSetVertices());
                                blockSet.setDrawingPosition(blockSet.getDrawingPosition() + shiftSteps);
                                break;
                            case UPPER:
                                System.out.println("shift up " + shiftSteps + " steps: " + blockSet.getBlockSetVertices());
                                blockSet.setDrawingPosition(blockSet.getDrawingPosition() - shiftSteps);
                                break;
                        }

                        /// Update Matrix
                        for (Block block : blockSet.getIncludingBlocks()) {
                            for (VertexSugiyama vertexSugiyama : block.getIncludingVertices()) {
                                matrix.get(verticalPosition)[vertexSugiyama.getAssociatedLayer().getIndex()] = null;
                                matrix.get(verticalPosition + shiftSteps)[vertexSugiyama.getAssociatedLayer().getIndex()] = vertexSugiyama;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isShiftable(
        BlockSet blockSet,
        VertexSugiyama[] matrixVertices
    ) {
        for (Block block : blockSet.getIncludingBlocks()) {
            for (VertexSugiyama vertexSugiyama : block.getIncludingVertices()) {
                if (matrixVertices[vertexSugiyama.getAssociatedLayer().getIndex()] != null) {
                    return false;
                }
            }
        }
        return true;
    }

    int getDepth() {
        return depth;
    }
}
