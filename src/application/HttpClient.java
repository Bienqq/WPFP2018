package application;

import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import okhttp3.*;
import java.io.IOException;
import org.json.*;

public class HttpClient
{
    private  final String login = "1000276626804260@facebook.com.facebook";
    private  final String token = "288D8C3EFEF24EF81B1267895F24541B";
    private  final MediaType mediaType = MediaType.parse("application/json");
    private  final OkHttpClient client = new OkHttpClient();
    private  String url;

    // formatting JSON string
    private static String formatString(String text)
    {
        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++)
        {
            char letter = text.charAt(i);
            switch (letter)
            {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;
                default:
                    json.append(letter);
                    break;
            }
        }
        return json.toString();
    }

    //GET request - describing current state of the game
    public String describe() throws IOException
    {
        //building url address
        String address = url + "/describe?login=" + login + "&token=" + token;

        // building request
        Request request = new Request.Builder()
                .url(address)
                .build();

        //if connect timed out
        Response response;
        try
        {
            response = client.newCall(request).execute();
        }
        catch (Exception e)
        {
            // display connection error
            showConnectionError();
            return null;
        }

        String responseString = null;

        if ( response.isSuccessful() && response.code() == 200)
        {
            responseString = response.body().string();
        }
        response.body().close();
        return responseString;
    }

    //getting data to save in txt file
    public String getFinalLog() throws Exception
    {
        String data = describe();
        data = formatString(data);
        return data;
    }

    // POST request method
    private boolean doPostRequest(String command) throws IOException
    {
        RequestBody body = RequestBody.create(mediaType, command);
        Request request = new Request.Builder()
                .url(url + "/execute")
                .post(body)
                .addHeader("content-type", "application/json")
                .addHeader("cache-control", "no-cache")
                .build();

        Response response;
        //if connect timed out
        try
        {
            response = client.newCall(request).execute();
        }
        catch (Exception e)
        {
            // display connection error
            showConnectionError();
            return false;
        }
        if (response.isSuccessful() && response.code() == 200)
        {
            //close connection
            response.body().close();
            return true;
        }

        // displaying alert with error - unknown command or experiment was terminated
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.getDialogPane().setCursor(Cursor.HAND);
        alert.setHeaderText(response.body().string());
        alert.showAndWait();

        //close connection
        response.body().close();
        return true;
    }

    // build command string
    public boolean sendCommand(String[] userString) throws IOException
    {
        String commandString;
        boolean successful;
        switch (userString.length)
        {
            case 1:
                commandString = "{\"Command\":\"" + userString[0] + "\","
                        + "\"Login\":\"" + login + "\","
                        + "\"Token\":\"" + token + "\"}";
                successful = doPostRequest(commandString);
                break;
            case 2:
                commandString = "{\"Command\":\"" + userString[0] + "\","
                        + "\"Login\":\"" + login + "\","
                        + "\"Token\":\"" + token + "\","
                        + "\"Parameter\":\"" + userString[1] + "\"}";
                successful = doPostRequest(commandString);
                break;
            case 3:
                commandString = "{\"Command\":\"" + userString[0] + "\","
                        + "\"Login\":\"" + login + "\","
                        + "\"Token\":\"" + token + "\","
                        + "\"Parameter\":\"" + userString[1] + "\","
                        + "\"Value\":\"" + userString[2] + "\"}";
                doPostRequest(commandString);
                successful = doPostRequest(commandString);
                break;
            default:
                successful = false;
                break;
        }
        return successful;
    }

    //setting url address
    public void setUrl( String newUrl)
    {
        url = newUrl;
    }

    //getting JSON header data
    public String []  getJSONdata() throws Exception
    {
        // json object to process data
        String [] JSONdata = new String[8];
        String responseString = describe();
        JSONObject obj;
        try
        {
             obj = new JSONObject(responseString);
        }
        catch (NullPointerException e)
        {
            return null ;
        }

        //scores and parameters
        String parameters = obj.getJSONObject("parameters").toString();
        String scores = obj.getJSONObject("scores").toString();
        parameters = formatString(parameters);
        scores = formatString(scores);
        JSONdata[0] = "Parameters:" + parameters + "\n Scores:" + scores;

        //logBook
        JSONArray arrLogBook = obj.getJSONArray("logBook");
        JSONdata[1] = formatString("Log Book" + arrLogBook.toString());

        //equipments
        JSONArray arrEquipments = obj.getJSONArray("equipments");
        JSONdata[2] = formatString("Equipments" + arrEquipments.toString());

        //events history
        JSONArray arrEvents = obj.getJSONArray("events");
        JSONdata[3] = formatString("History" + arrEvents.toString() );

        //last turn events
        JSONArray arrLastTurnEvents = obj.getJSONArray("lastTurnEvents");
        JSONdata[4] = formatString("Last turn" + arrLastTurnEvents.toString());

        // turn number
        int turn = obj.getInt("turn");
        JSONdata[5] = String.valueOf(turn);

        // current location
        JSONdata[6] = obj.getString("location");

        //game status
        boolean isTerminated = obj.getBoolean("isTerminated");
        JSONdata[7] = Boolean.toString(isTerminated);

        return JSONdata;
    }

    private void showConnectionError()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed connection to server!");
        alert.getDialogPane().setCursor(Cursor.HAND);
        alert.showAndWait();
    }
}
