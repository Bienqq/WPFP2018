package Controllers;

import application.HttpClient;
import application.SavingFile;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

public class GameController
{
    // Lists with commands, parameters and values ...
    private final ObservableList<String> commandList =
            FXCollections.observableArrayList("Scan", "MoveTo", "Produce", "Harvest", "Repair", "Order", "Restart");

    private final ObservableList<String> placesList =
            FXCollections.observableArrayList("Chaarr", "Esthajnalcsillag", "Shuttle", "Asteroids", "Południca");

    private final ObservableList<String> produceList =
            FXCollections.observableArrayList("Decoy", "Weapons", "Supplies", "Tools", "Energy", "Shuttlewrench");

    private final ObservableList<String> harvestList = placesList;

    private final ObservableList<String> repairList =
            FXCollections.observableArrayList("Communications", "Shuttle", "Partialshuttle", "Asteroids", "Południca",
                    "Esthajnalcsillag","Chaarr");

    private final ObservableList<String> orderList =
            FXCollections.observableArrayList("Help", "FinalWar", "EvacScience", "EvacSurvivors", "Retreat");

    private final ObservableList<String> orderValues = placesList;

    private final ObservableList<String> gameModeList = FXCollections.observableArrayList("Simulation", "Chaarr");

    // txt file for logs
    private final SavingFile savingObject = new SavingFile();

    //user inputs
    private String selectedCommand;
    private String selectedParameter;
    private String selectedValueOrder;
    private String selectedGameMode;
    private boolean gameStarted = false;
    private boolean savingToFile = false;

    //Http Client object
    private HttpClient clientObject;

    //ComboBoxes
    @FXML private ComboBox<String> commandBox;
    @FXML private ComboBox<String> valueBox;
    @FXML private ComboBox<String> parametersBox;
    @FXML private ComboBox<String> gameModeBox;

    //Check boxes
    @FXML private CheckBox savingToFIleCheckBox;

    //Spinner
    @FXML private Spinner<Integer> spinner;

    // buttons
    @FXML private Button startGameButton;

    //Labels
    @FXML private Label gameModeLabel;
    @FXML private Label lastRequestLabel;
    @FXML private Label turnLabel;
    @FXML private Label terminatedLabel;
    @FXML private Label locationLabel;

    //TextAreas
    @FXML private TextArea scoresAndParametersField;
    @FXML private TextArea logBookTextArea;
    @FXML private TextArea lastTurnTextArea;
    @FXML private TextArea historyTextArea;
    @FXML private TextArea equipmentsTextArea;


