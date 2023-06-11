import pymetis

def test(adjacency_list, n_partitions):
    n_cuts, membership = pymetis.part_graph(n_partitions, adjacency=adjacency_list)
    return membership
