package cn.liberg.coder.intellij;

import cn.liberg.coder.tool.util.Paths;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtil;

public class PsiContext {
    Project project;
    Editor editor;
    Module module;

    PsiManager psiManager;
    PsiDocumentManager psiDocumentManager;
    PsiFile psiFile;
    PsiDirectory containerDirectory;
    String packageName;
    private boolean parseOK = false;
    private VirtualFile[] rootDirectoryArray;
    private PsiClass[] currClassArray;
    private String projectBasePath;

    public PsiContext(Project project, Editor editor) {
        this.project = project;
        projectBasePath = Paths.unifyDirectory(project.getBasePath());
        System.out.println("Project: " + project.getName());
        this.editor = editor;
        psiManager = PsiManager.getInstance(project);
        psiDocumentManager = PsiDocumentManager.getInstance(project);
        psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
        System.out.println(psiFile.getFileType());
        System.out.println("fileName=" + psiFile.getName());

        rootDirectoryArray = ProjectRootManager.getInstance(project).getContentSourceRoots();
//        System.out.println("----------");
//        for(VirtualFile vf : rootDirectoryArray) {
//            System.out.println(vf.getUrl());
//        }
//        System.out.println("----------");

        if (psiFile!=null && (psiFile instanceof PsiJavaFile)) {
            PsiJavaFile javaFile = (PsiJavaFile) psiFile;
            PsiClass[] classes = javaFile.getClasses();

            if (classes.length > 0) {
                currClassArray = classes;
                containerDirectory = javaFile.getContainingDirectory();
                System.out.println(containerDirectory.getName());
                module = FileIndexFacade.getInstance(project).getModuleForFile(javaFile.getVirtualFile());
                packageName = PsiUtil.getPackageName(classes[0]);
                System.out.println("pkgName: " + packageName);
                parseOK = true;
            }
        }
    }

    public void reloadCurrFile() {
        if(psiFile!=null) {
            psiManager.reloadFromDisk(psiFile);
        }
    }

    public void reloadProject() {
        ProjectManager.getInstance().reloadProject(project);
    }

    public String getProjectBasePath() {
        return projectBasePath;
    }

    public String getEditFileName() {
        String fileName = psiFile.getName();
        return fileName;
    }

    public PsiClass getCurrClass() {
        if(currClassArray!=null && currClassArray.length>0) {
            return currClassArray[0];
        } else {
            return null;
        }
    }

    public void format(PsiElement psiElement) {
        CodeStyleManager.getInstance(project).reformat(psiElement);
    }

    public void uiCodes() {
//        Messages.showInputDialog(
//                project,
//                "What is your name?",
//                "Input your name",
//                Messages.getQuestionIcon());

//        Messages.showInfoMessage("Source roots for the " + projectName + " plugin:\n" + sourceRootsList,
//              "Project Properties");


//        Messages.showMessageDialog(
//                "Hello World !", "Information", Messages.getInformationIcon());
    }
}
