package test;

public class Method {
	String id="";
	String name="";
	String path="";
	History history=new History();
	int fanIN=0;
	int fanOut=0;
    int parameters=0;
    int localVar=0;
    double commentRatio=0;
    int countPath=0;
    int complexity=0;
    int execStmt=0;
    int maxNesting=0;

    int methodHistories=1;
    int authors=1;
    int stmtAdded=0;
    int maxStmtAdded=0;
    double avgStmtAdded=0;
    int stmtDeleted=0;
    int maxStmtDeleted=0;
    double avgStmtDeleted=0;
    int churn=0;
    int maxChurn=0;
    double avgChurn=0;
    int decl=0;
    int cond=0;
    int elseAdded=0;
    int elseDeleted=0;

    public Method() {
    }
    public Method(String path) {
    	this.path=path;
    }
    public Method(String id, String path) {
    	this.id=id;
    	this.path=path;
    }
}