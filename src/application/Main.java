package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
E-mail adress:   1000276626804260@facebook.com.facebook
Token:           288D8C3EFEF24EF81B1267895F24541B

created by Tomasz Bieniek
contact: tomabie433@student.polsl.pl
tel: 725505739

Please open generated txt file using better editor than default in Windows systems (eg.Notepad++)
in order to display text properly.
 */



public class Main extends Application
{
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        // loading fxml file
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(this.getClass().getResource("/Resources/fxml/GameScene.fxml"));
        Parent pane = loader.load();

        Scene scene = new Scene(pane);

        //setting scene in the stage
        primaryStage.setScene(scene);

        // setting title
        primaryStage.setTitle("WPFP2018");

        //blocking resizability
        primaryStage.setResizable(false);

        //displaying
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        Application.launch(args);
    }

}
