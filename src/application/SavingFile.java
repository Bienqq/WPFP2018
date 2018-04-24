package application;

import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class SavingFile
{
    private static File logFile;
    private static FileChooser fileChooser = new FileChooser();

    //creating file with dialog window
    public boolean createFile(Stage stage)
    {
        // showing window with saving file
        fileChooser.setTitle("Save file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text files", "*.txt"));

        logFile = fileChooser.showSaveDialog(stage);
        if(logFile == null)
        {
            displayError();
            return false;
        }
        try
        {
            // creating new file
            FileWriter fileWriter = new FileWriter(logFile);
            fileWriter.write("");
            fileWriter.close();
        }
        catch (IOException ex)
        {
            displayError();
            return false;
        }
        return true;
    }

    // saving data to existing file

    public void save(String content)
    {
        if(logFile == null)
        {
            displayError();
            return;
        }
        try
        {
            // append data to existing file
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(logFile, true));
            PrintStream printStream = new PrintStream(outputStream);
            printStream.println(content);
            printStream.close();
        }
        catch (IOException ex)
        {
            displayError();
        }
    }

    //displaying error
    private void displayError()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.getDialogPane().setCursor(Cursor.HAND);
        alert.setHeaderText("Cannot save file. File may not exist.");
        alert.showAndWait();
    }

}
