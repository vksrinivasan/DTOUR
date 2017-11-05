
public class MinHeap {

	HeapEdge heap[];
	int sizetillnow;
	static final int FRONT = 1;

	/**
	 * MinHeap constructor used to initialize MinHeap
	 * 
	 */

	public MinHeap(int size) {
		heap = new HeapEdge[size + 1];
		sizetillnow = 0;
		heap[0] = new HeapEdge(-1, -1, -1);
	}

	public MinHeap clone() {
		MinHeap toReturn = new MinHeap(this.heap.length);
		toReturn.sizetillnow = this.sizetillnow;
		int index = 0;

		for(HeapEdge temp : this.heap) {
			if(temp == null) {
				toReturn.heap[index] = null;
			} else {
				toReturn.heap[index] = new HeapEdge(temp);
			}
			index++;
		}

		return toReturn;
	}

	/**
	 * insert(int value) method used to insert value into MinHeap.
	 * 
	 */

	public void insert(HeapEdge value) {
		sizetillnow = sizetillnow + 1;
		heap[sizetillnow] = value;
		// System.out.println("insert"+sizetillnow);

		int current = sizetillnow;

		while (heap[current].priority < heap[current / 2].priority) {
			// System.out.println("current"+heap[current]+ "current/2"+heap[current/2]);
			HeapEdge tmp;
			tmp = heap[current];
			heap[current] = heap[current / 2];
			heap[current / 2] = tmp;
			current = current / 2;

		}

	}

	/**
	 * remove() method used to pop the minimum element.
	 * 
	 */
	public HeapEdge remove() {
		HeapEdge popped = heap[FRONT];
		// System.out.println("popped"+popped);
		heap[FRONT] = heap[sizetillnow--];
		minHeapify(FRONT);
		// for (int i = 0; i <= sizetillnow; i++ ){
		// System.out.println(heap[i]);
		// }
		return popped;
	}

	/**
	 * isLeaf(int pos) method takes position of element as input parameter and
	 * returns boolean value(true if it is a leaf).
	 * 
	 */
	private boolean isLeaf(int pos) {
		// System.out.println("pos" +pos +"sizetillnow "+sizetillnow );
		if (pos > sizetillnow / 2 && pos <= sizetillnow) {

			return true;

		}

		return false;

	}

	/**
	 * minHeapify(int pos) takes position as input parameter and is used to heapify
	 * the heap after insertion or deletion
	 * 
	 */

	public void minHeapify(int pos) {

		if (pos > sizetillnow)
			return;
		if (!isLeaf(pos)) {

			if (heap[pos].priority > heap[pos * 2].priority || heap[pos].priority > heap[(pos * 2) + 1].priority) {

				if (heap[pos * 2].priority < heap[pos * 2 + 1].priority) {

					HeapEdge tmp = heap[pos];
					heap[pos] = heap[pos * 2];
					heap[pos * 2] = tmp;
					minHeapify(pos * 2);

				} else {

					HeapEdge tmp = heap[pos];
					heap[pos] = heap[pos * 2 + 1];
					heap[pos * 2 + 1] = tmp;
					minHeapify(pos * 2 + 1);

				}

			}

		}

	}

	/**
	 * print() is used to check the status of the heap.
	 * 
	 */

	public void print() {
		System.out.println("status:");

		for (int i = 1; i <= sizetillnow; i++) {

			System.out.print(heap[i] + " ");

		}

		System.out.println();
	}

	/*
	 * public static void main(String...args){ System.out.println("MIN HEAP:");
	 * MinHeap minHeap = new MinHeap(200); minHeap.insert(10); minHeap.print();
	 * minHeap.insert(7); minHeap.print(); minHeap.insert(3); minHeap.print();
	 * 
	 * minHeap.insert(2); minHeap.print(); minHeap.insert(8); minHeap.print();
	 * System.out.println("Popping minimum value " + minHeap.remove());
	 * 
	 * minHeap.print();
	 * 
	 * minHeap.insert(34); minHeap.print(); minHeap.insert(16); minHeap.print();
	 * minHeap.insert(0); minHeap.print();
	 * System.out.println("Popping minimum value " + minHeap.remove());
	 * minHeap.print(); minHeap.print(); minHeap.insert(100); minHeap.print();
	 * 
	 * //minHeap.minHeap();
	 * 
	 * minHeap.print();
	 * 
	 * System.out.println("Popping minimum value " + minHeap.remove());
	 * minHeap.print(); System.out.println("Popping minimum value " +
	 * minHeap.remove()); minHeap.print();
	 * System.out.println("Popping minimum value " + minHeap.remove());
	 * minHeap.print(); System.out.println("Popping minimum value " +
	 * minHeap.remove()); minHeap.print();
	 * 
	 * System.out.println("Popping minimum value " + minHeap.remove());
	 * minHeap.print(); System.out.println("Popping minimum value " +
	 * minHeap.remove()); minHeap.print();
	 * System.out.println("Popping minimum value " + minHeap.remove());
	 * minHeap.print();
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

}
