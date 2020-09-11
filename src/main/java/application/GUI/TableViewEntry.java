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
package  main.java.application.GUI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TableViewEntry {

    private final StringProperty species = new SimpleStringProperty(this, "species");
    private final StringProperty chromosome = new SimpleStringProperty(this, "chromosome");

    public TableViewEntry(String species, String chromsome) {
        this.species.set(species);
        this.chromosome.set(chromsome);
    }

    public final StringProperty speciesProperty() {
        return this.species;
    }

    public final StringProperty chromosomeProperty() {
        return this.chromosome;
    }

    public String getSpecies() {
        return species.get();
    }

    public String getChromosome() {
        return chromosome.get();
    }
}
