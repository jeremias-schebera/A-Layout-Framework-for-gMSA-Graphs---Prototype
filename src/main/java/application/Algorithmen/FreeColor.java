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
