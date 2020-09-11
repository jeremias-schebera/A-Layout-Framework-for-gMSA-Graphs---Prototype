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
public class InEdgeComparator
    implements Comparator<EdgeSugiyama> {

    @Override
    public int compare(EdgeSugiyama edge1, EdgeSugiyama edge2) {
        VertexSugiyama vertex1 = edge1.getOutNode();
        VertexSugiyama vertex2 = edge2.getOutNode();

        return (vertex1.getAssociatedBlockSet().getDrawingPosition() - vertex2.getAssociatedBlockSet().getDrawingPosition());
    }
}
