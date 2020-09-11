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
package main.java.application.GUI;

import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import main.java.application.Algorithmen.GraphProjectionSugiyama;
import main.java.application.Controller.MainController;
import main.java.application.data.*;
//import org.apache.batik.transcoder.TranscoderException;
//import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.tinkerpop.gremlin.structure.Vertex;

//import org.jfxconverter.JFXConverter;
//import org.jfxconverter.drivers.svg.ConvertorSVGGraphics2D;
//import org.jfxconverter.drivers.svg.SVGDriverUtils;
//import org.mdiutil.swing.ExtensionFileFilter;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
//import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;

public class DrawingWindowController
    implements Initializable {

    @FXML
    private Pane drawPane;
    private Group drawGroup;

    private MainController parent;

    private List<Rectangle> lastSelectedRectangle = new ArrayList<>();

    private double maxSequenceLength;
    private double minSequenceLength;
    private double diffSequenceLength;

    private final double minDrawLengthVertex = 40;
    private final double maxDrawLengthVertex = 50;
    private final double minDrawHeightVertex = 26;
    private final double minVerticlaInterSpace = 15;
    private final double minTriangleHeight = 10;
    private final double triangleLength = 10;
    private double diffDrawLengthVertex;

    private HashMap<Chromosome, List<Path>> chromosomeLineAssociation;

    private ScrollPane rootPane;

    //Test!!!
//    ConvertorSVGGraphics2D g2D;
    File file;
    //Test!!!

    private double defaultScrollFactor;

    public void setPane(
        ScrollPane anchorPane
    ) {
        this.rootPane = anchorPane;
    }

    public Parent getRoot() {
        return rootPane;
    }

    public static DrawingWindowController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            // Load root layout from fxml file
            loader.setLocation(DrawingWindowController.class.getResource("DrawingWindow.fxml"));
            ScrollPane rootPane = (ScrollPane) loader.load();
            DrawingWindowController drawingWindowController = loader.<DrawingWindowController>getController();
            drawingWindowController.setPane(rootPane);
            return drawingWindowController;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return null;
        }
    }

    //SVG exporter -- disabled
    public void createDocument() {
        try {
            file = getFile();
            if (file != null) {

                Node node = drawGroup;
             //   SVGDriverUtils svgUtils = new SVGDriverUtils();
             //  svgUtils.convert(node, file);

//                Document doc = SVGDOMImplementation.getDOMImplementation()
//                        .createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg", null);
//                try (Writer writer = new BufferedWriter(new FileWriter(file))) {
//                    TranscoderOutput output = new TranscoderOutput(writer);
//                    Bounds bounds = node.getBoundsInLocal();
//                    Rectangle2D rec = new Rectangle2D.Double(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
//
//                    ConvertorSVGGraphics2D g2D = new ConvertorSVGGraphics2D(doc);
//                    JFXConverter converter = new JFXConverter();
//                    converter.convert(g2D, node);
//
//                    finishTranscoding(rec, output);
//                    writer.flush();
//                }
            }
//        } catch (DOMException | IOException | TranscoderException e) {
//            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected File getFile() {
        File dir = new File(System.getProperty("user.dir"));
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(dir);
        chooser.setTitle("Select PNG File to create");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("svg files (*.svg)", "*.svg"));
        File file = chooser.showSaveDialog(null);
        return file;
    }
    //SVG exporter -- disabled

    public void captureAndSaveDisplay(){
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));

        //Prompt user to select a file
        File file = fileChooser.showSaveDialog(null);

        if(file != null){
            try {
                //Pad the capture area

                int width = (int) drawPane.getWidth();
                int height = (int) drawPane.getHeight();
                if (width + 20 < 0) {
                    System.out.println("w - kleiner");
                    width = (int) Integer.MAX_VALUE - 20;
                }
                if (height + 20 < 0) {
                    System.out.println("h - kleiner");
                    height = (int) Integer.MAX_VALUE - 20;
                }

                WritableImage writableImage = new WritableImage(width + 20,
                        height + 20);
                drawPane.snapshot(null, writableImage);
                RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                //Write the snapshot to the chosen file
                ImageIO.write(renderedImage, "png", file);
            } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    public void initializeDrawGroup() {
        drawGroup = new Group();
        drawPane.getChildren().add(drawGroup);
    }

    //Feature disabled
    private void scrollHandling() {
        final double SCALE_DELTA = 1.1;
        defaultScrollFactor = 1.0;

        Node rootPaneContent = rootPane.getContent();

        rootPane.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable,
                                Bounds oldValue, Bounds newValue) {
                drawPane.setMinSize(newValue.getWidth(), newValue.getHeight());
            }
        });

        drawPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                event.consume();

                if (event.getDeltaY() == 0) {
                    return;
                }

                double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA
                        : 1 / SCALE_DELTA;

                defaultScrollFactor = (event.getDeltaY() > 0) ? defaultScrollFactor * (1 / SCALE_DELTA)
                        :  defaultScrollFactor * SCALE_DELTA;

