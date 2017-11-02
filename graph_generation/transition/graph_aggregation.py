# See the database schema in DTOUR/graph/README.txt for exact schema details.

# This file should connect to the mysql database and aggregate transition_groups.
# It should take in as command line arguments '<new_transition_group_name> <transition_group1> <transition_group2> ...'
# where transition_group1, transition_group2, ... are the (name or id) of existing transition_groups.

# To aggregate transition groups:
# 1. Create the new transition group
# 2. Take the union of the transition_periods that reference transition_group1, transition_group2, ... and make copies
#    of them that reference new transition group instead.
# 3. Take the intersection of transition_graphs (meaning same start and end interval) that correspond to transition_group1, transition_group2, ...
#    Create copies of them that reference the new transition_group.
# 4. For each of the new transition_graphs above,, add the corresponding transition_edges from the corresponding graphs in transition_group1, transition_group2, ...
#    Have these new transition_edges reference the new transition graph.