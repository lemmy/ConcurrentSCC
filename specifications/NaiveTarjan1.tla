------------------------- MODULE NaiveTarjan1 -------------------------
(***************************************************************************)
(* HERE IS THE ALGORITHM AS DESCRIBED BY BOB TARJAN:                       *)
(*                                                                         *)
(* NEEDS TO BE MODIFIED                                                    *)
(*                                                                         *)
(* A Concurrent Algorithm: To explain the algorithm, I'll describe it as   *)
(* contacting cycles immediately, although a good implementation would not *)
(* do the contractions explicitly (I think).  Each vertex is either        *)
(* unvisited, previsited, or postvisited.  The unvisited and previsited    *)
(* vertices form an in-forest (a set of in-trees): each unvisited vertex   *)
(* is a root with no children; each previsited vertex is either a root or  *)
(* it has a parent that is previsited.  Initially all vertices are         *)
(* unvisited.  Only roots are eligible for processing.  I'll call a root   *)
(* idle  if  no thread is processing it.  An idle thread can grab an idle  *)
(* root and start processing it.  To process a root, if it is unvisited,   *)
(* mark it visited.  Then traverse the next outgoing untraversed arc; if   *)
(* there is no such arc, mark the root postvisited and make all its        *)
(* children idle roots.  To traverse an arc (v, w), if w is postvisited do *)
(* nothing; otherwise, if w is in a different tree than v, make w the      *)
(* parent of v and mark w previsited if it is unvisited.  Since v is now a *)
(* child, it is not (for the moment) eligible for further processing.  The *)
(* thread can switch to an arbitrary root, or it can check to see if the   *)
(* root of the tree containing v and w is idle, and switch to this root if *)
(* so.  The other possibility is that w is in the same tree as v.  If v =  *)
(* w, do nothing.  (Self-loops can be created by contractions.) If v # w,  *)
(* contract all the ancestors of w into a single vertex, which is a root.  *)
(* Continue processing this root.  (The new root inherits all the outgoing *)
(* untraversed arcs from the set of contracted vertices).  It may be       *)
(* convenient and efficient to view this contraction as contracting all    *)
(* the vertices into v, since v is already a root.                         *)
(*                                                                         *)
(* Continue until all vertices are postvisited.                            *)
(*                                                                         *)
(* The algorithm specified below is a high-level abstract version of the   *)
(* algorithm in which the entire processing of a root described by Tarjan  *)
(* is a single atomic action.  Concurrency is achieved by an               *)
(* implementation in which this atomic action is split into a non-atomic   *)
(* operation implemented by subactions, and subactions of multiple         *)
(* operations are interleaved.                                             *)
(***************************************************************************)

EXTENDS Integers, Sequences, TLC

(***************************************************************************)
(* We declare as constants the sets of nodes, initial nodes, and arcs in   *)
(* the graph, where an arc is an ordered pair of nodes.  The ASSUME        *)
(* statement asserts the obvious relations between these sets.  TLC checks *)
(* that these assumptions hold for a model that it checks.                 *)
(*                                                                         *)
(* Note: A list of formulas bulleted by /\ (or \/ ) is the conjunction (or *)
(* disjunction) of those formulas.  Indentation is significant and is used *)
(* to eliminate the need for parentheses in nested dis/conjunctions.       *)
(*                                                                         *)
(* The symbols /\ and \/ are also used for the usual infix conjuncation    *)
(* and disjunction operators.                                              *)
(***************************************************************************)
IsGraph(G) == G.arcs \subseteq (G.nodes \X G.nodes)

CONSTANTS Graph

Nodes == Graph.nodes
Arcs == Graph.arcs

ASSUME Nodes \subseteq Int

NotANode == CHOOSE n : n \notin Nodes
NotAnArc == CHOOSE a : a \notin Arcs

ASSUME /\ IsGraph(Graph)

(***************************************************************************)
(* Reaches is defined so that Reaches(n, m) is true iff node m is      *)
(* reachable from node n in the graph.  It's defined in terms of the       *)
(* following:                                                              *)
(*                                                                         *)
(*    NbrsNotIn(x, S) is the set of neighbors of x that are not in         *)
(*       the set S of nodes                                                *)
(*                                                                         *)
(*    RF(S) is defined to be true iff m is reachable from some node in S.  *)
(*       It is defined recursively, so it has to be declared RECURSIVE     *)
(*       before its definition.  (Every symbol that appears in an          *)
(*       expression has to be either a TLA+ primitive or else              *)
(*       explicitly declared or defined.)                                  *)
(*                                                                         *)
(* Reaches(n, m) is then defined to be true iff n = m or RF({n}) is      *)
(* true.                                                                   *)
(*                                                                         *)
(* Note: TeX names like \in and \cap are used for some mathematical        *)
(* symbols.                                                                *)
(***************************************************************************)
Reaches(n, m, G) ==
  LET NbrsNotIn(x, S) == {y \in G.nodes \ S : <<x, y>> \in G.arcs}
      RECURSIVE RF(_)
      RF(S) == LET Nxt == UNION { NbrsNotIn(x, S) : x \in S }
               IN  IF m \in S THEN TRUE
                              ELSE IF Nxt = {} THEN FALSE
                                               ELSE RF(S \cup Nxt)
  IN  (n = m) \/ RF({n})


(***************************************************************************)
(* MCC is defined to be the set of maximal strongly connected components.  *)
(* It is defined by recursively M so that if Partial is a set of maximal   *)
(* SCCs of the graph and Rest is the set of nodes not in any of those      *)
(* SCCs, then M(Partial, Rest) is a set of maximal SCCs containing one     *)
(* more element than Partial.  of maximal SCCs of the graph that contain   *)
(* all the nodes.                                                          *)
(*                                                                         *)
(* Note: The expression                                                    *)
(*                                                                         *)
(*           CHOOSE x \in S : P(x)                                         *)
(*                                                                         *)
(* equals an arbitrarily chosen x in the set S satisfying P(x), if one     *)
(* exists.  Otherwise, it's undefined and TLC will report an error if it   *)
(* tries to compute the expression's value.  Note that there's no          *)
(* nondeterminism.  For the same S and P, the expression always equals the *)
(* same value.                                                             *)
(***************************************************************************)
MCC(G) ==
  LET RECURSIVE M(_, _)
      M(Partial, Rest) ==
        IF Rest = {} THEN Partial
                     ELSE LET x == CHOOSE x \in Rest : TRUE
                              CC == {y \in Rest : /\ Reaches(x, y, G)
                                                  /\ Reaches(y, x, G)}
                          IN M(Partial \cup {CC}, Rest \ CC)
  IN  M({}, Nodes)


(***************************************************************************)
(* Outgoing(x) is the set of all outgoing arcs of x.                       *)
(***************************************************************************)
Outgoing(node) == {a \in Arcs : a[1] = node}

Successors(n, G) == {m \in G.nodes : <<n, m>> \in G.arcs}

IsInForest(G) == /\ IsGraph(G)
                 /\ \A n \in G.nodes :
                    \/ Successors(n, G) = {}
                    \/ \E m \in Successors(n, G) :
                          /\ {m} = Successors(n, G)
                          /\ ~ Reaches(m, n, G)
InForestRoots(G) == {n \in G.nodes : Successors(n, G) = {}}
-------------------------------------------------------------------------------
FreeProcess == CHOOSE n : n \notin Nodes

LockValue == [count : Nat, owner : Nodes \cup {NotANode}]

\*(***************************************************************************)
\*(* We now declare the algorithm's variables.                               *)
\*(***************************************************************************)
\*VARIABLES unvisited,  \* The set of unvisited nodes.
\*          unexamined, \* The set of unexamined arcs
\*          inForest,
\*          myroot,
\*          locked

(***************************************************************************
--algorithm NT1 {
  variables unvisited = Nodes,
            unexamined = Arcs,
            inForest = [nodes |-> Nodes, arcs |-> {}],
            myroot = [n \in Nodes |-> n],
            locked = [n \in Nodes |-> [count |-> 0, owner |-> NotANode]],
            explorer = [n \in Nodes |-> NotAnArc]
  define {
    isRoot(n) == myroot[n] = n
    (***********************************************************************)
    (* If n is a representative (meaning n = myRoot[n]) of an SCC, then    *)
    (* reprSCC(n) is the set of nodes in that SCC.                         *)
    (***********************************************************************)
    reprSCC(n) == {m \in Nodes : \E p \in Nodes : /\ myroot[p] = n
                                                  /\ Reaches(p, m, inForest)
                                                  /\ Reaches(m, n, inForest)}

    isPost(n) == LET SCC == reprSCC(myroot[n])
                 IN  /\ n \in SCC
                     /\ ~\E arc \in unexamined : arc[1] \in SCC

    pathToRoot(n) ==
      LET RECURSIVE ptr(_)
          ptr(r) == IF myroot[r] = r
                      THEN <<r>>
                      ELSE <<r>> \o ptr(CHOOSE a \in inForest.arcs : a[1] = r)
      IN  ptr(n)

    realRoot(n) == LET p == pathToRoot(n) IN p[Len(p)]
  }


  macro release(node, acquierer) {
    assert acquierer = locked[node].owner ;
      locked[node].count := locked[node].count - 1
      || locked[node].owner := IF locked[node].count = 1 THEN NotANode
                                                         ELSE locked[node].owner
   }

  macro acquire(node, acquierer) {
    await locked[node].count = 0 \/ locked[node].owner = acquierer ;
      locked[node].count := locked[node].count + 1 || locked[node].owner := acquierer
   }

  process (arc \in Arcs)
    variable temp1 = NotANode ;  {
    a1: while (self \in unexamined /\ isRoot(myroot[self[1]])) {
         if (isPost(self[2])) {
           unexamined := unexamined \ {self} ;
         }
         else {
            temp1 := realRoot(self[2]) ;
         a2: if (temp1 = myroot[temp1]) {
               \* acquire(myroot[self[1]], myroot[self[1]]) ;  \* may not be necessary
               \*    make sure that myroot[self[1]] still a root
                if (myroot[myroot[self[1]]]= myroot[self[1]]) {
               \* follow inForest arcs, swing those arcs to
               \* point to myroot[self[1]], until we come to
               \* a node with root = myroot[self[1]]
                temp1 := self[2] ;
              a3: while (myroot[temp1] # myroot[self[1]])
                   { with (a = CHOOSE b \in inForest.arcs : b[1] = temp1) {
                       temp1 := a[2] ;
                       \*** ERROR: Doesn't work if a[2] is its own root and there's no outgoing arc from a[2].
                       inForest.arcs := (inForest.arcs \ {a}) \cup {<<a[2], myroot[self[1]]>>}
                      }
                   } ;
               unexamined := unexamined \ {self} ;
               \* release myroot[self[1]] lock if acquired
                 }

            }
            else {
             \* if currently exploring arc # NotANode, then goto a1 to abort
                if (explorer[myroot[self[1]]] # NotANode) {
                   temp1 := NotANode ;
                   goto a1
                  }
                else {
                   explorer := self ;
                        \* lock last node on  path to root in temp1 &
                        \*  myroot[self[1]] in proper order
                   if (myroot[self[1]] < temp1) {
                    a8: acquire (myroot[self[1]], myroot[self[1]]) ;
                    a5: acquire (temp1, self[1])
                        } else {
                    a6: acquire (temp1, self[1]) ;
                    a7: acquire (myroot[self[1]], myroot[self[1]])
                   } ;
                   \* if last node on path & myroot[self[1]] still  roots
                a10:  if (myroot[temp1] = temp1 /\ myroot[self[1]] = self[1]) {
                     if (~ isPost(temp1)) {
                       \* then if last node not a post root
                               \* add inForest arc from myroot[self[1]]
                               \* to last node on  path to root in temp1 &
                               \* change root of myroot[self[1]]
                       inForest.arcs := inForest.arcs  \cup {<<myroot[self[1]], temp1 >>};
                       myroot[myroot[self[1]]] := temp1
                       }
                    } ;
                       \* unlock last node on  path to root in temp1 &
                       \*  myroot[self[1]]
                    release (temp1, self[1]) ;
                a9: release (myroot[self[1]], myroot[self[1]]) ;
                    unexamined := unexamined \ {self} ;
                    explorer := NotAnArc
                 }
            } ;
            \* label needed before this
         a4: release(myroot[self[1]], myroot[self[1]]);
             temp1 := NotANode
         }
    }
   }

  process (free = FreeProcess) {
     	with (ar \in { a \in inForest.arcs : isPost(a[2])}) {
		    while (unexamined # {}) {
        	 inForest.arcs := inForest.arcs \ {ar}
       		}
   		}
    }
  }

 ***************************************************************************)
\* BEGIN TRANSLATION
VARIABLES unvisited, unexamined, inForest, myroot, locked, explorer, pc

(* define statement *)
isRoot(n) == myroot[n] = n




reprSCC(n) == {m \in Nodes : \E p \in Nodes : /\ myroot[p] = n
                                              /\ Reaches(p, m, inForest)
                                              /\ Reaches(m, n, inForest)}

isPost(n) == LET SCC == reprSCC(myroot[n])
             IN  /\ n \in SCC
                 /\ ~\E arc \in unexamined : arc[1] \in SCC

pathToRoot(n) ==
  LET RECURSIVE ptr(_)
      ptr(r) == IF myroot[r] = r
                  THEN <<r>>
                  ELSE <<r>> \o ptr(CHOOSE a \in inForest.arcs : a[1] = r)
  IN  ptr(n)

realRoot(n) == LET p == pathToRoot(n) IN p[Len(p)]

VARIABLE temp1

vars == << unvisited, unexamined, inForest, myroot, locked, explorer, pc,
           temp1 >>

ProcSet == (Arcs) \cup {FreeProcess}

Init == (* Global variables *)
        /\ unvisited = Nodes
        /\ unexamined = Arcs
        /\ inForest = [nodes |-> Nodes, arcs |-> {}]
        /\ myroot = [n \in Nodes |-> n]
        /\ locked = [n \in Nodes |-> [count |-> 0, owner |-> NotANode]]
        /\ explorer = [n \in Nodes |-> NotAnArc]
        (* Process arc *)
        /\ temp1 = [self \in Arcs |-> NotANode]
        /\ pc = [self \in ProcSet |-> CASE self \in Arcs -> "a1"
                                        [] self = FreeProcess -> "b1"]

a1(self) == /\ pc[self] = "a1"
            /\ IF self \in unexamined /\ isRoot(myroot[self[1]])
                  THEN /\ IF isPost(self[2])
                             THEN /\ unexamined' = unexamined \ {self}
                                  /\ pc' = [pc EXCEPT ![self] = "a1"]
                                  /\ temp1' = temp1
                             ELSE /\ temp1' = [temp1 EXCEPT ![self] = realRoot(self[2])]
                                  /\ pc' = [pc EXCEPT ![self] = "a2"]
                                  /\ UNCHANGED unexamined
                  ELSE /\ pc' = [pc EXCEPT ![self] = "Done"]
                       /\ UNCHANGED << unexamined, temp1 >>
            /\ UNCHANGED << unvisited, inForest, myroot, locked, explorer >>

a2(self) == /\ pc[self] = "a2"
            /\ IF temp1[self] = myroot[temp1[self]]
                  THEN /\ IF myroot[myroot[self[1]]]= myroot[self[1]]
                             THEN /\ temp1' = [temp1 EXCEPT ![self] = self[2]]
                                  /\ pc' = [pc EXCEPT ![self] = "a3"]
                             ELSE /\ pc' = [pc EXCEPT ![self] = "a4"]
                                  /\ temp1' = temp1
                       /\ UNCHANGED explorer
                  ELSE /\ IF explorer[myroot[self[1]]] # NotANode
                             THEN /\ temp1' = [temp1 EXCEPT ![self] = NotANode]
                                  /\ pc' = [pc EXCEPT ![self] = "a1"]
                                  /\ UNCHANGED explorer
                             ELSE /\ explorer' = self
                                  /\ IF myroot[self[1]] < temp1[self]
                                        THEN /\ pc' = [pc EXCEPT ![self] = "a8"]
                                        ELSE /\ pc' = [pc EXCEPT ![self] = "a6"]
                                  /\ temp1' = temp1
            /\ UNCHANGED << unvisited, unexamined, inForest, myroot, locked >>

a3(self) == /\ pc[self] = "a3"
            /\ IF myroot[temp1[self]] # myroot[self[1]]
                  THEN /\ LET a == CHOOSE b \in inForest.arcs : b[1] = temp1[self] IN
                            /\ temp1' = [temp1 EXCEPT ![self] = a[2]]
                            /\ inForest' = [inForest EXCEPT !.arcs = (inForest.arcs \ {a}) \cup {<<a[2], myroot[self[1]]>>}]
                       /\ pc' = [pc EXCEPT ![self] = "a3"]
                       /\ UNCHANGED unexamined
                  ELSE /\ unexamined' = unexamined \ {self}
                       /\ pc' = [pc EXCEPT ![self] = "a4"]
                       /\ UNCHANGED << inForest, temp1 >>
            /\ UNCHANGED << unvisited, myroot, locked, explorer >>

a10(self) == /\ pc[self] = "a10"
             /\ IF myroot[temp1[self]] = temp1[self] /\ myroot[self[1]] = self[1]
                   THEN /\ IF ~ isPost(temp1[self])
                              THEN /\ inForest' = [inForest EXCEPT !.arcs = inForest.arcs  \cup {<<myroot[self[1]], temp1[self] >>}]
                                   /\ myroot' = [myroot EXCEPT ![myroot[self[1]]] = temp1[self]]
                              ELSE /\ TRUE
                                   /\ UNCHANGED << inForest, myroot >>
                   ELSE /\ TRUE
                        /\ UNCHANGED << inForest, myroot >>
             /\ Assert((self[1]) = locked[temp1[self]].owner,
                       "Failure of assertion at line 194, column 5 of macro called at line 263, column 21.")
             /\ locked' = [locked EXCEPT ![temp1[self]].count = locked[temp1[self]].count - 1,
                                         ![temp1[self]].owner = IF locked[temp1[self]].count = 1 THEN NotANode
                                                                                                 ELSE locked[temp1[self]].owner]
             /\ pc' = [pc EXCEPT ![self] = "a9"]
             /\ UNCHANGED << unvisited, unexamined, explorer, temp1 >>

a9(self) == /\ pc[self] = "a9"
            /\ Assert((myroot[self[1]]) = locked[(myroot[self[1]])].owner,
                      "Failure of assertion at line 194, column 5 of macro called at line 264, column 21.")
            /\ locked' = [locked EXCEPT ![(myroot[self[1]])].count = locked[(myroot[self[1]])].count - 1,
                                        ![(myroot[self[1]])].owner = IF locked[(myroot[self[1]])].count = 1 THEN NotANode
                                                                                                            ELSE locked[(myroot[self[1]])].owner]
            /\ unexamined' = unexamined \ {self}
            /\ explorer' = NotAnArc
            /\ pc' = [pc EXCEPT ![self] = "a4"]
            /\ UNCHANGED << unvisited, inForest, myroot, temp1 >>

a8(self) == /\ pc[self] = "a8"
            /\ locked[(myroot[self[1]])].count = 0 \/ locked[(myroot[self[1]])].owner = (myroot[self[1]])
            /\ locked' = [locked EXCEPT ![(myroot[self[1]])].count = locked[(myroot[self[1]])].count + 1,
                                        ![(myroot[self[1]])].owner = myroot[self[1]]]
            /\ pc' = [pc EXCEPT ![self] = "a5"]
            /\ UNCHANGED << unvisited, unexamined, inForest, myroot, explorer,
                            temp1 >>

a5(self) == /\ pc[self] = "a5"
            /\ locked[temp1[self]].count = 0 \/ locked[temp1[self]].owner = (self[1])
            /\ locked' = [locked EXCEPT ![temp1[self]].count = locked[temp1[self]].count + 1,
                                        ![temp1[self]].owner = self[1]]
            /\ pc' = [pc EXCEPT ![self] = "a10"]
            /\ UNCHANGED << unvisited, unexamined, inForest, myroot, explorer,
                            temp1 >>

a6(self) == /\ pc[self] = "a6"
            /\ locked[temp1[self]].count = 0 \/ locked[temp1[self]].owner = (self[1])
            /\ locked' = [locked EXCEPT ![temp1[self]].count = locked[temp1[self]].count + 1,
                                        ![temp1[self]].owner = self[1]]
            /\ pc' = [pc EXCEPT ![self] = "a7"]
            /\ UNCHANGED << unvisited, unexamined, inForest, myroot, explorer,
                            temp1 >>

a7(self) == /\ pc[self] = "a7"
            /\ locked[(myroot[self[1]])].count = 0 \/ locked[(myroot[self[1]])].owner = (myroot[self[1]])
            /\ locked' = [locked EXCEPT ![(myroot[self[1]])].count = locked[(myroot[self[1]])].count + 1,
                                        ![(myroot[self[1]])].owner = myroot[self[1]]]
            /\ pc' = [pc EXCEPT ![self] = "a10"]
            /\ UNCHANGED << unvisited, unexamined, inForest, myroot, explorer,
                            temp1 >>

a4(self) == /\ pc[self] = "a4"
            /\ Assert((myroot[self[1]]) = locked[(myroot[self[1]])].owner,
                      "Failure of assertion at line 194, column 5 of macro called at line 270, column 14.")
            /\ locked' = [locked EXCEPT ![(myroot[self[1]])].count = locked[(myroot[self[1]])].count - 1,
                                        ![(myroot[self[1]])].owner = IF locked[(myroot[self[1]])].count = 1 THEN NotANode
                                                                                                            ELSE locked[(myroot[self[1]])].owner]
            /\ temp1' = [temp1 EXCEPT ![self] = NotANode]
            /\ pc' = [pc EXCEPT ![self] = "a1"]
            /\ UNCHANGED << unvisited, unexamined, inForest, myroot, explorer >>

