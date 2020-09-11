package  main.java.application.old_stuff;

import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class NodeOWNALGO {

    private int orderPostion;
    private Integer id;
    private double orgSceneX;
    private double orgSceneY;
    private double drawLevel = -1;
    private int drawPosition = -1;
    private boolean isPlaced = false;
    private static int gap = 150;
    private Circle circle;
    private List<EdgeOWNALGO> inEdgeOWNALGOS = new ArrayList<EdgeOWNALGO>();
    private List<EdgeOWNALGO> outEdgeOWNALGOS = new ArrayList<EdgeOWNALGO>();
    private static NodeOWNALGO lastSelectedVertex;
    private boolean isPseudoNode;
    private Path associatedPath;

    public NodeOWNALGO(Integer id) {
        this.id = id;
    }

    public NodeOWNALGO(boolean isPseudoNode) {
        this.isPseudoNode = isPseudoNode;
    }

    public void setAssociatedPath(Path associatedPath) {
        if (isPseudoNode) {
            this.associatedPath = associatedPath;
        }
    }

    public Path getAssociatedPath() {
        return associatedPath;
    }

    public void addInEdge(EdgeOWNALGO inEdgeOWNALGO) {
        this.inEdgeOWNALGOS.add(inEdgeOWNALGO);
    }

    public void addOutEdge(EdgeOWNALGO outEdgeOWNALGO) {
        this.outEdgeOWNALGOS.add(outEdgeOWNALGO);
    }

    public EdgeOWNALGO getOutEdgeByEndVertexAndByPath(NodeOWNALGO endVertex, Path pathIdentifier) {
        for (EdgeOWNALGO edgeOWNALGO : outEdgeOWNALGOS) {
            if (edgeOWNALGO.getInNodeOWNALGO() == endVertex && edgeOWNALGO.getPathIdentifier() == pathIdentifier) {
                return edgeOWNALGO;
            }
        }

        return null;
    }

    public boolean isEdgeExistingToEndVertex(NodeOWNALGO endVertex) {
        for (EdgeOWNALGO edgeOWNALGO : outEdgeOWNALGOS) {
            if (edgeOWNALGO.getInNodeOWNALGO() == endVertex) {
                return true;
            }
        }

        return false;
    }

    public int getOutDegree() {
        return outEdgeOWNALGOS.size();
    }

    public int getInDegree() {
        return inEdgeOWNALGOS.size();
    }

    public int getDegreeDifference() {
        return outEdgeOWNALGOS.size() - inEdgeOWNALGOS.size();
    }

    public boolean isSink() {
        if (outEdgeOWNALGOS.size() == 0) {
            return true;
        }
        return false;
    }

    public boolean isSource() {
        if (inEdgeOWNALGOS.size() == 0) {
            return true;
        }
        return false;
    }

    public List<EdgeOWNALGO> getInEdgeOWNALGOS() {
        return inEdgeOWNALGOS;
    }

    public List<EdgeOWNALGO> getOutEdgeOWNALGOS() {
        return outEdgeOWNALGOS;
    }

    public boolean isPseudoNode() {
        return isPseudoNode;
    }

    public void setPseudoNode(boolean pseudoNode) {
        isPseudoNode = pseudoNode;
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void setPlaced(boolean placed) {
        isPlaced = placed;
    }

    public Circle getCircle() {
        return circle;
    }

    public int getOrderPostion() {
        return orderPostion;
    }

    public void setOrderPostion(int orderPostion) {
        this.orderPostion = orderPostion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public double getDrawLevel() {
        return drawLevel;
    }

    public void setDrawLevel(double drawLevel) {
        this.drawLevel = drawLevel;
    }

    public int getDrawPosition() {
        return drawPosition;
    }

    public void setDrawPosition(int drawPosition) {
        this.drawPosition = drawPosition;
    }

    public void drawNode(double radius, Color color) {
        double xDrawCoordinate = drawPosition * gap + gap;
        double yDrawCoordinate = drawLevel * (gap/2) + 1000;
        circle = new Circle(xDrawCoordinate, yDrawCoordinate, radius, color);

        circle.setCursor(Cursor.HAND);

        circle.setOnMousePressed((t) -> {
            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();

            Circle c = (Circle) (t.getSource());
            c.toFront();
        });

        //Selecting Circle
        circle.setOnMouseClicked((event) -> {
            if(lastSelectedVertex != null) {
                lastSelectedVertex.getCircle().setFill(color);
                lastSelectedVertex.getCircle().setStroke(color);
            }
//            circle.setStroke(Color.rgb(100,100,100));
//            circle.setFill(Color.rgb(100,100,100));
            System.out.println(this.getId());
            lastSelectedVertex = this;
        });

        circle.setOnMouseDragged((t) -> {
            double offsetX = t.getSceneX() - orgSceneX;
            double offsetY = t.getSceneY() - orgSceneY;

            Circle c = (Circle) (t.getSource());

            double newX = c.getCenterX() + offsetX;
            if(newX - radius >= 0) {
                c.setCenterX(newX);
            } else {
                c.setCenterX(radius);
            }

            double newY = c.getCenterY() + offsetY;
            if(newY -radius >= 0) {
                c.setCenterY(newY);
            } else {
                c.setCenterY(radius);
            }

            orgSceneX = t.getSceneX();
            orgSceneY = t.getSceneY();

            for (EdgeOWNALGO edgeOWNALGO : this.getInEdgeOWNALGOS()) {
                edgeOWNALGO.drawEdge();
            }

            for (EdgeOWNALGO edgeOWNALGO : this.getOutEdgeOWNALGOS()) {
                edgeOWNALGO.drawEdge();
            }

        });
    }

}
