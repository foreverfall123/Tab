package com.namseoul.sa.tab;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by forev on 2018-11-22.
 */

public class DBConnection extends Activity {
    String myJSON;

    private static final String TAG_RESULTS = "result";
    private static final String TAG_ID = "id";
    private static final String TAG_PW = "name";
    private static final String TAG_NAME = "address";

    JSONArray people = null;

    ArrayList<HashMap<String, String>> personList;

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_activity);

        list = findViewById(R.id.listview);
        personList = new ArrayList<HashMap<String, String>>();
        getData("http://172.30.1.1/PHP_connection.php");
    }

    protected  void showList(){
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            people = jsonObj.getJSONArray(TAG_RESULTS);

            for(int i = 0; i < people.length(); i++){
                JSONObject c = people.getJSONObject(i);
                String id = c.getString(TAG_ID);
                String pw = c.getString(TAG_PW);
                String name = c.getString(TAG_NAME);

                HashMap<String, String> persons = new HashMap<String, String>();

                persons.put(TAG_ID,id);
                persons.put(TAG_PW,pw);
                persons.put(TAG_NAME,name);

                personList.add(persons);
            }

            ListAdapter adapter = new SimpleAdapter(
                    DBConnection.this, personList, R.layout.list_item,
                    new String[]{TAG_ID,TAG_PW,TAG_NAME},
                    new int[]{R.id.db_id,R.id.db_pw,R.id.db_name}
            );

            list.setAdapter(adapter);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void getData(String url){
        class GetDataJSON extends AsyncTask<String, Void, String>{
            @Override
            protected  String doInBackground(String... params){

                String uri = params[0];
                BufferedReader bufferedReader = null;

                try{
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine()) != null){
                        sb.append(json + "\n");
                    }

                    return sb.toString().trim();
                }catch(Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result){
                myJSON = result;
                showList();
            }
        }
        GetDataJSON g = new GetDataJSON();
        g.execute(url);
    }

}
