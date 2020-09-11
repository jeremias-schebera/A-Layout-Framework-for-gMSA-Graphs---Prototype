package main.java.application.data;

import javafx.scene.paint.Color;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

public class Chromosome {

    private List<Vertex> graphDBVerticesPath;
    private Vertex associatedChromosomeVertex;
    private List<VertexSugiyama> sugiyamaVertices;
    private boolean isSuperGenome = false;
    private Color color;

    public Chromosome(
        Vertex associatedChromosomeVertex,
        List<Vertex> graphDBVerticesPath,
        Color color
    ) {
        this.graphDBVerticesPath = graphDBVerticesPath;
        this.associatedChromosomeVertex = associatedChromosomeVertex;
        this.color = color;

        if (associatedChromosomeVertex == null) {
            isSuperGenome = true;
        }
    }

    public List<Vertex> getGraphDBVerticesPath() {
        return graphDBVerticesPath;
    }

    public Vertex getAssociatedChromosomeVertex() {
        return associatedChromosomeVertex;
    }

    public List<VertexSugiyama> getSugiyamaVertices() {
        return sugiyamaVertices;
    }

    public void setSugiyamaVertices(List<VertexSugiyama> sugiyamaVertices) {
        this.sugiyamaVertices = sugiyamaVertices;
    }

    public boolean isSuperGenome() {
        return isSuperGenome;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        if (this.getAssociatedChromosomeVertex().property("name").isPresent()) {
            return this.getAssociatedChromosomeVertex().value("name").toString();
        } else {
            return "supergenom";
        }
    }

    @Override
    public String toString() {
        String text;
        if (this.getAssociatedChromosomeVertex().property("name").isPresent()) {
            text = this.getAssociatedChromosomeVertex().value("name").toString() + ": ";
        } else {
            text = "supergenom: ";
        }
        for (Vertex v : this.getGraphDBVerticesPath()) {
            text += v.value("id").toString() + " -> ";
        }
        return text;
    }

    public String toString2() {
        String text = "";
//        if (this.getAssociatedChromosomeVertex().property("name").isPresent()) {
//            text = this.getAssociatedChromosomeVertex().value("name").toString() + ": ";
//        } else {
//            text = "supergenom: ";
//        }
        for (VertexSugiyama v : this.getSugiyamaVertices()) {
            text += v.getText();
            if (v != this.getSugiyamaVertices().get(this.getSugiyamaVertices().size() - 1)) {
                text += " -> ";
            }
        }
        return text;
    }
}
