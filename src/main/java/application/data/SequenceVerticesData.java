/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.application.data;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import main.java.application.Algorithmen.FreeColor;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 *
 * @author zeckzer
 */
public class SequenceVerticesData {

    private FreeColor freeColor = new FreeColor();

    private List<Pair<Vertex, List<Vertex>>> sequenceVerticesData;

    public SequenceVerticesData() {
        sequenceVerticesData = new ArrayList<>();
    }

    public void add(
        Pair pair
    ) {
        sequenceVerticesData.add(pair);
    }

    public List<Chromosome> getChromosomes() {
        List<Chromosome> chromosomes = new ArrayList<>();
        for (Pair<Vertex, List<Vertex>> chromosomePair : sequenceVerticesData) {
            chromosomes.add(new Chromosome(chromosomePair.getKey(), chromosomePair.getValue(), freeColor.getFreeColor()));
        }
        return chromosomes;
    }
}
