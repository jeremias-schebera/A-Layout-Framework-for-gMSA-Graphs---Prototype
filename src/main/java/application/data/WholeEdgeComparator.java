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
public class WholeEdgeComparator
    implements Comparator<EdgeSugiyama> {

    @Override
    public int compare(EdgeSugiyama e1, EdgeSugiyama e2) {
        int diffE1;
        int diffE2;

//        if (e1.getInNode().getAssociatedLayer().getIndex() < e1.getOutNode().getAssociatedLayer().getIndex()) {
//            diffE1 = e1.getInNode().getAssociatedBlock().getPosition() - e1.getOutNode().getAssociatedBlock().getPosition();
//        } else {
//            diffE1 = e1.getOutNode().getAssociatedBlock().getPosition() - e1.getInNode().getAssociatedBlock().getPosition();
//        }
//
//        if (e2.getInNode().getAssociatedLayer().getIndex() < e2.getOutNode().getAssociatedLayer().getIndex()) {
//            diffE2 = e2.getInNode().getAssociatedBlock().getPosition() - e2.getOutNode().getAssociatedBlock().getPosition();
//        } else {
//            diffE2 = e2.getOutNode().getAssociatedBlock().getPosition() - e2.getInNode().getAssociatedBlock().getPosition();
//        }
        diffE1 = Math.abs(e1.getInNode().getAssociatedBlock().getPosition() - e1.getOutNode().getAssociatedBlock().getPosition());
        diffE2 = Math.abs(e2.getInNode().getAssociatedBlock().getPosition() - e2.getOutNode().getAssociatedBlock().getPosition());

        return (diffE2 - diffE1);
    }
}
