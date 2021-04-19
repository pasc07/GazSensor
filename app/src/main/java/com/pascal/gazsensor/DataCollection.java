package com.pascal.gazsensor;

import com.github.mikephil.charting.charts.LineChart;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class DataCollection implements NetworkAsyncTask.Listeners {

    private List<HumidityData> list =new ArrayList<HumidityData>();
    private List<Double> hum = new ArrayList<Double>();

    public void executeHttpRequest(){

        new NetworkAsyncTask(this).execute("https://api.thingspeak.com/channels/1354241/fields/2.json?results=1");

    }
    private void updateUIWhenStartingHTTPRequest(){

        //Pre task
    }

    private void updateUIWhenStopingHTTPRequest(String response){
        String jsonFile = response;
        String[] file;
        double humidity;
        file = jsonFile.split(",");
        jsonFile = "";
        jsonFile=file[file.length-3]+","+file[file.length-2]+","+file[file.length-1];
        jsonFile=jsonFile.substring(8, jsonFile.length() - 1);

        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(jsonFile).getAsJsonArray();
        list.add(gson.fromJson(array.get(0), HumidityData.class));
        //mis a jour du tableau
        hum.add(Double.parseDouble(list.get(0).getField2()));
        //Mis a jour de du tableau

    }

    public List<Double> getHum(){
        return hum;
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void doInBackground() {
    }

    @Override
    public void onPostExecute(String success) {
        this.updateUIWhenStopingHTTPRequest(success);

        this.executeHttpRequest();
    }
}
