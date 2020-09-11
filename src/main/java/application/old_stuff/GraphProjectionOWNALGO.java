package  main.java.application.old_stuff;

import  main.java.application.old_stuff.EdgeOWNALGO;
import  main.java.application.old_stuff.NodeOWNALGO;
import  main.java.application.old_stuff.Path;
import  main.java.application.old_stuff.UndrawnPath;
import javafx.scene.paint.Color;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;

import java.util.*;

public class GraphProjectionOWNALGO {
    private List<Path> pathList = new ArrayList<>();
    private List<NodeOWNALGO> vertexList = new ArrayList<>();
    private HashMap<Integer, NodeOWNALGO> idVertexAssociation = new HashMap();
    private List<EdgeOWNALGO> edgeOWNALGOList = new ArrayList<>();
    private List<UndrawnPath> undrawnParts = new ArrayList<>();
    private HashMap<Integer, NodeOWNALGO> drawPositionVertexAssociation = new HashMap<>();
    private List<Color> colorĹist;

    public GraphProjectionOWNALGO() {
        colorĹist = new ArrayList<>();
        addColors();
    }

    private void addColors() {
        colorĹist.add(Color.rgb(77,175,74));
        colorĹist.add(Color.rgb(152,78,163));
        colorĹist.add(Color.rgb(217,95,2));
        colorĹist.add(Color.rgb(0,0,0));
        colorĹist.add(Color.rgb(0, 0, 128));
        colorĹist.add(Color.rgb(0, 128, 128));
        colorĹist.add(Color.rgb(202,178,214));
        colorĹist.add(Color.rgb(70, 240, 240));
        colorĹist.add(Color.rgb(240, 50, 230));
        colorĹist.add(Color.rgb(170, 110, 40));

        colorĹist.add(Color.rgb(48, 142, 31));
        colorĹist.add(Color.rgb(157, 0, 255));
        colorĹist.add(Color.rgb(255, 255, 0));
        colorĹist.add(Color.rgb(251,154,153));
        colorĹist.add(Color.rgb(105, 105, 105));
    }

    public List<NodeOWNALGO> getVertexList() {
        return vertexList;
    }

    public List<EdgeOWNALGO> getEdgeOWNALGOList() {
        return edgeOWNALGOList;
    }

    public List<Path> getPathList() {
        return pathList;
    }

    public List<UndrawnPath> getUndrawnParts() {
        return undrawnParts;
    }

    public void setUndrawnParts(List<UndrawnPath> undrawnParts) {
        this.undrawnParts = undrawnParts;
    }

    public void updateDrawPositions(Integer insertionPosition, NodeOWNALGO vertex) {
        int maxPosition = Collections.max(drawPositionVertexAssociation.keySet());
        if (insertionPosition > maxPosition) {
            drawPositionVertexAssociation.put(maxPosition + 1, vertex);
            vertex.setDrawPosition(maxPosition + 1);
            vertex.setPlaced(true);
        } else {
            for (Integer shiftPosition = maxPosition; shiftPosition >= insertionPosition; shiftPosition--) {
                drawPositionVertexAssociation.put(shiftPosition + 1, drawPositionVertexAssociation.get(shiftPosition));
                drawPositionVertexAssociation.get(shiftPosition + 1).setDrawPosition(shiftPosition + 1);
            }
            drawPositionVertexAssociation.put(insertionPosition, vertex);
            vertex.setDrawPosition(insertionPosition);
            vertex.setPlaced(true);
        }
    }

    private void sortUndrawnParts() {
        Collections.sort(undrawnParts, new UndrawnPathElmentCountComparator());
        Collections.sort(undrawnParts, new UndrawnPathPositionDifferenceAbsoluteComparator());
    }

    private class UndrawnPathPositionDifferenceAbsoluteComparator implements Comparator<UndrawnPath> {
        public int compare(UndrawnPath path1, UndrawnPath path2) {
            return Double.compare(Math.abs(path1.getPositionDifference()), Math.abs(path2.getPositionDifference()));
        }
    }

