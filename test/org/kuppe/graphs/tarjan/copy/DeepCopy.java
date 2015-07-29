/*******************************************************************************
 * Copyright (c) 2015 Microsoft Research. All rights reserved. 
 *
 * The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Contributors:
 *   Markus Alexander Kuppe - initial API and implementation
 ******************************************************************************/

package org.kuppe.graphs.tarjan.copy;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

// see http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
public class DeepCopy {
	
	public static Object copy(Object orig) {
		try {
			// Write the object out to a byte array
			FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(orig);
			out.flush();
			out.close();
			
			// Make an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = 
                new ObjectInputStream(bos.getInputStream());
			return in.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object copy(Object orig, final int size) {
		try {
			// Write the object out to a byte array
			FastByteArrayOutputStream bos = new FastByteArrayOutputStream(size);
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(orig);
			out.flush();
			out.close();
			
			// Make an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = 
                new ObjectInputStream(bos.getInputStream());
			return in.readObject();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
