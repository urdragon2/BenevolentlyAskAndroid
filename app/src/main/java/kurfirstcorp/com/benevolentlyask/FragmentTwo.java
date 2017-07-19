package kurfirstcorp.com.benevolentlyask;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by urdragon2 on 3/25/17.
 */
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_password;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_user;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_PROD;

public class FragmentTwo extends Fragment {
    View view;
    EditText editusername;
    EditText editpassword;
    EditText editemail;
    boolean aa = false;
    boolean ab = false;
    boolean ac = false;
    ImageView imgView1a;
    ImageView imgView1b;
    ImageView imgView2a;
    ImageView imgView2b;
    ImageView imgView3a;
    ImageView imgView3b;
    String usernametext;
    String passwordtext;
    String emailtext;
    String oldemail;
    Button Update;
    EditText inserted;
    SharedPreferences prefs;
    String myemail;
    String myid;
    String mypassword;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two,
                container, false);
        getActivity().setTitle("My Preferences");
        Update = (Button) view.findViewById(R.id.signup);
        prefs = getActivity().getSharedPreferences(
                "kurfirstcorp.com.benevolentlyask", Context.MODE_PRIVATE);
        myemail = prefs.getString("kurfirstcorp.com.benevolentlyask.login", "defaultvalue");
        myid = prefs.getString("kurfirstcorp.com.benevolentlyask.id", "defaultvalue");
        mypassword = prefs.getString("kurfirstcorp.com.benevolentlyask.password", "defaultvalue");
        editusername = (EditText) view.findViewById(R.id.UserName);
        editpassword = (EditText) view.findViewById(R.id.Password);
        editpassword.setHint(Html.fromHtml("<small><small>" +
                editpassword.getHint() + "</small><small>"));
        editemail = (EditText) view.findViewById(R.id.email);
        imgView1a = (ImageView) view.findViewById(R.id.good1);
        imgView1b = (ImageView) view.findViewById(R.id.bad1);
        imgView2a = (ImageView) view.findViewById(R.id.good2);
        imgView2b = (ImageView) view.findViewById(R.id.bad2);
        imgView3a = (ImageView) view.findViewById(R.id.good3);
        imgView3b = (ImageView) view.findViewById(R.id.bad3);
        new getData().execute();
        Update = (Button) view.findViewById(R.id.save);
        Update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                validator();
            }
        });
        return view;
    }

    private class getData extends AsyncTask<Void, Void, String> {
        private ProgressDialog pd = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait data is loading!");
            pd.show();
            //System.out.println("aa =" + "uploadingimage");
        }

        ArrayList<String> items = new ArrayList<String>();

        @Override
        protected String doInBackground(Void... strings) {
            try {
                //URL url = new URL(strings[0]);
                URL url = new URL(C_SERVER_PROD + "getdata.php");
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()

                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("id", myid);
                String query = builder.build().getEncodedQuery();
                //System.out.println("bb =" + query);
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
                        items.add(jo.getString("username"));
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
            } else {
                //authtext = "1";
                authtext = items.get(0);
            }
            return authtext;

        }

        protected void onPostExecute(String authtext) {
            editusername.setText(items.get(0));
            editemail.setText(items.get(1));
            oldemail = items.get(1);
            pd.hide();
            pd.dismiss();
        }


    }


    private boolean isValidUserName(String username) {
        String USERNAME_PATTERN = "^[0-9A-Za-z- ]*$";
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
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

    public void validator() {
        usernametext = editusername.getText().toString();
        passwordtext = editpassword.getText().toString();
        emailtext = editemail.getText().toString();
        String emailsame = "";
        if (!usernametext.equals("") && isValidUserName(usernametext)) {
            imgView1a.setVisibility(View.VISIBLE);
            imgView1b.setVisibility(View.INVISIBLE);
            aa = true;
        } else {
            imgView1b.setVisibility(View.VISIBLE);
            imgView1a.setVisibility(View.INVISIBLE);
            editusername.setError("Must be alphabetic, numbers, dashes, and spaces");
        }

        if (!passwordtext.equals("")) {
            if (isValidPassword(passwordtext)) {
                ab = true;
                imgView2a.setVisibility(View.VISIBLE);
                imgView2b.setVisibility(View.INVISIBLE);
            } else {
                imgView2b.setVisibility(View.VISIBLE);
                imgView2a.setVisibility(View.INVISIBLE);
                editpassword.setError("Must be alphabetic, one number, one special char !@#$% and be over 6 characters ");
            }
        } else {
            ab = true;
        }

        if (!emailtext.equals("") && isValidEmail(emailtext)) {
            //String url = String.format("http://mobiapi.kurfirstcorp.com/digibizcard/checkuseremail.php?email=%s",
            //      emailtext);
            if (!oldemail.equals(emailtext)) {
                new CheckUserJson(inserted).execute(emailtext);
            } else {
                emailsame = "yes";
            }
        } else {
            imgView3b.setVisibility(View.VISIBLE);
            imgView3a.setVisibility(View.INVISIBLE);
            editemail.setError("Invalid Email");
        }

        if (emailsame.equals("yes")) {

            String url = usernametext + "|";
            if (passwordtext.equals("")) {
                passwordtext = mypassword;
            }
            url = url + emailtext;
            //System.out.println("aa =" +  url);
            //System.out.println("aa =" +  aa + " " + ab + " " + ac + " " + ad + " " + ae + " " + af + " " + ah + " " + ai + " " + aj + " " + ak + " " + al + " " + am + " " + an+ " " + ao);
            if (aa && ab) {

                new UpdateUserJson(inserted).execute(url);
                //System.out.println("aa =" + "here I am");
            }
        }
    }

    private class CheckUserJson extends AsyncTask<String, Void, String> {
        private TextView inserted;

        public CheckUserJson(TextView inserted) {
            this.inserted = inserted;
        }

        ArrayList<String> items = new ArrayList<String>();

        @Override
        protected String doInBackground(String... strings) {
            try {
                //URL url = new URL(strings[0]);
                URL url = new URL(C_SERVER_PROD + "checkuseremail.php");
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
            } else {
                //authtext = "1";
                authtext = items.get(0);
            }
            return authtext;

        }

        protected void onPostExecute(String authtext) {
            if (authtext.equals("0")) {
                usernametext = editusername.getText().toString();
                passwordtext = editpassword.getText().toString();

                emailtext = editemail.getText().toString();
                ac = true;
                imgView3a.setVisibility(View.VISIBLE);
                imgView3b.setVisibility(View.INVISIBLE);
                String url = usernametext + "|";
                if (passwordtext.equals("")) {
                    passwordtext = mypassword;
                }
                url = url + emailtext;
                //System.out.println("aa =" +  url);
                //System.out.println("aa =" +  aa + " " + ab + " " + ac + " " + ad + " " + ae + " " + af + " " + ah + " " + ai + " " + aj + " " + ak + " " + al + " " + am + " " + an+ " " + ao);
                if (aa && ab && ac) {

                    new UpdateUserJson(inserted).execute(url);

                }
            } else {
                imgView3b.setVisibility(View.VISIBLE);
                imgView3a.setVisibility(View.INVISIBLE);
                editemail.setError("Email already in use. Please choose another.");
            }
        }


    }

    private class UpdateUserJson extends AsyncTask<String, Void, String> {
        private TextView Inserted;
        String thispassword = "";

        public UpdateUserJson(TextView Inserted) {
            this.Inserted = Inserted;
        }

        Fragment fr;
        ArrayList<String> items = new ArrayList<String>();

        @Override
        protected String doInBackground(String... strings) {
            try {

                URL url = new URL(C_SERVER_PROD + "updateuser.php");
                String params = strings[0];
                //System.out.println("aa =" + params);
                String myusername = "";
                String myemail = "";
                myusername = usernametext;
                thispassword = passwordtext;
                if (!thispassword.equals(mypassword)) {
                    prefs.edit().putString("kurfirstcorp.com.benevolentlyask.password", thispassword).commit();
                    thispassword = myhash(thispassword);
                } else {
                    thispassword = mypassword;
                    thispassword = myhash(thispassword);
                }
                myemail = emailtext;
                prefs.edit().putString("kurfirstcorp.com.benevolentlyask.login", myemail).commit();
                //System.out.println("aa =" + mylogin);
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("id", myid)
                        .appendQueryParameter("username", myusername)
                        .appendQueryParameter("password", thispassword)
                        .appendQueryParameter("email", myemail);
                String query = builder.build().getEncodedQuery();
                //System.out.println("aa =" + query);
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
            if (!items.isEmpty()) {
                //authtext = "Login successful";
                authtext = items.get(0);
                //prefs.edit().putString("com.kurfirstcorp.digibizcard.id", items.get(0)).commit();
            } else {
                authtext = "unsuccessful";
            }
            return authtext;

        }

        protected void onPostExecute(String authtext) {
            if (!authtext.equals("unsuccessful")) {
                Fragment fr;
                fr = new FragmentTwo();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.commit();

                /*Fragment fr2;
                fr2 = new FragmentbotMenu();
                FragmentManager fm2 = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction2 = fm2.beginTransaction();
                fragmentTransaction2.replace(R.id.fragment_botmenu, fr2);
                fragmentTransaction2.commit();*/

            } else {
                AlertDialog alertDialog2 = new AlertDialog.Builder(getActivity()).create();
                alertDialog2.setTitle(authtext);
                alertDialog2.setMessage("There are problems with your update. Please try again");
                alertDialog2.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog closed
                        //Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog2.show();
            }

        }


    }

    public String myhash(String clearpassword) {

        String password = clearpassword;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            //System.out.println("Hex format : " + hexString.toString());
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }

    }

}