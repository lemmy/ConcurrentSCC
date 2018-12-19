Following are the graphs that showed a bug in specification:

- Input: {Nodes <- {1, 2, 3}, Edges <- Nodes \X Nodes, Threads <- {1, 2}}
  - Problem: Type for recursion stack was being violated.
- Input: {Nodes <- {1, 2, 3}, Edges <- {<<1, 2>>, <<2, 1>>, <<3, 1>>}, Threads <- {1, 2}}
  - Problem: We were getting 2 was live although the set {1, 2} was dead.
- Input: {Nodes <- 1..6, Edges <- {<<1, 2>>, <<2, 3>>, <<3, 1>>, <<4, 5>>, <<5, 6>>, <<6, 4>>, <<1, 4>>}, Threads <- {1, 2}}
  - Problem: After updating status of a set to dead we were still using the previous status. To fix this added a new label.
