import sun.awt.HToolkit;

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
			HtCollection.put(order.get(i), rootHeap);

			if(!HinCollection.containsKey(order.get(i))) { HinCollection.put(order.get(i), new PathGraph.Heap_In()); }

			if (i == 0) {

				// Add pointers related to the Hin we just added into Ht
				if (HinCollection.get(order.get(i)).root != null) {

					rootHeap.insert(HinCollection.get(order.get(i)).root);

					// Add heapEdge-pointers from root to first child of heap
					HeapEdge source = HinCollection.get(order.get(i)).root;
					HeapEdge heapEdgeDestination = HinCollection.get(order.get(i)).children.heap[1];
					if(heapEdgeDestination != null) {
						double heapEdgeWeight = heapEdgeDestination.priority - source.priority;
						g.add(source, heapEdgeDestination, heapEdgeWeight, order.get(i), order.get(i));
					}

					// Add pointers through children binary heap
					HeapEdge[] tempChildHeap = HinCollection.get(order.get(i)).children.heap;
					int tempHeapSize = HinCollection.get(order.get(i)).children.sizetillnow;
					for(int z = 1; z <= tempHeapSize/2; z++) {
						if(tempChildHeap[2*z] != null) {
							double heapEdgeWeight_left = tempChildHeap[2 * z].priority - tempChildHeap[z].priority;
							g.add(tempChildHeap[z], tempChildHeap[2*z], heapEdgeWeight_left, order.get(i), order.get(i));
						}
						if(tempChildHeap[2*z+1] != null) {
							double heapEdgeWeight_right = tempChildHeap[2*z+1].priority - tempChildHeap[z].priority;
							g.add(tempChildHeap[z], tempChildHeap[2*z+1], heapEdgeWeight_right, order.get(i), order.get(i));
						}
					}

				}
			}

			if (i > 0) {

				// if(HtCollection.containsKey(order.get(i-1)) ) {
				if (HtCollection.get(order.get(i - 1)) != null) {

					MinHeap l = HtCollection.get(order.get(i - 1));
					MinHeap h;
					h = l.clone();

					rootHeap = h;
					HtCollection.put(order.get(i), rootHeap);

					// Add pointers related to the Hin that we just added into Ht
					if (HinCollection.get(order.get(i)).root != null) {
						h.insert(HinCollection.get(order.get(i)).root);

						// add heapEdge-pointers from root to first child of heap
						HeapEdge source = HinCollection.get(order.get(i)).root;
						HeapEdge heapEdgeDestination = HinCollection.get(order.get(i)).children.heap[1];
						if(heapEdgeDestination != null) {
							double heapEdgeweight = heapEdgeDestination.priority - source.priority;
							g.add(source, heapEdgeDestination, heapEdgeweight, order.get(i), order.get(i));
						}

						// add pointers through children binary heap
						HeapEdge[] tempChildHeap = HinCollection.get(order.get(i)).children.heap;
						int tempHeapSize = HinCollection.get(order.get(i)).children.sizetillnow;
						for(int z = 1; z <= tempHeapSize/2; z++) {
							if(tempChildHeap[2*z] != null) {
								double heapEdgeWeight_left = tempChildHeap[2 * z].priority - tempChildHeap[z].priority;
								g.add(tempChildHeap[z], tempChildHeap[2*z], heapEdgeWeight_left, order.get(i), order.get(i));
							}
							if(tempChildHeap[2*z+1] != null) {
								double heapEdgeWeight_right = tempChildHeap[2*z+1].priority - tempChildHeap[z].priority;
								g.add(tempChildHeap[z], tempChildHeap[2*z+1], heapEdgeWeight_right, order.get(i), order.get(i));
							}
						}
					}

					// Connect every root of Ht
					HeapEdge[] HT_temp = HtCollection.get(order.get(i)).heap;
					int tempHTSize = HtCollection.get(order.get(i)).sizetillnow;
					for(int z = 1; z <= tempHTSize/2; z++) {
						if(HT_temp[2*z] != null) {
							double HTEdgeWeight_left = HT_temp[2 * z].priority - HT_temp[z].priority;
							g.add(HT_temp[z], HT_temp[2*z], HTEdgeWeight_left, order.get(i), order.get(i));
						}
						if(HT_temp[2*z+1] != null) {
							double HTEdgeWeight_right = HT_temp[2*z+1].priority - HT_temp[z].priority;
							g.add(HT_temp[z], HT_temp[2*z+1], HTEdgeWeight_right, order.get(i), order.get(i));
						}
					}
				}

			}

		}

		// Now another iteration to create cross edges
		for(int i = 0; i < order.size(); i++) {

			// add crossedge every node in heap to root of source
			HeapEdge[] tempEdges = HtCollection.get(order.get(i)).heap;
			int tempEdgesSize = HtCollection.get(order.get(i)).sizetillnow;
			if(tempEdgesSize > 0) {
				for(int z = 1; z <= tempEdgesSize; z++) {
					HeapEdge tempSrc = tempEdges[z];

					if(!HtCollection.containsKey(tempSrc.source)) { continue; }
					if(HtCollection.get(tempSrc.source).sizetillnow == 0) { continue; }

					HeapEdge tempDest = HtCollection.get(tempSrc.source).heap[1];
					if(tempDest != null) {
						double weight = tempDest.priority;
						g.add(tempSrc, tempDest, weight, order.get(i), tempSrc.source);
					}
				}
			}

			// add pointers from every child to source's root
			tempEdges = HinCollection.get(order.get(i)).children.heap;
			tempEdgesSize = HinCollection.get(order.get(i)).children.sizetillnow;
			if(tempEdgesSize > 0) {
				for(int z = 1; z <= tempEdgesSize; z++) {
					HeapEdge tempSrc = tempEdges[z];

					if(!HtCollection.containsKey(tempSrc.source)) { continue; }
					if(HtCollection.get(tempSrc.source).sizetillnow == 0) { continue; }

					HeapEdge tempDest = HtCollection.get(tempSrc.source).heap[1];
					if(tempDest != null) {
						double weight = tempDest.priority;
						g.add(tempSrc, tempDest, weight, order.get(i), tempSrc.source);
					}
				}
			}


		}

		if(HtCollection.get(order.get(order.size()-1)).heap[1] != null)
			g.setStart(HtCollection.get(order.get(order.size()-1)).heap[1]);
		return HtCollection;

	}

}




