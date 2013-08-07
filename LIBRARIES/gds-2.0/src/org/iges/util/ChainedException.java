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
* Revision for this file: $Revision: 1.4 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/util/ChainedException.java,v $
*/
package org.iges.util;

/** An exception class supporting chaining. 
 *  
 *  Taken from the article <a href=
 *  "http://developer.java.sun.com/developer/technicalArticles/Programming/exceptions2/"> Exceptional practices, Part 2</a> by Brian Goetz, JavaWorld Oct 2001
 */
public class ChainedException extends Exception {
    private Throwable cause = null;
    
    public ChainedException() {
	super();
    }
    
    public ChainedException(String message) {
	super(message);
    }
    
    public ChainedException(String message, Throwable cause) {
	super(message);
	this.cause = cause;
    }
    
    /** @return The exception that caused this exception, or null if 
     *          there is none.
     */
    public Throwable getCause() {
	return cause;
    }
    
    public String getMessage() {
	if (cause != null) {
	    return super.getMessage() + "; " + cause.getMessage();
	} else {
	    return super.getMessage();
	}
    }
    
    /** Prints the stack trace for the entire chain of exceptions */
    public void printStackTrace() {
	super.printStackTrace();
	if (cause != null) {
	    System.err.println("Caused by:");
	    cause.printStackTrace();
	}
    }

    /** Prints the stack trace for the entire chain of exceptions */
    public void printStackTrace(java.io.PrintStream ps) {
	super.printStackTrace(ps);
	if (cause != null) {
	    ps.println("Caused by:");
	    cause.printStackTrace(ps);
	}
    }

    /** Prints the stack trace for the entire chain of exceptions */
    public void printStackTrace(java.io.PrintWriter pw) {
	super.printStackTrace(pw);
	if (cause != null) {
	    pw.println("Caused by:");
	    cause.printStackTrace(pw);
	}
    }
}

