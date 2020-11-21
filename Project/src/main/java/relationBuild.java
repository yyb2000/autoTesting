import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.annotations.Annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

public class relationBuild {
    private final HashSet<String> testMethods = new HashSet<String>();  //用于存放所有的test方法
    private final HashMap<String, List<String>> methodsCallRelationship = new HashMap<String, List<String>>();   //用于存放方法间的调用关系，其中key为一个方法，value为所有直接调用了这个方法的其他方法
    private final HashSet<String> methodsFind = new HashSet<String>();  //用于存放所有检测到的方法


    public void buildCallRelationship(CHACallGraph cg) throws InvalidClassFileException {
        for (CGNode cgNode : cg) {
            if (cgNode.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) cgNode.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String innerName = method.getDeclaringClass().getName().toString();
                    String sign = method.getSignature();
                    String caller = innerName + " " + sign;

                    String pattern = "Annotation type <Application,Lorg/junit/Test>.*";
                    for (Annotation annotation : method.getAnnotations()) {
                        if (Pattern.matches(pattern, annotation.toString())) {
                            testMethods.add(caller);
                            break;
                        }
                    }

                    for (CallSiteReference callSiteReference : method.getCallSites()) {
                        String classCalled = callSiteReference.getDeclaredTarget().toString().replace(" ","").split(",")[1].split("\\$")[0];
                        String methodCalled = callSiteReference.getDeclaredTarget().getSignature();
                        String callee = classCalled + " " + methodCalled;

                        if (methodsCallRelationship.containsKey(callee)) {
                            if (!methodsCallRelationship.get(callee).contains(caller)) {
                                methodsCallRelationship.get(callee).add(caller);
                            }
                        }
                        else {
                            List<String> list = new ArrayList<>();
                            list.add(caller);
                            methodsCallRelationship.put(callee, list);
                        }
                    }
                }
            }
        }
        for (String callee : methodsCallRelationship.keySet()) {
            methodsFind.add(callee);
            methodsFind.addAll(methodsCallRelationship.get(callee));
        }
    }
}
