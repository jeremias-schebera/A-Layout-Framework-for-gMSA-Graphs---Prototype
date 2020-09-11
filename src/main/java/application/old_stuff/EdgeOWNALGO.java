package  main.java.application.old_stuff;

import  main.java.application.GUI.Arrow;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EdgeOWNALGO {
    private NodeOWNALGO inNodeOWNALGO;
    private NodeOWNALGO outNodeOWNALGO;
    private Path pathIdentifier;
    private Arrow drawedArrow;
    private boolean isAllowedToDraw;
    private boolean isDrawed;
    private Color color;
    private int edgeNumberDrawing;
    private int totalEdgeCountDrawing;
    private int directionDrawing;
    public  boolean isConsensus = false;

    public EdgeOWNALGO(NodeOWNALGO outNodeOWNALGO, NodeOWNALGO inNodeOWNALGO, Path associatedPath) {
        this.inNodeOWNALGO = inNodeOWNALGO;
        this.outNodeOWNALGO = outNodeOWNALGO;
        this.pathIdentifier = associatedPath;
        inNodeOWNALGO.addInEdge(this);
        outNodeOWNALGO.addOutEdge(this);
        this.isAllowedToDraw = true;
        this.isDrawed = false;
        drawedArrow = new Arrow();
        this.color = associatedPath.getColor();
        this.edgeNumberDrawing = 0;
        this.totalEdgeCountDrawing = 1;

    }

    public void setEdgeNumberDrawing(int edgeNumberDrawing) {
        this.edgeNumberDrawing = edgeNumberDrawing;
    }

    public void setTotalEdgeCountDrawing(int totalEdgeCountDrawing) {
        this.totalEdgeCountDrawing = totalEdgeCountDrawing;
    }

    public void setDirectionDrawing(int directionDrawing) {
        this.directionDrawing = directionDrawing;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Path getPathIdentifier() {
        return pathIdentifier;
    }

    public NodeOWNALGO getInNodeOWNALGO() {
        return inNodeOWNALGO;
    }

    public NodeOWNALGO getOutNodeOWNALGO() {
        return outNodeOWNALGO;
    }

    public Arrow getdrawedArrow() {
        return drawedArrow;
    }

    public boolean isAllowedToDraw() {
        return isAllowedToDraw;
    }

    public boolean isDrawed() {
        return isDrawed;
    }

    public void setDrawed(boolean drawed) {
        isDrawed = drawed;
    }

    public void setAllowedToDraw(boolean allowedToDraw) {
        isAllowedToDraw = allowedToDraw;
    }

    public List<EdgeOWNALGO> getAllEdgesBetweenEdgeVertices() {
        List<EdgeOWNALGO> edgeOWNALGOList = new ArrayList<>();
        //forward direction
        for (EdgeOWNALGO edgeOWNALGO : outNodeOWNALGO.getOutEdgeOWNALGOS()) {
            if (edgeOWNALGO.getInNodeOWNALGO() == inNodeOWNALGO && edgeOWNALGO.isAllowedToDraw) {
                if (edgeOWNALGO.isDrawed == false) {
                    edgeOWNALGOList.add(edgeOWNALGO);
                    edgeOWNALGO.setDirectionDrawing(0);
                } else {
                    break;
                }
            }
        }
        //backward direction
        for (EdgeOWNALGO edgeOWNALGO : inNodeOWNALGO.getOutEdgeOWNALGOS()) {
            if (edgeOWNALGO.getInNodeOWNALGO() == outNodeOWNALGO) {
                if (edgeOWNALGO.isDrawed == false && edgeOWNALGO.isAllowedToDraw) {
                    edgeOWNALGOList.add(edgeOWNALGO);
                    edgeOWNALGO.setDirectionDrawing(1);
                } else {
                    break;
                }
            }
        }
        return edgeOWNALGOList;
    }

    private List<Double> createTriangle(double shiftX, double shiftY) {
        // shiftX und shiftY sind der Mittelpunkt des Dreiecks
        double triangleSize = 20;
        List<Double> pointList = new ArrayList<>(Arrays.asList(
//                shiftX - (size / 3), shiftY,
                shiftX - (triangleSize / 3), shiftY - (0.5 * triangleSize),
                shiftX + (2 * (triangleSize / 3)), shiftY,
                shiftX - (triangleSize / 3), shiftY + (0.5 * triangleSize)));
        return pointList;
    }

    public void drawEdge() {
        Circle circleStart = outNodeOWNALGO.getCircle();
        Circle circleEnd = inNodeOWNALGO.getCircle();

        //calculation of required values
        double deltaX = circleEnd.getCenterX() - circleStart.getCenterX();
        double deltaY = circleEnd.getCenterY() - circleStart.getCenterY();
        double mIncrease = deltaY / deltaX;
        double angleRad = Math.atan(mIncrease);
        double angleDegree = angleRad * 180 / Math.PI;
        //rotate the angle 180 degrees --> because the arrow else points in the wrong direction
        if(deltaX < 0) {
            angleDegree += 180;
            angleRad += Math.PI;
        }

        //horizontal shift of the edge
        double verticalShiftStep = Math.PI / (totalEdgeCountDrawing + 1);
        int middleEdgeCountPosition = (int) Math.ceil((totalEdgeCountDrawing - 1) / 2);
        double shiftVertical = (edgeNumberDrawing - middleEdgeCountPosition) * verticalShiftStep;
        double angleRad1 = angleRad + shiftVertical;
        double angleRad2 = angleRad - shiftVertical;
        if (directionDrawing == 0) {
            angleRad1 = angleRad - shiftVertical;
            angleRad2 = angleRad + shiftVertical;
        }

        //Point of contact of the line and the circle --> start and end coordinates for the line --> associated with the horizontal shift
        double xCircleStartConnectionPoint = circleStart.getRadius() * Math.cos(angleRad1) + circleStart.getCenterX();
        double yCircleStartConnectionPoint = circleStart.getRadius() * Math.sin(angleRad1) + circleStart.getCenterY();
        double xCircleEndConnectionPoint = circleEnd.getRadius() * Math.cos(angleRad2 + Math.PI) + circleEnd.getCenterX();
        double yCircleEndConnectionPoint = circleEnd.getRadius() * Math.sin(angleRad2 + Math.PI) + circleEnd.getCenterY();
//        drawedArrow.getLine().setStartX(xCircleStartConnectionPoint);
//        drawedArrow.getLine().setStartY(yCircleStartConnectionPoint);
//        drawedArrow.getLine().setEndX(xCircleEndConnectionPoint);
//        drawedArrow.getLine().setEndY(yCircleEndConnectionPoint);
//        drawedArrow.getLine().setStrokeWidth(3);
//        drawedArrow.getLine().setStroke(color);
//        if (isGuideSequence == false) {
//            drawedArrow.getLine().setOpacity(0.5);
//        }

        //new delta values with the point of contact coordinates
        deltaX = xCircleEndConnectionPoint - xCircleStartConnectionPoint;
        deltaY = yCircleEndConnectionPoint - yCircleStartConnectionPoint;

        //place triangel on line --> vertical placing
        double horizontalShiftStep = 1.0 / (totalEdgeCountDrawing + 1);
        double shiftHorizontal = (edgeNumberDrawing - middleEdgeCountPosition) * horizontalShiftStep;
        //if the dirction is backward --> the shift has to be turned around
        if (directionDrawing == 1) {
            shiftHorizontal =  - shiftHorizontal;
        }
        double prorportion = 0.5 + shiftHorizontal;
        double xMiddelLine = xCircleStartConnectionPoint + prorportion * deltaX;
        double yMiddelLine = yCircleStartConnectionPoint + prorportion * deltaY;

        //Triangle for the arrow
        drawedArrow.getTriangle().getTransforms().clear();
        drawedArrow.getTriangle().getPoints().clear();
        drawedArrow.getTriangle().getPoints().addAll(createTriangle(xMiddelLine, yMiddelLine));
        drawedArrow.getTriangle().setStroke(color);
        drawedArrow.getTriangle().setFill(color);
//        if (isGuideSequence == false) {
//            drawedArrow.getTriangle().setOpacity(0.5);
//        }


        //rotate triangle around the focus point of the triangle --> pivot-point
        Rotate rotation = new Rotate();
        Property xMiddleProperty = new SimpleDoubleProperty(xMiddelLine);
        Property yMiddleProperty = new SimpleDoubleProperty(yMiddelLine);
        rotation.pivotXProperty().bind(xMiddleProperty);
        rotation.pivotYProperty().bind(yMiddleProperty);
        drawedArrow.getTriangle().getTransforms().addAll(rotation);
        rotation.setAngle(angleDegree);

    }
}
