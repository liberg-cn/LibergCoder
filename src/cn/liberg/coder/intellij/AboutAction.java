package cn.liberg.coder.intellij;

import cn.liberg.coder.tool.Config;
import cn.liberg.coder.tool.LibergTool;
import cn.liberg.coder.tool.LibergToolContext;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AboutAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Messages.showMessageDialog(
                "version: " + LibergTool.getVersion(), "LibergCoder", Messages.getInformationIcon());
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        Presentation presentation = e.getPresentation();
        presentation.setText("v"+LibergTool.getVersion());
    }

}
