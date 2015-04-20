package com.example.stereotype13.jsonreader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.google.gson.Gson;


public class MainActivity extends ActionBarActivity {

    static {
        System.loadLibrary("myNDK");
    }

    private static final String URL = "http://api.icndb.com/jokes/random?escape=javascript";
    private static final String JOKE = "JOKE";
    private HttpAsyncTask httpAsyncTask;
    private TextView tvJoke;
    private Button btnGetJoke;
    private Random r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvJoke = (TextView) findViewById(R.id.textView);
        btnGetJoke = (Button) findViewById(R.id.btnGetJoke);

        r = new Random();
        btnGetJoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                httpAsyncTask = new HttpAsyncTask();
                httpAsyncTask.execute(URL);

                int i = r.nextInt(21 - 1) + 1;

                if(isPrime(i))
                    Toast.makeText(getApplicationContext(), i + " is prime", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), i + " is NOT prime", Toast.LENGTH_LONG).show();

            }
        });

        if(savedInstanceState != null) {
            tvJoke.setText(savedInstanceState.getString(JOKE));
        }
        else {
            if(!isNetworkAvailable()) {
                tvJoke.setText("Network not available.");

            }


        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        String joke = tvJoke.getText().toString();
        outState.putString(JOKE, joke);

    }

    private static String inputStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()));

        // Read response until the end
        try {
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Return full string

        return total.toString();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    private String GET(String url) {
        InputStream inputStream = null;
        String result = "";

        if(!isNetworkAvailable()) {
            return "Network not available";
        }

        try {

            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse httpResponse = httpClient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null) {
                return inputStreamToString(inputStream);
            }
            else {
                result = "Error getting joke";
            }

        } catch (Exception e) {

            if(httpAsyncTask != null) {
                httpAsyncTask.cancel(true);

                result = "Error getting joke";

            }

            e.printStackTrace();
        }

        return result;
    }

    private void updateTextView(String text) {
        if(tvJoke != null) {
            Gson gson = new Gson();
            JSONJoke jsonJoke = new JSONJoke();

            //We put this in a try block in case the text we get
            //isn't JSON or is otherwise not formatted correctly.
            //In that case, we just show the raw text instead of
            //attempting to create a JSONJoke object from it.
            try {
                jsonJoke = gson.fromJson(text, jsonJoke.getClass());
                tvJoke.setText(jsonJoke.getValue().getJoke());
            }
            catch (Exception e) {
                //Just set tvJoke to the raw text.
                tvJoke.setText(text);
            }
        }
    }

    public native boolean isPrime(int n);


    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            return GET(urls[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            updateTextView(s);
        }
    }
}
