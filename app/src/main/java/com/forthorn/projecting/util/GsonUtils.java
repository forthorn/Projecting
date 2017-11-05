package com.forthorn.projecting.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by: Forthorn
 * Date: 7/17/2017.
 * Description:
 */

public class GsonUtils {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyyMMddhhmmss")
            .disableHtmlEscaping()
            .create();

    public static <T> T convertObj(String json, Class<T> cls) {
//        if (StringUtils.isBlank(json)) {
//            return null;
//        }
        T t = null;
        try {
            t = gson.fromJson(json, cls);
        } catch (IllegalStateException e) {
        } finally {
            return t;
        }
//        return gson.fromJson(json, cls);
    }

    public static String getDataString(String json) {
        String data = null;
        try {
            JSONObject jsonObject = new JSONObject(json);
            data = jsonObject.getJSONObject("data").toString();
        } catch (JSONException e) {
            e.printStackTrace();
            data = "";
        } finally {
            return data;
        }
    }
}
