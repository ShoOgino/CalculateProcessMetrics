package test;

import java.util.ArrayList;
import java.util.HashMap;

public class History {
	public String path;
	public ArrayList<Commit> nodes;
	public HashMap<String, ArrayList<String>> edges;
	public HashMap<String, ArrayList<String>> edgesReverse;
	public ArrayList<String> heads;
	public ArrayList<String> roots;
	public HashMap<String, HashMap<String, String[]>> NOcommits;
	public ArrayList<String> pathCommit;

	public History() {
		this.path=new String();
		this.nodes=new ArrayList<Commit>();
		this.edges=new HashMap<String, ArrayList<String>>();
		this.edgesReverse=new HashMap<String, ArrayList<String>>();
		this.heads = new ArrayList<String>();
		this.roots = new ArrayList<String>();
		this.NOcommits=new HashMap<String, HashMap<String, String[]>>();
	}
}
