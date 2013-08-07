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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/UploadService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.http.*;

import org.iges.anagram.*;

/** Receives client uploads of data objects */
public class UploadService
    extends Service {

    public String getServiceName() {
	return "upload";
    }

    public void configure(Setting setting) {
	generating = new ArrayList();
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {
	
	String prefix = clientRequest.getDataPath();
	if (!prefix.startsWith("/_upload_")) {
	    throw new ModuleException(this,
				      "path for uploaded dataset must begin " +
				      "with '_upload_'");
	}

	String name;
	synchronized (generating) {
	    int counter = 0;
	    do {
		counter++;
		name = prefix + "_" + counter;
	    } while (server.getCatalog().contains(name) || 
		     generating.contains(name));
	    log.info(this, "storing upload: " + name);
	    generating.add(name);
	}
 
	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();

	try {
	
	    // Pass stream to cache to create dataset
	    long uploadSize = request.getIntHeader("Content-Length");
	    if (uploadSize <= 0) {
		throw new ModuleException(this,
					  "Content-Length <= 0 in request");
	    }

	    InputStream uploadStream;
	    try {
		uploadStream = request.getInputStream();
	    } catch (IOException ioe) {
		throw new ModuleException(this, "can't read request content");
	    }
	    
	    TempDataHandle handle = 
		server.getTool().doUpload(name, 
					  uploadStream, 
					  uploadSize, 
					  clientRequest.getPrivilege());
	    server.getCatalog().addTemp(handle);
	    
	    if (debug()) log.debug(this, clientRequest + 
				   "finished upload");
	    try {
		
		PrintWriter page = 
		    new PrintWriter
			(new OutputStreamWriter(response.getOutputStream()));
		page.println(name + " created successfully.");
		page.flush();
		
	    } catch (IOException ioe) {}

	    
	} catch (ModuleException me) {
	    throw me;
	} finally {
	    synchronized (generating) {
		generating.remove(name);
		generating.notifyAll();
	    }
	}
    }

    protected List generating;

}
