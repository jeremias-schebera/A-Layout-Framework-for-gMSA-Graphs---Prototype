/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.application.Algorithmen;

import javafx.scene.text.Text;
import javafx.util.Pair;
import main.java.application.data.Chromosome;
import main.java.application.data.VertexSugiyama;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import main.java.application.data.AlignmentBlockAssociation;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import scala.Char;

/**
 *
 * @author zeckzer
 */
public class VertexListInit {

    /// Input
    private AlignmentBlockAssociation alignmentBlockAssociation;
    private List<Chromosome> sequenceVerticesDataForSugiyama;
    private Map<Vertex, VertexSugiyama> vertexDBSugiyamaVertexAssociation = new HashMap<>();

    /// Local
    private int joinedVertexCount = 1;
    private Boolean isJoinEnabled;
    //JOIN-Test
    private HashMap<VertexSugiyama, VertexSugiyama> existingJoinedVertices = new HashMap<>();
    //JOIN-Test

    /// Output
    private List<VertexSugiyama> vertexList;
    //also Output:
        //List of SugiyamaVertex sequences

    VertexListInit(
        Boolean isJoinEnabled,
        AlignmentBlockAssociation alignmentBlockAssociation,
        List<Chromosome> sequenceVerticesDataForSugiyama
    ) {
        this.isJoinEnabled = isJoinEnabled;
        this.alignmentBlockAssociation = alignmentBlockAssociation;
        this.sequenceVerticesDataForSugiyama = sequenceVerticesDataForSugiyama;
    }

    /** Create SugiyamaVertices and join trivial SugiyamaVertices together if possible
     * Part of preparation step
     */
    List<VertexSugiyama> initializeVerticesAndMarkJoinableVertices() {
        vertexList = new ArrayList<>();

        //Join trivial SugiyamaVertices-sequences
        if (isJoinEnabled) {
            //Creation and mark joinable
            boolean isGuideSequence = true;
            for (Chromosome currentChromosomePath : sequenceVerticesDataForSugiyama) {
                VertexSugiyama currentDrawingVertex;

                //JOIN-Test
                int index = 0;
                //JOIN-Test

                for (Vertex currentDBVertex : currentChromosomePath.getGraphDBVerticesPath()) {
                    if (!vertexDBSugiyamaVertexAssociation.containsKey(currentDBVertex)) {
                        int length = alignmentBlockAssociation.getLength(currentDBVertex);
                        currentDrawingVertex = new VertexSugiyama(currentDBVertex, isGuideSequence, length);
                        vertexDBSugiyamaVertexAssociation.put(currentDBVertex, currentDrawingVertex);
                        vertexList.add(currentDrawingVertex);

                        //JOIN-Test
                        if (index > 0) {
                            currentDrawingVertex.setIdPredecessorForJoining(currentChromosomePath.getGraphDBVerticesPath().get(index - 1).value("id"));
                        } else {
                            currentDrawingVertex.setIdPredecessorForJoining(-1);
                        }
                        if (index < currentChromosomePath.getGraphDBVerticesPath().size() - 1) {
                            currentDrawingVertex.setIdSucessorForJoining(currentChromosomePath.getGraphDBVerticesPath().get(index + 1).value("id"));
                        } else {
                            currentDrawingVertex.setIdSucessorForJoining(-1);
                        }
                        //JOIN-Test
                    } else {
                        currentDrawingVertex = vertexDBSugiyamaVertexAssociation.get(currentDBVertex);
                        //JOIN-Test
//                        if (index > 0 && index < currentChromosomePath.getGraphDBVerticesPath().size() - 1) {
//                            if ((int) currentChromosomePath.getGraphDBVerticesPath().get(index - 1).value("id") == currentDrawingVertex.getIdSucessorForJoining() && (int) currentChromosomePath.getGraphDBVerticesPath().get(index + 1).value("id") == currentDrawingVertex.getIdPredecessorForJoining()) {
//                                index++;
//                                continue;
//                            }
//                        }

                        int idToCompare = -1;
                        if (index > 0) {
                            idToCompare = currentChromosomePath.getGraphDBVerticesPath().get(index - 1).value("id");
                        }
                        if (currentDrawingVertex.getIdPredecessorForJoining() != idToCompare) {
                            currentDrawingVertex.setJoinable(false);
                        }

                        idToCompare = -1;
                        if (index < currentChromosomePath.getGraphDBVerticesPath().size() - 1) {
                            idToCompare = currentChromosomePath.getGraphDBVerticesPath().get(index + 1).value("id");
                        }
                        if (currentDrawingVertex.getIdSucessorForJoining() != idToCompare) {
                            currentDrawingVertex.setJoinable(false);
                        }
                        //JOIN-Test
                        //currentDrawingVertex.setJoinable(false);
                    }

                    //JOIN-Test
                    index++;
                    //JOIN-Test
                }
                isGuideSequence = false;
            }

            //Joining them together, if more then 1 in a row and update ChromosomePath
            for (Chromosome currentChromosomePath : sequenceVerticesDataForSugiyama) {
                VertexSugiyama currentDrawingVertex;
                List<VertexSugiyama> verticesToJoin = new ArrayList<>();
                List<VertexSugiyama> newPath = new LinkedList<>();
                for (Vertex currentDBVertex : currentChromosomePath.getGraphDBVerticesPath()) {
                    currentDrawingVertex = vertexDBSugiyamaVertexAssociation.get(currentDBVertex);
                    if (currentDrawingVertex.isJoinable()) {
                        verticesToJoin.add(currentDrawingVertex);
                    } else {
                        addJoinedVertex(verticesToJoin, newPath);
                        newPath.add(currentDrawingVertex);
                        verticesToJoin.clear();
                    }
                }

                // last Vertices in verticesToJoin
                addJoinedVertex(verticesToJoin, newPath);
                verticesToJoin.clear();
                currentChromosomePath.setSugiyamaVertices(newPath);
            }
        } else {
            //no joining
            boolean isGuideSequence = true;
            for (Chromosome currentChromosomePath : sequenceVerticesDataForSugiyama) {
                VertexSugiyama currentDrawingVertex;
                List<VertexSugiyama> newPath = new LinkedList<>();
                for (Vertex currentDBVertex : currentChromosomePath.getGraphDBVerticesPath()) {
                    //SugiyamaVertex isn't created yet
                    if (!vertexDBSugiyamaVertexAssociation.containsKey(currentDBVertex)) {
                        int length = alignmentBlockAssociation.getLength(currentDBVertex);
                        currentDrawingVertex = new VertexSugiyama(currentDBVertex, isGuideSequence, length);
                        vertexDBSugiyamaVertexAssociation.put(currentDBVertex, currentDrawingVertex);
                        vertexList.add(currentDrawingVertex);
                    } else {
                        //SugiyamaVertex is already created
                        currentDrawingVertex = vertexDBSugiyamaVertexAssociation.get(currentDBVertex);
                    }
                    newPath.add(currentDrawingVertex);
                }
                isGuideSequence = false;
                currentChromosomePath.setSugiyamaVertices(newPath);
            }
        }

        System.out.println("Draw Vertex-Number: " + vertexList.size());

        //Set Vertex-Text
        int vertexNumber = 1;
        for (Chromosome c : sequenceVerticesDataForSugiyama) {
            for (VertexSugiyama v : c.getSugiyamaVertices()) {
                if (v.getText() == null) {
                    v.setText("v" + convertIntToSubscriptUnicode(vertexNumber));
                    vertexNumber++;
                }
            }
        }

        return vertexList;
    }

