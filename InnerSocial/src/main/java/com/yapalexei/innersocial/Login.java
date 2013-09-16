package com.yapalexei.innersocial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class Login extends Activity {
    private EditText userInputView;
    private EditText passInputView;
    private String[] userInfo = new String[2];
    private String url;

    private boolean loggedInStatus;
    private DataConnect connection;

    private String responseData = "";
    public int NORMAL_MESSAGE = 0;
    public int PROBLEM_OCCURRED = 1;
    AlertDialog alert;
    //ConnectToServer connectToServer; // Async Task
    InitTask connectionWithProgress; // Async Task

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        userInputView = (EditText) findViewById(R.id.userName);
        passInputView = (EditText) findViewById(R.id.password);
        loggedInStatus = false;
        url = getIntent().getStringExtra("url");

     /* Create some local check, to see if the user is
        already logged in, otherwise go through the
        normal logging in process
        TODO Create a local login check */

    }
    // Do the login process when the login button is pressed.
    public void onLogin(View view) throws ExecutionException, InterruptedException, IOException {
        userInfo[0] = userInputView.getText().toString();
        userInfo[1] = passInputView.getText().toString();
        if(!userInputView.getText().toString().equals("")){
            connectionWithProgress = new InitTask();
            connectionWithProgress.execute();
        }
    }

    /**
     * sub-class of AsyncTask
     */
    protected class InitTask extends AsyncTask<Context, Integer, String> {

        // -- run intensive processes here
        // -- notice that the datatype of the first param in the class definition matches the param passed to this
        // method
        // -- and that the datatype of the last param in the class definition matches the return type of this mehtod
        @Override
        protected String doInBackground(Context... params) {
            // -- on every iteration
            // -- runs a while loop that causes the thread to sleep for 50 milliseconds
            // -- publishes the progress - calls the onProgressUpdate handler defined below
            // -- and increments the counter variable i by one
            connection = new DataConnect(url, userInfo[0], userInfo[1], "");
            connection.connect();

            try{
                responseData = connection.getResults();
                if(responseData.charAt(0) == '[')
                    loggedInStatus = true;

            }catch (NullPointerException e){
                alert.dismiss();
                alert = messageToUser("user name and/or password is not correct! - NPE", PROBLEM_OCCURRED);

            }

            if(loggedInStatus){
                Intent data = new Intent();
                Bundle bundle = new Bundle();

                bundle.putStringArray("userInfo", userInfo);
                bundle.putString("data", responseData);

                data.putExtras(bundle);
                Login.this.setResult(RESULT_OK, data);

            }else{
                alert.dismiss();
                alert = messageToUser("user name and/or password is not correct!", PROBLEM_OCCURRED);
                return "FAILED!!";
            }
//            // This is for progress calculations for later.
//            int i = 0;
//            while (i <= 50) {
//                try {
//                    Thread.sleep(30);
//                    publishProgress(i);
//                    i++;
//                }
//                catch (Exception e) {
//                    Log.i("makemachine", e.getMessage());
//                }
//            }
            return "COMPLETE!";
        }

        // -- gets called just before thread begins
        @Override
        protected void onPreExecute() {
            Log.i("makemachine", "onPreExecute()");
            super.onPreExecute();
            alert = messageToUser("Loading... Please wait..", NORMAL_MESSAGE);
        }

        // -- called from the publish progress
        // -- notice that the datatype of the second param gets passed to this method
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("makemachine", "onProgressUpdate(): " + String.valueOf(values[0]));
//            _percentField.setText((values[0] * 2) + "%");
//            _percentField.setTextSize(values[0]);
        }

        // -- called if the cancel button is pressed
        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i("makemachine", "onCancelled()");
//            _percentField.setText("Cancelled!");
//            _percentField.setTextColor(0xFFFF0000);
            alert.dismiss();
            Login.this.finish();
        }

        // -- called as soon as doInBackground method completes
        // -- notice that the third param gets passed to this method
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("makemachine", "onPostExecute(): " + result);
//            _percentField.setText(result);
//            _percentField.setTextColor(0xFF69adea);
//            _cancelButton.setVisibility(View.INVISIBLE);
            alert.dismiss();
            Login.this.finish();
        }
    }

    private AlertDialog messageToUser(String message, int natureOfMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(natureOfMessage == PROBLEM_OCCURRED){
            Log.e("WE GOT A PROBLEM", message);
            builder.setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id){
                            // Do nothing to try and log in again.
                        }
                    })
                    .setNegativeButton("Quit", new DialogInterface.OnClickListener(){
                        public void onClick(DialogInterface dialog, int id){
                            Login.this.setResult(0);
                            Login.this.finish();
                        }
                    });
        }else if(natureOfMessage == NORMAL_MESSAGE){
            builder.setMessage(message)
                    .setCancelable(true).setIcon(R.drawable.ic_launcher);
        }
        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

}
