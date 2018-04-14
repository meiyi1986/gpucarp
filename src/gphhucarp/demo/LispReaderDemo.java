package gphhucarp.demo;

import ec.gp.GPTree;
import gphhucarp.gp.UCARPPrimitiveSet;
import gputils.LispUtils;

/**
 * A demo for lisp reader.
 * Given a string lisp expression, one can first simplify the expression,
 * then parse the string into a GPTree class, and print it in a Graphviz format.
 */
public class LispReaderDemo {
    public static void main(String[] args) {
        String expression =
                "(* (+ (min (max CFH 0.04039157370305135) (/ FULL CTD)) (max (- CFH FULL) 0.7542589688893021)) (+ (+ (/ DEM1 (- CFH CFR1)) (+ FUT CFH)) (- CFH CFR1)))";

        expression = LispUtils.simplifyExpression(expression);

        GPTree gpTree = LispUtils.parseExpression(expression, UCARPPrimitiveSet.wholePrimitiveSet());
        System.out.println(gpTree.child.makeGraphvizTree());
    }
}
