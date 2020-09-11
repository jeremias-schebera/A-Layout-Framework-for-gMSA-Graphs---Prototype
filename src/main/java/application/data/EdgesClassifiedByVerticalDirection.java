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
