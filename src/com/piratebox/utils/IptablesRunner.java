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
import android.widget.Toast;

import com.piratebox.R;
import com.piratebox.server.ServerConfiguration;

/**
 * This class is used to set up and tear down the redirection using iptables.
 * @author Aylatan
 */
public class IptablesRunner {

    private final String IPTABLES_SETUP_OPTIONS = " -t nat -A PREROUTING -i {interface} -p tcp -j REDIRECT --to-port " + ServerConfiguration.PORT;
    private final String IPTABLES_TEARDOWN_OPTIONS = " -t nat -D PREROUTING -i {interface} -p tcp -j REDIRECT --to-port " + ServerConfiguration.PORT;
    
    /**
     * The file name of the temporary script file that will be used.
     */
    public static final String TEMP_SCRIPT = "tmp_script";
    /**
     * The file name of the iptables executable.
     */
    public static final String IPTABLES = "iptables";
    
    private Context ctx;
    private String iptables = null;
    
    /**
     * Creates a new runner and loads the iptables binary.
     * @param ctx the context where it is used
     */
    public IptablesRunner(Context ctx) {
        this.ctx = ctx;

        try {
            iptables = loadIptables();
        } catch (IOException e) {
            ExceptionHandler.handle(this, R.string.error_script_loading, ctx);
        }
    }
    
    /**
     * Create a script with the redirection rules and run it.
     * @param wlanInterfaceName The interface name on which the script is to be run. Does nothing if {@code null}.
     * @throws IOException if an error occurs while writing or executing the script file
     * @throws InterruptedException if the calling thread is interrupted
     */
    public void setup(String wlanInterfaceName) throws IOException, InterruptedException {
        if (wlanInterfaceName == null || iptables == null) {
            return;
        }
        
        StringBuilder script = new StringBuilder();
        script.append(iptables).append(IPTABLES_SETUP_OPTIONS.replace("{interface}", wlanInterfaceName));

        runScript(script.toString());
    }
    
    /**
     * Create a script that removes the redirection rules and run it.
     * @param wlanInterfaceName The interface name on which the script is to be run. Does nothing if {@code null}.
     * @throws IOException if an error occurs while writing or executing the script file
     * @throws InterruptedException if the calling thread is interrupted
     */
    public void teardown(String wlanInterfaceName) throws IOException, InterruptedException {
        if (wlanInterfaceName == null || iptables == null) {
            return;
        }
        
        StringBuilder script = new StringBuilder();
        script.append(iptables).append(IPTABLES_TEARDOWN_OPTIONS.replace("{interface}", wlanInterfaceName));

        runScript(script.toString());
    }

    /**
     * Copy the iptables executables from the raw resources to an executable file.
     * @return the path to the generated iptables file
     * @throws IOException if a problem occurs while reading or writing the file
     */
    private String loadIptables() throws IOException {
        File tmpFolder = ctx.getDir("tmp", Context.MODE_PRIVATE);

        File f = new File(tmpFolder, IPTABLES);
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

    /**
     * Executes the string passed as argument as a runnable script with root rights.
     * Requires root access.
     * @param script the script content to be run
     * @return the exit value of the script
     * @throws IOException if an error occurs while writing or executing the script file
     * @throws InterruptedException if the calling thread is interrupted
     */
    private void runScript(String script) throws IOException, InterruptedException {
        File tmpFolder = ctx.getDir("tmp", Context.MODE_PRIVATE);

        File f = new File(tmpFolder, TEMP_SCRIPT);
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
        int res = exec.waitFor();
        Toast.makeText(ctx, "result: " + res, Toast.LENGTH_LONG).show();
        
        if (res != 0) {
            Toast.makeText(ctx, R.string.error_script_loading, Toast.LENGTH_LONG).show();
        }
    }
}
