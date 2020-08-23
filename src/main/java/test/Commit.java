package test;

import java.util.ArrayList;

import org.eclipse.jgit.revwalk.RevCommit;

public class Commit{
	public String id;
	public int date;
	public int type;//0:add, 1: rename, 2:copy, 3: modify,  4:delete, 5:renamed, 6: copied
	public String author;
	public boolean isMerge;
	public String pathNew;
	public String pathOld;
	public String sourceNew;
	public String sourceOld;
	public String[] bugFix=null;
	public boolean isBuggy;
	public ArrayList<String> nexts=new ArrayList<String>();
	public RevCommit revCommit;

	public Commit() {
	}

	public Commit(RevCommit revCommit) {
		this.revCommit=revCommit;
	}
}
