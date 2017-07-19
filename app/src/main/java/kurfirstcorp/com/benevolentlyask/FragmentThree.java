package kurfirstcorp.com.benevolentlyask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import static kurfirstcorp.com.benevolentlyask.Constant.C_ASK;
import static kurfirstcorp.com.benevolentlyask.Constant.C_ASKID;
import static kurfirstcorp.com.benevolentlyask.Constant.C_COUNT;
import static kurfirstcorp.com.benevolentlyask.Constant.C_DATE;
import static kurfirstcorp.com.benevolentlyask.Constant.C_ID;
import static kurfirstcorp.com.benevolentlyask.Constant.C_ISMYID;
import static kurfirstcorp.com.benevolentlyask.Constant.C_RECEIVED;
import static kurfirstcorp.com.benevolentlyask.Constant.C_USERNAME;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_password;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_AUTH_user;
import static kurfirstcorp.com.benevolentlyask.Init.C_SERVER_PROD;
/**
 * Created by urdragon2 on 3/25/17.
 */

public class FragmentThree extends ListFragment implements AdapterView.OnItemClickListener {
    View view;
    SharedPreferences prefs;
    String myid;
    boolean flag_loading = false;
    boolean first_focus = false;
    View loadMoreView;
    View emptyView;
    private ArrayList<HashMap> list;
    ListView lview;
    listviewProximityAdapter adapter;
    Context context;
    SwipeRefreshLayout mSwipeRefreshLayout;
    int myposition;
    ArrayAdapter<String> adapterspinner;
    String myaskid;
    String believechoice;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment

