package test;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class VisitorComplexity extends ASTVisitor {
	int complexity=0;


	public boolean visit(IfStatement node) {
	    complexity++;
	    return true;
    }
	public boolean visit(ForStatement node) {
		complexity++;
		return true;
	}
	public boolean visit(EnhancedForStatement node) {
		complexity++;
		return true;
	}
	public boolean visit(WhileStatement node) {
		complexity++;
		return true;
	}
	public boolean visit(SwitchStatement node) {
		complexity++;
		return true;
	}

}
