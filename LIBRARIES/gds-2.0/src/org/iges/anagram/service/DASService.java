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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/service/DASService.java,v $
*/
package org.iges.anagram.service;

import java.io.*;
import java.util.Date;
import javax.servlet.http.*;
import org.iges.anagram.*;

/** Provides the DODS Data Attribute Structure for a data object */
public class DASService 
    extends Service {

    public String getServiceName() {
	return "das";
    }

    public void configure(Setting setting) {
    }

    public void handle(ClientRequest clientRequest)
	throws ModuleException {

	HttpServletRequest request = clientRequest.getHttpRequest();
	HttpServletResponse response = clientRequest.getHttpResponse();
	
	DataHandle data = getDataFromPath(clientRequest);
	
	response.setContentType("text/plain");
	response.setHeader("XDODS-Server", "dods/3.2");
	response.setHeader("XDAP", "3.2");
	response.setHeader("Content-Description", "dods_das");
	response.setDateHeader("Last-Modified", data.getCreateTime());
	
	try {
	    server.getTool().writeDAS(data, 
				      response.getOutputStream());
	} catch (IOException ioe) {}
    }
	
}