    private class UndrawnPathElmentCountComparator implements Comparator<UndrawnPath> {
        public int compare(UndrawnPath path1, UndrawnPath path2) {
            return Double.compare(path1.getPathVertices().size(), path2.getPathVertices().size());
        }
    }

    public HashMap<Integer, NodeOWNALGO> getDrawPositionVertexAssociation() {
        return drawPositionVertexAssociation;
    }

    public void addItemToUndrawnParts(UndrawnPath undrawnPath) {
        undrawnParts.add(undrawnPath);
    }

    public void addItemListToUndrawnParts(List<UndrawnPath> undrawnPathList) {
        for (UndrawnPath undrawnPath : undrawnPathList) {
            undrawnParts.add(undrawnPath);
        }
    }

    private int determineMiddelPosition(int startPosition, int endPosition) {
        int middelPosition;
        double shift = endPosition - startPosition;
        //Special case if the shift is only 1
        if (shift == 1) {
            return endPosition;
        }
        shift = shift / 2;
        middelPosition = (int) (startPosition + Math.ceil(shift));
        return middelPosition;
    }

    public void edgeRedirection(UndrawnPath undrawnPath) {
        double currentLevel = 0;
        NodeOWNALGO startVertex = undrawnPath.getPathVertices().get(0);
        NodeOWNALGO endVertex = undrawnPath.getPathVertices().get(1);
        NodeOWNALGO pseudoVertexStart = new NodeOWNALGO(true);
        NodeOWNALGO pseudoVertexEnd = new NodeOWNALGO(true);

        //EdgeOWNALGO from right to left
        if (undrawnPath.getPositionDifference() < 0) {
            for (int position = (int) endVertex.getDrawPosition(); position <= startVertex.getDrawPosition(); position++) {
                if (currentLevel > drawPositionVertexAssociation.get(position).getDrawLevel()) {
                    currentLevel = drawPositionVertexAssociation.get(position).getDrawLevel();
                }
            }
            currentLevel--;
            updateDrawPositions((int) startVertex.getDrawPosition(), pseudoVertexStart);
            updateDrawPositions((int) (endVertex.getDrawPosition() + 1), pseudoVertexEnd);
        }
        //EdgeOWNALGO from left to right
        else {
            for (int position = (int) startVertex.getDrawPosition(); position <= endVertex.getDrawPosition(); position++) {
                if (currentLevel < drawPositionVertexAssociation.get(position).getDrawLevel()) {
                    currentLevel = drawPositionVertexAssociation.get(position).getDrawLevel();
                }
            }
            currentLevel++;
            updateDrawPositions((int) startVertex.getDrawPosition() + 1, pseudoVertexStart);
            updateDrawPositions((int) endVertex.getDrawPosition(), pseudoVertexEnd);
        }

        EdgeOWNALGO currentEdgeOWNALGO = startVertex.getOutEdgeByEndVertexAndByPath(endVertex, undrawnPath.getAssociatedPath());
        currentEdgeOWNALGO.setAllowedToDraw(false);
        EdgeOWNALGO startPseudoEdgeOWNALGO = new EdgeOWNALGO(startVertex, pseudoVertexStart, currentEdgeOWNALGO.getPathIdentifier());
        EdgeOWNALGO pseudoPseudoEdgeOWNALGO = new EdgeOWNALGO(pseudoVertexStart, pseudoVertexEnd, currentEdgeOWNALGO.getPathIdentifier());
        EdgeOWNALGO pseudoEndEdgeOWNALGO = new EdgeOWNALGO(pseudoVertexEnd, endVertex, currentEdgeOWNALGO.getPathIdentifier());
        if (currentEdgeOWNALGO.isConsensus) {
            startPseudoEdgeOWNALGO.isConsensus = true;
            pseudoPseudoEdgeOWNALGO.isConsensus = true;
            pseudoEndEdgeOWNALGO.isConsensus = true;
        }
        edgeOWNALGOList.add(startPseudoEdgeOWNALGO);
        edgeOWNALGOList.add(pseudoPseudoEdgeOWNALGO);
        edgeOWNALGOList.add(pseudoEndEdgeOWNALGO);

        pseudoVertexStart.setDrawLevel(currentLevel);
        pseudoVertexEnd.setDrawLevel(currentLevel);
        pseudoVertexStart.setPlaced(true);
        pseudoVertexEnd.setPlaced(true);
        vertexList.add(pseudoVertexStart);
        vertexList.add(pseudoVertexEnd);
        pseudoVertexStart.setAssociatedPath(currentEdgeOWNALGO.getPathIdentifier());
        pseudoVertexEnd.setAssociatedPath(currentEdgeOWNALGO.getPathIdentifier());

    }

