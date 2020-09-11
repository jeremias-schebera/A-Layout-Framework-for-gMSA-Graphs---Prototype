package main.java.application.data;

import java.util.*;

public class BlockSet {
//    private HashSet<VertexSugiyama> includingVertices;

    private List<Block> includingBlocks = new ArrayList<>();
    private int drawingPosition;
    private Set<BlockSet> connectedBlockSets = new HashSet<>();

    public BlockSet(
        List<VertexSugiyama> includingVertices,
        List<EdgeSugiyama> edgePosition
    ) {
//        this.includingVertices = new HashSet<>(includingVertices);

        for (VertexSugiyama vertex : includingVertices) {
            vertex.setAssociatedBlockSet(this);
        }

        for (EdgeSugiyama edge : edgePosition) {
            edge.setAssociatedBlockSet(this);
        }

    }

    public BlockSet(
            List<VertexSugiyama> includingVertices,
            List<EdgeSugiyama> edgePosition,
            VertexSugiyama startVertex,
            VertexSugiyama endVertex
    ) {
//        this.includingVertices = new HashSet<>(includingVertices);

        for (VertexSugiyama vertex : includingVertices) {
            vertex.setAssociatedBlockSet(this);
        }

        for (EdgeSugiyama edge : edgePosition) {
            edge.setAssociatedBlockSet(this);
        }

        connectedBlockSets.add(startVertex.getAssociatedBlockSet());
        startVertex.getAssociatedBlockSet().addConnectedBlockSet(this);
        connectedBlockSets.add(endVertex.getAssociatedBlockSet());
        endVertex.getAssociatedBlockSet().addConnectedBlockSet(this);

    }

    public void addConnectedBlockSet(BlockSet blockSet) {
        connectedBlockSets.add(blockSet);
    }

    public void addBlock(Block block) {
        includingBlocks.add(block);
        block.setAssociatedBlockSet(this);
    }

    public Set<BlockSet> getConnectedBlockSets() {
        return connectedBlockSets;
    }

    public void addVerticesAndBlocks(List<VertexSugiyama> vertexList, Block associatedBlock) {
        for (VertexSugiyama vertex : vertexList) {
//            includingVertices.add(vertex);
            vertex.setAssociatedBlockSet(this);
        }
        addBlock(associatedBlock);
    }

    public void printBlockSetVertices() {
        String text = "BlockSet: ";
        for (Block block : includingBlocks) {
            for (VertexSugiyama v : block.getIncludingVertices()) {
                text += v.getId() + ", ";
            }
        }
        System.out.println(text);
    }

    public String getBlockSetVertices() {
        String text = "BlockSet: ";
        for (Block block : includingBlocks) {
            for (VertexSugiyama v : block.getIncludingVertices()) {
                text += v.getId() + ", ";
            }
        }
        return text;
    }

    public List<Block> getIncludingBlocks() {
        return includingBlocks;
    }

    public int getDrawingPosition() {
        return drawingPosition;
    }

    public void setDrawingPosition(int drawingPosition) {
        this.drawingPosition = drawingPosition;
    }

    public void sortIncludingBlocks() {
        Collections.sort(includingBlocks, new BlockComparator());
    }
}

class BlockComparator implements Comparator<Block> {

    @Override
    public int compare(Block block1, Block block2) {
        int minLevelBlock1 = Collections.min(block1.getLevels().keySet());
        int minLevelBlock2 = Collections.min(block2.getLevels().keySet());

        return (minLevelBlock2 - minLevelBlock1);
    }
}
