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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.kuppe.graphs.tarjan.RepeatRule.Repeat;

/*
md5sums
=======
2bac4b28fa82fc8e7590c4d1b8d7247c  allocatorSetSize8.bin
1adf32e3bcdce38c019e7c7f88076143  allocatorSetSize9.bin
1ca1f7174fa58219a74c8cbd308d4c6b  alternatingBitLen04.bin
731f50cc56292daabc6b72b102186a27  alternatingBitLen32.bin
0126c4267176ca10b8512a82b3b4e788  BakeryN3.bin
db48dfa272f563ae95aaa59137822b97  largeDGsccs.txt
78ffa13d3318b8f61e3a0c200e659026  largeDG.txt
a30f428391f45c9729ba142fbbe67a1a  mediumDG.txt
b7d4f9c5cd16a5e77d039a8de15f0460  tinyDG.txt
cf4bd51065f4f7b0cf36c69b051aef88  tlcinits.txt
c205becd381260b4bf297a60c7e012cd  tlcn09.bin
417001e7657579f89b2218f0b0535b0b  tlcn10.bin
579b20528512f7ed689c8d7935c363e6  tlcn10.txt
58af038bbdb58e4c7a6f4afd24c695c9  tlcsccs.txt
f6b5f91266751ea4b4f0e1f1840fd7f0  tlc.txt
ab39e4d6d012642fb015ce2c9dc82b14  voteproofN4.bin
be68c065d99ff1ae7874c553c7296060  voteproofN4.bin.violation

Manually download from http://bugzilla.tlaplus.net/TarjancSCC/

 */
public class ConcurrentFastSCCTestFromFile extends AbstractConcurrentFastSCCTest {

	@Rule
	public RepeatRule repeatRule = new RepeatRule();

	/*
	    ============================
		=Histogram vertex out-degree=
		============================
		Observations: 7096320
		Min: 3
		Max: 20
		Mean: 5.87
		Median: 6
		Standard deviation: 0.92
		75%: 6.00
		95%: 6.00
		98%: 6.00
		99%: 7.00
		99.9%: 19.00
		numEdges/occurrences (log scale)
		--------------------------------
		03:1680 ########
		04:76134 ############
		05:1149693 ##############
		06:5787926 ################
		07:53111 ###########
		17:56 #####
		18:2515 ########
		19:25054 ###########
		20:151 ######
		============================
		============================
		=Histogram vertex in-degree=
		============================
		Observations: 7096320
		Min: 2
		Max: 21
		Mean: 5.87
		Median: 6
		Standard deviation: 0.98
		75%: 6.00
		95%: 6.00
		98%: 6.00
		99%: 7.00
		99.9%: 19.00
		numEdges/occurrences (log scale)
		--------------------------------
		02:420 #######
		03:26427 ###########
		04:415323 #############
		05:398938 #############
		06:6170634 ################
		07:56802 ###########
		16:28 ####
		17:840 #######
		18:867 #######
		19:26037 ###########
		21:04 ##
		============================
		============================
		=Histogram SCC sizes=
		============================
		Observations: 0
		Min: -1
		Max: -1
		Mean: -1.00
		Median: -1
		Standard deviation: -1.00
		75%: -1.00
		95%: -1.00
		98%: -1.00
		99%: -1.00
		99.9%: -1.00
		numEdges/occurrences (log scale)
		--------------------------------
		============================
		0 SCC found during liveness checking.	 
	*/
	@Test
	@Ignore
	public void testAlternatingBitLen32() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCAlternatingBitLen32");
		readBinFile(graph, "/data/alternatingBitLen32.bin"); // TLC needs ~50 seconds to search SCCs
		Assert.assertEquals(7096320, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(1, sccs.size());
	}
	