    private void placingUndrawnPathVertices(UndrawnPath undrawnPath) {
        double currentLevel = 0;
        NodeOWNALGO startVertex = undrawnPath.getPathVertices().get(0);
        NodeOWNALGO endVertex = undrawnPath.getPathVertices().get(undrawnPath.getPathVertices().size() - 1);
        NodeOWNALGO pseudoVertexStart = new NodeOWNALGO(true);
        NodeOWNALGO pseudoVertexEnd = new NodeOWNALGO(true);
        int positionVertex;

        //EdgeOWNALGO from right to left
        if (undrawnPath.getPositionDifference() < 0) {
//            positionVertex = (int) endVertex.getDrawPosition();
            for (int position = (int) endVertex.getDrawPosition(); position <= startVertex.getDrawPosition(); position++) {
                if (currentLevel > drawPositionVertexAssociation.get(position).getDrawLevel()) {
                    currentLevel = drawPositionVertexAssociation.get(position).getDrawLevel();
                }
            }
            currentLevel--;
            updateDrawPositions((int) startVertex.getDrawPosition(), pseudoVertexStart);
            updateDrawPositions((int) (endVertex.getDrawPosition() + 1), pseudoVertexEnd);
        }
        //EdgeOWNALGO from left to right
        else {
//            positionVertex = (int) startVertex.getDrawPosition();
            for (int position = (int) startVertex.getDrawPosition(); position <= endVertex.getDrawPosition(); position++) {
                if (currentLevel < drawPositionVertexAssociation.get(position).getDrawLevel()) {
                    currentLevel = drawPositionVertexAssociation.get(position).getDrawLevel();
                }
            }
            currentLevel++;
            updateDrawPositions((int) startVertex.getDrawPosition() + 1, pseudoVertexStart);
            updateDrawPositions((int) endVertex.getDrawPosition(), pseudoVertexEnd);
        }

//        positionVertex++;

        EdgeOWNALGO currentStartEdgeOWNALGO = startVertex.getOutEdgeByEndVertexAndByPath(undrawnPath.getPathVertices().get(1), undrawnPath.getAssociatedPath());
        EdgeOWNALGO startPseudoEdgeOWNALGO = new EdgeOWNALGO(startVertex, pseudoVertexStart, currentStartEdgeOWNALGO.getPathIdentifier());
        EdgeOWNALGO pseudoNextEdgeOWNALGO = new EdgeOWNALGO(pseudoVertexStart, undrawnPath.getPathVertices().get(1), currentStartEdgeOWNALGO.getPathIdentifier());
        edgeOWNALGOList.add(startPseudoEdgeOWNALGO);
        edgeOWNALGOList.add(pseudoNextEdgeOWNALGO);

        EdgeOWNALGO currentEndEdgeOWNALGO = undrawnPath.getPathVertices().get(undrawnPath.getPathVertices().size() - 2).getOutEdgeByEndVertexAndByPath(endVertex, undrawnPath.getAssociatedPath());
        EdgeOWNALGO previousPseudoEdgeOWNALGO = new EdgeOWNALGO(undrawnPath.getPathVertices().get(undrawnPath.getPathVertices().size() - 2), pseudoVertexEnd, currentEndEdgeOWNALGO.getPathIdentifier());
        EdgeOWNALGO pseudoEndEdgeOWNALGO = new EdgeOWNALGO(pseudoVertexEnd, endVertex, currentEndEdgeOWNALGO.getPathIdentifier());
        edgeOWNALGOList.add(previousPseudoEdgeOWNALGO);
        edgeOWNALGOList.add(pseudoEndEdgeOWNALGO);

        pseudoVertexStart.setDrawLevel(currentLevel);
        pseudoVertexEnd.setDrawLevel(currentLevel);
        pseudoVertexStart.setPlaced(true);
        pseudoVertexEnd.setPlaced(true);
        vertexList.add(pseudoVertexStart);
        vertexList.add(pseudoVertexEnd);
        pseudoVertexStart.setAssociatedPath(currentStartEdgeOWNALGO.getPathIdentifier());
        pseudoVertexEnd.setAssociatedPath(currentStartEdgeOWNALGO.getPathIdentifier());

        positionVertex = determineMiddelPosition(startVertex.getDrawPosition(), endVertex.getDrawPosition());

        for (int positionList = 0; positionList < undrawnPath.getPathVertices().size(); positionList++) {
            NodeOWNALGO vertex = undrawnPath.getPathVertices().get(positionList);
            if (!vertex.isPlaced()) {
                updateDrawPositions(positionVertex, vertex);
                vertex.setDrawLevel(currentLevel);
                vertex.setPlaced(true);
                positionVertex++;
            }
            //all vertices except the last one
//            if (positionList < undrawnPath.getPathVertices().size() - 1) {
//                NodeOWNALGO followingVertex = undrawnPath.getPathVertices().get(positionList + 1);
//                EdgeOWNALGO edge = vertex.getOutEdgeByEndvertex(followingVertex);
//                edge.setAllowedToDraw(true);
//            }
        }

        currentStartEdgeOWNALGO.setAllowedToDraw(false);
        currentEndEdgeOWNALGO.setAllowedToDraw(false);

    }

