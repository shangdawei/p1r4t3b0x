/**
 * This is a file from P1R4T3B0X, a program that lets you share files with everyone.
 * Copyright (C) 2012 by Aylatan
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License can be found at http://www.gnu.org/licenses.
 */

package com.piratebox.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StatUtils {
    public static final String STATS_STORAGE = "statStorage";

    public static final String STAT_FILE_DL = "statFileDl";
    public static final String STAT_FILE_DL_SESSION = "statFileDlSession";
    public static final String STAT_PREFIX_FILE = "file_";
    public static final int STAT_TOPDL_LENGTH = 5;
    
    
    public static void addStat(Context ctx, String key) {
        SharedPreferences stats = ctx.getSharedPreferences(STATS_STORAGE, 0);
        int stat = stats.getInt(key, 0);
        stat++;
        Editor edit = stats.edit();
        edit.putInt(key, stat).commit();
    }
    
    public static void resetStat(Context ctx, String key) {
        SharedPreferences stats = ctx.getSharedPreferences(STATS_STORAGE, 0);
        Editor edit = stats.edit();
        edit.putInt(key, 0).commit();
    }
    
    public static void resetAllStats(Context ctx) {
        SharedPreferences stats = ctx.getSharedPreferences(STATS_STORAGE, 0);
        stats.edit().clear().commit();
    }
    
    public static void addStatForFile(Context ctx, File f) {
        addStat(ctx, StatUtils.STAT_FILE_DL);
        addStat(ctx, StatUtils.STAT_FILE_DL_SESSION);
        
        addStat(ctx, STAT_PREFIX_FILE + f.getName());
    }
    
    public static String[] getTopDls(Context ctx) {
        SharedPreferences stats = ctx.getSharedPreferences(STATS_STORAGE, 0);
        Map<String, ?> allValues = stats.getAll();
        
        Object[] entries = allValues.entrySet().toArray();
        ArrayList<Entry<String, Integer>> files = new ArrayList<Map.Entry<String, Integer>>();
        for (Object obj : entries) {
            @SuppressWarnings("unchecked")
            Entry<String, Integer> entry = (Entry<String, Integer>) obj;
            if (entry.getKey().startsWith(STAT_PREFIX_FILE)) {
                files.add(entry);
            }
        }
        
        Collections.sort(files, new Comparator<Entry<String, Integer>>() {
            public int compare(Entry<String, Integer> lhs, Entry<String, Integer> rhs) {
                return (int)rhs.getValue() - (int)lhs.getValue();
            }
        });
        
        int length = Math.min(STAT_TOPDL_LENGTH, files.size());
        String[] result = new String[STAT_TOPDL_LENGTH];
        for (int i = 0; i < length; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(files.get(i).getKey().substring(STAT_PREFIX_FILE.length())).append(" (")
            .append(files.get(i).getValue()).append(")");
            
            result[i] = sb.toString();
        }
        return result;
    }
}
