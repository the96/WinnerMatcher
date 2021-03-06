package PuyoPuyoTetrisAutoCounter;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    Label p1NameLabel;
    @FXML
    Label p2NameLabel;
    @FXML
    Button openButton;
    @FXML
    Button setPlayer1;
    @FXML
    Button setPlayer2;
    @FXML
    Button previewButton;
    @FXML
    Button okButton;
    @FXML
    CheckBox engFlag;
    @FXML
    CheckBox fitFlag;
    @FXML
    CheckBox setCountFlag;
    @FXML
    TextField p1NameField;
    @FXML
    TextField p2NameField;
    @FXML
    TextField setCountNumber;

    SelectArea selectArea;
    Rectangle p1Area, p2Area;
    Stage stage;
    Stage previewStage;
    Stage countViewStage;
    PreviewWindow previewWindow;
    CountView countView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        previewStage = new Stage();
        previewStage.initModality(Modality.NONE);
        previewStage.initOwner(stage);
        previewStage.setOnCloseRequest(e -> {
            previewWindow.closePreview();
        });
        countViewStage = new Stage();
        countViewStage.initModality(Modality.NONE);
        countViewStage.initOwner(stage);
        countViewStage.setOnCloseRequest(e -> {
            countView.stopThread();
        });
        p1Area = new Rectangle(0, 0, 0, 0);
        p2Area = new Rectangle(0, 0, 0, 0);
        p1NameField.setText(p1NameLabel.getText());
        p2NameField.setText(p2NameLabel.getText());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(1);
        });
    }

    private static boolean isValidArea(Rectangle area) {
        return area.x != 0 && area.y != 0 && area.width != 0 && area.height != 0;
    }

    public static void showAlertInvalidArea(boolean isValidP1, boolean isValidP2) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        alert.setTitle("Error: invalid area");
        alert.getDialogPane().setContentText("Invalid area:" + (isValidP1 ? "" : " P1") + (isValidP2 ? "" : " P2") + "\r\n"
                + "Please Set Area.");
        alert.showAndWait();
    }
    public static void showAlertNotNumber() {
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        alert.setTitle("Error: invalid input");
        alert.getDialogPane().setContentText("Please input number.");
        alert.showAndWait();
    }
    public static boolean isValidNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            showAlertNotNumber();
        }
        return false;
    }

    public static void rewriteLabel(Label label, TextField field) {
        label.setText(field.getText());
        field.setVisible(false);
        label.setVisible(true);
    }

    public static boolean rewriteLabelNumber(Label label,TextField field) {
        if (Controller.isValidNumber(field.getText())) {
            rewriteLabel(label, field);
            return true;
        }
        return false;
    }

    public static void inputNewText(Label label, TextField field) {
        field.setText(label.getText());
        label.setVisible(false);
        field.setVisible(true);
    }
    public boolean readySelect() {
        return selectArea != null;
    }

    public boolean readyArea() {
        return isValidArea(p1Area) && isValidArea(p2Area);
    }

    @FXML
    public void openSelectArea() {
        if (!readySelect()) {
            try {
                selectArea = new SelectArea();
                selectArea.setAutoFitFlag(fitFlag.isSelected());
            } catch (AWTException e) {
                e.printStackTrace();
            }
        } else if (!selectArea.isVisible()) {
            selectArea.reopenWindow();
            selectArea.setAutoFitFlag(fitFlag.isSelected());
        }
    }

    @FXML
    public void changeFitFlag() {
        if (readySelect()) {
            selectArea.setAutoFitFlag(fitFlag.isSelected());
        }
    }

    @FXML
    public void changeEngFlag() {
        if (null != countView) {
            countView.setEngFlag(engFlag.isSelected());
        }
    }

    @FXML
    public void openPreview() {
        if (!(previewWindow == null) && previewWindow.isRunning()) {
            previewWindow.closePreview();
            previewStage.hide();
            return;
        }
        if (!readyArea()) {
            Controller.showAlertInvalidArea(isValidArea(p1Area), isValidArea(p2Area));
            return;
        }
        completeSetArea();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("preview.fxml"));
            SplitPane splitPane = loader.load();
            previewWindow = loader.getController();
            previewWindow.setArea(p1Area, p2Area);
            previewStage.setScene(new Scene(splitPane));
            previewStage.show();
            if (previewWindow.startCapture()) {
                System.out.println("running preview");
            } else {
                System.out.println("failed to run preview");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void countStart() {
        if (!readyArea()) {
            showAlertInvalidArea(isValidArea(p1Area), isValidArea(p2Area));
            return;
        }
        if (!isValidNumber(setCountNumber.getText())) return;
        completeSetArea();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("countView.fxml"));
            GridPane gridPane = loader.load();
            Scene scene = new Scene(gridPane);
            countView = loader.getController();
            countViewStage.setScene(scene);
            countView.setGridPane(gridPane);
            countView.setP1Name(this.getPlayer1Name());
            countView.setP2Name(this.getPlayer2Name());
            countViewStage.show();
            countView.startCount(p1Area, p2Area, engFlag.isSelected());
            countView.setP1Name(getPlayer1Name());
            countView.setP2Name(getPlayer2Name());
            countView.setNum(Integer.parseInt(setCountNumber.getText()));
            countView.setCountVisible(setCountFlag.isSelected());
            countViewStage.sizeToScene();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void setPlayer1Area() {
        if (readySelect()) {
            p1Area = selectArea.getArea();
        } else {
            openSelectArea();
        }
    }

    @FXML
    public void setPlayer2Area() {
        if (readySelect()) {
            p2Area = selectArea.getArea();
        } else {
            openSelectArea();
        }
    }

    public String getPlayer1Name() {
        return p1NameField.getText();
    }

    public String getPlayer2Name() {
        return p2NameField.getText();
    }
    @FXML
    public void completeSetArea() {
        selectArea.closeWindow();
    }

    @FXML
    public void renamePlayer1() {
        Controller.inputNewText(p1NameLabel,p1NameField);
    }

    @FXML
    public void renamePlayer2() {
        Controller.inputNewText(p2NameLabel,p2NameField);
    }

    @FXML
    public void renamedPlayer1() {
        Controller.rewriteLabel(p1NameLabel,p1NameField);
    }

    @FXML
    public void renamedPlayer2() {
        Controller.rewriteLabel(p2NameLabel,p2NameField);
    }

    @FXML
    public void isValidSetCountField(){
        Controller.isValidNumber(setCountNumber.getText());
    }

}