package com.sddev.botmaker;

import java.awt.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class Record {

    private ArrayList<Action> actions;

    public Record() {
        actions = new ArrayList<>();
    }

    public String generatePythonCode() {
        StringJoiner joiner = new StringJoiner("\n");
        for(Action action : actions)
            joiner.add(action.getPythonCode());
        return joiner.toString();
    }

    public void trimToSize() {
        actions.trimToSize();
    }

    public void ensureCapacity(int minCapacity) {
        actions.ensureCapacity(minCapacity);
    }

    public int size() {
        return actions.size();
    }

    public boolean isEmpty() {
        return actions.isEmpty();
    }

    public boolean contains(Object o) {
        return actions.contains(o);
    }

    public int indexOf(Object o) {
        return actions.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return actions.lastIndexOf(o);
    }

    public Object[] toArray() {
        return actions.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return actions.toArray(a);
    }

    public Action get(int index) {
        return actions.get(index);
    }

    public Action set(int index, Action element) {
        return actions.set(index, element);
    }

    public boolean add(Action action) {
        return actions.add(action);
    }

    public void add(int index, Action element) {
        actions.add(index, element);
    }

    public Action remove(int index) {
        return actions.remove(index);
    }

    public boolean remove(Object o) {
        return actions.remove(o);
    }

    public void clear() {
        actions.clear();
    }

    public boolean addAll(Collection<? extends Action> c) {
        return actions.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends Action> c) {
        return actions.addAll(index, c);
    }

    public boolean removeAll(Collection<?> c) {
        return actions.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return actions.retainAll(c);
    }

    public ListIterator<Action> listIterator(int index) {
        return actions.listIterator(index);
    }

    public ListIterator<Action> listIterator() {
        return actions.listIterator();
    }

    public Iterator<Action> iterator() {
        return actions.iterator();
    }

    public java.util.List<Action> subList(int fromIndex, int toIndex) {
        return actions.subList(fromIndex, toIndex);
    }

    public void forEach(Consumer<? super Action> action) {
        actions.forEach(action);
    }

    public Spliterator<Action> spliterator() {
        return actions.spliterator();
    }

    public boolean removeIf(Predicate<? super Action> filter) {
        return actions.removeIf(filter);
    }

    public void replaceAll(UnaryOperator<Action> operator) {
        actions.replaceAll(operator);
    }

    public void sort(Comparator<? super Action> c) {
        actions.sort(c);
    }

    public boolean containsAll(Collection<?> c) {
        return actions.containsAll(c);
    }

    public Stream<Action> stream() {
        return actions.stream();
    }

    public Stream<Action> parallelStream() {
        return actions.parallelStream();
    }

    public static interface Action {
        void execute(Robot robot);
        String getPythonCode();
    }

    public static class SleepAction implements Action {
        long length;

        public SleepAction(long length) {
            this.length = length;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        @Override
        public void execute(Robot robot) {
            try {
                Thread.sleep(length);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public String getPythonCode() {
            return "Bot.sleep(" + length + ")";
        }
    }

    public static enum MouseActionType {press, release}

    public static class MouseAction implements Action {
        int x, y, button;
        MouseActionType type;

        public MouseAction(int x, int y, int button, MouseActionType type) {
            this.x = x;
            this.y = y;
            this.button = button;
            this.type = type;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getButton() {
            return button;
        }

        public void setButton(int button) {
            this.button = button;
        }

        public MouseActionType getType() {
            return type;
        }

        public void setType(MouseActionType type) {
            this.type = type;
        }

        @Override
        public void execute(Robot robot) {
            robot.mouseMove(x, y);
            if(type == MouseActionType.press) {
                robot.mousePress(button);
            } else if(type == MouseActionType.release) {
                robot.mouseRelease(button);
            }
        }

        @Override
        public String getPythonCode() {
            return "Bot.mouse_move(" + x +", " + y + ")\n" + (type == MouseActionType.press ? "Bot.mouse_press(" + button + ")" : "Bot.mouse_release(" + button + ")");
        }
    }

    public static enum KeyboardActionType {press, release}

    public static class KeyboardAction implements Action {
        int code;
        KeyboardActionType type;

        public KeyboardAction(int code, KeyboardActionType type) {
            this.code = code;
            this.type = type;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public KeyboardActionType getType() {
            return type;
        }

        public void setType(KeyboardActionType type) {
            this.type = type;
        }

        @Override
        public void execute(Robot robot) {
            if(type == KeyboardActionType.press) {
                robot.keyPress(code);
            } else if(type == KeyboardActionType.release) {
                robot.keyRelease(code);
            }
        }

        @Override
        public String getPythonCode() {
            return (type == KeyboardActionType.press ? "Bot.key_press(" + code + ")" : "Bot.key_release(" + code + ")");
        }
    }

}