    private void addJoinedVertex(
        List<VertexSugiyama> verticesToJoin,
        List<VertexSugiyama> newPath
    ) {
        if (verticesToJoin.size() > 1) {
            //JOIN-Test
            VertexSugiyama newJoinedVertex;
            if (!existingJoinedVertices.containsKey(verticesToJoin.get(0))) {
                newJoinedVertex = new VertexSugiyama(joinedVertexCount, verticesToJoin, vertexList, verticesToJoin.get(0).getId());
                joinedVertexCount++;
                vertexList.add(newJoinedVertex);
                existingJoinedVertices.put(verticesToJoin.get(0), newJoinedVertex);
            } else {
                newJoinedVertex = existingJoinedVertices.get(verticesToJoin.get(0));
            }
            newPath.add(newJoinedVertex);
            //JOIN-Test

            //VertexSugiyama newJoinedVertex = new VertexSugiyama(joinedVertexCount, verticesToJoin, vertexList, verticesToJoin.get(0).getId());
            //joinedVertexCount++;
            //vertexList.add(newJoinedVertex);
            //newPath.add(newJoinedVertex);
        } else if (verticesToJoin.size() == 1) {
            newPath.add(verticesToJoin.get(0));
        }
    }

    private String convertIntToSubscriptUnicode(int number) {
        String subscriptUnicode = "";

//        System.out.println(number);
//        System.out.println(String.valueOf(number).toCharArray());
        for (char digit : String.valueOf(number).toCharArray()) {
//            System.out.println(digit);
            switch(digit){
                case '0':
                    subscriptUnicode = subscriptUnicode + ("\u2080");
                    break;
                case '1':
                    subscriptUnicode = subscriptUnicode + ("\u2081");
                    break;
                case '2':
                    subscriptUnicode = subscriptUnicode + ("\u2082");
                    break;
                case '3':
                    subscriptUnicode = subscriptUnicode + ("\u2083");
                    break;
                case '4':
                    subscriptUnicode = subscriptUnicode + ("\u2084");
                    break;
                case '5':
                    subscriptUnicode = subscriptUnicode + ("\u2085");
                    break;
                case '6':
                    subscriptUnicode = subscriptUnicode + ("\u2086");
                    break;
                case '7':
                    subscriptUnicode = subscriptUnicode + ("\u2087");
                    break;
                case '8':
                    subscriptUnicode = subscriptUnicode + ("\u2088");
                    break;
                case '9':
                    subscriptUnicode = subscriptUnicode + ("\u2089");
                    break;
            }
        }
        return subscriptUnicode;
    }

}
