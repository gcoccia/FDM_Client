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
* Revision for this file: $Revision: 1.9 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/AdminService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.iges.util.*;
import org.iges.anagram.*;

/** Provides administrative functions, protected by a simple authorization
 *  mechanism. */
public class AdminService 
    extends Service {

    public String getServiceName() {
	return "admin";
    }

    public void configure(Setting setting) {
	authCode = setting.getAttribute("auth", "");
	if (authCode.equals("")) {
	    if (verbose()) verbose("no authorization code; " + 
				   "admin service will be disabled");
	}
	timeout = setting.getNumAttribute("timeout", 60);
	if (verbose()) verbose("admin timeout set to " + timeout + " sec");
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();

	

	Privilege privilege = clientRequest.getPrivilege();
	if ( ! request.getRemoteAddr().equals("127.0.0.1") && 
	     ! privilege.getAttribute("admin_allowed", "false").equals("true")) {
	    if (debug()) debug("IP address " + request.getRemoteAddr() + 
			       " is not local, and is not authorized");
	    throw new ModuleException(this, "authorization failed");
	}
	
	Hashtable queryParams = getQueryParams(clientRequest);

	if (authCode.equals("")) {
	    throw new ModuleException(this, "service not available");
	}
	    
	if (debug()) debug("parsed parameters for admin cmd"); 

	String[] userAuth = (String[])queryParams.get("auth");
	if (userAuth == null) {
	    if (debug()) debug("no userAuth given");
	    throw new ModuleException(this, "authorization failed");
	}

	if (!userAuth[0].equals(authCode)) {
	    if (debug()) debug("userAuth is " + userAuth[0]);
	    throw new ModuleException(this, "authorization failed");
	}

	String[] command = (String[])queryParams.get("cmd");
	if (command == null) {
	    throw new ModuleException(this, "no command given");
	} else if (command[0].equals("reload")) {
	    if (debug()) debug("attempting to get exclusive lock"); 
	    try {
		synchronized (server.getSynch()) {
		    // must release regular lock, or exclusive lock will fail
		    server.getSynch().release();
		    if (debug()) debug("released non-exclusive lock"); 
		    if (!server.getSynch().tryLockExclusive(timeout * 1000)) {
			throw new ModuleException
			    (this, "timed out after " + timeout + 
			     " seconds; server is currently in use");
		    }
		    if (debug()) debug("got an exclusive lock"); 
		}
		info("reconfiguring server");
		server.reconfigure();
	    } catch (ModuleException me) {
		throw me;
	    } finally {
		if (debug()) debug("returning to non-exclusive lock"); 
		synchronized (server.getSynch()) {
		    server.getSynch().release();
		if (debug()) debug("switching back to non-exclusive lock"); 
		    server.getSynch().lock();		
		if (debug()) debug("switched back to non-exclusive lock"); 
		}
	    }
	} else if (command[0].equals("clear")) {
	    info("removing all temporary catalog entries");
	    server.getCatalog().clearTemp();
	} else {
	    throw new ModuleException(this, "unknown command " + command[0]);
	}
	
	printReply(clientRequest, command[0]);
	if (debug()) debug("sent admin cmd result to client"); 

    }

    protected void printReply(ClientRequest request, String command) {
	request.getHttpResponse().setHeader("Cache-Control", "no-cache");
	String baseURL = getBaseURL(request);
	PrintStream page = startHTML(request);
	if (page == null) { 
	    return;
	}
	printHeader(page, "admin", "admin", null, baseURL);
	page.print("Command:<p>\n");
	page.print("<b>");
	page.print(command);
	page.print("</b><p>\n");
	page.print("performed successfully.");
	printFooter(page, null, 0, baseURL);
	
    }


    protected String authCode;
    protected long timeout;

}
