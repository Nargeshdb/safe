/******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.cast.tree.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.cast.tree.CAstControlFlowMap;
import com.ibm.wala.cast.tree.CAstNode;
import com.ibm.wala.cast.tree.CAstSourcePositionMap;

/**
 * An implementation of a CAstControlFlowMap that is designed to be used by
 * producers of CAPA asts. In addition to implementing the control flow map, it
 * additionally allows clients to record control flow mappings in terms of some
 * arbitrary type object that are then mapped to CAstNodes by the client. These
 * objects can be anything, but one common use is that some type of parse tree
 * is walked to build a capa ast, with control flow being recorded in terms of
 * parse tree nodes and then ast nodes being mapped to parse tree nodes.
 * 
 * Note that, at present, support for mapping control flow on ast nodes directly
 * is clunky. It is necessary to establish that an ast nodes maps to itself,
 * i.e. call xx.map(node, node).
 * 
 * @author Julian Dolby (dolby@us.ibm.com)
 */
public class CAstControlFlowRecorder implements CAstControlFlowMap {
  private final CAstSourcePositionMap src;

  private final Map<CAstNode,Object> CAstToNode = new LinkedHashMap<CAstNode,Object>();

  private final Map<Object,CAstNode> nodeToCAst = new LinkedHashMap<Object,CAstNode>();

  private final Map<Key,Object> table = new LinkedHashMap<Key,Object>();

  private final Map<Object,Set<Object>> labelMap = new LinkedHashMap<Object,Set<Object>>();

  private final Map<Object,Set<Object>> sourceMap = new LinkedHashMap<Object,Set<Object>>();

  private static class Key {
    private final Object label;

    private final Object from;

    Key(Object label, Object from) {
      assert from != null;
      this.from = from;
      this.label = label;
    }

    public int hashCode() {
      if (label != null)
        return from.hashCode() * label.hashCode();
      else
        return from.hashCode();
    }

    public boolean equals(Object o) {
      return (o instanceof Key) && from == ((Key) o).from
          && ((label == null) ? ((Key) o).label == null : label.equals(((Key) o).label));
    }
  }

  public CAstControlFlowRecorder(CAstSourcePositionMap src) {
    this.src = src;
    map(EXCEPTION_TO_EXIT, EXCEPTION_TO_EXIT);
  }

  public CAstNode getTarget(CAstNode from, Object label) {
    Key key = new Key(label, CAstToNode.get(from));
    if (table.containsKey(key))
      return (CAstNode) nodeToCAst.get(table.get(key));
    else
      return null;
  }

  public Collection<Object> getTargetLabels(CAstNode from) {
    if (labelMap.containsKey(CAstToNode.get(from))) {
      return labelMap.get(CAstToNode.get(from));
    } else {
      return Collections.emptySet();
    }
  }

  public Collection getSourceNodes(CAstNode to) {
    if (sourceMap.containsKey(CAstToNode.get(to))) {
      return (Set) sourceMap.get(CAstToNode.get(to));
    } else {
      return Collections.EMPTY_SET;
    }
  }

  public Collection<CAstNode> getMappedNodes() {
    Set<CAstNode> nodes = new LinkedHashSet<CAstNode>();
    for (Iterator keys = table.keySet().iterator(); keys.hasNext();) {
      nodes.add((CAstNode) nodeToCAst.get(((Key) keys.next()).from));
    }

    return nodes;
  }

  /**
   * Add a control-flow edge from the `from' node to the `to' node with the
   * (possibly null) label `label'. These nodes must be mapped by the client to
   * CAstNodes using the `map' call; this mapping can happen before or after
   * this add call.
   */
  public void add(Object from, Object to, Object label) {
    table.put(new Key(label, from), to);

    Set<Object> ls = labelMap.get(from);
    if (ls == null)
      labelMap.put(from, ls = new LinkedHashSet<Object>(2));
    ls.add(label);

    Set<Object> ss =  sourceMap.get(to);
    if (ss == null)
      sourceMap.put(to, ss = new LinkedHashSet<Object>(2));
    ss.add(from);
  }

  /**
   * Establish a mapping between some object `node' and the ast node `ast'.
   * Objects used a endpoints in a control flow edge must be mapped to ast nodes
   * using this call.
   */
  public void map(Object node, CAstNode ast) {
    assert node != null;
    assert ast != null;
    assert ! nodeToCAst.containsKey(node) : node + " already mapped:\n" + this;
    assert ! CAstToNode.containsKey(ast) : ast + " already mapped:\n" + this;
    nodeToCAst.put(node, ast);
    CAstToNode.put(ast, node);
  }

  public void addAll(CAstControlFlowMap other) {
    for(CAstNode n : other.getMappedNodes()) {
      for(Object l : other.getTargetLabels(n)) {
        add(n, l, other.getTarget(n, l));
      }
    }
  }
  
  public boolean isMapped(Object node) {
    return nodeToCAst.containsKey(node);
  }

  public String toString() {
    StringBuffer sb = new StringBuffer("control flow map\n");
    for(Iterator keys = table.keySet().iterator(); keys.hasNext(); ) {
      Key key = (Key) keys.next();
      sb.append(key.from);
      if (src != null &&
	  nodeToCAst.get(key.from) != null &&
	  src.getPosition(nodeToCAst.get(key.from)) != null)
      {
	sb.append(" (").append(src.getPosition(nodeToCAst.get(key.from))).append(") ");
      }
      sb.append(" -- "); 
      sb.append(key.label);
      sb.append(" --> ");
      sb.append(table.get(key));
      sb.append("\n");
    }
    sb.append("\n");
    return sb.toString();
  }
}