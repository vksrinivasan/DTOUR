import java.util.*;

public class TreeHeap {

	HashMap<Integer, MinHeap> HtCollection;

	TreeHeap() {

		HtCollection = new HashMap<Integer, MinHeap>();

	}

	HashMap<Integer, MinHeap> createHtCollection(List<Integer> order, HashMap<Integer, PathGraph.Heap_In> HinCollection,
			Graph g) {

		for (int i = 0; i < order.size(); i++) {
			MinHeap rootHeap = new MinHeap(100);

			if (HinCollection.get(order.get(i)) == null) {
				PathGraph x = new PathGraph();
				HinCollection.put(order.get(i), x.nil1);
			}

			if (i == 0) {

				if (HinCollection.get(order.get(i)).root != null) {

					rootHeap.insert(HinCollection.get(order.get(i)).root);
					// add crossedge every node in heap to root of source
					PathGraph.HeapEdge source = HinCollection.get(i).root;
					int src = HinCollection.get(order.get(i)).root.source;
					PathGraph.HeapEdge destination = HinCollection.get(src).root;
					double weight = destination.priority;

					g.add(source, destination, weight);

					// add heapEdge-pointers to children

					PathGraph.HeapEdge heapEdgeDestination = HinCollection.get(order.get(i)).children.heap[1];
					double heapEdgeweight = heapEdgeDestination.priority - source.priority;
					g.add(source, heapEdgeDestination, heapEdgeweight);

				}
				HtCollection.put(order.get(i), rootHeap);
			}

			if (i > 0) {

				// if(HtCollection.containsKey(order.get(i-1)) ) {
				if (HtCollection.get(order.get(i - 1)) != null) {

					MinHeap l = HtCollection.get(order.get(i - 1));
					MinHeap h;
					try {
						h = (MinHeap) l.clone();
						if (HinCollection.get(order.get(i)).root != null) {
							h.insert(HinCollection.get(order.get(i)).root);
							// add crossedge every node in heap to root of source
							PathGraph.HeapEdge source = HinCollection.get(order.get(i)).root;
							int src = HinCollection.get(order.get(i)).root.source;
							PathGraph.HeapEdge destination = HinCollection.get(src).root;
							double weight = destination.priority;

							g.add(source, destination, weight);

							// add heapEdge-pointers to children

							PathGraph.HeapEdge heapEdgeDestination = HinCollection.get(order.get(i)).children.heap[1];
							double heapEdgeweight = heapEdgeDestination.priority - source.priority;
							g.add(source, heapEdgeDestination, heapEdgeweight);

						}

						rootHeap = h;
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block

						System.out.println("not able to clone");
						e.printStackTrace();
					}

					HtCollection.put(order.get(i), rootHeap);

					// }
				}

			}

		}

		return HtCollection;

	}

}
