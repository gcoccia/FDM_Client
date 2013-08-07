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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/PrivilegeMgr.java,v $
*/
package org.iges.anagram;

import java.util.*;
import javax.servlet.http.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;

/** Manages a hierarchical collection of privilege sets, and assigns them
 *  to incoming requests. 
 */
public class PrivilegeMgr
    extends AbstractModule {

    public PrivilegeMgr() {
	try {
	    DocumentBuilder blankBuilder = 
		DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    privilegeDocument = blankBuilder.newDocument();
	} catch (ParserConfigurationException pce) {
	    // if we can't create XML documents, something is very wrong!
	    throw new AnagramError("XML parser configuration error." + 
				   pce.getMessage());
	}
	
    }
	
    // module interface

    public final String getModuleID() {
	return "privilege_mgr";
    }


    public void configure(Setting setting)
    throws ConfigException {

	buildPrivileges(setting);

	buildInheritance(setting);

	buildIPRanges(setting);
	
	setDefaultPrivilege(setting);
	
    }

    /** Returns the privilege set 
     *  associated with the given HTTP servlet request */
    public Privilege getPrivilege(HttpServletRequest request) {
	String requestIP = request.getRemoteAddr();

	SortedMap possibleMatches = ipRanges.tailMap(requestIP);
	Iterator it = possibleMatches.keySet().iterator();
	while (it.hasNext()) {
	    String mask = (String)it.next();
	    if (requestIP.startsWith(mask)) {
		return (Privilege)ipRanges.get(mask);
	    }
	}

	if (debug()) log.debug(this, "returning default privilege");
	return defaultPrivilege;
    }

    protected void buildPrivileges(Setting setting) {
	List privilegeSettings = setting.getSubSettings("privilege");
	privileges = new HashMap();
	Iterator it = privilegeSettings.iterator();
	while (it.hasNext()) {
	    Setting current = (Setting)it.next();
	    String name = current.getAttribute("name");	    
	    if (name.equals("")) {
		log.error(this, 
			  "privilege tag has no name attribute; skipping");
		continue;
	    }
	    verbose("creating privilege level " + name);
	    try {
		Element imported = 
		    (Element)privilegeDocument.importNode(current.getXML(), 
							  true);
		privileges.put(name, new Privilege(name, imported));
	    } catch (DOMException de) {
		// indicates an exceptional condition
		throw new AnagramError(de.getMessage());
	    }
	}
    }

    protected void buildInheritance(Setting setting) 
	throws ConfigException {

	List privilegeSettings = setting.getSubSettings("privilege");
	Iterator it = privilegeSettings.iterator();
	while (it.hasNext()) {
	    Setting current = (Setting)it.next();
	    String parentName = current.getAttribute("inherit");
	    String name = current.getAttribute("name");
	    Privilege privilege = (Privilege)privileges.get(name);

	    if (privilege == null || parentName.equals("")) {
		continue;
	    }

	    Privilege parent = (Privilege)privileges.get(parentName);
	    if (parent == null) {
		throw new ConfigException(this,
					  "inheriting from non-existent " + 
					  "privilege", current);
	    }
	    try {
		verbose(name + " inherits from " + parent);
		privilege.setParent(parent);
	    } catch (AnagramException ae) {
		throw new ConfigException(this, ae.getMessage(), current);
	    }	    
	}
    }

    protected void setDefaultPrivilege(Setting setting) 
	throws ConfigException {
	if (setting.getAttribute("default").equals("")) {
	    if (verbose()) verbose("creating empty default privilege");
	    Element blankDefault = 
		privilegeDocument.createElement("privilege");
	    defaultPrivilege = new Privilege("default", blankDefault);
	} else {
	    String defaultName = setting.getAttribute("default");
	    defaultPrivilege = (Privilege)privileges.get(defaultName);
	    if (defaultPrivilege == null) {
		throw new ConfigException(this,
					  "default attribute set to " + 
					  "nonexistent privilege", 
					  setting);
	    }
	}
    }
    
    protected void buildIPRanges(Setting setting) {
	ipRanges = new TreeMap(Collections.reverseOrder()); 
	List ipRangeSettings = setting.getSubSettings("ip_range");
	Iterator it = ipRangeSettings.iterator();
	while (it.hasNext()) {
	    Setting current = (Setting)it.next();

	    String mask = current.getAttribute("mask");	    
	    if (mask.equals("")) {
		log.error(this, 
			  "ip_range tag has no mask attribute; skipping");
		continue;
	    }
	    mask = addDot(mask);

	    String name = current.getAttribute("privilege");	    
	    if (name.equals("")) {
		log.error(this, 
			  "ip_range tag has no privilege attribute; skipping");
		continue;
	    }
	    Privilege privilege = (Privilege)privileges.get(name);
	    if (privilege == null) {
		log.error(this, 
			  "ip_range tag refers to non-existent privilege " + 
			  name + "; skipping");
		continue;
	    }

	    ipRanges.put(mask, privilege);
	}
    }

    protected String addDot(String ip) {
	if (ip.endsWith(".")) {
	    return ip;
	}

	StringTokenizer st = new StringTokenizer(ip, ".");
	if (st.countTokens() >= 4) {
	    return ip;
	} else {
	    return ip + ".";
	}
    }

    protected SortedMap ipRanges;
    protected Map privileges;
    protected Document privilegeDocument;
    protected Privilege defaultPrivilege;

}
