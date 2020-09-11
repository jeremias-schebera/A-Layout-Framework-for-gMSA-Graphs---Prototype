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
package main.java.application.GUI;

import main.java.application.data.VertexSugiyama;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Rectangle
    extends javafx.scene.shape.Rectangle {

    private VertexSugiyama associatedVertex;
    private Color color;

    public Rectangle(
        double xCenterCoordinate,
        double yCenterCoordinate,
        double length,
        double height,
        Color color,
        VertexSugiyama associatedVertex
    ) {
        setX(xCenterCoordinate - 0.5 * length);
        setY(yCenterCoordinate - 0.5 * height);
        setWidth(length);
        setHeight(height);
//        this.setCenterX(xCenterCoordinate);
//        this.setCenterY(yCenterCoordinate);
//        this.setRadius(radius);
        this.setStroke(color);
        this.setFill(color);

        this.associatedVertex = associatedVertex;
        associatedVertex.setRectangle(this);

        this.color = color;

    }


    public VertexSugiyama getAssociatedVertex() {
        return associatedVertex;
    }

    public Color getColor() {
        return color;
    }

}
