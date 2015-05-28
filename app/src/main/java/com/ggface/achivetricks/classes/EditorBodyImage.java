package com.ggface.achivetricks.classes;


import com.ggface.achivetricks.Units;

public class EditorBodyImage {

    public static EditorBodyImage getInstance(String url) {
        EditorBodyImage instance = new EditorBodyImage(0);
        instance.setPath(url);
        instance.setProgress(false);
        return instance;
    }

    private int index;
    private String path;
    private boolean isProgress;

    public EditorBodyImage() {
        path = Units.EMPTY;
        isProgress = false;
    }

    public EditorBodyImage(int index) {
        this.index = index;
        path = Units.EMPTY;
        isProgress = false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isProgress() {
        return isProgress;
    }

    public boolean isLocal() {
        return !isProgress && !path.startsWith("/data/img/") && !path.contains("http") ;
    }

    public void setProgress(boolean value) {
        this.isProgress = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int value) {
        this.index = value;
    }

    @Override
    public String toString() {
        return String.format("<img src=\"%s\" alt=\"\" class=\"\" />", path);
    }
}
