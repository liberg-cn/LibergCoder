package cn.liberg.coder.intellij;

import cn.liberg.coder.tool.LibergTool;
import cn.liberg.coder.tool.LibergToolContext;
import cn.liberg.coder.tool.Config;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BuildAction extends AnAction {
    IJNotifier notifier;
    Project project;
    Editor editor;
    PsiContext psiContext;
    PsiClass currClass;

    List<String> entityList;
    List<String> interfaceList;

    @Override
    public void actionPerformed(AnActionEvent event) {
        project = event.getData(PlatformDataKeys.PROJECT);
        if (null == project) {
            return;
        }

        notifier = IJNotifier.of(project);
        editor = event.getData(CommonDataKeys.EDITOR);
        if (null == editor) {
            showWarningTip();
            return;
        }

        Config config = Config.loadFrom(project.getBasePath());
        if(config == null) {
            notifier.warning("LibergCoder: nothing todo.<br/>" +
                    "    Please open the <i style=\"color:#e64d52;\">@SpringBootApplication</i> class, <br/>" +
                    "    and run <i style=\"color:#e64d52;\">LibergCoder->Initialize</i> first.");
            return;
        }

        psiContext = new PsiContext(project, editor);
        currClass = psiContext.getCurrClass();
        if(null == currClass) {
            showWarningTip();
            return;
        }

        entityList = new ArrayList<>();
        interfaceList = new ArrayList<>();

        try {
            String basePackage = null;
            String currPackage = psiContext.packageName;
            if (currClass.isInterface()) {
                String interfacesPackage = config.get(Config.keyInterfacesPackage);
                if(currPackage.endsWith(interfacesPackage)) {
                    interfaceList.add(currClass.getName());
                    basePackage = currPackage.substring(0,
                                currPackage.length()-interfacesPackage.length()-1);
                } else {
                    notifier.warning("LibergCoder: nothing todo.<br/>" +
                            "    Interface class should be under package <i style=\"color:#e64d52;\">"+interfacesPackage+"</i>.");
                    return;
                }
            } else {
                String entityPackage = config.get(Config.keyEntityPackage);
                if(currPackage.endsWith(entityPackage)) {
                    entityList.add(currClass.getName());
                    basePackage = currPackage.substring(0,
                            currPackage.length()-entityPackage.length()-1);
                } else {
                    showWarningTip();
                    return;
                }
            }

            LibergToolContext ctx = new LibergToolContext(project.getName(),
                    psiContext.getProjectBasePath(), basePackage, config);
            LibergTool.doBuild(ctx, entityList, interfaceList);
            psiContext.reloadCurrFile();
            notifier.notify("LibergCoder: <br/>" +
                    "    <i style=\"color:#53b48b;\">build finished.</i>");
        } catch (Exception e) {
            notifier.error("LibergCoder: <br/>" +
                    "    <i style=\"color:#e64d52;\">build failed.</i>");
            
            e.printStackTrace();
        }
    }

    private void showWarningTip() {
        notifier.warning("LibergCoder: nothing todo.<br/>" +
                "    Please open the <i style=\"color:#e64d52;\">entity/interface</i> file first.");
    }

}
