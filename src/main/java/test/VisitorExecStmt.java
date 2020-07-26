package test;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class VisitorExecStmt extends ASTVisitor {
	int execStmt=0;

	public boolean visit(AssertStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(Block node) {
		execStmt++;
    	return true;
	}
	public boolean visit(BreakStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(ConstructorInvocation node) {
		execStmt++;
    	return true;
	}
	public boolean visit(ContinueStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(DoStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(EmptyStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(EnhancedForStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(ExpressionStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(ForStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(IfStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(LabeledStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(ReturnStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(SuperConstructorInvocation node) {
		execStmt++;
    	return true;
	}
	public boolean visit(SwitchCase node) {
		execStmt++;
    	return true;
	}
	public boolean visit(SwitchStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(SynchronizedStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(ThrowStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(TryStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(TypeDeclarationStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(VariableDeclarationStatement node) {
		execStmt++;
    	return true;
	}
	public boolean visit(WhileStatement node) {
		execStmt++;
    	return true;
	}
}