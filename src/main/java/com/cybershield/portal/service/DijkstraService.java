package com.cybershield.portal.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DijkstraService {

    public static class PathResult {
        private final List<String> path;
        private final int totalWeight;

        public PathResult(List<String> path, int totalWeight) {
            this.path = path;
            this.totalWeight = totalWeight;
        }

        public List<String> getPath() {
            return path;
        }

        public int getTotalWeight() {
            return totalWeight;
        }
    }

    /**
     * Finds the shortest path in the default simulation network topology.
     * Nodes: A, B, C, D, E
     * Default edges:
     * A - B: 2
     * A - C: 5
     * B - D: 1
     * C - D: 4
     * B - C: 2
     * D - E: 3
     * C - E: 6
     */
    public PathResult findShortestPath(String start, String end) {
        // Build default graph
        Map<String, Map<String, Integer>> graph = new HashMap<>();
        graph.put("Node A", Map.of("Node B", 2, "Node C", 5));
        graph.put("Node B", Map.of("Node A", 2, "Node D", 1, "Node C", 2));
        graph.put("Node C", Map.of("Node A", 5, "Node B", 2, "Node D", 4, "Node E", 6));
        graph.put("Node D", Map.of("Node B", 1, "Node C", 4, "Node E", 3));
        graph.put("Node E", Map.of("Node D", 3, "Node C", 6));

        // Short Names mapping support (A instead of Node A)
        String resolvedStart = start.startsWith("Node ") ? start : "Node " + start;
        String resolvedEnd = end.startsWith("Node ") ? end : "Node " + end;

        if (!graph.containsKey(resolvedStart) || !graph.containsKey(resolvedEnd)) {
            return new PathResult(Collections.emptyList(), 0);
        }

        // Dijkstra's Algorithm
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>(Comparator.comparingInt(nd -> nd.distance));

        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(resolvedStart, 0);
        queue.add(new NodeDistance(resolvedStart, 0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();
            String u = current.node;
            int distU = current.distance;

            if (distU > distances.get(u)) continue;
            if (u.equals(resolvedEnd)) break;

            Map<String, Integer> neighbors = graph.get(u);
            if (neighbors != null) {
                for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                    String v = neighbor.getKey();
                    int weight = neighbor.getValue();
                    int alt = distU + weight;

                    if (alt < distances.get(v)) {
                        distances.put(v, alt);
                        predecessors.put(v, u);
                        queue.add(new NodeDistance(v, alt));
                    }
                }
            }
        }

        // Reconstruct path
        List<String> path = new ArrayList<>();
        String step = resolvedEnd;
        if (distances.get(resolvedEnd) == Integer.MAX_VALUE) {
            return new PathResult(Collections.emptyList(), 0);
        }

        path.add(step);
        while (predecessors.containsKey(step)) {
            step = predecessors.get(step);
            path.add(0, step);
        }

        return new PathResult(path, distances.get(resolvedEnd));
    }

    private static class NodeDistance {
        String node;
        int distance;

        NodeDistance(String node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}
