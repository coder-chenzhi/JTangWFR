package org.jtang.distance.ged.editpath;


import java.util.ArrayList;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Queue;

import org.jtang.distance.ged.editpath.editoperation.EdgeDeletion;
import org.jtang.distance.ged.editpath.editoperation.EdgeInsertion;
import org.jtang.distance.ged.editpath.editoperation.EditOperation;
import org.jtang.distance.ged.editpath.editoperation.NodeDeletion;
import org.jtang.distance.ged.editpath.editoperation.NodeInsertion;
import org.jtang.distance.ged.editpath.editoperation.NodeSubstitution;
import org.jtang.distance.ged.graph.DecoratedGraph;
import org.jtang.distance.ged.graph.DecoratedNode;
import org.jtang.distance.ged.processor.CostContainer;

/**
 * Graph edit distance finding algorithm implementation.
 * 
 * @author Roman Tekhov
 */
public class EditPathFinder {
	
	/**
	 * Finds the edit path between two given graphs considering
	 * costs from the provided cost container.
	 * 
	 * @param from source graph
	 * @param to destination graph
	 * @param costContainer edit operation cost container
	 * 
	 * @return complete edit distance between the two graphs
	 * 
	 * @throws CostLimitExceededException in case acceptable cost  
	 * 			limit has been exceeded
	 */
	public static EditPath find(DecoratedGraph from, DecoratedGraph to, CostContainer costContainer) 
				throws CostLimitExceededException {
		
//		from.getNodes().contains(to.getNodes());
		
		// Priority queue for the (partial) edit paths. 
		// Paths are ordered by their total costs in ascending order.
		Queue<EditPath> queue = new PriorityQueue<EditPath>();
		
		// Initial operations before the main loop.
		init(from, to, queue, costContainer);
		
		// Main processing iterations.
		while(true) {
			// Retrieve the (partial) edit path with the smallest total cost from the queue.
			EditPath minimumCostPath = queue.poll();
			
			if(costContainer.getAcceptanceLimitCost() != null && minimumCostPath.
					getCost().compareTo(costContainer.getAcceptanceLimitCost()) > 0) {
				throw new CostLimitExceededException(minimumCostPath.getCost());
			}
			
			// If all source graph nodes are mapped return the path as the output.
			if(minimumCostPath.isComplete()) {
				return minimumCostPath;
			}
			
			// Process with the path by extending it further.
			process(from, to, minimumCostPath, queue, costContainer);
		}
	}

	//初始化两个图间的映射关系,源图要相对目标图来说自身节点上的操作就只有被替代或被删除两种
	private static void init(DecoratedGraph from, DecoratedGraph to, 
			Queue<EditPath> queue, CostContainer costContainer) {
		
		// Get the first random node of the source graph
		DecoratedNode firstNode = from.getNextNode(null);
		
		// Add substitutions for the first source node and each destination node to the queue.
		//将源节点与目标图中的每个节点进行操作的遍历比较
		for(DecoratedNode toNode : to.getNodes()) {			
			NodeEditPath substitutionNodeEditPath = new NodeEditPath();
			
			substitutionNodeEditPath.setFrom(firstNode);
			substitutionNodeEditPath.setTo(toNode);
			
			EditOperation substitution = new NodeSubstitution(firstNode, toNode, costContainer);
			substitutionNodeEditPath.addEditOperation(substitution);
			
			boolean firstNodeSelfAdjacent = firstNode.isAdjacent(firstNode);
			boolean toNodeSelfAdjacent = toNode.isAdjacent(toNode);
			
			/*如果源节点的邻近节点是其自己，而相应的目标节点邻节点不是其自身
			 *  那么这意味着源节点所关联的边在目标图中是被删除。
			 *  反过来，关联边是新插入的。
			 */
			if(firstNodeSelfAdjacent && !toNodeSelfAdjacent) {
				EditOperation edgeDeletion = new EdgeDeletion(firstNode, firstNode, costContainer);
				substitutionNodeEditPath.addEditOperation(edgeDeletion);
			} else if(!firstNodeSelfAdjacent && toNodeSelfAdjacent) {
				EditOperation edgeInsertion = new EdgeInsertion(toNode, toNode, costContainer);
				substitutionNodeEditPath.addEditOperation(edgeInsertion);
			}
			
			EditPath substitutionEditPath = new EditPath(from, to);
			substitutionEditPath.addNodeEditPath(substitutionNodeEditPath);
			queue.offer(substitutionEditPath);
		}
		
		// Add deletion of the first source node to the queue.
		EditPath deletionEditPath = new EditPath(from, to);
		processNodeDeletion(from, to, firstNode, deletionEditPath, costContainer, queue);
	}	
	

