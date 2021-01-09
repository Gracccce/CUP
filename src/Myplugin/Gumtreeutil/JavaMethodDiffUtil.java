package Myplugin.Gumtreeutil;

import com.github.gumtreediff.gen.SyntaxException;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class JavaMethodDiffUtil {
    public static Map<String,String> GetChangeMethod (InputStream srcStream, InputStream dstStream){
        ModifiedMethodExtractor extractor = new ModifiedMethodExtractor();
        Map<ITreeWrapper, ITreeWrapper> modifiedMethods = new HashMap<>();
        try {
            modifiedMethods = extractor.extract(srcStream, dstStream);
        } catch (SyntaxException | IOException e) {
            LoggerFactory.getLogger("Error").error("Syntax error for diff: {}",
                    String.join(" | "));
        }
        Map<String,String> method_pair = new HashMap<>();
        for (Map.Entry<ITreeWrapper, ITreeWrapper> entry : modifiedMethods.entrySet()){
//            ICSample tempSample = new ICSample(this, entry.getKey(), entry.getValue());
//            if (sampleFilter.pass(tempSample))
//                samples.add(tempSample);
//            System.out.println(entry.getKey().getMethodSignature());
//            System.out.println(entry.getValue().getMethodSignature());
            String src_method = entry.getKey().getASTNode().toString();
            String dst_method = entry.getValue().getASTNode().toString();
            method_pair.put(src_method,dst_method);

        }
        return method_pair;
    }
}
