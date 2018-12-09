------------------------------- MODULE BloemenSCC ------------------------------

EXTENDS Integers, Sequences, TLC

CONSTANTS Nodes, Edges, Threads
ASSUME Edges \subseteq Nodes \X Nodes

OutgoingEdges(node) ==
  { e \in Edges : e[1] = node }

(*******
--algorithm BloemenSCC {
  variables ufStatus = [node \in Nodes |-> "uf-live"],
            parent = [node \in Nodes |-> node],
            workerSet = [node \in Nodes |-> {}],
            liveElements = [node \in Nodes |-> {node}]

  define {
    undeadNodes == { node \in Nodes : ufStatus[node] # "uf-dead" }
    liveNodes   == { node \in Nodes : ufStatus[node] = "uf-live" }

    find(node) ==
      LET RECURSIVE PF(_)
          PF(x) == IF parent[x] = x THEN x
                                    ELSE PF(parent[x])
    sameSet(x, y) == find(x) = find(y)

    isDead(node) == ufStatus[find(node)] = "uf-dead"
    ufSet(x) == { node \in Nodes : sameSet(node, x) }
  }

  macro push(x, stack) {
    stack := << x >> \o stack;
  }

  macro pop(stack) {
    stack := Tail(stack);
  }

  macro makeClaim(node) {
    root := find(node);
    if (isDead(root)) {
      claimed := "claim-dead";
    } else {
      if (self \in workerSet[root]) {
        claimed := "claim-found";
      } else {
        workerSet[root] := workerSet[root] \cup {self};
        claimed := "claim-success";
      }
    }
  }

  macro remove(node) {
    root := find(node);
    if (node \in liveElements[root]) {
      liveElements[root] := liveElements[root] \ {node};
    }
  }

  macro pickFromSet(node) {
    root := find(node);
    if (liveElements[root] = {}) {
      ufStatus[root] := "uf-dead";
    } else {
      with (node \in liveElements[root]) {
        vp := node;
      }
    }
  }

  procedure unite(a, b)
    variables ra, rb {
    label13:
    ra := find(a) || rb := find(b);
    if (ra # rb) {
      parent[ra] := rb ||
      workerSet[rb] := workerSet[rb] \cup workerSet[ra] ||
      liveElements[rb] := liveElements[rb] \cup liveElements[ra];
    };
    label15:
    return;
  }

  fair process (T \in Threads)
    variables recursionStack = << >>,
              rootStack = << >>,
              backtrack = FALSE,
              v = CHOOSE node \in Nodes : TRUE,
              w = v, vp = v, root = v,
              edgesUnexplored = [node \in Nodes |-> OutgoingEdges(node)],
              claimed = "claim-dead";
  {
    label1:
    \* Pick an unexplored node here.
    while (undeadNodes # {}) {
      with (node \in undeadNodes) {
        v := node;
      };

      label2:
      if (backtrack = FALSE) {
        push(v, rootStack);
      };

      label3:
      if (backtrack = FALSE) {
        label4:
        if (Len(recursionStack) # 0 /\ sameSet(Head(recursionStack), v)) {
          goto label6;
        } else {
          \* pickFromList
          \* Obtain a live element from the list of v
          \* If not able to pick then enitre list dead and the SCC is completely explored
          \* Else found a new element to start exploring from
          pickFromSet(v);
          if (isDead(v)) {
            goto label6;
          }
        }
      } else {
        label5:
        vp := Head(recursionStack)[1] || v := Head(recursionStack)[2];
        pop(recursionStack);
        backtrack := FALSE;
        label7:
        if (isDead(v)) {
          remove(vp);
          goto label3;
        }
      };

      label8:
      while (edgesUnexplored[vp] # {}) {
        with (edge \in edgesUnexplored[vp]) {
          edgesUnexplored[vp] := edgesUnexplored[vp] \ {edge};
          w := edge[2];
        };

        \* Self loop found
        label10:
        if (w = vp) {
          goto label8;
        } else {
          makeClaim(w);
          if (claimed = "claim-dead") {
            goto label8;
          } else if (claimed = "claim-success") {
            push(<< vp, v >>, recursionStack);
            v := w;
            goto label2;
          } else {
            \* claim-found
            label11:
            if (sameSet(w, v) # FALSE) {
              root := Head(rootStack);
              pop(rootStack);
              call unite(Head(rootStack), root);
              label16:
              goto label11;
            }
          };

          label14:
          goto label8;
        }
      };

      label12:
      remove(vp);
      goto label3;

      label6:
      if (Head(rootStack) = v) {
        pop(rootStack);
      };

      if (Len(recursionStack) # 0) {
        backtrack := TRUE;
        goto label2;
      };
    };
  }
}
******)

\* BEGIN TRANSLATION
CONSTANT defaultInitValue
VARIABLES ufStatus, parent, workerSet, liveElements, pc, stack

(* define statement *)
undeadNodes == { node \in Nodes : ufStatus[node] # "uf-dead" }
liveNodes   == { node \in Nodes : ufStatus[node] = "uf-live" }

find(node) ==
  LET RECURSIVE PF(_)
      PF(x) == IF parent[x] = x THEN x
                                ELSE PF(parent[x])
sameSet(x, y) == find(x) = find(y)

isDead(node) == ufStatus[find(node)] = "uf-dead"
ufSet(x) == { node \in Nodes : sameSet(node, x) }

VARIABLES a, b, ra, rb, recursionStack, rootStack, backtrack, v, w, vp, root,
          edgesUnexplored, claimed

vars == << ufStatus, parent, workerSet, liveElements, pc, stack, a, b, ra, rb,
           recursionStack, rootStack, backtrack, v, w, vp, root,
           edgesUnexplored, claimed >>

ProcSet == (Threads)

Init == (* Global variables *)
        /\ ufStatus = [node \in Nodes |-> "uf-live"]
        /\ parent = [node \in Nodes |-> node]
        /\ workerSet = [node \in Nodes |-> {}]
        /\ liveElements = [node \in Nodes |-> {node}]
        (* Procedure unite *)
        /\ a = [ self \in ProcSet |-> defaultInitValue]
        /\ b = [ self \in ProcSet |-> defaultInitValue]
        /\ ra = [ self \in ProcSet |-> defaultInitValue]
        /\ rb = [ self \in ProcSet |-> defaultInitValue]
        (* Process T *)
        /\ recursionStack = [self \in Threads |-> << >>]
        /\ rootStack = [self \in Threads |-> << >>]
        /\ backtrack = [self \in Threads |-> FALSE]
        /\ v = [self \in Threads |-> CHOOSE node \in Nodes : TRUE]
        /\ w = [self \in Threads |-> v[self]]
        /\ vp = [self \in Threads |-> v[self]]
        /\ root = [self \in Threads |-> v[self]]
        /\ edgesUnexplored = [self \in Threads |-> [node \in Nodes |-> OutgoingEdges(node)]]
        /\ claimed = [self \in Threads |-> "claim-dead"]
        /\ stack = [self \in ProcSet |-> << >>]
        /\ pc = [self \in ProcSet |-> "label1"]

label13(self) == /\ pc[self] = "label13"
                 /\ /\ ra' = [ra EXCEPT ![self] = find(a[self])]
                    /\ rb' = [rb EXCEPT ![self] = find(b[self])]
                 /\ IF ra'[self] # rb'[self]
                       THEN /\ /\ liveElements' = [liveElements EXCEPT ![rb'[self]] = liveElements[rb'[self]] \cup liveElements[ra'[self]]]
                               /\ parent' = [parent EXCEPT ![ra'[self]] = rb'[self]]
                               /\ workerSet' = [workerSet EXCEPT ![rb'[self]] = workerSet[rb'[self]] \cup workerSet[ra'[self]]]
                       ELSE /\ TRUE
                            /\ UNCHANGED << parent, workerSet, liveElements >>
                 /\ pc' = [pc EXCEPT ![self] = "label15"]
                 /\ UNCHANGED << ufStatus, stack, a, b, recursionStack,
                                 rootStack, backtrack, v, w, vp, root,
                                 edgesUnexplored, claimed >>

