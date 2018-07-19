package models;

import android.graphics.Rect;
import android.view.View;

public class RectView {

    public View view;
    public Rect rect;

    public RectView(View view, Rect rect) {
        this.view = view;
        this.rect = rect;
    }
}
