package cn.liberg.coder.intellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class RebuildAllAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Messages.showMessageDialog("抱歉，暂未实现Rebuild All功能！", "知道了", Messages.getInformationIcon());
    }
}