	private static void process(DecoratedGraph from, DecoratedGraph to, EditPath minimumCostPath,
			Queue<EditPath> queue, CostContainer costContainer) {
		
		// All mapped source and destination nodes of the path.
		Collection<DecoratedNode> mappedFromNodes = new ArrayList<DecoratedNode>();
		Collection<DecoratedNode> mappedToNodes = new ArrayList<DecoratedNode>();
					
		for(NodeEditPath nodeEditPath : minimumCostPath.getNodeEditPaths()) {
			if(nodeEditPath.getFrom() != null) {
				mappedFromNodes.add(nodeEditPath.getFrom());
			}
			
			if(nodeEditPath.getTo() != null) {
				mappedToNodes.add(nodeEditPath.getTo());
			}
		}		
		
		// All unmapped destination nodes. 
		Collection<DecoratedNode> unmappedToNodes = to.getRestNodes(mappedToNodes);
		
		// Check if all source nodes are mapped.
		if(mappedFromNodes.size() < from.getNodeNumber()) {
			// If there are some unmapped source nodes left then choose the next source node.			
			DecoratedNode nextFromNode = from.getNextNode(mappedFromNodes);
			
			// Add substitutions between this node and all unmapped destination nodes to the queue.
			processNodeSubstitutions(from, minimumCostPath, mappedFromNodes, mappedToNodes, 
					unmappedToNodes, nextFromNode, costContainer, queue);
			
			// Add deletion of this node to the queue.
			processNodeDeletion(from, to, nextFromNode, minimumCostPath, costContainer, queue);
		} else {
			// Add insertions of all remaining destination nodes to the queue.
			processNodeInsertions(from, minimumCostPath, unmappedToNodes, costContainer, queue);
		}
	}


