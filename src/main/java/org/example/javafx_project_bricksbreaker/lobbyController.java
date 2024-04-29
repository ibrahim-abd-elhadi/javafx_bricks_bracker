package org.example.javafx_project_bricksbreaker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.example.javafx_project_bricksbreaker.GamePaneController;

import java.io.IOException;

public class lobbyController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private Button switchButton;

    @FXML
    void switch_scene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GamePane.fxml"));
            root = loader.load();

            // Access the controller of GamePane.fxml if needed
            // GamePaneController gamePaneController = loader.getController();

            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
