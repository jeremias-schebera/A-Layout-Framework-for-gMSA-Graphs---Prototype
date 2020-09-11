package main.java.application.GUI;

import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import main.java.application.Controller.MainController;
import main.java.application.data.*;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.*;
import javafx.util.Pair;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

public class ControlWindowController
    implements Initializable {

    /** nur zu Testzwecken*/
    //Für Dirk
//    private final static String DB_PATH = "/datadisk/zeckzer/Forschung/Projekte/Leipzig/IVDA/Schebera-Jeremias/SuperGenomeBrowser/graph.db";
    //Für Jeremias
//    private final static String DB_PATH = "/home/jeremias/Schreibtisch/Fabian_data/new DB/graph.db";

    private static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    private List<TableViewEntry> selections = new ArrayList<>();

    private AutoScrollableTableThread autoScrollThread = null;

    @FXML
    private Label lblRange;
    @FXML
    private ComboBox comboBoxSpecies;
    @FXML
    private ComboBox comboBoxStructure;
    @FXML
    private TextField txtFldStart;
    @FXML
    private TextField txtFldEnd;
    @FXML
    private Button btnPaintGraph;
    @FXML
    private Button btnLoadGuideSequence;
    @FXML
    private TreeView treeViewOtherChromosomesAndSpecies;
    @FXML
    private TreeView treeViewDrawedChromosomes;
    @FXML
    private TableView tableOtherChromosomeOrder;
    @FXML
    private CheckBox joinCheckBox;
    @FXML
    private TextField txtFldMinLength;

    @FXML
    private Slider sliderSpaceFactor;
    @FXML
    private Label labelSpaceFactor;
    @FXML
    private Slider sliderThicknessFactor;
    @FXML
    private Label labelThicknessFactor;
    @FXML
    private Slider sliderRangeVertexWidth;

    @FXML
    private Button btnSnapshot;

    private MainWindowController parent;

    private Graph graphDB;
    private GraphTraversalSource graphTraversal;
    private Vertex guideStructure;
    private Vertex guideSpecies;
    private int minPositionOnChromosome;
    private int maxPositionOnChromosome;
    private javafx.scene.shape.Rectangle selectionRectangle = new javafx.scene.shape.Rectangle();
    boolean circleIsHovered = false;
    private Map<String, Vertex> speciesVertexAssociation;
    private Map<String, Vertex> chromosomesVertexAssociation;
    private Map<String, Vertex> compareableChromosomesVertexAssociation;
    private List<Vertex> guidePath;
    private SequenceVerticesData sequenceVerticesData;
    private Map<String, TableViewEntry> selectedCompareOptions;
    private List<Rectangle> lastSelectedRectangle = new ArrayList<>();
//    private Group vertexTextGroup;
    private Set<Vertex> chromosomesToDraw;
    private AlignmentBlockAssociation alignmentBlockAssociation;
//    private final int spaceFactor = 2;

    //Test!!!
//    private StringBuffer csvText;
    //Test!!!

    private VBox rootPane;

    public void setPane(
        VBox anchorPane
    ) {
        this.rootPane = anchorPane;
    }

    public Parent getRoot() {
        return rootPane;
    }

    public static ControlWindowController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            // Load root layout from fxml file
            loader.setLocation(ControlWindowController.class.getResource("ControlWindow.fxml"));
            VBox rootPane = (VBox) loader.load();
            ControlWindowController mainWindowController = loader.<ControlWindowController>getController();
            mainWindowController.setPane(rootPane);
            return mainWindowController;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return null;
        }
    }

    public void initCsvText() {
        //Test!!!
//        csvText = new StringBuffer();
//        csvText.append("GS-Species;GS-Structure;GS-Start-Pos.;GS-End-Pos.;Length-Filter;Order-Comparative-Sequences;Join;#Genomes;#Vertices-Original;#Edge-Forward-DAG;#Edge-Backward-DAG;#Edge-DAG;#Edge-Forward;#Edge-Backward;#Edge;#ByPaths;Time-Cycle-Removal;#Layer;#Long-Span-Edges-DAG;#Dummy-Vertices;#Blocks;#Vertices-All;#Edges-New-DAG;Time-Layering;#BlockSets;Time-Vertex-Ordering;Max-Special-Case-Shift-Steps;Time-Vertical-Coordinate-Assignment;#Edge-Dif.-Preprocessing-DAG;#Edge-New-DAG2;#Vertex-New;Time-Preprocessing;#Edge-Final;Time-Edge-Routing;Time-Drawing;Time-Overall\n");
        //Test!!!
    }

    public boolean isJoinCheckBoxSelected() {
        return joinCheckBox.isSelected();
    }

    public void setParent(MainWindowController parent) {
        this.parent = parent;
    }

    public void clearLastSelectedCircle() {
        lastSelectedRectangle.clear();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initStatus();

        //Change-Listener if (another) species in the ComboBox of Species is selcted --> fill Chromosome ComboBox
        //2. Step
        comboBoxSpecies.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) -> {
                comboBoxSpeciesSelected(newValue);
            });

        //Change-Listener if (another) chromosome in ComboBox of Chromosomes is selected
        //3. Step
        comboBoxStructure.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observableValue, Number oldNumber, Number newNumber) -> {
                comboBoxStructureSelected(newNumber);
            });

        //Change-Listener if text in Start range textfield is changed
        //4. Step
        txtFldStart.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldString, String newString) -> {
            textChanged();
        });

        //Change-Listener if text in End range textfield is changed
        //also 4. Step
        txtFldEnd.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldString, String newString) -> {
            textChanged();
        });

        //Change-Listener if text in min block length textfield is changed
        //also 4. Step
        txtFldMinLength.textProperty().addListener((ObservableValue<? extends String> observableValue, String oldString, String newString) -> {
            textChanged();
        });

        //Change-Listener if entries changed in TableView
        //
        tableOtherChromosomeOrder.getItems().addListener(new ListChangeListener<TableViewEntry>() {
            @Override
            public void onChanged(javafx.collections.ListChangeListener.Change<? extends TableViewEntry> pChange) {
                tableOtherChromosomeOrderChanged(pChange);
            }
        });

        treeViewOtherChromosomesAndSpecies.getSelectionModel().selectedIndexProperty().addListener(
            (ObservableValue<? extends Number> observableValue, Number number, Number number2) -> {
                if (number2.intValue() == 0) {
                    tableOtherChromosomeOrder.setDisable(true);
                }
            });
    }

    private void comboBoxSpeciesSelected(Number newValue) {
        if ((int) newValue != -1) {
            comboBoxStructure.getSelectionModel().clearSelection();
            guideSpecies = speciesVertexAssociation.get(comboBoxSpecies.getItems().get((Integer) newValue).toString());
            getChromosomes();
            ObservableList<String> chromosomeNames = FXCollections.observableArrayList(chromosomesVertexAssociation.keySet());
            if (chromosomeNames.size() > 0) {
                comboBoxStructure.setDisable(false);
                comboBoxStructure.setItems(chromosomeNames);
            }
        } else {
            comboBoxStructure.setItems(FXCollections.observableArrayList());
            comboBoxStructure.setDisable(true);
        }
    }

    private void comboBoxStructureSelected(Number newNumber) {
        if ((int) newNumber != -1) {
            guideStructure = chromosomesVertexAssociation.get(comboBoxStructure.getItems().get((Integer) newNumber).toString());
            getChromosomeBoundaries();
            lblRange.setText("Gib einen Bereich zwischen " + minPositionOnChromosome + " und " + maxPositionOnChromosome + " an");
            txtFldStart.setDisable(false);
            txtFldEnd.setDisable(false);
            txtFldMinLength.setDisable(false);
        } else {
            txtFldStart.setDisable(true);
            txtFldEnd.setDisable(true);
            txtFldStart.setText("");
            txtFldEnd.setText("");
            txtFldMinLength.setDisable(true);
            txtFldMinLength.setText("");
        }
    }

    private void tableOtherChromosomeOrderChanged(ListChangeListener.Change<? extends TableViewEntry> pChange) {
        System.out.println(pChange.getList().size());
        while (pChange.next()) {
            // Do your changes here
            if (tableOtherChromosomeOrder.getItems().size() > 0) {
                btnPaintGraph.setDisable(false);
                joinCheckBox.setDisable(false);
            } else {
                btnPaintGraph.setDisable(true);
                joinCheckBox.setDisable(true);
            }
        }
    }

    //Set the elements to the initial status --> disable and clear them
    //0. Step
    private void initStatus() {
        comboBoxSpecies.setItems(FXCollections.observableArrayList());
        comboBoxSpecies.setDisable(true);
        comboBoxStructure.setItems(FXCollections.observableArrayList());
        comboBoxStructure.setDisable(true);
        txtFldEnd.setDisable(true);
        txtFldStart.setDisable(true);
        txtFldMinLength.setDisable(true);
        lblRange.setText("");
        btnLoadGuideSequence.setDisable(true);
        btnPaintGraph.setDisable(true);
        joinCheckBox.setDisable(true);

        labelSpaceFactor.setText("Space: " + sliderSpaceFactor.getValue());
        labelThicknessFactor.setText("Thickness: " + sliderThicknessFactor.getValue());

        treeViewDrawedChromosomes.setCellFactory(CheckBoxTreeCell.<Layer>forTreeView());
        treeViewDrawedChromosomes.setDisable(true);
        treeViewOtherChromosomesAndSpecies.setCellFactory(CheckBoxTreeCell.<Layer>forTreeView());
        treeViewOtherChromosomesAndSpecies.setDisable(true);
        tableOtherChromosomeOrder.setDisable(true);

        if (tableOtherChromosomeOrder.getColumns().isEmpty()) {
            tableOtherChromosomeOrder.getColumns().add(createCol("Genome", TableViewEntry::speciesProperty, 150));
            tableOtherChromosomeOrder.getColumns().add(createCol("Sub-Structure", TableViewEntry::chromosomeProperty, 150));
            tableOtherChromosomeOrder.getColumns().add(createCol("Mark", TableViewEntry::chromosomeProperty, 50));
            tableOtherChromosomeOrder.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }

        // !!!Test!!!
//        loadFileChooser();
//        txtFldStart.setText("7744");
//        txtFldEnd.setText("9984");
//        txtFldMinLength.setText("20");
        // !!!Test!!!
    }

    //Create new column for a TableView
    private TableColumn<TableViewEntry, String> createCol(
        String title,
        Function<TableViewEntry, ObservableValue<String>> mapper,
        double size
    ) {
        TableColumn<TableViewEntry, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> mapper.apply(cellData.getValue()));
        col.setPrefWidth(size);

        return col;
    }

    //Klick on Open Button --> to Load Graph DB
    //1. Step
    @FXML
    private void loadFileChooser() {
        //it is allready a graphDB-DB connected --> loading new graphDB-DB
        //close existing connection and set initial status
        stop();
//        if (graphDB != null) {
//            initStatus();
//        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Neo4j Database");
        File file = directoryChooser.showDialog(MainController.getPrimaryStage());
        if (file != null){
            String db_path = file.getPath();
            graphDB = Neo4jGraph.open(db_path);
////        directoryChooser.setInitialDirectory(new File("/home/jeremias/Schreibtisch/Fabian_data/new DB"));
//        String dbpath = directoryChooser.showDialog(MainController.getPrimaryStage()).getPath();
            //!!!Testzwecke!!!
//        graphDB = Neo4jGraph.open(DB_PATH);
            //!!!Testzwecke!!!
            graphTraversal = graphDB.traversal();

            getSpecies();
            ObservableList<String> speciesNames = FXCollections.observableArrayList(speciesVertexAssociation.keySet());
            if (speciesNames.size() > 0) {
                comboBoxSpecies.setDisable(false);
                comboBoxSpecies.setItems(speciesNames);
            }
        } else {
            System.out.println("No File selected");
        }
    }

    //Query to get all Species-Vertices from graph DB
    //Part of 1. Step
    private void getSpecies() {
        System.out.println("Species:");
        speciesVertexAssociation = new HashMap<>();
        GraphTraversal<Vertex, Map<String, Object>> traversalSpecies = graphTraversal.V().hasLabel("Species"
        ).project(
            "vertex", "name"
        ).by(__.identity()
        ).by(__.values("name"));
        while (traversalSpecies.hasNext()) {
            Map<String, Object> specie = traversalSpecies.next();
            speciesVertexAssociation.put(specie.get("name").toString(), (Vertex) specie.get("vertex"));
            System.out.println(specie.get("name").toString());
        }
    }

    //Query to get all Chromosome-Vertices from graph DB
    //Part of 2. Step
    private void getChromosomes() {
        chromosomesVertexAssociation = new HashMap<>();
        GraphTraversal<Vertex, Map<String, Object>> traversalChromsomes = graphTraversal.V(guideSpecies
        ).out("hasChr"
        ).project("vertex", "name"
        ).by(__.identity()
        ).by(__.values("name"));
        while (traversalChromsomes.hasNext()) {
            Map<String, Object> chromosome = traversalChromsomes.next();
            chromosomesVertexAssociation.put(chromosome.get("name").toString(), (Vertex) chromosome.get("vertex"));
        }
    }

    //Query to get the min- and max-position in chromosome from GraphDB
    //also 3. Step
    private void getChromosomeBoundaries() {

        Map<String, Object> range = graphTraversal.V(guideStructure).as("chr").in("isOn").not(__.in("nextSeq")).until(__.has("throw", false)).repeat(__.out("nextSeq")).values("start").as("min_pos").select("chr").in("isOn").not(__.out("nextSeq")).until(__.has("throw", false)).repeat(__.in("nextSeq")).project("start", "length").by("start").by("length").math("start + length - 1").as("max_pos").select("min_pos", "max_pos").next();
        minPositionOnChromosome = (Integer) range.get("min_pos");
        maxPositionOnChromosome = ((Double) range.get("max_pos")).intValue();
    }

    // Evaluation if the range positions (start & end) in the textFields are inside the chromosome positions from GraphDB --> if yes enable Button
    //Part of 4. Step
    private void textChanged() {
        try {
            int rangeStartPosition = Integer.parseInt(txtFldStart.getText());
            int rangeEndPosition = Integer.parseInt(txtFldEnd.getText());
            int minBlockLength = Integer.valueOf(txtFldMinLength.getText());
            // are start and end inside the chromosome and is start smaller then end and is minBlockLength greater zero --> if this is the case activate the button
            if ((rangeStartPosition >= minPositionOnChromosome && rangeStartPosition < maxPositionOnChromosome) && (rangeEndPosition >= minPositionOnChromosome && rangeEndPosition <= maxPositionOnChromosome) && (rangeStartPosition < rangeEndPosition) && (minBlockLength > 0)) {
                btnLoadGuideSequence.setDisable(false);
            } else {
                btnLoadGuideSequence.setDisable(true);
            }
        } catch (NumberFormatException e) {
            //Not an integer
            btnLoadGuideSequence.setDisable(true);
        }
    }

    //Klick on Load Guide Sequence Button --> to query the Guide Sequence from the GraphDB
    //5. Step
    @FXML
    private void loadGuideSequence() {
        //preparation
        tableOtherChromosomeOrder.getItems().clear();
        tableOtherChromosomeOrder.setDisable(true);
        guidePath = new ArrayList<>();
        alignmentBlockAssociation = new AlignmentBlockAssociation();

        //query
        int start_range = Integer.parseInt(txtFldStart.getText());
        int end_range = Integer.parseInt(txtFldEnd.getText());
        GraphTraversal<Vertex, Map<String, Object>> traversalChromsomes = graphTraversal.V(guideStructure
        ).in("isOn").has("throw", false
        ).has("start", P.lte(end_range)
        ).where(
                __.project("start", "length"
                ).by("start"
                ).by("length"
                ).math("start + length - 1"
                ).is(P.gte(start_range))
        ).order().by("start"
        ).in("hasSeq").in("containsBlock"
        ).project("vertex", "block"
        ).by(__.identity()
        ).by(__.out("containsBlock"));
        while (traversalChromsomes.hasNext()) {
            Map<String, Object> entry = traversalChromsomes.next();
            Vertex vertex = (Vertex) entry.get("vertex");
            Vertex alignmentBlock = (Vertex) entry.get("block");
            if ((Integer) alignmentBlock.value("length") >= Integer.valueOf(txtFldMinLength.getText())) {
                guidePath.add(vertex);
                alignmentBlockAssociation.add(vertex, alignmentBlock);
            }
        }

        if (guidePath.size() > 0) {
            treeViewOtherChromosomesAndSpecies.setDisable(false);
            getComparableChromosomesAndSpecies();
        }

        sliderSpaceFactor.valueProperty().addListener((obs, oldval, newVal)
                -> sliderSpaceFactor.setValue((int) Math.round(newVal.doubleValue())));

        sliderThicknessFactor.valueProperty().addListener((obs, oldval, newVal)
                -> sliderThicknessFactor.setValue((int) Math.round(newVal.doubleValue())));

        sliderSpaceFactor.valueProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) -> {
            labelSpaceFactor.setText("Space: " + sliderSpaceFactor.getValue());
        });

        sliderThicknessFactor.valueProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) -> {
            labelThicknessFactor.setText("Thickness: " + sliderThicknessFactor.getValue());
        });
    }

    //Fill the TreeView with the compareable Chromosomes
    //Part of 5. Step
    private void getComparableChromosomesAndSpecies() {
        CheckBoxTreeItem<String> root = new CheckBoxTreeItem("All");
        Map<String, Integer> speciesindex = new HashMap<>();
        compareableChromosomesVertexAssociation = new HashMap<>();

        //Super Genome --> is always compareable
        //query the comparable Chromosomes from graphDB
        GraphTraversal<Vertex, Vertex> traversalSuperGenome = graphTraversal.V().hasLabel("Order");
        //fill TreeView with Super Genome Orders
        while (traversalSuperGenome.hasNext()) {
            Vertex superGenomeVertex = traversalSuperGenome.next();
            String superGenomeText = "Super Genome " + superGenomeVertex.value("id").toString();
            CheckBoxTreeItem<String> treeItemSuperGenome = new CheckBoxTreeItem<>(superGenomeText);
            root.getChildren().add(treeItemSuperGenome);
            compareableChromosomesVertexAssociation.put(superGenomeText, superGenomeVertex);
            speciesindex.put(superGenomeText, speciesindex.keySet().size());
        }

        //query the comparable Chromosomes from graphDB
        GraphTraversal<Vertex, Map<String, Object>> traversalChromosomes = graphTraversal.V(guidePath
        ).out("containsBlock").out("hasSeq").out("isOn").as("chromosome"
        ).in("hasChr").as("species"
        ).select("species", "chromosome").by("name").by(__.identity()
        ).dedup();

        //fill TreeView with other Chromosomes
        while (traversalChromosomes.hasNext()) {
            Map nextEntry = traversalChromosomes.next();
            Vertex chromosomeVertex = (Vertex) nextEntry.get("chromosome");
            String speciesName = nextEntry.get("species").toString();
            if (!guideStructure.equals(chromosomeVertex)) {
                if (speciesindex.containsKey(speciesName)) {
                    int position = speciesindex.get(speciesName);
                    root.getChildren().get(position).getChildren().add(new CheckBoxTreeItem<>(chromosomeVertex.value("name").toString()));
                    compareableChromosomesVertexAssociation.put(chromosomeVertex.value("name").toString(), chromosomeVertex);
                } else {
                    CheckBoxTreeItem<String> treeItem = new CheckBoxTreeItem<>(speciesName);
                    treeItem.getChildren().add(new CheckBoxTreeItem<>(chromosomeVertex.value("name").toString()));
                    speciesindex.put(speciesName, speciesindex.keySet().size());
                    root.getChildren().add(treeItem);
                    compareableChromosomesVertexAssociation.put(chromosomeVertex.value("name").toString(), chromosomeVertex);
                }
            }
        }

        treeViewOtherChromosomesAndSpecies.setRoot(root);
        selectedCompareOptions = new HashMap<>();

        //EventHandler --> TreeView something is ticked --> fill TableView
        //6. Step
        treeViewOtherChromosomesAndSpecies.getRoot().addEventHandler(
            CheckBoxTreeItem.checkBoxSelectionChangedEvent(),
            new EventHandler<CheckBoxTreeItem.TreeModificationEvent<String>>() {
            @Override
            public void handle(CheckBoxTreeItem.TreeModificationEvent<String> evt) {
                treeViewOtherChromosomesAndSpeciesHandleEvent(evt);
            }
        });

        //Drag and Drop mechanism of TabelView --> TO-DO: überarbeiten oder ersetzen
        //geklaut aus Internet
        tableOtherChromosomeOrder.setRowFactory(tv -> {
            TableRow<TableViewEntry> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                rowSetOnDragDetected(event, row);
            });

            row.setOnDragOver(event -> {
                rowSetOnDragOver(event, row);
            });

            row.setOnDragDropped(event -> {
                rowSetOnDragDropped(event, row);
            });

            return row;
        });

        tableOtherChromosomeOrder.addEventFilter(
            DragEvent.DRAG_DROPPED,
            event -> {
                chromosomeOrderDragDropped();
            });

        tableOtherChromosomeOrder.addEventFilter(
            DragEvent.DRAG_OVER,
            event -> {
                chromosomeOrderDragOver(event);
            });
    }

    private void chromosomeOrderDragDropped() {
        if (autoScrollThread != null) {
            autoScrollThread.stopScrolling();
            autoScrollThread = null;
        }
    }

    private void chromosomeOrderDragOver(DragEvent event) {
        double proximity = 100;

        Bounds tableBounds = tableOtherChromosomeOrder.getLayoutBounds();

        double dragY = event.getY();

        //System.out.println(tableBounds.getMinY() + " --> " + tableBounds.getMaxY() + " --> " + dragY);
        // Area At Top Of Table View. i.e Initiate Upwards Auto Scroll If
        // We Detect Anything Being Dragged Above This Line.
        double topYProximity = tableBounds.getMinY() + proximity;

        // Area At Bottom Of Table View. i.e Initiate Downwards Auto Scroll If
        // We Detect Anything Being Dragged Below This Line.
        double bottomYProximity = tableBounds.getMaxY() - proximity;

        // We Now Make Use Of A Thread To Scroll The Table Up Or Down If
        // The Objects Being Dragged Are Within The Upper Or Lower
        // Proximity Areas
        if (dragY < topYProximity) {
            // We Need To Scroll Up
            if (autoScrollThread == null) {
                autoScrollThread = new AutoScrollableTableThread(tableOtherChromosomeOrder);
                autoScrollThread.scrollUp();
                autoScrollThread.start();
            }

        } else if (dragY > bottomYProximity) {
            // We Need To Scroll Down
            if (autoScrollThread == null) {
                autoScrollThread = new AutoScrollableTableThread(tableOtherChromosomeOrder);
                autoScrollThread.scrollDown();
                autoScrollThread.start();
            }

        } else {
            // No Auto Scroll Required We Are Within Bounds
            chromosomeOrderDragDropped();
        }
    }

    private void rowSetOnDragDropped(
        DragEvent event,
        TableRow<TableViewEntry> row
    ) {
        Dragboard db = event.getDragboard();

        if (db.hasContent(SERIALIZED_MIME_TYPE)) {

            int dropIndex;
            TableViewEntry dI = null;

            if (row.isEmpty()) {
                dropIndex = tableOtherChromosomeOrder.getItems().size();
            } else {
                dropIndex = row.getIndex();
                dI = (TableViewEntry) tableOtherChromosomeOrder.getItems().get(dropIndex);
            }
            int delta = 0;
            if (dI != null) {
                while (selections.contains(dI)) {
                    delta = 1;
                    --dropIndex;
                    if (dropIndex < 0) {
                        dI = null;
                        dropIndex = 0;
                        break;
                    }
                    dI = (TableViewEntry) tableOtherChromosomeOrder.getItems().get(dropIndex);
                }
            }

            for (TableViewEntry sI : selections) {
                tableOtherChromosomeOrder.getItems().remove(sI);
            }

            if (dI != null) {
                dropIndex = tableOtherChromosomeOrder.getItems().indexOf(dI) + delta;
            } else if (dropIndex != 0) {
                dropIndex = tableOtherChromosomeOrder.getItems().size();
            }

            tableOtherChromosomeOrder.getSelectionModel().clearSelection();

            for (TableViewEntry sI : selections) {
                //draggedIndex = selections.get(i);
                tableOtherChromosomeOrder.getItems().add(dropIndex, sI);
                tableOtherChromosomeOrder.getSelectionModel().select(dropIndex);
                dropIndex++;

            }

            event.setDropCompleted(true);
            selections.clear();
            event.consume();
        }
    }

    private void rowSetOnDragOver(
        DragEvent event,
        TableRow<TableViewEntry> row
    ) {
        Dragboard db = event.getDragboard();
        if (db.hasContent(SERIALIZED_MIME_TYPE)) {
            if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                event.consume();
            }
        }
    }

    private void rowSetOnDragDetected(
        MouseEvent event,
        TableRow<TableViewEntry> row
    ) {
        if (!row.isEmpty()) {
            Integer index = row.getIndex();

            selections.clear();//important...

            ObservableList<TableViewEntry> items = tableOtherChromosomeOrder.getSelectionModel().getSelectedItems();

            for (TableViewEntry iI : items) {
                selections.add(iI);
            }

            Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
            db.setDragView(row.snapshot(null, null));
            ClipboardContent cc = new ClipboardContent();
            cc.put(SERIALIZED_MIME_TYPE, index);
            db.setContent(cc);
            event.consume();
        }
    }

    private void treeViewOtherChromosomesAndSpeciesHandleEvent(CheckBoxTreeItem.TreeModificationEvent<String> evt) {
        CheckBoxTreeItem<String> item = evt.getTreeItem();

        if (evt.wasIndeterminateChanged()) {
            if (item.isLeaf()) {
                if (item.isIndeterminate()) {
                    String text = item.getValue() + " (" + item.getParent().getValue() + ")";
                    TableViewEntry entry = selectedCompareOptions.get(text);
                    tableOtherChromosomeOrder.getItems().remove(entry);
                    selectedCompareOptions.remove(text);
                } else if (evt.getTreeItem().isSelected()) {
                    String text = item.getValue() + " (" + item.getParent().getValue() + ")";
                    TableViewEntry entry = new TableViewEntry(item.getParent().getValue(), item.getValue());
                    selectedCompareOptions.put(text, entry);
                    tableOtherChromosomeOrder.getItems().add(entry);

                }
            }
        } else if (evt.wasSelectionChanged()) {
            if (item.isLeaf()) {
                if (item.isSelected()) {
                    String text = item.getValue() + " (" + item.getParent().getValue() + ")";
                    TableViewEntry entry = new TableViewEntry(item.getParent().getValue(), item.getValue());
                    selectedCompareOptions.put(text, entry);
                    tableOtherChromosomeOrder.getItems().add(entry);

                } else {
                    String text = item.getValue() + " (" + item.getParent().getValue() + ")";
                    TableViewEntry entry = selectedCompareOptions.get(text);
                    tableOtherChromosomeOrder.getItems().remove(entry);
                    selectedCompareOptions.remove(text);
                }
            }
        }

        if (!selectedCompareOptions.isEmpty()) {
            tableOtherChromosomeOrder.setDisable(false);
        } else {
            tableOtherChromosomeOrder.setDisable(true);
        }
    }

    //Klick on Paint Graph Button --> to query the compareable Chromosome Sequences from the GraphDB
    //7. Step
    @FXML
    private void paintGraph() {
        sequenceVerticesData = new SequenceVerticesData();
        Pair pair = new Pair(guideStructure, guidePath);
        sequenceVerticesData.add(pair);

//        Chromosome guideChromosomePath = new Chromosome(guideChromosome, guidePath, freeColor.getFreeColor());
//        sequenceVerticesData.add(guideChromosomePath);
        for (Object entryObject : tableOtherChromosomeOrder.getItems()) {
            TableViewEntry entry = (TableViewEntry) entryObject;

            GraphTraversal<Vertex, Map<String, Object>> traversalSequencesVertices = null;
            Vertex otherChromosomeVertex = compareableChromosomesVertexAssociation.get(entry.getChromosome());

            if (entry.getChromosome().startsWith("Super Genome")) {
                //TO-DO mehrere Super Genome Vertices einbauen!!!!
                traversalSequencesVertices = graphTraversal.V(guidePath).project(
                    "super_g_pos", "guide_vertices"
                ).by(
                    __.inE("next").values("pos")
                ).by(
                    __.identity()
                ).fold().sideEffect(
                    __.unfold().select("super_g_pos").min().store("min_pos")
                ).sideEffect(
                    __.unfold().select("super_g_pos").max().store("max_pos")
                ).as("startset").unfold().where(
                    "super_g_pos", P.within("max_pos")
                ).select("guide_vertices").as("end_vertex").select("startset").unfold().where(
                    "super_g_pos", P.within("min_pos")
                ).select("guide_vertices").as("start_vertex").<Vertex>select("start_vertex").emit().until(
                    __.where(P.eq("end_vertex"))
                ).repeat(
                    __.out("next")
                ).project("vertex", "block"
                ).by(__.identity()
                ).by(__.out("containsBlock"));
            } else {
                traversalSequencesVertices = graphTraversal.V(guidePath).where(
                    __.out("containsBlock").out("hasSeq").out("isOn").is(otherChromosomeVertex)
                ).choose(
                    __.in("graphedge").where(__.out("val").is(otherChromosomeVertex)),
                    __.project("genome_pos", "guide_vertices").by(__.in("graphedge").where(__.out("val").is(otherChromosomeVertex)).values("position")).by(__.identity()),
                    __.project("genome_pos", "guide_vertices").by(__.constant(-1)).by(__.identity())
                ).fold().sideEffect(
                    __.unfold().select("genome_pos").min().store("min_pos")
                ).sideEffect(
                    __.unfold().select("genome_pos").max().store("max_pos")
                ).as("startset").unfold().where("genome_pos", P.within("max_pos")).select("guide_vertices").as("end_vertex"
                ).select("startset").unfold().where("genome_pos", P.within("min_pos")).select("guide_vertices").as("start_vertex"
                ).<Vertex>select("start_vertex").emit().until(
                    __.where(P.eq("end_vertex"))
                ).repeat(
                    __.out("graphedge").where(__.out("val").is(otherChromosomeVertex)).out("graphedge")
                ).project("vertex", "block"
                ).by(__.identity()
                ).by(__.out("containsBlock"));
            }

            List<Vertex> sequence = null;
            if (traversalSequencesVertices.hasNext()) {
                sequence = new ArrayList<>();
            }
            while (traversalSequencesVertices.hasNext()) {
                Map<String, Object> mapEntry = traversalSequencesVertices.next();
                Vertex vertex = (Vertex) mapEntry.get("vertex");
                Vertex alignmentBlock = (Vertex) mapEntry.get("block");
                if ((Integer) alignmentBlock.value("length") >= Integer.valueOf(txtFldMinLength.getText())) {
                    alignmentBlockAssociation.add(vertex, alignmentBlock);
                    sequence.add(vertex);
                }
            }
            if (!sequence.isEmpty()) {
                pair = new Pair(otherChromosomeVertex, sequence);
                sequenceVerticesData.add(pair);
            }
        }

        chromosomesToDraw = new HashSet<>();
        CheckBoxTreeItem<String> newRoot = new CheckBoxTreeItem("All");
        CheckBoxTreeItem newItem = new CheckBoxTreeItem(guideStructure.value("name") + " (Guide Sequence)");
        newRoot.getChildren().add(newItem);
        isChecked((CheckBoxTreeItem<String>) treeViewOtherChromosomesAndSpecies.getRoot(), newRoot);
        treeViewDrawedChromosomes.setRoot(newRoot);
        treeViewDrawedChromosomes.setDisable(false);
        newRoot.setSelected(false);

        //EventHandler --> TreeView something is ticked --> change Drawing
        treeViewDrawedChromosomes.getRoot().addEventHandler(
            CheckBoxTreeItem.checkBoxSelectionChangedEvent(),
            new EventHandler<CheckBoxTreeItem.TreeModificationEvent<String>>() {

            @Override
            public void handle(CheckBoxTreeItem.TreeModificationEvent<String> evt) {

                CheckBoxTreeItem<String> item = evt.getTreeItem();
                if (!item.equals(newRoot)) {
                    if (evt.wasIndeterminateChanged()) {
                        if (item.isLeaf()) {
//                            System.out.println("indeterminate");
                            if (item.isIndeterminate()) {
                                String chromosomeText = item.getValue().split("\\(")[0];
                                chromosomeText = chromosomeText.substring(0, chromosomeText.length() - 1);
//                                System.out.println("remove:" + chromosomeText);
                                Vertex chromosomeVertex;
                                if (chromosomeText.equals(guideStructure.value("name").toString())) {
                                    chromosomeVertex = guideStructure;
                                } else {
                                    chromosomeVertex = compareableChromosomesVertexAssociation.get(chromosomeText);
                                }
                                chromosomesToDraw.remove(chromosomeVertex);
                            } else if (evt.getTreeItem().isSelected()) {
                                String chromosomeText = item.getValue().split("\\(")[0];
                                chromosomeText = chromosomeText.substring(0, chromosomeText.length() - 1);
//                                System.out.println("add:" + chromosomeText);
                                Vertex chromosomeVertex;
                                if (chromosomeText.equals(guideStructure.value("name").toString())) {
                                    chromosomeVertex = guideStructure;
                                } else {
                                    chromosomeVertex = compareableChromosomesVertexAssociation.get(chromosomeText);
                                }
                                chromosomesToDraw.add(chromosomeVertex);
                            }
                        }
                    } else if (evt.wasSelectionChanged()) {
                        if (item.isLeaf()) {
//                            System.out.println("determinate");
                            if (item.isSelected()) {
                                String chromosomeText = item.getValue().split("\\(")[0];
                                chromosomeText = chromosomeText.substring(0, chromosomeText.length() - 1);
//                                System.out.println("add:" + chromosomeText);
                                Vertex chromosomeVertex;
                                if (chromosomeText.equals(guideStructure.value("name").toString())) {
                                    chromosomeVertex = guideStructure;
                                } else {
                                    chromosomeVertex = compareableChromosomesVertexAssociation.get(chromosomeText);
                                }
                                chromosomesToDraw.add(chromosomeVertex);
                            } else {
                                String chromosomeText = item.getValue().split("\\(")[0];
                                chromosomeText = chromosomeText.substring(0, chromosomeText.length() - 1);
//                                System.out.println("remove:" + chromosomeText);
                                Vertex chromosomeVertex;
                                if (chromosomeText.equals(guideStructure.value("name").toString())) {
                                    chromosomeVertex = guideStructure;
                                } else {
                                    chromosomeVertex = compareableChromosomesVertexAssociation.get(chromosomeText);
                                }
                                chromosomesToDraw.remove(chromosomeVertex);
                            }
                        }
                    }
                }

                parent.reDrawSugiyamaFramework(chromosomesToDraw);
            }
        });

        //starting the Sugiyama Algorithm
        Configuration configuration = new Configuration();
        //Test!!!
//        configuration.setCsvText(csvText);
//        //Configuration in CSV
//        //GS-Species;GS-Structure;GS-Start-Pos.;GS-End-Pos.;Length-Filter;Order-Comparative-Sequences;Join
//        StringBuffer compSeqOrder = new StringBuffer();
//        for (Object entryObject : tableOtherChromosomeOrder.getItems()) {
//            TableViewEntry entry = (TableViewEntry) entryObject;
//            compSeqOrder.append(entry.getChromosome() + "(" + entry.getSpecies() + "),");
//        }
//        compSeqOrder.setLength(compSeqOrder.length() - 1); //delete last Comma
//        csvText.append(guideSpecies.value("name").toString() + ";" + guideStructure.value("name").toString() + ";" + txtFldStart.getText() + ";" + txtFldEnd.getText() + ";" + txtFldMinLength.getText() + ";" + compSeqOrder + ";" + joinCheckBox.isSelected() + ";");
        //Test!!!
        configuration.setSequenceVerticesData(sequenceVerticesData);
        configuration.setAlignmentBlockAssociation(alignmentBlockAssociation);
        configuration.setIsJoinEnabled(joinCheckBox.isSelected());
        configuration.setDrawingThicknessFactor(sliderThicknessFactor.getValue());
        configuration.setSpaceFactor((int) sliderSpaceFactor.getValue());
        parent.startSugiyama(configuration, chromosomesToDraw);
    }

    @FXML
    private void snapshot() {
        parent.snapshot();
    }

    private void isChecked(CheckBoxTreeItem<String> item, TreeItem<String> newRoot) {
        if (item.isLeaf() && item.isSelected()) {
            CheckBoxTreeItem newItem = new CheckBoxTreeItem(item.getValue() + " (" + item.getParent().getValue() + ")");
            newRoot.getChildren().add(newItem);
//            chromosomesToDraw.add(compareableChromosomesVertexAssociation.get(item.getValue()));
        } else {
            for (TreeItem<String> child : item.getChildren()) {
                isChecked((CheckBoxTreeItem<String>) child, newRoot);
            }
        }
    }

    // Commit DB and close Connection
    public void stop() {
        System.out.println("Close connection");
        if (graphDB != null) {
            try {
                graphDB.tx().commit();
            } catch (UnsupportedOperationException e) {
            }
            try {
                graphDB.close();
            } catch (Exception e1) {
                e1.printStackTrace();
                System.exit(1);
            }
            initStatus();
        }
        graphDB = null;
    }

