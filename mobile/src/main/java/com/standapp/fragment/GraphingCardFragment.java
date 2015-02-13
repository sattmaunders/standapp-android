package com.standapp.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.standapp.R;
import com.standapp.backend.UserInfoListener;
import com.standapp.backend.UserInfoMediator;
import com.standapp.common.BaseActionBarFragment;
import com.standapp.util.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javax.inject.Inject;
/**
 * Created by Matt on 10/02/2015. Fragment for graphs
 */
public class GraphingCardFragment extends BaseActionBarFragment implements UserInfoListener {

    private static final String ARG_POSITION = "position";

    private int position;

    @Inject
    UserInfoMediator userInfoMediator;

    private PieChart chartOne;
    private LineChart chartTwo;
    private LinearLayout graphOne;
    private LinearLayout graphTwo;

    private User user;
    private OnFragmentCreatedListener onFragmentCreatedListener;

    public static GraphingCardFragment newInstance(int position) {
        GraphingCardFragment f = new GraphingCardFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userInfoMediator.unregisterUserInfoListener(this);
    }


    @Override
    public void onActivityCreated (Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        userInfoMediator.registerUserInfoListener(this);
        onFragmentCreatedListener.onFragmentCreated();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onFragmentCreatedListener = (OnFragmentCreatedListener) activity;
        } catch (ClassCastException e) {
            // Ensure the parent activity implements this interface
            throw new ClassCastException(activity.toString() + " must implement OnFragmentCreatedListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_graphing_one, container, false);

        //Get linear layout containers:
        graphOne = (LinearLayout) rootView.findViewById(R.id.graphOne);
        graphTwo = (LinearLayout) rootView.findViewById(R.id.graphTwo);

        //TODO: charts are not redrawn if orientation is changed - fix me
        if (user != null) {
            //Set chart data
            setDataLine(user);
            setDataPie(user);
        }

        return rootView;
    }

    private void setDataLine(User user) {

        chartTwo = new LineChart(getActivity());
/*
        boolean confirmBreak, int breakFreq, int breakDur, int workStart, int workEnd,
        boolean[] workDays, int goalDailySteps, int goalDailyOnFoot, int goalDailyBreaks,
        int[] bestSteps, int[] bestOnFoot, int[] bestBreaks,
        int[] previousWeekSteps, int[] previousWeekOnFoot, int[] previousWeekBreaks,
        int[] currentWeekSteps, int[] currentWeekOnFoot, int[] currentWeekBreak,
*/
        int colorBest = Color.BLACK;
        int colorPrevious = Color.GREEN;
        int colorCurrent = Color.BLUE;


        ArrayList<String> xVals = new ArrayList<String>();      //X axis label values
        ArrayList<Entry> yVals = new ArrayList<Entry>();        //Values to display (best)
        ArrayList<Entry> yVals1 = new ArrayList<Entry>();        //Values to display (previous)
        ArrayList<Entry> yVals2 = new ArrayList<Entry>();        //Values to display (current)

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        xVals.addAll(Arrays.asList("Sun","Mon","Tue","Wed","Thur","Fri","Sat"));

        int[] bestSteps = user.getBestSteps();
        int[] previousWeekSteps = user.getPreviousWeekSteps();
        int[] currentWeekSteps = user.getCurrentWeekSteps();

        System.out.println(bestSteps.length + " | " + previousWeekSteps.length + " | " + currentWeekSteps.length);
        int index = 0;
        for (int i : bestSteps) {
            yVals.add(new Entry(i, index));
            index++;
        }
        index = 0;
        for (int i : previousWeekSteps) {
            yVals1.add(new Entry(i, index));
            index++;
        }
        index = 0;
        for (int i : currentWeekSteps) {
            yVals2.add(new Entry(i, index));
            index++;
        }

        // create a dataset and give it a type
        LineDataSet set = new LineDataSet(yVals, "Best Week");

        //set1.enableDashedLine(10f, 5f, 0f);
        set.setColor(colorBest);
        set.setCircleColor(colorBest);
        set.setLineWidth(2f);
        set.setCircleSize(4f);
        set.setFillAlpha(65);
        set.setFillColor(colorBest);
        set.setDrawCubic(true);
        set.setDrawCircles(false);
        //set.setDrawFilled(true);

        dataSets.add(set); // add the datasets

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals1, "Last Week");

        //set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(colorPrevious);
        set1.setCircleColor(colorPrevious);
        set1.setLineWidth(2f);
        set1.setCircleSize(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(colorPrevious);
        set1.setDrawCubic(true);
        set1.setDrawCircles(false);
        //set1.setDrawFilled(true);

        dataSets.add(set1); // add the datasets

        // create a dataset and give it a type
        LineDataSet set2 = new LineDataSet(yVals2, "This Week");

        //set1.enableDashedLine(10f, 5f, 0f);
        set2.setColor(colorCurrent);
        set2.setCircleColor(colorCurrent);
        set2.setLineWidth(2f);
        set2.setCircleSize(4f);
        set2.setFillAlpha(65);
        set2.setFillColor(colorCurrent);
        set2.setDrawCubic(true);
        set2.setDrawCircles(false); //draw circles for each point of data
        //set2.setDrawFilled(true); //fills underneath line

        dataSets.add(set2); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
/*
        for (LineDataSet d : data.getDataSets()){
            System.out.println("LineDataSet d entry count: " + d.getEntryCount());
            for (Entry e : d.getYVals()) {
                System.out.println("Entry in data set in data: " + e.toString());
            }
        }
*/
        // set data
        chartTwo.setData(data);

        chartTwo.setYRange(-1000,chartTwo.getYMax()+ 100,false); //Set y bounds of chart - lines get cut off with cubic stuff without this

        chartTwo.setValueTextColor(Color.BLACK);
        //chartTwo.setDescription("Week summary");
        chartTwo.setDescription("");

        chartTwo.setDragEnabled(false);
        chartTwo.setScaleEnabled(false);
        chartTwo.setTouchEnabled(false); //disables touching to get t lines
        chartTwo.fitScreen();
        //chartTwo.setScaleX(0.95f);

        // enable / disable grid lines
        chartTwo.setDrawVerticalGrid(false);
        chartTwo.setDrawHorizontalGrid(false);
        // enable / disable grid background
        chartTwo.setDrawGridBackground(false);
        chartTwo.setGridColor(Color.WHITE & 0x70FFFFFF);
        chartTwo.setGridWidth(1.25f);

        //chartTwo.setDrawUnitsInChart(false); //Useless for our purposes, for displaying things like $
        //chartTwo.setDrawYLabels(false);
        chartTwo.setDrawYValues(false);
        chartTwo.setDrawBorder(false);
        //chartTwo.setDrawXLabels(false);

        chartTwo.invalidate();
        //chartTwo.animateX(2500); //fills values in from left to right

        graphTwo.addView(chartTwo);
    }

    private void setDataPie(User user) {

        chartOne = new PieChart(getActivity());

        chartOne.setDescription("Daily goal");
        chartOne.setValueTextColor(Color.BLACK);

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();       //Values to display

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.

        ArrayList<String> xVals = new ArrayList<String>();      //Strings for labels

        int dayGoalSteps = user.getGoalDailySteps();
        int[] curWeekSteps = user.getCurrentWeekSteps();
        int curDaySteps = 0;

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);    // Sunday, day=1... Saturday, day=7
        switch (day) {
            case Calendar.SUNDAY:
                curDaySteps = curWeekSteps[0];
                break;
            case Calendar.MONDAY:
                curDaySteps = curWeekSteps[1];
                break;
            case Calendar.TUESDAY:
                curDaySteps = curWeekSteps[2];
                break;
            case Calendar.WEDNESDAY:
                curDaySteps = curWeekSteps[3];
                break;
            case Calendar.THURSDAY:
                curDaySteps = curWeekSteps[4];
                break;
            case Calendar.FRIDAY:
                curDaySteps = curWeekSteps[5];
                break;
            case Calendar.SATURDAY:
                curDaySteps = curWeekSteps[6];
                break;
        }
        curDaySteps = 600; //temporary number for example displays (today's steps are 0 in the JSON)

        System.out.println("Goal steps: " + dayGoalSteps + ", Today steps: " + curDaySteps);

        yVals1.add(new Entry(curDaySteps,0));
        yVals1.add(new Entry(dayGoalSteps-curDaySteps,1)); // this needs to be fixed, since curDaySteps can be > dayGoalSteps

        xVals.add("Today's steps");
        xVals.add("Steps to goal");
        //xVals.add("");
        //xVals.add("");

        PieDataSet set1 = new PieDataSet(yVals1, "");
        set1.setSliceSpace(3f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();
        colors.add(ColorTemplate.getHoloBlue());
        colors.add(Color.GREEN);

        set1.setColors(colors);

        PieData data = new PieData(xVals, set1);
        chartOne.setData(data);

        // undo all highlights
        chartOne.highlightValues(null);

        DecimalFormat df = new DecimalFormat("#.##");
        float displayPercetage = ((float)curDaySteps / (float)dayGoalSteps) * 100;
        chartOne.setCenterText(String.valueOf(df.format(displayPercetage)) + "%");

        chartOne.setDrawXValues(false);

        chartOne.invalidate();

        graphOne.addView(chartOne);
    }


    @Override
    public void onUserUpdated(User user) {
        // Chart your data @MS WOOO WOOO //hell yeah!
        this.user = user;
        setDataLine(user);
        setDataPie(user);
    }

    @Override
    public void onEmailMissing(String userEmail) {

    }

    @Override
    public void onUserNotFound(String userEmail) {

    }
}
