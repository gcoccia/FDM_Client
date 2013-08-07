/* 
* Copyright (C) 2000-2008 Institute for Global Environment and Society /
*                        Center for Ocean-Land-Atmosphere Studies
* Author: Joe Wielgosz <joew@cola.iges.org>
* 
* This file is part of the GrADS Data Server.
* 
* The GrADS Data Server is free software; you can redistribute
* it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; either version
* 2, or (at your option) any later version.
* 
* The GrADS Data Server is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with the GrADS Data Server; see the file COPYRIGHT.  If
* not, write to the Free Software Foundation, Inc., 59 Temple Place -
* Suite 330, Boston, MA 02111-1307, USA.
* 
* You can contact IGES/COLA at 4041 Powder Mill Rd Ste 302, Calverton MD 20705.
* 
* Last modified: $Date: 2008/07/22 17:22:27 $ 
* Revision for this file: $Revision: 1.7 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/client/Uploader.java,v $
*/
package org.iges.grads.client;

import java.io.*;
import java.util.*;
import java.net.*;

public class Uploader {


    public static void main(String[] args) {
	if (args.length < 3) {
	    syntaxError();
	}

	String serverURL = args[0];
	String datasetName = args[1];
	File dataFile = new File(args[2]);
	if (!dataFile.exists()) {
	    System.out.println("error: specified data file " + dataFile + " does not exist.");
	    syntaxError();
	}
	long length = dataFile.length();

	
	try {
	    URL uploadURL = new URL(serverURL + "/dods/" + datasetName);
	    HttpURLConnection con = (HttpURLConnection)uploadURL.openConnection();
	    con.setRequestMethod("POST");
	    con.setRequestProperty("ContentLength", String.valueOf(length));
	    con.setDoOutput(true);
	    con.setDoInput(true);

	    try {
		con.connect();
	    } catch (IOException ioe) {
		System.out.println("error: couldn't connect to server " + serverURL);
		syntaxError();
	    }
	    
	    InputStream fileStream = 
		new BufferedInputStream
		    (new FileInputStream
			(dataFile));

	    OutputStream uploadStream = 
		new BufferedOutputStream
		    (con.getOutputStream());

	    int bytesRead;
	    byte[] buf = new byte[16384];
	    while (true) {
		bytesRead = fileStream.read(buf, 0, buf.length);
		if (bytesRead == -1) {
		    break;
		}
		uploadStream.write(buf, 0, bytesRead);
	    }
	    uploadStream.close();

	    BufferedReader responseReader =
		new BufferedReader
		    (new InputStreamReader
			(con.getInputStream()));

	    String nextLine;
	    do {
		nextLine = responseReader.readLine();
		if (nextLine == null) {
		    break;
		}
		System.out.println(nextLine);
	    } while (true);

	} catch (MalformedURLException mue) {
	    System.out.println("error: specified URL " + serverURL + " is invalid.");
	    syntaxError();
	} catch (IOException ioe) {
	    System.out.println("error: connected ok but couldn't transfer file. more info:");
	    ioe.printStackTrace();
	    System.out.println(ioe.getMessage());
	}
	
	

    }

    private static void syntaxError(){ 
	System.out.println("usage: java grads.client.Uploader server_url shorthand_name udf_data_file");
		System.exit(0);
    }


}