    public void processUndrawnParts() {
        sortUndrawnParts();
        System.out.println("===");
        for (UndrawnPath undrawnPath : undrawnParts) {
            undrawnPath.printPathVerices();
            if (undrawnPath.getPathVertices().size() == 2) {
                //place new Vertices between the already existing
                if (Math.abs(undrawnPath.getPositionDifference()) > 1) {
                    edgeRedirection(undrawnPath);
                } else {
//                    NodeOWNALGO startVertex = undrawnPath.getPathVertices().get(0);
//                    NodeOWNALGO endVertex = undrawnPath.getPathVertices().get(1);
//                    EdgeOWNALGO currentEdge = startVertex.getOutEdgeByEndvertex(endVertex);
//                    currentEdge.setAllowedToDraw(true);
                }
            } else {
                List<UndrawnPath> seperatedUndrawnPathList = undrawnPath.identifyUndrawnPaths(undrawnPath.getAssociatedPath());
                for (UndrawnPath  seperatedUndrawnPath : seperatedUndrawnPathList) {
                    if (seperatedUndrawnPath.getPathVertices().size() == 2) {
                        //place new Vertices between the already existing
                        if (Math.abs(seperatedUndrawnPath.getPositionDifference()) > 1) {
                            edgeRedirection(seperatedUndrawnPath);
                        } else {
//                            NodeOWNALGO startVertex = seperatedUndrawnPath.getPathVertices().get(0);
//                            NodeOWNALGO endVertex = seperatedUndrawnPath.getPathVertices().get(1);
//                            EdgeOWNALGO currentEdge = startVertex.getOutEdgeByEndvertex(endVertex);
//                            currentEdge.setAllowedToDraw(true);
                        }
                    } else {
                        placingUndrawnPathVertices(seperatedUndrawnPath);
                    }
                }
            }
        }

    }

    private boolean isNewVertex(Integer vertexID) {
        if (idVertexAssociation.containsKey(vertexID)) {
            return true;
        } else {
            return false;
        }
    }

