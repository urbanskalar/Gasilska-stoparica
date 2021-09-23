/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stopwatch;



import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 *
 * @author urban
 */
public class Stopwatch extends Application{
    
    // Creating class instances
    StopwatchPreferences Preferences = new StopwatchPreferences();
    
    public static Stage parentWindow;
    public static Parent root;
        
    @Override
    public void start(Stage stage) throws Exception {
                
        parentWindow = stage;
        
        
        
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Stopwatch.class.getResource("MainFXML.fxml"));
        root = loader.load();
        
        MainFXMLController mainController = loader.getController();
        
        String[] ColorArray = {"#ffc107", "#30e6f1", "#00c853", "#aeea00"};
        
        mainController.RandomColor = getRandomColor(ColorArray);
        root.setStyle("-fx-custom-color:" + mainController.RandomColor + ";");
        
        Scene scene = new Scene(root);
        // set css style
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        
        // handle keypresses
        scene.setOnKeyPressed(new EventHandler<KeyEvent>(){
            @Override
            public void handle(KeyEvent event){
                // Pressing of ADD key
                if (event.getCode() == KeyCode.ADD){
                    mainController.IntermediateTimes();
                }
                if (event.getCode() == KeyCode.ALT_GRAPH){
                    mainController.StartStopReset();
                }
                
            }    
        });
        
        stage.setTitle("Tik tak app <3");
        //stage.setResizable(false);
        stage.getIcons().add(new Image(Stopwatch.class.getResourceAsStream("icon.png")));
        
        stage.setScene(scene);
        
        
        stage.show();
        
        // Setting minimal stage size
        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
        
        parentWindow = stage;
        
        
        // Closing all threads
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.out.println("Bye bye!");
                mainController.Disconnect();
                Platform.exit();
                System.exit(0);
            }
        });

    }
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
        
    }
    
    public static String getRandomColor(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }


    
}

