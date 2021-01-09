package Myplugin.Gumtreeutil;


import com.github.gumtreediff.gen.SyntaxException;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.io.LineReader;
import com.github.gumtreediff.tree.TreeContext;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Collectors;

public class ICJavaParserMethodGenerator extends TreeGenerator {
    ICJavaParser parser;

    public ICJavaParserMethodGenerator(){
        super();
        this.parser = new ICJavaParser();
    }

    public ICJavaParserMethodGenerator(ICJavaParser parser){
        super();
        this.parser = parser;
    }

    @VisibleForTesting
    @Override
    protected TreeContext generate(Reader reader) throws IOException {
        LineReader lr = new LineReader(reader);
        BufferedReader buffer = new BufferedReader(lr);
        String method = buffer.lines().collect(Collectors.joining("\n"));
        try {
            MethodDeclaration methodDecl = parser.parseMethodDeclaration(method);
            ICJavaParserVisitor v = new ICJavaParserVisitor(lr);
            v.visitPreOrder(methodDecl);
            return v.getTreeContext();
        }
        catch (ParseProblemException e) {
            throw new SyntaxException(this, reader);
        }
    }
}
