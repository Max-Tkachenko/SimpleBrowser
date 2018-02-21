package max.example.com.quickurl.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import max.example.com.quickurl.model.GroupItem;
import max.example.com.quickurl.model.LinkItem;

public class GetData {

    private DBHelper dbHelper;
    private SQLiteDatabase database;
    private List<LinkItem> linksFromDB;
    private List<GroupItem> groupsFromDB;

    public GetData(Context context) {
        this.dbHelper = new DBHelper(context);
        database = dbHelper.getWritableDatabase();
        linksFromDB = new ArrayList<>();
        groupsFromDB = new ArrayList<>();
        sortData();
    }

    public void addGroup(GroupItem groupItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_GROUP_ID, groupItem.getGroupId());
        contentValues.put(DBHelper.KEY_GROUP_NAME, groupItem.getName());
        database.insert(DBHelper.TABLE_GROUPS, null, contentValues);
        sortData();
    }

    public void addLink(LinkItem linkItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_OWNER_ID, linkItem.getGroupItemId());
        contentValues.put(DBHelper.KEY_LINK_NAME, linkItem.getName());
        contentValues.put(DBHelper.KEY_URL, linkItem.getUrl());
        database.insert(DBHelper.TABLE_LINKS, null, contentValues);
        sortData();
    }

    public void deleteLink(String name) {
        database.delete(DBHelper.TABLE_LINKS,DBHelper.KEY_LINK_NAME + "= ?", new String[] {name});
        sortData();
    }

    public void deleteGroup(GroupItem groupItem) {
        for(LinkItem item : groupItem.getLinks()) {
            database.delete(DBHelper.TABLE_LINKS,DBHelper.KEY_LINK_NAME + "= ?", new String[] {item.getName()});
        }
        database.delete(DBHelper.TABLE_GROUPS,DBHelper.KEY_GROUP_NAME + "= ?", new String[] {groupItem.getName()});
        sortData();
    }

    public void clearGroup(GroupItem groupItem) {
        for(LinkItem item : groupItem.getLinks()) {
            database.delete(DBHelper.TABLE_LINKS,DBHelper.KEY_LINK_NAME + "= ?", new String[] {item.getName()});
        }
        sortData();
    }

    public void updateLink(String oldName, String newName, String newURL) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_LINK_NAME, newName);
        contentValues.put(DBHelper.KEY_URL, newURL);
        database.update(DBHelper.TABLE_LINKS, contentValues, DBHelper.KEY_LINK_NAME + "= ?", new String[] {oldName});
        sortData();
    }

    public void updateGroup(String oldName, String newName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.KEY_GROUP_NAME, newName);
        database.update(DBHelper.TABLE_GROUPS, contentValues, DBHelper.KEY_GROUP_NAME + "= ?", new String[] {oldName});
        sortData();
    }

    public void getDataFromDB() {
        linksFromDB.clear();
        groupsFromDB.clear();
        Cursor linkCursor = database.query(DBHelper.TABLE_LINKS, null, null, null, null, null, null);
        if (linkCursor.moveToFirst()) {
            int linkOwnerId = linkCursor.getColumnIndex(DBHelper.KEY_OWNER_ID);
            int linkName = linkCursor.getColumnIndex(DBHelper.KEY_LINK_NAME);
            int linkURL = linkCursor.getColumnIndex(DBHelper.KEY_URL);
            do {
                linksFromDB.add(new LinkItem(linkCursor.getString(linkName),
                        linkCursor.getString(linkURL), linkCursor.getInt(linkOwnerId)));
            } while (linkCursor.moveToNext());
        }
        linkCursor.close();

        Cursor groupCursor = database.query(DBHelper.TABLE_GROUPS, null, null, null, null, null, null);
        if (groupCursor.moveToFirst()) {
            int linkGroupId = groupCursor.getColumnIndex(DBHelper.KEY_GROUP_ID);
            int linkName = groupCursor.getColumnIndex(DBHelper.KEY_GROUP_NAME);
            do {
                groupsFromDB.add(new GroupItem(groupCursor.getInt(linkGroupId),
                        groupCursor.getString(linkName)));
            } while (groupCursor.moveToNext());
        }
        groupCursor.close();

        for (GroupItem group : groupsFromDB) {
            for (LinkItem link : linksFromDB) {
                if (link.getGroupItemId() == group.getGroupId()) {
                    group.addLink(link);
                }
            }
        }
    }

    public void sortData() {
        getDataFromDB();
        int index = 0;

        for(GroupItem group : groupsFromDB) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.KEY_GROUP_ID, index);
            database.update(DBHelper.TABLE_GROUPS, contentValues, DBHelper.KEY_GROUP_NAME + "= ?", new String[] {group.getName()});
            for (LinkItem link : linksFromDB) {
                if (link.getGroupItemId() == group.getGroupId()) {
                    ContentValues values = new ContentValues();
                    values.put(DBHelper.KEY_OWNER_ID, index);
                    database.update(DBHelper.TABLE_LINKS, values, DBHelper.KEY_LINK_NAME + "= ?", new String[]{link.getName()});
                }
            }
            index++;
        }
        getDataFromDB();
    }

    public List<LinkItem> getLinksFromDB() {
        sortData();
        return linksFromDB;
    }

    public List<GroupItem> getGroupsFromDB() {
        sortData();
        return groupsFromDB;
    }

    public DBHelper getDbHelper() {
        return dbHelper;
    }
}
