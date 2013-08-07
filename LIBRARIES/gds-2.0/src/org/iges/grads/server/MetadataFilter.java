/* 
* Copyright (C) 2000-2008 Institute for Global Environment and Society /
*                        Center for Ocean-Land-Atmosphere Studies
* Author: Joe Wielgosz <joew@cola.iges.org>
* 
* This file is part of the GrADS Data Server.
* 
* The GrADS Data Server is free software; you can redistribute
* it and/or modify it under the terms of the GNU General Public
* License as published by the Free Software Foundation; either version
* 2, or (at your option) any later version.
* 
* The GrADS Data Server is distributed in the hope that it will
* be useful, but WITHOUT ANY WARRANTY; without even the implied
* warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with the GrADS Data Server; see the file COPYRIGHT.  If
* not, write to the Free Software Foundation, Inc., 59 Temple Place -
* Suite 330, Boston, MA 02111-1307, USA.
* 
* You can contact IGES/COLA at 4041 Powder Mill Rd Ste 302, Calverton MD 20705.
* 
* Last modified: $Date: 2008/07/22 17:22:28 $ 
* Revision for this file: $Revision: 1.5 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/MetadataFilter.java,v $
*/
package org.iges.grads.server;

import java.io.Serializable;
import java.util.*;

import org.iges.anagram.AnagramException;

/** A filter for whether metadata extracted from a dataset should be
 * sent to the client. The filter tests pairs of the form
 * (variable-name, attribute-name), with one of three possible
 * results: the attribute should be included in the client metadata,
 * removed from the client metadata, or neither, if the filter does
 * not match. */
