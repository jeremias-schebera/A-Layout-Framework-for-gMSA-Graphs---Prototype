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

import java.util.List;

public class UndrawnPath extends Path {
    private Path associatedPath;
    private boolean isAllowedToPlace = false;
    private double positionDifference;

    public UndrawnPath(List<NodeOWNALGO> pathVertices, Path associatedPath) {
        super(pathVertices);
        this.associatedPath = associatedPath;
    }

    public void updateIsAllowedToPlace() {
        List<NodeOWNALGO> vertexList = super.getPathVertices();
        //If first and last vertex is already placed -->
        if (vertexList.get(0).isPlaced() && vertexList.get(vertexList.size() - 1).isPlaced()) {
            isAllowedToPlace = true;
        }
    }

    public void calculatePositionDifference() {
        updateIsAllowedToPlace();
        if (isAllowedToPlace) {
            List<NodeOWNALGO> vertexList = super.getPathVertices();
            positionDifference = vertexList.get(vertexList.size() - 1).getDrawPosition() - vertexList.get(0).getDrawPosition();
        }
    }

    public double getPositionDifference() {
        calculatePositionDifference();
        return positionDifference;
    }

    public boolean isAllowedToPlace() {
        return isAllowedToPlace;
    }

    public Path getAssociatedPath() {
        return associatedPath;
    }
}
