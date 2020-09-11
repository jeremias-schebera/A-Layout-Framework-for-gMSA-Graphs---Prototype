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

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;
import main.java.application.Algorithmen.FreeColor;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 *
 * @author zeckzer
 */
public class SequenceVerticesData {

    private FreeColor freeColor = new FreeColor();

    private List<Pair<Vertex, List<Vertex>>> sequenceVerticesData;

    public SequenceVerticesData() {
        sequenceVerticesData = new ArrayList<>();
    }

    public void add(
        Pair pair
    ) {
        sequenceVerticesData.add(pair);
    }

    public List<Chromosome> getChromosomes() {
        List<Chromosome> chromosomes = new ArrayList<>();
        for (Pair<Vertex, List<Vertex>> chromosomePair : sequenceVerticesData) {
            chromosomes.add(new Chromosome(chromosomePair.getKey(), chromosomePair.getValue(), freeColor.getFreeColor()));
        }
        return chromosomes;
    }
}