public class MetadataFilter 
    implements Serializable {

    
    /** Creates a filter. 
     * @param varPrefix The filter will match only when the variable
     * name starts with the string specified. The value of "" always
     * matches.
     * @param varSuffix The filter will match only when the variable
     * name ends with the string specified. The value of "" always
     * matches.
     * @param varName The filter will match only when the variable
     * name is exactly equal to the string specified. The value of ""
     * always matches.
     * @param matchGlobal If true, this filter will only match
     * variable names of "".
     * @param prefix The filter will match only when the attribute
     * name starts with the string specified. The value of "" always
     * matches.
     * @param suffix The filter will match only when the attribute
     * name ends with the string specified. The value of "" always
     * matches.
     * @param name The filter will match only when the attribute
     * name is exactly equal to the string specified. The value of ""
     * always matches.
     * @param sendIfMatch If true, the filter "includes" pairs that
     * match. If false, it "removes" pairs that match.
     */
    public MetadataFilter(String varPrefix, 
			  String varSuffix,
			  String varName,
			  boolean globalOnly,
			  String attPrefix,
			  String attSuffix,
			  String attName,
			  boolean sendIfMatch)
	throws AnagramException {
    	
	if (globalOnly &&
	    ! ( varPrefix.equals("") && varSuffix.equals("") 
		&& varName.equals(""))
	    ) {
	    throw new AnagramException
		("can't use global_only together with variable " +
		 "prefix/suffix/name\n");
	}

	if (( ! varName.equals("")) && 
	    ! ( varPrefix.equals("") && varSuffix.equals(""))) {
	    throw new AnagramException
		("can't use variable name together with variable " +
		 "prefix/suffix\n");
	}	    

	if (( ! attName.equals("")) && 
	    ! ( attPrefix.equals("") && attSuffix.equals(""))) {
	    throw new AnagramException
		("can't use attribute name together with attribute " +
		 "prefix/suffix\n");
	}	    

	this.varPrefix = varPrefix; 
	this.varSuffix = varSuffix;
	this.varName = varName;
	this.globalOnly = globalOnly;
	this.attPrefix = attPrefix;
        this.attSuffix = attSuffix;
        this.attName = attName;
        this.sendIfMatch = sendIfMatch;
    }

    /** @return True if the (var, attribute) pair should be removed
     *  from the client metadata. A false result does not imply that the
     *  metadata should be included; a separate call to include() should
     *  be made. 
     */
    public boolean shouldRemove(MetadataAttribute attribute) {
	if (sendIfMatch) {
	    return false;
	} else {
	    return match(attribute);
	}
    }

    /** @return True if the (var, attribute) pair should be include
     *  from the client metadata. A false result does not imply that the
     *  metadata should be included; a separate call to include() should
     *  be made. 
     */
    public boolean shouldInclude(MetadataAttribute attribute) {
	if (!sendIfMatch) {
	    return false;
	} else {
	    return match(attribute);
	}
    }

    /** Returns true if the variable name and attribute name
     *  match the prefixes, suffixes, and names given.  */
    protected boolean match(MetadataAttribute attribute) {
	// Note that startsWith() and endsWith() always return
	// a match when passed an argument of "" 
	if (DEBUG) {
	    System.out.println("matching " + attribute + " vs:\n" + this);
	    System.out.println("tests: " + 
			       (!globalOnly || attribute.var.equals("")) + " " +
			       attribute.var.startsWith(varPrefix) + " " +
			       attribute.var.endsWith(varSuffix) + " " +
			       (varName.equals("") || varName.equals(attribute.var)) + " " +
			       attribute.name.startsWith(attPrefix) + " " +
			       attribute.name.endsWith(attSuffix) + " " +
			       (attName.equals("") || attName.equals(attribute.name)));
	}

	return 
	    (!globalOnly || attribute.var.equals("")) &&
	    attribute.var.startsWith(varPrefix) &&
	    attribute.var.endsWith(varSuffix) &&
	    (varName.equals("") || varName.equals(attribute.var)) &&
	    attribute.name.startsWith(attPrefix) &&
	    attribute.name.endsWith(attSuffix) &&
	    (attName.equals("") || attName.equals(attribute.name));
    }

    /** If any of the filters in the given list recommends removing
     *  the attribute, returns false. Otherwise, if any filter in the
     *  list recommends including the attribute, returns true. If no filters
     *  recommend anything, returns the default value given. 
     */
    public static boolean attributePassesFilters(MetadataAttribute attribute,
						 List filters, 
						 boolean defaultVal) {
	Iterator it = filters.iterator();
	while (it.hasNext()) {
	    MetadataFilter current = (MetadataFilter)it.next();
	    if (current.shouldInclude(attribute)) {
		if (DEBUG) System.out.println("include " + attribute);
		defaultVal = true;
	    }
	    if (current.shouldRemove(attribute)) {
		if (DEBUG) System.out.println("remove " + attribute);
		return false;
	    }
	}
	return defaultVal;
    }
	     

    protected String varPrefix;
    protected String varSuffix;
    protected String varName;
    protected boolean globalOnly;
    protected String attPrefix;
    protected String attSuffix;
    protected String attName;
    protected boolean sendIfMatch;

    public boolean equals(Object obj) {
	if (! (obj instanceof MetadataFilter)) {
	    return false;
	} else {
	    MetadataFilter other = (MetadataFilter)obj;
	    return varPrefix.equals(other.varPrefix) 
		&& varSuffix.equals(other.varSuffix)
		&& varName.equals(other.varName)
		&& (globalOnly == other.globalOnly)
		&& attPrefix.equals(other.attPrefix)
		&& attSuffix.equals(other.attSuffix)
		&& attName.equals(other.attName)
		&& (sendIfMatch == other.sendIfMatch);
	}
    }

    public String toString() {
	return 
	    "varPrefix: " + varPrefix +
	    " varSuffix: " + varSuffix +
	    " varName: " + varName + 
	    " globalOnly: " + globalOnly + "\n" +
	    "attPrefix: " + attPrefix + 
	    " attSuffix: " + attSuffix + 
	    " attName: " + attName + 
	    " sendIfMatch: " + sendIfMatch;
	    
    }

    /** Filters to remove all COARDS metadata. Exceptions are the
     * 'units' and 'long_name' attributes, since these are not
     * supplied by GrADS. 
     */
    public static List COARDS_FILTERS = null;
    static {
	try {
	    COARDS_FILTERS = Arrays.asList
		(new MetadataFilter[] {
		     new MetadataFilter("", "", "", false, 
					"", "", "title", false),
		     new MetadataFilter("", "", "", false, 
					"", "", "convention", false),
		     new MetadataFilter("", "", "", false, 
					"", "", "missing_value", false),
		     new MetadataFilter("", "", "", false, 
					"", "", "_FillValue", false),
		     new MetadataFilter("", "", "", false, 
					"", "", "scale_factor", false),
		     new MetadataFilter("", "", "", false, 
					"", "", "add_offset", false),
		     new MetadataFilter("", "", "", false, 
					"", "", "Conventions", false),
		 });
	} catch (AnagramException ae) {
	    System.out.println(MetadataFilter.class + 
			       " initialization error! (" + ae.getMessage() + ")");
	}
    }

    private final static boolean DEBUG = false;

}
