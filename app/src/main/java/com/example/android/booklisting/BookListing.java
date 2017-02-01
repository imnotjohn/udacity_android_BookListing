package com.example.android.booklisting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class BookListing extends AppCompatActivity {

    private EditText mURLtext;
    private TextView mStatusTextView;

    //JSON container string
    private String mStrJson = "";
    private String mParsedTitle = "";
    private String mParsedAuthors = "";
    private JSONObject mParsedVolumeInfo;
    private JSONArray mParsedAuthorsArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_listing);

        mURLtext = (EditText) findViewById(R.id.url_text);
        mStatusTextView = (TextView) findViewById(R.id.status_text_view);
    }

    // When user clicks button, calls AsyncTask.
    // Before attempting to fetch the URL, makes sure that there is a network connection.
    public void myClickHandler(View view) {
        // Get URL from textField:
        String stringURL = mURLtext.getText().toString();
        // Check for network availability
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            // fetch data
            new DownloadWebPageTask().execute(stringURL);
        } else {
            // display error
            mStatusTextView.setText(R.string.connection_error);
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebPageTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }

        // onPostExecute displays the result of the AsyncTask
        @Override
        protected void onPostExecute(String result) {
            mStatusTextView.setText(R.string.validJSON);

            ArrayList<BookListBuilderClass> bookListBuilderArrayList = new ArrayList<>();
            BookListBuilderAdapterClass bookListBuilderAdapterClass = new BookListBuilderAdapterClass(BookListing.this, 0, bookListBuilderArrayList);
            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(bookListBuilderAdapterClass);

            //emptyView
            View view = getLayoutInflater().inflate(R.layout.empty_view, null);
            ViewGroup viewGroup= (ViewGroup)listView.getParent();
            viewGroup.addView(view);

            mStrJson = result;
            Log.i("Result", "onPostExecute: " + mStrJson);
            if (isJsonStrValid(mStrJson) && result != "") {
                try {
                    JSONObject jsonRootObject = new JSONObject(mStrJson);
                    Log.i("jsonArray", "***jsonRootObject: " + jsonRootObject);
                    JSONArray jsonArray = jsonRootObject.optJSONArray("items");
                    Log.i("jsonArray", "\n\n***jsonArray " + jsonArray);

                    //convert jsonArray to jsonObject to allow for iteration:
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                            for (Iterator<String> keysIterator = jsonObject.keys(); keysIterator.hasNext(); ) {
                                String keys = keysIterator.next();
                                Log.i("jsonObjectIterator", "Keys: " + jsonObject.get(keys) + "\n\t***Keys: " + keys);

                                Log.i("jsonArrayKeys", "jsonArray --> jsonOjbect.toString: " + keys);
                                if (keys.equals("volumeInfo")) {
                                    mParsedVolumeInfo = jsonObject.getJSONObject("volumeInfo");
                                    mParsedTitle = mParsedVolumeInfo.optString("title");
                                    mParsedAuthorsArray = mParsedVolumeInfo.optJSONArray("authors");
                                    Log.i("jsonArrayParsed", "\n\t***ParsedJsonArrays -- volumeInfo: " + mParsedVolumeInfo + "\n\t***ParsedJsonArrays -- mParsedTitle: " + mParsedTitle + "\n\t***ParsedJsonArrays -- mParseDAuthors: " + mParsedAuthorsArray);

                                    // Build String mParsedAuthors with elements from mParsedAuthorsArray:
                                    for (int j = 0; j < mParsedAuthorsArray.length(); j++) {
                                        if (mParsedAuthorsArray.length() == 0) {
                                            mParsedAuthors = "No Author Provided";
                                        } else if (mParsedAuthorsArray.length() - 1 == j) {
                                            mParsedAuthors += mParsedAuthorsArray.optString(j);
                                        } else {
                                            mParsedAuthors += mParsedAuthorsArray.optString(j) + ", ";
                                        }
                                    }
                                    bookListBuilderArrayList.add(new BookListBuilderClass(mParsedTitle, mParsedAuthors));
                                }
                            }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                mStatusTextView.setText(R.string.invalidJSON);
                listView.setEmptyView(view); //emptyView
            }

        }
    }

    // Check whether or not JSON String input is valid
    public boolean isJsonStrValid(String testString) {
        mParsedAuthors = "";
        mParsedTitle = "";
        try {
            new JSONObject(testString);
        } catch (JSONException ex) {
            try {
                new JSONArray(testString);
            } catch (JSONException ex1) {
                return false;
            }
        }   return true;
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;

        String apiKey = "AIzaSyB15mtI5E0UNh_ONSCCYw-B9YcwV3w_U_8";
        String requestURLstring = "https://www.googleapis.com/books/v1/volumes" + "?q=" + myurl;
        try {
            URL url = new URL(requestURLstring);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000/*milliseconds*/);
            conn.setConnectTimeout(15000/*milliseconds*/);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            //Starts the Query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("HTTP Test", "The response is " + response);
            is = conn.getInputStream();

            StringBuffer buffer = new StringBuffer();
            if(is == null) {
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                //adding new line mark
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                //stream was empty
                return null;
            }
            return buffer.toString();

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            return null;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
}