arc(self) == a1(self) \/ a2(self) \/ a3(self) \/ a10(self) \/ a9(self)
                \/ a8(self) \/ a5(self) \/ a6(self) \/ a7(self) \/ a4(self)

b1 == /\ pc[FreeProcess] = "b1"
      /\ IF unexamined # {}
            THEN /\ pc' = [pc EXCEPT ![FreeProcess] = "b2"]
            ELSE /\ pc' = [pc EXCEPT ![FreeProcess] = "Done"]
      /\ UNCHANGED << unvisited, unexamined, inForest, myroot, locked,
                      explorer, temp1 >>

b2 == /\ pc[FreeProcess] = "b2"
      /\ \E ar \in { a \in inForest.arcs : isPost(a[2])}:
           inForest' = [inForest EXCEPT !.arcs = inForest.arcs \ {ar}]
      /\ pc' = [pc EXCEPT ![FreeProcess] = "b1"]
      /\ UNCHANGED << unvisited, unexamined, myroot, locked, explorer, temp1 >>

free == b1 \/ b2

Next == free
           \/ (\E self \in Arcs: arc(self))
           \/ (* Disjunct to prevent deadlock on termination *)
              ((\A self \in ProcSet: pc[self] = "Done") /\ UNCHANGED vars)

Spec == Init /\ [][Next]_vars

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION

