/*******************************************************************************
 * Copyright (c) 2020 Jeremias Schebera, Dirk Zeckzer, Daniel Wiegreffe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
