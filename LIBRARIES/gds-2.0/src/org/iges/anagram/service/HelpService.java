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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/HelpService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.http.*;

import org.iges.anagram.*;

/** Provides an HTML page with links to information about the
 *  server's contents and how to use it.
 */
public class HelpService
    extends Service {

    public String getServiceName() {
	return "help";
    }

    public void configure(Setting setting) {
	cachedResponse = null;
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	PrintStream page = startHTML(clientRequest);
	if (page == null) {
	    return;
	}
	
	String baseURL = getBaseURL(clientRequest);
	printHeader(page, "help", "help", null, baseURL);
	
	// After cache is initialized
	if (cachedResponse == null) {
	    cachedResponse = buildResponse(baseURL);
	}
	
	page.print(cachedResponse); 
	
	printFooter(page, null, 0, baseURL);
	
	page.flush();
	page.close();
	    
    }
    
    private String cachedResponse = null;

    private String buildResponse(String baseURL) {
	StringBuffer buffer = new StringBuffer();
	
	buffer.append("This server provides online access to, and analysis\n");
	buffer.append("of, scientific data, using the OPeNDAP protocol.\n");
	buffer.append("<p>For more information about: </p>\n");
	buffer.append("<ul>\n");
	buffer.append("  <li>this site's data holdings, ");
	buffer.append("and other site-specific information - see\n");
	buffer.append("    <a href=\"");
	buffer.append(server.getSiteHomePage(baseURL));
	buffer.append("\">");
	buffer.append("this site's home page</a>\n");
	buffer.append(" .&nbsp;<br>\n");
	buffer.append("    <br>\n");
	buffer.append("  </li>\n");
	buffer.append("  <li>The ");
	buffer.append(server.getImplName());
	buffer.append(", and features specific to this server, ");
	buffer.append("such as remote analysis - see the\n");
	buffer.append("    <a href=\"");
	buffer.append(server.getImplHomePage());
	buffer.append("\">");
	buffer.append(server.getImplName());
	buffer.append(" home page</a>\n");
	buffer.append(" .<br>\n");
	buffer.append("    <br>\n");
	buffer.append("  </li>\n");
	buffer.append("  <li>the OPeNDAP protocol, how to access data on OPeNDAP servers, and how to obtain\n");
	buffer.append("OPeNDAP-enabled client software - see the&nbsp; <a href=\"http://www.opendap.org\">\n");
	buffer.append("OPeNDAP home page</a>\n");
	buffer.append(".\n");
	buffer.append("  </li>\n");
	buffer.append("</ul>\n");

	return buffer.toString();
    }

}
