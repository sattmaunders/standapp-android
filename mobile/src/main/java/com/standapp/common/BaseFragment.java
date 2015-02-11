package com.standapp.common;

/**
 * Created by John on 2/2/2015.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Base fragment which performs injection using the activity-scoped object graph
 */
public abstract class BaseFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        // Assume that it lives within a BaseActivity host
        ((BaseActivity)getActivity()).inject(this);
    }

    protected <T> T getView(int id) {
        return (T) getView().findViewById(id);
    }
}
