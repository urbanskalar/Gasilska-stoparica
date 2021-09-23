/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stopwatch;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import java.io.BufferedWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import jssc.SerialPort;
import static jssc.SerialPort.MASK_RXCHAR;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

/**
 *
 * @author urban
 */
public class MainFXMLController implements Initializable {
    
    @FXML
    private Label Time;
    
    @FXML
    public Label StatusMessages;
    
    @FXML
    public Label Intermediate;
    
    @FXML
    public JFXTextField MistakePoints;
    
    @FXML
    private JFXButton StartStopResetButton;
    
    @FXML
    private TableView<Results> table;
    
    
    private final ObservableList<Results> data = FXCollections.observableArrayList();

    // Needed for loading of settings stage
    FXMLLoader loader;
    Parent root2;
    Stage stage2;
    SettingsController setController;
    
    static String IntermediateTime = "";
    
    private static Timeline RunningTimeline;
    private static int sequence = 0;
    private static long timeBegan = 0;
    private static long timeStopped = 0;
    private static long currentTime;
    private static long timeElapsed; 
    
    String Baudrate;
    String SerialPort = null;
    Integer StartDelay;
    String PathToAudioFile;
    Integer AudioOffset;
    
    private Long AudioDuration;
   
    AudioFormat decodedFormat;
    AudioInputStream in;
    AudioInputStream din = null;
    
    // Get system line separator
    public static String newLine = System.getProperty("line.separator");
    
    // Creating class instance
    StopwatchPreferences Preferences = new StopwatchPreferences();
    
    public String RandomColor;
    
    public boolean ConnectionEstablished = false;
    
