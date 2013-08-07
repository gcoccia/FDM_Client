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
* Revision for this file: $Revision: 1.7 $
* Release name: $Name: v2_0 $
* Original for this file: $Source: /homes/cvsroot/gds/gds/src/org/iges/util/Spooler.java,v $
*/
package org.iges.util;

import java.io.*;

/** A collection of methods that efficiently 
 *  spool data from a finite-length source to a sink. 
 */ 
public class Spooler {

    /** Spools character data from a Reader to a Writer 
     *  until the Reader is exhausted. <p>
     *  Warning: if called on a stream with unlimited length 
     *  (such as a network socket) this method will not return until the
     *  stream is closed.
     *  A buffer of 1024 characters is used for read and write operations.
     * @throws IOException If an I/O error occurs.
     */
    public static long spool(Reader r, Writer w) 
	throws IOException {
	return spool(r, w, new char[1024]);
    }

    /** Spools character data from a Reader to a Writer
     *  until the Reader is exhausted.<p>
     *  Warning: if called on a stream with unlimited length 
     *  (such as a network socket) this method will not return until the
     *  stream is closed.
     * @param bufferSize The buffer size to use for read and write operations.
     * @throws IOException If an I/O error occurs.
     */
    public static long spool(Reader r, Writer w, char[] buf) 
	throws IOException {
	
	// Allocate a buffer to read bytes into and out of
	// char[] buf = new char[bufferSize];

	// Write data
	int bytesRead = 0;
	long totalBytesRead = 0;

	while (true) {
	    // Read the next chunk
	    bytesRead = r.read(buf);
	    // Check for eof
	    if (bytesRead < 0) {
		break;
	    }
	    // Write it out
	    w.write(buf, 0, bytesRead);
	    totalBytesRead += bytesRead;
	}
	return totalBytesRead;
    }


    /** Spools byte data from an InputStream to an OutputStream
     *  until the InputStream is exhausted.<p>
     * A buffer of 1024 bytes is used for read and write operations.<p>
     *  Warning: if called on a stream with unlimited length 
     *  (such as a network socket) this method will not return until the
     *  stream is closed.
     * @throws IOException If an I/O error occurs.
     */
    public static long spool(InputStream in, OutputStream out) 
	throws IOException {
	return spool(in, out, new byte[1024]);
    }

    /** Spools byte data from an InputStream to an OutputStream
     *  until the InputStream is exhausted.<p>
     *  Warning: if called on a stream with unlimited length 
     *  (such as a network socket) this method will not return until the
     *  stream is closed.
     * @param bufferSize The buffer size to use for read and write operations.
     * @throws IOException If an I/O error occurs.
     * @throws IllegalArgumentException If bufferSize is <= 0.
     */
    public static long spool(InputStream in, 
			     OutputStream out, 
			     byte[] buf) 
	throws IOException {

	// Allocate a buffer to read bytes into and out of
	// byte[] buf = new byte[bufferSize];

	// Write data
	int bytesRead = 0;
	long totalBytesWritten = 0;

	while (true) {
	    // Read the next chunk
	    if (DEBUG) System.err.print("reading...");
	    bytesRead = in.read(buf);
	    // Check for eof
	    if (bytesRead < 0) {
	      if (DEBUG) System.err.println("eof");
		break;
	    }
	    if (DEBUG) System.err.print("got " + bytesRead + 
					" bytes. writing...");
	    out.write(buf, 0, bytesRead);
	    if (DEBUG) System.err.println("done.");
	    totalBytesWritten += bytesRead;
	}
	return totalBytesWritten;
    }

    /** Spools a fixed quantity of byte data from an 
     *  InputStream to an OutputStream.
     * @param totalBytesToWrite The number of bytes that should be read. 
     * @param bufferSize The buffer size to use for read and write operations.
     * @throws IOException If an I/O error occurs, or the InputStream returns
     * EOF before <code>totalBytesToWrite</code> bytes have been read.
     */
    public static long spool(long totalBytesToWrite, 
			     InputStream in, 
			     OutputStream out, 
			     byte[] buf) 
	throws IOException {
	
	// Allocate a buffer to read bytes into and out of
	// byte[] buf = new byte[bufferSize];

	// Write data
	int bytesRead = 0;
	long totalBytesWritten = 0;

	while (true) {
	    // Read the next chunk
	    if (DEBUG) System.err.println("reading...");
	    bytesRead = in.read(buf);
	    if (DEBUG) System.err.print("read " + bytesRead + " bytes...");
	    if (bytesRead < 0) {
		throw new IOException("ran out of input while spooling " + 
				      totalBytesToWrite + " bytes");
	    }
	    long bytesLeft = totalBytesToWrite - totalBytesWritten;
	    if (DEBUG) System.err.println("writing...");
	    if (bytesLeft <= bytesRead) {
		out.write(buf, 0, (int)bytesLeft);
		if (DEBUG) System.err.print("wrote " + 
					    bytesLeft + " bytes...");
		totalBytesWritten += bytesLeft;
		break;
	    } else {
		out.write(buf, 0, bytesRead);
		totalBytesWritten += bytesRead;
		if (DEBUG) System.err.print("wrote " + 
					    bytesRead + " bytes...");
	    }
	    if (DEBUG) System.err.println("bytes left = " + bytesLeft + 
					  " of " + totalBytesToWrite);
	}
	if (DEBUG) System.err.println("done");
	return totalBytesWritten;
    }

    private final static boolean DEBUG = false;

}
