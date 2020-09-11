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
package  main.java.application.old_stuff;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private List<NodeOWNALGO> pathVertices;
    private GraphProjectionOWNALGO associatedProjection;
    private boolean isConsensusPath;
    private List<UndrawnPath> undrawnParts;
    private Color color;

    public Path(List<NodeOWNALGO> pathVertices) {
        this.pathVertices = pathVertices;
    }

    public Path(boolean isConsensusPath, GraphProjectionOWNALGO associatedProjection, Color color) {
        this.pathVertices = pathVertices;
        this.isConsensusPath = isConsensusPath;
        this.associatedProjection = associatedProjection;
        this.undrawnParts = new ArrayList<>();
        this.color = color;
    }

    public List<UndrawnPath> identifyUndrawnPaths(Path associatedPath) {
        System.out.println("###");
        List<NodeOWNALGO> undrawnPart = new ArrayList<>();
        List<UndrawnPath> undrawnPathList= new ArrayList<>();
        for (NodeOWNALGO pathVertex : pathVertices) {
            undrawnPart.add(pathVertex);
            if (pathVertex.isPlaced() && undrawnPart.size() > 1) {
                UndrawnPath tempUndrawnPath = new UndrawnPath(undrawnPart, associatedPath);
                tempUndrawnPath.printPathVerices();
                undrawnPathList.add(tempUndrawnPath);
                undrawnPart = new ArrayList<>();
                undrawnPart.add(pathVertex);
            }
        }
        return undrawnPathList;
    }

    public void printPathVerices() {
        String text = "";
        for (NodeOWNALGO v : pathVertices) {
            text += v.getId() + "\t";
        }
        System.out.println(text);
    }

    public Color getColor() {
        return color;
    }

    public void addPathVertex(NodeOWNALGO pathNodeOWNALGO) {
        pathVertices.add(pathNodeOWNALGO);
    }

    public List<Integer> getDrawPositionList() {
        List<Integer> postionList = new ArrayList<>();
        for (NodeOWNALGO vertex : pathVertices) {
            postionList.add(vertex.getDrawPosition());
        }

        return postionList;
    }

    public List<UndrawnPath> getUndrawnParts() {
        return undrawnParts;
    }

    public List<NodeOWNALGO> getPathVertices() {
        return this.pathVertices;
    }

    public void setPathVertices(List<NodeOWNALGO> pathVertices) {
        this.pathVertices = pathVertices;
    }

    public boolean isConsensusPath() {
        return isConsensusPath;
    }

    public void setIsConsensusPath(boolean isConsensusPath) {
        this.isConsensusPath = isConsensusPath;
    }
}
