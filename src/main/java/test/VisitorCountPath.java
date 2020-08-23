package test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
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
				pathStatement+=2*branch;
			}
			pathIf+=pathStatement;
		}
		branches.add(pathIf);
		return false;
	}

	public boolean visit(SwitchStatement node) {
		int pathSwitch=0;
		List<Statement> statements= node.statements();
		for(int i=0; i<statements.size();i++) {
			if(statements.get(i).getNodeType()==ASTNode.SWITCH_CASE) {
				int pathSwitchCase=1;
				for(int j=i; j<statements.size();j++) {
					if(statements.get(j).getNodeType()==ASTNode.BREAK_STATEMENT)break;
					VisitorCountPath visitor=new VisitorCountPath();
					statements.get(j).accept(visitor);
					for(Integer branch: visitor.branches) {
						pathSwitchCase=pathSwitchCase*branch;
					}
				}
				pathSwitch+=pathSwitchCase;
			}
		}
		if(pathSwitch==0)pathSwitch=1;
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

	public boolean visit(TryStatement node) {
		int pathTry=0;

		int pathStatement=1;
		Statement statement=node.getBody();
		VisitorCountPath visitor = new VisitorCountPath();
		statement.accept(visitor);
		for(Integer branch: visitor.branches) {
			pathStatement=pathStatement*branch;
		}
		pathTry+=pathStatement;

		List<CatchClause> catchClauses = node.catchClauses();
		for(CatchClause catchClause: catchClauses) {
			int pathCatchClause=1;
			visitor=new VisitorCountPath();
			catchClause.accept(visitor);
			for(Integer branch: visitor.branches) {
				pathCatchClause=pathCatchClause*branch;
			}
			pathTry+=pathCatchClause;
		}

		if(node.getFinally()!=null) {
			int pathFinally=1;
		    statement=node.getFinally();
		    visitor=new VisitorCountPath();
	    	statement.accept(visitor);
			for(Integer branch: visitor.branches) {
				pathFinally=pathFinally*branch;
			}
			pathTry+=pathFinally;
		}

	    branches.add(pathTry);
		return false;
	}


	public ArrayList<Statement> getStatementsIf(IfStatement node){


		ArrayList<Statement> statements= new ArrayList<Statement>();
		statements.add(node.getThenStatement());

		Statement last=node.getElseStatement();
		while(last!=null) {
			statements.add(last);
			if(last.getNodeType()!=ASTNode.IF_STATEMENT)break;
			IfStatement tmp=(IfStatement)last;
			last=tmp.getElseStatement();
		}
		return statements;
	}
}