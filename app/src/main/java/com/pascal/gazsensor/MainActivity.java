package com.pascal.gazsensor;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NetworkAsyncTask.Listeners {
    private static final String TAG="MainActivity";

    private LineChart mChart;
    private  Thread thread;
    private boolean plotData=true;
    private List<HumidityData> list =new ArrayList<HumidityData>();
    private List<Double> hum = new ArrayList<Double>();
    private TextView textView;
    private String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textview);


        mChart= findViewById(R.id.linechart);

        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText("Real Time Sensor Data");

        mChart.setTouchEnabled(false);
        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);
        mChart.setBackgroundColor(Color.WHITE);


        LineData  data =new LineData();

        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);
        textView.setText("Start");
        this.executeHttpRequest();
        startPlot();
       // textView.setText("End");
    }

    private void startPlot() {
        if(thread !=null){
            thread.interrupt();
        }
        textView.setText("thread");

        thread = new Thread( new Runnable ()  {
            @Override
            public void run() {
                while (true){
                    plotData=true;
                    //textView.setText("Thread2");
                    try {
                       // updateUIWhenStopingHTTPRequest();
                        executeHttpRequest();
                        Thread.sleep(2000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }


    public void executeHttpRequest(){
            new NetworkAsyncTask(this).execute("https://api.thingspeak.com/channels/1354241/fields/2.json?results=1");
        }
        private void updateUIWhenStartingHTTPRequest(){

            //Pre task
        }

        public void updateUIWhenStopingHTTPRequest(String response){
        int i=0;
            //response = MyHttpURLConnection.startHttpRequest("https://api.thingspeak.com/channels/1354241/fields/2.json?results=1");
            //textView.setText(response);
            String jsonFile = response;
            String[] file;
            float humidity;
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
            humidity=(hum.get(hum.size()-1)).floatValue();

            if(plotData) {
                addEntry(humidity);
                textView.setText(list.get(0).getField2());
               plotData=false;
           }
        }

    public void addEntry(float humidity) {
        LineData data = mChart.getData();
        if(data!=null){
            ILineDataSet set =data.getDataSetByIndex(0);
            if(set==null){
                set = createSet();
                data.addDataSet(set);
            }

            data.addEntry( new Entry(set.getEntryCount() , (float)(Math.random()*75)+60f),0);
            //data.addEntry( new Entry(set.getEntryCount() , humidity),0);
            data.notifyDataChanged();

            mChart.notifyDataSetChanged();
            //mChart.setData(data);
            mChart.setMaxVisibleValueCount(150);
            mChart.moveViewToX(data.getEntryCount());

        }
    }

    public LineDataSet createSet(){
        LineDataSet set = new LineDataSet(null,"Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }


    @Override
    public void onPreExecute() {

    }

    @Override
    public void doInBackground() {

    }


    @Override
    public void onPostExecute(String success) {
       // this.updateUIWhenStopingHTTPRequest(success);
        //this.executeHttpRequest();
    }



    @Override
    protected void onPostResume() {
        super.onPostResume();
        //real time simulation and it work when added random in addentry function

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<100; i++){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //MyHttpURLConnection.startHttpRequest("https://api.thingspeak.com/channels/1354241/fields/2.json?results=1");
                            //updateUIWhenStopingHTTPRequest(response);
                            addEntry(10);
                        }
                    });
                    //pause
                    try {
                        Thread.sleep(600);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        thread.interrupt();
        super.onDestroy();
    }
}
