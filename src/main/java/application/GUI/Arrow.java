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

import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class Arrow {

    private Polygon triangle;
//    private Line line;
    private List<Path> curvePaths;
//    private Path curve2;

    public Arrow() {
//        this.line = new Line();
        this.curvePaths = new ArrayList<>();
//        this.curve2 = new Path();
        this.triangle = new Polygon();
    }

    public Polygon getTriangle() {
        return triangle;
    }

//    public Line getLine() {
//        return line;
//    }
    public List<Path> getCurvePaths() {
        return curvePaths;
    }

//    public Path getCurve2() {
//        return curve2;
//    }
}
