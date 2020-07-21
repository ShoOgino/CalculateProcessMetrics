package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.TreeDelete;
import com.github.gumtreediff.actions.model.TreeInsert;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
public class Test {
	public static void main(String[] args) throws IOException {
        Method method=new Method();
        History history=new History();
		getHistory(history, "C:/Users/login/work/InferBugs/actions/test/testDataset/ant/repository/.git", "src/main/org/apache/tools/zip/ZipShort#clone().mjava");
        getMetrics(method,history);
	}


	private static void getHistory(History history, String pathRepository, String pathFile){
		try {
			Repository repository = new FileRepositoryBuilder()
				    .setGitDir(new File(pathRepository))
				    .build();
			final Git git = new Git(repository);
	        DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(repository
	                .getConfig()
	                .getEnum(ConfigConstants.CONFIG_DIFF_SECTION, null, ConfigConstants.CONFIG_KEY_ALGORITHM, SupportedAlgorithm.HISTOGRAM));
	        ObjectReader reader = repository.newObjectReader();

	        ObjectId head = repository.resolve("HEAD");
	        Iterable<RevCommit> log = git.log().add(head).call();
	        Iterable<RevCommit> revCommits = git.log().addPath(pathFile).call();

	        DiffFormatter diffFormatter = new DiffFormatter(System.out);
	        diffFormatter.setRepository(repository);
	        AnyObjectId prevTree = null;
	        AnyObjectId currentTree = log.iterator().next().getTree();

	        ObjectId commitPrev = null;
	        for (RevCommit rev: log) {
	            prevTree = currentTree.copy();
	            RevCommit commit=rev;
	            //System.out.println(commit.toObjectId());

	            currentTree = commit.getTree();
	            List<DiffEntry> list = diffFormatter.scan(currentTree,prevTree);

	            for (DiffEntry diffEntry : list) {
	            	if(diffEntry.getNewPath().equals(pathFile)){
        	            System.out.println(commitPrev.toObjectId());
        	            System.out.println(diffEntry.getNewPath());
        	            System.out.println(diffEntry.getChangeType());
	                    if (diffEntry.getChangeType() != DiffEntry.ChangeType.DELETE) {
	                    	history.authors.add(commit.getAuthorIdent().getName());
	            	        String str;
	            	        ObjectLoader loader=repository.open(diffEntry.getNewId().toObjectId());
	            	    	str=new String(loader.getBytes());
	            	    	history.snapshots.add(0, str);
	            	    	System.out.println(str);
	                    }
	                    if (diffEntry.getChangeType() == DiffEntry.ChangeType.RENAME) {
	                    	pathFile=diffEntry.getOldPath();
	                    }
	                }
	            }
	            commitPrev= commit.copy();
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}


	private static void getMetrics(Method method, History history) throws IOException {
		ArrayList<Integer> stmtAddeds=new ArrayList<Integer>();
        ArrayList<Integer> stmtDeleteds=new ArrayList<Integer>();
        ArrayList<Integer> churns=new ArrayList<Integer>();

        List<String> statements = Arrays.asList("AssertStatement","Block","BreakStatement","ConstructorInvocation","ContinueStatement","DoStatement","EmptyStatement","EnhancedForStatement", "ExpressionStatement","ForStatement","IfStatement","LabeledStatement","ReturnStatement","SuperConstructorInvocation","SwitchCase","SwitchStatement","SynchronizedStatement","ThrowStatement","TryStatement","TypeDeclarationStatement","VariableDeclarationStatement","WhileStatement");

        String sourcePrev =  "public class Test{}";
        String sourceCurrent =null;

		for(String snapshot: history.snapshots) {
			int stmtAdded=0;
			int stmtDeleted=0;
			sourceCurrent="public class Test {"+snapshot+" public void test(){if(true){}else{}}}";
            JdtTreeGenerator jdtTreeGenerator = new JdtTreeGenerator();
			ITree iTreePrev = jdtTreeGenerator.generateFrom().string(sourcePrev).getRoot();
			ITree iTreeCurrent = jdtTreeGenerator.generateFrom().string(sourceCurrent).getRoot();
			Matcher defaultMatcher = Matchers.getInstance().getMatcher();
			MappingStore mappings = defaultMatcher.match(iTreePrev, iTreeCurrent);
			EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator();
			EditScript actions = editScriptGenerator.computeActions(mappings);
			for(Action action: actions) {
				System.out.println(action);
				if("insert-node".equals(action.getName())) {
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Insert) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							stmtAdded++;
						}
					}
				}else if("insert-tree".equals(action.getName())) {
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeInsert) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							stmtAdded++;
						}
					}
				}else if("delete-node".equals(action.getName())){
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((Delete) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							stmtDeleted++;
						}
					}
				}else if("delete-tree".equals(action.getName())) {
					Iterator<ITree> childs=TreeUtils.breadthFirstIterator(((TreeDelete) action).getNode());
					while(childs.hasNext()) {
						if(statements.contains(childs.next().getType().toString())) {
							stmtDeleted++;
						}
					}

				}else if("move-tree".equals(action.getName())) {
				}else if("update-node".equals(action.getName())) {
				}
				//System.out.println(action.getNode());
				//System.out.println(action.getName());
			}
			stmtAddeds.add(stmtAdded);
			stmtDeleteds.add(stmtDeleted);
			churns.add(stmtAdded-stmtDeleted);
			sourcePrev="public class Test {"+snapshot+"}";
		}
		method.methodHistories=history.snapshots.size();
		method.authors=history.authors.size();
		method.stmtAdded=sum(stmtAddeds);
		method.maxStmtAdded=max(stmtAddeds);
		method.avgStmtAdded=avg(stmtAddeds);
		method.stmtDeleted=sum(stmtDeleteds);
		method.maxStmtDeleted=max(stmtDeleteds);
		method.avgStmtDeleted=avg(stmtDeleteds);
		method.churn=sum(churns);
		method.maxChurn=max(churns);
		method.avgChurn=avg(churns);
	}
	private static int sum(ArrayList<Integer> input) {
		int sum=0;
		for(int i:input) {
			sum+=i;
		}
		return sum;
	}
	private static int max(ArrayList<Integer> input) {
		int max=0;
		for(int i:input) {
			if(max<i) {
				max=i;
			}
		}
		return max;
	}
	private static int avg(ArrayList<Integer> input) {
		return sum(input)/input.size();
	}

}
