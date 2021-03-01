package com.moon.processing.holder;

import com.moon.accessor.annotation.condition.IfMatching;
import com.moon.processing.JavaFiler;
import com.moon.processing.JavaWritable;
import com.moon.processing.decl.MatchingDeclared;

import javax.lang.model.element.TypeElement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author benshaoye
 */
public class MatchingHolder extends BaseHolder implements JavaWritable {

    private final Map<String, MatchingDeclared> matchingDeclaredMap = new LinkedHashMap<>();

    public MatchingHolder(Holders holders) { super(holders); }

    public MatchingDeclared with(TypeElement matchingAnnotationElem, TypeElement matcherElem, IfMatching matching) {
        MatchingDeclared declared = new MatchingDeclared(matchingAnnotationElem, matcherElem, matching);
        matchingDeclaredMap.put(declared.getMatchingAnnotationName(), declared);
        return declared;
    }

    @Override
    public void write(JavaFiler writer) {
        writer.write(matchingDeclaredMap.values());
    }
}
