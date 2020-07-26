package test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class VisitorCountPath  extends ASTVisitor{
    ArrayList<Integer> branches=new ArrayList<Integer>();

	public boolean visit(IfStatement node) {
		int pathIf=1;
		ArrayList<Statement> statements= getStatementsIf(node);
		for(Statement statement:statements) {
			int pathStatement=1;
			VisitorCountPath visitor=new VisitorCountPath();
			statement.accept(visitor);
			for(Integer branch: visitor.branches) {
				pathStatement=pathStatement*branch;
			}
			pathIf+=pathStatement;
		}
		branches.add(pathIf);
		return false;
	}

	public boolean visit(SwitchStatement node) {
		int pathSwitch=1;
		List<Statement> statements= node.statements();
		for(Statement statement:statements) {
			int pathStatement=1;
			VisitorCountPath visitor=new VisitorCountPath();
			statement.accept(visitor);
			for(Integer branch: visitor.branches) {
				pathStatement=pathStatement*branch;
			}
			pathSwitch+=pathStatement;
		}
		branches.add(pathSwitch);
		return false;
	}
	public boolean visit(ForStatement node) {
		int pathFor=1;
		Statement statement=node.getBody();
		int pathStatement=1;
		VisitorCountPath visitor=new VisitorCountPath();
		statement.accept(visitor);
		for(Integer branch: visitor.branches) {
			pathStatement=pathStatement*branch;
		}
		pathFor+=pathStatement;
	    branches.add(pathFor);
	    return false;
	}

	public boolean visit(WhileStatement node) {
		int pathWhile=1;
		Statement statement=node.getBody();
		int pathStatement=1;
		VisitorCountPath visitor=new VisitorCountPath();
		statement.accept(visitor);
		for(Integer branch: visitor.branches) {
			pathStatement=pathStatement*branch;
		}
		pathWhile+=pathStatement;
	    branches.add(pathWhile);
	    return false;
	}

	public ArrayList<Statement> getStatementsIf(IfStatement node){
		ArrayList<Statement> statements= new ArrayList<Statement>();
		statements.add(node.getThenStatement());

		Statement last=node.getElseStatement();
		while(last!=null&&last.getNodeType()==ASTNode.IF_STATEMENT) {
			IfStatement tmp=(IfStatement)last;
			statements.add(last);
			last=tmp.getElseStatement();
		}
		return statements;
	}
}