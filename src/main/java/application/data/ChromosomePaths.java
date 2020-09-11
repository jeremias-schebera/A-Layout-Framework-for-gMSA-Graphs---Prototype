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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author zeckzer
 */
public class ChromosomePaths {

    private Map<EdgeDirection, List<Chromosome>> chromosomePaths;

    public ChromosomePaths() {
        chromosomePaths = new HashMap<>();
        chromosomePaths.put(EdgeDirection.BACKWARD, new ArrayList<>());
        chromosomePaths.put(EdgeDirection.FORWARD, new ArrayList<>());
    }

    public void add(
            EdgeDirection direction,
            Chromosome chromosome
    ) {
        chromosomePaths.get(direction).add(chromosome);
    }

    public boolean isEmpty(
            EdgeDirection direction
    ) {
        return chromosomePaths.get(direction).isEmpty();
    }

    public int size(
            EdgeDirection direction
    ) {
        return chromosomePaths.get(direction).size();
    }

    public List<Chromosome> getChromosomePaths(
            EdgeDirection direction
    ) {
        return chromosomePaths.get(direction);
    }
}
