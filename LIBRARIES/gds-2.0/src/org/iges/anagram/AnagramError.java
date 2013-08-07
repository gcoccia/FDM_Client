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
* Last modified: $Date: 2008/07/22 17:22:26 $ 
* Revision for this file: $Revision: 1.4 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/anagram/AnagramError.java,v $
*/

package org.iges.anagram;

/** Thrown if an unrecoverable error occurs, that normal server code should
 *  not try to anticipate.<p>
 *
 *  This class should be used extremely sparingly, and no code should
 *  ever attempt to catch it except the top-level error handler in 
 *  AnagramServlet.<p>
 * 
 *  It should not be used for problems which are recoverable,
 *  or caused by administrator or user mistakes. These should be handled using
 *  AnagramException and its subclasses.<p>
 *  
 *  Throwing it should be considered the equivalent of a failed "assert"
 *  statement. (In fact once Java 1.4 is widely adopted it may be 
 *  desirable to eliminate this class). <p>
 *  
 *  For instance, appropriate uses might be things like mysteriously 
 *  missing classes and 
 *  libraries, or Java core methods which declare an exception, but 
 *  which should never throw under the circumstances the server is 
 *  calling them. <p>
 *
 *  Careful use of this class eliminates many unnecessary throws declarations
 *  and try blocks for conditions that are not anticipated to occur.
 *
 * @see AnagramException
*/
public class AnagramError 
    extends Error {

    /** Creates an AnagramException with no message */
    public AnagramError() {
	super();
    }
    
    /** Creates an AnagramException with the message given */
    public AnagramError(String message) {
	super(message);
    }
    
}
