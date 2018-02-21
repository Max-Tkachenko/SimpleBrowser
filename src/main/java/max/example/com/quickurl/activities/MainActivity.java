package max.example.com.quickurl.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import java.util.List;

import max.example.com.quickurl.R;
import max.example.com.quickurl.adapters.CustomSpinnerAdapter;
import max.example.com.quickurl.adapters.MyExpandableListAdapter;
import max.example.com.quickurl.database.GetData;
import max.example.com.quickurl.model.AppWebViewClient;
import max.example.com.quickurl.model.GroupItem;
import max.example.com.quickurl.model.LinkItem;

public class MainActivity extends AppCompatActivity implements TabHost.OnTabChangeListener, ObservableScrollViewCallbacks {

    private MyExpandableListAdapter expandableListAdapter;
    private ExpandableListView expandableListView;
    private ObservableWebView mainWebView, checkWebView;
    private FloatingActionButton refresh, scroll, home, open;

    public Context context;
    private ActionBar actionBar;
    private TabHost myTabHost;
    private Menu actionBarMenu;
    private MenuItem firstItem, secondItem;

    private RelativeLayout noLoad;
    private RelativeLayout noInternet;

    private GetData database;

    private String newLinkName;
    private String newLinkURL;
    private int newLinkGroupId;
    private String newFolderName;

    private String homePageURL;

