package cn.liberg.coder.tool;

import cn.liberg.coder.tool.java.JClassEntity;
import cn.liberg.coder.tool.java.JInterface;

public interface ILibergToolCallback {
    public ILibergToolCallback DefalutImpl = new ILibergToolCallback() {
        @Override
        public void onStart() {

        }

        @Override
        public void onAddEntity(JClassEntity entity) {

        }

        @Override
        public void onAddInterface(JInterface jInterface) {

        }

        @Override
        public void onApiDocumentCreated(String documentPath) {

        }

        @Override
        public void onEnd() {

        }
    };

    public void onStart();

    public void onAddEntity(JClassEntity entity);

    public void onAddInterface(JInterface jInterface);

    public void onApiDocumentCreated(String documentPath);

    public void onEnd();
}