	private static void processNodeSubstitutions(DecoratedGraph from,
			EditPath editPath, Collection<DecoratedNode> mappedFromNodes,
			Collection<DecoratedNode> mappedToNodes, 
			Collection<DecoratedNode> unmappedToNodes, 
			DecoratedNode fromNode, CostContainer costContainer, 
			Queue<EditPath> queue) {
		
		for(DecoratedNode unmappedToNode : unmappedToNodes) {
			// Create a copy of edit path.
			EditPath substitutionEditPath = editPath.copy();
			
			// Add node substitution.
			NodeEditPath substitutionNodeEditPath = new NodeEditPath();
			
			substitutionNodeEditPath.setFrom(fromNode);
			substitutionNodeEditPath.setTo(unmappedToNode);
			
			EditOperation substitution = new NodeSubstitution(fromNode, unmappedToNode, costContainer);
			substitutionNodeEditPath.addEditOperation(substitution);
			
			// Check the adjacent nodes of the source node. If the destination node does
			// not have a corresponding adjacent node then add edge deletion to the queue.
			for(DecoratedNode adjacentFrom : fromNode.getAdjacent()) {
				DecoratedNode adjacentTo = null;
				
				if(!adjacentFrom.equals(fromNode)) {
					// Edge to some other mapped source node.
					if(mappedFromNodes.contains(adjacentFrom)) {
						adjacentTo = substitutionEditPath.getMapped(adjacentFrom);
					}
				} else {
					// Edge to itself.
					adjacentTo = unmappedToNode;
				}
								
				if(adjacentTo != null && !unmappedToNode.isAdjacent(adjacentTo)) {
					EditOperation edgeDeletion = new EdgeDeletion(fromNode, adjacentFrom, costContainer);
					substitutionNodeEditPath.addEditOperation(edgeDeletion);
				}
			}
			
			// Check the adjacent nodes of the destination node. If the source node does
			// not have a corresponding adjacent node then add edge insertion to the queue.
			for(DecoratedNode adjacentTo : unmappedToNode.getAdjacent()) {
				DecoratedNode adjacentFrom = null;
				
				if(!adjacentTo.equals(unmappedToNode)) {
					// Edge to some other mapped destination node.
					if(mappedToNodes.contains(adjacentTo)) {
						adjacentFrom = substitutionEditPath.getMappedReverse(adjacentTo);
					}
				} else {
					// Edge to itself.
					adjacentFrom = fromNode;
				}
								
				if(adjacentFrom != null && !fromNode.isAdjacent(adjacentFrom)) {
					EditOperation edgeInsertion = new EdgeInsertion(unmappedToNode, adjacentTo, costContainer);
					substitutionNodeEditPath.addEditOperation(edgeInsertion);
				}
			}
			
			// In case of directed graph also check incoming edges.
			if(from.isDirected()) {
				// Check the nodes connected to the source node. If the destination node does
				// not have a corresponding node connected to it then add edge deletion to the queue.
				for(DecoratedNode accessedByFrom : fromNode.getAccessedBy()) {
					if(mappedFromNodes.contains(accessedByFrom)) {
						DecoratedNode accessedByTo = substitutionEditPath.getMapped(accessedByFrom);
						
						if(!unmappedToNode.isAccessedBy(accessedByTo)) {
							EditOperation edgeDeletion = new EdgeDeletion(accessedByFrom, fromNode, costContainer);
							substitutionNodeEditPath.addEditOperation(edgeDeletion);
						}
					}
				}
				
				// Check the nodes connected to the destination node. If the source node does
				// not have a corresponding node connected to it then add edge insertion to the queue.
				for(DecoratedNode accessedByTo : unmappedToNode.getAccessedBy()) {
					if(mappedToNodes.contains(accessedByTo)) {
						DecoratedNode accessedByFrom = substitutionEditPath.getMappedReverse(accessedByTo);
						
						if(!fromNode.isAccessedBy(accessedByFrom)) {
							EditOperation edgeInsertion = new EdgeInsertion(accessedByTo, unmappedToNode, costContainer);
							substitutionNodeEditPath.addEditOperation(edgeInsertion);
						}
					}
				}
			}
							
			substitutionEditPath.addNodeEditPath(substitutionNodeEditPath);
			queue.offer(substitutionEditPath);
		}
	}

	
	private static void processNodeDeletion(DecoratedGraph from, DecoratedGraph to, 
			DecoratedNode node, EditPath editPath,
			CostContainer costContainer, Queue<EditPath> queue) {
		
		// Create a copy of edit path.
		EditPath deletionEditPath = editPath.copy();
		
		// Add node deletion.
		NodeEditPath deletionNodeEditPath = new NodeEditPath();
		
		deletionNodeEditPath.setFrom(node);
		
		EditOperation deletion = new NodeDeletion(node, costContainer);
		deletionNodeEditPath.addEditOperation(deletion);
		
		// For all adjacent nodes add edge deletions.
		for(DecoratedNode adjucentNode : node.getAdjacent()) {
			EditOperation edgeDeletion = new EdgeDeletion(node, adjucentNode, costContainer);
			deletionNodeEditPath.addEditOperation(edgeDeletion);
		}
		
		// If the graph is directed then also add edge deletions for all incoming edges.
		if(from.isDirected()) {
			for(DecoratedNode accessedByNode : node.getAccessedBy()) {
				EditOperation edgeDeletion = new EdgeDeletion(accessedByNode, node, costContainer);
				deletionNodeEditPath.addEditOperation(edgeDeletion);
			}
		}
		
		deletionEditPath.addNodeEditPath(deletionNodeEditPath);
		queue.offer(deletionEditPath);
	}
	
	
	private static void processNodeInsertions(DecoratedGraph from, EditPath editPath,
			Collection<DecoratedNode> unmappedToNodes, CostContainer costContainer, 
			Queue<EditPath> queue) {
		
		// Create a copy of edit path.
		EditPath insertionPath = editPath.copy();
		
		// Add insertion for all remaining destination nodes.
		for(DecoratedNode unmappedToNode : unmappedToNodes) {
			// Add node insertion
			NodeEditPath insertionNodeEditPath = new NodeEditPath();
			
			insertionNodeEditPath.setTo(unmappedToNode);
			
			EditOperation insertion = new NodeInsertion(unmappedToNode, costContainer);
			insertionNodeEditPath.addEditOperation(insertion);
			
			insertionPath.addNodeEditPath(insertionNodeEditPath);
			
			// For all adjacent nodes add edge insertions.
			for(DecoratedNode adjucentNode : unmappedToNode.getAdjacent()) {
				EditOperation edgeInsertion = new EdgeInsertion(unmappedToNode, adjucentNode, costContainer);
				insertionNodeEditPath.addEditOperation(edgeInsertion);
			}
			
			// If the graph is directed then also add edge insertions for all incoming edges.
			if(from.isDirected()) {
				for(DecoratedNode accessedByNode : unmappedToNode.getAccessedBy()) {
					EditOperation edgeInsertion = new EdgeInsertion(accessedByNode, unmappedToNode, costContainer);
					insertionNodeEditPath.addEditOperation(edgeInsertion);
				}
			}
		}
		
		insertionPath.setComplete(true);
		queue.offer(insertionPath);
	}

}
