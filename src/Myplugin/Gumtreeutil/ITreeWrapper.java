package Myplugin.Gumtreeutil;


import com.github.gumtreediff.tree.ITree;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ITreeWrapper {
    private static final String AST_NODE = "astNode";
    ITree tree;
    List<ITreeWrapper> children;

    public ITreeWrapper(ITree tree) {
        this.tree = tree;
        this.children = new ArrayList<>();
    }

    public ITree getTree() {
        return tree;
    }

    public static void setASTNode(ITree tree, Node node){
        tree.setMetadata(AST_NODE, node);
    }

    public Node getASTNode() {
        return (Node) tree.getMetadata(AST_NODE);
    }

    public static Node getASTNode(ITree tree){
        return (Node) tree.getMetadata(AST_NODE);
    }

    public List<ITreeWrapper> getChildren(){
        if (children.size() == tree.getChildren().size())
            return children;
        children = tree.getChildren()
                .stream()
                .map(ITreeWrapper::new)
                .collect(Collectors.toList());
        return children;
    }

    public boolean isMethodDeclaration(){
        // FIXME: for now, we only consider MethodDeclaration, ignore ConstructorDeclaration!
        // In JDT, MethodDeclaration contains constructors
        // But in javaparser, they are separated.
        return (getASTNode() instanceof MethodDeclaration);
    }

    public boolean isCompilationUnit(){
        return (getASTNode() instanceof CompilationUnit);
    }

    public String getCode() {
        return getASTNode().getTokenRange().isPresent() ? getASTNode().getTokenRange().get().toString() : "";
    }

    public String getComment() {
        return getASTNode().getComment().isPresent() ? getASTNode().getComment().get().toString() : "";
    }

    public boolean isEmptyComment() {
        return getComment() == null || getComment().equals("");
    }

    public String getMethodName(){
        assert isMethodDeclaration();
        return MethodDeclarationUtil.getMethodName((MethodDeclaration) getASTNode());
    }

    public String getMethodSignature(){
        assert isMethodDeclaration();
        return MethodDeclarationUtil.getMethodSignature((MethodDeclaration) getASTNode());
    }

    /**
     * Find a class by name and get all the methods in this class.
     * If the class can not be found, return an empty list
     */
    public List<MethodDeclaration> getMethodsFromCU() {
        assert isCompilationUnit();
        return MethodDeclarationUtil.getMethodsFromCU((CompilationUnit) getASTNode());
    }

    public static boolean isEqualTree(ITree t1, ITree t2){
        // root is not equal
        if (! (t1.getType() == t2.getType() && t1.getLabel().equals(t2.getLabel())) )
            return false;
        // different sizes of children
        if (t1.getChildren().size() != t2.getChildren().size())
            return false;

        Iterator<ITree> iter1 = t1.getChildren().iterator();
        Iterator<ITree> iter2 = t2.getChildren().iterator();
        while(iter1.hasNext() && iter2.hasNext()){
            ITree child1 = iter1.next();
            ITree child2 = iter2.next();
            // specific children are different
            if (!isEqualTree(child1, child2))
                return false;
        }

        return true;
    }
}