    private Snackbar linkSnackbar, folderSnackbar, optionsMenuSnackbar;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.main_layout);
        homePageURL = "http://google.com";

        context = this;
        database = new GetData(context);
        initializeView();
    }

    // -20   +25 два последних для точек меню
    // -100   -65 для открытой папки на синих тонах

    //region ADD NEW LINK
    private void showAddLinkDialog(final boolean showUrl, final String title, String name, final String url, int folderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.add_new_link, null);
        final EditText linkName = view.findViewById(R.id.edit_child_name);
        linkName.setText(name);
        linkName.setSelection(name.length());
        final EditText linkURL = view.findViewById(R.id.edit_child_url);
        linkURL.setText(url);
        linkURL.setSelection(url.length());
        TextView tvURL = view.findViewById(R.id.text_add_url);
        if(!showUrl) {
            linkURL.setVisibility(View.GONE);
            tvURL.setVisibility(View.GONE);
        }
        final Spinner spinner_group = view.findViewById(R.id.spinner_group);
        database.getDataFromDB();
        spinner_group.setAdapter(new CustomSpinnerAdapter(context, database.getGroupsFromDB()));
        spinner_group.setSelection(folderId);
        builder.setTitle(title);
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
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinkItem linkItem;
                String editName = linkName.getText().toString();
                String editURL;
                if(showUrl)
                    editURL = linkURL.getText().toString();
                else
                    editURL = url;
                int editGroupId = spinner_group.getSelectedItemPosition();
                if (editName.length() == 0 || editURL.length() == 0) {
                    newLinkName = editName;
                    newLinkURL = editURL;
                    newLinkGroupId = editGroupId;
                    dialog.dismiss();
                    showLinkSnackbar("Some fields are empty!", "Try again", title,
                            newLinkName, newLinkURL, newLinkGroupId, 5000);
                } else if (editName.length() > 20) {
                    newLinkName = editName;
                    newLinkURL = editURL;
                    newLinkGroupId = editGroupId;
                    dialog.dismiss();
                    showLinkSnackbar("String \"Name\" is too long!", "Try again", title,
                            newLinkName, newLinkURL, newLinkGroupId, 5000);
                } else if (isLinkInList(database.getLinksFromDB(),
                            new LinkItem(editName, editURL, editGroupId))) {
                    newLinkName = editName;
                    newLinkURL = editURL;
                    newLinkGroupId = editGroupId;
                        dialog.dismiss();
                        showLinkSnackbar("Link has a duplicate name!", "Try again", title,
                                newLinkName, newLinkURL, newLinkGroupId,5000);
                } else {
                    if (editURL.contains("http")) {
                        linkItem = new LinkItem(editName, editURL, editGroupId);
                    } else {
                        linkItem = new LinkItem(editName, "http://" + editURL, editGroupId);
                    }
                    database.addLink(linkItem);
                    expandableListAdapter.updateData();
                    dialog.dismiss();
                    showLinkSnackbar("Successfully added new link!", "", title,
                            "", "", 0, 1500);
                }
            }
        });
    }
    private void showLinkSnackbar(String text, String actionText, final String title,
                                  final String linkName, final String linkUrl, final int folderId, int duration) {
        linkSnackbar = Snackbar.make(findViewById(android.R.id.content), text, duration);
        linkSnackbar.setAction(actionText, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddLinkDialog(true, title, linkName, linkUrl, folderId);
            }
        });
        linkSnackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
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
    private boolean isLinkInList(List<LinkItem> linkItems, LinkItem item) {
        for(LinkItem link : linkItems) {
            if(link.getName().equals(item.getName())) {
                return true;
            }
        }
        return false;
    }
    //endregion

    //region ADD NEW FOLDER
    private void showAddFolderDialog(String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = getLayoutInflater().inflate(R.layout.add_new_folder, null);
        final EditText nameText = view.findViewById(R.id.edit_child_name);
        nameText.setText(name);
        nameText.setSelection(name.length());
        builder.setTitle("Add a new folder");
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
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String editName = nameText.getText().toString();
                if (editName.length() == 0) {
                    newFolderName = editName;
                    dialog.dismiss();
                    showFolderSnackbar("Field \"Name\" is empty!", "Try again", "", 5000);
                } else if (editName.length() > 18) {
                    newFolderName = editName;
                    dialog.dismiss();
                    showFolderSnackbar("String \"Name\" is too long!", "Try again", newFolderName, 5000);
                } else if (isGroupInList(database.getGroupsFromDB(),
                        new GroupItem(database.getGroupsFromDB().size(), editName))) {
                    newFolderName = editName;
                    dialog.dismiss();
                    showFolderSnackbar("Folder has a duplicate name!", "Try again", newFolderName, 5000);
                }
                else {
                    database.getDataFromDB();
                    int id = database.getGroupsFromDB().size();
                    GroupItem groupItem = new GroupItem(id, editName);
                    database.addGroup(groupItem);
                    expandableListAdapter.updateData();
                    dialog.dismiss();
                    showFolderSnackbar("Successfully added new folder!", "", "", 1500);
                }
            }
        });
    }
    private void showFolderSnackbar(String text, String actionText, final String folderName, int duration) {
        folderSnackbar = Snackbar.make(findViewById(android.R.id.content), text, duration);
        folderSnackbar.setAction(actionText, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFolderDialog(folderName);
            }
        });
        folderSnackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
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

    //region CREATE CUSTOM TABS
    private void setupTab(final String tag, int layoutId) {
        View tabview = createTabView(myTabHost.getContext(), tag);
        TabHost.TabSpec setContent = myTabHost.newTabSpec(tag).setIndicator(tabview).setContent(layoutId);
        myTabHost.addTab(setContent);
    }
    private static View createTabView(final Context context, final String text) {
        View tabView = LayoutInflater.from(context).inflate(R.layout.tab_item, null);
        TextView tabText = tabView.findViewById(R.id.tabsText);
        tabText.setText(text);
        return tabView;
    }
    @Override
    public void onTabChanged(String tabTag) {
        actionBar.show();
        TextView tabText1, tabText2, tabText3;
        firstItem = actionBarMenu.findItem(R.id.first_item);
        secondItem = actionBarMenu.findItem(R.id.second_item);
        if (tabTag.equals("Bookmarks")) {
            tabText1 = myTabHost.getTabWidget().getChildTabViewAt(0).findViewById(R.id.tabsText);
            tabText2 = myTabHost.getTabWidget().getChildTabViewAt(1).findViewById(R.id.tabsText);
            tabText3 = myTabHost.getTabWidget().getChildTabViewAt(2).findViewById(R.id.tabsText);
            tabText1.setTextColor(getResources().getColor(R.color.colorWhite));
            tabText2.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));
            tabText3.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Storage");
            firstItem.setVisible(true);
            firstItem.setTitle("New link");
            firstItem.setIcon(R.drawable.addlink);
            secondItem.setVisible(true);
            secondItem.setTitle("New folder");
            secondItem.setIcon(R.drawable.addfolder);
        } else if (tabTag.equals("Web page")) {
            tabText1 = myTabHost.getTabWidget().getChildTabViewAt(0).findViewById(R.id.tabsText);
            tabText2 = myTabHost.getTabWidget().getChildTabViewAt(1).findViewById(R.id.tabsText);
            tabText3 = myTabHost.getTabWidget().getChildTabViewAt(2).findViewById(R.id.tabsText);
            tabText1.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));
            tabText2.setTextColor(getResources().getColor(R.color.colorWhite));
            tabText3.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));
            if(!linkSnackbar.isShown() && !folderSnackbar.isShown() && !optionsMenuSnackbar.isShown()
                    && !expandableListAdapter.getAdapterLinkSnackbar().isShown()
                    && !expandableListAdapter.getAdapterFolderSnackbar().isShown()) {
                scroll.show();
                refresh.show();
                home.show();
                open.show();
            }
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setTitle("Browser");
            firstItem.setVisible(true);
            firstItem.setTitle("Save");
            firstItem.setIcon(R.drawable.save);
            secondItem.setVisible(true);
            secondItem.setTitle("Share");
            secondItem.setIcon(R.drawable.share);
        } else if (tabTag.equals("Player")) {
            tabText1 = myTabHost.getTabWidget().getChildTabViewAt(0).findViewById(R.id.tabsText);
            tabText2 = myTabHost.getTabWidget().getChildTabViewAt(1).findViewById(R.id.tabsText);
            tabText3 = myTabHost.getTabWidget().getChildTabViewAt(2).findViewById(R.id.tabsText);
            tabText1.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));
            tabText2.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));
            tabText3.setTextColor(getResources().getColor(R.color.colorWhite));
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.player_icon);
            actionBar.setTitle("Enjoy life with music!");
            firstItem.setVisible(false);
            secondItem.setVisible(false);
        }
    }
    //endregion

    //region INITIALIZE VIEW ACTIONS
    private void initializeView() {
        actionBar = getSupportActionBar();
        actionBar.setTitle("Browser");
        myTabHost = findViewById(android.R.id.tabhost);
        myTabHost.setup();
        setupTab("Bookmarks", R.id.tab_list);
        setupTab("Web page", R.id.tab_web);
        setupTab("Player", R.id.tab_music);
        myTabHost.setCurrentTab(1);
        myTabHost.setOnTabChangedListener(this);
        TextView tabText1, tabText2;
        tabText1 = myTabHost.getTabWidget().getChildTabViewAt(0).findViewById(R.id.tabsText);
        tabText2 = myTabHost.getTabWidget().getChildTabViewAt(2).findViewById(R.id.tabsText);
        tabText1.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));
        tabText2.setTextColor(getResources().getColor(R.color.colorTabTextUnselected));

        noLoad = findViewById(R.id.no_load_layout);
        noInternet = findViewById(R.id.no_internet_layout);
        noLoad.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);

        mainWebView = findViewById(R.id.main_webview);
        mainWebView.getSettings().setJavaScriptEnabled(true);
        mainWebView.getSettings().setBuiltInZoomControls(true);
        mainWebView.getSettings().setDisplayZoomControls(false);
        mainWebView.setScrollViewCallbacks(this);
        mainWebView.setVisibility(View.GONE);

        //region CHECK URL WEBVIEW
        checkWebView = findViewById(R.id.check_webview);
        //endregion WE

        refresh = findViewById(R.id.refresh);
        scroll = findViewById(R.id.scroll);
        home = findViewById(R.id.home_button);
        open = findViewById(R.id.open_url_button);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mainWebView.getUrl();
                loadLocalUrl(isOnline(context), url);
            }
        });
        scroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionBar.show();
                open.show();
                home.show();
                refresh.show();
                mainWebView.scrollTo(0, 0);
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadLocalUrl(isOnline(context), homePageURL);
            }
        });
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder openLinkDialog = new AlertDialog.Builder(context);
                View viewForFialog = getLayoutInflater().inflate(R.layout.open_user_link, null);
                final EditText linkText = viewForFialog.findViewById(R.id.user_link);
                openLinkDialog.setView(viewForFialog);
                openLinkDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                openLinkDialog.setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String url = linkText.getText().toString();
                        if(url.contains("http"))
                            loadLocalUrl(isOnline(context), url);
                        else
                            loadLocalUrl(isOnline(context), "http://" + url);
                    }
                });
                openLinkDialog.show();
            }
        });

        if(!isOnline(context)) {
            open.hide();
            home.hide();
            refresh.hide();
            scroll.hide();
            loadLocalUrl(false, "");
        }
        else {
            loadLocalUrl(true, homePageURL);
        }

        expandableListAdapter = new MyExpandableListAdapter(context, this, mainWebView, checkWebView,
                myTabHost, actionBar, open, home, refresh, scroll);
        expandableListView = findViewById(R.id.expandable_list);
        expandableListView.setGroupIndicator(null);
        expandableListView.setAdapter(expandableListAdapter);

        linkSnackbar = Snackbar.make(findViewById(android.R.id.content), "", 1);
        folderSnackbar = Snackbar.make(findViewById(android.R.id.content), "", 1);
        optionsMenuSnackbar = Snackbar.make(findViewById(android.R.id.content), "", 1);
    }
    private void showPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.from_main_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.settings_item:
                        goToActivity(SettingsActivity.class);
                        break;
                    case R.id.manual_item:
                        Intent toManual = new Intent(context, ManualActivity.class);
                        toManual.putExtra("parent", "main");
                        startActivity(toManual);
                        break;
                    case R.id.support_item:
                        goToActivity(SupportActivity.class);
                        break;
                    case R.id.about_app_item:
                        goToActivity(InfoActivity.class);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        popup.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        actionBarMenu = menu;
        return true;
    }
    @Override
    public void onBackPressed() {
        if(mainWebView.canGoBack()) {
            mainWebView.setVisibility(View.VISIBLE);
            mainWebView.goBack();
        }
        else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("Are you sure to close application?");
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.super.onBackPressed();
                }
            });
            dialog.show();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.first_item:
                if (myTabHost.getCurrentTabTag().equals("Bookmarks")) {
                    database.sortData();
                    if(database.getGroupsFromDB().size() == 0) {
                        showOptionsMenuSnackbar("Add a new folder, please!");
                    }
                    else {
                        showAddLinkDialog(true, "Add a new link","", "", 0);
                    }
                } else if (myTabHost.getCurrentTabTag().equals("Web page")) {
                    if(database.getGroupsFromDB().size() == 0) {
                        showOptionsMenuSnackbar("Add a new folder, please!");
                    }
                    else {
                        try {
                            if (mainWebView.getUrl() != null && !mainWebView.getUrl().equals("about:blank")) {
                                showAddLinkDialog(false, "Save page", "", mainWebView.getUrl(), 0);
                            } else {
                                showOptionsMenuSnackbar("Nothing to save!");
                            }
                        } catch (Exception ex) {
                            showOptionsMenuSnackbar("Nothing to save!");
                        }
                    }
                }
                break;
            case R.id.second_item:
                if (myTabHost.getCurrentTabTag().equals("Bookmarks")) {
                    showAddFolderDialog("");
                } else if (myTabHost.getCurrentTabTag().equals("Web page")) {
                    try {
                        if(mainWebView.getUrl() != null && !mainWebView.getUrl().equals("about:blank")) {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("plain/text");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Link for you");
                            shareIntent.putExtra(Intent.EXTRA_TEXT, mainWebView.getUrl());
                            MainActivity.this.startActivity(Intent.createChooser(shareIntent, "Share link..."));
                        }
                        else {
                            showOptionsMenuSnackbar("Nothing to share!");
                        }
                    } catch (Exception ex) {
                        showOptionsMenuSnackbar("Nothing to share!");
                    }
                }
                break;
            case R.id.third_item:
                View menuItemView = findViewById(R.id.third_item);
                showPopupMenu(menuItemView);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showOptionsMenuSnackbar(String text) {
        optionsMenuSnackbar = Snackbar.make(findViewById(android.R.id.content), text, 1500);
        open.hide();
        home.hide();
        refresh.hide();
        scroll.hide();
        optionsMenuSnackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                open.show();
                home.show();
                refresh.show();
                scroll.show();
            }
        });
        optionsMenuSnackbar.show();
    }
    //endregion

    //region SCROLL ACTIONS
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }
    @Override
    public void onDownMotionEvent() {
    }
    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        if (scrollState == ScrollState.UP) {
            if (actionBar.isShowing()) {
                actionBar.hide();
                scroll.hide();
                refresh.hide();
                home.hide();
                open.hide();
            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!actionBar.isShowing()) {
                actionBar.show();
                scroll.show();
                refresh.show();
                home.show();
                open.show();
            }
        }
    }
    //endregion

    //region NETWORK CHECK ACTIONS
    public void loadLocalUrl(boolean network, final String url) {
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
    
    //region GO TO OTHER ACTIVITY ACTION
    private void goToActivity(Class c) {
        Intent intent = new Intent(this, c);
        startActivity(intent);
    }
    //endregion

    //region RUNNABLE
    private void showNoConnecting() {
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    runOnUiThread(visibleNoConnecting);
                    sleep(2000);
                    runOnUiThread(unVisibleNoConnecting);
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
                    runOnUiThread(visibleNoLoad);
                    sleep(2500);
                    runOnUiThread(unVisibleNoLoad);
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