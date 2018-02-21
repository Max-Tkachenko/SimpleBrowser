package max.example.com.quickurl.adapters;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableWebView;

import java.util.List;

import max.example.com.quickurl.R;
import max.example.com.quickurl.database.GetData;
import max.example.com.quickurl.model.AppWebViewClient;
import max.example.com.quickurl.model.GroupItem;
import max.example.com.quickurl.model.LinkItem;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private static final int GROUP_ITEM_RESOURCE = R.layout.group_item_view;
    private static final int CHILD_ITEM_RESOURCE = R.layout.child_item_view;

    public Context context;
    private Activity activity;
    private LayoutInflater inflater;
    private ObservableWebView mainWebView;
    private ObservableWebView checkWebView;
    private TabHost tabHost;
    private ActionBar actionBar;
    private FloatingActionButton refresh, home, open, scroll;

    private RelativeLayout noLoad;
    private RelativeLayout noInternet;

    private GetData dataClass;
    private List<LinkItem> linksFromDB;
    private List<GroupItem> groupsFromDB;

    private String oldLinkName;
    private String oldGroupName;
    private String newLinkName;
    private String newLinkURL;
    private String newGroupName;

    private Snackbar linkSnackbar;
    private Snackbar folderSnackbar;

    public MyExpandableListAdapter(Context ctx, Activity parentActivity,
                                   ObservableWebView parentMainWebView, ObservableWebView parentCheckWebView,
                                   TabHost parentTabHost, ActionBar parActionBar, FloatingActionButton parOpen,
                                   FloatingActionButton parHome, FloatingActionButton parRefresh,
                                   FloatingActionButton parScroll) {
        context = ctx;
        activity = parentActivity;
        mainWebView = parentMainWebView;
        checkWebView = parentCheckWebView;
        tabHost = parentTabHost;
        actionBar = parActionBar;
        open = parOpen; home = parHome; refresh = parRefresh; scroll = parScroll;
        linkSnackbar = Snackbar.make(parentActivity.findViewById(android.R.id.content), "", 1);
        folderSnackbar = Snackbar.make(parentActivity.findViewById(android.R.id.content), "", 1);
        noLoad = activity.findViewById(R.id.no_load_layout);
        noInternet = activity.findViewById(R.id.no_internet_layout);
        noLoad.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        dataClass = new GetData(context);
        linksFromDB = dataClass.getLinksFromDB();
        groupsFromDB = dataClass.getGroupsFromDB();
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateData() {
        linksFromDB.clear();
        groupsFromDB.clear();
        linksFromDB = dataClass.getLinksFromDB();
        groupsFromDB = dataClass.getGroupsFromDB();
        notifyDataSetChanged();
    }

    //region GET VIEWS
    public View getChildView(final int groupPosition, final int childPosition, final boolean isLastChild,
                             final View convertView, ViewGroup parent) {
        View childView = convertView;
        final LinkItem child = getChild(groupPosition, childPosition);
        if (child != null) {
            childView = inflater.inflate(CHILD_ITEM_RESOURCE, null);

            ChildViewHolder holder = new ChildViewHolder(childView);
            holder.text.setText(child.getName());
            holder.imageview.setImageResource(R.drawable.star_small);
        }

        ImageView crossImage = childView.findViewById(R.id.delete_link);
        crossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Delete a link");
                alert.setMessage("Are you sure to remove this link?");
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dataClass.deleteLink(child.getName());
                        updateData();
                        notifyDataSetChanged();
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alert.show();
            }
        });
        childView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.setHeaderTitle("What would you like to do?");
                contextMenu.add("Copy URL").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("URL", child.getUrl());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                contextMenu.add("Edit link item").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        for(int i = 0; i < linksFromDB.size(); i++) {
                            if(linksFromDB.get(i).getName().equals(child.getName())
                                    && linksFromDB.get(i).getUrl().equals(child.getUrl())) {
                                oldLinkName = linksFromDB.get(i).getName();
                                break;
                            }
                        }
                        showEditLinkDialog(child.getName(), child.getUrl());
                        return true;
                    }
                });
            }
        });
        childView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tabHost.setCurrentTab(1);
                loadLocalUrl(isOnline(context), child.getUrl());
            }
        });
        return childView;
    }
    public View getGroupView(final int groupPosition, boolean isExpanded, final View convertView, ViewGroup parent) {
        View groupView = convertView;
        final GroupItem group = getGroup(groupPosition);
        if (group != null) {
            groupView = inflater.inflate(GROUP_ITEM_RESOURCE, null);

            GroupViewHolder holder = new GroupViewHolder(groupView);
            holder.text.setText(group.getName());
            if(isExpanded)
                holder.imageview.setImageResource(R.drawable.open_folder_small);
            else
                holder.imageview.setImageResource(R.drawable.close_folder_small);
        }

        final ImageView imageView = groupView.findViewById(R.id.edit_group_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(imageView, group);
            }
        });
        return groupView;
    }
    private void showPopupMenu(View view, final GroupItem group) {
        PopupMenu popup = new PopupMenu(context, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.edit_group_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.rename_menu:
                        oldGroupName = group.getName();
                        showEditFolderDialog(group.getName());
                        break;
                    case R.id.clear_menu:
                        dataClass.clearGroup(group);
                        updateData();
                        notifyDataSetChanged();
                        break;
                    case R.id.delete_menu:
                        dataClass.deleteGroup(group);
                        updateData();
                        notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popup.show();
    }
    //endregion

    //region EDIT LINK ACTIONS
    private void showEditLinkDialog(String name, String url) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        View view = inflater.inflate(R.layout.edit_link, null);
        final EditText linkName = view.findViewById(R.id.edit_child_name);
        linkName.setText(name);
        linkName.setSelection(name.length());
        final EditText linkURL = view.findViewById(R.id.edit_child_url);
        linkURL.setText(url);
        linkURL.setSelection(url.length());
        builder.setTitle("Edit link dialog");
        builder.setView(view);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editName = linkName.getText().toString();
                String editURL = linkURL.getText().toString();
                if(editName.length() == 0 || editURL.length() == 0) {
                    newLinkName = editName;
                    newLinkURL = editURL;
                    dialog.dismiss();
                    showLinkSnackbar("Some fields are empty!", "Try again",
                            newLinkName, newLinkURL, 5000);
                }
                else if(editName.length() > 20) {
                    newLinkName = editName;
                    newLinkURL = editURL;
                    dialog.dismiss();
                    showLinkSnackbar("String \"Name\" is too long!", "Try again",
                            newLinkName, newLinkURL, 5000);
                }
                else {
                    if (!editURL.contains("http")) {
                        editURL = "http://" + editURL;
                    }
                    dataClass.updateLink(oldLinkName, editName, editURL);
                    updateData();
                    dialog.dismiss();
                    showLinkSnackbar("Link has been successfully updated!", "",
                            "", "",1500);
                }
            }
        });
    }
    private void showLinkSnackbar(String text, String actionText,
                                  final String linkName, final String linkUrl, int duration) {
        linkSnackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, duration);
        linkSnackbar.setAction(actionText, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditLinkDialog(linkName, linkUrl);
            }
        });
        linkSnackbar.setActionTextColor(activity.getResources().getColor(R.color.colorAccent));
        open.hide();
        home.hide();
        refresh.hide();
        scroll.hide();
        linkSnackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                open.show();
                home.show();
                refresh.show();
                scroll.show();
            }
        });
        linkSnackbar.show();
    }
    //endregion

    //region EDIT FOLDER ACTIONS
    private void showEditFolderDialog(String name) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        View view = inflater.inflate(R.layout.add_new_folder, null);
        final EditText nameText = view.findViewById(R.id.edit_child_name);
        nameText.setText(name);
        nameText.setSelection(name.length());
        builder.setTitle("Edit folder dialog");
        builder.setView(view);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        final android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editName = nameText.getText().toString();
                if (editName.length() == 0) {
                    newGroupName = editName;
                    dialog.dismiss();
                    showFolderSnackbar("Field \"Name\" is empty!", "Try again", "", 5000);
                } else if (editName.length() > 18) {
                    newGroupName = editName;
                    dialog.dismiss();
                    showFolderSnackbar("String \"Name\" is too long!", "Try again", newGroupName, 5000);
                } else if (isGroupInList(groupsFromDB,
                        new GroupItem(groupsFromDB.size(), editName))) {
                    newGroupName = editName;
                    dialog.dismiss();
                    showFolderSnackbar("Folder has a duplicate name!", "Try again", newGroupName, 5000);
                } else {
                    dataClass.updateGroup(oldGroupName, editName);
                    updateData();
                    dialog.dismiss();
                    showFolderSnackbar("Folder has been successfully updated!", "",
                            "",1500);
                }
            }
        });
    }
    private void showFolderSnackbar(String text, String actionText, final String folderName, int duration) {
        folderSnackbar = Snackbar.make(activity.findViewById(android.R.id.content), text, duration);
        folderSnackbar.setAction(actionText, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditFolderDialog(folderName);
            }
        });
        folderSnackbar.setActionTextColor(activity.getResources().getColor(R.color.colorAccent));
        open.hide();
        home.hide();
        refresh.hide();
        scroll.hide();
        folderSnackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                open.show();
                home.show();
                refresh.show();
                scroll.show();
            }
        });
        folderSnackbar.show();
    }
    private boolean isGroupInList(List<GroupItem> groupItems, GroupItem item) {
        for(GroupItem gr : groupItems) {
            if(gr.getName().equals(item.getName())) {
                return true;
            }
        }
        return false;
    }
    //endregion

    //region OTHER METHODS
    public Snackbar getAdapterLinkSnackbar() {
        return this.linkSnackbar;
    }
    public Snackbar getAdapterFolderSnackbar() {
        return this.folderSnackbar;
    }
    public LinkItem getChild(int groupPosition, int childPosition) {
        return groupsFromDB.get(groupPosition).getLinks().get(childPosition);
    }
    public GroupItem getGroup(int groupPosition) {
        return groupsFromDB.get(groupPosition);
    }
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }
    public int getChildrenCount(int groupPosition) {
        return groupsFromDB.get(groupPosition).getLinks().size();
    }
    public int getGroupCount() {
        return groupsFromDB.size();
    }
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    public boolean hasStableIds() {
        return true;
    }
    //endregion

    //region NETWORK CHECK ACTIONS
    private void loadLocalUrl(boolean network, String url) {
        if (network) {
            checkWebView.setWebViewClient(new AppWebViewClient() {

                private boolean isURLcorrect;

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    isURLcorrect = true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if(isURLcorrect) {
                        mainWebView.setWebViewClient(new WebViewClient());
                        mainWebView.loadUrl(url);
                        mainWebView.setVisibility(View.VISIBLE);
                    }
                    else {
                        mainWebView.setVisibility(View.GONE);
                        mainWebView.loadUrl("about:blank");
                        showNoLoad();
                    }
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    isURLcorrect = false;
                }
            });

            checkWebView.loadUrl(url);
        } else {
            mainWebView.setVisibility(View.GONE);
            showNoConnecting();
        }
    }
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
    //endregion

    //region RUNNABLE
    private void showNoConnecting() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    activity.runOnUiThread(visibleNoConnecting);
                    sleep(2000);
                    activity.runOnUiThread(unVisibleNoConnecting);
                } catch (InterruptedException ex) { }

            }
        };
        thread.start();
    }
    private void showNoLoad() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    activity.runOnUiThread(visibleNoLoad);
                    sleep(2500);
                    activity.runOnUiThread(unVisibleNoLoad);
                } catch (InterruptedException ex) { }

            }
        };
        thread.start();
    }

    private Runnable visibleNoLoad = new Runnable() {
        public void run() {
            open.hide();
            home.hide();
            refresh.hide();
            scroll.hide();
            actionBar.hide();
            noLoad.setVisibility(View.VISIBLE);
        }
    };
    private Runnable unVisibleNoLoad = new Runnable() {
        public void run() {
            noLoad.setVisibility(View.GONE);
            actionBar.show();
            open.show();
            home.show();
            refresh.show();
            scroll.show();
        }
    };
    private Runnable visibleNoConnecting = new Runnable() {
        @Override
        public void run() {
            open.hide();
            home.hide();
            refresh.hide();
            scroll.hide();
            actionBar.hide();
            noInternet.setVisibility(View.VISIBLE);
        }
    };
    private Runnable unVisibleNoConnecting = new Runnable() {
        @Override
        public void run() {
            noInternet.setVisibility(View.GONE);
            actionBar.show();
            open.show();
            home.show();
            refresh.show();
            scroll.show();
        }
    };
    //endregion
}