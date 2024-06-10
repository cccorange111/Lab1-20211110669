import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DirectedGraphAppTest {
    public DirectedGraphApp graph;

    @BeforeEach
    void setUp() throws FileNotFoundException {
        // 初始化图并添加测试所需的单词和连接
        HashMap<String, HashMap<String, Integer>> graph = new HashMap<>();
        String filePath = "test.txt";
        DirectedGraphApp.buildGraph(filePath);
    }

    @Test
    void testQueryBridgeWords_Valid() {
        // 测试存在桥接词的情况
        String result = graph.queryBridgeWords("explore", "new");
        assertEquals("The bridge words from explore to new are: strange", result);
    }

    @Test
    void testQueryBridgeWords_FirstWordEmpty() {
        // 测试第一个单词为空的情况
        String result = graph.queryBridgeWords(" ", "new");
        assertEquals("No   or new in the graph!", result);
    }

    @Test
    void testQueryBridgeWords_WordNotInGraph() {
        // 测试单词不在图中的情况
        String result = graph.queryBridgeWords("hello", "new");
        assertEquals("No hello or new in the graph!", result);
    }
}
