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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/filter/OverloadFilter.java,v $
*/
package org.iges.anagram.filter;

import java.util.*;

import org.iges.anagram.*;

/** Tracks the number of simultaneous requests, and blocks those
 *  that exceed the server's limit. 
 */
public class OverloadFilter 
    extends Filter {

    public String getFilterName() {
	return "overload";
    }

    public void configure(Setting setting) 
	throws ConfigException {

	super.configure(setting);
	limit = (int)setting.getNumAttribute("limit", 0);
    }

    public void doFilter(ClientRequest clientRequest) 
	throws ModuleException {

	synchronized(this) {
	    if (limit > 0 && currentLoad >= limit) {
		fail("server is experiencing heavy load. " + 
		     "please try again later.");
	    }
	    currentLoad++;
	    if (debug()) debug("current load: " + currentLoad + " threads");
	    if (currentLoad > maxLoad) {
		maxLoad = currentLoad;
	    }
	} 
	
	try {
	    next.handle(clientRequest);
	} catch (ModuleException me) {
	    throw me;
	} catch (RuntimeException re) {
	    throw re;
	} catch (Error e) {
	    throw e;
	} finally {
	    synchronized(this) {
		currentLoad--;
	    }
	}
    }

    protected int limit;
    protected int currentLoad;
    protected int maxLoad;
    
}
