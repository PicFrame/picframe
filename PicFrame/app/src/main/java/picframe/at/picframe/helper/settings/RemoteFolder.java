package picframe.at.picframe.helper.settings;

import java.util.LinkedList;

public class RemoteFolder {
    public String name;
    public RemoteFolder parent;
    public LinkedList<RemoteFolder> children;
}