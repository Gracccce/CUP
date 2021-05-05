package CUPPlugin.Gumtreeutil;


import com.github.gumtreediff.io.LineReader;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.javaparser.Position;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.LiteralStringValueExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.TreeVisitor;
//import com.google.common.reflect.TypeToken.TypeSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.NoSuchElementException;

/**
 * Copy from JavaParserVisitor and modified some
 */
public class ICJavaParserVisitor extends TreeVisitor {
    protected TreeContext context;

    private Deque<ITree> trees;

    private LineReader reader;

    public ICJavaParserVisitor(LineReader reader) {
        this.context = new TreeContext();
        this.trees = new ArrayDeque<>();
        this.reader = reader;
    }

    public TreeContext getTreeContext() {
        return context;
    }

    @Override
    public void visitPreOrder(Node node) {
        process(node);
        new ArrayList<>(node.getChildNodes()).forEach(this::visitPreOrder);
        if (trees.size() > 0)
            trees.pop();
    }

    @Override
    public void process(Node node) {
        String label = "";
        if (node instanceof SimpleName)
            label = ((SimpleName) node).getIdentifier();
        else if (node instanceof StringLiteralExpr)
            label = ((StringLiteralExpr) node).asString();
        else if (node instanceof BooleanLiteralExpr)
            label = Boolean.toString(((BooleanLiteralExpr) node).getValue());
        else if (node instanceof LiteralStringValueExpr)
            label = ((LiteralStringValueExpr) node).getValue();
        else if (node instanceof PrimitiveType)
            label = ((PrimitiveType) node).asString();
        else if (node instanceof Modifier)
            label = ((Modifier) node).getKeyword().asString();
        pushNode(node, label);
    }

    protected void pushNode(Node n, String label) {
//        int type = n.getClass().getName().hashCode();
        int type = TypeSet.getTypeCode(n.getClass());
        String typeName = n.getClass().getSimpleName();

        // do not add comment into ITree
        if (n instanceof Comment){
            trees.push(context.createTree(type, label, typeName));
        }

        try {
            Position begin = n.getRange().get().begin;
            Position end = n.getRange().get().end;
            int startPos = reader.positionFor(begin.line, begin.column);
            int length = reader.positionFor(end.line, end.column) - startPos + 2;
            push(n, type, typeName, label, startPos, length);
        }
        catch (NoSuchElementException ignore) {
            // push a placeholder token!
            trees.push(context.createTree(type, label, typeName));
        }

    }

    private void push(Node n, int type, String typeName, String label, int startPosition, int length) {
        ITree t = context.createTree(type, label, typeName);
        ITreeWrapper.setASTNode(t, n);
        t.setPos(startPosition);
        t.setLength(length);

        if (trees.isEmpty())
            context.setRoot(t);
        else {
            ITree parent = trees.peek();
            t.setParentAndUpdateChildren(parent);
        }

        trees.push(t);
    }
}