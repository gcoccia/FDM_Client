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
* Revision for this file: $Revision: 1.6 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/grads/server/MetadataAttribute.java,v $
*/
package org.iges.grads.server;

import java.util.*;
import java.io.Serializable;

import dods.dap.Attribute;

import org.iges.util.Strings;
import org.iges.anagram.AnagramException;

/** Stores a metadata attribute to be added to the DAS document for a
 * dataset. */
public class MetadataAttribute 
    implements Serializable {

    /** Creates a new attribute. The value of the attribute is
     *  automatically converted as follows: For attributes with type
     *  "String", quotation marks `"' are escaped as `\"'. For
     *  attributes of all other types, blanks ` ' are replaced by
     *  commas `, '.
     */
    public MetadataAttribute(String var, 
			     String type, 
			     String name, 
			     String val)  
	throws AnagramException {

	if (name.equals("")) {
	    throw new AnagramException("no attribute name provided" );
	}

	if (! types.containsKey(type)) {
	    throw new AnagramException("invalid attribute type: " + type + 
				       "\n(valid types are " + types.keySet() +
				       ")");
	}

	this.var = var;
	this.type = type;
	this.name = name;
	if (type.equals("String") || type.equals("Str") || type.equals("Url")) {
	    val = Strings.escape(val, '\"');
	    val = Strings.replace(val, "\n", "\",\n\"");
	    this.val = "\"" + val + "\"";
	} else {
	    this.val = Strings.replace(val.trim(), " ", ", ");
	}
	this.intType = parseType(type);
    }

    public String var;
    public String type;
    public String name;
    public String val;
    public int intType;

    public boolean equals(Object obj) {
	if (! (obj instanceof MetadataAttribute)) {
	    return false;
	} else {
	    MetadataAttribute other = (MetadataAttribute)obj;
	    return var.equals(other.var) 
		&& (intType == other.intType)
		&& name.equals(other.name)
		&& val.equals(other.val);
	}
    }
       
	

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(var);
	sb.append(" ");
	sb.append(type);
	sb.append(" ");
	sb.append(name);
	sb.append(" ");
	sb.append(val);
	return sb.toString();
    }
    /*
    public final static List VALID_TYPES = Collections.unmodifiableList
	(Arrays.asList(new String[] 
	    {"String", 
	     "Str",
	     "Byte", 
	     "Int16", 
	     "UInt16", 
	     "Int32", 
	     "UInt32", 
	     "Float32", 
	     "Float64", 
	     "Url" }));
*/
    public static Map types = new HashMap();
    { 
	types.put("String", new Integer(Attribute.STRING));
	types.put("Str", new Integer(Attribute.STRING));
	types.put("Url", new Integer(Attribute.URL));
	types.put("Float32", new Integer(Attribute.FLOAT32));
	types.put("Float64", new Integer(Attribute.FLOAT64));
	types.put("Int16", new Integer(Attribute.INT16));
	types.put("Int32", new Integer(Attribute.INT32));
	types.put("UInt16", new Integer(Attribute.UINT16));
	types.put("UInt32", new Integer(Attribute.UINT32));
    }

    public static int parseType(String typeString) {
	    Integer intType = (Integer)types.get(typeString);
	    if (intType == null) {
		return Attribute.UNKNOWN;
	    } else {
		return intType.intValue();
	    }
	}
	

}
