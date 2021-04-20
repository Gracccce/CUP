package Myplugin;
import com.intellij.lang.ASTNode;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.TextRange;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.ui.HyperlinkAdapter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.util.List;
import java.util.Properties;


public class Comment_update extends AnAction{
    /***
     * this is class;
     */

    public void show(String update_comment, Project currentProject, Editor mEditor, PsiFile psiFile, Document doc ){
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                String display = update_comment + "<br><a href=\"\">fix</a>";
                HyperlinkListener listener = new HyperlinkAdapter() {
                    @Override
                    protected void hyperlinkActivated(HyperlinkEvent e) {
                        SelectionModel selectionModel = mEditor.getSelectionModel();
                        int selectionStart = selectionModel.getSelectionStart();
                        int selectionEnd = selectionModel.getSelectionEnd();
                        String docString = doc.getText();
                        //!!!
                        String method_name = docString.substring(selectionStart, selectionEnd);
                        int pos = -1;
                        PsiComment targetComment = null;
                        int CommentStart = 0;
                        int CommentEnd = 0;
                        for (PsiElement psiElement : psiFile.getChildren()) {
                            if (psiElement.toString().indexOf("PsiClass") != -1) {
                                for (PsiElement elem : psiElement.getChildren()) {
                                    if (elem.toString().indexOf("PsiMethod") != -1 && elem.toString().indexOf(method_name) != -1) {
                                        for (PsiElement tmpEle : elem.getChildren()) {
                                            if (tmpEle instanceof PsiComment) {
//                                                tmpEle.delete();
                                                int tmpStart = tmpEle.getTextOffset();
                                                int tmpEnd = tmpStart + tmpEle.getTextLength();
                                                if(tmpStart < selectionStart && tmpStart > CommentStart ){
                                                    CommentStart = tmpStart;
                                                    CommentEnd = tmpEnd;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        int line = 0;
                        int lineOffset = 0;
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
                        if(psiFile.getFileType().isReadOnly() ) return;
                        WriteCommandAction.runWriteCommandAction(currentProject, new Runnable() {
                            @Override
                            public void run() {
                                doc.deleteString(start, end);
                                doc.insertString(start, ans_comment);
                            }
                        });

                    }

                };
                        Color bgColor = MessageType.INFO.getPopupBackground();
                        BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(display, null, bgColor, listener);
                        builder.setFadeoutTime(2000)
                                .setCloseButtonEnabled(true)
                                .setHideOnClickOutside(true)
                                .setHideOnAction(true)
                                .setAnimationCycle(200)
                                .createBalloon()
                                .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);

                }
        });
    }
    /***
     * this is class
     */
//    public void actionPerformed(AnActionEvent e ){
//        Project currentProject = e.getProject();
//        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
//        Document doc = mEditor.getDocument();
//        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(mEditor, currentProject);
//        if( psiFile.getFileType().isReadOnly()){
//            return;
//        }
//        String update_comment = "this is a method\n" +
//                "this is a test";
//        show(update_comment,currentProject, mEditor, psiFile,doc );
//    }

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
        PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement == null) return;
        ASTNode node = psiElement.getNode();
        String update_method = node.getText();
        //
//        Jgit_diff jd = new Jgit_diff();
//        List<String> ls = jd.get_code_comment(pathname, filePath, update_method);
        GitUtil gitUtil = new GitUtil();
        List<String> ls = gitUtil.get_code_comment(pathname, filePath, update_method, cur_file);
        Comment_util comment_util = new Comment_util();
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
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(mEditor, currentProject);
        show(update_comment,currentProject, mEditor, psiFile,doc );
    }
}

//    public void actionPerformed(AnActionEvent e ){
//        Project currentProject = e.getProject();
//        String update_comment = "this is a test\n" +
//                "this is a method!";
//        final Editor mEditor = e.getData(PlatformDataKeys.EDITOR);
////        ApplicationManager.getApplication().invokeLater(()->JBPopupFactory.getInstance()
////                .createHtmlTextBalloonBuilder(update_comment, Messages.getInformationIcon(),
////                        new JBColor(new Color(214, 241, 255), new Color(0, 10, 10)), null)
////                .setFadeoutTime(20000)
////                .setClickHandler(new ActionListener {})
////                .setHideOnClickOutside(true)
////                .createBalloon()
////                .show(JBPopupFactory.getInstance()
////                        .guessBestPopupLocation(mEditor), Balloon.Position.below));
//        ApplicationManager.getApplication().invokeLater(new Runnable() {
//            public void run() {
//                JBPopupFactory factory = JBPopupFactory.getInstance();
//                String display = update_comment + "<br><a href=\"\">Save changes and finish merging</a>";
//                HyperlinkListener listener = new HyperlinkAdapter() {
//                    @Override
//                    protected void hyperlinkActivated(HyperlinkEvent e) {
//                        Messages.showMessageDialog(currentProject,update_comment, "result",Messages.getInformationIcon());
////                        destroyChangedBlocks();
////                        myMergeContext.finishMerge(MergeResult.RESOLVED);
//                    }
//                };
//                BalloonBuilder builder = factory.createHtmlTextBalloonBuilder(display, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), listener);
//                builder.setFadeoutTime(20000)
//                        .setCloseButtonEnabled(true)
//                        .setHideOnClickOutside(true)
//                        .createBalloon()
//                        .show(factory.guessBestPopupLocation(mEditor), Balloon.Position.below);
//
//            }
//        });
//
//    }


//if (psiElement instanceof PsiComment) {
//        PsiComment psiComment = (PsiComment) psiElement;
//        int offset = psiComment.getTextOffset();
//        if (offset < selectionStart && offset > pos) {
//        targetComment = psiComment;
//        }
//        }
//        test += psiElement.getText();
//        test = test + "0\n"+psiElement.toString();
//                PsiClass psiClass = (PsiClass) psiElement;
//                                PsiMethod[] methods = psiClass.getMethods();
//                                for(PsiMethod psiMethod : methods){
//                                   for(PsiElement tmpEle : psiMethod.getChildren()){
//                                        if(tmpEle instanceof PsiComment){
//                                            PsiComment psiComment = (PsiComment) psiElement;
//                                            int offset = psiComment.getTextOffset();
//                                            if(offset<selectionStart && offset>pos ){
//                                                targetComment = psiComment;
//                                            }
//                                        }
//                                    }
//                                }
//                                test = psiElement.getText();
//    }
//                            test  =  test + psiElement.toString() + psiElement.getText()+'0';
//}
//                        String title;
//                        if(targetComment!=null){
//                            title = targetComment.getText();
//                        }else{
//                             title = "what";
//                        }
//                        Messages.showMessageDialog(test, title, Messages.getInformationIcon());
//                        targetComment.delete();
//        final PsiFile psiFile = PsiDocumentManager.getInstance(currentProject).getPsiFile(mEditor.getDocument());