label15(self) == /\ pc[self] = "label15"
                 /\ pc' = [pc EXCEPT ![self] = Head(stack[self]).pc]
                 /\ ra' = [ra EXCEPT ![self] = Head(stack[self]).ra]
                 /\ rb' = [rb EXCEPT ![self] = Head(stack[self]).rb]
                 /\ a' = [a EXCEPT ![self] = Head(stack[self]).a]
                 /\ b' = [b EXCEPT ![self] = Head(stack[self]).b]
                 /\ stack' = [stack EXCEPT ![self] = Tail(stack[self])]
                 /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                 recursionStack, rootStack, backtrack, v, w,
                                 vp, root, edgesUnexplored, claimed >>

unite(self) == label13(self) \/ label15(self)

label1(self) == /\ pc[self] = "label1"
                /\ IF undeadNodes # {}
                      THEN /\ \E node \in undeadNodes:
                                v' = [v EXCEPT ![self] = node]
                           /\ pc' = [pc EXCEPT ![self] = "label2"]
                      ELSE /\ pc' = [pc EXCEPT ![self] = "Done"]
                           /\ v' = v
                /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                stack, a, b, ra, rb, recursionStack, rootStack,
                                backtrack, w, vp, root, edgesUnexplored,
                                claimed >>

label2(self) == /\ pc[self] = "label2"
                /\ IF backtrack[self] = FALSE
                      THEN /\ rootStack' = [rootStack EXCEPT ![self] = << v[self] >> \o rootStack[self]]
                      ELSE /\ TRUE
                           /\ UNCHANGED rootStack
                /\ pc' = [pc EXCEPT ![self] = "label3"]
                /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                stack, a, b, ra, rb, recursionStack, backtrack,
                                v, w, vp, root, edgesUnexplored, claimed >>

