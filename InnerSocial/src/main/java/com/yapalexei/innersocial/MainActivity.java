package com.yapalexei.innersocial;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends Activity {
    private boolean loggedIn = false;
    private String[] userInfo;
    private String url, usr, psw;
    private ListView mListView;
    private ProgressDialog mProgressDialog;
    private ArrayAdapter<String> aa;
    private ArrayList<String> posts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url = "http://www.alexeiyagolnikov.com/AndroidAuth.php";
        mListView = (ListView) findViewById(R.id.listView);
        // Login first!
        // Are we logged in?
        if(!loggedIn){
            Intent loginView = new Intent(this, Login.class);
            loginView.putExtra("url", url);
            startActivityForResult(loginView, 0);
        }

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int position, long id){
                if(!aa.isEmpty()){
                    try{
                        final String item = (String) adapterView.getItemAtPosition(position);
                        //aa.remove(item);
                        posts.remove(position);
                        aa.notifyDataSetChanged();
                    }catch (IndexOutOfBoundsException e){
                        // In case there are delays in index lengths
                        Log.e("DELETE_ITEM_ERROR", e.toString() + "; position=" + position + "; id=" + id);
                    }

                }
                return true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //int result = data.getIntExtra(this.getString(R.string.result), -1);
        if (resultCode == RESULT_OK) {
            loggedIn = true;
            String dataContent = data.getStringExtra("data");
            usr = data.getStringArrayExtra("userInfo")[0];
            psw = data.getStringArrayExtra("userInfo")[1];
            posts = getInfoFromJSON(dataContent, "Content");
            aa = new ArrayAdapter<String>(this, R.layout.row, R.id.label, posts);
            mListView.setAdapter(aa);

        }else if(resultCode == RESULT_CANCELED)
            this.finish();
    }

    private void exitMessageToUser(String message) {
        mProgressDialog.dismiss();
        Log.e("WE GOT A PROBLEM", message);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setNeutralButton("ok", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onRefreshClick(View view){
        visitServer(url, usr, psw, "");
    }

    private void visitServer(String url, String usr, String pass, String content){
        ConnectToServer connectToServer = new ConnectToServer(url, usr, pass, content);
        connectToServer.execute();

        try {
            posts = getInfoFromJSON(connectToServer.get().toString(), "Content");
            UpdateUIList updateUIList = new UpdateUIList(posts);
            updateUIList.execute();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public class UpdateUIList extends AsyncTask{
        public UpdateUIList(ArrayList<String> updatedList){
            aa = new ArrayAdapter<String>(MainActivity.this,
                    R.layout.row,
                    R.id.label,
                    updatedList);
        }

        @Override
        protected Object doInBackground(Object[] objs) {

            mListView.post(new Runnable() {
                @Override
                public void run() {
                    aa.notifyDataSetChanged();
                    mListView.setAdapter(aa);
                }
            });
            return null;
        }
    }

    private class ConnectToServer extends AsyncTask{
        String url, usr, psw, singlePost;
        public ConnectToServer(String url, String usr, String psw, String singlePost){
            this.url = url;
            this.usr = usr;
            this.psw = psw;
            this.singlePost = singlePost;
        }

        @Override
        protected Object doInBackground(Object[] objs) {
            DataConnect connection = new DataConnect(url, usr, psw, singlePost);
            connection.connect();

            return connection.getResults();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Post a new entry");
        alert.setMessage("Message");


        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //String value = input.getText().toString();
                visitServer(url,usr,psw,input.getText().toString());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();

        Toast.makeText(MainActivity.this, "clicked: " + item.getTitle().toString(), 1000).show();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public String getContentFromJSON(String inString, int item){
        String content = null;
        JSONArray jArray = null;
        JSONObject json_data = null;
        try {
            jArray = new JSONArray(inString);
        } catch (JSONException e) {
            Log.i("JSON info", e.toString());
        }
        if(jArray != null){
            try {
                json_data = jArray.getJSONObject(item);
            } catch (JSONException e) {
                Log.i("JSON info", e.toString());
            }
        }

        if (json_data != null) {
            try {
                content=json_data.getString("Content");
            } catch (JSONException e) {
                Log.i("JSON info", e.toString());
            }

        }
        return content;
    }

    public ArrayList<String> getInfoFromJSON(String inString, String column){
        //Map<String,String[]> hashContentTable = new HashMap<String,String[]>();
        ArrayList<String> listItems = null;
        JSONObject json_data = null;
        String name;
        JSONArray jArray = null;
        int listSize = 0;

        try {
            jArray = new JSONArray(inString);
        } catch (JSONException e) {
            Log.i("JSON info", e.toString());
        }

        if (jArray != null) {
            listSize = jArray.length();
        }

        listItems = new ArrayList<String>(listSize); // set the size of list

        for( int i = listSize - 1; i > 0; i-- ){
            try {
                json_data = jArray.getJSONObject(i);
                // Add to the HashMap
//                hashContentTable.put(json_data.getString("ID"),
//                        new String[]{json_data.getString("ID"),
//                                json_data.getString(column)});
            } catch (JSONException e) {
                Log.i("JSON info", e.toString());
            }

            if (json_data != null) {
                try {
                    name=json_data.getString(column);
                    Log.i("JSON info", name);
                    listItems.add(name);
                } catch (JSONException e) {
                    Log.i("JSON info", e.toString());
                }
            }
        }

        return listItems;
    }
}
