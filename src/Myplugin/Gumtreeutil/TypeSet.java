package Myplugin.Gumtreeutil;

import com.github.javaparser.ast.Node;

public class TypeSet {
    public static int getTypeCode(Class<? extends Node> nodeClass){
        return nodeClass.getName().hashCode();
    }
}