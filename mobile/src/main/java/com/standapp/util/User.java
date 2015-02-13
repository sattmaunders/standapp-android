package com.standapp.util;

import org.json.JSONArray;

/**
 * Created by Matt on 11/02/2015. User object
 */
public class User {

    private boolean confirmBreak = false;
    private int breakFreq = 5;
    private int breakDur = 5;
    private String email = null;

    //Time as int - a value between 0 and 1439 (the number of minutes in the day).
    //So noon would be represented as 720 (12 * 60);
    // 9:30am would be 570 (9 * 60 + 30),
    // 9:30pm would be 1290 (21 * 60 + 30), etc.
    private int workStart = 540;
    private int workEnd = 1020;

    private boolean[] workDays = {
            false,
            true,
            true,
            true,
            true,
            true,
            false
    };

    private int goalDailySteps = 2000;
    private int goalDailyOnFoot = 40; //minutes
    private int goalDailyBreaks = 8;


    private int[] bestSteps = new int[7];
    private int[] bestOnFoot = new int[7];
    private int[] bestBreaks = new int[7];

    private int[] previousWeekSteps = new int[7];
    private int[] previousWeekOnFoot = new int[7];
    private int[] previousWeekBreaks = new int[7];

    private int[] currentWeekSteps = new int[7];
    private int[] currentWeekOnFoot = new int[7];
    private int[] currentWeekBreak = new int[7];

    private JSONArray keyArr;
    private String _id;


    public User(
            boolean confirmBreak, int breakFreq, int breakDur, int workStart, int workEnd,
            boolean[] workDays, int goalDailySteps, int goalDailyOnFoot, int goalDailyBreaks,
            int[] bestSteps, int[] bestOnFoot, int[] bestBreaks,
            int[] previousWeekSteps, int[] previousWeekOnFoot, int[] previousWeekBreaks,
            int[] currentWeekSteps, int[] currentWeekOnFoot, int[] currentWeekBreak,
            JSONArray keyArr, String _id
            ) {
        this.confirmBreak = confirmBreak;
        this.breakFreq = breakFreq;
        this.breakDur = breakDur;
        this.workStart = workStart;
        this.workEnd = workEnd;
        this.workDays = workDays;
        this.goalDailySteps = goalDailySteps;
        this.goalDailyOnFoot = goalDailyOnFoot;
        this.goalDailyBreaks = goalDailyBreaks;
        this.bestSteps = bestSteps;
        this.bestOnFoot = bestOnFoot;
        this.bestBreaks = bestBreaks;
        this.previousWeekSteps = previousWeekSteps;
        this.previousWeekOnFoot = previousWeekOnFoot;
        this.previousWeekBreaks = previousWeekBreaks;
        this.currentWeekSteps = currentWeekSteps;
        this.currentWeekOnFoot = currentWeekOnFoot;
        this.currentWeekBreak = currentWeekBreak;
        this.keyArr = keyArr;
        this._id = _id;
    }

    public boolean getConfirmBreak() { return confirmBreak; }
    public int getBreakFreq() { return breakFreq; }
    public int getBreakDur() { return breakDur; }
    public int getWorkStart() { return workStart; }
    public int getWorkEnd() { return workEnd; }
    public boolean[] getWorkDays() { return workDays; }
    public int getGoalDailySteps() { return goalDailySteps; }
    public int getGoalDailyOnFoot() { return goalDailyOnFoot; }
    public int getGoalDailyBreaks() { return goalDailyBreaks; }
    public int[] getBestSteps() { return bestSteps; }
    public int[] getBestOnFoot() { return bestOnFoot; }
    public int[] getBestBreaks() { return bestBreaks; }
    public int[] getPreviousWeekSteps() { return previousWeekSteps; }
    public int[] getPreviousWeekOnFoot() { return previousWeekOnFoot; }
    public int[] getPreviousWeekBreaks() { return previousWeekBreaks; }
    public int[] getCurrentWeekSteps() { return currentWeekSteps; }
    public int[] getCurrentWeekOnFoot() { return currentWeekOnFoot; }
    public int[] getCurrentWeekBreak() { return currentWeekBreak; }
    public JSONArray getKeyArr() { return keyArr; }
    public String get_id() { return _id; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
