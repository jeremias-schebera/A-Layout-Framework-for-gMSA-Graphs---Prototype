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

import main.java.application.Algorithmen.*;
import main.java.application.Controller.MainController;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import main.java.application.data.Configuration;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class MainWindowController
    implements Initializable {

    private MainController parent;

    private DrawingWindowController drawingWindowController;
    private ControlWindowController controlWindowController;

    private GraphProjectionSugiyama sugiyamaProjection;

    //Test!!!
//    private String csvFile;
//    private FileWriter writer;
    //Test!!!

    @FXML
    private VBox vbox;

    private AnchorPane rootPane;

    public void setPane(
        AnchorPane anchorPane
    ) {
        this.rootPane = anchorPane;
    }

    public Parent getRoot() {
        return rootPane;
    }

    public static MainWindowController getInstance() {
        FXMLLoader loader = new FXMLLoader();
        try {
            // Load root layout from fxml file
            loader.setLocation(MainWindowController.class.getResource("MainWindow.fxml"));
            AnchorPane rootPane = (AnchorPane) loader.load();
            MainWindowController mainWindowController = loader.<MainWindowController>getController();
            mainWindowController.setPane(rootPane);
            return mainWindowController;
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
            return null;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void init(
        MainController parent
    ) {
        this.parent = parent;
        drawingWindowController = DrawingWindowController.getInstance();
        drawingWindowController.initializeDrawGroup();
        Parent drawingRoot = drawingWindowController.getRoot();
        vbox.getChildren().add(drawingRoot);

        controlWindowController = ControlWindowController.getInstance();

        //Test!!!
//        controlWindowController.initCsvText();
        //Test!!!

        Parent controlRoot = controlWindowController.getRoot();
        vbox.getChildren().add(controlRoot);
        controlWindowController.setParent(this);

        //Test!!!
//        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//        csvFile = "/home/jeremias/Schreibtisch/log" + timestamp + ".csv";
//        try {
//            writer = new FileWriter(csvFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //Test!!!
    }

    // Commit DB and close Connection
    public void stop() {
        //Test!!!
//        StringBuffer sb = sugiyamaProjection.getCSVLine();
//        try {
//            writer.append(sb);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            writer.flush();
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //Test!!!
        controlWindowController.stop();
    }

    public void startSugiyama(
        Configuration configuration,
        Set<Vertex> chromosomesToDraw
    ) {
        long timeStart = System.currentTimeMillis();

        sugiyamaProjection = new GraphProjectionSugiyama(configuration);
        sugiyamaProjection.computeLayout(configuration);
        drawSugiyamaFramework(sugiyamaProjection, chromosomesToDraw);

        long timeEnd = System.currentTimeMillis();
        //TEST!!!
//        StringBuffer sb = sugiyamaProjection.getCSVLine();
//        sb.append((timeEnd - timeStart) + "\n");
        //TEST!!!
    }

    public void drawSugiyamaFramework(
        GraphProjectionSugiyama sugiyamaProjection,
        Set<Vertex> chromosomesToDraw
    ) {
        drawingWindowController.drawSugiyamaFramework(sugiyamaProjection);
    }

    public void reDrawSugiyamaFramework(
            Set<Vertex> chromosomesToDraw
    ) {
        drawingWindowController.reDrawSugiyamaFramework(chromosomesToDraw);
    }

    public void snapshot() {
        drawingWindowController.captureAndSaveDisplay();
    }
}
