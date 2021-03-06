package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import static android.provider.DocumentsContract.Document.MIME_TYPE_DIR;

public class PopUpSongFolderRenameFragment extends DialogFragment {
    // This is a quick popup to enter a new song folder name, or rename a current one
    // Once it has been completed positively (i.e. ok was clicked) it sends a refreshAll() interface call

    static String myTask;
    String mTitle;
    static ArrayList<String> oldtempfolders;
    GetFoldersAsync getFolders_async;
    String tempNewFolder;
    String tempOldFolder;
    TextView title;
    ProgressBar progressBar;
    StorageAccess storageAccess;
    Preferences preferences;
    SongFolders songFolders;
    ListSongFiles listSongFiles;
    TextView currentFolder_TextView;

    static PopUpSongFolderRenameFragment newInstance(String message) {
        myTask = message;
        PopUpSongFolderRenameFragment frag;
        frag = new PopUpSongFolderRenameFragment();
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_song_rename));
        switch (myTask) {
            case "create":
                mTitle = getActivity().getResources().getString(R.string.options_song_newfolder);
                break;

            default:
                mTitle = getActivity().getResources().getString(R.string.options_song_rename);
                break;
        }
        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View V = inflater.inflate(R.layout.popup_songfolderrename, container, false);

        title = V.findViewById(R.id.dialogtitle);
        doSetTitle();
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        final FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(saveMe,getActivity());
                //saveMe.setEnabled(false);
                createOrRename();
            }
        });

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        songFolders = new SongFolders();
        listSongFiles = new ListSongFiles();

        // Initialise the views
        oldFolderNameSpinner = V.findViewById(R.id.oldFolderNameSpinner);
        newFolderNameEditText = V.findViewById(R.id.newFolderNameEditText);
        progressBar = V.findViewById(R.id.progressBar);
        currentFolder_TextView = V.findViewById(R.id.currentFolder_TextView);

        // Set up the folderspinner
        // Set up the spinner
        // Populate the list view with the current song folders
        // Reset to the main songs folder, so we can list them
        FullscreenActivity.currentFolder = FullscreenActivity.whichSongFolder;
        FullscreenActivity.newFolder = FullscreenActivity.whichSongFolder;
        //FullscreenActivity.whichSongFolder = "";

        // Do the time consuming bit as an asynctask
        getFolders_async = new GetFoldersAsync();
        try {
            getFolders_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Probably closed popup before folders listed\n"+e);
        }

        if (myTask.equals("create")) {
            // Hide the spinner
            oldFolderNameSpinner.setVisibility(View.GONE);

            // Show the current folder location (to allow subfolder creation)
            currentFolder_TextView.setVisibility(View.VISIBLE);
            String s;
            if (FullscreenActivity.whichSongFolder.equals("") ||
                    FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                s = "../OpenSong/Songs/";
            } else {
                s = "../OpenSong/Songs/" + FullscreenActivity.whichSongFolder + "/";
            }
            currentFolder_TextView.setText(s);
        }

        // Set the oldFolderNameSpinnerListener
        oldFolderNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                FullscreenActivity.currentFolder = oldtempfolders.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        return V;
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    Spinner oldFolderNameSpinner;
    EditText newFolderNameEditText;

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    public void createOrRename() {
        progressBar.setVisibility(View.VISIBLE);
        getVariables();
        if (myTask.equals("create")) {
            doCreateFolder();
        } else {
            doRenameFolder();
        }
    }

    public void doSetTitle() {
        title.setText(mTitle);
    }

    public void doCreateFolder() {
        // Try to create inside the current folder
        @SuppressLint("StaticFieldLeak") AsyncTask<Object, String, String> docreation = new AsyncTask<Object, String, String>() {
            @Override
            protected String doInBackground(Object... objects) {
                if (!FullscreenActivity.whichSongFolder.equals(getString(R.string.mainfoldername)) &&
                        !FullscreenActivity.whichSongFolder.equals("")) {
                    tempNewFolder = FullscreenActivity.whichSongFolder + "/" + tempNewFolder;
                }
                if (storageAccess.createFile(getActivity(), preferences, MIME_TYPE_DIR, "Songs", tempNewFolder, null)) {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.newfolder) + " - " + getResources().getString(R.string.ok);
                    FullscreenActivity.whichSongFolder = tempNewFolder;
                } else {
                    FullscreenActivity.myToastMessage = getResources().getString(R.string.newfolder) + " - " + getResources().getString(R.string.createfoldererror);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                ShowToast.showToast(getActivity());

                progressBar.setVisibility(View.GONE);

                // Save preferences
                Preferences.savePreferences();

                // Let the app know we need to rebuild the database
                FullscreenActivity.needtorefreshsongmenu = true;

                if (mListener != null) {
                    mListener.loadSong();
                }
                try {
                    dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        docreation.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public interface MyInterface {
        void loadSong();
    }

    public void doRenameFolder() {

        tempOldFolder = FullscreenActivity.currentFolder;
        // Try to rename
        @SuppressLint("StaticFieldLeak") AsyncTask<Object, String, String> renamefolders = new AsyncTask<Object, String, String>() {

            @Override
            protected String doInBackground(Object... objects) {
                if (storageAccess.renameFolder(getActivity(), preferences, tempOldFolder, tempNewFolder)) {
                    return "success";
                } else {
                    return "error";
                }
            }

            @Override
            protected void onPostExecute(String s) {
                ShowToast.showToast(getActivity());
                if (s.equals("success")) {
                    // Save preferences
                    Preferences.savePreferences();

                    // Let the app know we need to rebuild the database
                    FullscreenActivity.needtorefreshsongmenu = true;

                    if (mListener != null) {
                        mListener.loadSong();
                    }
                    try {
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                }
            }
        };
        renamefolders.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void getVariables() {
        // Get the variables
        tempNewFolder = newFolderNameEditText.getText().toString().trim();
        Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Songs", tempNewFolder, "");

        if (!tempNewFolder.equals("") && !tempNewFolder.isEmpty() && !tempNewFolder.contains("/") &&
                !storageAccess.uriExists(getActivity(),uri) &&
                !tempNewFolder.equals(FullscreenActivity.mainfoldername)) {
            FullscreenActivity.newFolder = tempNewFolder;
            tempOldFolder = FullscreenActivity.currentFolder;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFoldersAsync extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... params) {
            songFolders.prepareSongFolders(getActivity(), storageAccess, preferences);

            // The song folder
            oldtempfolders = new ArrayList<>();
            for (int e=0;e<FullscreenActivity.mSongFolderNames.length;e++) {
                if (FullscreenActivity.mSongFolderNames[e]!=null &&
                        !FullscreenActivity.mSongFolderNames[e].equals(FullscreenActivity.mainfoldername)) {
                    oldtempfolders.add(FullscreenActivity.mSongFolderNames[e]);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                ArrayAdapter<String> folders = new ArrayAdapter<>(getActivity(), R.layout.my_spinner, oldtempfolders);
                folders.setDropDownViewResource(R.layout.my_spinner);
                oldFolderNameSpinner.setAdapter(folders);

                // Select the current folder as the preferred one
                oldFolderNameSpinner.setSelection(0);
                for (int w = 0; w < oldtempfolders.size(); w++) {
                    if (FullscreenActivity.whichSongFolder.equals(oldtempfolders.get(w))) {
                        oldFolderNameSpinner.setSelection(w);
                        FullscreenActivity.currentFolder = oldtempfolders.get(w);
                    }
                }
            } catch (Exception e) {
                // Error
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getFolders_async!=null) {
            getFolders_async.cancel(true);
        }
        this.dismiss();
    }
}
