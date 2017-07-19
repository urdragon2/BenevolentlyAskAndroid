package kurfirstcorp.com.benevolentlyask;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by urdragon2 on 3/25/17.
 */

public class TempFragment extends Fragment {
    View view;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        //Inflate the layout for this fragment

        view = inflater.inflate(
                R.layout.fragment_temp, container, false);
        return view;
    }
}
