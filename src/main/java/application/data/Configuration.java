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

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import main.java.application.Algorithmen.FreeColor;

/**
 *
 * @author zeckzer
 */
public class Configuration {

    private SequenceVerticesData sequenceVerticesData;
    private AlignmentBlockAssociation alignmentBlockAssociation;
    private Boolean isJoinEnabled;
    private double drawingThicknessFactor;
    private int spaceFactor;

    //Test!!!
//    public StringBuffer csvText = new StringBuffer();
//    public void setCsvText(StringBuffer csvText) {
//        this.csvText = csvText;
//    }
//    public StringBuffer getCsvText() {
//        return csvText;
//    }
    //Test!!!

    private static FreeColor freeColor = new FreeColor();

    public SequenceVerticesData getSequenceVerticesData() {
        return sequenceVerticesData;
    }

    public void setSequenceVerticesData(SequenceVerticesData sequenceVerticesData) {
        this.sequenceVerticesData = sequenceVerticesData;
    }

    public AlignmentBlockAssociation getAlignmentBlockAssociation() {
        return alignmentBlockAssociation;
    }

    public void setAlignmentBlockAssociation(AlignmentBlockAssociation alignmentBlockAssociation) {
        this.alignmentBlockAssociation = alignmentBlockAssociation;
    }

    public Boolean getIsJoinEnabled() {
        return isJoinEnabled;
    }

    public void setIsJoinEnabled(Boolean isJoinEnabled) {
        this.isJoinEnabled = isJoinEnabled;
    }

    public double getDrawingThicknessFactor() {
        return drawingThicknessFactor;
    }

    public void setDrawingThicknessFactor(double drawingThicknessFactor) {
        this.drawingThicknessFactor = drawingThicknessFactor;
    }

    public int getSpaceFactor() {
        return spaceFactor;
    }

    public void setSpaceFactor(int spaceFactor) {
        this.spaceFactor = spaceFactor;
    }

    public static Color getDefaultColor() {
        return freeColor.getDefaultColor();
    }
}