//    private void relocateText(javafx.geometry.Point2D cicleCenter, Circle circle) {
//        Text text = circle.getAssociatedText();
//        double width = text.getBoundsInLocal().getWidth();
//        double height = text.getBoundsInLocal().getHeight();
//        text.relocate(cicleCenter.getX() - (0.5 * width), cicleCenter.getY() - (0.5 * height));
//        text.toFront();
//    }
//
//    public void dragCircle(double radius, MouseEvent event, Circle circle) {
//        double offsetX = event.getSceneX() - circle.getOrgSceneX();
//        double offsetY = event.getSceneY() - circle.getOrgSceneY();
//
////        ScrollPane scrollPane = graphProjection.GUI().getScrollPane();
////        System.out.println("Y: " + (scrollPane.getVvalue() + offsetY) + " = " + scrollPane.getVvalue() + " + " + offsetY);
////        System.out.println("X: " + (scrollPane.getHvalue() + offsetX) + " = " + scrollPane.getHvalue() + " + " + offsetX);
////        scrollPane.setVvalue(scrollPane.getVvalue() + offsetY);
////        scrollPane.setHvalue(scrollPane.getHvalue() + offsetX);
////
////        Point2D currentMousePosition = new Point2D(event.getSceneX(), event.getSceneY());
////        System.out.println("mousePosition: " + event.getSceneX() + "; " + event.getSceneY());
////        System.out.println("bounds: " + scrollPane.getWidth() + "; " + scrollPane.getHeight());
////        if (currentMousePosition.getX() > scrollPane.getWidth() || currentMousePosition.getY() > scrollPane.getHeight()) {
////            System.out.println("offset: " + offsetX + ", " + offsetY);
//////            try {
////                javafx.scene.robot.Robot robot = new Robot();
////
////                robot.mouseMove(0, 0);
////                robot.mouseMove((int) (robot.getMouseX()), (int) (robot.getMouseY() - offsetY));
//////            } catch (AWTException e) {
//////                e.printStackTrace();
//////            }
////            System.out.println("after: " + event.getSceneX() + "; " + event.getSceneY());
////        }
//
//        double newX = circle.getCenterX() + offsetX;
//        if(newX - radius < 0) {
//            newX = radius;
//        }
//        circle.setCenterX(newX);
//
//        double newY = circle.getCenterY() + offsetY;
//        if(newY -radius < 0) {
//            newY = radius;
//        }
//        circle.setCenterY(newY);
//
//        if (!circle.getAssociatedVertex().isDummyNode()) {
//            relocateText(new javafx.geometry.Point2D(newX, newY), circle);
//        }
//
//        circle.setOrgSceneX(event.getSceneX());
//        circle.setOrgSceneY(event.getSceneY());
//
//        for (EdgeSugiyama edge : circle.getAssociatedVertex().getInEdge()) {
//            drawEdge(edge);
//        }
//
//        for (EdgeSugiyama edge : circle.getAssociatedVertex().getOutEdge()) {
//            drawEdge(edge);
//        }
//    }
//
//    public void onMousePressed(MouseEvent event, Circle circle) {
//        circle.setOrgSceneX(event.getSceneX());
//        circle.setOrgSceneY(event.getSceneY());
//        circle.getVertexTextGroup().toFront();
//    }
//
//    public void mark(boolean selection, Circle circleMark) {
//        if (!circleMark.getAssociatedVertex().isDummyNode()) {
//            circleMark.getAssociatedText().toFront();
//        }
//
//        if (selection == false) {
//            if (lastSelectedCircle.size() > 0) {
//                for (Circle circle : lastSelectedCircle) {
//                    circle.setFill(circle.getColor());
//                    circle.setStroke(circle.getColor());
//                }
//            }
//            lastSelectedCircle.clear();
//        }
//
//        circleMark.setStroke(Color.rgb(100,100,100));
//        circleMark.setFill(Color.rgb(100,100,100));
//        System.out.println(circleMark.getAssociatedVertex().getId());
//        lastSelectedCircle.add(circleMark);
//
//        selectionActive = selection;
//    }
}

