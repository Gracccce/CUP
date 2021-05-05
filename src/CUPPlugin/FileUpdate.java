package CUPPlugin;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;

import java.util.List;
import java.util.Properties;
public class FileUpdate extends AnAction{
    public void actionPerformed(AnActionEvent e ) {
        Project currentProject = e.getProject();
        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
        Document doc = mEditor.getDocument();
        // currentProject.getBasePath(); doc.toString();
        String str1 = doc.toString();
        String str2 = currentProject.getBasePath();
        int beg_idx = str1.indexOf(str2) + str2.length() + 1;
        int end_idx = str1.length() - 1;
        String filePath = str1.subSequence(beg_idx, end_idx).toString();
        String pathname = str2 + "/.git";
        String cur_file = doc.getText();
        // update_method
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(mEditor, currentProject);
        int bias = 0;
        for (PsiElement psiElement : psiFile.getChildren()) {
            if (psiElement.toString().indexOf("PsiClass") != -1) {
                for (PsiElement elem : psiElement.getChildren()) {
                    if (elem.toString().indexOf("PsiMethod") != -1 ) {
                        boolean flag = false;
                        int CommentStart = -1;
                        int CommentEnd = -1;
                        int pre_len = 0;
                        String comment = "";
                        for (PsiElement tmpEle : elem.getChildren()) {
                            if (tmpEle instanceof PsiComment) {
                                flag = true;
                                CommentStart = tmpEle.getTextOffset() + bias;
                                CommentEnd = CommentStart + tmpEle.getTextLength();
                                pre_len = tmpEle.getTextLength();
                                comment = tmpEle.getText();
                                break;
                            }
                        }
                        if(flag == true){
                            String update_method = elem.getText();
                            update_method.replace(comment,"");
                            GitUtil gitUtil = new GitUtil();
                            List<String> ls = gitUtil.get_code_comment(pathname, filePath, update_method, cur_file);
                            //if the method is not changed, then there is no need to update the comment !!!!
                            if (ls.get(0).equals(ls.get(1))) continue;
                            CommentUtil comment_util = new CommentUtil();
                            String comments = comment_util.get_comment(ls.get(2));
                            String update_comment;
                            try {
                                Properties props = new Properties();
                                props.load(this.getClass().getResourceAsStream("/server.properties"));
                                String serverAddr = props.getProperty("server-address");
                                MHttpClient client = new MHttpClient();
                                update_comment = client.post("http://" + serverAddr + ":5000/test", ls.get(0), ls.get(1), comments);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Messages.showMessageDialog("Failed to connect to server.", "Information", Messages.getInformationIcon());
                                return;
                            }
                            int update_len = replace(update_comment,currentProject,  psiFile,doc,CommentStart,CommentEnd);
                            if(update_len!=-1){
                                bias = update_len - pre_len;
                            }
                        }
                    }
                }
            }
        }

//        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(mEditor, currentProject);
//        show(update_comment,currentProject, mEditor, psiFile,doc );
    }
    public int replace(String update_comment, Project currentProject, PsiFile psiFile, Document doc,int CommentStart, int CommentEnd){
        int line = 0;
        int lineOffset = 0;
        int ans_len = 0;
        for(int i = 0;i<doc.getLineCount();i++){
            int tmpOffset = doc.getLineStartOffset(i);
            if(tmpOffset <= CommentStart){
                line = i;
                lineOffset = tmpOffset;
            }else break;
        }
        int lineEndOffset = doc.getLineEndOffset(line);
        String s = doc.getText(new TextRange(lineOffset, lineEndOffset));
        int spaceNum = 0;
        for(int i=0;i<s.length();i++){
            if(s.charAt(i) == ' ') spaceNum++;
            else break;
        }
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<spaceNum; i++) sb.append(" ");
        String[] comments = update_comment.split("\n");
        String ans = "/**\n";
        for (int i = 0; i < comments.length; i++) {
            ans = ans + sb + "* " + comments[i] + "\n";
        }
        ans = ans +sb + "*/";
        String ans_comment = ans;
        int start = CommentStart;
        int end = CommentEnd;
        ans_len = ans_comment.length();
        if(psiFile.getFileType().isReadOnly() ) return -1;
        WriteCommandAction.runWriteCommandAction(currentProject, new Runnable() {
            @Override
            public void run() {
                doc.deleteString(start, end);
                doc.insertString(start, ans_comment);
            }
        });
        return ans_len;
    }
}

