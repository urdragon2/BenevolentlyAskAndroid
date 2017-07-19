package kurfirstcorp.com.benevolentlyask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_password;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_user;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_PROD;

/**
 * Created by urdragon2 on 3/25/17.
 */

public class LandingActivity extends AppCompatActivity {
    SharedPreferences prefs;
    String myid;
    public String backfrag="";
    public String myfragment="";
    public String mylimit="10";
    public String myoffset="0";
    public String myask = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_place, new FragmentOne())
                    .commit();
        }
        LandingActivity.this.setTitle("ME");
        prefs = getSharedPreferences(
                "kurfirstcorp.com.benevolentlyask", Context.MODE_PRIVATE);
        myid = prefs.getString("kurfirstcorp.com.benevolentlyask.id", "defaultvalue");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
// Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_item:
                View viewDial;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflaterDial = LayoutInflater.from(this);
                viewDial = inflaterDial.inflate(R.layout.askdialog, null);
                builder.setView(viewDial);
                final EditText asktext = (EditText) viewDial.findViewById(R.id.asktext);
                builder.setTitle("ASK");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("Ask and Believe", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        myask = asktext.getText().toString();
                        new InsertAsk().execute();

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class InsertAsk extends AsyncTask<Void,Void,String>
    {
        ArrayList<String> items = new ArrayList<String>();
        private ProgressDialog pd = new ProgressDialog(LandingActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait in need is being filled!");
            pd.show();
            //System.out.println("aa =" + "uploadingimage");
        }
        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(C_SERVER_PROD+"insertask.php");
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("id", myid)
                        .appendQueryParameter("myask", myask);
                String query = builder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();
                System.out.println("aa =" + query);
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream()));

                String next;
                while ((next = bufferedReader.readLine()) != null){
                    JSONArray ja = new JSONArray(next);

                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        items.add(jo.getString("id"));
                    }
                }
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            String authtext;
            if (!items.isEmpty())
            {
                //authtext = "Login successful";
                authtext = items.get(0);
            }
            else {
                authtext = "unsuccessful";
            }
            return authtext;

        }
        protected void onPostExecute(String authtext){
            if (!authtext.equals("unsuccessful"))
            {
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(LandingActivity.this);
                alertDialog2.setTitle("You asked.");
                alertDialog2.setMessage("Believe and Receive.");
                alertDialog2.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_place, new FragmentOne())
                                .commit();
                        dialog.dismiss();
                    }
                });
                alertDialog2.show();
            }
            else
            {
                AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(LandingActivity.this);
                alertDialog2.setTitle("Error");
                alertDialog2.setMessage("Please try again.");
                alertDialog2.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog2.show();
            }
            pd.hide();
            pd.dismiss();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public void onBackPressed() {

        /*if (myfragment == "FragmentProfile" && backfrag == "Fragment3") {
            super.onBackPressed();
            Fragment fr = new FragmentThree();
            FragmentManager fm = this.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_place, fr);
            fragmentTransaction.commit();
        } else {
            super.onBackPressed();
        }*/

    }

}
