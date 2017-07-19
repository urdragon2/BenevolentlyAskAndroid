package kurfirstcorp.com.benevolentlyask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_password;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_user;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_PROD;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    EditText Login;
    EditText Password;
    Button signin;
    Button button2;
    String logintext;
    String passwordtext;
    Button emailBtn;
    String myemail;
    String mypassword;
    CheckBox mycheckbox;
    String token;
    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        prefs = this.getSharedPreferences(
                "kurfirstcorp.com.benevolentlyask", Context.MODE_PRIVATE);
        myemail = prefs.getString("kurfirstcorp.com.benevolentlyask.login", "defaultvalue");
        mypassword = prefs.getString("kurfirstcorp.com.benevolentlyask.password", "defaultvalue");
        Login = (EditText)findViewById(R.id.Login);
        Password = (EditText)findViewById(R.id.Password);
        mycheckbox = (CheckBox)findViewById(R.id.checkBox);
        token = FirebaseInstanceId.getInstance().getToken();
        if (!myemail.equals("defaultvalue") && !mypassword.equals("defaultvalue")) {
            Login.setText(myemail);
            Password.setText(mypassword);
            mycheckbox.setChecked(true);
        }
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mynewUser();
            }
        });
        emailBtn = (Button) findViewById(R.id.button);
        emailBtn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                // TODO Auto-generated method stub
                Login = (EditText)findViewById(R.id.Login);
                logintext = Login.getText().toString();
                TextView Auth = (TextView)findViewById(R.id.Auth);

                if (!logintext.equals("")) {
                    String passwordmash;
                    String[] chars = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
                    String[] nums ={"0","1","2","3","4","5","6","7","8","9"};
                    String[] spcls = {"!","@","#","$","%"};
                    passwordmash =chars[(int) (Math.random() * 24)] + chars[(int) (Math.random() * 24)] + chars[(int) (Math.random() * 24)] + chars[(int) (Math.random() * 24)] + chars[(int) (Math.random() * 24)] + nums[(int) (Math.random() * 9)]+spcls[(int) (Math.random() * 5)];
                    //System.out.println("password mash =" + passwordmash);
                    prefs.edit().putString("kurfirstcorp.com.benevolentlyask.password", passwordmash).commit();
                    new sendEmail(Auth).execute(logintext + "|" + passwordmash);
                    //System.out.println("Sending mail");
                }else {
                    Login.setError("Please enter your email to have a temporary password sent to you.");
                }

            }
        });

        signin = (Button)findViewById(R.id.signin);

        signin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Login = (EditText)findViewById(R.id.Login);
                Password = (EditText)findViewById(R.id.Password);
                logintext = Login.getText().toString();
                passwordtext = Password.getText().toString();

                TextView Auth = (TextView)findViewById(R.id.Auth);
                new LoginJson(Auth).execute(logintext + "|" + passwordtext);

            }
        });

    }

    protected void mynewUser(){
        Intent inent = new Intent(this, NewUserActivity.class);
        startActivity(inent);
    }

    private class sendEmail extends AsyncTask<String,Void,String> {
        private TextView Auth;

        public sendEmail(TextView Auth) {
            this.Auth = Auth;
        }

        ArrayList<String> items = new ArrayList<String>();

        @Override
        protected String doInBackground(String... strings) {
            try {
                //URL url = new URL(strings[0]);
                URL url = new URL(C_SERVER_PROD+"forgotemail.php");
                String params = strings[0];
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                String mylogin ="";
                String mypassword ="";
                String clearpassword ="";
                if(!params.equals("|")) {
                    StringTokenizer tokens = new StringTokenizer(params, "|");
                    mylogin = tokens.nextToken();
                    mypassword = tokens.nextToken();
                    clearpassword=mypassword;
                    mypassword = myhash(mypassword);
                    //System.out.println("encrypted passord =" + mypassword);
                }
                urlConnection.setRequestMethod("POST");

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("login", mylogin)
                        .appendQueryParameter("password", mypassword)
                        .appendQueryParameter("clearpassword", clearpassword);
                String query = builder.build().getEncodedQuery();

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
                while ((next = bufferedReader.readLine()) != null) {
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
            if (items.isEmpty()) {
                authtext = "0";
                //authtext = items.get(0);
                authtext = "That email does not exist in our system.";
            } else {
                //authtext = "1";
                //authtext = items.get(0);
                authtext = "New password has been sent to your email address.";
            }
            return authtext;

        }
        protected void onPostExecute(String authtext){
            Auth.setText(authtext);
        }

    }
    private class LoginJson extends AsyncTask<String,Void,String>
    {
        private TextView Auth;
        public LoginJson(TextView Auth){
            this.Auth = Auth;
        }
        private ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        ArrayList<String> items = new ArrayList<String>();
        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait getting your data!");
            pd.show();
            //System.out.println("aa =" + "uploadingimage");
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(C_SERVER_PROD+"auth.php");
                String params = strings[0];
                //System.out.println("aa =" + params);
                String mylogin ="";
                String mypassword ="";
                if(!params.equals("|")) {
                    StringTokenizer tokens = new StringTokenizer(params, "|");
                    mylogin = tokens.nextToken();
                    mypassword = tokens.nextToken();
                    prefs.edit().putString("kurfirstcorp.com.benevolentlyask.password", mypassword).commit();
                    mypassword = myhash(mypassword);
                    prefs.edit().putString("kurfirstcorp.com.benevolentlyask.login", mylogin).commit();
                    //System.out.println("aa =" + mypassword);
                }
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("login", mylogin)
                        .appendQueryParameter("password", mypassword)
                        .appendQueryParameter("device", "Android")
                        .appendQueryParameter("mytoken", token);
                String query = builder.build().getEncodedQuery();
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
                        items.add(jo.getString("username"));
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
                prefs.edit().putString("kurfirstcorp.com.benevolentlyask.id", items.get(1)).commit();
            }
            else {
                authtext = "Login and or password are incorrect";
            }
            return authtext;

        }
        protected void onPostExecute(String authtext){
            if (!authtext.equals("Login and or password are incorrect"))
            {
                UserLanding();
            }
            else
            {
                Auth.setText(authtext);
            }
            pd.hide();
            pd.dismiss();
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
