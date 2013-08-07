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
* Revision for this file: $Revision: 1.5 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/DODSErrorService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;

import dods.dap.DODSException;

import org.iges.anagram.Setting;
import org.iges.anagram.ClientRequest;

/** Sends an error message in the DODS protocol format */
public class DODSErrorService
    extends ErrorService {

    public String getServiceName() {
	return "error-dods";
    }

    public void configure(Setting setting) {
    }

    protected void sendErrorMsg(ClientRequest request, 
				String msg) {
	PrintWriter out = null;
	try {
	    out = 
		new PrintWriter
		    (new OutputStreamWriter
			(request.getHttpResponse().getOutputStream()));
	} catch (IOException ioe) {}

	setHeaders(request);

	sendDODSError(out, msg);
	
    }

    protected void sendUnexpectedErrorMsg(ClientRequest request, 
					  String debugInfo) {
	PrintWriter out = null;
	try {
	    out = 
		new PrintWriter
		    (new OutputStreamWriter
			(request.getHttpResponse().getOutputStream()));
	} catch (IOException ioe) {}

	setHeaders(request);

	StringWriter msgString = new StringWriter();
	PrintWriter msg = new PrintWriter(msgString);
	msg.print("Oops! The server encountered an unexpected error ");
	msg.print("while serving the this request.\n");
	msg.print("Please report this error at\n\t");
	msg.print(server.getImplHomePage());
	msg.print("\nand include the following debug information:\n");
	msg.print(debugInfo);

	sendDODSError(out, msgString.toString());
	
    }

    protected void setHeaders(ClientRequest request) {
	request.getHttpResponse().setContentType("text/plain");
	request.getHttpResponse().setHeader("XDODS-Server", "dods/3.2");
	request.getHttpResponse().setHeader("XDAP", "3.2");
	request.getHttpResponse().setHeader("Cache-Control", "no-cache");
	request.getHttpResponse().setHeader("Content-Description", 
					    "dods_error");
    }	

    protected void sendDODSError(PrintWriter out, String msg) {

	DODSException de = new DODSException(msg);
	de.print(out);
	out.close();
    }

}
