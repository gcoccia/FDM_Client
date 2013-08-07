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
* Last modified: $Date: 2008/07/22 17:22:28 $ 
* Revision for this file: $Revision: 1.11 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/util/Strings.java,v $
*/
package org.iges.util;

public class Strings {

    public static String replace(String source, String findStr, String replaceStr) {
	if (source == null) { return null; }

	int start = 0;
	StringBuffer result = new StringBuffer();

	while (true) {
	    int pos = source.indexOf(findStr, start);
	    if (pos == -1) {
		result.append(source.substring(start));
		break;
	    }
	    result.append(source.substring(start, pos));
	    result.append(replaceStr);
	    start = pos + 1;
	    //	    System.out.println(result.toString());
	}
	
	return result.toString();
    }

    public static String escape(String source, char escape) {
	return replace(source, "" + escape, "\\" + escape);
    }

    public static String escapeXMLSpecialChars(String source) {
       	source = replace(source, "&", "&amp;"); // must come first
	source = replace(source, "<", "&lt;");
	source = replace(source, ">", "&gt;");
	source = replace(source, "\'", "&apos;");
	source = replace(source, "\"", "&quot;");
	source = replace(source, "·", "");
	source = replace(source, "¼", "0.25 ");
	source = replace(source, "½", "0.5 ");
	source = replace(source, "°", "degree ");
	source = replace(source, "×", "by ");
	return source;
    }
}
