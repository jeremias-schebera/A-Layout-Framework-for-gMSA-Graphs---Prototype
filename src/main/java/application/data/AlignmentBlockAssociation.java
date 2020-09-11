/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.application.data;

import java.util.HashMap;
import java.util.Map;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 *
 * @author zeckzer
 */
public class AlignmentBlockAssociation {

    private Map<Vertex, Vertex> alignmentBlockAssociation;

    public AlignmentBlockAssociation() {
        alignmentBlockAssociation = new HashMap<>();
    }

    public void add(
        Vertex vertex,
        Vertex alignmentBlock
    ) {
        alignmentBlockAssociation.put(vertex, alignmentBlock);
    }

    public int getLength(
        Vertex vertex
    ) {
        return alignmentBlockAssociation.get(vertex).value("length");
    }
}
