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
* Revision for this file: $Revision: 1.4 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/XMLCatalogService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.http.*;

import org.iges.anagram.*;

/** Sends a complete listing of the server's contents in XML format.
 */
public class XMLCatalogService
    extends Service {

    public String getServiceName() {
	return "xml";
    }

    public void configure(Setting setting) {
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();

	PrintWriter page;
	try {
	    page = 
		new PrintWriter
		    (new OutputStreamWriter
			(response.getOutputStream()));
	} catch (IOException ioe) { return;}
	    
	String baseURL = "http://" + 
	    request.getServerName() + ":" +
	    request.getServerPort() + 
		request.getContextPath();
	
	String servletURL = baseURL + request.getServletPath();
	
	StringBuffer buffer = new StringBuffer("");
	
	// Retrieve dataset list
	DirHandle root = (DirHandle)server.getCatalog().getLocked("/");
	
	Collection datasets = root.getEntries(true).values();
	
	response.setHeader("CacheControl", "no-cache");
	
	buffer.append("<?xml version=\"1.0\"?>\n" +
		      "  <serverdirectory count=\"");
	buffer.append(datasets.size());
	buffer.append("\">\n");
	
	Iterator it = datasets.iterator();
	int i = 1;
	while (it.hasNext()) {
	    Handle handle = (Handle)it.next();
	    if (handle instanceof DirHandle) {
		continue;
	    }
	    DataHandle dataset = (DataHandle)handle;
	    dataset.getSynch().lock();
	    buffer.append("    <dataset protocol=\"dods\" rank=\"");
	    buffer.append(i);
	    buffer.append("\">\n");
	    buffer.append("      <name>");
	    buffer.append(dataset.getCompleteName());
	    buffer.append("</name>\n");
	    buffer.append("      <description>");
	    buffer.append(dataset.getDescription());
	    buffer.append("</description>\n");

	    buffer.append("      <dods>");
	    buffer.append(baseURL);
	    buffer.append(dataset.getCompleteName());
	    buffer.append("</dods>\n");

	    buffer.append("      <dds>");
	    buffer.append(baseURL);
	    buffer.append(dataset.getCompleteName());
	    buffer.append(".dds</dds>\n");

	    buffer.append("      <das>");
	    buffer.append(baseURL);
	    buffer.append(dataset.getCompleteName());
	    buffer.append(".das</das>\n");

	    buffer.append("   </dataset>\n");
	    dataset.getSynch().release();
	    i++;
	}
	buffer.append("</serverdirectory>\n");
	
	root.getSynch().release();

	page.println(buffer);
	page.flush();
    }

}
