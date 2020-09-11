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
