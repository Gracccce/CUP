package CUPPlugin;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import java.util.*;

public class JavaParser {
    private List<String> method_decs = new ArrayList<>();
    private Map<String,String> method_comment = new HashMap<>();
    private Map<String,String>  method_body= new HashMap<>();

    public List<String> getMethod_decs() {
        return method_decs;
    }

    public Map<String, String> getMethod_comment() {
        return method_comment;
    }

    public Map<String, String> getMethod_body() {
        return method_body;
    }
    public void init(String file){
        CompilationUnit compilationUnit = StaticJavaParser.parse(file);
        List<Node> nodes = compilationUnit.getChildNodes();
        for(int i=0;i<nodes.size();i++){
            if(nodes.get(i) instanceof ClassOrInterfaceDeclaration){
                List<Node> chidnodes = nodes.get(i).getChildNodes();
                for(int j=0;j<chidnodes.size();j++){
                    if(chidnodes.get(j) instanceof MethodDeclaration){
                        MethodDeclaration methodDeclaration = (MethodDeclaration)chidnodes.get(j);
                        String method_dec =  methodDeclaration.getDeclarationAsString();
                        method_decs.add(method_dec);
                        if(methodDeclaration.getBody().isPresent()){
                            method_body.put(method_dec,methodDeclaration.getBody().get().toString());
                        }
                        if(methodDeclaration.getComment().isPresent()){
                            method_comment.put(method_dec,methodDeclaration.getComment().get().toString());
                        }
                    }
                }
            }
        }
    }
}
