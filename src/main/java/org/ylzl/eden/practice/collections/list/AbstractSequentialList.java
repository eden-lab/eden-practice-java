/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ylzl.eden.practice.collections.list;

import org.ylzl.eden.practice.collections.Collection;
import org.ylzl.eden.practice.collections.Iterator;

import java.util.NoSuchElementException;

/**
 * 抽象的链表 List
 *
 * @author gyl
 * @since 2.0.0
 */
public abstract class AbstractSequentialList<E> extends AbstractList<E> {

	public E get(int index) {
		try {
			return listIterator(index).next();
		} catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: "+index);
		}
	}

	public E set(int index, E element) {
		try {
			ListIterator<E> e = listIterator(index);
			E oldVal = e.next();
			e.set(element);
			return oldVal;
		} catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: "+index);
		}
	}

	public void add(int index, E element) {
		try {
			listIterator(index).add(element);
		} catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: "+index);
		}
	}

	public E remove(int index) {
		try {
			ListIterator<E> e = listIterator(index);
			E outCast = e.next();
			e.remove();
			return outCast;
		} catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: "+index);
		}
	}

	public boolean addAll(int index, Collection<? extends E> c) {
		try {
			boolean modified = false;
			ListIterator<E> e1 = listIterator(index);
			Iterator<? extends E> e2 = c.iterator();
			while (e2.hasNext()) {
				e1.add(e2.next());
				modified = true;
			}
			return modified;
		} catch (NoSuchElementException exc) {
			throw new IndexOutOfBoundsException("Index: "+index);
		}
	}

	public Iterator<E> iterator() {
		return listIterator();
	}

	public abstract ListIterator<E> listIterator(int index);
}
