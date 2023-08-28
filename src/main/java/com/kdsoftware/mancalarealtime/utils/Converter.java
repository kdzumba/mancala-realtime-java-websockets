package com.kdsoftware.mancalarealtime.utils;

import com.google.gson.Gson;

public class Converter<T>
{
    private static final Gson gson = new Gson();
    public static String toJson(Object string)
    {
        return gson.toJson(string);
    }
}
