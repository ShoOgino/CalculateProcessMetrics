package test;

public class Commit {
	public String id;
	public int date;
	public int type;///0:add, 1: rename, 2:copy, 3: modify,  4:delete
	public String author;
	public boolean isMerge;
	public String sourceNew;
	public String sourceOld;
	public String pathNew;
	public String pathOld;
	public String[] bugFix;

	int stmtAdded=0;
	int stmtDeleted=0;
	int churn=0;
	int decl=0;
	int cond=0;
	int elseAdded=0;
	int elseDeleted=0;

}
