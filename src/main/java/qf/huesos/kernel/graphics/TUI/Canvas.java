package qf.huesos.kernel.graphics.TUI;

import qf.huesos.kernel.Screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends TUIElement{
    List<TUIElement> elements = new ArrayList<>();

    public Canvas(Screen screen) {
        super(screen);
    }

    public void addElement(TUIElement element) {
        elements.add(element);
    }
    public void removeElement(int id) {
        elements.remove(id);
    }
    public void removeElement(TUIElement element) {
        elements.remove(element);
    }
    public TUIElement getElement(int id) {
        return elements.get(id);
    }
    public List<TUIElement> getElements() {
        return elements;
    }

    @Override
    public void draw() throws IOException {
        for (TUIElement element : elements) {
            element.draw();
        }
    }
}
