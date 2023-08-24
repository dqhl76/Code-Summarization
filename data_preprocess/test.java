import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.modules.ModuleDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.utils.ParserCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class test {

    static int method_index;
    static int raw_method_index;
    static int class_index;
    static ArrayList classlist;
    static ArrayList methodlist;
    static ArrayList m2mlist;
    static ArrayList m2clist;
    static ArrayList m2mbyclist;

    static String methodname;

    static String name = "test";
    static String path_from = "/Users/liutianhao/IdeaProjects/"+name;
    static String path_to = "/Users/liutianhao/IdeaProjects/test/src/"+name;

    public static void parseProject(String path) {
        Path root = Paths.get(path);
        // only parsing
        ProjectRoot projectRoot = new ParserCollectionStrategy().collect(root);
        projectRoot.getSourceRoots().forEach(sourceRoot -> {
            System.out.println(sourceRoot);
            try {
                // 解析source root
                sourceRoot.tryToParse();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 获取解析后的编译单元列表
            List<CompilationUnit> cuList = sourceRoot.getCompilationUnits();
            for(CompilationUnit cu:cuList){

                parseOneFile(cu);
            }
            //cuList.forEach(test::parseOneFile);
        });
    }

    /**
     * 解析单个Java文件
     *
     * @param cu 编译单元
     */
    public static void parseOneFile(CompilationUnit cu) {
        // 类型声明
        NodeList<TypeDeclaration<?>> types = cu.getTypes();
        for (TypeDeclaration<?> type : types) {

            System.out.println(type.getName());
            HashMap classmap = new HashMap();
            classmap.put("id",class_index);
            classmap.put("name",type.getName());
//                System.out.println();
            classlist.add(classmap);
            class_index+=1;
            //System.out.println("## " + type.getName());
            // 成员
            NodeList<BodyDeclaration<?>> members = type.getMembers();



            for(Node n:members){
//                System.out.println("root:  "+n.findRootNode());
                processNode(n,class_index-1);
            }

            //members.forEach(test::processNode);
        }
    }

    /**
     * 处理类型,方法,成员
     *
     * @param node
     */
    public static void processNode(Node node,int class_index) {
        HashMap methodmap = new HashMap();
        HashMap contentmap = new HashMap();
        if (node instanceof TypeDeclaration) {
            // 类型声明
            // do something with this type declaration
            //System.out.println("## " + ((TypeDeclaration<?>) node).getName().getIdentifier());
        } else if (node instanceof MethodDeclaration) {
            // 方法声明
            // do something with this method declaration



            List<Comment> comments = node.getAllContainedComments();
            //contentmap.put("comment",comment);
            String comment  = "";
            List<Comment> blockComments = new ArrayList<>();
            //if(methodName.equals("addPerson")) {
            for (Comment c : comments) {
                if (c != null) {
                    if ((c instanceof BlockComment)) {
                        blockComments.add(c);
                    }
                }
            }
            // System.out.println(blockComments.get(0));
            if(blockComments.size()!=0){
                comment = String.valueOf(blockComments.get(0));
            }
            Comment widecomment = node.getComment().orElse(null);
            if(widecomment!=null){
                comment = widecomment.getContent();
            }

            //System.out.println(widecomment.getContent());
            //}
            raw_method_index+=1;
            if(!comment.equals("")){
                String methodName = ((MethodDeclaration) node).getName().getIdentifier();
                methodname = methodName;
                // System.out.println("Method: " + methodName);

                methodmap.put("id",method_index);
                methodmap.put("name",methodName);
                //if(methodName.equals("addPerson")) { methodmap.put("content",contentmap);}
                methodmap.put("content",contentmap);
                node = node.removeComment();
                String nodeStr = node.clone().toString();
                nodeStr = nodeStr.replace("\n","");
                nodeStr = nodeStr.replace("\r","");
                contentmap.put("method",nodeStr);

                comment = comment.replace("\n","");
                comment = comment.replace("\t","");
                comment = comment.replace("*","");
                comment = comment.replace("@param","");
                comment = comment.replace("@","");
                comment = comment.replace("  ","");
                comment = comment.replace("   ","");
                comment = comment.replace("    ","");


                contentmap.put("comment",comment);
                method_index+=1;
                methodlist.add(methodmap);
                //if(methodName.equals("findAddressesOfPersons")){
                //System.out.println(((MethodDeclaration) node).getName());
                //System.out.println( ((MethodDeclaration) node).getBody());
                //System.out.println(node.getChildNodes());
                //if(methodName.equals("addPerson")) {

                node.accept(new MethodCallVisitor(methodName,class_index), null);
            }



            // }


            //}


        } else if (node instanceof FieldDeclaration) {
//            // 成员变量声明
//            // do something with this field declaration
//            // 注释
//            Comment comment = node.getComment().orElse(null);
//
//            // 变量
//            NodeList<VariableDeclarator> variables = ((FieldDeclaration) node).getVariables();
//            SimpleName fieldName = variables.get(0).getName();
//            String con = "";
//            if (comment != null) {
//
//                //System.out.print(handleComment(comment.getContent()));
//                //System.out.print(1);
//                String content = comment.getContent();
//                con = content.replace("*", "").trim();
//                //System.out.println(con);
//            }
//            contentmap.put("comment",con);
//            //System.out.print("\t");
//            //System.out.print(fieldName);
//            //System.out.println();
        }

        for (Node child : node.getChildNodes()) {
            processNode(child,class_index);
        }
    }


    //static int mode;
//    public static void changemode(int x){
//        mode = x;
//    }
    public static void Parse(String path){
        class_index = 1;
        method_index = 1;
        classlist = new ArrayList<HashMap>();
        methodlist = new ArrayList<HashMap>();
        m2mlist = new ArrayList();
        m2clist = new ArrayList();
        m2mbyclist = new ArrayList();
        parseProject(path);

        //parseProject("src/main/java/",1);
        //System.out.println(mode);
        HashMap map = new HashMap();
        HashMap m2m = new HashMap();
        HashMap m2c = new HashMap();

        ArrayList mtype = new ArrayList<>();
        mtype.add("method");
        mtype.add("method");
        m2m.put("type",mtype);
        ArrayList ctype = new ArrayList<>();
        ctype.add("method");
        ctype.add("class");
        m2c.put("type",ctype);

        HashMap nodemap = new HashMap();
        nodemap.put("class",classlist);
        nodemap.put("method",methodlist);

        HashMap edgemap = new HashMap();
        m2m.put("content",m2mlist);
        m2c.put("content",m2clist);




        Integer[][] connection_m2m = new Integer[m2mlist.size()][2];
        for (int i = 0; i < m2mlist.size(); i++) {
            ArrayList<Integer> m2m_entry = (ArrayList<Integer>) m2mlist.get(i);
            connection_m2m[i][0] = m2m_entry.get(0);
            connection_m2m[i][1] = m2m_entry.get(1);
        }
        saveAsFileWriter(Arrays.deepToString(connection_m2m), "m2m.txt");

        Integer[][] connection_m2c = new Integer[m2clist.size()][2];
        for (int i = 0; i < m2clist.size(); i++) {
            ArrayList<Integer> m2c_entry = (ArrayList<Integer>) m2clist.get(i);
            connection_m2c[i][0] = m2c_entry.get(0);
            connection_m2c[i][1] = m2c_entry.get(1);
        }
        saveAsFileWriter(Arrays.deepToString(connection_m2c), "m2c.txt");

        //m2clist
        HashMap map1 = new HashMap<>();
        for(Object list:m2clist){
            ArrayList<Integer> list1 = (ArrayList) list;
            if(!map1.containsKey(list1.get(0))){
                ArrayList l = new ArrayList<>();
                l.add(list1.get(1));
                map1.put(list1.get(0),l);
            }else{
                ArrayList l = (ArrayList) map1.get(list1.get(0));
                l.add(list1.get(1));
                map1.put(list1.get(0),l);
            }
        }
//        System.out.println(map1.toString());

        for (Object i:map1.keySet()){
            int i1 = (int) i;
            ArrayList<Integer> list = (ArrayList) map1.get(i1);
            for (int a : list){
                for (int b:list){
                    if(a!=b){
                        ArrayList list2 = new ArrayList<>();
                        list2.add(a);
                        list2.add(b);
                        m2mbyclist.add(list2);
                    }
                }
            }
        }
//        System.out.println(m2mbyclist.toString());
        Integer[][] connection_m2mc = new Integer[m2mbyclist.size()][2];
        for (int i = 0; i < m2mbyclist.size(); i++) {
            ArrayList<Integer> m2mc_entry = (ArrayList<Integer>) m2mbyclist.get(i);
            connection_m2mc[i][0] = m2mc_entry.get(0);
            connection_m2mc[i][1] = m2mc_entry.get(1);
        }
        saveAsFileWriter(Arrays.deepToString(connection_m2mc), "m2mbyc.txt");

        HashMap<String, Object> methods = new HashMap<>();
        for (int i = 0; i < methodlist.size(); i++) {
            HashMap method = (HashMap) methodlist.get(i);
            HashMap mixedContent = (HashMap) method.get("content");

            String method_name = method.get("name").toString();
            String method_content = mixedContent.get("method").toString();
            String method_comment = mixedContent.get("comment").toString();

            Map<String, String> method_info = new HashMap<>();
            method_info.put("name", method_name);
            method_info.put("content", method_content);
            method_info.put("comment", method_comment);

            methods.put(method.get("id").toString(), method_info);
        }

        HashMap<String, Object> classes = new HashMap<>();
        for (int i = 0; i < classlist.size(); i++) {
            HashMap clas = (HashMap) classlist.get(i);
            classes.put(clas.get("id").toString(), clas.get("name").toString());
        }
        writeJsonFile(new JSONObject(methods), "node-method");
        writeJsonFile(new JSONObject(classes), "node-class");
        System.out.println("Total: "+raw_method_index);
        System.out.println("Selected: "+methods.size());

    }

    public static void main(String[] args) throws IOException {
        File file = new File(path_to);
        if (!file.exists()){
            file.mkdir();
        }
        Parse(path_from);
//        parseProject(path_from);
    }
    public static void writeJsonFile(JSONObject jobj, String name) {
        String content = JSON.toJSONString(jobj, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat);
        try {
            File file = new File(path_to + name + ".json");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            // 写入文件
            Writer write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            write.write(content);
            write.flush();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String filePath = "-output.txt";


    private static void saveAsFileWriter(String content, String type) {
        FileWriter fwriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fwriter = new FileWriter(path_to + type, true);
            fwriter.write(content);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}



class MethodCallVisitor extends VoidVisitorAdapter<Void> {
    String methodname;
    int class_id;
    public MethodCallVisitor(String methodName,int class_index) {
        methodname = methodName;
        class_id = class_index;
    }

    @Override
    public void visit(MethodCallExpr n, Void arg) {
        // Found a method call
        //System.out.print(methodname+'\n');
        //System.out.print(n.getName());

        for(Object lh:test.methodlist){
            if(((HashMap<?,?>) lh).get("name").equals(methodname)){

                //System.out.println(((HashMap<?, ?>) lh).get("name"));
                int l_id = (int) ((HashMap<?, ?>) lh).get("id");
                //System.out.print(l_id+'\n');
                for(Object rh:test.methodlist){
                    if(((HashMap<?,?>) rh).get("name").equals(n.getName().getIdentifier())){
                        int r_id = (int) ((HashMap<?, ?>) rh).get("id");
                        ArrayList method_lr = new ArrayList<>();
                        method_lr.add(l_id);
                        method_lr.add(r_id);
                        if(!method_lr.contains(method_lr)){
                            test.m2mlist.add(method_lr);
                        }

                    }
                }
                ArrayList class_lr = new ArrayList<>();
                class_lr.add(class_id);
                class_lr.add(l_id);
                if(!test.m2clist.contains(class_lr)){
                    test.m2clist.add(class_lr);
                }

            }
        }
        super.visit(n, arg);
    }
}