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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/DirectoryService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import org.iges.anagram.*;

/** Provides HTML listings of the server's contents for a given directory
 *  path. 
*/
public class DirectoryService
    extends Service {

    public String getServiceName() {
	return "dir";
    }

    public void configure(Setting setting) {
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	String path = clientRequest.getDataPath();
	Handle subDir = clientRequest.getHandle();
	Privilege privilege = clientRequest.getPrivilege();
	if (subDir == null || 
	    !(subDir instanceof DirHandle) || 
	    !privilege.everAllows(subDir.getCompleteName())) {
	    throw new ModuleException(this, "no directory called " + path);
	}

	long updateTime = server.getLastConfigTime();
	List dataHandles = new ArrayList();
	List subdirs = new ArrayList();
	synchronized (subDir) {
	    Map entries = ((DirHandle)subDir).getEntries(false);
	    Iterator it = entries.entrySet().iterator();
	    while (it.hasNext()) {
		Map.Entry current = (Map.Entry)it.next();
		if (current.getValue() instanceof DirHandle) {
		    subdirs.add(current.getValue());
		} else {
		    dataHandles.add(current.getValue());
		    updateTime = Math.max
			(updateTime,
			 ((DataHandle)current.getValue()).getCreateTime());
		}
	    }
	}

	clientRequest.getHttpResponse().setHeader("CacheControl", "no-cache");
	clientRequest.getHttpResponse().setDateHeader("Last-Modified", 
						      updateTime);
	
	PrintStream page = startHTML(clientRequest);
	if (page == null) {
	    return;
	}
	int size = dataHandles.size() + subdirs.size();
	String windowTitle = "directory for " + subDir.getCompleteName();
	String pageTitle = windowTitle + " : " + size + " entries";
	String baseURL = getBaseURL(clientRequest);
	printHeader(page, windowTitle, pageTitle, subDir, baseURL);

	int i = 1;
	    
	Iterator it = subdirs.iterator();
	while (it.hasNext()) {
	    DirHandle current = (DirHandle)it.next();
	    if (!privilege.everAllows(current.getCompleteName())) {
		if (debug()) debug(current.getCompleteName() + " is forbidden");
		continue;
	    }
	    if (debug()) debug(current.getCompleteName() + " is allowed");
	    page.print("<b>");
	    page.print(i);
	    page.print(": ");
	    page.print(current.getName());
	    page.print("/:</b> ");
	    page.print("<a href=\"");
	    page.print(baseURL);
	    page.print(current.getCompleteName());
	    page.print("\">");
	    page.print("dir");
	    page.print("</a><br><br>\n");
	    i++;
	}
	/*
	if (subdirs.size() > 0) {
	    page.print("<br>\n");
	}
	*/

	it = dataHandles.iterator();
	
	while (it.hasNext()) {
	    DataHandle dataHandle = (DataHandle)it.next();
	    if (!privilege.allows(dataHandle.getCompleteName())) {
		continue;
	    }
	    dataHandle.getSynch().lock();
	    page.print("<b>");
	    page.print(i);
	    page.print(": \n");
	    page.print(dataHandle.getName());
	    page.print(":</b>&nbsp;");
	    Object info = dataHandle.getToolInfo();

	    /*
	    if (!dataHandle.isAvailable()) {
		page.print("temporarily unavailable<br><br>\n");
	    } else {
	    */
	    if (!dataHandle.isAvailable()) {
		page.print("<font color=\"#999999\">");
	    }
		page.print(dataHandle.getDescription());
		page.print("\n&nbsp;\n");
		
		page.print("<a href=\"");
		page.print(baseURL);
		page.print(dataHandle.getCompleteName());
		page.print(".info\">info</a>&nbsp;\n");
		
		page.print("<a href=\"");
		page.print(baseURL);
		page.print(dataHandle.getCompleteName());
		page.print(".dds\">dds</a>&nbsp;\n");
		
		page.print("<a href=\"");
		page.print(baseURL);
		page.print(dataHandle.getCompleteName());
		page.print(".das\">das</a><br><br>\n");
	    if (!dataHandle.isAvailable()) {
		page.print("</font>");
	    }
		/*
	    }
		*/
	    dataHandle.getSynch().release();

	    i++;
	}

	DirHandle parent = server.getCatalog().getParent(subDir);
	printFooter(page, parent, updateTime, baseURL);
	
	page.flush();
	page.close();
    }

}
