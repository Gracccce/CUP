package CUPPlugin.Gumtreeutil;


import com.github.gumtreediff.gen.Register;
import com.github.gumtreediff.gen.Registry;
import com.github.gumtreediff.gen.SyntaxException;
import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.io.LineReader;
import com.github.gumtreediff.tree.TreeContext;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.io.Reader;

/**
 * copy from JavaParserGenerator
 * Modifies:
 * 1. change the language level of javaparser
 * 2. use ICJavaParserVisitor
 */
@Register(id = "java-javaparser-ic", accept = "\\.java$", priority = Registry.Priority.MAXIMUM)
public class ICJavaParserGenerator extends TreeGenerator {
    ICJavaParser parser;

    public ICJavaParserGenerator() {
        super();
        this.parser = new ICJavaParser();
    }

    public ICJavaParserGenerator(ICJavaParser parser) {
        super();
        this.parser = parser;
    }

    @Override
    protected TreeContext generate(Reader reader) throws IOException {
        LineReader lr = new LineReader(reader);
        try {
            CompilationUnit cu = parser.parse(lr);
            ICJavaParserVisitor v = new ICJavaParserVisitor(lr);
            v.visitPreOrder(cu);
            return v.getTreeContext();
        }
        catch (ParseProblemException e) {
            throw new SyntaxException(this, reader);
        }
    }
}