	@Test
	public void testAlternatingBitLen04() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCAlternatingBitLen04");
		readBinFile(graph, "/data/alternatingBitLen04.bin");
		Assert.assertEquals(16800, graph.size());

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(1, sccs.size());
		for (Set<GraphNode> set : sccs) {
			Assert.assertEquals(16800, set.size());
		}
	}
	
	/*
	    ============================
		=Histogram vertex out-degree=
		============================
		Observations: 15728640
		Min: 1
		Max: 49
		Mean: 9.61
		Median: 9
		Standard deviation: 4.69
		75%: 12.00
		95%: 18.00
		98%: 22.00
		99%: 23.00
		99.9%: 31.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:32 ####
		02:327152 #############
		03:1182336 ##############
		04:141304 ############
		05:872480 ##############
		06:2491104 ###############
		07:527600 ##############
		08:889680 ##############
		09:2453696 ###############
		10:834264 ##############
		11:1162496 ##############
		12:1463744 ###############
		13:706160 ##############
		14:494480 ##############
		15:547680 ##############
		16:327656 #############
		17:456992 ##############
		18:197344 #############
		19:151424 ############
		20:91952 ############
		21:62944 ############
		22:38728 ###########
		23:159936 ############
		24:9344 ##########
		25:47968 ###########
		26:1024 #######
		27:18480 ##########
		29:42560 ###########
		31:14280 ##########
		33:4816 #########
		35:5376 #########
		37:2856 ########
		39:112 #####
		41:448 #######
		43:168 ######
		45:16 ###
		49:08 ###
		============================
		============================
		=Histogram vertex in-degree=
		============================
		Observations: 15728640
		Min: 2
		Max: 32
		Mean: 9.61
		Median: 9
		Standard deviation: 3.12
		75%: 11.00
		95%: 16.00
		98%: 19.00
		99%: 21.00
		99.9%: 26.00
		numEdges/occurrences (log scale)
		--------------------------------
		02:08 ###
		03:2016 ########
		04:36752 ###########
		05:273672 #############
		06:1107792 ##############
		07:2517480 ###############
		08:3181248 ###############
		09:2117024 ###############
		10:1479296 ###############
		11:1546440 ###############
		12:1415176 ###############
		13:543760 ##############
		14:562632 ##############
		15:93968 ############
		16:275464 #############
		17:103104 ############
		18:126512 ############
		19:64064 ############
		20:116592 ############
		21:14560 ##########
		22:86128 ############
		23:960 #######
		24:45488 ###########
		25:08 ###
		26:15120 ##########
		28:3024 #########
		30:336 ######
		32:16 ###
		============================
		============================
		=Histogram SCC sizes=
		============================
		Observations: 2303364576
		Min: 1
		Max: 1638400
		Mean: 1.10
		Median: 1
		Standard deviation: 113.28
		75%: 1.00
		95%: 1.00
		98%: 1.00
		99%: 1.00
		99.9%: 1.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:2281312712 ######################
		03:10621184 #################
		08:7639296 ################
		09:64 #####
		20:3001600 ###############
		48:689920 ##############
		64:224 ######
		112:91392 ############
		256:6272 #########
		400:448 #######
		576:160 ######
		2304:560 #######
		12544:448 #######
		65536:224 ######
		331776:64 #####
		1638400:08 ###
		============================
		2303364576 SCCs found during liveness checking.
	 */
	@Test
	@Ignore
	public void testAllocatorSetSize8() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCAllocatorSetSize8");
		readBinFile(graph, "/data/allocatorSetSize8.bin"); // TLC needs ~25-50 seconds to search SCCs
		Assert.assertEquals(1966080, graph.size());
			
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(4119, sccs.size());
	}
	

	/*
		============================
		=Histogram vertex out-degree=
		============================
		Observations: 84344832
		Min: 1
		Max: 55
		Mean: 10.42
		Median: 9
		Standard deviation: 5.02
		75%: 13.00
		95%: 19.00
		98%: 23.00
		99%: 25.00
		99.9%: 35.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:36 ####
		02:1260432 ###############
		03:5163300 ################
		04:505242 ##############
		05:3683951 ################
		06:12366720 #################
		07:1926729 ###############
		08:3995280 ################
		09:13730040 #################
		10:3595608 ################
		11:5670000 ################
		12:9529056 #################
		13:3919968 ################
		14:2997360 ###############
		15:4516884 ################
		16:2002140 ###############
		17:3085776 ###############
		18:1286208 ###############
		19:1289052 ###############
		20:400176 #############
		21:730296 ##############
		22:198360 #############
		23:1245456 ###############
		24:74592 ############
		25:365148 #############
		26:19008 ##########
		27:137358 ############
		28:2394 ########
		29:365184 #############
		31:113526 ############
		33:41328 ###########
		35:80640 ############
		37:25704 ###########
		39:8568 ##########
		41:8064 #########
		43:4284 #########
		45:144 #####
		47:576 #######
		49:216 ######
		51:18 ###
		55:09 ###
		============================
		============================
		=Histogram vertex in-degree=
		============================
		Observations: 84344832
		Min: 2
		Max: 36
		Mean: 10.42
		Median: 9
		Standard deviation: 3.37
		75%: 12.00
		95%: 16.00
		98%: 20.00
		99%: 23.00
		99.9%: 28.00
		numEdges/occurrences (log scale)
		--------------------------------
		02:09 ###
		03:2880 ########
		04:61074 ############
		05:546480 ##############
		06:2752596 ###############
		07:8300880 ################
		08:15274980 #################
		09:15921360 #################
		10:9521721 #################
		11:5590134 ################
		12:7986843 ################
		13:5886000 ################
		14:5048892 ################
		15:1342656 ###############
		16:2107188 ###############
		17:335664 #############
		18:1087209 ##############
		19:437760 #############
		20:511920 ##############
		21:286416 #############
		22:482004 ##############
		23:77112 ############
		24:394956 #############
		25:7344 #########
		26:246321 #############
		27:162 ######
		28:102069 ############
		30:27216 ###########
		32:4536 #########
		34:432 #######
		36:18 ###
		============================
		============================
		=Histogram SCC sizes=
		============================
		Observations: 26161113753
		Min: 1
		Max: 7929856
		Mean: 1.07
		Median: 1
		Standard deviation: 177.32
		75%: 1.00
		95%: 1.00
		98%: 1.00
		99%: 1.00
		99.9%: 1.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:26010520593 ########################
		03:62750880 ##################
		08:53253648 ##################
		09:81 #####
		20:25524576 ##################
		48:7517160 ################
		64:324 ######
		112:1380960 ###############
		256:152208 ############
		400:756 #######
		576:8928 ##########
		1280:198 ######
		2304:1134 ########
		12544:1134 ########
		65536:756 #######
		331776:324 ######
		1638400:81 #####
		7929856:09 ###
		============================
		26.161.113.753 SCCs found during liveness checking.
	*/
	/*
	 * This huge number of SCCs stems from the fact that TLC has to check
	 * ~512 Possible error models for 9 different OrderOfSolutions. Thus it
	 * searches SCC 512 times on the same graph times 9 for all OOS.
	 */
	@Test
	@Ignore
	public void testAllocatorSetSize9() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCAllocatorSetSize9");
		readBinFile(graph, "/data/allocatorSetSize9.bin"); // TLC needs ~160seconds to search SCCs
		Assert.assertEquals(9371648, graph.size());
			
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(12611, sccs.size());
	}

	/*
		============================
		=Histogram vertex out-degree=
		============================
		Observations: 16586850
		Min: 1
		Max: 31
		Mean: 16.84
		Median: 17
		Standard deviation: 4.10
		75%: 19.00
		95%: 24.00
		98%: 26.00
		99%: 27.00
		99.9%: 30.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:21 ####
		02:64610 ############
		03:07 ##
		04:32305 ###########
		10:44142 ###########
		11:1527337 ###############
		12:1012256 ##############
		13:992803 ##############
		14:1262646 ###############
		15:1528373 ###############
		16:1629012 ###############
		17:1692278 ###############
		18:1418459 ###############
		19:1301097 ###############
		20:976556 ##############
		21:875119 ##############
		22:638218 ##############
		23:576198 ##############
		24:341754 #############
		25:268296 #############
		26:174762 #############
		27:104447 ############
		28:58702 ###########
		29:42994 ###########
		30:13902 ##########
		31:10556 ##########
		============================
		============================
		=Histogram vertex in-degree=
		============================
		Observations: 16586850
		Min: 2
		Max: 55
		Mean: 16.84
		Median: 16
		Standard deviation: 5.47
		75%: 20.00
		95%: 27.00
		98%: 31.00
		99%: 33.00
		99.9%: 41.00
		numEdges/occurrences (log scale)
		--------------------------------
		02:15246 ##########
		03:28 ####
		04:9072 ##########
		05:119 #####
		06:1127 ########
		07:455 #######
		08:8071 #########
		09:1087016 ##############
		10:679294 ##############
		11:199920 #############
		12:1887319 ###############
		13:1273790 ###############
		14:788578 ##############
		15:1783180 ###############
		16:1281259 ###############
		17:1157835 ##############
		18:873012 ##############
		19:1155112 ##############
		20:784007 ##############
		21:578781 ##############
		22:661640 ##############
		23:336931 #############
		24:507969 ##############
		25:338513 #############
		26:210721 #############
		27:206409 #############
		28:189126 #############
		29:86135 ############
		30:149527 ############
		31:83496 ############
		32:53942 ###########
		33:47054 ###########
		34:34769 ###########
		35:32522 ###########
		36:15015 ##########
		37:26460 ###########
		38:12334 ##########
		39:7007 #########
		40:6923 #########
		41:4032 #########
		42:3423 #########
		43:3388 #########
		44:959 #######
		45:2863 ########
		46:819 #######
		47:420 #######
		48:371 ######
		49:266 ######
		50:161 ######
		51:140 #####
		52:140 #####
		53:14 ###
		54:126 #####
		55:14 ###
		============================
		============================
		=Histogram SCC sizes=
		============================
		Observations: 75369
		Min: 1
		Max: 1963253
		Mean: 219.65
		Median: 1
		Standard deviation: 18919.55
		75%: 1.00
		95%: 510.00
		98%: 511.00
		99%: 511.00
		99.9%: 511.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:67319 ############
		02:1344 ########
		04:672 #######
		08:336 ######
		16:168 ######
		32:84 #####
		64:42 ####
		128:21 ####
		256:21 ####
		384:21 ####
		448:42 ####
		480:84 #####
		496:168 ######
		504:336 ######
		508:672 #######
		510:1344 ########
		511:2688 ########
		1963253:07 ##
		============================
		75369 SCCs found during liveness checking.
	 */
	@Test
	@Ignore
	public void testTLCVoteProofN4() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCVoteProofN4");
		readBinFile(graph, "/data/voteproofN4.bin"); // TLC needs ~45 seconds
		Assert.assertEquals(693930, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(0, sccs.size());
	}

	/*
		============================
		=Histogram vertex out-degree=
		============================
		Observations: 6576710
		Min: 1
		Max: 21
		Mean: 6.64
		Median: 6
		Standard deviation: 2.15
		75%: 8.00
		95%: 10.00
		98%: 13.00
		99%: 15.00
		99.9%: 19.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:216 ######
		02:13025 ##########
		03:163289 #############
		04:633390 ##############
		05:1207192 ###############
		06:1475220 ###############
		07:1277062 ###############
		08:807816 ##############
		09:496900 ##############
		10:183960 #############
		11:151192 ############
		12:14472 ##########
		13:80208 ############
		15:44256 ###########
		17:20520 ##########
		19:6768 #########
		21:1224 ########
		============================
		============================
		=Histogram vertex in-degree=
		============================
		Observations: 6576710
		Min: 2
		Max: 20
		Mean: 6.64
		Median: 6
		Standard deviation: 2.33
		75%: 8.00
		95%: 11.00
		98%: 13.00
		99%: 14.00
		99.9%: 18.00
		numEdges/occurrences (log scale)
		--------------------------------
		02:58936 ###########
		03:301706 #############
		04:718125 ##############
		05:1111276 ##############
		06:1239314 ###############
		07:1146069 ##############
		08:834698 ##############
		09:502786 ##############
		10:306647 #############
		11:112748 ############
		12:112689 ############
		13:27154 ###########
		14:60066 ############
		15:6632 #########
		16:27120 ###########
		17:608 #######
		18:7576 #########
		20:2560 ########
		============================
		============================
		=Histogram SCC sizes=
		============================
		Observations: 1434520
		Min: 1
		Max: 535250
		Mean: 4.36
		Median: 2
		Standard deviation: 775.69
		75%: 3.00
		95%: 9.00
		98%: 12.00
		99%: 18.00
		99.9%: 27.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:465251 ##############
		02:341026 #############
		03:317299 #############
		04:67564 ############
		06:139426 ############
		08:3752 #########
		09:67796 ############
		12:13288 ##########
		18:14256 ##########
		27:4824 #########
		116:08 ###
		232:08 ###
		7408:03 ##
		14816:16 ###
		535250:03 ##
		============================
		1434520 SCCs found during liveness checking.
	 */
	@Test
	public void testTLCBakeryN3() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCbakeryN3");
		readBinFile(graph, "/data/BakeryN3.bin"); // TLC needs ~21 seconds
		Assert.assertEquals(2212736, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(324590, sccs.size());
	}

	@Test
	@Ignore
	public void testTLCN10() throws IOException {
		// Manually copy the 2gb file to the test directory. It's too large for git to handle.
		final Graph graph = new Graph("testTLCN10");
		readFile(graph, "/data/tlcn10.txt");
		Assert.assertEquals(10508304, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph); // TLC needs ~202 seconds to search SCCs.
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(2302, sccs.size());
	}

	
	/*
		============================
		=Histogram vertex out-degree=
		============================
		Observations: 16586850
		Min: 1
		Max: 31
		Mean: 16.84
		Median: 17
		Standard deviation: 4.10
		75%: 19.00
		95%: 24.00
		98%: 26.00
		99%: 27.00
		99.9%: 30.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:21 ####
		02:64610 ############
		03:07 ##
		04:32305 ###########
		10:44142 ###########
		11:1527337 ###############
		12:1012256 ##############
		13:992803 ##############
		14:1262646 ###############
		15:1528373 ###############
		16:1629012 ###############
		17:1692278 ###############
		18:1418459 ###############
		19:1301097 ###############
		20:976556 ##############
		21:875119 ##############
		22:638218 ##############
		23:576198 ##############
		24:341754 #############
		25:268296 #############
		26:174762 #############
		27:104447 ############
		28:58702 ###########
		29:42994 ###########
		30:13902 ##########
		31:10556 ##########
		============================
		============================
		=Histogram vertex in-degree=
		============================
		Observations: 16586850
		Min: 2
		Max: 55
		Mean: 16.84
		Median: 16
		Standard deviation: 5.47
		75%: 20.00
		95%: 27.00
		98%: 31.00
		99%: 33.00
		99.9%: 41.00
		numEdges/occurrences (log scale)
		--------------------------------
		02:15246 ##########
		03:28 ####
		04:9072 ##########
		05:119 #####
		06:1127 ########
		07:455 #######
		08:8071 #########
		09:1087016 ##############
		10:679294 ##############
		11:199920 #############
		12:1887319 ###############
		13:1273790 ###############
		14:788578 ##############
		15:1783180 ###############
		16:1281259 ###############
		17:1157835 ##############
		18:873012 ##############
		19:1155112 ##############
		20:784007 ##############
		21:578781 ##############
		22:661640 ##############
		23:336931 #############
		24:507969 ##############
		25:338513 #############
		26:210721 #############
		27:206409 #############
		28:189126 #############
		29:86135 ############
		30:149527 ############
		31:83496 ############
		32:53942 ###########
		33:47054 ###########
		34:34769 ###########
		35:32522 ###########
		36:15015 ##########
		37:26460 ###########
		38:12334 ##########
		39:7007 #########
		40:6923 #########
		41:4032 #########
		42:3423 #########
		43:3388 #########
		44:959 #######
		45:2863 ########
		46:819 #######
		47:420 #######
		48:371 ######
		49:266 ######
		50:161 ######
		51:140 #####
		52:140 #####
		53:14 ###
		54:126 #####
		55:14 ###
		============================
		============================
		=Histogram SCC sizes=
		============================
		Observations: 75369
		Min: 1
		Max: 1963253
		Mean: 219.65
		Median: 1
		Standard deviation: 18919.55
		75%: 1.00
		95%: 510.00
		98%: 511.00
		99%: 511.00
		99.9%: 511.00
		numEdges/occurrences (log scale)
		--------------------------------
		01:67319 ############
		02:1344 ########
		04:672 #######
		08:336 ######
		16:168 ######
		32:84 #####
		64:42 ####
		128:21 ####
		256:21 ####
		384:21 ####
		448:42 ####
		480:84 #####
		496:168 ######
		504:336 ######
		508:672 #######
		510:1344 ########
		511:2688 ########
		1963253:07 ##
		============================
		75369 SCCs found during liveness checking.
	 */
	@Test
	public void testTLCN9() throws IOException, InterruptedException {
		final Graph graph = new Graph("testTLCN9");
		readBinFile(graph, "/data/tlcn09.bin");
		Assert.assertEquals(2369550, graph.size());
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(1150, sccs.size());
	}

	@Test
	@Repeat(times=1)
	public void testTLCN8() throws IOException {
		final Graph graph = new Graph("testTLCN8");
		readFile(graph, "/data/tlc.txt");
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(574, sccs.size());
		
		final Set<Set<Integer>> convertedSCCs = convertToInts(sccs);
		
		// Read the file with the correct SCCs
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream("/data/tlcsccs.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				final Set<Integer> hashSet = new HashSet<Integer>();
				final String[] ints = line.trim().split("\\s+");
				for (String string : ints) {
					hashSet.add(Integer.parseInt(string));
				}
				Assert.assertTrue(convertedSCCs.contains(hashSet));
			}
		}
	}
	
	@Test
	@Ignore
	public void testTLCN8JustInits() throws IOException {
		final Graph graph = new Graph("testTLCN8");
		readFile(graph, "/data/tlc.txt");
		readInits(graph, "/data/tlcinits.txt");
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(574, sccs.size());
		
		final Set<Set<Integer>> convertedSCCs = convertToInts(sccs);
		
		// Read the file with the correct SCCs
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream("/data/tlcsccs.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				final Set<Integer> hashSet = new HashSet<Integer>();
				final String[] ints = line.trim().split("\\s+");
				for (String string : ints) {
					hashSet.add(Integer.parseInt(string));
				}
				Assert.assertTrue(convertedSCCs.contains(hashSet));
			}
		}
	}
	
	/*
	 * tinyDG.txt has 13 nodes, 22 arcs
	 * 
	 * 3 (non-trivial) components:
	 * {0 2 3 4 5}, {9 10 11 12}, {6 8} 
	 */
	@Test
	public void testTiny() throws IOException {
		final Graph graph = new Graph("testTiny");
		readFile(graph, "/data/tinyDG.txt");
		
		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(printSCCs(sccs), 3, sccs.size());
		
		testTinySCCs(graph, sccs);
	}
	
	@Test
	public void testTinyLoop() throws IOException {
		final Graph graph = new Graph("testTinyLoop");
		readFile(graph, "/data/tinyDG.txt");
		
		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		final NoopExecutorService executor = new NoopExecutorService();
		final Iterator<GraphNode> iterator = graph.iterator();
		while (iterator.hasNext()) {
			new SCCWorker(executor, graph, sccs, iterator.next()).run();
		}
		
		final Set<Set<GraphNode>> result = new HashSet<>(sccs.size());
		for (GraphNode graphNode : sccs.values()) {
			result.add(graphNode.getSCC());
		}
		testTinySCCs(graph, result);
	}	
	
	private void testTinySCCs(final Graph graph, final Set<Set<GraphNode>> sccs) {
		Assert.assertEquals(printSCCs(sccs), 3, sccs.size());
	
		final Set<Set<Integer>> converted = convertToInts(sccs);

		// Now compare ints
		// Now compare ints
		final Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
		Set<Integer> anSCC = new HashSet<Integer>();
		anSCC.add(0);
		anSCC.add(2);
		anSCC.add(3);
		anSCC.add(4);
		anSCC.add(5);
		expected.add(anSCC);

		anSCC = new HashSet<Integer>();
		anSCC.add(9);
		anSCC.add(10);
		anSCC.add(11);
		anSCC.add(12);
		expected.add(anSCC);
		
		anSCC = new HashSet<Integer>();
		anSCC.add(6);
		anSCC.add(8);
		expected.add(anSCC);
		
		Assert.assertEquals(printSCCs(sccs), expected, converted);
	}
	
	/*
	 * mediumDG.txt has 49 nodes (no node #21) and 147 (136 without dupes) arcs
	 * 
	 * 2 (non-trivial) components
	 * {2 5 6 8 9 11 12 13 15 16 18 19 22 23 25 26 28 29 30 31 32 33 34 35 37 38 39 40 42 43 44 46 47 48 49},
	 * {3 4 17 20 24 27 36}
	 */
	@Test
	public void testMedium() throws IOException {
		final Graph graph = new Graph("testMedium");
		readFile(graph, "/data/mediumDG.txt");

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		testMediumSCCs(graph, sccs);
	}
	
	@Test
	public void testMediumWithThreeInitsOnly() throws IOException {
		final Graph graph = new Graph("testMediumWithInits");
		readFile(graph, "/data/mediumDG.txt");
		graph.setInit(1); // Source node
		graph.setInit(10); // Source node
		graph.setInit(20); // One node of the smaller SCC which is isolated WRT incoming arcs.

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		testMediumSCCs(graph, sccs);
	}
	
	@Test
	public void testMediumLoop() throws IOException {
		final Graph graph = new Graph("testMediumLoop");
		readFile(graph, "/data/mediumDG.txt");
		
		final Map<GraphNode, GraphNode> sccs = new HashMap<GraphNode, GraphNode>(0);

		final NoopExecutorService executor = new NoopExecutorService();
		final Iterator<GraphNode> iterator = graph.iterator();
		while (iterator.hasNext()) {
			new SCCWorker(executor, graph, sccs, iterator.next()).run();
		}
		final Set<Set<GraphNode>> result = new HashSet<>(sccs.size());
		for (GraphNode graphNode : sccs.values()) {
			result.add(graphNode.getSCC());
		}
		testMediumSCCs(graph, result);
	}

	private void testMediumSCCs(final Graph graph, final Set<Set<GraphNode>> sccs) {
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(printSCCs(sccs), 2, sccs.size());
	
		final Set<Set<Integer>> converted = convertToInts(sccs);

		// Now compare ints
		final Set<Set<Integer>> expected = new HashSet<Set<Integer>>();
		Set<Integer> anSCC = new TreeSet<Integer>();
		anSCC.add(3);
		anSCC.add(4);
		anSCC.add(17);
		anSCC.add(20);
		anSCC.add(24);
		anSCC.add(27);
		anSCC.add(36);
		expected.add(anSCC);

		anSCC = new TreeSet<Integer>();
		anSCC.add(2);
		anSCC.add(5);
		anSCC.add(6);
		anSCC.add(8);
		anSCC.add(9);
		anSCC.add(11);
		anSCC.add(12);
		anSCC.add(13);
		anSCC.add(15);
		anSCC.add(16);
		anSCC.add(18);
		anSCC.add(19);
		anSCC.add(22);
		anSCC.add(23);
		anSCC.add(25);
		anSCC.add(26);
		anSCC.add(28);
		anSCC.add(29);
		anSCC.add(30);
		anSCC.add(31);
		anSCC.add(32);
		anSCC.add(33);
		anSCC.add(34);
		anSCC.add(35);
		anSCC.add(37);
		anSCC.add(38);
		anSCC.add(39);
		anSCC.add(40);
		anSCC.add(42);
		anSCC.add(43);
		anSCC.add(44);
		anSCC.add(46);
		anSCC.add(47);
		anSCC.add(48);
		anSCC.add(49);
		expected.add(anSCC);
		
		Assert.assertEquals(printSCCs(sccs), expected, converted);
	}
	
	/*
	 * largeDG.txt has 25 components, 1.000.000 nodes and 7.500.000 arcs
	 */
	@Test
	public void testLarge() throws IOException {
		final Graph graph = new Graph("testLarge");
		readFile(graph, "/data/largeDG.txt");

		final Set<Set<GraphNode>> sccs = new ConcurrentFastSCC().searchSCCs(graph);
		Assert.assertTrue(graph.checkPostCondition());
		Assert.assertEquals(25, sccs.size());
		
		final Set<Set<Integer>> convertedSCCs = convertToInts(sccs);
		
		// Read the file with the correct SCCs
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream("/data/largeDGsccs.txt");
		try(BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for(String line = br.readLine(); line != null; line = br.readLine()) {
				final Set<Integer> hashSet = new HashSet<Integer>();
				final String[] ints = line.trim().split("\\s+");
				for (String string : ints) {
					hashSet.add(Integer.parseInt(string));
				}
				Assert.assertTrue(convertedSCCs.contains(hashSet));
			}
		}
	}	

	// Convert the set of sets of GraphNodes into a set of sets of ints
	private Set<Set<Integer>> convertToInts(final Set<Set<GraphNode>> sccs) {
		final Set<Set<Integer>> converted = new HashSet<Set<Integer>>();
		for (Set<GraphNode> set : sccs) {
			Set<Integer> anSCC = new TreeSet<Integer>();
			for (GraphNode graphNode : set) {
				anSCC.add(graphNode.getId());
			}
			converted.add(anSCC);
		}
		return converted;
	}

	// This implementation makes heavy use of multi-threading and is considerably faster on multi-core machines.
	private static void readBinFile(Graph graph, String filename) throws IOException, InterruptedException {
		// Read the inputstream into a byte buffer all at once. This provides good performance because
		// we don't call read() repeatedly.
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		final DataInputStream dis = new DataInputStream(in);
		final byte[] buf = new byte[dis.available()];
		dis.readFully(buf);
		dis.close();
		
		// Create an executor with as many threads as available on the system.
		final int nThreads = Runtime.getRuntime().availableProcessors();
		final ExecutorService executor = Executors.newFixedThreadPool(nThreads);

		// Wrap the byte buffer as an int buffer.
		final IntBuffer intBuf = ByteBuffer.wrap(buf).asIntBuffer();

		// Partition the work among the threads 
		final int partition = intBuf.capacity() / nThreads;
		
		for (int i = 0; i < nThreads; i++) {
			final int start= i * partition;
			// The thread responsible for the last partition will read until the
			// end of the buffer in case partition has a remainder.
			final int end = i == (nThreads -1) ? intBuf.capacity() : (i + 1) * partition;

			executor.execute(new Runnable() {
				@Override
				public void run() {
					int s = start;
					if (s > 0 && intBuf.get(s-1) != Integer.MIN_VALUE) {
						// Records (node and its out arcs) in the buffer are of
						// variable length and terminated by an end marker
						// (MIN_VALUE).
						// Advance/align to the first end marker in this
						// partition unless we are at the beginning of a p. or
						// we are right at the beginning of a record. The reader
						// of the previous partition will read into our
						// partition if necessary.
						while ((intBuf.get(s++) != Integer.MIN_VALUE)) {
							; // no-op
						}
					}
					// TODO Implement collecting n graphnodes into an
					// intermediate collection to append the collection to graph
					// => less contention on graph#put.
					while (s < end) {
						// Create a new node instance
						final int nodeId = intBuf.get(s++);
						final GraphNode graphNode = new GraphNode(nodeId);

						// Read the node's arcs
						int arcId;
						final LinkedList<Integer> arcs = new LinkedList<Integer>();
						while ((arcId = intBuf.get(s++)) != Integer.MIN_VALUE) {
							arcs.add(arcId);
						}
						graphNode.setArcs(arcs);
						
						// Add the node to the graph
						graph.put(nodeId, graphNode);
					}
				}
			});
		}
		
		// Wait for the threads to do their work.
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	private static void readFile(Graph graph, String filename) throws IOException {
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				final String[] split = line.trim().split("\\s+");
				final int nodeId = Integer.parseInt(split[0]);
				final int arcId = Integer.parseInt(split[1]);

				// set up the from node
				if (graph.hasNode(nodeId)) {
					graph.addArc(nodeId, arcId);
				} else {
					graph.addNode(new GraphNode(nodeId), arcId);
				}
				
				// Also create the to node (arcId) in case it's not explicitly
				// created in a test graph.
				if (!graph.hasNode(arcId)) {
					graph.addNode(new GraphNode(arcId));
				}
			}
		}
	}
	
	private static void readInits(Graph graph, String filename) throws IOException {
		final InputStream in = ConcurrentFastSCCTestFromFile.class.getResourceAsStream(filename);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				final int nodeId = Integer.parseInt(line.trim());
				graph.setInit(nodeId);
			}
		}
	}
}
