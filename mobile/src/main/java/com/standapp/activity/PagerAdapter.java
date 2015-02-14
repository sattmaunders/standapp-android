package com.standapp.activity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.astuetz.PagerSlidingTabStrip;
import com.standapp.R;
import com.standapp.fragment.GraphingCardFragment;
import com.standapp.fragment.SuperAwesomeCardFragment;

/**
 * Created by John on 2/9/2015.
 */
public class PagerAdapter extends FragmentPagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

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
    public int getPageIconResId(int position) {
        int data=0;
        switch (position) {
            case 0:
                data = R.drawable.walking;
                break;
            case 1:
                data = R.drawable.standing;
                break;
            case 2:
                data = R.drawable.breaks;
        }

        return data;

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
