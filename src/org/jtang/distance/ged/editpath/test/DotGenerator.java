package org.jtang.distance.ged.editpath.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DotGenerator {
	
	private static final char[] ALPHABET = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
		'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 
		'x', 'y', 'z'};

	public static String generate(int nodeCount, int egdeCount,
			int maxLabelLength, String graphName, boolean directed) {
		
		if(maxLabelLength > ALPHABET.length) {
			throw new IllegalArgumentException("Max label length is bigger than " + 
					ALPHABET.length);
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(directed ? "digraph" : "graph").
			append(" ").append(graphName).append(" {\n");
		
		List<String> nodes = new ArrayList<String>(nodeCount);
		
		Random random = new Random();
		
		for(int i = 0; i < nodeCount; i++) {
			int nodeLabelLength = random.nextInt(maxLabelLength) + 1;
			
			char[] chars = new char[nodeLabelLength];
			for(int j = 0; j < nodeLabelLength; j++) {
				chars[j] = ALPHABET[random.nextInt(ALPHABET.length)];
			}
			
			String nodeLabel = String.valueOf(chars);
			sb.append(nodeLabel).append(";\n");
			nodes.add(nodeLabel);
		}
		
		for(int k = 0; k < egdeCount; k++) {
			sb.append(nodes.get(random.nextInt(nodeCount))).
				append(" ").append(directed ? "->" : "--").
				append(" ").append(nodes.get(random.nextInt(nodeCount))).
				append(";\n");
		}
		
		sb.append("}");
		
		return sb.toString();
	}
}
