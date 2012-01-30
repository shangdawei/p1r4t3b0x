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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.piratebox.R;

public class IptablesRunner {
    
    private Context ctx;
    private String iptables;
    
    public IptablesRunner(Context ctx) {
        this.ctx = ctx;

        try {
            iptables = loadIptables();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), e.toString());
        }
    }
    
    public void setup() {
        try {
            StringBuilder script = new StringBuilder();
            script.append(iptables).append(" --version\n");
    
            // TODO Add lines !!!!
    
            int res = runScript(script.toString());
            if (res != 1) {
                Toast.makeText(ctx, R.string.error_redirect, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.toString());
        }
    }
    
    public void teardown() {
        try {
            StringBuilder script = new StringBuilder();
            script.append(iptables).append(" --version\n");
    
            // TODO Add lines !!!!
    
            int res = runScript(script.toString());
            if (res != 1) {
                Toast.makeText(ctx, R.string.error_redirect, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.toString());
        }
    }

    private String loadIptables() throws IOException {
        File tmpFolder = ctx.getDir("tmp", Context.MODE_PRIVATE);

        File f = new File(tmpFolder, Script.IPTABLES);
        f.setExecutable(true);
        f.deleteOnExit();

        InputStream is = ctx.getResources().openRawResource(R.raw.iptables);
        byte[] buff = new byte[is.available()];
        is.read(buff);
        is.close();

        FileOutputStream out = new FileOutputStream(f);
        out.write(buff);
        out.close();
        return f.getAbsolutePath();
    }

    private int runScript(String script) throws IOException, InterruptedException {

        File tmpFolder = ctx.getDir("tmp", Context.MODE_PRIVATE);

        File f = new File(tmpFolder, Script.TEMP_SCRIPT);
        f.setExecutable(true);
        f.deleteOnExit();

        // Write the script to be executed
        PrintWriter out = new PrintWriter(new FileOutputStream(f));
        if (new File("/system/bin/sh").exists()) {
            out.write("#!/system/bin/sh\n");
        }
        out.write(script);
        if (!script.endsWith("\n")) {
            out.write("\n");
        }
        out.write("exit\n");
        out.flush();
        out.close();
        Process exec = Runtime.getRuntime().exec("su -c " + f.getAbsolutePath());
        return exec.waitFor();
    }
}
