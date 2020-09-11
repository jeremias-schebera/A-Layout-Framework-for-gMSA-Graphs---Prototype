package main.java.application.GUI;

import main.java.application.data.VertexSugiyama;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Rectangle
    extends javafx.scene.shape.Rectangle {

    private VertexSugiyama associatedVertex;
    private Color color;

    public Rectangle(
        double xCenterCoordinate,
        double yCenterCoordinate,
        double length,
        double height,
        Color color,
        VertexSugiyama associatedVertex
    ) {
        setX(xCenterCoordinate - 0.5 * length);
        setY(yCenterCoordinate - 0.5 * height);
        setWidth(length);
        setHeight(height);
//        this.setCenterX(xCenterCoordinate);
//        this.setCenterY(yCenterCoordinate);
//        this.setRadius(radius);
        this.setStroke(color);
        this.setFill(color);

        this.associatedVertex = associatedVertex;
        associatedVertex.setRectangle(this);

        this.color = color;

    }


    public VertexSugiyama getAssociatedVertex() {
        return associatedVertex;
    }

    public Color getColor() {
        return color;
    }

}
