/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.util.graph.impl;

import java.util.Iterator;

import com.ibm.wala.util.graph.NumberedEdgeManager;
import com.ibm.wala.util.intset.IntSet;

/**
 * An edge manager that reverses the edges in a graph
 */
public class InvertingNumberedEdgeManager<T> implements NumberedEdgeManager<T> {

  private final NumberedEdgeManager<T> original;

  public InvertingNumberedEdgeManager(NumberedEdgeManager<T> original) {
    if (original == null) {
      throw new IllegalArgumentException("null original");
    }
    this.original = original;
  }

  public Iterator<T> getPredNodes(T N) throws IllegalArgumentException{
    return original.getSuccNodes(N);
  }

  public int getPredNodeCount(T N) throws IllegalArgumentException{
    return original.getSuccNodeCount(N);
  }

  public Iterator<T> getSuccNodes(T N) throws IllegalArgumentException{
    return original.getPredNodes(N);
  }

  public int getSuccNodeCount(T N) throws IllegalArgumentException{
    return original.getPredNodeCount(N);
  }

  public void addEdge(T src, T dst) throws IllegalArgumentException{
    original.addEdge(dst, src);
  }

  public void removeEdge(T src, T dst)throws IllegalArgumentException {
    original.removeEdge(dst, src);
  }
 
  public boolean hasEdge(T src, T dst) {
    return original.hasEdge(dst, src);
  }

  public void removeAllIncidentEdges(T node) throws IllegalArgumentException{
    original.removeAllIncidentEdges(node);
  }
  
  public void removeIncomingEdges(T node) throws IllegalArgumentException{
    original.removeOutgoingEdges(node);
  }
  
  public void removeOutgoingEdges(T node) throws IllegalArgumentException{
    original.removeIncomingEdges(node);
  }

  public IntSet getSuccNodeNumbers(T node) throws IllegalArgumentException{
    return original.getPredNodeNumbers(node);
  }

  public IntSet getPredNodeNumbers(T node) throws IllegalArgumentException{
    return original.getSuccNodeNumbers(node);
  }

}