//                System.out.println(defaultScrollFactor);

                // amount of scrolling in each direction in scrollContent coordinate
                // units
                Point2D scrollOffset = figureScrollOffset(rootPaneContent, rootPane);

                drawGroup.setScaleX(drawGroup.getScaleX() * scaleFactor);
                drawGroup.setScaleY(drawGroup.getScaleY() * scaleFactor);

                // move viewport so that old center remains in the center after the
                // scaling
                repositionScroller(rootPaneContent, rootPane, scaleFactor, scrollOffset);

            }
        });

        // Panning via drag....
        final ObjectProperty<Point2D> lastMouseCoordinates = new SimpleObjectProperty<Point2D>();
        rootPaneContent.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                lastMouseCoordinates.set(new Point2D(event.getX(), event.getY()));
            }
        });

        rootPaneContent.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double deltaX = event.getX() - lastMouseCoordinates.get().getX();
                double extraWidth = rootPaneContent.getLayoutBounds().getWidth() - rootPane.getViewportBounds().getWidth();
                double deltaH = deltaX * (rootPane.getHmax() - rootPane.getHmin()) / extraWidth;
                double desiredH = rootPane.getHvalue() - deltaH;
                rootPane.setHvalue(Math.max(0, Math.min(rootPane.getHmax(), desiredH)));

                double deltaY = event.getY() - lastMouseCoordinates.get().getY();
                double extraHeight = rootPaneContent.getLayoutBounds().getHeight() - rootPane.getViewportBounds().getHeight();
                double deltaV = deltaY * (rootPane.getHmax() - rootPane.getHmin()) / extraHeight;
                double desiredV = rootPane.getVvalue() - deltaV;
                rootPane.setVvalue(Math.max(0, Math.min(rootPane.getVmax(), desiredV)));
            }
        });
    }
    //Feature disabled

    //Feature disabled
    private void defaultScroll() {
        Node rootPaneContent = rootPane.getContent();
        Point2D scrollOffset = figureScrollOffset(rootPaneContent, rootPane);

        drawGroup.setScaleX(drawGroup.getScaleX() * defaultScrollFactor);
        drawGroup.setScaleY(drawGroup.getScaleY() * defaultScrollFactor);

        // move viewport so that old center remains in the center after the
        // scaling
        repositionScroller(rootPaneContent, rootPane, defaultScrollFactor, scrollOffset);
    }
    //Feature disabled

    //Feature disabled
    private Point2D figureScrollOffset(Node scrollContent, ScrollPane scroller) {
        System.out.println("bla");

        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        double hScrollProportion = (scroller.getHvalue() - scroller.getHmin()) / (scroller.getHmax() - scroller.getHmin());
        double scrollXOffset = hScrollProportion * Math.max(0, extraWidth);
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        double vScrollProportion = (scroller.getVvalue() - scroller.getVmin()) / (scroller.getVmax() - scroller.getVmin());
        double scrollYOffset = vScrollProportion * Math.max(0, extraHeight);
        return new Point2D(scrollXOffset, scrollYOffset);
    }
    //Feature disabled

    //Feature disabled
    private void repositionScroller(Node scrollContent, ScrollPane scroller, double scaleFactor, Point2D scrollOffset) {
        double scrollXOffset = scrollOffset.getX();
        double scrollYOffset = scrollOffset.getY();
        double extraWidth = scrollContent.getLayoutBounds().getWidth() - scroller.getViewportBounds().getWidth();
        if (extraWidth > 0) {
            double halfWidth = scroller.getViewportBounds().getWidth() / 2 ;
            double newScrollXOffset = (scaleFactor - 1) *  halfWidth + scaleFactor * scrollXOffset;
            scroller.setHvalue(scroller.getHmin() + newScrollXOffset * (scroller.getHmax() - scroller.getHmin()) / extraWidth);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
        double extraHeight = scrollContent.getLayoutBounds().getHeight() - scroller.getViewportBounds().getHeight();
        if (extraHeight > 0) {
            double halfHeight = scroller.getViewportBounds().getHeight() / 2 ;
            double newScrollYOffset = (scaleFactor - 1) * halfHeight + scaleFactor * scrollYOffset;
            scroller.setVvalue(scroller.getVmin() + newScrollYOffset * (scroller.getVmax() - scroller.getVmin()) / extraHeight);
        } else {
            scroller.setHvalue(scroller.getHmin());
        }
    }
    //Feature disabled

    public void setParent(MainController parent) {
        this.parent = parent;
    }

    /*
    public void clearLastSelectedCircle() {
        lastSelectedRectangle.clear();
    }
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initStatus();
    }

    //Set the elements to the initial status --> disable and clear them
    private void initStatus() {
    }

    public void drawSugiyamaFramework(
        GraphProjectionSugiyama sugiyamaProjection
    ) {
        long timeStart = System.currentTimeMillis();

//        if (!drawGroup.getChildren().isEmpty()) {
//            defaultScroll();
//        }
//        scrollHandling();


        drawGroup.getChildren().clear();

        Map<Integer, Layer> indexLayerAssociation = sugiyamaProjection.getIndexLayerAssociation();
        chromosomeLineAssociation = new HashMap<>();

        drawVertices(sugiyamaProjection, indexLayerAssociation);
        drawEdges(indexLayerAssociation);

        long timeEnd = System.currentTimeMillis();
        System.out.println("ZEIT - Zeichnen: " + (timeEnd - timeStart));
        //Test!!!
//        sugiyamaProjection.getCSVLine().append((timeEnd - timeStart) + ";");
        //Test!!!
    }

    public void reDrawSugiyamaFramework(
            Set<Vertex> chromosomesToDraw
    ) {
//        drawPane.getChildren().clear();
        for (Chromosome chromosome : chromosomeLineAssociation.keySet()) {
            Color pathColor;
            if (chromosomesToDraw.contains(chromosome.getAssociatedChromosomeVertex())) {
                pathColor = chromosome.getColor();
            } else {
                pathColor = Configuration.getDefaultColor();
            }

            for (Path path : chromosomeLineAssociation.get(chromosome)) {
                path.setStroke(pathColor);
            }
        }
    }

    private void drawVertices(
        GraphProjectionSugiyama sugiyamaProjection,
        Map<Integer, Layer> indexLayerAssociation
    ) {
        diffDrawLengthVertex = maxDrawLengthVertex - minDrawLengthVertex;
        maxSequenceLength = sugiyamaProjection.getLongestAlignmentBlock();
        minSequenceLength = sugiyamaProjection.getShortestAlignmentBlock();
        diffSequenceLength = maxSequenceLength - minSequenceLength;
        double currentX = 100;

        for (int indexLayer = 0; indexLayer < indexLayerAssociation.keySet().size(); indexLayer++) {
            Layer currentLayer = indexLayerAssociation.get(indexLayer);

            double drawHeight = Math.max(minDrawHeightVertex, currentLayer.getNeededVertexHeight());
            double verticalInterSpace = Math.max(minVerticlaInterSpace, currentLayer.getNeededInterLayerSpace());

            double percent = (currentLayer.getLongestAlignmentBlockInLayer() - minSequenceLength) / diffSequenceLength;
            double longestLength = minDrawLengthVertex + (percent * diffDrawLengthVertex);
            currentX += 0.5 * longestLength;

            currentLayer.setXPosition(currentX);
            currentLayer.setCurrentXMiddelPointPosition(currentX + 0.5 * longestLength);

            for (VertexSugiyama vertex : currentLayer.getVertexOrderList()) {
                double vertexLength;
                Color vertexColor;
                if (vertex.isDummyNode()) {
                    finalePlacementDummyVertex(vertex, drawHeight);
                } else if (vertex.isJoinedVertex()) {
                    percent = (vertex.getSequenceLength() - minSequenceLength) / diffSequenceLength;
                    vertexLength = minDrawLengthVertex + (percent * diffDrawLengthVertex);
                    vertexColor = Color.rgb(243, 117, 0);
                    drawVertex(vertex, currentX, vertexLength, drawHeight, vertexColor);
                } else {
                    percent = (vertex.getSequenceLength() - minSequenceLength) / diffSequenceLength;
                    vertexLength = minDrawLengthVertex + (percent * diffDrawLengthVertex);
                    vertexColor = Color.rgb(0, 117, 243);
                    drawVertex(vertex, currentX, vertexLength, drawHeight, vertexColor);
                }
            }

            currentX += (0.5 * longestLength) + verticalInterSpace;
        }
    }

    private void drawEdges(
        Map<Integer, Layer> indexLayerAssociation
    ) {
        for (int indexLayer = 0; indexLayer < indexLayerAssociation.keySet().size() - 1; indexLayer++) {
            Layer currentLayer = indexLayerAssociation.get(indexLayer);

            for (VertexSugiyama vertex : currentLayer.getVertexOrderList()) {
                for (EdgeSugiyama upEdge : vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.UP)) {
                    drawEdge(upEdge, VerticalEdgeDirection.UP);
                }

                for (EdgeSugiyama straightEdge : vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.STRAIGHT)) {
                    drawEdge(straightEdge, VerticalEdgeDirection.STRAIGHT);
                }

                for (EdgeSugiyama downEdge : vertex.getOutEdgesClassifiedByVerticalDirection().get(VerticalEdgeDirection.DOWN)) {
                    drawEdge(downEdge, VerticalEdgeDirection.DOWN);
                }
            }
        }
    }

    private void postProcessEdgePlacing(VertexSugiyama drawVertex, double yDrawCoordinate, double height) {
        for (EdgeSugiyama outEdge : drawVertex.getOutEdges()) {
            outEdge.increaseYStart(EdgeDirection.FORWARD, yDrawCoordinate);
            outEdge.increaseYStart(EdgeDirection.BACKWARD, yDrawCoordinate);
        }

        for (EdgeSugiyama inEdge : drawVertex.getInEdges()) {
            inEdge.increaseyEnd(EdgeDirection.FORWARD, yDrawCoordinate);
            inEdge.increaseyEnd(EdgeDirection.BACKWARD, yDrawCoordinate);
        }

    }

    private void finalePlacementDummyVertex(
            VertexSugiyama vertex,
            double height
    ) {
        double yDrawCoordinate = vertex.getAssociatedBlockSet().getDrawingPosition() * (height + 20) + height;
        yDrawCoordinate += 0.5 * height;

        postProcessEdgePlacing(vertex, yDrawCoordinate, height);
    }

    private void drawVertex(
        VertexSugiyama vertex,
        double currentX,
        double length,
        double height,
        Color color
    ) {
        double xDrawCoordinate = currentX;
        double yDrawCoordinate = vertex.getAssociatedBlockSet().getDrawingPosition() * (height + 20) + height;
        yDrawCoordinate += 0.5 * height;

        postProcessEdgePlacing(vertex, yDrawCoordinate, height);

        Group vertexTextGroup = new Group();

        Rectangle rectangle = new Rectangle(Math.round(xDrawCoordinate),
                                            Math.round(yDrawCoordinate),
                                            length, height, color,
                                            vertex);
        vertexTextGroup.getChildren().add(rectangle);

        Text text = new Text(0, 0, String.valueOf(vertex.getText()));
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
        text.setFill(Color.WHITE);
        length = text.getBoundsInLocal().getWidth();
        height = text.getBoundsInLocal().getHeight();
        text.relocate(Math.round(xDrawCoordinate - (0.5 * length)), Math.round(yDrawCoordinate - (0.5 * height)));
        vertexTextGroup.getChildren().add(text);

        vertexTextGroup.setCursor(Cursor.HAND);

        drawGroup.getChildren().add(vertexTextGroup);
    }

    private List<Double> createTriangle(double posX, double posY, double height) {


        List<Double> pointList = new ArrayList<>();
        pointList.add(Double.valueOf(Math.round(posX)));
        pointList.add(Double.valueOf(Math.round(posY)));
        pointList.add(Double.valueOf(Math.round(posX - triangleLength)));
        pointList.add(Double.valueOf(Math.round(posY - (0.5 * height))));
        pointList.add(Double.valueOf(Math.round(posX - triangleLength)));
        pointList.add(Double.valueOf(Math.round(posY + (0.5 * height))));
        return pointList;
    }

    private void drawEdge(
        EdgeSugiyama edge,
        VerticalEdgeDirection verticalDirection
    ) {
        List<EdgeDirection> directionList = new ArrayList<>();
        if (verticalDirection == VerticalEdgeDirection.DOWN) {
            directionList.add(EdgeDirection.BACKWARD);
            directionList.add(EdgeDirection.FORWARD);
        } else {
            directionList.add(EdgeDirection.FORWARD);
            directionList.add(EdgeDirection.BACKWARD);
        }

        //initialize Arrows
        Map<EdgeDirection, Arrow> drawedArrows = new HashMap<>();
        if (drawedArrows.isEmpty()) {
            for (EdgeDirection direction : directionList) {
                Arrow arrow = new Arrow();

                for (Chromosome chromosomePath : edge.getAssociatedChromosomes().getChromosomePaths(direction)) {
                    Path curve = new Path();
                    arrow.getCurvePaths().add(curve);
                }
                if (arrow.getCurvePaths().size() > 0) {
                    drawedArrows.put(direction, arrow);
//                    System.out.println(edge.getOutNode().getId() + " -->" + edge.getInNode().getId() + "|" + direction + "|" + drawedArrows.get(direction).getCurvePaths().size());
                }
            }
        }

        for (EdgeDirection direction : directionList) {
            Arrow drawedArrow = drawedArrows.get(direction);
            int curveIndex = 0;

            if (!edge.getAssociatedChromosomes().isEmpty(direction)) {

                VertexSugiyama vertexLeft = edge.getOutNode();
                VertexSugiyama vertexRight = edge.getInNode();
                Rectangle rectangleLeft = vertexLeft.getRectangle();
                Rectangle rectangleRight = vertexRight.getRectangle();

                double edgeThickness = edge.getDrawingThicknesFactor() * edge.getAssociatedChromosomes().size(direction);
                double halfEdgeThickness = edgeThickness * 0.5;

                double yLeftConnectionPoint = edge.getyStart(direction);
                double yRightConnectionPoint = edge.getyEnd(direction);

                double xLeftConnectionPoint;
                if (vertexLeft.isDummyNode()) {
                    xLeftConnectionPoint = vertexLeft.getAssociatedLayer().getXPosition();
                } else {
                    xLeftConnectionPoint = rectangleLeft.getX() + rectangleLeft.getWidth() + 0.5 * edge.getDrawingThicknesFactor();
                }

                double xRightConnectionPoint;
                if (vertexRight.isDummyNode()) {
                    xRightConnectionPoint = vertexRight.getAssociatedLayer().getXPosition();
                } else {
                    xRightConnectionPoint = rectangleRight.getX()- 0.5 * edge.getDrawingThicknesFactor();
                }

                double xEdgeMiddelPoint;
                if (verticalDirection == VerticalEdgeDirection.UP) {
                    xEdgeMiddelPoint = edge.getOutNode().getAssociatedLayer().getCurrentXMiddelPointPosition() + edge.getxMiddelPoint(direction);
                } else {
                    xEdgeMiddelPoint = edge.getOutNode().getAssociatedLayer().getCurrentXMiddelPointPosition() + edge.getxMiddelPoint(direction) + edgeThickness;
                }

                for (Chromosome chromosomePath : edge.getAssociatedChromosomes().getChromosomePaths(direction)) {

                    yLeftConnectionPoint += 0.5 * edge.getDrawingThicknesFactor();
                    yRightConnectionPoint += 0.5 * edge.getDrawingThicknesFactor();
                    if (verticalDirection.equals(VerticalEdgeDirection.UP)) {
                        xEdgeMiddelPoint += 0.5 * edge.getDrawingThicknesFactor();
                    } else {
                        xEdgeMiddelPoint = xEdgeMiddelPoint - 0.5 * edge.getDrawingThicknesFactor();
                    }

                    VertexSugiyama connectionVertex;
                    if (curveIndex == 0) {
                        double xConnectionPoint;
                        double yConnectionPoint;
                        if (direction.equals(EdgeDirection.FORWARD)) {
                            connectionVertex = vertexRight;
                        } else {
                            connectionVertex = vertexLeft;
                        }
                        if (!connectionVertex.isDummyNode()) {
                            if (direction.equals(EdgeDirection.FORWARD)) {
                                xRightConnectionPoint -= triangleLength;

                                xConnectionPoint = xRightConnectionPoint + 0.5 * edge.getDrawingThicknesFactor() + triangleLength;
                                yConnectionPoint = yRightConnectionPoint - 0.5 * edge.getDrawingThicknesFactor() + halfEdgeThickness;
                            } else {
                                xLeftConnectionPoint += triangleLength;

                                xConnectionPoint = xLeftConnectionPoint - 0.5 * edge.getDrawingThicknesFactor() - triangleLength;
                                yConnectionPoint = yLeftConnectionPoint - 0.5 * edge.getDrawingThicknesFactor() + halfEdgeThickness;
                            }

                            //Triangle for the arrow
                            drawedArrow.getTriangle().getTransforms().clear();
                            drawedArrow.getTriangle().getPoints().clear();

                            double arrowHeight = Math.max(minTriangleHeight, edgeThickness);

                            drawedArrow.getTriangle().getPoints().addAll(createTriangle(xConnectionPoint, yConnectionPoint, arrowHeight));
                            drawedArrow.getTriangle().setStroke(Color.BLACK);
                            drawedArrow.getTriangle().setFill(Color.BLACK);

                            //rotate triangle around the focus point of the triangle --> pivot-point
                            if (direction.equals(EdgeDirection.BACKWARD)) {
                                Rotate rotation = new Rotate();
                                javafx.beans.property.Property xConnectionPointProperty = new SimpleDoubleProperty(xConnectionPoint);
                                Property yConnectionPointProperty = new SimpleDoubleProperty(yConnectionPoint);
                                rotation.pivotXProperty().bind(xConnectionPointProperty);
                                rotation.pivotYProperty().bind(yConnectionPointProperty);
                                drawedArrow.getTriangle().getTransforms().addAll(rotation);
                                rotation.setAngle(180);
                            }
                        }
                    }

                    Path curve = drawedArrow.getCurvePaths().get(curveIndex);
                    curve.getElements().clear();
                    MoveTo start = new MoveTo();
                    start.setX(xLeftConnectionPoint);
                    start.setY(yLeftConnectionPoint);

                    LineTo lineTo3 = new LineTo();
                    lineTo3.setX(xRightConnectionPoint);
                    lineTo3.setY(yRightConnectionPoint);

                    if (!verticalDirection.equals(VerticalEdgeDirection.STRAIGHT)) {
                        LineTo lineTo1 = new LineTo();
                        LineTo lineTo2 = new LineTo();
                        lineTo1.setX(xEdgeMiddelPoint);
                        lineTo1.setY(yLeftConnectionPoint);
                        lineTo2.setX(xEdgeMiddelPoint);
                        lineTo2.setY(yRightConnectionPoint);
                        curve.getElements().add(start);
                        curve.getElements().add(lineTo1);
                        curve.getElements().add(lineTo2);
                        curve.getElements().add(lineTo3);
                    } else {
                        curve.getElements().add(start);
                        curve.getElements().add(lineTo3);
                    }

                    //Special additional connection if Dummy-Shift was necessary
                    if (vertexRight.isDummyNode() && edge.isShiftedInVertexEndPoint()) {
                        EdgeSugiyama nextEdge = vertexRight.getOutEdges().get(0);
                        LineTo lineTo4 = new LineTo();
                        lineTo4.setX(xRightConnectionPoint);
                        lineTo4.setY(nextEdge.getyStart(direction) + 0.5 * nextEdge.getDrawingThicknesFactor());
                        curve.getElements().add(lineTo4);
                    }

                    curve.setStrokeWidth(edge.getDrawingThicknesFactor());
                    curve.setStroke(Configuration.getDefaultColor());

                    if (!chromosomeLineAssociation.containsKey(chromosomePath)) {
                        chromosomeLineAssociation.put(chromosomePath, new ArrayList<>());
                    }
                    chromosomeLineAssociation.get(chromosomePath).add(curve);

                    //prepare for next step
                    curveIndex++;

                    yLeftConnectionPoint += 0.5 * edge.getDrawingThicknesFactor();
                    yRightConnectionPoint += 0.5 * edge.getDrawingThicknesFactor();
                    if (verticalDirection.equals(VerticalEdgeDirection.UP) || verticalDirection.equals(VerticalEdgeDirection.STRAIGHT)) {
                        xEdgeMiddelPoint += 0.5 * edge.getDrawingThicknesFactor();
                    } else {
                        xEdgeMiddelPoint = xEdgeMiddelPoint - 0.5 * edge.getDrawingThicknesFactor();
                    }
//                    }

                }
//                edge.getOutNode().getAssociatedLayer().setCurrentXMiddelPointPosition(edge.getOutNode().getAssociatedLayer().getCurrentXMiddelPointPosition() + edgeThickness + sugiyamaProjection.getSpaceFactor() * edge.getDrawingThicknesFactor());

//                shift += 7;
            }
        }

        for (EdgeDirection direction : drawedArrows.keySet()) {
            Arrow arrow = drawedArrows.get(direction);
            for (javafx.scene.shape.Path curve : arrow.getCurvePaths()) {
                drawGroup.getChildren().add(curve);
            }
            if (!arrow.getTriangle().getPoints().isEmpty()) {
                drawGroup.getChildren().add(arrow.getTriangle());
            }
        }
    }
}
