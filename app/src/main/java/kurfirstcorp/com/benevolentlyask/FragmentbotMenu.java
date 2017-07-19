package kurfirstcorp.com.benevolentlyask;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by urdragon2 on 3/25/17.
 */
public class FragmentbotMenu extends Fragment {
    ImageButton mebutton;
    ImageButton rateButton;
    ImageButton searchButton;
    ImageButton recieveButton;
    SharedPreferences prefs;
    String myid;
    Fragment fr;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bot_menu,
                container, false);
        prefs = getActivity().getSharedPreferences(
                "kurfirstcorp.com.benevolentlyask", Context.MODE_PRIVATE);
        myid = prefs.getString("kurfirstcorp.com.benevolentlyask.id", "defaultvalue");
        mebutton= (ImageButton) view.findViewById(R.id.meButton);
        mebutton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                fr = new FragmentTwo();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                getActivity().setTitle("My Preferences");
            }
        });

        rateButton= (ImageButton) view.findViewById(R.id.rateButton);
        rateButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                fr = new FragmentOne();
                ((LandingActivity) getActivity()).backfrag = "";
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                getActivity().setTitle("Believes");
            }
        });

        searchButton= (ImageButton) view.findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                fr = new FragmentThree();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                getActivity().setTitle("My Asks");

            }
        });

        recieveButton= (ImageButton) view.findViewById(R.id.receiveButton);
        recieveButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                fr = new FragmentFour();
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_place, fr);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                getActivity().setTitle("Receives");

            }
        });
        return view;
    }
}
