package com.standapp.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.standapp.fragment.GraphingCardFragment;
import com.standapp.fragment.SuperAwesomeCardFragment;

/**
 * Created by John on 2/9/2015.
 */
public class PagerAdapter extends FragmentPagerAdapter {

    private final String[] TITLES = { "Steps", "Standing", "Breaks" }; //"Home",

    public PagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                System.out.println("Pager Adapter: new GraphsBaseFragment - Steps");
                return GraphingCardFragment.newInstance(position);
            case 1:
                System.out.println("Pager Adapter: new GraphsBaseFragment - Standing");
                return GraphingCardFragment.newInstance(position);
            case 2:
                System.out.println("Pager Adapter: new GraphsBaseFragment - Breaks");
                return GraphingCardFragment.newInstance(position);
            default:
                System.out.println("Pager Adapter: new SuperAwesomeCardFragment");
                return SuperAwesomeCardFragment.newInstance(position);
        }
        /*
            case 0:
                System.out.println("Pager Adapter: new SuperAwesomeCardFragment");
                return SuperAwesomeCardFragment.newInstance(position);
        */
    }

}
