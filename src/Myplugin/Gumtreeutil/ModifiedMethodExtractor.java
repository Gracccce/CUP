package Myplugin.Gumtreeutil;

import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.javaparser.ast.body.MethodDeclaration;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * NOTE: Extract modified methods.
 * Do not consider the methods where only Javadoc and comments are changed.
 */
public class ModifiedMethodExtractor {
    private TreeGenerator generator;

    public ModifiedMethodExtractor(){
        generator = new ICJavaParserGenerator();
    }

    public Map<ITreeWrapper, ITreeWrapper> extract(InputStream srcStream, InputStream dstStream)
            throws IOException {
        return extract(generator.generateFromStream(srcStream).getRoot(),
                generator.generateFromStream(dstStream).getRoot());
    }

    public Map<ITreeWrapper, ITreeWrapper> extract(String srcPath, String dstPath) throws IOException {
        // NOTE: this function may through SyntaxException
        //       but IntelliJ can not detect because of the abstract class TreeGenerator
        return extract(generator.generateFromFile(srcPath).getRoot(),
                generator.generateFromFile(dstPath).getRoot());
    }

    public Map<ITreeWrapper, ITreeWrapper> extract(ITree src, ITree dst){
//        Logger logger = LoggerFactory.getLogger(this.getClass());
//        logger.debug("Src tree: {}", src.toShortString());
//        logger.debug("Dst tree: {}", dst.toShortString());

        Matcher m = new ICCompositeMatchers.MethodGumtree(src, dst, new MappingStore());
        m.match();
        return extractModifiedMethod(m.getMappings());
    }

    private Map<ITreeWrapper, ITreeWrapper> extractModifiedMethod(MappingStore mappings){
        Map<ITreeWrapper, ITreeWrapper> modifiedMethodMap = new HashMap<>();
        Set<ITree> modifiedSrcMethods = new HashSet<>();
        for (Mapping pair : mappings){
            ITreeWrapper t1 = new ITreeWrapper(pair.getFirst());
            ITreeWrapper t2 = new ITreeWrapper(pair.getSecond());
            assert t1.getASTNode().getClass().equals(t1.getASTNode().getClass());
            if (t1.getASTNode() instanceof MethodDeclaration
                    && !ITreeWrapper.isEqualTree(t1.getTree(), t2.getTree())){
                // make sure this src method has not been added
                assert !modifiedSrcMethods.contains(t1.getTree());
                modifiedSrcMethods.add(t1.getTree());
                modifiedMethodMap.put(t1, t2);
            }
        }
        return modifiedMethodMap;
    }
}