package max.example.com.quickurl.model;

public class LinkItem {

    private int groupItemId;
    private String name;
    private String url;

    public LinkItem(String name, String url, int groupItemId) {
        this.name = name;
        this.url = url;
        this.groupItemId = groupItemId;
    }

    public int getGroupItemId() {
        return groupItemId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}