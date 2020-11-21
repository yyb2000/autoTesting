import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 将选择出的测试用例集合存储到指定文件
 */
public class output {
    private final HashSet<String> testMethods = new HashSet<String>();  //用于存放所有的test方法
    private final HashMap<String, List<String>> classDependency =  new HashMap<>(); //用于存放class粒度的依赖，key为类名，value为直接依赖此类的类名
    private final HashMap<String, List<String>> methodDependency = new HashMap<>(); //用于存放method粒度的依赖，key为方法名，value为直接依赖此方法的方法名

    public void storemethod(HashSet<String> methodSelected, String filename) {
        try {
            File file = new File(filename);
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (String s : methodSelected) {
                if (testMethods.contains(s)) {
                    bufferedWriter.write(s + "\n");
                }
            }
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeClassDot(String filename) {
        try {
            File file = new File(filename);
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("digraph dependencies {\n");
            for (String string : classDependency.keySet()) {
                for (String s : classDependency.get(string)) {
                    bufferedWriter.write("\t");
                    bufferedWriter.write("\"" + string + "\" -> \"" + s + "\";\n");
                }
            }
            bufferedWriter.write("}");
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void storeMethodDot(String filename) {
        try {
            File file = new File(filename);
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write("digraph dependencies {\n");
            for (String string : methodDependency.keySet()) {
                for (String s : methodDependency.get(string)) {
                    bufferedWriter.write("\t");
                    bufferedWriter.write("\"" + string + "\" -> \"" + s + "\";\n");
                }
            }
            bufferedWriter.write("}");
            bufferedWriter.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
