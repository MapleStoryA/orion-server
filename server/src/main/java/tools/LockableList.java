/*
 * This file is part of the OdinMS MapleStory Private Server
 * Copyright (C) 2011 Patrick Huy and Matthias Butz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tools;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author GoldenKevin
 */
public class LockableList<T> implements List<T> {

  private List<T> list;
  private Lock readLock, writeLock;

  public LockableList(List<T> list, ReadWriteLock rwLock) {
    this.list = list;
    this.readLock = rwLock.readLock();
    this.writeLock = rwLock.writeLock();
  }

  public LockableList(List<T> list) {
    this(list, new ReentrantReadWriteLock());
  }

  public LockableList(ReadWriteLock rwLock) {
    this(new ArrayList<T>(), rwLock);
  }

  public LockableList() {
    this(new ArrayList<T>(), new ReentrantReadWriteLock());
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return list.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return list.iterator();
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <E> E[] toArray(E[] a) {
    return list.toArray(a);
  }

  @Override
  public boolean add(T e) {
    return list.add(e);
  }

  @Override
  public boolean remove(Object o) {
    return list.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return list.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    return list.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return list.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return list.retainAll(c);
  }

  @Override
  public void clear() {
    list.clear();
  }

  @Override
  public T get(int index) {
    return list.get(index);
  }

  @Override
  public T set(int index, T element) {
    return list.set(index, element);
  }

  @Override
  public void add(int index, T element) {
    list.add(index, element);
  }

  @Override
  public T remove(int index) {
    return list.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return list.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return list.listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  public void lockRead() {
    readLock.lock();
  }

  public void unlockRead() {
    readLock.unlock();
  }

  public void lockWrite() {
    writeLock.lock();
  }

  public void unlockWrite() {
    writeLock.unlock();
  }

  public void addWhenSafe(T e) {
    lockWrite();
    try {
      add(e);
    } finally {
      unlockWrite();
    }
  }

  public void removeWhenSafe(T e) {
    lockWrite();
    try {
      remove(e);
    } finally {
      unlockWrite();
    }
  }

  public int getSizeWhenSafe() {
    lockRead();
    try {
      return size();
    } finally {
      unlockRead();
    }
  }
}
