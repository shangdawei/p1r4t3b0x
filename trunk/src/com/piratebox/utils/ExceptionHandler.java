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

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * This class handles exceptions.
 * @author Aylatan
 */
public class ExceptionHandler {
    
    /**
     * Add a log entry with the provided tad and message.
     * If {@code showToUser} is true, show the provided message to the user via a Toast.
     * {@code ctx} must not be null if {@code showToUser} is true.
     * @param tag the tag for the log entry
     * @param message the message to display in the log entry and in the Toast
     * @param ctx the context of the application
     * @param showToUser if {@code true}, show the message to the user via a Toast
     */
    public static void handle(String tag, String message, Context ctx, boolean showToUser) {
        Log.e(tag, message);
        if (showToUser) {
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Calls {@link #handle(String, String, Context, boolean)} with showToUser = true.
     * @param tag the tag for the log entry
     * @param message the message to display in the log entry and in the Toast
     * @param ctx the context of the application
     */
    public static void handle(String tag, String message, Context ctx) {
        handle(tag, message, ctx, true);
    }
    
    /**
     * Calls {@link #handle(String, String, Context)} with e.toString() as message.
     * @param tag the tag for the log entry
     * @param e the exception to show. Used to build the message
     * @param ctx the context of the application
     */
    public static void handle(String tag, Exception e, Context ctx) {
        handle(tag, e.toString(), ctx);
    }

    /**
     * Calls {@link #handle(String, String, Context)} with sender.getClass().getName() as tag.
     * @param sender the object that sends the error. Used to build the tag
     * @param message the message to display in the log entry and in the Toast
     * @param ctx the context of the application
     */
    public static void handle(Object sender, String message, Context ctx) {
        handle(sender.getClass().getName(), message, ctx);
    }
    
    /**
     * Calls {@link #handle(String, Exception, Context)} with sender.getClass().getName() as tag.
     * @param sender the object that sends the error. Used to build the tag
     * @param e the exception to show. Used to build the message
     * @param ctx the context of the application
     */
    public static void handle(Object sender, Exception e, Context ctx) {
        handle(sender.getClass().getName(), e, ctx);
    }
    
    /**
     * Calls {@link #handle(String, String, Context, boolean)} with ctx = null and showToUser = false.
     * @param tag the tag for the log entry
     * @param message the message to display in the log entry
     */
    public static void handle(String tag, String message) {
        handle(tag, message, null, false);
    }
    
    /**
     * Calls {@link #handle(String, String)} with e.toString() as message.
     * @param tag the tag for the log entry
     * @param e the exception to show. Used to build the message
     */
    public static void handle(String tag, Exception e) {
        handle(tag, e.toString());
    }
    
    /**
     * Calls {@link #handle(String, Exception)} with sender.getClass().getName() as tag.
     * @param sender the object that sends the error. Used to build the tag
     * @param e the exception to show. Used to build the message
     */
    public static void handle(Object sender, Exception e) {
        handle(sender.getClass().getName(), e);
    }
    
    /**
     * Calls {@link #handle(String, String)} with sender.getClass().getName() as tag.
     * @param sender the object that sends the error. Used to build the tag
     * @param message the message to display in the log entry
     */
    public static void handle(Object sender, String message) {
        handle(sender.getClass().getName(), message);
    }
    
    /**
     * Calls {@link #handle(String, String, Context)} with the resource string stringId as message.
     * @param tag the tag for the log entry
     * @param stringId the id of the string to use as message
     * @param ctx the context of the application
     */
    public static void handle(String tag, int stringId, Context ctx) {
        handle(tag, ctx.getResources().getString(stringId), ctx);
    }
    
    /**
     * Calls {@link #handle(String, int, Context)} with sender.getClass().getName() as tag.
     * @param sender the object that sends the error. Used to build the tag
     * @param stringId the id of the string to use as message
     * @param ctx the context of the application
     */
    public static void handle(Object sender, int stringId, Context ctx){
        handle(sender.getClass().getName(), stringId, ctx);
    }
}