(***************************************************************************)
(* It is a useful convention define TypeOK to be the state predicate that  *)
(* asserts that all the variables have the right types.                    *)
(***************************************************************************)
TypeOK == /\ unexamined \subseteq Arcs
          /\ unvisited  \subseteq Nodes
          /\ IsInForest(inForest)
          /\ myroot \in [Nodes -> Nodes]
          /\ locked \in [Nodes -> LockValue]
          /\ explorer \in [Nodes -> Arcs \cup {NotAnArc}]




=============================================================================

(***************************************************************************)
(* successor(P) is the successor of the Part P in `partition', if its      *)
(* successor is non-NotANode                                                   *)
(***************************************************************************)
successor(P) == CHOOSE Q \in partition : P.succ \in Q.nodes

(***************************************************************************)
(* part(n) is the element of `partition' containing node n, or << >> (the  *)
(* 0-tuple which is also the unique function with domain {}) if there is   *)
(* no such node.  This value is convenient for the following reason.  The  *)
(* semantics of TLA+ define whether two values are equal iff they belong   *)
(* to some primitive set--for example, if they're both integers or         *)
(* strings--or the axioms of set theory and of functions define whether or *)
(* not they're equal.  For example, {1} doesn't equal {"a", "b"} because   *)
(* they are sets with different cardinalities.  TLC will report an error   *)
(* if it tries to decide if two values are equal when that isn't specified *)
(* by the semantics of TLA+.  Since tuple and records are functions, TLC   *)
(* knows that << >> is not equal to any record with a non-empty set of     *)
(* fields (a record being a function whose domain is a finite set of       *)
(* strings).                                                               *)
(***************************************************************************)
part(n) == IF \E P \in partition : n \in P.nodes
             THEN CHOOSE P \in partition : n \in P.nodes
             ELSE << >>

(***************************************************************************)
(* isAncestorOf(P, Q) is true iff P is an ancestor of Q in the             *)
(* in-forest--in other words iff there is a successor-path from Q to P.    *)
(***************************************************************************)
RECURSIVE isAncestorOf(_, _)
isAncestorOf(P, Q) ==
  IF P = Q THEN TRUE
           ELSE IF Q.succ = NotANode THEN FALSE
                                 ELSE isAncestorOf(P,successor(Q))

(***************************************************************************)
(* If P is an ancestor of Q in the partition in-graph, then partsOnPath is *)
(* the union of all the parts in the in-forest path from Q to P.           *)
(***************************************************************************)
RECURSIVE partsOnPath(_, _)
partsOnPath(Q, P) ==
  IF P = Q THEN {Q}
           ELSE {Q} \cup partsOnPath(successor(Q), P)
-----------------------------------------------------------------------------
(***************************************************************************)
(* It is conventional to call Init the initial predicate, which specifies  *)
(* the intial values of all the variables.                                 *)
(***************************************************************************)
Init == /\ unexamined = Arcs
        /\ unvisited = Nodes
        /\ partition = {}

(***************************************************************************)
(* We now define the next-state action Next.  An action is a formula that  *)
(* can contained primed and unprimed variables.  It defines a relation on  *)
(* <<old state, new state>> pairs, where unprimed variables represent the  *)
(* values of the variables in the old state and primed variables represent *)
(* their values in the new state.  The next-state action specifies all     *)
(* possible state transitions that the algorithm allows.                   *)
(*                                                                         *)
(* The action Next is defined in terms of operations that are described in *)
(* in Bob's documents.  The correspondence between these actions and those *)
(* observations should be pretty obvious--e.g., action d4 is the operation *)
(* (d)(iv).  Each of those actions are atomic--they describe a single      *)
(* "instantaneous" step of the algorithm.                                  *)
(***************************************************************************)

(***************************************************************************)
(* Explanation of notation:                                                *)
(*                                                                         *)
(*    \ is set difference.                                                 *)
(*                                                                         *)
(*    [nodes |-> {x}, succ |-> {}, dead |-> FALSE]                         *)
(*       is the record whose `nodes' component is {x}, whose `succ'        *)
(*       component is {} and whose `dead' component is the Boolean         *)
(*       value FALSE.                                                      *)
(*                                                                         *)
(*    UNCHANGED x  is an "abbreviation" for x' = x.                        *)
(***************************************************************************)
a ==
\E x \in InitNodes \cap unvisited :
        /\ unvisited' = unvisited \ {x}
        /\ partition' = partition \cup
                          {[nodes |-> {x}, succ |-> NotANode, dead |-> FALSE]}
        /\ UNCHANGED unexamined

(***************************************************************************)
(* Explanation of notation:                                                *)
(*                                                                         *)
(*    ~ is negation                                                        *)
(*                                                                         *)
(*    As explained above, UNCHANGED <<unvisited, unexamined>> means        *)
(*                                                                         *)
(*       <<unvisited, unexamined>>' = <<unvisited, unexamined>>            *)
(*                                                                         *)
(*    Priming an expression means priming all the variables in the         *)
(*    expression, so this formula is equivalent to                         *)
(*                                                                         *)
(*      (unvisited' = unvisited) /\ (unexamined' = unexamined)             *)
(*                                                                         *)
(* Here is the ugliest piece of TLA+ notation:                             *)
(*                                                                         *)
(*    [Y EXCEPT !.dead = TRUE]  is the record R which is the same as the   *)
(*       record Y except that R.dead equals TRUE.  If Y were a variable    *)
(*       (which it isn't), then                                            *)
(*                                                                         *)
(*          Y' = [Y EXCEPT !.dead = TRUE]                                  *)
(*                                                                         *)
(*       would be the TLA+ action that corresponds to                      *)
(*                                                                         *)
(*          Y.dead := TRUE                                                 *)
(***************************************************************************)
b == \E Y \in partition :
       /\ ~ Y.dead
       /\ Y.succ = NotANode
       /\ \A n \in Y.nodes :
            /\ Outgoing(n) \cap unexamined = {}
            /\ partition' = (partition \ {Y}) \cup { [Y EXCEPT !.dead = TRUE]}
            /\ UNCHANGED <<unvisited, unexamined>>

c == \E X \in partition :
       /\ ~ X.dead
       /\ X.succ # NotANode /\ part(X.succ).dead
       /\ partition' = (partition \ {X}) \cup { [X EXCEPT !.succ = NotANode]}
       /\ UNCHANGED <<unvisited, unexamined>>

(***************************************************************************)
(* At this point, you should skip down to the definition of d to           *)
(* understand what the arguments of the d1--d4 action operators mean.      *)
(* Action d describes all the state transition allowed by operations       *)
(* (d)(i)--(d)(iv).                                                        *)
(***************************************************************************)
d1(x, y) == /\ unvisited' = unvisited \ {y}
            /\ LET chgdPartition == IF part(x).succ = NotANode
                                      THEN (partition \ {part(x)}) \cup
                                             {[part(x) EXCEPT !.succ = y]}
                                      ELSE partition
               IN  partition' = chgdPartition \cup
                                  {[nodes |-> {y}, succ |-> NotANode, dead |-> FALSE]}

d2(X, Y) == /\ Y # << >>
            /\ (X = Y) \/ Y.dead
            /\ UNCHANGED <<unvisited, partition>>

d3(X, Y) == /\ (X # Y) /\ isAncestorOf(X, Y)
            /\ partition' = (partition \ partsOnPath(Y, X)) \cup
                              {[nodes |-> UNION {P.nodes : P \in partsOnPath(Y, X)},
                                succ  |-> X.succ,
                                dead  |-> FALSE]}
            /\ UNCHANGED unvisited

d4(X, Y) == /\ X.succ = NotANode
            /\ ~ isAncestorOf(X, Y)
            /\ partition' = (partition \ {X}) \cup
                              {[X EXCEPT !.succ = Y.succ]}
            /\ UNCHANGED unvisited

d == \E x \in Nodes \ unvisited :
       \E y \in Nodes :
         /\ <<x, y>> \in unexamined
         /\ unexamined' = unexamined \ {<<x, y>>}
         /\ IF y \in unvisited
              THEN d1(x, y)
              ELSE LET X == part(x)  Y == part(y)
                   IN  d2(X, Y) \/ d3(X, Y) \/ d4(X, Y)

(***************************************************************************)
(* Here's the definition of the next-state action Next.                    *)
(***************************************************************************)
Next ==  a \/ b \/ c \/ d

(***************************************************************************)
(* Formula Spec is a temporal-logic formula that is the complete           *)
(* specification of the algorithm.  The only thing you need to know is     *)
(* that the WF term asserts that the algorith doesn't stop if it's         *)
(* possible for it to take a Next step.                                    *)
(***************************************************************************)
Spec == Init /\ [][Next]_vars /\ WF_vars(Next)
-----------------------------------------------------------------------------
Vertices == [nodes : SUBSET Nodes, succ : Nodes \cup {NotANode}, dead : BOOLEAN]

(***************************************************************************)
(* A partition is a set of vertices having disjoint, non-empty `nodes'     *)
(* fields such that each vertex's `succ' field is either empty or is the   *)
(* `nodes' field of some vertex in the partition (possibly itself).  We    *)
(* define Partitions to be the set of all partitions.                      *)
(*                                                                         *)
(* Note: # mean unequal to, which can also be written /= .                 *)
(***************************************************************************)
Partitions == {P \in SUBSET Vertices :
                 /\ \A v1, v2 \in P :
                       (v1 # v2) => ((v1.nodes \cap v2.nodes) = {})
                 /\ \A v \in P : /\ v.nodes # {}
                                 /\ \E w \in P : \/ v.succ = NotANode
                                                 \/ v.succ \in w.nodes}
-----------------------------------------------------------------------------
(***************************************************************************)
(* The state predicate Correct asserts that, if the algorithm has visited  *)
(* all nodes and examined all arcs, then the variable `partition'          *)
(* describes the correct set of MCCs.  Partial correctness means that      *)
(* Correct is an invariant (true of all reachable states).  TLC will check *)
(* this for us on a particular model (assignment of values to the constant *)
(* parameters Nodes, InitNodes, Arcs).                                     *)
(***************************************************************************)
Correct == (unvisited = {}) /\ (unexamined = {}) =>
             ({ P.nodes  : P \in partition } = MCC)

=============================================================================
\* Modification History
\* Last modified Tue Nov 17 20:52:08 CET 2015 by markus
\* Last modified Wed Oct 07 07:48:26 PDT 2015 by lamport
\* Created Thu Sep 17 06:53:31 PDT 2015 by lamport
