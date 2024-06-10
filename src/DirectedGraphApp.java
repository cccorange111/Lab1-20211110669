import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DirectedGraphApp {
    private static HashMap<String, HashMap<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) throws FileNotFoundException {
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
            System.out.print("Your choice is : ");
            int choice = scanner.nextInt();
            scanner.nextLine();
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
    static void buildGraph(String filePath) throws FileNotFoundException {
        Scanner fileScanner = new Scanner(new File(filePath));
        String previousWord = null;
        //遍历所有单词
        while (fileScanner.hasNext()) {
            //格式化
            String currentWord = fileScanner.next().replaceAll("[^a-zA-Z]", "").toLowerCase();
            if (!currentWord.isEmpty()) {
                if (previousWord != null) {
                    graph.putIfAbsent(previousWord, new HashMap<>());//没有变则添加边
                    graph.get(previousWord).merge(currentWord, 1, Integer::sum);//有这条边，增加权重
                }
                previousWord = currentWord;
            }
        }
        fileScanner.close();
    }

    //展示
    public static void showDirectedGraph() {
        for (String key : graph.keySet()) {
            System.out.println("Word: " + key);
            graph.get(key).forEach((word, count) -> System.out.println(" -> " + word + " (weight: " + count + ")"));
        }
    }

    //查找桥接词
    public static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }
        ArrayList<String> bridges = new ArrayList<>();
        //遍历 word1 的所有邻接单词，对于每个邻接单词 middle，检查它是否也连接到 word2
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

    //根据bridge word生成新文本
    public static String generateNewText(String inputText) {
        String[] words = inputText.split("\\s+");
        StringBuilder newText = new StringBuilder();
        //两两单词，查询桥接词
        for (int i = 0; i < words.length - 1; i++) {
            newText.append(words[i]);
            String bridge = queryBridgeWords(words[i], words[i + 1]);
            if (bridge.startsWith("The bridge words")) {
                //随机选择一个桥接词加入
                String[] parts = bridge.split(": ")[1].split(", ");
                newText.append(" ").append(parts[new Random().nextInt(parts.length)]).append(" ");
            } else {
                newText.append(" ");
            }
        }
        //加上最后一个单词
        newText.append(words[words.length - 1]);
        return newText.toString();
    }

    //使用迪杰斯特拉算法计算最短路径
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

        Map<String, Integer> distances = new HashMap<>();//距离
        Map<String, String> predecessors = new HashMap<>();//前驱
        for (String vertex : vertices) {
            distances.put(vertex, Integer.MAX_VALUE);
            predecessors.put(vertex, null);
        }
        //init
        distances.put(word1, 0);
        PriorityQueue<Map.Entry<String, Integer>> queue = new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        //word1 0 加入队列
        queue.add(new AbstractMap.SimpleEntry<>(word1, 0));

        while (!queue.isEmpty()) {
            //最短路径出队列
            String current = queue.poll().getKey();
            int currentDistance = distances.get(current);
            //抵达
            if (current.equals(word2)) {
                break;
            }
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors != null) {
                //遍历所有的邻居
                for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                    String neighborKey = neighbor.getKey();
                    int distanceThroughU = currentDistance + neighbor.getValue();
                    if (distanceThroughU < distances.get(neighborKey)) {
                        //距离更小则更新最短距离和前驱
                        distances.put(neighborKey, distanceThroughU);
                        predecessors.put(neighborKey, current);
                        queue.add(new AbstractMap.SimpleEntry<>(neighborKey, distanceThroughU));
                    }
                }
            }
        }
        //没有路径
        if (distances.get(word2) == Integer.MAX_VALUE) {
            return "No path between " + word1 + " and " + word2;
        }
        //逆推路径
        List<String> path = new ArrayList<>();
        for (String at = word2; at != null; at = predecessors.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);
        return "Shortest path (" + distances.get(word2) + "): " + String.join(" -> ", path);
    }

    //随机游走
    public static String randomWalk() {
        Scanner scanner = new Scanner(System.in);
        AtomicBoolean pause = new AtomicBoolean(false);
        Random random = new Random();
        List<String> keys = new ArrayList<>(graph.keySet());
        //随机选择起点
        String current = keys.get(random.nextInt(keys.size()));
        Set<String> visited = new HashSet<>();
        StringBuilder result = new StringBuilder(current);
        new Thread(() -> {
            while (true) {
                System.out.println("Enter 's' to stop:");
                String command = scanner.nextLine().trim().toLowerCase();
                switch (command) {
                    case "s":
                        System.out.println("Random walk: " + result.toString());
                        System.exit(0);
                        break;
                }
            }
        }).start();


        //循环直到当前顶点没有更多的出边或已访问的边被再次访问
        while (graph.get(current) != null && !graph.get(current).isEmpty()) {
            while (pause.get()) {
                synchronized (pause) {
                    try {
                        pause.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            List<String> possible = new ArrayList<>(graph.get(current).keySet());
            String next = possible.get(random.nextInt(possible.size()));
            String edge = current + " " + next;
            if (visited.contains(edge)) {
                break;
            }
            visited.add(edge);
            result.append(" -> ").append(next);
            current = next;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        WriteFileCon("testout.txt", result.toString());
        return "Random walk: " + result.toString();
    }

    private static void WriteFileCon(String filePathName, String str) {
        try (FileWriter fw = new FileWriter(filePathName);
             BufferedWriter info = new BufferedWriter(fw)) {
            info.write(String.format(str)); // 加个 %n 相当于换行
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("写入失败");
        }
    }
}