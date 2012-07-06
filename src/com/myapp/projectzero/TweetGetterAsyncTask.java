package com.myapp.projectzero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class TweetGetterAsyncTask extends AsyncTask<String, String, String>{
	
	// setup vars for activity, dialog and context
	private Activity activity;
	private ProgressDialog dialog;
	private Context context;
	boolean appendnew = false;	
	private AsyncTaskCompleteListener<String> listener;
	
	public TweetGetterAsyncTask(AsyncTaskCompleteListener<String> listener, Activity activity, boolean appendnew){
		this.activity = activity;
		this.context = activity;
		this.dialog = new ProgressDialog(context);		
		this.listener = listener;
		this.appendnew = appendnew;
	}
	
	@Override
	protected String doInBackground(String... urls) {			
    	// set up required http request objects
        HttpClient httpclient = new DefaultHttpClient();
        String responseString = null;
        
        try {
        	HttpGet httpget = new HttpGet(urls[0]);
        	HttpResponse execute = httpclient.execute(httpget);
            StatusLine statusLine = execute.getStatusLine();
            	            
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            	
            	// get the http entity?
            	HttpEntity entity = execute.getEntity();
            	
            	// set response string to the response of the inputStreamToString
            	responseString = inputStreamToString(entity.getContent()).toString();	            	            		            	

            } else{
                //Closes the connection.
                execute.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }catch (IllegalStateException e){
        	e.printStackTrace();
        }
        
        return responseString;
	}
	
    // convert the input stream to a string
    private StringBuilder inputStreamToString(InputStream inputstream) throws IOException {
        StringBuilder sbuilder = new StringBuilder();
        String line = "";
        
        // wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputstream));

        // read response until the end
        while ((line = rd.readLine()) != null) { 
        	sbuilder.append(line); 
        }
        
        // return full string
        return sbuilder;
    }		
	
    @Override
    protected void onPreExecute() {
    	// show loader
        this.dialog.setMessage(!appendnew ? "Loading tweets..." : "Loading more tweets...");
        this.dialog.show();
    };    
    
	@Override
	protected void onPostExecute(String result) {
        
		// hide loader
        if (dialog.isShowing()) {
            dialog.dismiss();
        }		
		
		listener.onTaskComplete(result);
	}
	
    @Override
    protected void onCancelled() {
        // hide loader in case of async task being cancelled. on post execute may not run and everything will break!
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }	
}
