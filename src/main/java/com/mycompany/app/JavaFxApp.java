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
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.BasemapStyle;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApp extends Application {

    private ConfigurableApplicationContext applicationContext;
    private MapView mapView;

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

        // set the title and size of the stage and show it
        stage.setTitle("My Map App");
        stage.setWidth(800);
        stage.setHeight(700);
        stage.show();

        GraphicsOverlay graphicsOverlay = new GraphicsOverlay();

//text box
//        VBox vbCenter = new VBox(); // use any container as center pane e.g. VBox
//        TextField console = new TextField();
//        vbCenter.getChildren().add(console);

        FlowPane hbButtons = new FlowPane();
        hbButtons.setHgap(10);
        hbButtons.setOpacity(0.9);

        Button scrapeBtn = new Button();
        scrapeBtn.setText("Очистити");
        scrapeBtn.setOnAction(event -> {
            graphicsOverlay.getGraphics().clear();
        });

        Button addBtn = new Button();
        addBtn.setText("Відобразити");
        addBtn.setLineSpacing(10);
        addBtn.setOnAction(event -> {
            createPoint(graphicsOverlay, 49.644502, 32.340027);
        });

        hbButtons.getChildren().add(scrapeBtn);
        hbButtons.getChildren().add(addBtn);
        hbButtons.setAlignment(Pos.CENTER_RIGHT);

        // root
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20)); // space between elements and window border
//        root.setCenter(vbCenter);
        root.setBottom(hbButtons);

        // create a JavaFX scene with a stack pane as the root node and add it to the scene
        StackPane stackPane = new StackPane();
        Scene scene = new Scene(stackPane);
        stage.setScene(scene);

        // Note: it is not best practice to store API keys in source code.
        // An API key is required to enable access to services, web maps, and web scenes hosted in ArcGIS Online.
        // If you haven't already, go to your developer dashboard to get your API key.
        // Please refer to https://developers.arcgis.com/java/get-started/ for more information
        String yourApiKey = "AAPK3739c9917e3741e28bdab4ab08217e9aDCbMsy88u2T6TfSAoFi_pDEm1UrIIREFqB7-R_55eraFYi3Ycq8Ld-w43Iz3kYnp";
        ArcGISRuntimeEnvironment.setApiKey(yourApiKey);

        // create a MapView to display the map and add it to the stack pane
        mapView = new MapView();
        stackPane.getChildren().add(mapView);
        stackPane.getChildren().add(root);
        // create an ArcGISMap with an imagery basemap
        ArcGISMap map = new ArcGISMap(BasemapStyle.ARCGIS_IMAGERY);

        // display the map by setting the map on the map view
        mapView.setMap(map);
        //Ukraine viewpoint
        mapView.setViewpoint(new Viewpoint(48.644502, 31.240027, 10000000));

        // create a graphics overlay and add it to the map view
        mapView.getGraphicsOverlays().add(graphicsOverlay);

        // create a point geometry with a location and spatial reference

        //set points
    }

    private static void createPoint(GraphicsOverlay graphicsOverlay, Double lat, Double lon) {
        Point point = new Point(lon, lat, SpatialReferences.getWgs84());
        SimpleMarkerSymbol simpleMarkerSymbol =
                new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, 0xFFFF5733, 10);
        SimpleLineSymbol blueOutlineSymbol =
                new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, 0xFF0063FF, 2);
        simpleMarkerSymbol.setOutline(blueOutlineSymbol);
        Graphic pointGraphic = new Graphic(point, simpleMarkerSymbol);
        graphicsOverlay.getGraphics().add(pointGraphic);
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
