/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.application.data;

import java.util.Comparator;

/**
 *
 * @author zeckzer
 */
public class VertexLayerIndexComparator
    implements Comparator<VertexSugiyama> {

    @Override
    public int compare(VertexSugiyama vertex1, VertexSugiyama vertex2) {

        return (vertex1.getAssociatedBlockSet().getDrawingPosition() - vertex2.getAssociatedBlockSet().getDrawingPosition());
    }
}
