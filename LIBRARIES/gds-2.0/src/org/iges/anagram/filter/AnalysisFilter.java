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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/filter/AnalysisFilter.java,v $
*/
package org.iges.anagram.filter;

import java.util.*;

import org.iges.anagram.*;
import org.iges.anagram.service.*;
    
/** Performs analysis for requests that contain analysis expressions.
 */
public class AnalysisFilter 
    extends Filter {

    protected static String ANALYSIS_PREFIX = "/_expr_";

    public AnalysisFilter() {
	generating = new LinkedList();
	applicable = Arrays.asList(new String[] {
	    "dds", "das", "dods", "info", "asc", "ascii"
	});
    }

    public String getFilterName() {
	return "analysis";
    }

    protected void doFilter(ClientRequest clientRequest) 
	throws ModuleException {

	if (applicable.contains(clientRequest.getServiceName()) &&
	    clientRequest.getHandle() == null &&
	    clientRequest.getDataPath().startsWith(ANALYSIS_PREFIX)) {

	    doAnalysis(clientRequest);
	} else {
	    if (debug()) debug("no analysis to do");
	}

	next.handle(clientRequest);
	
    }

    protected void doAnalysis(ClientRequest clientRequest) 
	throws ModuleException {

	String name = clientRequest.getDataPath();
	String ae = name.substring(ANALYSIS_PREFIX.length());

	if (debug()) log.debug(this, 
			       clientRequest + 
			       "doing analysis for expression " +
			       ae);

	synchronized (generating) {
	    while (generating.contains(name)) {
		if (debug()) log.debug(this, clientRequest + 
				       "waiting for analysis to complete");
		try {
		    generating.wait(0);
		} catch (InterruptedException ie) {}
		
	    }
	    /*
	    if (server.getCatalog().contains(name)) {
		if (debug()) log.debug(this, clientRequest + 
				       "analysis result already in cache");
		return;
	    } 
	    */
	    log.info(this, "evaluating analysis expression: " + ae);
	    generating.add(name);
	}
	
	try {
	    TempDataHandle result = 
		server.getTool().doAnalysis(name, ae, 
					    clientRequest.getPrivilege());
	    
	    server.getCatalog().addTemp(result);
	    clientRequest.setHandle(server.getCatalog().getLocked(clientRequest.getDataPath()));
	    
	    if (debug()) log.debug(this, clientRequest + 
				   "finished analysis");

	} catch (ModuleException me) {
	    throw me;
	} finally {
	    synchronized (generating) {
		generating.remove(name);
		generating.notifyAll();
	    }
	}
    }


    protected List applicable;

    protected List generating;

}
