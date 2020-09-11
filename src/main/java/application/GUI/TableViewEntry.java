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
