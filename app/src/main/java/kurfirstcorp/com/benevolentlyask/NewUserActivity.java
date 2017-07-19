package kurfirstcorp.com.benevolentlyask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by urdragon2 on 3/25/17.
 */

import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_password;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_user;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_PROD;

public class NewUserActivity   extends AppCompatActivity {
    Button signup;
    SharedPreferences prefs;
    boolean aa = false;
    boolean ab = false;
    boolean ac = false;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_user_activity);

        prefs = this.getSharedPreferences(
                "kurfirstcorp.com.benevolentlyask", Context.MODE_PRIVATE);

        token = FirebaseInstanceId.getInstance().getToken();
        signup = (Button) findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                validator();
            }
        });
    }
    private boolean isValidUserName(String Username) {
        String USERNAME_PATTERN = "^[A-Za-z0-9- ]*$";
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(Username);
        return matcher.matches();
    }

    private boolean isValidPassword(String password) {
        String PASSWORD_PATTERN = "^(?=(.*\\d){1})(?=.*[a-zA-Z])(?=.*[!@#$%])[0-9a-zA-Z!@#$%]{6,}";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    public void validator(){

        EditText txtusername = (EditText) findViewById(R.id.UserName);
        String usernametext = txtusername.getText().toString();

        EditText txtpassword = (EditText) findViewById(R.id.Password);
        String passwordtext = txtpassword.getText().toString();

        EditText txtemail = (EditText) findViewById(R.id.email);
        String emailtext = txtemail.getText().toString();

        final ImageView imgView1a = (ImageView)findViewById(R.id.good1);
        final ImageView imgView1b = (ImageView)findViewById(R.id.bad1);
        final ImageView imgView2a = (ImageView)findViewById(R.id.good2);
        final ImageView imgView2b = (ImageView)findViewById(R.id.bad2);
        final ImageView imgView3a = (ImageView)findViewById(R.id.good3);
        final ImageView imgView3b = (ImageView)findViewById(R.id.bad3);
        if (!usernametext.equals("") && isValidUserName(usernametext)) {
            imgView1a.setVisibility(View.VISIBLE);
            imgView1b.setVisibility(View.INVISIBLE);
            aa= true;
        } else {
            imgView1b.setVisibility(View.VISIBLE);
            imgView1a.setVisibility(View.INVISIBLE);
            txtusername.setError("Must be alphabetic, dashes, and spaces");
        }

        if (!passwordtext.equals("") && isValidPassword(passwordtext)) {
            ab= true;
            imgView2a.setVisibility(View.VISIBLE);
            imgView2b.setVisibility(View.INVISIBLE);
        } else {
            imgView2b.setVisibility(View.VISIBLE);
            imgView2a.setVisibility(View.INVISIBLE);
            txtpassword.setError("Must be alphabetic, one number, one special char !@#$% and be over 6 characters ");
        }

        if (!emailtext.equals("") && isValidEmail(emailtext)) {
            TextView inserted = (TextView)findViewById(R.id.inserted);
            new CheckUserJson(inserted).execute(emailtext);
        } else {
            imgView3b.setVisibility(View.VISIBLE);
            imgView3a.setVisibility(View.INVISIBLE);
            txtemail.setError("Invalid Email");
        }


    }

    private class CheckUserJson extends AsyncTask<String,Void,String>
    {
        private TextView inserted;
        public CheckUserJson(TextView inserted){
            this.inserted = inserted;
        }

        ArrayList<String> items = new ArrayList<String>();

        @Override
        protected String doInBackground(String... strings) {
            try {
                //URL url = new URL(strings[0]);
                URL url = new URL(C_SERVER_PROD+"checkuseremail.php");
                String myemail = strings[0];
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("email", myemail);
                String query = builder.build().getEncodedQuery();

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();
                int statusCode = urlConnection.getResponseCode();
                //System.out.println("status code =" + statusCode);
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream()));

                String next;
                while ((next = bufferedReader.readLine()) != null){
                    JSONArray ja = new JSONArray(next);

                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        items.add(jo.getString("email"));
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
            if (items.isEmpty())
            {
                authtext = "0";
                //authtext = items.get(0);
            }
            else {
                //authtext = "1";
                authtext = items.get(0);
            }
            return authtext;

        }

        protected void onPostExecute(String authtext){
            final ImageView imgView3a = (ImageView)findViewById(R.id.good3);
            final ImageView imgView3b = (ImageView)findViewById(R.id.bad3);
            EditText txtemail = (EditText) findViewById(R.id.email);
            String emailtext = txtemail.getText().toString();
            if (authtext.equals("0")) {

                EditText txtusername = (EditText) findViewById(R.id.UserName);
                String usernametext = txtusername.getText().toString();

                EditText txtpassword = (EditText) findViewById(R.id.Password);
                String passwordtext = txtpassword.getText().toString();

                TextView inserted = (TextView)findViewById(R.id.inserted);
                ac= true;
                imgView3a.setVisibility(View.VISIBLE);
                imgView3b.setVisibility(View.INVISIBLE);
                String url = usernametext + "|" + passwordtext + "|" + emailtext;
                //System.out.println("aa =" +  aa);
                if (aa && ab && ac)
                {
                    new NewUserJson(inserted).execute(url);
                }
            }
            else
            {
                imgView3b.setVisibility(View.VISIBLE);
                imgView3a.setVisibility(View.INVISIBLE);
                txtemail.setError("Email already in use. Please choose another.");
            }
        }


    }

    private class NewUserJson extends AsyncTask<String,Void,String>
    {
        private TextView Inserted;
        public NewUserJson(TextView Inserted){
            this.Inserted = Inserted;
        }

        ArrayList<String> items = new ArrayList<String>();

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(C_SERVER_PROD+"newuser.php");
                String params = strings[0];
                //System.out.println("aa =" + params);
                String myusername ="";
                String mypassword ="";
                String myemail ="";
                if(!params.equals("|")) {
                    StringTokenizer tokens = new StringTokenizer(params, "|");
                    myusername = tokens.nextToken();
                    mypassword = tokens.nextToken();
                    prefs.edit().putString("kurfirstcorp.com.benevolentlyask.password", mypassword).commit();
                    mypassword = myhash(mypassword);
                    myemail = tokens.nextToken();
                    prefs.edit().putString("kurfirstcorp.com.benevolentlyask.login", myemail).commit();
                    //System.out.println("aa =" + mylogin);
                }
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("username", myusername)
                        .appendQueryParameter("password", mypassword)
                        .appendQueryParameter("email", myemail)
                        .appendQueryParameter("device", "Android")
                        .appendQueryParameter("mytoken", token);
                String query = builder.build().getEncodedQuery();
                System.out.println("Query : " + query);
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();

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
                prefs.edit().putString("kurfirstcorp.com.benevolentlyask.id", items.get(0)).commit();
            }
            else {
                authtext = "unsuccessful";
            }
            return authtext;

        }
        protected void onPostExecute(String authtext){
            if (!authtext.equals("unsuccessful"))
            {
                UserLanding();
            }
            else
            {
                AlertDialog alertDialog2 = new AlertDialog.Builder(NewUserActivity.this).create();
                alertDialog2.setTitle(authtext);
                alertDialog2.setMessage("There are problems");
                alertDialog2.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog closed
                        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog2.show();
            }

        }


    }


    protected void UserLanding(){
        Intent inent = new Intent(this, LandingActivity.class);
        startActivity(inent);
    }


    public String myhash(String clearpassword){

        String password = clearpassword;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<byteData.length;i++) {
                String hex=Integer.toHexString(0xff & byteData[i]);
                if(hex.length()==1) hexString.append('0');
                hexString.append(hex);
            }

            //System.out.println("Hex format : " + hexString.toString());
            return hexString.toString();
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }
}
