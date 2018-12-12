--------------------- MODULE MCBloemenSCC ------------------------

EXTENDS BloemenSCC

MCNodes == 1..3
MCEdges == MCNodes \X MCNodes
MCThreads == 1..2

==================================================================
