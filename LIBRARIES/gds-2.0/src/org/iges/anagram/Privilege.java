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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/Privilege.java,v $
*/
package org.iges.anagram;

import java.util.*;

import org.w3c.dom.*;

/** An extension of Setting which represents a set of privileges that can be 
 *  associated with a given request. */
public class Privilege 
    extends Setting {

    /** Creates a privilege set with the given name, 
     * based on the XML tag provided. */
    public Privilege(String name, Element xml) {
	super(xml);
	this.name = name;
	allowTags = sortPathTags("allow");
	denyTags = sortPathTags("deny");
    }

    /** Used by the privilege manager when building the privilege
     *  hierarchy. 
     */
    void setParent(Privilege parent) 
	throws AnagramException {
	
	Privilege ancestor = parent;
	while (ancestor != null) {
	    if (ancestor == this) {
		throw new AnagramException("circular relationship between " + 
					   "privileges " + this.getName() + 
					   " and " + parent.getName());
	    }
	    ancestor = parent.getParent();
	} 

	this.parent = parent;
    }
    
    protected Privilege getParent() {
	return parent;
    }

    /** Returns the name of this privilege set. */
    public String getName() {
	return name;
    }

    public String toString() {
	return name;
    }

    public String getAttribute(String name, String defaultValue) {
	String value = super.getAttribute(name, "");
	if (value.equals("")) {
	    if (parent == null) {
		value = defaultValue;
	    } else {
		value = getParent().getAttribute(name, defaultValue);
	    }
	}
	return value;
    }

    public long getNumAttribute(String name, long defaultValue) {
	long value = super.getNumAttribute(name, Long.MIN_VALUE);
	if (value == Long.MIN_VALUE) {
	    if (parent == null) {
		value = defaultValue;
	    } else {
		value = getParent().getNumAttribute(name, defaultValue);
	    }
	}
	return value;
    }

    /** Returns true if this privilege set permits access to the catalog
     *  path given. */
    public boolean allows(String path) {
	String denyMatch = getDenyMatch(path);
	if (denyMatch == null) {
	    return true;
	} 

	String allowMatch = getAllowMatch(path);
	if (allowMatch == null) {
	    return false;
	}
	    
	if (allowMatch.length() == denyMatch.length()) {
	    return allowIsMostRecent(allowMatch);
	} 

	return (allowMatch.length() > denyMatch.length());
    }

    /** Returns true if there is any possibility that the privilege
     *  set will permit access to a sub-path of the path given.
     */
    public boolean everAllows(String path) {
	if (allows(path)) {
	    return true;
	} else {
	    return checkForAllowedSubPath(path);
	}
    }

    protected boolean checkForAllowedSubPath(String path) {
	Iterator it = allowTags.headSet(path).iterator();
	while (it.hasNext()) {
	    String next = (String)it.next();
	    if (next.startsWith(path)) {
		return true;
	    }
	}
	if (parent != null) {
	    return parent.checkForAllowedSubPath(path);
	} else {
	    return false;
	}
    }
	    
	    
    protected boolean allowIsMostRecent(String path) {
	if (allowTags.contains(path)) {
	    return true;
	}
	if (denyTags.contains(path)) {
	    return false;
	}
	if (parent != null) {
	    return parent.allowIsMostRecent(path);
	} else {
	    return true;
	}
    }

    protected SortedSet sortPathTags(String tagName) {

	SortedSet returnVal = new TreeSet(Collections.reverseOrder());
	Iterator it = getSubSettings(tagName).iterator();
	while (it.hasNext()) {
	    String path = ((Setting)it.next()).getAttribute("path");
	    if (!path.startsWith("/")) {
		path = '/' + path;
	    }
	    returnVal.add(path);
	}
	return returnVal;
    }

    protected String getAllowMatch(String path) {
	String parentMatch = null;
	if (parent != null) { 
	    parentMatch = parent.getAllowMatch(path);
	}
	return getClosestMatch(parentMatch, allowTags, path);
    }

    protected String getDenyMatch(String path) {
	String parentMatch = null;
	if (parent != null) { 
	    parentMatch = parent.getDenyMatch(path);
	}
	return getClosestMatch(parentMatch, denyTags, path);
    }

    protected String getClosestMatch(String parentMatch, 
				     SortedSet pathSet, 
				     String path) {
	String localMatch = null;

	Iterator it = pathSet.tailSet(path).iterator();
	while (it.hasNext()) {
	    String next = (String)it.next();
	    if (path.startsWith(next)) {
		localMatch = next;
	    }
	}

	if (localMatch == null) {
	    return parentMatch;
	} 
	if (parentMatch == null) {
	    return localMatch;
	} 

	if (localMatch.length() < parentMatch.length()) {
	    return parentMatch;
	} else {
	    return localMatch;
	}
    }


    protected SortedSet allowTags;
    protected SortedSet denyTags;

    protected String name;
    protected Privilege parent;


}
