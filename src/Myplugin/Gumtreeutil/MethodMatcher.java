package Myplugin.Gumtreeutil;


import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
//import gumtree_test.ITreeWrapper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MethodMatcher extends Matcher {
    private class MethodVisitor implements TreeUtils.TreeVisitor {
        Map<String, List<ITree>> methodNameMap;
        Map<String, List<ITree>> unmatchedMethodSignMap;
        final Boolean isSrc;

        public MethodVisitor(Boolean isSrc){
            this.methodNameMap = new HashMap<>();
            this.unmatchedMethodSignMap = new HashMap<>();
            this.isSrc = isSrc;
        }

        private void addPairToMap(String name, ITree tree, Map<String, List<ITree>> map){
            List<ITree> treeList;
            if (map.containsKey(name)){
                treeList = map.get(name);
            } else {
                treeList = new LinkedList<>();
                map.put(name, treeList);
            }
            treeList.add(tree);
        }

        private void addMethodName(String methodName, ITree tree){
            addPairToMap(methodName, tree, methodNameMap);
        }

        private void addMethodSignature(String methodSign, ITree tree){
            addPairToMap(methodSign, tree, unmatchedMethodSignMap);
        }

        @Override
        public void startTree(ITree tree) {
            ITreeWrapper treeWrapper = new ITreeWrapper(tree);
            if (treeWrapper.isMethodDeclaration() && !isMatched(tree, isSrc)){
                addMethodName(treeWrapper.getMethodName(), tree);
                addMethodSignature(treeWrapper.getMethodSignature(), tree);
            }
        }

        @Override
        public void endTree(ITree iTree) {
        }
    }

    public MethodMatcher(ITree src, ITree dst, MappingStore mappings) {
        super(src, dst, mappings);
    }

    public boolean isMatched(ITree iTree, Boolean isSrc){
        if (isSrc)
            return mappings.hasSrc(iTree);
        else
            return mappings.hasDst(iTree);
    }

    private List<ITree> getUnmatchedTrees(List<ITree> trees, Boolean isSrc){
        List<ITree> unmatchedTress = new LinkedList<>();
        for (ITree tree : trees){
            if (!isMatched(tree, isSrc))
                unmatchedTress.add(tree);
        }
        return unmatchedTress;
    }

    @Override
    public void match() {
        // TODO: refactor this function
        MethodVisitor srcVisitor = new MethodVisitor(true);
        MethodVisitor dstVisitor = new MethodVisitor(false);
        TreeUtils.visitTree(src, srcVisitor);
        TreeUtils.visitTree(dst, dstVisitor);

        for(Map.Entry<String, List<ITree>> entry : srcVisitor.unmatchedMethodSignMap.entrySet()){
            // for each unmatched method declaration in src
            List<ITree> srcTrees = entry.getValue();
            List<ITree> dstTrees = dstVisitor.unmatchedMethodSignMap.get(entry.getKey());
            if (srcTrees.size() == 1 && dstTrees != null && dstTrees.size() == 1){
                addMapping(srcTrees.get(0), dstTrees.get(0));
            }
        }
        for(Map.Entry<String, List<ITree>> entry : srcVisitor.methodNameMap.entrySet()){
            // check whether there are only one unmatched
            List<ITree> srcUnmatchedTrees = getUnmatchedTrees(entry.getValue(), true);
            if (srcUnmatchedTrees.size() != 1 || !dstVisitor.methodNameMap.containsKey(entry.getKey()))
                continue;
            List<ITree> dstUnmatchedTrees = getUnmatchedTrees(
                    dstVisitor.methodNameMap.get(entry.getKey()), false);
            if (dstUnmatchedTrees.size() != 1)
                continue;
            // if there is only one src method and one dst method with the same methodName
            addMapping(srcUnmatchedTrees.get(0), dstUnmatchedTrees.get(0));
        }
    }
}
