package CUPPlugin.Gumtreeutil;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserParameterDeclaration;

import java.util.*;

public class MethodDeclarationUtil {

    public static String getMethodName(MethodDeclaration method){
        return method.getName().toString();
    }

    public static String getMethodSignature(MethodDeclaration method){
        // NOTE: signature does not contain return type and generic types
        // MethodDeclaration: String test(Content<A> label, int a);
        // signature: test(Content, int)
        return method.getSignature().toString();
    }

    public static String getReturnTypeString(MethodDeclaration method){
        return getReturnType(method).asString();
    }

    public static Type getReturnType(MethodDeclaration method){
        return method.getType();
    }

    public static NodeList<Parameter> getParameters(MethodDeclaration method){
        return method.getParameters();
    }

    public static boolean hasReturnStmts(MethodDeclaration method){
        List<ReturnStmt> returnStmts = method.findAll(ReturnStmt.class);
        return returnStmts.size() > 0;
    }

    public static boolean isEqualMethodName(MethodDeclaration m1, MethodDeclaration m2){
        return getMethodName(m1).equals(getMethodName(m2));
    }

    public static boolean isEqualReturnType(MethodDeclaration m1, MethodDeclaration m2){
        return getReturnType(m1).equals(getReturnType(m2));
    }

    public static boolean isEqualParamList(MethodDeclaration m1, MethodDeclaration m2){
        NodeList<Parameter> params1 = getParameters(m1);
        NodeList<Parameter> params2 = getParameters(m2);
        return params1.equals(params2);
    }

    public static Set<Parameter> extractChangedParams(MethodDeclaration src, MethodDeclaration dst) {
        List<Parameter> srcParams = src.getParameters();
        Set<Parameter> srcParamSet = new HashSet<>(srcParams);
        List<Parameter> dstParams = dst.getParameters();
        Set<Parameter> changedParams = new HashSet<>();
        if (srcParams.equals(dstParams))
            return new HashSet<>();
        for (Parameter dp : dstParams) {
            if (!srcParamSet.contains(dp))
                changedParams.add(dp);
        }
        return changedParams;
    }

    /**
     * Assume the input MethodDeclaration m is solvable
     */
    public static boolean isUseGivenParams(MethodDeclaration m, Set<Parameter> params){
        List<NameExpr> names = m.findAll(NameExpr.class);
        ResolvedValueDeclaration decl;
        for (NameExpr name : names){
            try {
                decl = name.resolve();
            } catch (RuntimeException e) {
                // not solvable
                continue;
            }
            if (decl instanceof ResolvedParameterDeclaration &&
                    params.contains(((JavaParserParameterDeclaration) decl.asParameter()).getWrappedNode())){
                return true;
            }
        }
        return false;
    }

    public static int getCompsacStmtNum(MethodDeclaration m){
        return m.findAll(Statement.class).size() + m.findAll(CatchClause.class).size();
    }

    public static List<MethodDeclaration> getMethodsFromCU(CompilationUnit cu){
        Optional<ClassOrInterfaceDeclaration> clazz = cu.getClassByName(getFakeCUName());
        if (clazz.isPresent())
            return clazz.get().getMethods();
        else{
            Optional<ClassOrInterfaceDeclaration> interfazz = cu.getInterfaceByName(getFakeCUName());
            if (interfazz.isPresent())
                return interfazz.get().getMethods();
        }
        return new ArrayList<>();
    }

    public static String getFakeClass(String method) {
        String header1 = "public class " + getFakeCUName() + " {\n";
        String header2 = "\n}";
        return header1 + method + header2;
    }

    public static String getFakeInterface(String method) {
        String header1 = "public interface " + getFakeCUName() + "{\n";
        String header2 ="\n}";
        return header1 + method + header2;
    }

    public static String getFakeCUName() {
        return "Test";
    }

//    public static MethodDeclaration parseMethodWithinCU(String method){
//        ICJavaParser parser = ICJavaParser.createParserWithSymbolSolver();
//        CompilationUnit cu;
//        try {
//            String fakeClass = getFakeClass(method);
//            cu = parser.parse(fakeClass);
//        } catch (SyntaxException e){
//            String fakeInterface = getFakeInterface(method);
//            cu = parser.parse(fakeInterface);
//        }
//
//        return gumtree_test.MethodDeclarationUtil.getMethodsFromCU(cu).get(0);
//    }

    public static boolean isCompsacStmt(Node node){
        // Both Statement and CatchClause should be extracted
        return node instanceof Statement || node instanceof CatchClause;
    }
}