import java.util.*;
import java.io.*;

public class DirectedGraphApp {
    private static HashMap<String, HashMap<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
        // Automatically set the file path to "test.txt" in the same directory
        String filePath = "test.txt";
        buildGraph(filePath);

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;
        while (!exit) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Show directed graph");
            System.out.println("2. Query bridge words");
            System.out.println("3. Generate new text");
            System.out.println("4. Calculate shortest path");
            System.out.println("5. Random walk");
            System.out.println("0. Exit");
            System.out.print("Your choice : ");

            int choice = scanner.nextInt();
            scanner.nextLine();  // consume the remaining newline

            switch (choice) {
                case 1:
                    showDirectedGraph();
                    break;
                case 2:
                    System.out.println("Enter two words (e.g., 'word1 word2'):");
                    String[] words = scanner.nextLine().split(" ");
                    System.out.println(queryBridgeWords(words[0], words[1]));
                    break;
                case 3:
                    System.out.println("Enter a text to transform:");
                    String inputText = scanner.nextLine();
                    System.out.println(generateNewText(inputText));
                    break;
                case 4:
                    System.out.println("Enter two words to find the shortest path (e.g., 'word1 word2'):");
                    words = scanner.nextLine().split(" ");
                    System.out.println(calcShortestPath(words[0], words[1]));
                    break;
                case 5:
                    System.out.println(randomWalk());
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number between 0 and 5.");
            }
        }
        scanner.close();
    }
    //创建图
    private static void buildGraph(String filePath) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filePath));
        String previousWord = null;
        while (fileScanner.hasNext()) {
            String currentWord = fileScanner.next().replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (!currentWord.isEmpty()) {
                if (previousWord != null) {
                    graph.putIfAbsent(previousWord, new HashMap<>());
                    graph.get(previousWord).merge(currentWord, 1, Integer::sum);
                }
                previousWord = currentWord;
            }
        }
        fileScanner.close();
    }

    public static void showDirectedGraph() {
        for (String key : graph.keySet()) {
            System.out.println("Word: " + key);
            graph.get(key).forEach((word, count) -> System.out.println(" -> " + word + " (weight: " + count + ")"));
        }
    }

    public static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        ArrayList<String> bridges = new ArrayList<>();
        if (graph.containsKey(word1)) {
            for (String middle : graph.get(word1).keySet()) {
                if (graph.containsKey(middle) && graph.get(middle).containsKey(word2)) {
                    bridges.add(middle);
                }
            }
        }
        if (bridges.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        }
        return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridges);
    }

    public static String generateNewText(String inputText) {
        String[] words = inputText.split("\\s+");
        StringBuilder newText = new StringBuilder();
        for (int i = 0; i < words.length - 1; i++) {
            newText.append(words[i]);
            String bridge = queryBridgeWords(words[i], words[i+1]);
            if (bridge.startsWith("The bridge words")) {
                String[] parts = bridge.split(": ")[1].split(", ");
                newText.append(" ").append(parts[new Random().nextInt(parts.length)]).append(" ");
            } else {
                newText.append(" ");
            }
        }
        newText.append(words[words.length - 1]);
        return newText.toString();
    }

    public static String calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No path between " + word1 + " and " + word2;
        }

        // 使用一个集合来存储所有顶点，确保距离和前驱映射包含图中的所有顶点
        Set<String> vertices = new HashSet<>();
        graph.forEach((key, value) -> {
            vertices.add(key);
            vertices.addAll(value.keySet());
        });

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        for (String vertex : vertices) {
            distances.put(vertex, Integer.MAX_VALUE);
            predecessors.put(vertex, null);
        }

        distances.put(word1, 0);
        PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        queue.add(new AbstractMap.SimpleEntry<>(word1, 0));

        while (!queue.isEmpty()) {
            String current = queue.poll().getKey();
            int currentDistance = distances.get(current);

            if (current.equals(word2)) {
                break;
            }

            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors != null) {
                for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                    String neighborKey = neighbor.getKey();
                    int distanceThroughU = currentDistance + neighbor.getValue();
                    if (distanceThroughU < distances.get(neighborKey)) {
                        distances.put(neighborKey, distanceThroughU);
                        predecessors.put(neighborKey, current);
                        queue.add(new AbstractMap.SimpleEntry<>(neighborKey, distanceThroughU));
                    }
                }
            }
        }

        if (distances.get(word2) == Integer.MAX_VALUE) {
            return "No path between " + word1 + " and " + word2;
        }

        List<String> path = new ArrayList<>();
        for (String at = word2; at != null; at = predecessors.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return "Shortest path (" + distances.get(word2) + "): " + String.join(" -> ", path);
    }


    public static String randomWalk() {
        Random random = new Random();
        List<String> keys = new ArrayList<>(graph.keySet());
        String current = keys.get(random.nextInt(keys.size()));
        Set<String> visited = new HashSet<>();
        StringBuilder result = new StringBuilder(current);

        while (graph.get(current) != null && !graph.get(current).isEmpty()) {
            List<String> possible = new ArrayList<>(graph.get(current).keySet());
            String next = possible.get(random.nextInt(possible.size()));
            String edge = current + " " + next;
            if (visited.contains(edge)) {
                break;
            }
            visited.add(edge);
            result.append(" -> ").append(next);
            current = next;
        }

        return "Random walk: " + result.toString();
    }
}
