package me.flickersoul.dawn.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import me.flickersoul.dawn.functions.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.lang.Thread.sleep;

public class MaterialEditorWindowController{
    @FXML
    TextArea material_name_text_area;
    @FXML
    TextArea material_description_text_area;
    @FXML
    Button submit_button;
    @FXML
    Button cancel_button;
    @FXML
    Text changeable_text;

    private final String SUBMIT_MATERIAL_INFO_SQL = "UPDATE total_material_index SET material_name=?, description=? WHERE id=?";
    private int id;
    private Stage stage;

    public MaterialEditorWindowController setMaterialName(String name, boolean editable){
        material_name_text_area.setText(name);
        material_name_text_area.setDisable(!editable);

        return this;
    }

    public MaterialEditorWindowController setMaterialDescription(String description, boolean editable){
        material_description_text_area.setText(description);
        material_description_text_area.setDisable(!editable);

        return this;
    }

    public MaterialEditorWindowController setPopup(Stage stage){
        this.stage = stage;
        return this;
    }

    public MaterialEditorWindowController setSubmitButtonVisible(boolean sign){
        submit_button.setVisible(sign);

        return this;
    }

    public void setId(int id){
        this.id = id;
    }

    public void submitAction(){
        //TODO 同时更新 WordMember
        try(Connection connection = DBConnection.establishMaterialRepoConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(SUBMIT_MATERIAL_INFO_SQL)) {
            preparedStatement.setString(1, material_name_text_area.getText());
            preparedStatement.setString(2, material_description_text_area.getText());
            preparedStatement.setInt(3, id);

            preparedStatement.executeUpdate();

            changeable_text.setVisible(true);
            System.out.println("Update Successfully!");

            sleep(300);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stage.hide();
    }

    public void cancelAction(){
        stage.hide();
    }

    public void deleteAction(){

    }
}