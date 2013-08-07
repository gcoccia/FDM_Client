/* 
* Copyright (C) 2000-2008 Institute for Global Environment and Society /
*                        Center for Ocean-Land-Atmosphere Studies
* Author: Joe Wielgosz <joew@cola.iges.org>
* 
* This file is part of the Anagram Server Framework.
* 
* The Anagram Server Framework is free software; you can redistribute
* it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; either version
* 2, or (at your option) any later version.
* 
* The Anagram Server Framework is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with the Anagram Server Framework; see the file COPYRIGHT.  If
* not, write to the Free Software Foundation, Inc., 59 Temple Place -
* Suite 330, Boston, MA 02111-1307, USA.
* 
* You can contact IGES/COLA at 4041 Powder Mill Rd Ste 302, Calverton MD 20705.
* 
* Last modified: $Date: 2008/07/22 17:22:27 $ 
* Revision for this file: $Revision: 1.6 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/WebErrorService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.iges.anagram.Setting;
import org.iges.anagram.ModuleException;
import org.iges.anagram.ClientRequest;

/** Sends an error message as an HTML page */
public class WebErrorService
    extends ErrorService {

    public String getServiceName() {
	return "error-html";
    }

    public void configure(Setting setting) {
    }

    protected void sendErrorMsg(ClientRequest request, 
				String msg) {
	if (request.getHttpResponse().isCommitted()) {
	    try {
		PrintStream out = new PrintStream
		    (request.getHttpResponse().getOutputStream());
		sendErrorText(out, request, msg);
		out.close();
	    } catch (IOException ioe) {}
	} else {
	    request.getHttpResponse().setHeader("Cache-Control", "no-cache");
	    String baseURL = getBaseURL(request);
	    PrintStream out = startHTML(request);
	    if (out == null) {
		return;
	    }
	    printHeader(out, "error", "error", null, baseURL);
	    sendErrorHTML(out, request.getFullURL(), msg);
	    printFooter(out, null, 0, baseURL);
	    out.close();
	}	    
	
	
    }

    protected void sendUnexpectedErrorMsg(ClientRequest request, 
					  String debugInfo) {
	if (request.getHttpResponse().isCommitted()) {
	    try {
		PrintStream out = new PrintStream
		    (request.getHttpResponse().getOutputStream());
		sendErrorText(out, request, debugInfo);
		out.close();
	    } catch (IOException ioe) {}
	    
	} else {
	    request.getHttpResponse().setHeader("Cache-Control", "no-cache");
	    String baseURL = getBaseURL(request);
	    PrintStream out = startHTML(request);
	    if (out == null) {
		return;
	    }
	    printHeader(out, "error", "error", null, 
			getBaseURL(request));
	    sendUnexpectedHTML(out, 
			       request, 
			       debugInfo);
	    printFooter(out, null, 0, baseURL);
	    out.close();
	}	    
    }

    protected void sendErrorHTML(PrintStream out, String url, String msg) {
	    out.print("The server could not fulfill this request:<p>");
	    out.print("<b>");
	    out.print(url);
	    out.print("</b><p>");
	    out.print(" because of the following error:<p>\n");
	    out.print("<b>");
	    out.print(msg);
	    out.print("</b><p>\n");
	    out.print("Check the syntax of your request, ");
	    out.print("or click <a href=\".help\">here</a> for help ");
	    out.print("using the server.\n");
    }

    protected void sendUnexpectedHTML(PrintStream out, 
				      ClientRequest request,
				      String debugInfo) {
	out.print("Oops! The server encountered an unexpected error while serving the this request:<p>");
	out.print("<b>");
	out.print(request.getFullURL());
	out.print("</b><p>");
	out.print("Please report this error to <a href=\"");
	out.print(server.getImplHomePage());
	out.print("\">");
	out.print(server.getImplHomePage());
	out.print("</a>");
	out.print(", and include the following debug information:<p>\n");
	out.print("<code>");
	out.print(new Date(System.currentTimeMillis()));
	out.print(": ");
	out.print(request);
	out.print("\n");
	out.print(debugInfo);
	out.print("</code>");
    }

    protected void sendErrorText(PrintStream out, 
				 ClientRequest request,
				 String debugInfo) {
	out.print("\n\n");
	out.print("Error during request processing.\n");
	out.print("Please report this error to\n\t");
	out.print(server.getImplHomePage());
	out.print("\nand include the following debug information:\n");
	out.print(new Date(System.currentTimeMillis()));
	out.print(": ");
	out.print(request);
	out.print("\n");
	out.print(debugInfo);
    }

}
