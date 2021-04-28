package Myplugin;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.NotTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import Myplugin.Gumtreeutil.JavaMethodDiffUtil;
import  java.io.IOException.*;

public class JgitDiff {
    public String get_method_name(String test_method, List<String> new_methods){
        String ans="";
        for(int i=0;i<new_methods.size();i++){
            if(test_method.indexOf(new_methods.get(i))!=-1){
                ans = new_methods.get(i);
                break;
            }
        }
        return ans;
    }
    //two commit version, not used
    public List<String> get_code_comment(String pathname, String filePath, String test_method){
        String new_file = null;
        String old_file = null;
        String parent="";
        String child="";
        List<String> ans = new ArrayList<>();
        int count=0;
        try(Repository existingRepo = new FileRepositoryBuilder()
                .setGitDir(new File(pathname)).build()){
            RevWalk walk = new RevWalk(existingRepo);
            Ref head = existingRepo.findRef("HEAD");
            walk.markStart(walk.parseCommit(head.getObjectId()));
            for (RevCommit commit : walk) {
                count++;
                RevTree tree = commit.getTree();
//                String entryPath ="src/MHttpClient.java";
                String entryPath =filePath;
                TreeWalk treeWalk = TreeWalk.forPath(existingRepo, entryPath, tree);
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = existingRepo.open(objectId);
                if(count==1){
                    parent = commit.getId().name();
                }else{
                    child = commit.getId().name();
                }
                byte[] bytes = loader.getBytes();
                String file = new String( bytes, "utf-8" );
                if(count==1){
                    new_file = file;
                }else{
                    old_file = file;
                    break;
                }
            }
            Git git = new Git(existingRepo);
            String[] diffs = diffMethod(child,parent,existingRepo,git,filePath);
//            for(int i=0;i<diffs.length;i++){
//                System.out.println(diffs[i]);
//            }
            JavaParser oldjp = new JavaParser();
            JavaParser newjp = new JavaParser();
            oldjp.init(old_file);
            newjp.init(new_file);
//            InputStream srcStream = new ByteArrayInputStream(old_file.getBytes(StandardCharsets.UTF_8));
//            InputStream dstStream = new ByteArrayInputStream(new_file.getBytes(StandardCharsets.UTF_8));
//            Map<String,String> changeMethodPair = JavaMethodDiffUtil.GetChangeMethod(srcStream,dstStream);
//            Map<String,String> changed_name = method_match(changeMethodPair,oldjp.getMethod_decs(),newjp.getMethod_decs());
            Map<String,String> changed_name = method_match(diffs,oldjp.getMethod_decs(),newjp.getMethod_decs());
            test_method = get_method_name(test_method,newjp.getMethod_decs() );
            String pre_method = test_method;
            if(changed_name.get(test_method)!=null){
                pre_method = changed_name.get(test_method);
            }
            ans.add(pre_method+oldjp.getMethod_body().get(pre_method));
            ans.add(test_method+newjp.getMethod_body().get(test_method));
            ans.add(oldjp.getMethod_comment().get(pre_method));

        }catch(IOException ex){
            ex.printStackTrace();
        }
        return ans;
    }
    public Map<String,String> method_match(Map<String,String> method_change, List<String> old_methods, List<String> new_methods){
        Map<String,String> ans = new HashMap<>();
        Set<Map.Entry<String, String>> mapEntrySet = method_change.entrySet();
        Iterator<Map.Entry<String,String>> it = mapEntrySet.iterator();
        while(it.hasNext()){
            Map.Entry<String,String> entry = it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            String old_mtd="",new_mtd = "";
            for(int j=0;j<old_methods.size();j++){
                if(key.indexOf(old_methods.get(j))!=-1){
                    old_mtd = old_methods.get(j);
                    break;
                }
            }
            for(int j=0;j<new_methods.size();j++){
                if(value.indexOf(new_methods.get(j))!=-1){
                    new_mtd = new_methods.get(j);
                    break;
                }
            }
            if(old_mtd!="" && new_mtd != ""){
                ans.put(new_mtd,old_mtd);
            }
        }
        return ans;
    }
    public Map<String,String> method_match(String[] diffs, List<String> old_methods, List<String> new_methods){
        Map<String,String> ans = new HashMap<>();
        for(int i=0; i<diffs.length; i++){
            String old_mtd="",new_mtd = "";
            if(diffs[i].charAt(0)=='-' && diffs[i+1].charAt(0)=='+'){
                for(int j=0;j<old_methods.size();j++){
                    if(diffs[i].indexOf(old_methods.get(j))!=-1){
                        old_mtd = old_methods.get(j);
                        break;
                    }
                }
                if(old_mtd!=""){
                    for(int j=0;j<new_methods.size();j++){
                        if(diffs[i+1].indexOf(new_methods.get(j))!=-1){
                            new_mtd = new_methods.get(j);
                            break;
                        }
                    }
                }
                if(old_mtd!="" && new_mtd != ""){
                    ans.put(new_mtd,old_mtd);
                }
            }
        }
        return  ans;
    }
    public  String[] diffMethod(String repoPath,String file_path){
        String returnDiffs = "";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.addCeilingDirectory(new File(repoPath));
        builder.findGitDir(new File(repoPath));
        try{
            Repository repo = builder.build();
            PathFilter pathFilter = PathFilter.create(".idea");
            TreeFilter treeFilter = NotTreeFilter.create(pathFilter);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Git git = new Git(repo);
            List<DiffEntry> diffs = git.diff()
                    .setOutputStream(outputStream)
                    .setPathFilter(treeFilter)
                    .call();
            returnDiffs = outputStream.toString("UTF-8");
            return get_file_diff(returnDiffs,file_path);

        }catch (IOException | GitAPIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnDiffs.split("\n");

    }
    public boolean check_start(String str){
        if(str.indexOf("diff --git")!=-1){
            return true;
        }
        return false;
    }
    public String[] get_file_diff(String diff,String file_path){
        String[] diffs = diff.split("\n");
        String ans="";
        boolean flag=false;
        for(int i=0;i<diffs.length;i++){
            if(check_start(diffs[i])){
                String[] dfs = diffs[i].split("\\s+");
                if(dfs[2].substring(2).equals(file_path)){
                    flag=true;
                }else{
                    flag=false;
                }
            }
            if(flag){
                ans= ans+ diffs[i] + '\n';
            }
        }
        return ans.split("\n");
    }
    public String[] diffMethod(String Child, String Parent,Repository repository,Git git,String file_path){
        ObjectReader reader = repository.newObjectReader();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
        try {
            ObjectId old = repository.resolve(Child + "^{tree}");
            ObjectId head = repository.resolve(Parent+"^{tree}");
            oldTreeIter.reset(reader, old);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            List<DiffEntry> diffs= git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
            df.setRepository(repository);

            for (DiffEntry diffEntry : diffs) {
                out.reset();
                df.format(diffEntry);
                String diffText = out.toString("UTF-8");
                String[] diffTexts = diffText.split("\n");
                String[] dfs = diffTexts[0].split("\\s+");
//                System.out.println(dfs[2].substring(2));
                if(dfs[2].substring(2).equals(file_path)){
                    return diffTexts;
                }
            }

        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }
}
