import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class testSelection {
    
    //配置文件声明
    private final String targetPath;
    private final String changePath;
    private final String scopePath = "D:\\自动化测试\\autoTesting\\Project\\src\\main\\resources\\scope.txt";
    private final String exclusionPath = "D:\\自动化测试\\autoTesting\\Project\\src\\main\\resources\\exclusion.txt";

    //存放说明
    private final HashSet<String> classesFind = new HashSet<String>();  //用于存放所有检测到的类
    private final HashSet<String> methodsFind = new HashSet<String>();  //用于存放所有检测到的方法
    private final HashSet<String> testMethods = new HashSet<String>();  //用于存放所有的test方法
    private final ArrayList<String> changeInfo = new ArrayList<String>();   //用于存放记录变更信息
    private final HashMap<String, List<String>> methodsCallRelaionship = new HashMap<String, List<String>>();   //用于存放方法间的调用关系，其中key为一个方法，value为所有直接调用了这个方法的其他方法
    private final HashMap<String, List<String>> classDependency =  new HashMap<>(); //用于存放class粒度的依赖，key为类名，value为直接依赖此类的类名
    private final HashMap<String, List<String>> methodDependency = new HashMap<>(); //用于存放method粒度的依赖，key为方法名，value为直接依赖此方法的方法名

    /**
     * @param targetPath 待分析文件路径
     * @param changePath 信息变更文件路径
     */
    public testSelection (String targetPath, String changePath) {
        this.targetPath = targetPath;
        this.changePath = changePath;
    }

    relationBuild rb = new relationBuild();
    classType ct = new classType();
    methodType mt = new methodType();

    /**
     * 获取所有类
     */
    public void getClasses(CHACallGraph cg) {
        for (CGNode cgnode: cg) {
            if (cgnode.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) cgnode.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String innerClass = method.getDeclaringClass().getName().toString();
                    this.classesFind.add(innerClass.split("\\$")[0]);
                }
            }
        }
    }

    public void selecter(char selectOption) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        String sourcedirPath = this.targetPath + "\\classes\\net\\mooctest";
        String testdirPath = this.targetPath + "\\test-classes\\net\\mooctest";

        File soucedir = new File(sourcedirPath);
        File testdir = new File(testdirPath);
        File[] sourceFiles = soucedir.listFiles();
        File[] testFiles = testdir.listFiles();

        ClassLoader classLoader = testSelection.class.getClassLoader();

        AnalysisScope analysisScope = AnalysisScopeReader.readJavaScope(this.scopePath, new File(this.exclusionPath), classLoader);

        if (sourceFiles != null) {
            for (File file : sourceFiles) {
                analysisScope.addClassFileToScope(ClassLoaderReference.Application, file);
            }
        }
        if (testFiles != null) {
            for (File file : testFiles) {
                analysisScope.addClassFileToScope(ClassLoaderReference.Application, file);
            }
        }

        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(analysisScope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(analysisScope, cha);
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);

        this.getClasses(cg);
        rb.buildCallRelationship(cg);

        try {
            BufferedReader in = new BufferedReader(new FileReader(this.changePath));
            String line;
            while ((line = in.readLine()) != null) {
                this.changeInfo.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if ( selectOption == 'c') {
            ct.classSelection();
        }
        else if (selectOption == 'm') {
            mt.methodSelection();
        }
        else {
            System.err.println("Unknown select option" + selectOption);
        }

        ct.getClassDependency();
        mt.getMethodDependency();
    }

    public static void main(String[] args) throws IOException, InvalidClassFileException, ClassHierarchyException, CancelException {
        testSelection testsel = new testSelection(args[1], args[2]);
        testsel.selecter(args[0].charAt(1));
    }
}