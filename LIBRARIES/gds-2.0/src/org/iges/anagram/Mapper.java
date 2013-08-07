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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/Mapper.java,v $
*/
package org.iges.anagram;

import java.util.*;

import javax.servlet.http.*;

import org.iges.anagram.service.*;

/** Maps incoming servlet requests to services and privilege levels,
 *  and returns completed ClientRequest objects.
 */
public class Mapper 
    extends AbstractModule {

    public String getModuleID() {
	return "mapper";
    }

    public void configure(Setting setting) 
	throws ConfigException {
	if (services == null) {
	    createServices();
	}

	Iterator it = services.values().iterator();
	while (it.hasNext()) {
	    Service service = (Service)it.next();
	    Setting serviceSetting = null;
	    try {
		serviceSetting = 
		    setting.getUniqueSubSetting(service.getModuleID());
	    } catch (AnagramException ae) {
		throw new ConfigException(this, ae.getMessage());
	    }
	    boolean enabled = 
		serviceSetting.getAttribute("enabled", "true").equals("true");
	    service.setEnabled(enabled);
	    service.configure(serviceSetting);
	}

    }


    protected void createServices() {
	services = new HashMap();
	services.put("admin", new AdminService());
	services.put("asc", new ASCIIDataService());
	services.put("ascii", services.get("asc"));
	services.put("das", new DASService());
	services.put("dds", new DDSService());
	services.put("dir", new DirectoryService());
	services.put("dods", new BinaryDataService());
	services.put("help", new HelpService());
	services.put("info", new InfoService());
	services.put("thredds", new THREDDSCatalogService());
	services.put("upload", new UploadService());
	//	services.put("ver", new VersionService());
	//	services.put("version", services.get("ver"));
	services.put("xml", new XMLCatalogService());
	Iterator it = services.values().iterator();
	while (it.hasNext()) {
	    ((Module)it.next()).init(server, this);
	}
    }

    /** Builds a ClientRequest object from the servlet request provided.
     */
    public ClientRequest map(HttpServletRequest request,
			     HttpServletResponse response) {
	
	Privilege privilege = server.getPrivilegeMgr().getPrivilege(request);

	String url = request.getServletPath();
	if (url == null) {
	    url = "/";
	} else {
	    url = url.replace(' ', '+');
	}
	Handle handle = server.getCatalog().getLocked(url);
	if (handle != null) {
	    if (debug()) log.debug(this, "got lock for handle: " + url);
	    return mapToHandle(request, response, handle, privilege);
	} else {
	    return mapByExtension(request, response, url, privilege);
	}
	
    }

    protected ClientRequest mapToHandle(HttpServletRequest request,
					HttpServletResponse response,
					Handle handle,
					Privilege privilege) {
	Service service;
	if (handle instanceof DirHandle) {
	    if (debug()) debug(handle + " is a dir");
	    service = (Service)services.get("dir");
	} else {
	    if (debug()) debug(handle + " is a dataset");
	    service =  (Service)services.get("info");
	}
	String dataPath = handle.getCompleteName();
	return new ClientRequest(request, 
				 response, 
				 privilege,
				 service, 
				 service.getServiceName(), 
				 dataPath,
				 handle);
    }
	

    protected ClientRequest mapByExtension(HttpServletRequest request,
					   HttpServletResponse response,
					   String url,
					   Privilege privilege) {
	if (debug()) debug("parsing extension");

	int lastSlash = url.lastIndexOf('/');
	if (lastSlash < 0) {
	    lastSlash = 0;
	}
	String fileName = url.substring(lastSlash + 1);
	int lastDot = fileName.lastIndexOf('.');
	String extension = fileName.substring(lastDot + 1);

	if (debug()) debug("extension is " + extension);

	Service service = (Service)services.get(extension);

	String dataPath = url.substring(0, url.length() - 
					(extension.length() + 1));
	Handle handle = server.getCatalog().getLocked(dataPath);
	if (debug()) log.debug(this, "got lock for handle: " + dataPath);

	return new ClientRequest(request, 
				 response, 
				 privilege,
				 service, 
				 extension, 
				 dataPath,
				 handle);
    }

    protected Map services;
}
