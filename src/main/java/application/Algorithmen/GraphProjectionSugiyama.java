package main.java.application.Algorithmen;

import main.java.application.Algorithmen.compactification.Compactification;
import main.java.application.data.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphProjectionSugiyama {

    /// Graph
    private List<VertexSugiyama> vertexList;
    private List<EdgeSugiyama> edgeList;
    private BlockSet guideSequenceBlockSet;
    private List<BlockSet> blockSetList;

    /// Layer
    private LayerAssignment layerAssignment;
    private Map<Integer, Layer> indexLayerAssociation;
    private int maxLayer;
    private int longestAlignmentBlock;
    private int shortestAlignmentBlock;

    //Test!!!
//    StringBuffer csvText;
    //Test!!!

    /// Drawing
//    private Map<Integer, VertexSugiyama> drawPositionVertexAssociation = new HashMap<>();
//    private int minimumSeparation = 100;
//    private List<List<VertexSugiyama>> paths = new ArrayList<>();
//    private HashMap<Integer, VertexSugiyama> vertexOrderInGuideSequence = new HashMap<>();
//    private int indexSCC;
//    private Stack<VertexSugiyama> stackSCC = new Stack<>();
    private double drawingThicknessFactor;

    /// edge routing
//    private EdgeRoutingOLD edgeRoutingOLD;
    private EdgeRouting edgeRouting;

    public GraphProjectionSugiyama(
        Configuration configuration
    ) {
        this.longestAlignmentBlock = 0;
        this.shortestAlignmentBlock = Integer.MAX_VALUE;
        this.drawingThicknessFactor = configuration.getDrawingThicknessFactor();

    }

    public void computeLayout(
        Configuration configuration
    ) {

        //Test!!!
//        csvText = configuration.getCsvText();
        //Test!!!

        // Preparation Step + Cylce removement
        long timeStart = System.currentTimeMillis();
        createGraph(configuration);
        long timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Knoten- & Kantenerzeugung + Zyklenentfernung: " + (timeEnd - timeStart));

        // Layering and Dummy Vertex creation
        timeStart = System.currentTimeMillis();
        assignLayers();
        timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Schichtzuweisung: " + (timeEnd - timeStart));

        // Edge Crossing reduction
        timeStart = System.currentTimeMillis();
        reduceEdgeCrossings(configuration);
        timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Global-Sifting + Vorverarbeitung: " + (timeEnd - timeStart));
//        projection.yCoordinateAssignmentWithBrandesAndKoepf();

        //Assign BlockSets horizontal Position (Compactification)
        timeStart = System.currentTimeMillis();
        assignVerticalCoordinate();
        timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Kompaktifizierung: " + (timeEnd - timeStart));

        // Pre processing for Edge Routing
        timeStart = System.currentTimeMillis();
        preProcessingEdgeRouting();
        timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Nachverarbeitung: " + (timeEnd - timeStart));

        /// Edge routing
        timeStart = System.currentTimeMillis();
        edgeRouting = new EdgeRouting(configuration, indexLayerAssociation, maxLayer);
        edgeRouting.computeRouting();
        timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Edge routing: " + (timeEnd - timeStart));

        //Test!!!
        int edgeCount = 0;
        for (EdgeSugiyama e : edgeList) {
            edgeCount += e.getAssociatedChromosomes().getChromosomePaths(EdgeDirection.FORWARD).size();
            edgeCount += e.getAssociatedChromosomes().getChromosomePaths(EdgeDirection.BACKWARD).size();
        }
//        csvText.append(edgeCount + ";" + (timeEnd - timeStart) + ";");
        //Test!!!
    }

    private void createGraph(
        Configuration configuration
    ) {
        System.out.println("################### START Zyklenentfernung ###################");
        long timeStart = System.currentTimeMillis();

        int vertexCountOriginal = 0;
        List<Chromosome> sequenceVerticesDataForSugiyama = configuration.getSequenceVerticesData().getChromosomes();
        for (Chromosome chromosome : sequenceVerticesDataForSugiyama) {
            System.out.println(chromosome.toString());
            vertexCountOriginal += chromosome.getGraphDBVerticesPath().size();
        }
        System.out.println("Original Number of Vertices from DB (unjoined): " + vertexCountOriginal);

        VertexListInit vertexListInit = new VertexListInit(configuration.getIsJoinEnabled(),
                configuration.getAlignmentBlockAssociation(),
                sequenceVerticesDataForSugiyama);
        vertexList = vertexListInit.initializeVerticesAndMarkJoinableVertices();

        System.out.println("");
        for (Chromosome chromosome : sequenceVerticesDataForSugiyama) {
            System.out.println(chromosome.toString2());
        }

        EdgeAndBlockSetInit edgeAndBlockSetInit = new EdgeAndBlockSetInit(sequenceVerticesDataForSugiyama,
                drawingThicknessFactor);

        edgeList = edgeAndBlockSetInit.cycleDestroyerAndInitializeEdgesAndBlockSets();

        long timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Zyklenentfernung: " + (timeEnd - timeStart));
        System.out.println("################### ENDE Zyklenentfernung ###################");

        //Test!!!
//        csvText.append(sequenceVerticesDataForSugiyama.size() + ";");
//        csvText.append(vertexList.size() + ";");
//        csvText = edgeAndBlockSetInit.csvLine(csvText);
//        csvText.append((timeEnd - timeStart) + ";");
//        System.out.println(csvText.toString());
        //Test!!!


        guideSequenceBlockSet = edgeAndBlockSetInit.getGuideSequenceBlockSet();
    }

    //Test!!!
//    public StringBuffer getCSVLine() {
//        return csvText;
//    }
    //Test!!!

    private void assignLayers() {
        System.out.println("################### START Schichtung ###################");
        long timeStart = System.currentTimeMillis();

        layerAssignment = new LayerAssignment(vertexList, edgeList, drawingThicknessFactor);
        layerAssignment.assignLayerWithLongestPathAndImprovement();

        long timeEnd = System.currentTimeMillis();

        System.out.println("ZEIT - Schichtung: " + (timeEnd - timeStart));
        System.out.println("################### ENDE Schichtung ###################");

        //Test!!!
//        csvText = layerAssignment.csvLine(csvText);
//        csvText.append(vertexList.size() + ";" + edgeList.size() + ";" + (timeEnd - timeStart) + ";");
        //Test!!!

        indexLayerAssociation = layerAssignment.getIndexLayerAssociation();
        maxLayer = layerAssignment.getMaxLayer();
        longestAlignmentBlock = layerAssignment.getLongestAlignmentBlock();
        shortestAlignmentBlock = layerAssignment.getShortestAlignmentBlock();
    }

    private void reduceEdgeCrossings(Configuration configuration) {
        System.out.println("################### START Kreuzungsminimierung ###################");
        long timeStart = System.currentTimeMillis();

        GlobalSifting globalSifting = new GlobalSifting(indexLayerAssociation, maxLayer);
        //         projection.reorderVerticesWithBarycenterHeuristic();
        globalSifting.createInitialBlockSetList();
        //        projection.deleteEmptyBlocks();

        blockSetList = globalSifting.globalSifting(10);

        long timeEnd = System.currentTimeMillis();

        System.out.println("ZEIT - Kreuzungsminimierung: " + (timeEnd - timeStart));
        System.out.println("################### ENDE Kreuzungsminimierung ###################");

        //Test!!!
//        csvText.append(blockSetList.size() + ";" + (timeEnd - timeStart) + ";");
        //Test!!!
    }

    private void assignVerticalCoordinate() {
        System.out.println("################### START Vertikale Koordinate ###################");
        long timeStart = System.currentTimeMillis();

        Compactification compactification = new Compactification(maxLayer, guideSequenceBlockSet, blockSetList);
        compactification.calculateVerticalDrawingPositions();

        long timeEnd = System.currentTimeMillis();

        System.out.println("ZEIT - Vertikale Koordinate: " + (timeEnd - timeStart));
        System.out.println("################### ENDE Vertikale Koordinate ###################");

        //Test!!!
//        csvText = compactification.csvLine(csvText);
//        csvText.append((timeEnd - timeStart) + ";");
        //Test!!!
    }

    /** Post-Processing
    
     */
    private void preProcessingEdgeRouting() {
        //Preparation for drawing
        //        turnAroundEdgesToOriginal();
        System.out.println("################### START Nachverarbeitung ###################");

        long timeStart = System.currentTimeMillis();

        turnAroundLayering();

        //Test!!!
//        int vertexNumber = vertexList.size();
//        vertexNumber = layerAssignment.deleteDummyEdgesAndCreateDrawingEdges(vertexNumber);
        //Test!!!

        long timeEnd = System.currentTimeMillis();

        System.out.println("ZEIT - Nachverarbeitung: " + (timeEnd - timeStart));
        System.out.println("################### ENDE Nachverarbeitung ###################");

        //Test!!!
//        csvText = layerAssignment.csvLine2(csvText);
//        csvText.append(edgeList.size() + ";" + vertexNumber + ";" + (timeEnd - timeStart) + ";");
        //Test!!!
    }

    private void turnAroundLayering() {
        int newLayerIndex = maxLayer;
        Map<Integer, Layer> newIndexLayerAssociation = new HashMap<>();
        for (int layerIndex = 0; layerIndex <= maxLayer; layerIndex++) {
            Layer layer = indexLayerAssociation.get(layerIndex);
            layer.setIndex(newLayerIndex);
            newIndexLayerAssociation.put(newLayerIndex, layer);
            newLayerIndex--;
        }
        indexLayerAssociation = newIndexLayerAssociation;
    }

    /* currently unused
    public void updateDrawPositions(Integer insertionPosition, VertexSugiyama vertex) {
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
     */

 /* currently unused
    //Post-Processing --> Preparation for drawing
    public void calculateMaximaleNeededEdgeSpace(int spaceFactor) {
        for (VertexSugiyama vertex : vertexList) {
            if (!vertex.isDummyNode()) {
                double neededEdgeSpace = vertex.calculateMaxiumumNeededEdgeSpace(spaceFactor);
                if (neededEdgeSpace > maximaleHalfNeededEdgeSpaceOnVertex) {
                    maximaleHalfNeededEdgeSpaceOnVertex = neededEdgeSpace;
                }
            }
        }
    }
     */
    public Map<Integer, Layer> getIndexLayerAssociation() {
        return indexLayerAssociation;
    }

    public int getLongestAlignmentBlock() {
        return longestAlignmentBlock;
    }

    public int getShortestAlignmentBlock() {
        return shortestAlignmentBlock;
    }
}