label3(self) == /\ pc[self] = "label3"
                /\ IF backtrack[self] = FALSE
                      THEN /\ pc' = [pc EXCEPT ![self] = "label4"]
                      ELSE /\ pc' = [pc EXCEPT ![self] = "label5"]
                /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                stack, a, b, ra, rb, recursionStack, rootStack,
                                backtrack, v, w, vp, root, edgesUnexplored,
                                claimed >>

label4(self) == /\ pc[self] = "label4"
                /\ IF Len(recursionStack[self]) # 0 /\ sameSet(Head(recursionStack[self]), v[self])
                      THEN /\ pc' = [pc EXCEPT ![self] = "label6"]
                           /\ UNCHANGED << ufStatus, vp, root >>
                      ELSE /\ root' = [root EXCEPT ![self] = find(v[self])]
                           /\ IF liveElements[root'[self]] = {}
                                 THEN /\ ufStatus' = [ufStatus EXCEPT ![root'[self]] = "uf-dead"]
                                      /\ vp' = vp
                                 ELSE /\ \E node \in liveElements[root'[self]]:
                                           vp' = [vp EXCEPT ![self] = v[self]]
                                      /\ UNCHANGED ufStatus
                           /\ IF isDead(v[self])
                                 THEN /\ pc' = [pc EXCEPT ![self] = "label6"]
                                 ELSE /\ pc' = [pc EXCEPT ![self] = "label8"]
                /\ UNCHANGED << parent, workerSet, liveElements, stack, a, b,
                                ra, rb, recursionStack, rootStack, backtrack,
                                v, w, edgesUnexplored, claimed >>

label5(self) == /\ pc[self] = "label5"
                /\ /\ v' = [v EXCEPT ![self] = Head(recursionStack[self])[2]]
                   /\ vp' = [vp EXCEPT ![self] = Head(recursionStack[self])[1]]
                /\ recursionStack' = [recursionStack EXCEPT ![self] = Tail(recursionStack[self])]
                /\ backtrack' = [backtrack EXCEPT ![self] = FALSE]
                /\ pc' = [pc EXCEPT ![self] = "label7"]
                /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                stack, a, b, ra, rb, rootStack, w, root,
                                edgesUnexplored, claimed >>

label7(self) == /\ pc[self] = "label7"
                /\ IF isDead(v[self])
                      THEN /\ root' = [root EXCEPT ![self] = find(vp[self])]
                           /\ IF vp[self] \in liveElements[root'[self]]
                                 THEN /\ liveElements' = [liveElements EXCEPT ![root'[self]] = liveElements[root'[self]] \ {vp[self]}]
                                 ELSE /\ TRUE
                                      /\ UNCHANGED liveElements
                           /\ pc' = [pc EXCEPT ![self] = "label3"]
                      ELSE /\ pc' = [pc EXCEPT ![self] = "label8"]
                           /\ UNCHANGED << liveElements, root >>
                /\ UNCHANGED << ufStatus, parent, workerSet, stack, a, b, ra,
                                rb, recursionStack, rootStack, backtrack, v, w,
                                vp, edgesUnexplored, claimed >>

label8(self) == /\ pc[self] = "label8"
                /\ IF edgesUnexplored[self][vp[self]] # {}
                      THEN /\ \E edge \in edgesUnexplored[self][vp[self]]:
                                /\ edgesUnexplored' = [edgesUnexplored EXCEPT ![self][vp[self]] = edgesUnexplored[self][vp[self]] \ {edge}]
                                /\ w' = [w EXCEPT ![self] = edge[2]]
                           /\ pc' = [pc EXCEPT ![self] = "label10"]
                      ELSE /\ pc' = [pc EXCEPT ![self] = "label12"]
                           /\ UNCHANGED << w, edgesUnexplored >>
                /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                stack, a, b, ra, rb, recursionStack, rootStack,
                                backtrack, v, vp, root, claimed >>

label10(self) == /\ pc[self] = "label10"
                 /\ IF w[self] = vp[self]
                       THEN /\ pc' = [pc EXCEPT ![self] = "label8"]
                            /\ UNCHANGED << workerSet, recursionStack, v, root,
                                            claimed >>
                       ELSE /\ root' = [root EXCEPT ![self] = find(w[self])]
                            /\ IF isDead(root'[self])
                                  THEN /\ claimed' = [claimed EXCEPT ![self] = "claim-dead"]
                                       /\ UNCHANGED workerSet
                                  ELSE /\ IF self \in workerSet[root'[self]]
                                             THEN /\ claimed' = [claimed EXCEPT ![self] = "claim-found"]
                                                  /\ UNCHANGED workerSet
                                             ELSE /\ workerSet' = [workerSet EXCEPT ![root'[self]] = workerSet[root'[self]] \cup {self}]
                                                  /\ claimed' = [claimed EXCEPT ![self] = "claim-success"]
                            /\ IF claimed'[self] = "claim-dead"
                                  THEN /\ pc' = [pc EXCEPT ![self] = "label8"]
                                       /\ UNCHANGED << recursionStack, v >>
                                  ELSE /\ IF claimed'[self] = "claim-success"
                                             THEN /\ recursionStack' = [recursionStack EXCEPT ![self] = << (<< vp[self], v[self] >>) >> \o recursionStack[self]]
                                                  /\ v' = [v EXCEPT ![self] = w[self]]
                                                  /\ pc' = [pc EXCEPT ![self] = "label2"]
                                             ELSE /\ pc' = [pc EXCEPT ![self] = "label11"]
                                                  /\ UNCHANGED << recursionStack,
                                                                  v >>
                 /\ UNCHANGED << ufStatus, parent, liveElements, stack, a, b,
                                 ra, rb, rootStack, backtrack, w, vp,
                                 edgesUnexplored >>

label14(self) == /\ pc[self] = "label14"
                 /\ pc' = [pc EXCEPT ![self] = "label8"]
                 /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                 stack, a, b, ra, rb, recursionStack,
                                 rootStack, backtrack, v, w, vp, root,
                                 edgesUnexplored, claimed >>

label11(self) == /\ pc[self] = "label11"
                 /\ IF sameSet(w[self], v[self]) # FALSE
                       THEN /\ root' = [root EXCEPT ![self] = Head(rootStack[self])]
                            /\ rootStack' = [rootStack EXCEPT ![self] = Tail(rootStack[self])]
                            /\ /\ a' = [a EXCEPT ![self] = Head(rootStack'[self])]
                               /\ b' = [b EXCEPT ![self] = root'[self]]
                               /\ stack' = [stack EXCEPT ![self] = << [ procedure |->  "unite",
                                                                        pc        |->  "label16",
                                                                        ra        |->  ra[self],
                                                                        rb        |->  rb[self],
                                                                        a         |->  a[self],
                                                                        b         |->  b[self] ] >>
                                                                    \o stack[self]]
                            /\ ra' = [ra EXCEPT ![self] = defaultInitValue]
                            /\ rb' = [rb EXCEPT ![self] = defaultInitValue]
                            /\ pc' = [pc EXCEPT ![self] = "label13"]
                       ELSE /\ pc' = [pc EXCEPT ![self] = "label14"]
                            /\ UNCHANGED << stack, a, b, ra, rb, rootStack,
                                            root >>
                 /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                 recursionStack, backtrack, v, w, vp,
                                 edgesUnexplored, claimed >>

