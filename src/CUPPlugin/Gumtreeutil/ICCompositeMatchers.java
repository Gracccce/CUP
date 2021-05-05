package CUPPlugin.Gumtreeutil;

import com.github.gumtreediff.gen.Registry;
import com.github.gumtreediff.matchers.*;
import com.github.gumtreediff.matchers.heuristic.gt.GreedySubtreeMatcher;
import com.github.gumtreediff.tree.ITree;
//import diff.gen.javaparser.gumtree_test.MethodMatcher;

public class ICCompositeMatchers {
    @Register(id = "gumtree-method", defaultMatcher = true, priority = Registry.Priority.MAXIMUM)
    public static class MethodGumtree extends CompositeMatcher {

        public MethodGumtree(ITree src, ITree dst, MappingStore store) {
            super(src, dst, store, new Matcher[]{
                    new GreedySubtreeMatcher(src, dst, store),
                    new MethodMatcher(src, dst, store),
                    new ICGreedyBottomUpMatcher(src, dst, store)
            });
        }

        public MethodGumtree(ITree src, ITree dst, MappingStore store, Matcher methodMatcher){
            super(src, dst, store, new Matcher[]{
                    new GreedySubtreeMatcher(src, dst, store),
                    methodMatcher,
                    new ICGreedyBottomUpMatcher(src, dst, store)
            });
        }
    }
}
