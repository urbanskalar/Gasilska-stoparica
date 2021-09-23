/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stopwatch;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
/**
 * FXML Controller class
 *
 * @author urban
 */
public class SettingsController implements Initializable {


    /**
     * Initializes the controller class.
     */
    
    // Creating class instances
    MainFXMLController mainController = new MainFXMLController();
    StopwatchPreferences Preferences = new StopwatchPreferences();
    
    // Access to preferences
    //Preferences preferences = Preferences.userRoot().node("StopwatchAppSettings");
    
    // Get preferences (key, default value if no other saved)
    String SerialPort = Preferences.preferences.get("SerialPort", null);
    String Baudrate = Preferences.preferences.get("Baudrate", "115200");
    Integer StartDelay = Preferences.preferences.getInt("StartDelay", 0) / 1000;
    String PathToAudioFile = Preferences.preferences.get("PathToAudioFile", "Path to audio file");
    Integer AudioOffset = Preferences.preferences.getInt("AudioOffset", 0);
    
    
    @FXML
    private JFXComboBox<String> SerialPortBox;
    @FXML
    private JFXComboBox<String> BaudrateBox;
    @FXML
    private JFXTextField StartDelayField;
    @FXML
    private JFXTextField PathToAudioTxtFld;
    @FXML
    private JFXTextField AudioOffsetTime;
    
    @FXML
    private AnchorPane ap;
    
    
            
    @FXML
    private void handleSaveButton(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {  
        
        // Save preferences before chancging stage
        if(SerialPortBox.getValue() != null){
            Preferences.preferences.put("SerialPort", SerialPortBox.getValue());
        }
        if(BaudrateBox.getValue() != null){
            Preferences.preferences.put("Baudrate", BaudrateBox.getValue());
        }
        if(StartDelayField.getText().length() != 0){
            Preferences.preferences.putInt("StartDelay", 1000*Integer.parseInt(StartDelayField.getText()));
        }
        if(PathToAudioTxtFld.getText().length() != 0){
            Preferences.preferences.put("PathToAudioFile", PathToAudioTxtFld.getText());
        }
        if(AudioOffsetTime.getText().length() != 0){
            Preferences.preferences.put("AudioOffset", AudioOffsetTime.getText());
        }
        
        
        ap.getScene().getWindow().hide();
    }
    
    @FXML
    private void handleChooseAudioBtn(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {  
    
        FileChooser fileChooser = new FileChooser();
        
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("mp3 files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);
    
        //Set initial directory
        fileChooser.setInitialDirectory(new File ("./"));
        
        // Choose file
        File selectedFile = fileChooser.showOpenDialog(null);
        
        if (selectedFile != null){
            PathToAudioTxtFld.setText(selectedFile.getPath());
        }

    }
    

    private ObservableList<String> options;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // Populate GUI with initial values
        options = mainController.searchForPorts();
        SerialPortBox.getItems().addAll(options);
        SerialPortBox.getSelectionModel().select(SerialPort);
        BaudrateBox.getSelectionModel().select(Baudrate);
        StartDelayField.setText(StartDelay.toString());
        PathToAudioTxtFld.setText(PathToAudioFile);
        AudioOffsetTime.setText(AudioOffset.toString());
        
        // Detect enter press on text fields and change focus
        PathToAudioTxtFld.setOnAction((e) -> ap.requestFocus());
        StartDelayField.setOnAction((e) -> ap.requestFocus());
        AudioOffsetTime.setOnAction((e) -> ap.requestFocus());
        
        // Refreshing port values on combobox showing event
        // Removing old values from combo box
        // Adding nw values to combo box
        SerialPortBox.setOnShowing(event -> {
            options = mainController.searchForPorts();
            
            // Adding new
            for (int ind = 0; ind <= options.size()-1; ind++){
                if (!SerialPortBox.getItems().contains(options.get(ind))){
                    SerialPortBox.getItems().add(options.get(ind));
                }
            }
            // Removing old
            int BoxSize = SerialPortBox.getItems().size();
            for (int ind = 0; ind <= BoxSize-1; ind++){
                ObservableList<String> tmpList = SerialPortBox.getItems();
                if (!options.contains(tmpList.get(ind))){
                    SerialPortBox.getItems().remove(ind);
                    ind--;
                    BoxSize--;
                }
            }
        });
        
        
        
        // Add baudrates to combo box
        ObservableList<String> baudrates = FXCollections.observableArrayList(
            "9600",
            "19200",
            "38400",
            "57600",
            "74880",
            "115200",
            "230400",
            "250000",
            "500000",
            "1000000",
            "2000000"
        );
        BaudrateBox.getItems().addAll(baudrates);
        
        

        
    }    
    
    public void refresh(){
        
        SerialPort = Preferences.preferences.get("SerialPort", null);
        Baudrate = Preferences.preferences.get("Baudrate", "115200");
        StartDelay = Preferences.preferences.getInt("StartDelay", 0) / 1000;
        PathToAudioFile = Preferences.preferences.get("PathToAudioFile", null);
        AudioOffset = Preferences.preferences.getInt("AudioOffset", 0);
        
        SerialPortBox.getSelectionModel().select(SerialPort);
        BaudrateBox.getSelectionModel().select(Baudrate);
        StartDelayField.setText(StartDelay.toString());
        PathToAudioTxtFld.setText(PathToAudioFile);
        AudioOffsetTime.setText(AudioOffset.toString());
    }
    
    


    
}
