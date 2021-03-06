import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class classType {
    private final ArrayList<String> changeInfo = new ArrayList<String>();   //用于存放记录变更信息
    private final HashSet<String> methodsFind = new HashSet<String>();  //用于存放所有检测到的方法
    private final HashMap<String, List<String>> methodsCallRelationship = new HashMap<String, List<String>>();   //用于存放方法间的调用关系，其中key为一个方法，value为所有直接调用了这个方法的其他方法
    private final HashSet<String> classesFind = new HashSet<String>();
    private final HashMap<String, List<String>> classDependency =  new HashMap<>(); //用于存放class粒度的依赖，key为类名，value为直接依赖此类的类名

    output op = new output();
    /**
     * 找到所有关联class，从methodsFind中找到对应的方法
     */
    public void classSelection() {
        HashSet<String> classRelated = new HashSet<String>();
        HashSet<String> methodsSelected = new HashSet<String>();
        for (String s : changeInfo) {
            String classToSearch = s.split(" ")[0];
            classRelated.add(classToSearch);
            classSelectionAll(classRelated, classToSearch);
        }

        for (String s : methodsFind) {
            if (classRelated.contains(s.split(" ")[0])) {
                methodsSelected.add(s);
            }
        }

        op.storemethod(methodsSelected, "./selection-class.txt");
    }

    /**
     * 记录所有依赖了classToSearch的class，并add进classRelated中
     */
    public void classSelectionAll(HashSet<String> classRelated, String classToSearch) {
        for (String callee : methodsCallRelationship.keySet()) {
            if (callee.split(" ")[0].equals(classToSearch)) {
                for (String caller : methodsCallRelationship.get(callee)) {
                    classRelated.add(caller.split(" ")[0]);
                    if (classRelated.contains(caller.split(" ")[0]))
                        continue;
                    //递归检查依赖caller的类，以实现寻找间接依赖classToSearch的类
                    classSelectionAll(classRelated, caller.split(" ")[0]);
                }
            }
        }
    }

    public void getClassDependency() {
        for (String s : methodsCallRelationship.keySet()) {
            String callee = s.split(" ")[0];
            if (!classesFind.contains(callee))
                continue;
            if (!classDependency.containsKey(callee)) {
                List<String> list = new ArrayList<>();
                classDependency.put(callee, list);
            }
            for (String string : methodsCallRelationship.get(s)) {
                String caller = string.split(" ")[0];
                if (!classDependency.get(callee).contains(caller)) {
                    if (classesFind.contains(caller))
                        continue;
                    classDependency.get(callee).add(caller);
                }
            }
        }
        op.storeClassDot("./class-dependencies.dot");
    }
}
