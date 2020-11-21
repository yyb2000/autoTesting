import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class methodType {
    private final ArrayList<String> changeInfo = new ArrayList<String>();
    private final HashSet<String> methodsFind = new HashSet<String>();
    private final HashMap<String, List<String>> methodsCallRelaionship = new HashMap<String, List<String>>();
    private final HashSet<String> classesFind = new HashSet<String>();
    private final HashMap<String, List<String>> methodDependency = new HashMap<>(); //用于存放method粒度的依赖，key为方法名，value为直接依赖此方法的方法名

    output op = new output();

    public void methodSelection() {
        HashSet<String> methodsSelected = new HashSet<String>();
        for (String s : changeInfo) {
            HashSet<String> methodRelated = new HashSet<String>();
            methodsSelectionAll(methodRelated, s);
            methodsSelected.addAll(methodRelated);
        }
        op.storemethod(methodsSelected, "./selection-method.txt");
    }

    public void methodsSelectionAll(HashSet<String> methodsRelated, String methodToSearch) {
        HashSet<String> newMethod = new HashSet<String>();
        if (!this.methodsCallRelaionship.containsKey(methodToSearch))
            return;
        for (String s : this.methodsCallRelaionship.get(methodToSearch)) {
            if (!methodsRelated.contains(s)) {
                methodsRelated.add(s);
                newMethod.add(s);
            }
        }
        for (String s : newMethod) {
            methodsSelectionAll(methodsRelated, s);
        }
    }

    public void getMethodDependency() {
        for (String s : methodsCallRelaionship.keySet()) {
            String callee = s.split(" ")[1];
            if (!classesFind.contains(s.split(" ")[0]))
                continue;
            if (!methodDependency.containsKey(callee)) {
                List<String> list = new ArrayList<>();
                methodDependency.put(callee, list);
            }
            for (String string : methodsCallRelaionship.get(s)) {
                String caller = string.split(" ")[1];
                if (!methodDependency.get(callee).contains(caller)) {
                    if (!classesFind.contains(string.split(" ")[0]))
                        continue;
                    methodDependency.get(callee).add(caller);
                }
            }
        }
        op.storeMethodDot("./method-dependencies.dot");
    }
}
