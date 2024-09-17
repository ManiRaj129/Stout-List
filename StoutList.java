import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Implementation of the list interface based on linked nodes that store
 * multiple items per node. Rules for adding and removing elements ensure that
 * each node (except possibly the last one) is at least half full.
 * 
 * @author Rejinthala Mani Raj
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
	/**
	 * Default number of elements that may be stored in each node.
	 */
	private static final int DEFAULT_NODESIZE = 4;

	/**
	 * Number of elements that can be stored in each node.
	 */
	private final int nodeSize;

	/**
	 * Dummy node for head. It should be private but set to public here only for
	 * grading purpose. In practice, you should always make the head of a linked
	 * list a private instance variable.
	 */
	public Node head;

	/**
	 * Dummy node for tail.
	 */
	private Node tail;

	/**
	 * Number of elements in the list.
	 */
	private int size;

	/**
	 * Constructs an empty list with the default node size.
	 */
	public StoutList()
	{
		this(DEFAULT_NODESIZE);
	}

	/**
	 * Constructs an empty list with the given node size.
	 * 
	 * @param nodeSize number of elements that may be stored in each node, must be
	 *                 an even number
	 */
	public StoutList(int nodeSize)
	{
		if (nodeSize <= 0 || nodeSize % 2 != 0)
			throw new IllegalArgumentException();

		// dummy nodes
		head = new Node();
		tail = new Node();
		head.next = tail;
		tail.previous = head;
		this.nodeSize = nodeSize;
	}

	/**
	 * Constructor for grading only. Fully implemented.
	 * 
	 * @param head
	 * @param tail
	 * @param nodeSize
	 * @param size
	 */
	public StoutList(Node head, Node tail, int nodeSize, int size)
	{
		this.head = head;
		this.tail = tail;
		this.nodeSize = nodeSize;
		this.size = size;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public boolean add(E item)
	{
		if (item == null)
		{
			throw new NullPointerException();
		}

		// As, size index corresponds to the tail node and offset 0.
		add(size, item);

		return true;
	}

	/**
	 * 
	 * This class holds the information of node and its offset (index availbable for
	 * next element or no of elements in the node)
	 *
	 */
	private class NodeInfo
	{

		public Node node;
		public int offset;

		public NodeInfo(Node node, int offset)
		{
			this.node = node;
			this.offset = offset;
		}
	}

	/**
	 * returns the node and offset corresponding to the logic index
	 * 
	 * @param pos logic index of element
	 * @return NodeInfo object
	 */
	private NodeInfo find(int pos)
	{
		NodeInfo result = null;
		if (pos == size)
		{
			result = new NodeInfo(tail, 0);
		} else
		{
			Node curNode = head.next;

			int curPos = curNode.count;

			while (curNode != tail && curPos <= pos)
			{
				curNode = curNode.next;

				curPos += curNode.count;
			}

			int offset = (curNode.count - 1) - (curPos - 1 - pos);

			result = new NodeInfo(curNode, offset);
		}

		return result;
	}

	/**
	 * adds or links new node to list after the given current node
	 * 
	 * @param curr    current node
	 * @param newNode new Node
	 */
	private void link(Node curr, Node newNode)
	{
		newNode.previous = curr;
		newNode.next = curr.next;
		curr.next = newNode;
		curr.next.previous = newNode;
	}

	/**
	 * Unlinks the given node from the list
	 * 
	 * @param node node
	 */
	private void unlink(Node node)
	{
		node.previous.next = node.next;
		node.next.previous = node.previous;
	}

	@Override
	public void add(int pos, E item)
	{
		if (item == null)
		{
			throw new NullPointerException();
		}

		if (pos < 0 || pos > size)
		{
			throw new IndexOutOfBoundsException();
		}

		NodeInfo nodeInfo = find(pos);

		Node node = nodeInfo.node;
		int offset = nodeInfo.offset;

		if (size == 0)
		{
			Node newNode = new Node();

			newNode.addItem(item);

			link(head, newNode);
		} else if (offset == 0)
		{

			if (node.previous != head && node.previous.count < nodeSize)
			{
				node.previous.addItem(item);

			} else if (node == tail && node.previous.count == nodeSize)
			{
				Node newNode = new Node();

				newNode.addItem(item);

				link(tail.previous, newNode);
			}
		} else if (node.count < nodeSize)
		{
			node.addItem(offset, item);

		} else
		{

			Node newNode = new Node();

			link(node, newNode);

			int mid = nodeSize / 2;

			for (int i = mid; i < nodeSize; i++)
			{
				newNode.addItem(node.data[i]);
				node.removeItem(i);
			}

			if (offset <= mid)
			{
				node.addItem(offset, item);
			}

			else
			{
				newNode.addItem(offset - mid, item);

			}
		}

		size++;

	}

	@Override
	public E remove(int pos)
	{
		if (pos < 0 || pos >= size)
		{
			throw new IndexOutOfBoundsException();
		}

		NodeInfo nodeInfo = find(pos);

		return remove(nodeInfo);

	}

	/**
	 * helper method for remove
	 * 
	 * @param nodeInfo nodeInfo object of node that has element which has to be
	 *                 deleted
	 * @return deleted element
	 */
	public E remove(NodeInfo nodeInfo)
	{
		Node node = nodeInfo.node;
		int off = nodeInfo.offset;

		E item = node.data[off];

		int mid = nodeSize / 2;

		if (node.next == tail && node.count == 1)
		{
			unlink(node);
		} else if (node.next == tail || node.count > mid)
		{
			node.removeItem(off);

		} else
		{

			node.removeItem(off);

			if (node.next.count > mid)
			{
				node.addItem(node.next.data[0]);
				node.next.removeItem(0);
			} else
			{

				for (int i = 0; i < node.next.count; i++)
				{
					node.addItem(node.next.data[i]);
				}
				unlink(node.next);
			}

		}

		size--;
		return item;
	}

	/**
	 * Sort all elements in the stout list in the NON-DECREASING order. You may do
	 * the following. Traverse the list and copy its elements into an array,
	 * deleting every visited node along the way. Then, sort the array by calling
	 * the insertionSort() method. (Note that sorting efficiency is not a concern
	 * for this project.) Finally, copy all elements from the array back to the
	 * stout list, creating new nodes for storage. After sorting, all nodes but
	 * (possibly) the last one must be full of elements.
	 * 
	 * Comparator<E> must have been implemented for calling insertionSort().
	 */
	public void sort()
	{
		ListIterator<E> iter = listIterator();
		
		 E[] sortList = (E[]) new Comparable[size];
		
		 for(int i=0; i<size;i++)
		 {
			 sortList[i]=iter.next();
		 }
		 
		 head.next=tail;
		 tail.previous=head;
		 size=0;
		 
		 Comparator<E> compare =new ElementComparator();
		 insertionSort(sortList,compare);
		 
		 for(int i=0; i<sortList.length;i++)
		 {
			 add(sortList[i]);
		 }
		 
	}

	/**
	 * Sort all elements in the stout list in the NON-INCREASING order. Call the
	 * bubbleSort() method. After sorting, all but (possibly) the last nodes must be
	 * filled with elements.
	 * 
	 * Comparable<? super E> must be implemented for calling bubbleSort().
	 */
	public void sortReverse()
	{
		 ListIterator<E> iter = listIterator();
		
		 E[] reverseList = (E[]) new Comparable[size];
		
		 for(int i=0; i<size;i++)
		 {
			 reverseList[i]=iter.next();
		 }
		 	
//		 int j=0;
//		 for(E e: this)
//		 {
//			 reverseList[j]= e;
//			 j++;
//		 }
		 
		 head.next=tail;
		 tail.previous=head;
		 size=0;
		 
		 bubbleSort(reverseList);
		 
		 for(int i=0; i<reverseList.length;i++)
		 {
			 add(reverseList[i]);
		 }
		 
	}

	@Override
	public Iterator<E> iterator()
	{

		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator()
	{

		return new StoutListIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index)
	{
		if (index < 0 || index > size)
		{
			throw new IndexOutOfBoundsException();
		}

		return new StoutListIterator(index);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes.
	 */
	public String toStringInternal()
	{
		return toStringInternal(null);
	}

	/**
	 * Returns a string representation of this list showing the internal structure
	 * of the nodes and the position of the iterator.
	 *
	 * @param iter an iterator for this list
	 */
	public String toStringInternal(ListIterator<E> iter)
	{
		int count = 0;
		int position = -1;
		if (iter != null)
		{
			position = iter.nextIndex();
		}

		StringBuilder sb = new StringBuilder();
		sb.append('[');
		Node current = head.next;
		while (current != tail)
		{
			sb.append('(');
			E data = current.data[0];
			if (data == null)
			{
				sb.append("-");
			} else
			{
				if (position == count)
				{
					sb.append("| ");
					position = -1;
				}
				sb.append(data.toString());
				++count;
			}

			for (int i = 1; i < nodeSize; ++i)
			{
				sb.append(", ");
				data = current.data[i];
				if (data == null)
				{
					sb.append("-");
				} else
				{
					if (position == count)
					{
						sb.append("| ");
						position = -1;
					}
					sb.append(data.toString());
					++count;

					// iterator at end
					if (position == size && count == size)
					{
						sb.append(" |");
						position = -1;
					}
				}
			}
			sb.append(')');
			current = current.next;
			if (current != tail)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Node type for this list. Each node holds a maximum of nodeSize elements in an
	 * array. Empty slots are null.
	 */
	private class Node
	{
		/**
		 * Array of actual data elements.
		 */
		// Unchecked warning unavoidable.
		public E[] data = (E[]) new Comparable[nodeSize];

		/**
		 * Link to next node.
		 */
		public Node next;

		/**
		 * Link to previous node;
		 */
		public Node previous;

		/**
		 * Index of the next available offset in this node, also equal to the number of
		 * elements in this node.
		 */
		public int count;

		/**
		 * Adds an item to this node at the first available offset. Precondition: count
		 * < nodeSize
		 * 
		 * @param item element to be added
		 */
		void addItem(E item)
		{
			if (count >= nodeSize)
			{
				return;
			}
			data[count++] = item;
			// useful for debugging
			// System.out.println("Added " + item.toString() + " at index " + count + " to
			// node " + Arrays.toString(data));
		}

		/**
		 * Adds an item to this node at the indicated offset, shifting elements to the
		 * right as necessary.
		 * 
		 * Precondition: count < nodeSize
		 * 
		 * @param offset array index at which to put the new element
		 * @param item   element to be added
		 */
		void addItem(int offset, E item)
		{
			if (count >= nodeSize)
			{
				return;
			}
			for (int i = count - 1; i >= offset; --i)
			{
				data[i + 1] = data[i];
			}
			++count;
			data[offset] = item;
			// useful for debugging
//      System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
		}

		/**
		 * Deletes an element from this node at the indicated offset, shifting elements
		 * left as necessary. Precondition: 0 <= offset < count
		 * 
		 * @param offset
		 */
		void removeItem(int offset)
		{
			E item = data[offset];
			for (int i = offset + 1; i < nodeSize; ++i)
			{
				data[i - 1] = data[i];
			}
			data[count - 1] = null;
			--count;
		}
	}

	/**
	 * 
	 * StoutListIterator 
	 *
	 */
	private class StoutListIterator implements ListIterator<E>
	{
		private int logicIndex;

		private NodeInfo lastNode;

		private boolean anyMove;

		/**
		 * Default constructor
		 */
		public StoutListIterator()
		{
			this(0);
		}

		/**
		 * Constructor finds node at a given position.
		 * 
		 * @param pos logic index of element in stoutList
		 */
		public StoutListIterator(int pos)
		{
			logicIndex = pos;

			lastNode = null;

			anyMove = false;
		}

		@Override
		public int nextIndex()
		{
			return logicIndex;
		}

		@Override
		public int previousIndex()
		{
			return logicIndex - 1;
		}

		@Override
		public boolean hasNext()
		{
			return logicIndex < size;
		}

		@Override
		public E next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			} else
			{

				NodeInfo nodeInfo = find(logicIndex++);
				lastNode = nodeInfo;
				anyMove = true;

				return nodeInfo.node.data[nodeInfo.offset];
			}
		}

		@Override
		public void add(E item)
		{
			if (item == null)
			{
				throw new NullPointerException();
			}
			
			StoutList.this.add(logicIndex,item);
			
			++logicIndex;
			anyMove =false;
		}
		
		@Override
		public void set(E item)
		{
			if (item == null)
			{
				throw new NullPointerException();
			}

			else if (!anyMove)
			{
				throw new IllegalStateException();
			}
            
			lastNode.node.data[lastNode.offset] = item;
			
		}

		@Override
		public void remove()
		{
			if (!anyMove)
			{
				throw new IllegalStateException();
			}

			NodeInfo currNode = find(logicIndex);

			if (lastNode.offset < currNode.offset || lastNode.node == currNode.node.previous)
			{
				logicIndex--;
			}

			StoutList.this.remove(lastNode);

			lastNode = null;
			anyMove = false;
		}

		@Override
		public boolean hasPrevious()
		{
			return logicIndex > 0;
		}

		@Override
		public E previous()
		{
			if (!hasPrevious())
			{
				throw new NoSuchElementException();
			} else
			{
				NodeInfo nodeInfo = find(--logicIndex);
				lastNode = nodeInfo;
				anyMove = true;

				return nodeInfo.node.data[nodeInfo.offset];
			}
		}
		

	}

	/**
	 * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING
	 * order.
	 * 
	 * @param arr  array storing elements from the list
	 * @param comp comparator used in sorting
	 */
	private void insertionSort(E[] arr, Comparator<? super E> comp)
	{
		for (int i = 1; i < arr.length; i++)
		{
			E temp = arr[i];

			int j = i - 1;

			while (j >= 0 && comp.compare(arr[j], temp) > 0)
			{
				arr[j + 1] = arr[j];
				j--;
			}

			arr[j + 1] = temp;
		}
	}

	/**
	 * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a
	 * description of bubble sort please refer to Section 6.1 in the project
	 * description. You must use the compareTo() method from an implementation of
	 * the Comparable interface by the class E or ? super E.
	 * 
	 * @param arr array holding elements from the list
	 */
	private void bubbleSort(E[] arr)
	{
		boolean swapped;
		for (int i = 1; i < arr.length; i++)
		{
			swapped = false;
			for (int j = 0; j < arr.length - i; j++)
			{
				if (arr[j].compareTo(arr[j + 1]) < 0)
				{
					E temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
					swapped = true;
				}
			}

			if (swapped == false)
			{
				break;
			}
		}
	}

	/**
	 * 
	 * this class implements comparator interface; which is used to compare two
	 * elements.
	 *
	 */
	private class ElementComparator implements Comparator<E>
	{
		@Override
		public int compare(E e1, E e2)
		{
			return e1.compareTo(e2);
		}
	}

}