package main.java.application.data;

import javafx.util.Pair;
import main.java.application.data.EdgeSugiyama;
import main.java.application.data.VertexSugiyama;

import java.util.HashSet;
import java.util.List;
import main.java.application.data.ChromosomePaths;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class DummyPathEdgeSugiyama {

    private VertexSugiyama startVertex;
    private VertexSugiyama endVertex;
    private VertexSugiyama firstDummyVertex;
    private VertexSugiyama lastDummyVertex;
    private List<EdgeSugiyama> replacingDummyEdges;

    //Test!!!
    private int edgeCountDif = 0;
    //Test!!!

    // for creating new edges
    private ChromosomePaths associatedChromosomes;
    private double drawingThicknessFactor;

    public DummyPathEdgeSugiyama(
        EdgeSugiyama originalEdge,
        List<EdgeSugiyama> replacingDummyEdges
    ) {
        this.replacingDummyEdges = replacingDummyEdges;

        associatedChromosomes = originalEdge.getAssociatedChromosomes();
        drawingThicknessFactor = originalEdge.getDrawingThicknesFactor();
        this.firstDummyVertex = replacingDummyEdges.get(0).getInNode();
        this.lastDummyVertex = replacingDummyEdges.get(replacingDummyEdges.size() - 1).getOutNode();
        startVertex = replacingDummyEdges.get(0).getOutNode();
        endVertex = replacingDummyEdges.get(replacingDummyEdges.size() - 1).getInNode();
    }

    public Pair<Integer, Integer> deleteDummyEdgesAndCreateDrawingEdges(
        List<EdgeSugiyama> edgeList,
        int vertexNumber
    ) {
        HashSet<VertexSugiyama> verticesToCheck = new HashSet<>();
        int startVertexBlockPosition = startVertex.getAssociatedBlockSet().getDrawingPosition();
        int endVertexBlockPosition = endVertex.getAssociatedBlockSet().getDrawingPosition();
        int firstDummyVertexBlockPosition = firstDummyVertex.getAssociatedBlockSet().getDrawingPosition();
        int lastDummyVertexBlockPosition = lastDummyVertex.getAssociatedBlockSet().getDrawingPosition();

        if (!firstDummyVertex.equals(lastDummyVertex) || (startVertexBlockPosition == firstDummyVertexBlockPosition && lastDummyVertexBlockPosition == endVertexBlockPosition)) {
            for (EdgeSugiyama dummyEdge : replacingDummyEdges) {
                VertexSugiyama outVertex = dummyEdge.getOutNode();
                VertexSugiyama inVertex = dummyEdge.getInNode();
                outVertex.removeOutEdge(dummyEdge);
                inVertex.removeInEdge(dummyEdge);
                edgeList.remove(dummyEdge);

                verticesToCheck.add(outVertex);
                verticesToCheck.add(inVertex);
                
                //Test!!!
                edgeCountDif--;
                //Test!!!
            }
        }

        addNewEdgesForDrawing(edgeList);

        for (VertexSugiyama checkVertex : verticesToCheck) {
            if (checkVertex.getOutEdges().isEmpty() && checkVertex.getInEdges().isEmpty()) {
                checkVertex.getAssociatedLayer().getVertexOrderList().remove(checkVertex);

                //Test!!!
                vertexNumber--;
                //Test!!!
            }
        }

        //Test!!!
        Pair<Integer, Integer> values = new Pair<>(edgeCountDif, vertexNumber);
        //Test!!!

        return values;
    }

    private void addNewEdgesForDrawing(
        List<EdgeSugiyama> edgeList
    ) {
        int startVertexBlockPosition = startVertex.getAssociatedBlockSet().getDrawingPosition();
        int endVertexBlockPosition = endVertex.getAssociatedBlockSet().getDrawingPosition();
        int firstDummyVertexBlockPosition = firstDummyVertex.getAssociatedBlockSet().getDrawingPosition();
        int lastDummyVertexBlockPosition = lastDummyVertex.getAssociatedBlockSet().getDrawingPosition();

        if (startVertexBlockPosition == firstDummyVertexBlockPosition && lastDummyVertexBlockPosition == endVertexBlockPosition) {
            edgeList.add(new EdgeSugiyama(startVertex, endVertex, associatedChromosomes, drawingThicknessFactor));

            //Test!!!
            edgeCountDif += 1;
            //Test!!!
            return;
        }

        if (!firstDummyVertex.equals(lastDummyVertex)) {
            if (startVertexBlockPosition != firstDummyVertexBlockPosition
                    && lastDummyVertexBlockPosition != endVertexBlockPosition) {
                edgeList.add(new EdgeSugiyama(startVertex, firstDummyVertex, associatedChromosomes, drawingThicknessFactor));
                edgeList.add(new EdgeSugiyama(firstDummyVertex, lastDummyVertex, associatedChromosomes, drawingThicknessFactor));
                edgeList.add(new EdgeSugiyama(lastDummyVertex, endVertex, associatedChromosomes, drawingThicknessFactor));

                //Test!!!
                edgeCountDif += 3;
                //Test!!!
            } else if (startVertexBlockPosition == firstDummyVertexBlockPosition
                    && lastDummyVertexBlockPosition != endVertexBlockPosition) {
                edgeList.add(new EdgeSugiyama(startVertex, lastDummyVertex, associatedChromosomes, drawingThicknessFactor));
                edgeList.add(new EdgeSugiyama(lastDummyVertex, endVertex, associatedChromosomes, drawingThicknessFactor));

                //Test!!!
                edgeCountDif += 2;
                //Test!!!
            } else if (startVertexBlockPosition != firstDummyVertexBlockPosition
                    && lastDummyVertexBlockPosition == endVertexBlockPosition) {
                edgeList.add(new EdgeSugiyama(startVertex, firstDummyVertex, associatedChromosomes, drawingThicknessFactor));
                edgeList.add(new EdgeSugiyama(firstDummyVertex, endVertex, associatedChromosomes, drawingThicknessFactor));

                //Test!!!
                edgeCountDif += 2;
                //Test!!!
            }
        }
    }
}
