package com.standapp.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Matt on 11/02/2015. Builds the User object from JSONObject response
 */
public class UserTransformer {


    public UserTransformer() { }

    public User buildUserObject(JSONObject jsonUser) {
        User userObject;

        try {
            //Get top level JSON objects:
            JSONObject historyWeeks = jsonUser.getJSONObject("history").getJSONObject("weeks");
            JSONObject preferences = jsonUser.getJSONObject("preferences");


            //Start with preferences:
            boolean confirmBreak = preferences.getBoolean("confirmBreak");

            JSONObject tempJson = preferences.getJSONObject("breaks");
            int breakFreq = tempJson.getInt("frequency");
            int breakDur = tempJson.getInt("duration");

            tempJson = preferences.getJSONObject("work").getJSONObject("hours");
            int workStart = hoursToInt(tempJson.getString("start"));
            int workEnd = hoursToInt(tempJson.getString("end"));

            JSONArray tempJsonArr = preferences.getJSONObject("work").getJSONArray("days");
            boolean[] workDays = { false, true, true, true, true, true, false };
            if (tempJsonArr.length() == 7) {
                workDays = new boolean[] {
                        tempJsonArr.getBoolean(0), tempJsonArr.getBoolean(1), tempJsonArr.getBoolean(2),
                        tempJsonArr.getBoolean(3), tempJsonArr.getBoolean(4), tempJsonArr.getBoolean(5),
                        tempJsonArr.getBoolean(6)
                };
            }
            //TODO: there should be a true of false value for each day of the week, but there isn't always?

            tempJson = preferences.getJSONObject("goals").getJSONObject("daily");
            int goalDailySteps = tempJson.getInt("steps");
            int goalDailyOnFoot = tempJson.getInt("onfoot");
            int goalDailBreaks = tempJson.getInt("breaks");


            //Now on to history:
            //Best:
            tempJson = historyWeeks.getJSONObject("best");
            tempJsonArr = tempJson.getJSONArray("steps");
            int[] bestSteps = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                bestSteps[i] = tempJsonArr.getInt(i);
            }
            tempJsonArr = tempJson.getJSONArray("onfoot");
            int[] bestOnFoot = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                bestOnFoot[i] = tempJsonArr.getInt(i);
            }
            tempJsonArr = tempJson.getJSONArray("breaks");
            int[] bestBreaks = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                bestBreaks[i] = tempJsonArr.getInt(i);
            }
            //Previous:
            tempJson = historyWeeks.getJSONObject("previous");
            tempJsonArr = tempJson.getJSONArray("steps");
            int[] previousWeekSteps = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                previousWeekSteps[i] = tempJsonArr.getInt(i);
            }
            tempJsonArr = tempJson.getJSONArray("onfoot");
            int[] previousWeekOnFoot = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                previousWeekOnFoot[i] = tempJsonArr.getInt(i);
            }
            tempJsonArr = tempJson.getJSONArray("breaks");
            int[] previousWeekBreaks = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                previousWeekBreaks[i] = tempJsonArr.getInt(i);
            }
            //Current:
            tempJson = historyWeeks.getJSONObject("current");
            tempJsonArr = tempJson.getJSONArray("steps");
            int[] currentWeekSteps = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                currentWeekSteps[i] = tempJsonArr.getInt(i);
            }
            tempJsonArr = tempJson.getJSONArray("onfoot");
            int[] currentWeekOnFoot = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                currentWeekOnFoot[i] = tempJsonArr.getInt(i);
            }
            tempJsonArr = tempJson.getJSONArray("breaks");
            int[] currentWeekBreak = new int[tempJsonArr.length()];
            for (int i = 0; i < tempJsonArr.length(); i++) {
                currentWeekBreak[i] = tempJsonArr.getInt(i);
            }


            //Finally on to config:
            JSONArray keyArr = jsonUser.getJSONObject("config").getJSONArray("gcmKeys");
            String _id = jsonUser.getString("_id");

            userObject = new User(
                    confirmBreak, breakFreq, breakDur, workStart, workEnd, workDays,
                    goalDailySteps, goalDailyOnFoot, goalDailBreaks,
                    bestSteps, bestOnFoot, bestBreaks,
                    previousWeekSteps, previousWeekOnFoot, previousWeekBreaks,
                    currentWeekSteps, currentWeekOnFoot, currentWeekBreak,
                    keyArr, _id
                    );

            return userObject;
            /* User constructor:
            boolean confirmBreak, int breakFreq, int breakDur, int workStart, int workEnd,
            boolean[] workDays, int goalDailySteps, int goalDailyOnFoot, int goalDailyBreaks,
            int[] bestSteps, int[] bestOnFoot, int[] bestBreaks,
            int[] previousWeekSteps, int[] previousWeekOnFoot, int[] previousWeekBreaks,
            int[] currentWeekSteps, int[] currentWeekOnFoot, int[] currentWeekBreak
            */
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private int hoursToInt(String hours) {
        //Split string
        String[] splitHours = hours.split(":");
        return (Integer.parseInt(splitHours[0]) * 60) + Integer.parseInt(splitHours[1]);
    }
}
