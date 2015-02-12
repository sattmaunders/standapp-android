package com.standapp.fragment;

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
import com.github.mikephil.charting.utils.LimitLine;
import com.standapp.R;
import com.standapp.common.BaseActionBarFragment;
import com.standapp.util.User;
import com.standapp.util.UserInfo;

import java.util.ArrayList;

import javax.inject.Inject;
/**
 * Created by Matt on 10/02/2015. Fragment for graphs
 */
public class GraphingCardFragment extends BaseActionBarFragment  {

    private static final String ARG_POSITION = "position";

    private int position;

    @Inject
    UserInfo userInfo;

    private PieChart chartOne;
    private LineChart chartTwo;

    private User user;

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

        user = userInfo.getUser();
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

        //Set chart data
        setDataLine(5,150);
        setDataPie(2,75);

        return rootView;
    }

    private void setDataLine(int count, float range) {

        ArrayList<String> xVals = new ArrayList<String>();      //X axis label values
        for (int i = 0; i < count; i++) {
            xVals.add((i) + "");
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();        //Values to display

        for (int i = 0; i < count; i++) {
            float mult = (range + 1);
            float val = (float) (Math.random() * mult) + 3;// + (float)
            // ((mult *
            // 0.1) / 10);
            yVals.add(new Entry(val, i));
        }

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        // set the line to be drawn like this "- - - - - -"
        set1.enableDashedLine(10f, 5f, 0f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setLineWidth(1f);
        set1.setCircleSize(4f);
        set1.setFillAlpha(65);
        set1.setFillColor(Color.BLACK);
        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);

        LimitLine ll1 = new LimitLine(130f);
        ll1.setLineWidth(4f);
        ll1.enableDashedLine(10f, 10f, 0f);
        ll1.setDrawValue(true);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT);

        LimitLine ll2 = new LimitLine(-30f);
        ll2.setLineWidth(4f);
        ll2.enableDashedLine(10f, 10f, 0f);
        ll2.setDrawValue(true);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT);

        data.addLimitLine(ll1);
        data.addLimitLine(ll2);

        // set data
        chartTwo.setData(data);
    }

    private void setDataPie(int count, float range) {

        float mult = range;

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();       //Values to display

        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < count + 1; i++) {
            yVals1.add(new Entry((float) (Math.random() * mult) + mult / 5, i));
        }

        ArrayList<String> xVals = new ArrayList<String>();      //Strings for labels

        for (int i = 0; i < count + 1; i++)
            xVals.add("test");

        PieDataSet set1 = new PieDataSet(yVals1, "Election Results");
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


}
