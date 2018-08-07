// This file uses the Storage Access Framework to allow users to specify their storage location
// For KitKat, users have to choose the default storage location
// Lollipop+ can choose their storage location via OPEN_DOCUMENT_TREE
// Older devices can't now be supported as I'm moving to DocumentFile

package com.garethevans.church.opensongtablet;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BootUpCheck extends AppCompatActivity {

    // Declare helper classes:
    Preferences mPreferences;
    StorageAccess storageAccess;
    DocumentFile homeFolder;

    // Declare views
    ProgressBar progressBar;
    TextView progressText, version;
    Button chooseStorageButton, goToSongsButton, userGuideButton;
    LinearLayout storageLinearLayout, readUpdate, userGuideLinearLayout;
    RelativeLayout goToSongsRelativeLayout;
    Spinner appMode;
    Toolbar toolbar;
    // Declare variables
    String text="", tree, versionCode="";
    Uri treeUri;
    boolean foldersok, storageGranted, skiptoapp;
    int lastUsedVersion, thisVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the helper classes (preferences)
        mPreferences = new Preferences();
        storageAccess = new StorageAccess();

        // This will do one of 2 things - it will either show the splash screen or the welcome screen
        // To determine which one, we need to check the storage is set and is valid
        // The last version used must be the same or greater than the current app version

        // Load up all of the preferences and the user specified storage location if it exists
        //Preferences.loadPreferences();
        loadStoragePreference();

        homeFolder = storageAccess.getHomeFolder(BootUpCheck.this);

        // Check we have the required storage permission
        checkStoragePermission();

        // Determine the last used version and what version the app is now
        skiptoapp = versionCheck();

        if (checkStorageIsValid() && storageGranted && skiptoapp) {
            Log.d("d","Ready to go straight to the app");
            setContentView(R.layout.activity_logosplash);
            goToSongs();

        } else {
            setContentView(R.layout.boot_up_check);

            // Identify the views
            identifyViews();

            // Update the verion and storage
            showCurrentStorage(treeUri);
            version.setText(versionCode);

            // Set up the button actions
            setButtonActions();

            // Check our state of play (based on if location is set and valid)
            checkReadiness();

        }

    }

    void identifyViews() {
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        goToSongsButton = findViewById(R.id.goToSongsButton);
        chooseStorageButton = findViewById(R.id.chooseStorageButton);
        storageLinearLayout = findViewById(R.id.storageLinearLayout);
        goToSongsRelativeLayout = findViewById(R.id.goToSongsRelativeLayout);
        readUpdate = findViewById(R.id.readUpdate);
        version = findViewById(R.id.version);
        version.setText(versionCode);
        userGuideLinearLayout = findViewById(R.id.userGuideLinearLayout);
        userGuideButton = findViewById(R.id.userGuideButton);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        appMode = findViewById(R.id.appMode);
        // Set the 3 options
        ArrayList<String> appModes = new ArrayList<>();
        appModes.add(getString(R.string.performancemode));
        appModes.add(getString(R.string.stagemode));
        appModes.add(getString(R.string.presentermode));
        ArrayAdapter<String> aa = new ArrayAdapter<>(BootUpCheck.this,R.layout.my_spinner,appModes);
        appMode.setAdapter(aa);
        // Select the appropriate one
        switch (FullscreenActivity.whichMode) {
            case "Stage":
                appMode.setSelection(1);
                break;
            case "Presentation":
                appMode.setSelection(2);
                break;
            case "Performance":
            default:
                appMode.setSelection(0);
                break;
        }
    }
    void setButtonActions() {
        showLoadingBar(true);
        goToSongsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (appMode.getSelectedItemPosition()) {
                    case 0:
                    default:
                        FullscreenActivity.whichMode = "Performance";
                        break;

                    case 1:
                        FullscreenActivity.whichMode = "Stage";
                        break;

                    case 2:
                        FullscreenActivity.whichMode = "Presentation";
                        break;

                }
                goToSongs();
            }
        });
        chooseStorageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                chooseStorageLocation();
            }
        });
        readUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.opensongapp.com/latest-updates";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    Log.d("d", "Error showing activity");
                }
            }
        });
        userGuideLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.opensongapp.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    Log.d("d", "Error showing activity");
                }
            }
        });
        userGuideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://www.opensongapp.com";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(i);
                } catch (Exception e) {
                    Log.d("d", "Error showing activity");
                }
            }
        });
    }
    void pulseStartButton() {
        CustomAnimations ca = new CustomAnimations();
        ca.pulse(BootUpCheck.this, goToSongsButton);
    }
    void showLoadingBar(boolean clickable) {
        int progressvisibility;
        int visibility;

        if (clickable) {
            visibility = View.VISIBLE;
            progressvisibility = View.GONE;
            pulseStartButton();
        } else {
            visibility = View.INVISIBLE;
            progressvisibility = View.VISIBLE;
            goToSongsButton.clearAnimation();
        }

        // This bit disables the buttons, stops the animation on the start button
        progressBar.setVisibility(progressvisibility);
        readUpdate.setClickable(clickable);
        storageLinearLayout.setClickable(clickable);
        goToSongsButton.setClickable(clickable);
        appMode.setClickable(clickable);
        goToSongsButton.setVisibility(visibility);
        appMode.setVisibility(visibility);
        userGuideLinearLayout.setClickable(clickable);
        userGuideButton.setClickable(clickable);
    }
    void checkStoragePermission() {
        Log.d("d","checkStoragePermission");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permission has not been granted.
            storageGranted = false;
            requestStoragePermission();
        } else {
            storageGranted = true;
        }
    }
    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            try {
                Snackbar.make(findViewById(R.id.page), R.string.storage_rationale,
                        Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(BootUpCheck.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
                    }
                }).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                // Storage permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(BootUpCheck.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            storageGranted = grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        checkReadiness();
    }

    void loadStoragePreference() {
        tree = mPreferences.getMyPreferenceString(BootUpCheck.this,"treeUri",null);
        if (tree != null) {
            treeUri = Uri.parse(tree);
        } else {
            treeUri = null;
        }
        // Try to display the correct folder
        showCurrentStorage(treeUri);
    }
    void showCurrentStorage(Uri u) {
        if (u!=null) {
            List<String> bits = u.getPathSegments();
            StringBuilder sb = new StringBuilder();
            for (String b : bits) {
                sb.append("/");
                sb.append(b);
            }
            text = sb.toString();
            if (!text.endsWith(storageAccess.appFolder)) {
                text += "/"+storageAccess.appFolder;
            }
            text = text.replace("tree", "/");
            text = text.replace(":", "/");
            while (text.contains("//")) {
                text = text.replace("//", "/");
            }
        } else {
            text = "";
        }

        if (progressText!=null) {
            // We aren't just passing through, so we can set the text
            progressText.setText(text);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void chooseStorageLocation() {
        if (storageGranted) {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, 42);
        } else {
            requestStoragePermission();
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK) {
            treeUri = resultData.getData();
            if (treeUri != null) {
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Save the location
                mPreferences.setMyPreferenceString(this,"treeUri",treeUri.toString());

                // Update the storage text
                showCurrentStorage(treeUri);

                // See if we can show the start button yet
                checkReadiness();
            }
        }
    }

    boolean checkStorageIsValid() {
        // Check that the location exists and is writeable
        // Since the OpenSong folder may not yet exist, we can check for the locationUri or it's parent
        if (treeUri != null) {
            DocumentFile df = storageAccess.getDocumentFileFromUri(BootUpCheck.this, treeUri);
            return df != null && df.canWrite();
        }
        return false;
    }
    boolean versionCheck() {
        // Do this as a separate thread
        mPreferences = new Preferences();
        lastUsedVersion = mPreferences.getMyPreferenceInt(BootUpCheck.this,"showSplashVersion",0);
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            thisVersion = pInfo.versionCode;
            versionCode = "V."+pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
            thisVersion = 0;
            versionCode = "";
        }
        Log.d("d","lastUsedVersion (showSplashVersion)="+lastUsedVersion);
        Log.d("d","thisversion="+thisVersion);
        return lastUsedVersion >= thisVersion;
    }

    void checkReadiness() {
        if (checkStorageIsValid() && storageGranted && !skiptoapp) {
            // We're good to go, but need to wait for the user to click on the start button
            goToSongsRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            // Not ready, so hide the start button
            goToSongsRelativeLayout.setVisibility(View.GONE);
        }
    }

    void goToSongs() {
        // Show the progressBar if we were on the BootUpCheck screen
        if (progressBar!=null) {
            showLoadingBar(false);
        }

        final TextView tv = findViewById(R.id.currentAction);
        tv.setVisibility(View.VISIBLE);
        tv.setText("");
        // Do this as a separate thread
        new Thread(new Runnable() {
            String message;
            @Override
            public void run() {
                // Check if the folders exist, if not, create them
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        message = getString(R.string.storage_check);
                        tv.setText(message);
                    }
                });
                String progress = storageAccess.checkRootFoldersExist(BootUpCheck.this,homeFolder);
                foldersok = !progress.contains("Error");

                if (foldersok) {
                    // Load up all of the preferences into FullscreenActivity (static variables)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message = getString(R.string.load_preferences);
                            tv.setText(message);
                        }
                    });
                    FullscreenActivity fullscreenActivity = new FullscreenActivity();
                    fullscreenActivity.mainSetterOfVariables(BootUpCheck.this);

                    // Search for the user's songs
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message = getString(R.string.initialisesongs_start).replace("-","").trim();
                            tv.setText(message);
                        }
                    });
                    storageAccess.getSongFolderContents(BootUpCheck.this);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Show how many songs have been found and display this to the user
                            // This will remain as until the current folder is build
                            int numsongs = FullscreenActivity.allSongs.size();
                            String result = numsongs + " " + getString(R.string.initialisesongs_end);
                            tv.setText(result);
                        }
                    });
                    ListSongFiles listSongFiles = new ListSongFiles();
                    listSongFiles.songUrisInFolder(BootUpCheck.this);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            message = getString(R.string.success);
                            tv.setText(message);
                        }
                    });
                    // Decide on where we are going and set the intent to launch it
                    Intent intent = new Intent();

                    switch (FullscreenActivity.whichMode) {
                        case "Performance":
                        case "Stage":
                        default:
                            intent.setClass(BootUpCheck.this, StageMode.class);
                            break;

                        case "Presentation":
                            intent.setClass(BootUpCheck.this, PresenterMode.class);
                            break;

                    }
                    // Now save the appropriate variables and then start the intent
                    // Set the current version
                    mPreferences.setMyPreferenceInt(BootUpCheck.this, "showSplashVersion", thisVersion);
                    Preferences.savePreferences();

                    startActivity(intent);
                    finish();
                } else {
                    // Show the progressBar if we were on the BootUpCheck screen
                    if (progressBar!=null) {
                        showLoadingBar(true);
                        tv.setVisibility(View.GONE);
                    } else {
                        // There was a problem with the folders, so restart the app!
                        Intent intent = new Intent();
                        intent.setClass(BootUpCheck.this, BootUpCheck.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }).start();
    }
}