    public void initialize()
    {
        clientObject = new HttpClient();

        // setting starting options on GUI
        commandBox.getItems().addAll(commandList);
        gameModeBox.getItems().addAll(gameModeList);

        //setting starting options
        setStartingOptions();

        // Updating GUI with timer
        Timeline timer = new Timeline(new KeyFrame(Duration.millis(30),(event) -> updateGUI() ));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();

        // blocking letters as spinner inputs
        NumberFormat format = NumberFormat.getIntegerInstance();
        UnaryOperator<TextFormatter.Change> filter = c ->
        {
            if (c.isContentChange())
            {
                ParsePosition parsePosition = new ParsePosition(0);
                // NumberFormat evaluates the beginning of the text
                format.parse(c.getControlNewText(), parsePosition);
                if (parsePosition.getIndex() == 0 || parsePosition.getIndex() < c.getControlNewText().length())
                {
                    // reject parsing the complete text failed
                    return null;
                }
            }
            return c;
        };
        TextFormatter<Integer> numberFormatter = new TextFormatter<>(new IntegerStringConverter(),1, filter);
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, Integer.parseInt("1")));
        spinner.getEditor().setTextFormatter(numberFormatter);
    }

    // Sending request method
    @FXML private void sendRequest() throws Exception
    {
        // if game mode is not selected
        if(selectedGameMode == null)
        {
            displayInfoAlert();
            return;
        }
        // if game is not started
        if(!commandBox.isVisible())
        {
            // showing information alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("Start game at first");
            alert.getDialogPane().setCursor(Cursor.HAND);
            alert.showAndWait();
            return;
        }

        // building complete command String
        String command = selectedCommand;

        if(selectedParameter != null)
        {
            command += " " + selectedParameter;
        }
        if( selectedValueOrder != null && valueBox.isVisible())
        {
            command += " " + selectedValueOrder;
        }
        if(spinner.isVisible())
        {
            command += " " + spinner.getValue();
        }
        if(command == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Your command is empty!");
            alert.showAndWait();
            return;
        }

        //setting label with last sending command
        lastRequestLabel.setText(command);

        // splitting command string into string array
        String [] commandArray = command.split("\\s+");

        // sending POST request with command
        boolean connectionError = clientObject.sendCommand(commandArray);

        if(!connectionError)
        {
            return;
        }
        //sending GET request to get current game state
        getGameInformation();

        // if saving to file is on then save logs
        if(savingToFile)
        {
            String logs =  "Sending command: " + command + "\n";
            savingObject.save(logs);
        }
    }

    // taking value order selected by user
    @FXML private void onValueSelected()
    {
        selectedValueOrder = valueBox.getSelectionModel().getSelectedItem();
    }

    @FXML private void startGameButtonClicked() throws Exception
    {
        // if game mode is not selected
        if(selectedGameMode == null)
        {
            displayInfoAlert();
            return;
        }

        gameStarted = true;
        commandBox.setVisible(true);
        startGameButton.setVisible(false);
        savingToFIleCheckBox.setVisible(true);

        // sending GET request to get current status of the game
        getGameInformation();
    }

    @FXML private void savingToFileCheckBoxClicked()
    {
        if(!savingToFile)
        {
            // creating file to save logs
            Stage currentStage = (Stage) savingToFIleCheckBox.getScene().getWindow();
            boolean result = savingObject.createFile(currentStage);
            // if cannot creating a file
            if(!result)
            {
                savingToFile = false;
                //uncheck box
                savingToFIleCheckBox.setSelected(false);
                return;
            }

        }
        savingToFile = !savingToFile;
    }

    private void getGameInformation() throws Exception
    {
        //get current game status
        String [] jsonData = clientObject.getJSONdata();
        if (jsonData == null)
        {
            return;
        }

        // setting labels and text areas with JSON data content
        scoresAndParametersField.textProperty().setValue(jsonData[0]);
        logBookTextArea.textProperty().setValue(jsonData[1]);
        equipmentsTextArea.textProperty().setValue(jsonData[2]);
        historyTextArea.textProperty().setValue(jsonData[3]);
        lastTurnTextArea.textProperty().setValue(jsonData[4]);
        turnLabel.setText(jsonData[5]);
        locationLabel.setText(jsonData[6]);

        String terminated = jsonData[7];

        // setting terminated label with appropriate color
        if(terminated.equals("false"))
        {
            terminatedLabel.setText("NO");
            terminatedLabel.setTextFill(Paint.valueOf("green"));
        }
        else
        {
            // if game is over save final log in text file
            if(savingToFile)
            {
                //saving last command
                String finalCommand =  "Sending command: " + lastRequestLabel.getText() + "\n";
                savingObject.save(finalCommand);
                //geting final game log
                String finalLog = "=====Final Log =====\n" + clientObject.getFinalLog();
                savingObject.save(finalLog);
                // uncheck saving to file checkbox
                savingToFile = false;
                savingToFIleCheckBox.setSelected(false);
            }

            //end of game
            terminatedLabel.setText("YES");
            terminatedLabel.setTextFill(Paint.valueOf("red"));
        }
    }


    // Taking command selected by user
    @FXML private void commandSelected()
    {
        selectedCommand = commandBox.getSelectionModel().getSelectedItem();
    }

    //taking parameter selected by user
    @FXML private void parameterSelected()
    {
        selectedParameter = parametersBox.getSelectionModel().getSelectedItem();
    }

    // taking selected game mode
    @FXML private void gameSelected()
    {
        selectedGameMode = gameModeBox.getSelectionModel().getSelectedItem();
        setStartingOptions();

        if(selectedGameMode.equals("Simulation"))
        {
            clientObject.setUrl("https://simulation.future-processing.pl");
            gameModeLabel.setText("Simulation");
            gameModeLabel.setTextFill(Paint.valueOf("green"));
        }
        else
        {
            clientObject.setUrl("https://chaarr.future-processing.pl");
            gameModeLabel.setText("CHAARR");
            gameModeLabel.setTextFill(Paint.valueOf("red"));
        }
    }

    // Updating GUI
    private void updateGUI()
    {
        if(selectedCommand != null && gameStarted)
        {
            parametersBox.setVisible(true);
            switch (selectedCommand)
            {
                case "Scan":
                    parametersBox.setItems(placesList);
                    break;
                case "MoveTo":
                    parametersBox.setItems(placesList);
                    break;
                case "Produce":
                    parametersBox.setItems(produceList);
                    break;
                case "Harvest":
                    parametersBox.setItems(harvestList);
                    break;
                case "Order":
                    parametersBox.setItems(orderList);
                    break;
                case "Repair":
                    parametersBox.setItems(repairList);
                    break;
                case "Restart":
                default:
                    parametersBox.setVisible(false);
                    spinner.setVisible(false);
                    valueBox.setVisible(false);
                    selectedParameter = null;
                    break;
            }
            if(selectedParameter == null)
            {
                spinner.setVisible(false);
                valueBox.setVisible(false);
            }
            else
            {
                if (selectedParameter.equals("Supplies"))
                {
                    spinner.setVisible(true);
                    valueBox.setVisible(false);
                }
                else if(selectedCommand.equals("Order"))
                {
                    valueBox.setVisible(true);
                    spinner.setVisible(false);
                    valueBox.setItems(orderValues);
                }
                else
                {
                    spinner.setVisible(false);
                    valueBox.setVisible(false);
                }
            }
        }
    }

    // displaying alert function
    private void displayInfoAlert()
    {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("You need to select game mode at first!");
        alert.getDialogPane().setCursor(Cursor.HAND);
        alert.showAndWait();
    }

    private void setStartingOptions()
    {
        startGameButton.setVisible(true);
        gameStarted = false;

        //clearing text Area
        logBookTextArea.clear();
        scoresAndParametersField.clear();
        equipmentsTextArea.clear();
        lastTurnTextArea.clear();
        historyTextArea.clear();

        //clearing labels
        turnLabel.setText("");
        locationLabel.setText("");
        terminatedLabel.setText("");
        lastRequestLabel.setText("");

        //hiding GUI items
        commandBox.setVisible(false);
        parametersBox.setVisible(false);
        valueBox.setVisible(false);
        spinner.setVisible(false);
        savingToFIleCheckBox.setVisible(false);
    }
}
