package com.standapp.common;

/**
 * Created by John on 2/2/2015.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Base fragment which performs injection using the activity-scoped object graph
 *
 * TODO refactor with {@link com.standapp.common.BaseFragment}
 */
public abstract class BaseActionBarFragment extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * To use injected values, we must make sure that the BaseActivity had been created
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        // Assume that it lives within a BaseActivity host
        ((BaseActionBarActivity)getActivity()).inject(this);
    }

    protected <T> T getView(int id) {
        return (T) getView().findViewById(id);
    }
}
