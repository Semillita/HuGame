package dev.hugame.util;

import java.util.Arrays;
import java.util.Optional;

public class IntList {

	private final boolean lockedSize;
	private int[] elements;
	private int size;
	
	public IntList() {
		elements = new int[32];
		lockedSize = false;
	}
	
	public IntList(int sizeAllocated) {
		elements = new int[sizeAllocated];
		lockedSize = true;
		size = 0;
	}
	
	public int indexOf(int target) {
		for (int i = 0; i < size; i++) {
			if (elements[i] == target) {
				return i;
			}
		}
		
		return -1;
	}

	public int get(int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("Index " + index + " out of bounds for size " + size);
		}
		
		return elements[index];
	}
	
	public int getSize() {
		return size;
	}
	
	public boolean contains(int target) {
		return indexOf(target) != -1;
	}
	
	public void add(int newElement) {
		if (size >= elements.length) {
			if (lockedSize) {
				throw new UnsupportedOperationException("Size limit of list reached");
			} else {
				var newElements = Arrays.copyOf(elements, elements.length * 2);
				elements = newElements;
			}
		}
		
		elements[size] = newElement;
		size++;
	}
	
	public void clear() {
		size = 0;
	}
	
	/*@Override
	public boolean contains(Object object) {
		if (o instanceof )
		
	}
	
	public boolean containsExact(E object) {
		for (int i = 0; i < super.size(); i++) {
			var objectAt = super.elementData
		}
		return false;
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean add(E e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean remove(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public E get(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public E set(int index, E element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void add(int index, E element) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public E remove(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int indexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ListIterator<E> listIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		// TODO Auto-generated method stub
		return null;
	}*/

}