    public void createVerticesAndEdges(List<Integer> path, String species, String chromosome, boolean isConsensusPath, int pathIndex) {
        NodeOWNALGO currentVertex;
        NodeOWNALGO nextVertex = null;
        List<NodeOWNALGO> tempVertexList = new ArrayList<>();
        pathList.add(new Path(isConsensusPath, this, colorĹist.get(pathList.size())));

        for (int vertexIndex = 0; vertexIndex < path.size() - 1; vertexIndex++) {
            Integer currentID = path.get(vertexIndex);
            Integer nextID = path.get(vertexIndex + 1);
            if (vertexIndex == 0) {
                if (!isNewVertex(currentID)) {
                    currentVertex = new NodeOWNALGO(currentID);
                    idVertexAssociation.put(currentID, currentVertex);
                    vertexList.add(currentVertex);
                } else {
                    currentVertex = idVertexAssociation.get(currentID);
                }
                tempVertexList.add(currentVertex);
            } else {
                currentVertex = nextVertex;
            }

            if (!isNewVertex(nextID)) {
                nextVertex = new NodeOWNALGO(nextID);
                idVertexAssociation.put(nextID, nextVertex);
                vertexList.add(nextVertex);
            } else {
                nextVertex = idVertexAssociation.get(nextID);
            }

            tempVertexList.add(nextVertex);

            EdgeOWNALGO edgeOWNALGO = new EdgeOWNALGO(currentVertex, nextVertex, pathList.get(pathList.size() - 1));
            edgeOWNALGOList.add(edgeOWNALGO);
        }

        pathList.get(pathList.size() - 1).setPathVertices(tempVertexList);
        if (isConsensusPath == true) {
            for (EdgeOWNALGO e : edgeOWNALGOList) {
                e.isConsensus = true;
            }
            if (pathIndex == 0) {
                placeConsensusPath(pathList.get(pathList.size() - 1));
            }
        }

    }

    private void placeConsensusPath(Path consensuPath) {
        int position;
        for (position = 0; position < consensuPath.getPathVertices().size(); position++) {
            NodeOWNALGO vertex = consensuPath.getPathVertices().get(position);
            //all vertices except the last one
//            if (position < consensuPath.getPathVertices().size() - 1) {
//                NodeOWNALGO followingVertex = consensuPath.getPathVertices().get(position + 1);
//                EdgeOWNALGO edge = vertex.getOutEdgeByEndvertex(followingVertex);
//                edge.setAllowedToDraw(true);
//            }
            drawPositionVertexAssociation.put(position, vertex);
            vertex.setDrawPosition(position);
            vertex.setDrawLevel(0);
            vertex.setPlaced(true);
        }
    }

    public void generateSpeciesPaths(GraphTraversalSource g, String chromosome, int start_range, int end_range) {
//        GraphTraversal<Vertex, Map.Entry> speciesPathGraphTraversal =
        List<Object> speciesPath =
                g.V().hasLabel("Chromosome").has("name", chromosome).as("chrom").in("isOn")
                        .has("throw", false).as("seqV")
                        // start and start+length-1 is in interval --> find sequences in interval
                        .project("start", "length")
                        .by(__.values("start"))
                        .by(__.values("length"))
                        .where(__.select("start").is(P.between(start_range, end_range + 1)).or().math("start + length - 1").is(P.between(start_range, end_range + 1)))
                        .select("seqV")
                        .order().by("start")
                        // find blocks
                        .in("hasSeq").in("containsBlock")
                        .dedup()// fliegt raus --> da keine Duplikate in validem Datensatz
//                        .aggregate("speciesPath")
//                        .inE("next")
//                        .aggregate("positionSpecies").by("pos")
//                        .order().by("pos")
//                        .inV()
//                        .aggregate("all")
//                        .cap("all")
//                        .unfold().tail(1).as("end")
//                        .select("all")
//                        .<Vertex>unfold().limit(1)
//                        .emit()
//                        .repeat(
//                                __.out("next")
//                        )
//                        .until(__.where(P.eq("end")))
//                        .aggregate("orderPath")
//                        .inE("next")
//                        .aggregate("positionOrder").by("pos")
//                        .cap("speciesPath", "orderPath", "positionSpecies", "positionOrder").unfold()
                        .values("id")
                        .toList();

    }

}