package CUPPlugin.Gumtreeutil;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.heuristic.gt.GreedyBottomUpMatcher;
import com.github.gumtreediff.tree.ITree;

public class ICGreedyBottomUpMatcher extends GreedyBottomUpMatcher {

    public ICGreedyBottomUpMatcher(ITree src, ITree dst, MappingStore store) {
        super(src, dst, store);
    }

    @Override
    protected void addMapping(ITree src, ITree dst) {
        if (mappings.hasSrc(src) || mappings.hasDst(dst))
            return;
        super.addMapping(src, dst);
    }
}