        view = inflater.inflate(
                R.layout.fragment_three, container, false);
        prefs = getActivity().getSharedPreferences(
                "kurfirstcorp.com.benevolentlyask", Context.MODE_PRIVATE);
        myid = prefs.getString("kurfirstcorp.com.benevolentlyask.id", "defaultvalue");
        ((LandingActivity) getActivity()).myoffset = "0";
        ((LandingActivity) getActivity()).mylimit = "10";
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        list = new ArrayList<HashMap>();
        final ListView lview = (ListView) view.findViewById(android.R.id.list);
        loadMoreView = ((LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.loadmore, null, false);
        emptyView = ((LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.empty, null, false);
        lview.addFooterView(loadMoreView);
        adapter = new listviewProximityAdapter(context, getActivity(), list);
        lview.setAdapter(adapter);
        lview.setEmptyView(view.findViewById(android.R.id.empty));
        TextView header = (TextView) emptyView.findViewById(R.id.header_1);
        header.setText("No new asks");
        getActivity().setTitle("My Asks");
        lview.setOnScrollListener(new AbsListView.OnScrollListener() {

            public void onScrollStateChanged(AbsListView view, int scrollState) {


            }

            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem + visibleItemCount == totalItemCount && !(flag_loading) && (first_focus)) {
                    int myoffsetint = Integer.parseInt(((LandingActivity) getActivity()).myoffset) + Integer.parseInt(((LandingActivity) getActivity()).mylimit);
                    ((LandingActivity) getActivity()).myoffset = String.valueOf(myoffsetint);
                    new populateList().execute();
                }
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Fragment fr;
                fr = new FragmentOne();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.commit();
                getActivity().setTitle("Benevolently Ask");
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //populateList();
        new populateList().execute();
    }


    private class populateList extends AsyncTask<Void, Void, String>

    {
        private ProgressDialog pd = new ProgressDialog(getActivity());
        List<List<String>> allitems = new ArrayList<List<String>>();

        protected void onPreExecute() {
            super.onPreExecute();
            pd.setMessage("Wait search is being filled!");
            pd.show();
            //System.out.println("aa =" + "uploadingimage");
        }

        @Override
        protected String doInBackground(Void... params) {

            try {
                //URL url = new URL(strings[0]);
                flag_loading = true;
                URL url = new URL(C_SERVER_PROD + "myasks.php");
                HttpURLConnection urlConnection =
                        (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                        .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                        .appendQueryParameter("id", myid)
                        .appendQueryParameter("limit", ((LandingActivity) getActivity()).mylimit)
                        .appendQueryParameter("offset", ((LandingActivity) getActivity()).myoffset);
                String query = builder.build().getEncodedQuery();
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                urlConnection.connect();
                //System.out.println("aa =" + query);
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(
                                urlConnection.getInputStream()));

                String next;
                while ((next = bufferedReader.readLine()) != null) {
                    JSONArray ja = new JSONArray(next);

                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jo = (JSONObject) ja.get(i);
                        ArrayList<String> items = new ArrayList<String>();
                        items.add(jo.getString("userid"));
                        items.add(jo.getString("askid"));
                        items.add(jo.getString("username"));
                        items.add(jo.getString("ask"));
                        items.add(jo.getString("date"));
                        items.add(jo.getString("counts"));
                        items.add(jo.getString("received"));
                        allitems.add(items);
                        //System.out.println("aa =" + jo.getString("myuserid"));
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
            if (allitems.isEmpty()) {
                authtext = "0";
                //authtext = items.get(0);
                //authtext = "That email does not exist in our system.";
            } else {
                authtext = "1";
                //authtext = items.get(0);
                //authtext = "New password has been sent to your email address.";
            }
            return authtext;

        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //System.out.println("aa =" + allitems.size());
            if (allitems.size() == 0) {

                lview = (ListView) view.findViewById(android.R.id.list);
                lview.removeFooterView(loadMoreView);
                if (list.isEmpty()) {
                    lview.addHeaderView(emptyView);
                }
            } else {

                for (int i = 0; i < allitems.size(); i++) {
                    HashMap temp = new HashMap();
                    temp.put(C_ID, allitems.get(i).get(0));
                    temp.put(C_ASKID, allitems.get(i).get(1));
                    temp.put(C_USERNAME, allitems.get(i).get(2));
                    temp.put(C_ASK, allitems.get(i).get(3));
                    temp.put(C_DATE, allitems.get(i).get(4));
                    temp.put(C_COUNT, allitems.get(i).get(5));
                    temp.put(C_RECEIVED, allitems.get(i).get(6));
                    list.add(temp);
                }

                adapter.notifyDataSetChanged();
                flag_loading = false;

            }
            first_focus = true;
            pd.hide();
            pd.dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
        //System.out.println("row =" + id);
    }

    public class listviewProximityAdapter extends BaseAdapter {
        public ArrayList<HashMap> list;
        Activity activity;
        Context context;
        SharedPreferences prefs;
        String myid;
        Context mycontext;

        public listviewProximityAdapter(Context context, Activity activity, ArrayList<HashMap> list) {
            super();
            this.activity = activity;
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        private class ViewHolder {
            TextView txtUsername;
            TextView txtAsk;
            TextView txtDate;
            TextView txtCount;
            Button Ibelieve;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            // TODO Auto-generated method stub
            final ViewHolder holder;
            LayoutInflater inflater = activity.getLayoutInflater();

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.myasks_layout, null);
                prefs = activity.getSharedPreferences(
                        "kurfirstcorp.com.benevolentlyask", Context.MODE_PRIVATE);
                myid = prefs.getString("kurfirstcorp.com.benevolentlyask.id", "defaultvalue");

                holder = new ViewHolder();
                holder.txtUsername = (TextView) convertView.findViewById(R.id.cusername);
                holder.txtAsk = (TextView) convertView.findViewById(R.id.cask);
                holder.txtDate = (TextView) convertView.findViewById(R.id.cdate);
                holder.txtCount = (TextView) convertView.findViewById(R.id.ccount);
                holder.Ibelieve = (Button) convertView.findViewById(R.id.ibelieve);


                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final String curid;
            final String user_name;
            final HashMap map = list.get(position);
            holder.txtUsername.setText(map.get(C_USERNAME).toString());
            holder.txtDate.setText(map.get(C_DATE).toString());
            holder.txtCount.setText(map.get(C_COUNT).toString() + " Believes");
            user_name = map.get(C_USERNAME).toString();
            holder.txtAsk.setText(map.get(C_ASK).toString());
            curid = map.get(C_ID).toString();
            //System.out.println("aa =" + map.get(C_ISMYID).toString());
            //System.out.println("aa =" + myid);
            if (map.get(C_RECEIVED).toString().equals("1")) {
                holder.Ibelieve.setTextColor(Color.parseColor("#000000"));
            } else {
                holder.Ibelieve.setTextColor(Color.parseColor("#007aff"));
            }
            holder.Ibelieve.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (holder.Ibelieve.getCurrentTextColor() == Color.parseColor("#007aff")) {
                        believechoice = "1";
                        holder.Ibelieve.setTextColor(Color.parseColor("#000000"));
                        myaskid = map.get(C_ASKID).toString();
                        new ibelieveaction().execute();
                    } else {
                        believechoice = "0";
                        holder.Ibelieve.setTextColor(Color.parseColor("#007aff"));
                        myaskid = map.get(C_ASKID).toString();
                        new ibelieveaction().execute();
                    }
                    //new ibelieveaction().execute();

                }
            });
            myposition = position;

            return convertView;
        }


        public class ibelieveaction extends AsyncTask<String, Void, String> {
            ArrayList<String> items = new ArrayList<String>();

            protected void onPreExecute() {
                super.onPreExecute();
                //System.out.println("aa =" + "uploadingimage");
            }

            @Override
            protected String doInBackground(String... strings) {

                try {
                    //URL url = new URL(strings[0]);
                    URL url = new URL(C_SERVER_PROD + "receivedaction.php");
                    HttpURLConnection urlConnection =
                            (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("POST");

                    urlConnection.setDoInput(true);
                    urlConnection.setDoOutput(true);

                    Uri.Builder builder = new Uri.Builder()
                            .appendQueryParameter("auth_user", C_SERVER_AUTH_user)
                            .appendQueryParameter("auth_password", C_SERVER_AUTH_password)
                            .appendQueryParameter("id", myid)
                            .appendQueryParameter("myaskid", myaskid)
                            .appendQueryParameter("believechoice", believechoice);
                    String query = builder.build().getEncodedQuery();
                    System.out.println("aa =" + query);
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
                            //System.out.println("aa =" + jo.getString("id"));
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
                    authtext = "unsuccessful";
                } else {

                    authtext = "successful";
                }
                return authtext;
            }

            protected void onPostExecute(String authtext) {
                if (!authtext.equals("unsuccessful")) {

                } else {
                    AlertDialog alertDialog2 = new AlertDialog.Builder(activity).create();
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




    }
}