class AutoScrollableTableThread extends Thread {

    enum ScrollMode {
        UP, DOWN, NONE
    }

    private boolean running = true;
    private ScrollMode scrollMode = ScrollMode.NONE;
    private ScrollBar verticalScrollBar = null;

    public AutoScrollableTableThread(TableView tableView) {
        super();
        setDaemon(true);
        verticalScrollBar = (ScrollBar) tableView.lookup(".scroll-bar:vertical");

    }

    @Override
    public void run() {

        try {
            Thread.sleep(300);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        while (running) {

            Platform.runLater(() -> {
                if (verticalScrollBar != null && scrollMode == ScrollMode.UP) {
                    verticalScrollBar.setValue(verticalScrollBar.getValue() - 0.01);
                } else if (verticalScrollBar != null && scrollMode == ScrollMode.DOWN) {
                    verticalScrollBar.setValue(verticalScrollBar.getValue() + 0.01);
                }
            });

            try {
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void scrollUp() {
        System.out.println("Start To Scroll Up");
        scrollMode = ScrollMode.UP;
        running = true;
    }

    public void scrollDown() {
        System.out.println("Start To Scroll Down");
        scrollMode = ScrollMode.DOWN;
        running = true;
    }

    public void stopScrolling() {
        System.out.println("Stop Scrolling");
        running = false;
        scrollMode = ScrollMode.NONE;
    }
}
