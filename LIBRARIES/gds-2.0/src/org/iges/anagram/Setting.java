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
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/Setting.java,v $
*/
package org.iges.anagram;

import java.util.*;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;

/** An encapsulation of XML tag data from the server's configuration file. 
 *  Each Setting object provides access to the attributes and subtags of 
 *  a single XML tag. */
public class Setting {

    /** Creates a Setting object with no attributes or subtags */
    /*
    public Setting() 
	throws AnagramException {

	this.xml = blankElement();
    }
    */

    /** Creates a Setting object from the given XML tag. */
    public Setting(Element xml) {
	this.xml = xml;
    }

    /** Creates a Setting object from the top-level tag of the given 
     *  XML document. 
     * @param name The name of the top-level tag 
     */
    public Setting(String name, Document document)
	throws AnagramException {
	//{

	//	this.xml = uniqueElement(name, xmlList);
	NodeList xmlList = document.getElementsByTagName(name);
	this.xml = uniqueElement(name, document, xmlList);
    }
    
    /** Returns the name of this XML tag */
    public String getName() {
	return xml.getTagName();
    }

    /** Returns a list of Setting objects representing all sub-tags
     *  of this XML tag with the given name.
     */
    public List getSubSettings(String name) {
	List subSettings = new ArrayList();
	NodeList xmlList = xml.getElementsByTagName(name);
	for (int i = 0; i < xmlList.getLength(); i++) {
	    subSettings.add(new Setting((Element)xmlList.item(i)));
	}
	return subSettings;
    }

    /** Returns a list of Setting objects representing all sub-tags
     *  of this XML tag.
     */
    public List getSubSettings() {
	return getSubSettings("*");
    }

    /** Returns the sub-tag of this XML tag with the given name.
     * @throws AnagramException If more than one sub-tag exists with that name.
     */
    public Setting getUniqueSubSetting(String name) 
    throws AnagramException {
	//{

	NodeList xmlList = xml.getElementsByTagName(name);
	Document document = xml.getOwnerDocument();
	return new Setting(uniqueElement(name, document, xmlList));

	//	return new Setting(uniqueElement(name, xmlList));
    }

    /** Returns the DOM interface to the XML tag associated with this
     *  Setting.
     */
    public Element getXML() {
	return xml;
    }

    /** Returns the numerical value of the named attribute of this tag.
     * @param defaultValue Returned if the tag has no such attribute, 
     *  or the attribute's value is non-numeric.
     */
    public long getNumAttribute(String name, long defaultValue) {
	if (xml.getAttribute(name) == "") {
	    return defaultValue;
	}
	try {
	    return Long.valueOf(xml.getAttribute(name)).longValue();
	} catch (NumberFormatException nfe) {
	    return defaultValue;
	}
    }

    /** Returns the value of the named attribute of this tag. Returns
     * "" if the tag has no such attribute.
     */
    public String getAttribute(String name) {
	return getAttribute(name, "");
    }

    /** Returns the value of the named attribute of this tag. 
     * @param defaultValue Returned if the tag has no such attribute.
     */
    public String getAttribute(String name, String defaultValue) {
	if (xml.getAttribute(name) == "") {
	    return defaultValue;
	} else {
	    return xml.getAttribute(name);
	}
    }
	
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("<");
	sb.append(xml.getTagName());
	NamedNodeMap map = xml.getAttributes();
	for (int i = 0; i < map.getLength(); i++) {
	    sb.append(" ");
	    sb.append(((Attr)map.item(i)).getName());
	    sb.append("=\"");
	    sb.append(((Attr)map.item(i)).getValue());
	    sb.append("\"");
	}
	List subSettings = getSubSettings("*");
	if (subSettings.size() == 0) {
	    sb.append("/>");
	} else {
	    sb.append(">");
	    /*
	    for (Iterator it = subSettings.iterator(); it.hasNext(); ) {
		sb.append("\n");
		sb.append(it.next());
	    }
	    sb.append("\n</");
	    sb.append(xml.getTagName());
	    sb.append(">");
	    */
	}
	return sb.toString();
    }

    //    protected Element uniqueElement(String name, NodeList xmlList)
    protected Element uniqueElement(String name, 
				    Document document, 
				    NodeList tagList)
    throws AnagramException 
    {
	if (tagList.getLength() < 1 ) {
	    return document.createElement(name);
	} else if (tagList.getLength() > 1) {
	    throw new AnagramException("duplicate tags for " + name);
	} else {
	    return (Element)tagList.item(0);	
	}
    }
    
    /*
    protected Element blankElement() {

	if (blankBuilder == null) {
	    try {
		blankBuilder = 
		    DocumentBuilderFactory.newInstance().newDocumentBuilder();
	    } catch (ParserConfigurationException pce) {
		// if we can't make XML tags, we're in big trouble!
		throw new AnagramError("XML parser configuration error." + 
					   pce.getMessage());
	    }
	}
	Document document = blankBuilder.newDocument();
	return .createElement(name);
    }
    */
	    

    protected Element xml;

    //    protected static DocumentBuilder blankBuilder;


}
