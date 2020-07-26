package test;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;

public class VisitorCommentRatio extends ASTVisitor {
	public boolean visit(ExpressionStatement node) {
    	return true;
	}
}
