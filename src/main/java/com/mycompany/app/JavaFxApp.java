/**
 * Copyright 2019 Esri
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.mycompany.app;

import com.esri.arcgisruntime.ArcGISRuntimeEnvironment;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.mycompany.app.controller.ReportEnvelop;
import com.mycompany.app.controller.ReportEnvelopItem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JavaFxApp extends Application {

    private ConfigurableApplicationContext applicationContext;
    private MapView mapView;
    private static ReportEnvelop points;
    private static BasemapStyle currentMapLook;
    private static Viewpoint viewpoint = new Viewpoint(48.944502, 31.240027, 10000000*0.8);
    private static ComboBox<String> combobox;
    private static ReportEnvelop result;
    private static CheckBox drawPoligon = new CheckBox("Полігональне відображення");
    private static TextField searchTextField;

    public void setEnvelope(ReportEnvelop reportEnvelop) {
        points = reportEnvelop;
    }

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        this.applicationContext = new SpringApplicationBuilder()
                .sources(Main.class)
                .run(args);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setTitle("Emergency Bot Map viewer");
        stage.centerOnScreen();
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setFullScreen(true);
        stage.show();

        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

        FlowPane hbButtons = new FlowPane();
        setupButtonsRow(graphicsOverlay, hbButtons);

        // root
        BorderPane buttonBorderPaneRoot = new BorderPane();
        buttonBorderPaneRoot.setPadding(new Insets(10)); // space between elements and window border
        buttonBorderPaneRoot.setBottom(hbButtons);
        buttonBorderPaneRoot.setStyle("-fx-background-color: rgba(167,168,171,0.96); ");

        // create a JavaFX scene with a stack pane as the root node and add it to the scene
        VBox stackPane = new VBox();
        stackPane.setAlignment(Pos.BOTTOM_CENTER);
        Scene scene = new Scene(stackPane);
        stage.setScene(scene);
        scene.getStylesheets().add("stylesheet.css");

        // Note: it is not best practice to store API keys in source code.
        // An API key is required to enable access to services, web maps, and web scenes hosted in ArcGIS Online.
        // If you haven't already, go to your developer dashboard to get your API key.
        // Please refer to https://developers.arcgis.com/java/get-started/ for more information
        String yourApiKey = "AAPK3739c9917e3741e28bdab4ab08217e9aDCbMsy88u2T6TfSAoFi_pDEm1UrIIREFqB7-R_55eraFYi3Ycq8Ld-w43Iz3kYnp";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();
        stackPane.getChildren().add(mapView);
        stackPane.getChildren().add(buttonBorderPaneRoot);
//        stackPane.getChildren().add(rootPane);

        // create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_DARK_GRAY);
        currentMapLook = BasemapStyle.ARCGIS_DARK_GRAY;

        // display the map by setting the map on the map view
        mapView.setMap(map);
//        mapView.setMinHeight(600);
//        mapView.setX(bounds.getMinX());
//        mapView.setY(bounds.getMinY());
        mapView.setMinWidth(bounds.getWidth());
        mapView.setMinHeight(bounds.getHeight());
        //Ukraine viewpoint
        mapView.setViewpoint(viewpoint);

        // create a graphics overlay and add it to the map view
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        refreshAction(graphicsOverlay);

        // create a point geometry with a location and spatial reference

        //set points
    }

    private void setupButtonsRow(GraphicsOverlay graphicsOverlay, FlowPane hbButtons) {
        hbButtons.setHgap(10);
        hbButtons.setOpacity(0.9);
        hbButtons.setMaxHeight(1);
        hbButtons.setAlignment(Pos.CENTER_RIGHT);

        searchTextField = new TextField();
        searchTextField.setMinWidth(400);
        searchTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {


            }
        });

        Button scrapeBtn = new Button();
        scrapeBtn.setText("Очистити");
        scrapeBtn.setOnAction(event -> {
            graphicsOverlay.getGraphics().clear();
        });

        Button refreshBtn = new Button();
        refreshBtn.setText("Оновити");
        refreshBtn.setLineSpacing(10);
        refreshBtn.setOnAction(event -> refreshAction(graphicsOverlay));

        Button changeMapLookBtn = new Button();
        changeMapLookBtn.setText("Вигляд");
        changeMapLookBtn.setOnAction(event -> changeMapViewAction());

        Button resetLocationBtn = new Button();
        resetLocationBtn.setText("Центрувати");
        resetLocationBtn.setOnAction(event -> mapView.setViewpoint(viewpoint));

        ObservableList<String> options =
                FXCollections.observableArrayList(
                        "Інформація",
                        "Низький",
                        "Середній",
                        "Високий",
                        "Максимальний"
                );
        combobox = new ComboBox<>(options);
        combobox.setValue("Інформація");
        combobox.getSelectionModel().selectedItemProperty().addListener((optionsVal, oldValue, selectedDngrLvl) -> {
            filterAndDrawPoints(graphicsOverlay, result, selectedDngrLvl);
        });

        hbButtons.getChildren().add(searchTextField);
        hbButtons.getChildren().add(drawPoligon);
        hbButtons.getChildren().add(scrapeBtn);
        hbButtons.getChildren().add(refreshBtn);
        hbButtons.getChildren().add(changeMapLookBtn);
        hbButtons.getChildren().add(resetLocationBtn);
        hbButtons.getChildren().add(combobox);
    }

    private void changeMapViewAction() {
        List<BasemapStyle> values = Arrays.asList(BasemapStyle.values());
        int currIndex = values.indexOf(currentMapLook);
        if (currIndex + 1 > values.size()) {
            currIndex = -1;
        }
        BasemapStyle basemapStyleToSet = values.get(currIndex + 1);
        System.out.println(basemapStyleToSet);
        ArcGISMap map = new ArcGISMap(basemapStyleToSet);
        map.setInitialViewpoint(viewpoint);
        // display the map by setting the map on the map view
        mapView.getMap().setBasemap(new Basemap(basemapStyleToSet));
        currentMapLook = basemapStyleToSet;
    }

    private static void refreshAction(GraphicsOverlay graphicsOverlay) {
        final String uri = "http://localhost:8081/integration/map-btn-trigger";
        RestTemplate restTemplate = new RestTemplate();
        result = restTemplate.getForObject(uri, ReportEnvelop.class);
        assert result != null;
        filterAndDrawPoints(graphicsOverlay, result, null);
    }

    private static void filterAndDrawPoints(GraphicsOverlay graphicsOverlay, ReportEnvelop result, String selectedDngrLvl) {
        List<ReportEnvelopItem> filterPoints = filterPoints(result, selectedDngrLvl);
        if (!filterPoints.isEmpty()) {
            graphicsOverlay.getGraphics().clear();
        }
        List<ReportEnvelopItem> collect = filterPoints.stream().sorted(Comparator.comparingDouble(ReportEnvelopItem::getLat)).collect(Collectors.toList());
        PointCollection polygonPoints = new PointCollection(SpatialReferences.getWgs84());
        for (ReportEnvelopItem item : collect) {
            polygonPoints.add(item.getLon(), item.getLat());
            createPoint(graphicsOverlay, item.getLat(), item.getLon(), item.getDangerLevel());
        }
        if (drawPoligon.isSelected()) {
            Polygon polygon = new Polygon(polygonPoints);
            SimpleFillSymbol polygonFillSymbol =
                    new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, 0x80FF5733, null);
            Graphic polygonGraphic = new Graphic(polygon, polygonFillSymbol);
            // add the polygon graphic to the graphics overlay
            graphicsOverlay.getGraphics().add(polygonGraphic);
        }
    }

    private static List<ReportEnvelopItem> filterPoints(ReportEnvelop result, String filterParam) {
        if (filterParam != null) {
            String accessibleText = String.valueOf(combobox.getValue());
            String dangerLevelText = textToDangerLevelString(accessibleText);
            return result.getItems().stream()
                    .filter(reportEnvelopItem -> reportEnvelopItem.getDangerLevel().equals(dangerLevelText))
                    .collect(Collectors.toList());
        }
        return result.getItems();
    }

    public static void createPoint(GraphicsOverlay graphicsOverlay, Double lat, Double lon, String dangerLevel) {
        Point point = new Point(lon, lat, SpatialReferences.getWgs84());
        SimpleMarkerSymbol simpleMarkerSymbol =
                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, pickColour(dangerLevel), 10);
        Graphic pointGraphic = new Graphic(point, simpleMarkerSymbol);
        graphicsOverlay.getGraphics().add(pointGraphic);
    }

    public static String textToDangerLevelString(String dangerLevel) {
        switch (dangerLevel) {
            case "Інформація":
                return "INFO";
            case "Низький":
                return "LOW";
            case "Середній":
                return "MEDIUM";
            case "Високий":
                return "HIGH";
            case "Максимальний":
                return "EXTREME";
        }
        return "MEDIUM";
    }

    private static int pickColour(String dangerLevel) {
        switch (dangerLevel) {
            case "INFO":
                return 0xfff4fff2;
            case "LOW":
                return 0xffa4ff96;
            case "MEDIUM":
                return 0xffebff30;
            case "HIGH":
                return 0xffff400d;
            case "EXTREME":
                return 0xfff000db;
        }
        return 0xFFFF5733;
    }

    /**
     * Stops and releases all resources used in application.
     */
    @Override
    public void stop() {
        this.applicationContext.close();
        if (mapView != null) {
            mapView.dispose();
        }
        Platform.exit();
    }
}