    @FXML
    private void handleButtonAction(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {       
        
        StartStopReset();
    }
    
    public void StartStopReset(){
        
        // Execute function corresponding to sequence number
        switch (sequence){
            case 0: CountDownToStart();
                    break;
            case 2: Stop("GUI");
                    break;
            case 3: Reset();
                    break;
                    
        }
    }
    
    @FXML
    private void handleSettings(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {  
        
        try
        {
            Scene scene2 = new Scene(root2);
            // set css style
            scene2.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
            root2.setStyle("-fx-custom-color:" + RandomColor + ";");
            
            stage2.setScene(scene2); 
            stage2.setTitle("Settings");
            stage2.showAndWait();
        }
        catch(IllegalArgumentException ex)
        {
            stage2.show();
            setController.refresh();
        }
            
    }
    
    @FXML
    private void handleDelete(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {  
        
        // TODO: delete selected entry
        ObservableList<Results> SelectedResults, allResults;
        allResults = table.getItems();
        SelectedResults = table.getSelectionModel().getSelectedItems();
        allResults.removeAll(SelectedResults);
    }
    
    @FXML
    private void handleSave(ActionEvent event) throws UnsupportedAudioFileException, IOException, LineUnavailableException {  
        SaveFile();
        
    }

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // init from preferences
        PreferencesInit();
        
        // Set as default button (Enter can toggle this button)
        // StartStopResetButton.setDefaultButton(true);
        
        // Init visibility
        MistakePoints.setDisable(true);
        MistakePoints.setOpacity(0.5);
        
        // Detect enter press and change focus
        MistakePoints.setOnAction((e) -> {
            Stopwatch.root.requestFocus();
            Reset();
        });
        
        // Loading settings stage
        try 
        {
            loader = new FXMLLoader();
            loader.setLocation(Stopwatch.class.getResource("Settings.fxml"));
            root2 = loader.load();
            setController = (SettingsController) loader.getController();
            stage2 = new Stage();
            stage2.setResizable(false);
        
            
        } 
        catch (IOException e) 
        {
            System.out.println(e);
            StatusMessages.setText("Exception catched: " + e);
        }
        
        stage2.setOnHiding( event -> {
            // Read new preferences and start serial communication
            PreferencesInit();
        } );
        
        
        // Table initialization
        table.setEditable(true);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Callback<TableColumn<Results, String>, TableCell<Results, String>> cellFactory
                = (TableColumn<Results, String> param) -> new EditingCell();

        TableColumn<Results, String> EndTimeCol = new TableColumn<>("Končni čas");
        EndTimeCol.setSortable(false);
        EndTimeCol.setResizable(false);
        EndTimeCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
        EndTimeCol.setCellValueFactory(cellData -> cellData.getValue().EndTimeProperty());
        EndTimeCol.setCellFactory(cellFactory);
        EndTimeCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Results, String> t) -> {
                    ((Results) t.getTableView().getItems()
                    .get(t.getTablePosition().getRow()))
                    .setEndTime(t.getNewValue());

                });
        
        TableColumn<Results, String> InterTimeCol = new TableColumn<>("Vmesni časi (+)");
        InterTimeCol.setSortable(false);
        InterTimeCol.setResizable(false);
        InterTimeCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
        InterTimeCol.setCellValueFactory(cellData -> cellData.getValue().InterTimeProperty());
        InterTimeCol.setCellFactory(cellFactory);
        InterTimeCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Results, String> t) -> {
                    ((Results) t.getTableView().getItems()
                    .get(t.getTablePosition().getRow()))
                    .setInterTime(t.getNewValue());

                });

        TableColumn<Results, String> BlackPointsCol = new TableColumn<>("Kazenske");
        BlackPointsCol.setSortable(false);
        BlackPointsCol.setResizable(false);
        BlackPointsCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
        BlackPointsCol.setCellValueFactory(cellData -> cellData.getValue().BlackPointsProperty());
        BlackPointsCol.setCellFactory(cellFactory);
        BlackPointsCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Results, String> t) -> {
                    ((Results) t.getTableView().getItems()
                    .get(t.getTablePosition().getRow()))
                    .setBlackPoints(t.getNewValue());

                });
        
        TableColumn<Results, String> SumCol = new TableColumn<>("Skupaj");
        SumCol.setSortable(false);
        SumCol.setResizable(false);
        SumCol.prefWidthProperty().bind(table.widthProperty().divide(4)); // w * 1/4
        SumCol.setCellValueFactory(cellData -> cellData.getValue().SumProperty());
        SumCol.setCellFactory(cellFactory);
        SumCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Results, String> t) -> {
                    ((Results) t.getTableView().getItems()
                    .get(t.getTablePosition().getRow()))
                    .setSum(t.getNewValue());

                });

        table.setItems(data);
        // Using add insted of addAll to avoid warnings
        //table.getColumns().addAll(EndTimeCol, InterTimeCol, BlackPointsCol, SumCol);
        table.getColumns().add(EndTimeCol);
        table.getColumns().add(InterTimeCol);
        table.getColumns().add(BlackPointsCol);
        table.getColumns().add(SumCol);
    }    
    
    // Start stopwatch after delay
    private void CountDownToStart(){
        if(sequence == 0){
            if (StartDelay != 0){
                StatusMessages.setText("Start delayed for: " + (StartDelay/1000) + " s");
            }
            
            // Set new button text and lower opacity untill command playback is finished
            StartStopResetButton.setText("Stop");
            StartStopResetButton.setDisable(true);
            StartStopResetButton.setOpacity(0.5);
            Stopwatch.root.requestFocus();
            
            //SettingsButton.setDisable(true);
            //SettingsButton.setOpacity(0.5);
            
            
            new Timer().schedule(  
                new TimerTask() {
                @Override
                public void run() {
                    //Stop stopwatch
                    Platform.runLater(() -> {
                        StatusMessages.setText("Running...");
                        Start();
                    });
                }
            },
            StartDelay
            );
        }
    }
    
    // Starting stopwatch
    private void Start(){
        
        if(sequence == 0){
            
            // Increase sequence number to prevent more than one call of method Start()
            sequence = 1;
            
            try{
                // Open audio file from preferences
                File file = new File(PathToAudioFile);
                in = AudioSystem.getAudioInputStream(file);
                
                AudioFormat baseFormat = in.getFormat();
                decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                din = AudioSystem.getAudioInputStream(decodedFormat, in);
                
                // Get audio duration
                AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
                Map properties = baseFileFormat.properties();
                AudioDuration = (Long) properties.get("duration");
                
                // Define new timer
                Timer timer = new Timer();
            
                // Schedule timer execution after audio stops playing: tag "AudioDuration"
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                    
                        // Remember starting time
                        timeBegan = System.currentTimeMillis();
                    
                        StartStopResetButton.setDisable(false);
                        StartStopResetButton.setOpacity(1);
                        sequence = 2;
                
                        // Set timeline: calling of Running() method every 30 milliseconds
                        RunningTimeline = new Timeline(
                            new KeyFrame(
                                Duration.millis( 30 ),
                                event -> {
                                    if (sequence == 2){
                                        Running();                            
                                    }
                        
                                }
                            )
                        );
                        RunningTimeline.setCycleCount( Animation.INDEFINITE );
                        RunningTimeline.play();
                    
                    }
                }, (AudioDuration/1000) - AudioOffset);
            
                // Creating new thread to prevent window freezing
                Thread t1 = new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try {
                                                
                        
                        
                            // Play audio file (command)
                            rawplay(decodedFormat, din);
                            in.close();
                        
                        } catch (IOException | LineUnavailableException e){
                            StatusMessages.setText("Exception catched: " + e);
                        }
                        // Interrupt thread when finished
                        Thread.currentThread().interrupt();
                    }
            
                });  
                t1.start();
            
            } catch (IOException | UnsupportedAudioFileException e){
                StatusMessages.setText("Exception catched: " + e);
                
                // Reset values
                sequence = 0;
                // Set new button text and lower opacity untill command playback is finished
                StartStopResetButton.setText("Start");
                StartStopResetButton.setDisable(false);
                StartStopResetButton.setOpacity(1);
            
                //SettingsButton.setDisable(false);
                //SettingsButton.setOpacity(1);
            }
            
        }
    }
    
    // Stoping stopwatch
    public void Stop(String ButtonNr) {
        if (sequence == 2){
            
            // Stop timeline execution
            RunningTimeline.stop();
            
            // Get current time and calculate elapsed time
            timeStopped = System.currentTimeMillis();
            timeElapsed = (timeStopped - timeBegan);
            
            long milliseconds = (timeElapsed % 1000) / 10;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed)%60;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed);
        
            // Set new time in GUI
            Time.setText(String.format( "%02d:%02d.%02d", minutes, seconds, milliseconds));
            
            
            // Report which button stopped stopwatch
            StatusMessages.setText("Stopped by button: " + ButtonNr);
            
            // Set new button text
            StartStopResetButton.setText("Reset");
            sequence = 3;
            
            // Toggle visibility
            MistakePoints.setDisable(false);
            MistakePoints.setOpacity(1);
            
        }
        
    }
    
    // Reseting stopwatch
    private void Reset() {
        if (sequence == 3){
            
            // Generate string with mistake points
            String Kazenske = MistakePoints.getText();
            if (Kazenske.equals("")){
                Kazenske = "0";
            }
            
            String Total;
            
            try{
                // Calulate total time
                long total = timeElapsed + Integer.parseInt(Kazenske)*1000;
            
                long milliseconds = (total % 1000) / 10;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(total)%60;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(total);
        
                // Set Total text
                Total = String.format( "%02d:%02d.%02d", minutes, seconds, milliseconds);
            }
            catch( Exception e){
                Total = Kazenske;
                Kazenske = "";
            }
            
            // Saving results
            String[] SplittedIntermediate = IntermediateTime.split(newLine);
            for (int ind = 0; ind <= SplittedIntermediate.length-1; ind++){
                if (ind == 0){
                    data.add(new Results(Time.getText(), SplittedIntermediate[ind], Kazenske, Total));
                }
                else{
                    data.add(new Results("", SplittedIntermediate[ind], "", ""));
                }
            }
            
            
            // Resetting parameters
            timeBegan = 0;
            timeStopped = 0;
            IntermediateTime = "";
            Intermediate.setText(IntermediateTime);
            MistakePoints.setText("");
            
            // Set new button text
            StartStopResetButton.setText("Start");
            sequence = 0;
            
            // Reset GUI text
            Time.setText("00:00.00");
            
            //SettingsButton.setDisable(false);
            //SettingsButton.setOpacity(1);
            
            // Toggle visibility
            MistakePoints.setDisable(true);
            MistakePoints.setOpacity(0.5);
            
            
        }
    }
    
    // Stopwatch running: executed periodically while running
    private void Running() {
        // Get current time and calculate elapsed time
        currentTime = System.currentTimeMillis();
        timeElapsed = (currentTime - timeBegan);
        
        long milliseconds = (timeElapsed % 1000) / 10;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed)%60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed);
        
        // Set GUI text
        Time.setText(String.format( "%02d:%02d.%02d", minutes, seconds, milliseconds));
    }
    
    public void IntermediateTimes(){
        
        if(!Time.getText().equals("00:00.00")){
            // Get current time and calculate elapsed time
            currentTime = System.currentTimeMillis();
            timeElapsed = (currentTime - timeBegan);
        
            long milliseconds = (timeElapsed % 1000) / 10;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed)%60;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeElapsed);
        
            // Set GUI text
            IntermediateTime = IntermediateTime + String.format( "%02d:%02d.%02d", minutes, seconds, milliseconds) + newLine;
            Intermediate.setText(IntermediateTime);
        }
        
    }
    
    
    //////////////////////////////////////SERIAL COMMUNICATION/////////////////////////////////////
    
    SerialPort serialPort;
    
    //search for all the serial ports
    //pre: none
    //post: adds all the found ports to a combo box on the GUI
    public ObservableList<String> searchForPorts()
    {
        ObservableList<String> PortsList = FXCollections.observableArrayList();
 
        String[] serialPortNames = SerialPortList.getPortNames();
        
        for(String name: serialPortNames){
            PortsList.add(name);
        }
        
        return PortsList;
    }

    // Connect to selected Serial port
    // success flag is returned if connection succesfull
    public void ConnectAndListen(String port){
        
        serialPort = new SerialPort(port);
        
        try {
            serialPort.openPort();
            serialPort.setParams(Integer.parseInt(Baudrate), serialPort.DATABITS_8, serialPort.STOPBITS_1, serialPort.PARITY_NONE);
            serialPort.setEventsMask(MASK_RXCHAR);
            
            StatusMessages.setText("Established connection on " + port + " with baudrate of " + Baudrate);
            
            serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
                
                if(serialPortEvent.isRXCHAR()){
                    
                    try {
                        
                        
                        byte[] b = serialPort.readBytes();
                        String st = new String(b);
                        
                        //Stop stopwatch
                        Platform.runLater(() -> {
                            Stop(st);
                        });
                        
          
                    } catch (SerialPortException ex) {
                        System.out.println("SerialPortException: " + ex.toString());
                        StatusMessages.setText("SerialPortException: " + ex.toString());
                    }
                    
                }
            });
            
        } catch (SerialPortException ex) {
            
            System.out.println("SerialPortException: " + ex.toString());
            StatusMessages.setText("SerialPortException: " + ex.toString());
        }
        
    }
    
    public void Disconnect(){
        
        
        if(serialPort != null){
            try {
                serialPort.removeEventListener();
                
                if(serialPort.isOpened()){
                    serialPort.closePort();
                    StatusMessages.setText("Connection closed");
                }
                
            } catch (SerialPortException ex) {
                System.out.println("SerialPortException: " + ex.toString());
                StatusMessages.setText("SerialPortException: " + ex.toString());
            }
        }
    }
    
    ////////////////////////////////////////SAVE FILE///////////////////////////////////////////
    public void SaveFile() throws IOException {    
        FileChooser chooser = new FileChooser();
    
        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Comma Separated Files (*.csv)", "*.csv");
        chooser.getExtensionFilters().add(extFilter);
    
        //Set initial directory
        chooser.setInitialDirectory(new File (System.getProperty("user.home") + System.getProperty("file.separator")+ "Desktop"));
              
        //Show save file dialog
        File file = chooser.showSaveDialog(null);
        
        if (file != null){
            //Write to file
            Writer writer = null;
            try {
            
                writer = new BufferedWriter(new FileWriter(file));
                String header = "Koncni cas;Vmesni Cas;Kazenske;Skupaj\n";
                writer.write(header);
                for (Results results : data) {

                    String text = results.getEndTime() + ";" + results.getInterTimes() + ";" + results.getBlackPoints() + ";" + results.getSum() + "\n";



                    writer.write(text);
                }
            } catch (Exception e) {
                StatusMessages.setText("Exception catched: " + e);
            }
            finally {

                writer.flush();
                writer.close();
            } 
        }
    
    
    }
    
    // Playing audio
    private void rawplay(AudioFormat targetFormat, AudioInputStream din) throws IOException, LineUnavailableException
    {
        byte[] data = new byte[4096];
        SourceDataLine line = getLine(targetFormat);
        if (line != null)
        {
            // Start
            line.start();
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1)
            {
                nBytesRead = din.read(data, 0, data.length);
                if (nBytesRead != -1) nBytesWritten = line.write(data, 0, nBytesRead);
            }
            // Stop
            line.drain();
            line.stop();
            line.close();
            din.close();
        }
    }

    private SourceDataLine getLine(AudioFormat audioFormat) throws LineUnavailableException
    {
        SourceDataLine res = null;
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        res = (SourceDataLine) AudioSystem.getLine(info);
        res.open(audioFormat);
        return res;
    } 

    public void PreferencesInit() {
        //Connect to Serial port if defined
        Baudrate = Preferences.preferences.get("Baudrate", "115200");
        StartDelay = Preferences.preferences.getInt("StartDelay", 0);
        PathToAudioFile = Preferences.preferences.get("PathToAudioFile", "Path to audio file");
        AudioOffset = Preferences.preferences.getInt("AudioOffset", 0);
        if (!Objects.equals(SerialPort, Preferences.preferences.get("SerialPort", null))){
            if (ConnectionEstablished){
                Disconnect();
                ConnectionEstablished = false;
            }
            SerialPort = Preferences.preferences.get("SerialPort", null);
        }
        if (SerialPort != null && !ConnectionEstablished){
            ConnectAndListen(SerialPort);
            ConnectionEstablished = true;
        }
            
        
    }
    
    
    //////////////////////////////Operating table//////////////////////////////
    
    class EditingCell extends TableCell<Results, String> {

        private TextField textField;

        private EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                //textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(item);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
//                        setGraphic(null);
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction((e) -> commitEdit(textField.getText()));
            /*/textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    StatusMessages.setText("Commiting " + textField.getText());
                    commitEdit(textField.getText());
                }
            });*/
            
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
    
    public static class Results {

        private final SimpleStringProperty EndTime;
        private final SimpleStringProperty InterTimes;
        private final SimpleStringProperty BlackPoints;
        private final SimpleStringProperty Sum;

        public Results(String EndTime, String InterTimes, String BlackPoints, String Sum) {
            this.EndTime = new SimpleStringProperty(EndTime);
            this.InterTimes = new SimpleStringProperty(InterTimes);
            this.BlackPoints = new SimpleStringProperty(BlackPoints);
            this.Sum = new SimpleStringProperty(Sum);
        }

        public String getEndTime() {
            return EndTime.get();
        }

        public StringProperty EndTimeProperty() {
            return this.EndTime;
        }

        public void setEndTime(String EndTime) {
            this.EndTime.set(EndTime);
        }

        public String getInterTimes() {
            return InterTimes.get();
        }

        public StringProperty InterTimeProperty() {
            return this.InterTimes;
        }

        public void setInterTime(String InterTimes) {
            this.InterTimes.set(InterTimes);
        }

        public String getBlackPoints() {
            return BlackPoints.get();
        }

        public StringProperty BlackPointsProperty() {
            return this.BlackPoints;
        }

        public void setBlackPoints(String BlackPoints) {
            this.BlackPoints.set(BlackPoints);
        }
        
        public String getSum() {
            return Sum.get();
        }

        public StringProperty SumProperty() {
            return this.Sum;
        }

        public void setSum(String Sum) {
            this.Sum.set(Sum);
        }

    }
    
    
}
