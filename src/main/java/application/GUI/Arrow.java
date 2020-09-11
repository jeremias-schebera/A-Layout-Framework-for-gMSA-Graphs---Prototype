package main.java.application.GUI;

import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class Arrow {

    private Polygon triangle;
//    private Line line;
    private List<Path> curvePaths;
//    private Path curve2;

    public Arrow() {
//        this.line = new Line();
        this.curvePaths = new ArrayList<>();
//        this.curve2 = new Path();
        this.triangle = new Polygon();
    }

    public Polygon getTriangle() {
        return triangle;
    }

//    public Line getLine() {
//        return line;
//    }
    public List<Path> getCurvePaths() {
        return curvePaths;
    }

//    public Path getCurve2() {
//        return curve2;
//    }
}
