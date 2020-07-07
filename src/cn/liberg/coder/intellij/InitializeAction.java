package cn.liberg.coder.intellij;

import cn.liberg.coder.tool.Config;
import cn.liberg.coder.tool.LibergTool;
import cn.liberg.coder.tool.LibergToolContext;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

public class InitializeAction extends AnAction {
    IJNotifier notifier;
    Project project;
    Editor editor;
    PsiContext psiContext;
    PsiClass currClass;
    String projectBasePath;

    @Override
    public void actionPerformed(AnActionEvent event) {
        project = event.getData(PlatformDataKeys.PROJECT);
        if (null == project) {
            return;
        }

        projectBasePath = project.getBasePath();
        notifier = IJNotifier.of(project);
        editor = event.getData(CommonDataKeys.EDITOR);
        if (null == editor) {
            showWarningTip();
            return;
        }

        psiContext = new PsiContext(project, editor);
        currClass = psiContext.getCurrClass();
        if(null == currClass) {
            showWarningTip();
            return;
        }

        try {
            if(currClass.hasAnnotation("org.springframework.boot.autoconfigure.SpringBootApplication")) {
                LibergToolContext ctx = new LibergToolContext(project.getName(),
                        projectBasePath, psiContext.packageName,
                        Config.createOrLoad(projectBasePath));
                LibergTool.doInitialize(ctx);
                psiContext.reloadProject();
                notifier.notify("LibergCoder: <br/>" +
                        "    <i style=\"color:#53b48b;\">initializing finished.</i>");
            } else {
                showWarningTip();
            }
        } catch (Exception e) {
            notifier.error("LibergCoder: <br/>" +
                    "    <i style=\"color:#e64d52;\">initializing failed.</i>");
            e.printStackTrace();
        }
    }

    private void showWarningTip() {
        notifier.warning("LibergCoder: nothing todo.<br/>" +
                "    Please open the <i style=\"color:#e64d52;\">@SpringBootApplication</i> class first.");
    }

}
