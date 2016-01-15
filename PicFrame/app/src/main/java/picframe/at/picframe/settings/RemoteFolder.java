package picframe.at.picframe.settings;

import java.util.LinkedList;

public class RemoteFolder {
    public String name;
    public RemoteFolder parent;
    public LinkedList<RemoteFolder> children;
}