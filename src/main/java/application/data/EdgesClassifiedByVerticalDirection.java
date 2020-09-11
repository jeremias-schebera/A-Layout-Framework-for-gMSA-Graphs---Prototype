package main.java.application.data;

import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author zeckzer
 */
public class EdgesClassifiedByVerticalDirection {

    private List<EdgeSugiyama> upEdges = new ArrayList<>();
    private List<EdgeSugiyama> straightEdges = new ArrayList<>();
    private List<EdgeSugiyama> downEdges = new ArrayList<>();

    public EdgesClassifiedByVerticalDirection() {

    }

    public void add(
        VerticalEdgeDirection edgeDirectionAtVertex,
        EdgeSugiyama edge
    ) {
        switch (edgeDirectionAtVertex) {
            case UP:
                upEdges.add(edge);
                break;
            case STRAIGHT:
                straightEdges.add(edge);
                break;
            case DOWN:
                downEdges.add(edge);
                break;
        }
    }

    public List<EdgeSugiyama> get(
        VerticalEdgeDirection edgeDirectionAtVertex
    ) {
        switch (edgeDirectionAtVertex) {
            case UP:
                return upEdges;
            case STRAIGHT:
                return straightEdges;
            case DOWN:
                return downEdges;
            default:
                return null;
        }
    }
}