label16(self) == /\ pc[self] = "label16"
                 /\ pc' = [pc EXCEPT ![self] = "label11"]
                 /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                 stack, a, b, ra, rb, recursionStack,
                                 rootStack, backtrack, v, w, vp, root,
                                 edgesUnexplored, claimed >>

label12(self) == /\ pc[self] = "label12"
                 /\ root' = [root EXCEPT ![self] = find(vp[self])]
                 /\ IF vp[self] \in liveElements[root'[self]]
                       THEN /\ liveElements' = [liveElements EXCEPT ![root'[self]] = liveElements[root'[self]] \ {vp[self]}]
                       ELSE /\ TRUE
                            /\ UNCHANGED liveElements
                 /\ pc' = [pc EXCEPT ![self] = "label3"]
                 /\ UNCHANGED << ufStatus, parent, workerSet, stack, a, b, ra,
                                 rb, recursionStack, rootStack, backtrack, v,
                                 w, vp, edgesUnexplored, claimed >>

label6(self) == /\ pc[self] = "label6"
                /\ IF Head(rootStack[self]) = v[self]
                      THEN /\ rootStack' = [rootStack EXCEPT ![self] = Tail(rootStack[self])]
                      ELSE /\ TRUE
                           /\ UNCHANGED rootStack
                /\ IF Len(recursionStack[self]) # 0
                      THEN /\ backtrack' = [backtrack EXCEPT ![self] = TRUE]
                           /\ pc' = [pc EXCEPT ![self] = "label2"]
                      ELSE /\ pc' = [pc EXCEPT ![self] = "label1"]
                           /\ UNCHANGED backtrack
                /\ UNCHANGED << ufStatus, parent, workerSet, liveElements,
                                stack, a, b, ra, rb, recursionStack, v, w, vp,
                                root, edgesUnexplored, claimed >>

T(self) == label1(self) \/ label2(self) \/ label3(self) \/ label4(self)
              \/ label5(self) \/ label7(self) \/ label8(self)
              \/ label10(self) \/ label14(self) \/ label11(self)
              \/ label16(self) \/ label12(self) \/ label6(self)

Next == (\E self \in ProcSet: unite(self))
           \/ (\E self \in Threads: T(self))
           \/ (* Disjunct to prevent deadlock on termination *)
              ((\A self \in ProcSet: pc[self] = "Done") /\ UNCHANGED vars)

Spec == /\ Init /\ [][Next]_vars
        /\ \A self \in Threads : WF_vars(T(self)) /\ WF_vars(unite(self))

Termination == <>(\A self \in ProcSet: pc[self] = "Done")

\* END TRANSLATION

SCCsFromAlgo == { ufSet(node) : node \in Nodes }

\* SCCs by definition

OutgoingNodes(S) ==
  UNION { { e[2] \in OutgoingEdges(node) : e[2] \notin S } : node \in S }

Reachable(x, y) ==
  LET RECURSIVE RF(_)
      RF(S) == LET NS == OutgoingEdgesSet(S)
               IN IF y \in S THEN TRUE
                             ELSE IF NS = {} THEN FALSE
                                              ELSE RF(S \cup NS)
  IN RF({x})

SCCsByDef ==
  LET RECURSIVE M(_, _)
      M(Partial, Rest) ==
        IF Rest = {} THEN Partial
                     ELSE LET x == CHOOSE x \in Rest : TRUE
                              C == {y \in Rest : /\ Reachable(x, y)
                                                 /\ Reachable(y, x)}
                          IN M(Partial \cup{C}, Rest \ C)
  IN M({}, Nodes)

Terminated == \A self \in ProcSet: pc[self] = "Done"
Correct == Terminated => SCCsFromAlgo = SCCsByDef

================================================================================
