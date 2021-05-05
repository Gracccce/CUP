package CUPPlugin.Gumtreeutil;


import com.github.javaparser.*;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Reader;

public class ICJavaParser {
    JavaParser parser;

    /**
     * By default, we use LanguageLevel.CURRENT
     */
    public ICJavaParser() {
        this.parser = new JavaParser(getDefaultConfiguration());
    }

    public ICJavaParser(ParserConfiguration config) {
        this.parser = new JavaParser(config);
    }

    public static ICJavaParser createParserWithSymbolSolver() {
        ParserConfiguration config = getDefaultConfiguration();
        TypeSolver solver = new CombinedTypeSolver(
                new ReflectionTypeSolver()
        );
        config.setSymbolResolver(new JavaSymbolSolver(solver));
        return new ICJavaParser(config);
    }

    public static ParserConfiguration getDefaultConfiguration() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.CURRENT);
        return config;
    }

    /**
     * Copy from StaticJavaParser
     */
    private static <T extends Node> T handleResult(ParseResult<T> result) {
        if (result.isSuccessful()) {
            return (T) result.getResult().get();
        } else {
            throw new ParseProblemException(result.getProblems());
        }
    }

    public CompilationUnit parse(final File file) throws FileNotFoundException {
        return handleResult(parser.parse(file));
    }

    public CompilationUnit parse(final Reader reader) {
        return handleResult(parser.parse(reader));
    }

    public CompilationUnit parse(final String content) {
        return handleResult(parser.parse(content));
    }

    public MethodDeclaration parseMethodDeclaration(String methodDeclaration) {
        return handleResult(parser.parseMethodDeclaration(methodDeclaration));
    }

    public static Javadoc parseJavadoc(String javadoc) {
        // FIXME: We can not use JavadocParser in JavaParser directly, know we use the method provide
        //  StaticJavaParser to parse Javadoc. Luckily, StaticJavaParser also only call JavadocParser.parse().
        //  So, there is nothing about ParserConfiguration for now. This method should work well.
        return StaticJavaParser.parseJavadoc(javadoc);
    }
}