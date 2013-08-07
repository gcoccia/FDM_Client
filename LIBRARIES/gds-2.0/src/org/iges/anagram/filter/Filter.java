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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/filter/Filter.java,v $
*/
package org.iges.anagram.filter;

import org.iges.anagram.*;

/** A module that performs a step in the handling of a client request. */
public abstract class Filter 
    extends AbstractModule {

    public String getModuleID() {
	if (moduleID == null) {
	    moduleID = "filter-" + getFilterName();
	}
	return moduleID;
    }

    public void configure(Setting setting) 
	throws ConfigException {
	enabled = setting.getAttribute("enabled", "true").equals("true");
	if (debug()) debug(getModuleID() + " enabled = " + enabled);
    }
	

    /** The name of this filter. Used to build the module ID */
    public abstract String getFilterName();

    /** Sets the filter that this filter should pass requests to */
    public void setNext(Filter next) {
	this.next = next;
    }

    /** Sets whether this filter is enabled */
    public void setEnabled(boolean enabled) {
	this.enabled = enabled;
    }

    /** Indicates whether this filter is enabled. Requests should not
     *  be sent to a filter that is not enabled. */
    public boolean isEnabled() {
	return enabled;
    }

    /** Handles a client request. 
     *  Unless the filter is throwing an exception it should
     *  pass the request on to <code>next.handle()</code>. 
     *  If the <code>enabled</code> property
     *  is set to false, this method should perform no action, and
     *  always pass requests directly to <code>next.handle()</code>.
     */
    public void handle(ClientRequest request) 
	throws ModuleException {
	
	if (enabled) {
	    if (debug()) debug("running " + getModuleID());
	    doFilter(request);
	} else {
	    if (debug()) debug("skipping " + getModuleID());
	    if (next != null) {
		next.handle(request);
	    }
	}
    }

    protected abstract void doFilter(ClientRequest request)
	throws ModuleException;

    protected Filter next;
    protected String filterName;
    protected String moduleID;

    protected boolean enabled = true;

}
