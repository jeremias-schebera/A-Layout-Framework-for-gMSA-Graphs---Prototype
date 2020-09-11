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
package  main.java.application.Algorithmen;

import javafx.scene.paint.Color;

import java.util.Stack;

public class FreeColor {
    private Stack<Color> freeColors;
    private final Color defaultColor = Color.rgb(100,100,100);

    public FreeColor() {
        freeColors = new Stack<>();

        freeColors.push(Color.rgb(55,126,184));
        freeColors.push(Color.rgb(255,224,71));
        freeColors.push(Color.rgb(255,127,0));
        freeColors.push(Color.rgb(166,86,40));
        freeColors.push(Color.rgb(247,129,191));
        freeColors.push(Color.rgb(152,78,163));
        freeColors.push(Color.rgb(77,175,74));
        freeColors.push(Color.rgb(228,26,28));
    }

    public Color getFreeColor() {
        if (freeColors.empty()) {
            return defaultColor;
        } else {
            return freeColors.pop();
        }
    }

    public void releaseColor(Color color) {
        freeColors.push(color);
    }

    public Color getDefaultColor() {
        return defaultColor;
    }
}
