package cn.liberg.coder.intellij;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;

public class IJNotifier {
    private Project project = null;
    private final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Liberg Coder", NotificationDisplayType.BALLOON, true);

    private IJNotifier(Project project) {
        this.project = project;
    }

    public static IJNotifier of(Project project) {
        return new IJNotifier(project);
    }

    public Notification notify(String content, NotificationType type) {
        Notification notification = NOTIFICATION_GROUP.createNotification(content, NotificationType.INFORMATION);
        notification.notify(project);
        return notification;
    }

    public Notification notify(String content) {
        return notify(content, NotificationType.INFORMATION);
    }
    public Notification warning(String content) {
        return notify(content, NotificationType.WARNING);
    }
    public Notification error(String content) {
        return notify(content, NotificationType.ERROR);
    }
}
