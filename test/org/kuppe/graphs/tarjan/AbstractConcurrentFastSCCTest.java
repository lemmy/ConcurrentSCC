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

package org.kuppe.graphs.tarjan;

import java.util.Set;

public abstract class AbstractConcurrentFastSCCTest {

//	@Rule public TestName name = new TestName();
//
//	@Before
//	public void before() {
//		System.out.println("=================================================================");
//		System.out.println("==================== " + name.getMethodName() + " =========================");
//		System.out.println("=================================================================");
//	}
//
//	@After
//	public void after() {
//		System.out.println("=================================================================");
//		System.out.println("==================== " + name.getMethodName() + " =========================");
//		System.out.println("=================================================================\n\n");
//	}

	protected String printSCC(Set<GraphNode> scc) {
		StringBuffer buf = new StringBuffer(scc.size());
		buf.append("{");
		for (GraphNode graphNode : scc) {
			buf.append(graphNode.getId());
			buf.append(",");
		}
		removeIfDangling(buf, ",");
		buf.append("}");
		return buf.toString();
	}

	private void removeIfDangling(StringBuffer buf, String string) {
		if (buf.lastIndexOf(",") == buf.length() - 1) {
			buf.setLength(buf.length() - 1);
		}
	}

	protected String printSCCs(Set<Set<GraphNode>> sccs) {
		StringBuffer buf = new StringBuffer(sccs.size());
		buf.append("Found SCCs: [");
		for (Set<GraphNode> set : sccs) {
			buf.append(printSCC(set));
			buf.append(",");
		}
		removeIfDangling(buf, ",");
		buf.append("]");
		return buf.toString();
	}

}
