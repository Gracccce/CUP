package CUPPlugin;

public class CommentUtil {
    String ans="";
    public  boolean check(String comment){
        boolean flag=false;
        if(comment.charAt(0)=='/' && comment.charAt(1)=='*' ){
            flag=true;
            int count=2;
            while(count<comment.length() && (comment.charAt(count)=='*' || comment.charAt(count)==' ')){
                count++;
            }
            if(count<comment.length()){
                int end=comment.length();
                if(comment.charAt(end-2)=='*' && comment.charAt(end-1)=='/'){
                    end=end-2;
                    flag=false;
                }
                ans += comment.subSequence(count,end).toString();
                ans +='\n';
            }
        }else{
            flag=false;
            int count = 2;
            while(count<comment.length() && comment.charAt(count)==' '){
                count++;
            }
            if(count<comment.length()){
                ans += comment.subSequence(count,comment.length()).toString();
                ans +='\n';
            }
        }
        return flag;
    }
    public String get_comment(String raw){
        String[] comments = raw.split("\n");
        if(comments.length==0) return "";
        boolean flag=false;
        flag=check(comments[0]);
        for(int i=1;i<comments.length-1;i++){
            int end = comments[i].length();
            if(flag){
                if(comments[i].length()>=2 && comments[i].charAt(0)=='*' && comments[i].charAt(1)=='/'){
                    flag=false;
                    continue;
                }
                int count=0;
                while(count<comments[i].length() && (comments[i].charAt(count)=='*' || comments[i].charAt(count)==' ')){
                    count++;
                }
                if(count<comments[i].length()){
                    if(comments[i].charAt(end-2)=='*' && comments[i].charAt(end-1)=='/'){
                        end=end-2;
                        flag=false;
                    }
                    ans += comments[i].subSequence(count,end).toString();
                    ans +='\n';
                }
            }else{
                flag=check(comments[i]);
            }
        }
        return ans;
    }
}
