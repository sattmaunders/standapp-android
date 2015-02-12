package com.standapp.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.LimitLine;
import com.standapp.R;
import com.standapp.backend.UserInfoListener;
import com.standapp.backend.UserInfoMediator;
import com.standapp.common.BaseActionBarFragment;
import com.standapp.util.User;

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
        //LinearLayout graphOne = (LinearLayout) rootView.findViewById(R.id.graphOne);
        //LinearLayout graphTwo = (LinearLayout) rootView.findViewById(R.id.graphTwo);

        //Get charts:
        chartOne = (PieChart) rootView.findViewById(R.id.chartOne);
        chartTwo = (LineChart) rootView.findViewById(R.id.chartTwo);

        //Set chart description/title thing
        chartOne.setDescription("Daily goal");
        chartTwo.setDescription("Week summary");

        //user = userInfo.getUser();

        //Set chart data
        //setDataLine(5,150);
        //setDataPie(user);

        return rootView;
    }

    private void setDataLine(User user) {
/*
        boolean confirmBreak, int breakFreq, int breakDur, int workStart, int workEnd,
        boolean[] workDays, int goalDailySteps, int goalDailyOnFoot, int goalDailyBreaks,
        int[] bestSteps, int[] bestOnFoot, int[] bestBreaks,
        int[] previousWeekSteps, int[] previousWeekOnFoot, int[] previousWeekBreaks,
        int[] currentWeekSteps, int[] currentWeekOnFoot, int[] currentWeekBreak,
*/
        int colorBest = Color.BLACK;
        int colorPrevious = Color.BLUE;
        int colorCurrent = Color.RED;

        int[] colors = {0,colorBest, colorPrevious, colorCurrent};

        ArrayList<String> xVals = new ArrayList<String>();      //X axis label values
        ArrayList<Entry> yVals = new ArrayList<Entry>();        //Values to display

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();

        String[] daysOfTheWeek = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday"};
        xVals.addAll(Arrays.asList(daysOfTheWeek));

        int[] bestSteps = user.getBestSteps();
        int[] previousWeekSteps = user.getPreviousWeekSteps();
        int[] currentWeekSteps = user.getCurrentWeekSteps();

        ArrayList<int[]>  tempArray = new ArrayList<>();
        tempArray.add(bestSteps);
        tempArray.add(previousWeekSteps);
        tempArray.add(currentWeekSteps);

        int indexTop = 0;
        for (int[] i : tempArray) {
            int index = 0;
            for (int j : i) {
                yVals.add(new Entry(j,index));
                index++;
            }
            // create a dataset and give it a type
            LineDataSet set1 = new LineDataSet(yVals, "DataSet " + (indexTop+1));
            // set1.setFillAlpha(110);
            // set1.setFillColor(Color.RED);

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.setColor(colors[indexTop]);
            set1.setCircleColor(colors[indexTop]);
            set1.setLineWidth(1f);
            set1.setCircleSize(4f);
            set1.setFillAlpha(65);
            set1.setFillColor(colors[indexTop]);
            // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
            // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

            indexTop++;

            dataSets.add(set1); // add the datasets
        }





        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        // set data
        chartTwo.setData(data);
    }

    private void setDataPie(User user) {

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

        yVals1.add(new Entry(curDaySteps,0));
        yVals1.add(new Entry(dayGoalSteps-curDaySteps,1));

        xVals.add("Today's steps");
        xVals.add("Steps to go");

        PieDataSet set1 = new PieDataSet(yVals1, "Goal steps");
        set1.setSliceSpace(3f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        set1.setColors(colors);

        PieData data = new PieData(xVals, set1);
        chartOne.setData(data);

        // undo all highlights
        chartOne.highlightValues(null);

        chartOne.invalidate();
    }


    @Override
    public void onUserUpdated(User user) {
        // Chart your data @MS WOOO WOOO
//        user.getBestBreaks();
    }

    @Override
    public void onEmailMissing(String userEmail) {

    }

    @Override
    public void onUserNotFound(String userEmail) {

    }
}
