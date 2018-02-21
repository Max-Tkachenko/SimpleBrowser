package max.example.com.quickurl.model;

import java.util.ArrayList;
import java.util.List;

public class GroupItem {

    private int groupId;
    private String name;
    private List<LinkItem> links;

    public GroupItem(int groupId, String name) {
        this.groupId = groupId;
        this.name = name;
        links = new ArrayList<>();
    }

    public int getGroupId() {
        return groupId;
    }

    public String getName() {
        return name;
    }

    public List<LinkItem> getLinks() {
        return links;
    }

    public void addLink(LinkItem linkItem) {
        links.add(linkItem);
    }
}