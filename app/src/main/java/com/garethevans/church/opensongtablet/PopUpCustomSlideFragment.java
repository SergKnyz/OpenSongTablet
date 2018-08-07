package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.net.URLEncoder;

public class PopUpCustomSlideFragment extends DialogFragment {

    static PopUpCustomSlideFragment newInstance() {
        PopUpCustomSlideFragment frag;
        frag = new PopUpCustomSlideFragment();
        return frag;
    }

    public interface MyInterface {
        void addSlideToSet();
        void openFragment();
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

    AsyncTask<Object,Void,String> update_fields;

    // Declare views
    View V;
    RadioGroup customRadioGroup;
    RadioButton noteRadioButton;
    RadioButton slideRadioButton;
    RadioButton imageRadioButton;
    RadioButton scriptureRadioButton;
    TextView slideTitleTextView;
    TextView slideContentTextView;
    @SuppressLint("StaticFieldLeak")
    static EditText slideTitleEditText;
    @SuppressLint("StaticFieldLeak")
    static EditText slideContentEditText;
    Button loadReusableButton;
    CheckBox saveReusableCheckBox;
    @SuppressLint("StaticFieldLeak")
    static Button addPageButton;
    @SuppressLint("StaticFieldLeak")
    static TableLayout slideImageTable;
    @SuppressLint("StaticFieldLeak")
    static CheckBox loopCheckBox;
    @SuppressLint("StaticFieldLeak")
    static TextView timeTextView;
    @SuppressLint("StaticFieldLeak")
    static EditText timeEditText;
    @SuppressLint("StaticFieldLeak")
    static TextView warningTextView;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout reusable_LinearLayout;
    @SuppressLint("StaticFieldLeak")
    static LinearLayout searchBible_LinearLayout;
    @SuppressLint("StaticFieldLeak")
    static RelativeLayout slideDetails_RelativeLayout;
    @SuppressLint("StaticFieldLeak")
    static EditText bibleSearch;
    @SuppressLint("StaticFieldLeak")
    static EditText bibleVersion;
    Button searchBibleGateway_Button;
    Button localBibleFile;
    WebView bibleGateway_WebView;
    @SuppressLint("StaticFieldLeak")
    static Button grabVerse_Button;
    @SuppressLint("StaticFieldLeak")
    static ProgressBar searchBible_progressBar;

    // Declare variables used
    static String whattype = "note";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        V = inflater.inflate(R.layout.popup_customslidecreator, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.add_custom_slide));
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
                saveMe.setEnabled(false);
                doSave();
            }
        });

        // Initialise the basic views
        customRadioGroup = V.findViewById(R.id.customRadioGroup);
        noteRadioButton = V.findViewById(R.id.noteRadioButton);
        slideRadioButton = V.findViewById(R.id.slideRadioButton);
        imageRadioButton = V.findViewById(R.id.imageRadioButton);
        scriptureRadioButton = V.findViewById(R.id.scriptureRadioButton);
        slideTitleTextView = V.findViewById(R.id.slideTitleTextView);
        slideContentTextView = V.findViewById(R.id.slideContentTextView);
        slideTitleEditText = V.findViewById(R.id.slideTitleEditText);
        slideContentEditText = V.findViewById(R.id.slideContentEditText);
        addPageButton = V.findViewById(R.id.addPageButton);
        loadReusableButton = V.findViewById(R.id.loadReusableButton);
        saveReusableCheckBox = V.findViewById(R.id.saveReusableCheckBox);
        slideImageTable = V.findViewById(R.id.slideImageTable);
        loopCheckBox = V.findViewById(R.id.loopCheckBox);
        timeTextView = V.findViewById(R.id.timeTextView);
        timeEditText = V.findViewById(R.id.timeEditText);
        warningTextView = V.findViewById(R.id.warningTextView);
        reusable_LinearLayout = V.findViewById(R.id.reusable_LinearLayout);
        searchBible_LinearLayout = V.findViewById(R.id.searchBible_LinearLayout);
        slideDetails_RelativeLayout = V.findViewById(R.id.slideDetails_RelativeLayout);
        bibleSearch = V.findViewById(R.id.bibleSearch);
        bibleVersion = V.findViewById(R.id.bibleVersion);
        localBibleFile = V.findViewById(R.id.localBibleFile);
        searchBibleGateway_Button = V.findViewById(R.id.searchBibleGateway_Button);
        bibleGateway_WebView = V.findViewById(R.id.bibleGateway_WebView);
        grabVerse_Button = V.findViewById(R.id.grabVerse_Button);
        grabVerse_Button.setVisibility(View.GONE);
        searchBible_progressBar = V.findViewById(R.id.searchBible_progressBar);
        searchBible_progressBar.setVisibility(View.GONE);
        bibleGateway_WebView.setVisibility(View.GONE);
        bibleGateway_WebView.getSettings().getJavaScriptEnabled();
        bibleGateway_WebView.getSettings().setJavaScriptEnabled(true);
        bibleGateway_WebView.getSettings().setDomStorageEnabled(true);
        bibleGateway_WebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        bibleGateway_WebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        grabVerse_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBible_progressBar.setVisibility(View.VISIBLE);
                bibleGateway_WebView.setVisibility(View.GONE);
                grabVerse_Button.setVisibility(View.GONE);
                Bible.grabBibleText(getActivity().getApplicationContext(), bibleGateway_WebView.getUrl());
            }
        });

        localBibleFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullscreenActivity.whattodo = "localbible";
                if (mListener!=null) {
                    try {
                        mListener.openFragment();
                        dismiss();
                    } catch (Exception e) {
                        Log.d("d","Error opening local bible");
                    }
                }
            }
        });
        searchBibleGateway_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBible_progressBar.setVisibility(View.VISIBLE);
                searchBible();
            }
        });

        if (FullscreenActivity.whattodo.contains("customreusable_")) {
            updateFields();
        } else {
            // By default we want to make a brief note/placeholder
            noteRadioButton.setChecked(true);
            FullscreenActivity.whattodo = "customnote";
            slideRadioButton.setChecked(false);
            imageRadioButton.setChecked(false);
            scriptureRadioButton.setChecked(false);
            saveReusableCheckBox.setChecked(false);
            switchViewToNote();
        }

        // Set button listeners
        addPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (whattype.equals("slide")) {
                    String newText = slideContentEditText.getText().toString().trim() + "\n---\n";
                    newText = newText.trim() + "\n";
                    slideContentEditText.setText(newText);
                } else if (whattype.equals("image")) {
                    // Call file browser
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    try {
                        startActivityForResult(i, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        FullscreenActivity.myToastMessage = getResources().getString(R.string.no_filemanager);
                        ShowToast.showToast(getActivity());
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.estrongs.android.pop")));
                        } catch (Exception anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.estrongs.android.pop")));
                        }
                    }

                }
            }
        });

        loadReusableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This reopens the choose backgrounds popupFragment
                dismiss();
                DialogFragment newFragment = PopUpFileChooseFragment.newInstance();
                newFragment.show(getFragmentManager(), "dialog");
            }
        });
        customRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (noteRadioButton.isChecked()) {
                    switchViewToNote();
                } else if (slideRadioButton.isChecked()) {
                    switchViewToSlide();
                } else if (scriptureRadioButton.isChecked()) {
                    switchViewToScripture();
                } else {
                    switchViewToImage();
                }
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doSave(){
        FullscreenActivity.noteorslide = whattype;
        StringBuilder text = new StringBuilder(slideContentEditText.getText().toString().trim());
        FullscreenActivity.customreusable = saveReusableCheckBox.isChecked();
        StringBuilder imagecontents;
        if (whattype.equals("image")) {
            imagecontents = new StringBuilder();
            // Go through images in list and extract the full location and the filename
            for (int r = 0; r < slideImageTable.getChildCount(); r++) {
                // Look for image file location
                if (slideImageTable.getChildAt(r) instanceof TableRow) {
                    TextView tv = (TextView) ((TableRow) slideImageTable.getChildAt(r)).getChildAt(0);
                    String tv_text = tv.getText().toString();
                    imagecontents.append(tv_text).append("\n");
                }
            }

            while (imagecontents.toString().contains("\n\n")) {
                imagecontents = new StringBuilder(imagecontents.toString().replace("\n\n", "\n"));
            }
            imagecontents = new StringBuilder(imagecontents.toString().trim());
            String[] individual_images = imagecontents.toString().split("\n");

            // Prepare the lyrics
            text = new StringBuilder();
            for (int t = 0; t < individual_images.length; t++) {
                text.append("[").append(getActivity().getResources().getString(R.string.image)).append("_").append(t + 1).append("]\n").append(individual_images[t]).append("\n\n");
            }
            text = new StringBuilder(text.toString().trim());

        } else {
            imagecontents = new StringBuilder();
        }
        FullscreenActivity.customslide_title = slideTitleEditText.getText().toString();
        FullscreenActivity.customslide_content = text.toString();
        FullscreenActivity.customimage_list = imagecontents.toString();
        FullscreenActivity.customimage_loop = "" + loopCheckBox.isChecked() + "";
        FullscreenActivity.customimage_time = timeEditText.getText().toString();
        // Check the slide has a title.  If not, use _
        if (FullscreenActivity.customslide_title == null || FullscreenActivity.customslide_title.equals("") || FullscreenActivity.customslide_title.isEmpty()) {
            FullscreenActivity.customslide_title = "_";
        }
        mListener.addSlideToSet();
        dismiss();
    }

    public void updateFields() {
        update_fields = new UpdateFields();
        try {
            update_fields.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } catch (Exception e) {
            Log.d("d","Error updating fields");
        }
    }
    @SuppressLint("StaticFieldLeak")
    private class UpdateFields extends AsyncTask<Object, Void, String> {

        @Override
        protected String doInBackground(Object... objects) {
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            switch (FullscreenActivity.whattodo) {
                case "customreusable_note":
                    // Fill in the details
                    noteRadioButton.setChecked(true);
                    slideRadioButton.setChecked(false);
                    imageRadioButton.setChecked(false);
                    scriptureRadioButton.setChecked(false);
                    switchViewToNote();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText(FullscreenActivity.customslide_content);
                    break;
                case "customreusable_scripture":
                    // Fill in the details
                    noteRadioButton.setChecked(false);
                    slideRadioButton.setChecked(false);
                    imageRadioButton.setChecked(false);
                    scriptureRadioButton.setChecked(true);
                    switchViewToScripture();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText(FullscreenActivity.customslide_content);
                    if (FullscreenActivity.scripture_title!=null && FullscreenActivity.scripture_title!=null &&
                            FullscreenActivity.scripture_verse!=null && FullscreenActivity.scripture_verse!=null) {
                        slideTitleEditText.setText(FullscreenActivity.scripture_verse);
                        slideContentEditText.setText(FullscreenActivity.scripture_verse);
                        addScripture();
                    }
                    break;
                case "customreusable_slide":
                    // Fill in the details
                    noteRadioButton.setChecked(false);
                    slideRadioButton.setChecked(true);
                    imageRadioButton.setChecked(false);
                    scriptureRadioButton.setChecked(false);
                    switchViewToSlide();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText(FullscreenActivity.customslide_content);
                    timeEditText.setText(FullscreenActivity.customimage_time);
                    if (FullscreenActivity.customimage_loop.equals("true")) {
                        loopCheckBox.setChecked(true);
                    } else {
                        loopCheckBox.setChecked(false);
                    }
                    break;
                case "customreusable_image":
                    // Fill in the details
                    noteRadioButton.setChecked(false);
                    slideRadioButton.setChecked(false);
                    imageRadioButton.setChecked(true);
                    scriptureRadioButton.setChecked(false);
                    switchViewToImage();
                    slideTitleEditText.setText(FullscreenActivity.customslide_title);
                    slideContentEditText.setText("");
                    timeEditText.setText(FullscreenActivity.customimage_time);
                    if (FullscreenActivity.customimage_loop.equals("true")) {
                        loopCheckBox.setChecked(true);
                    } else {
                        loopCheckBox.setChecked(false);
                    }
                    // Now parse the list of images...
                    String imgs[] = FullscreenActivity.customimage_list.split("\n");
                    slideImageTable.removeAllViews();
                    for (String img : imgs) {
                        addRow(img);
                    }
                    break;
            }
        }
    }

    public void switchViewToNote() {
        whattype = "note";
        FullscreenActivity.whattodo ="customnote";
        grabVerse_Button.setVisibility(View.GONE);
        reusable_LinearLayout.setVisibility(View.VISIBLE);
        searchBible_LinearLayout.setVisibility(View.GONE);
        slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        addPageButton.setVisibility(View.GONE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.GONE);
        timeTextView.setVisibility(View.GONE);
        timeEditText.setVisibility(View.GONE);
        warningTextView.setVisibility(View.GONE);
    }

    public void switchViewToScripture() {
        whattype = "scripture";
        grabVerse_Button.setVisibility(View.GONE);
        searchBible_progressBar.setVisibility(View.GONE);
        FullscreenActivity.whattodo ="customscripture";
        reusable_LinearLayout.setVisibility(View.GONE);
        searchBible_LinearLayout.setVisibility(View.VISIBLE);
        slideDetails_RelativeLayout.setVisibility(View.GONE);
        addPageButton.setVisibility(View.GONE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.GONE);
        timeTextView.setVisibility(View.GONE);
        timeEditText.setVisibility(View.GONE);
        warningTextView.setVisibility(View.GONE);
        bibleSearch.setVisibility(View.VISIBLE);
        bibleVersion.setVisibility(View.VISIBLE);
    }

    public void switchViewToSlide() {
        whattype = "slide";
        FullscreenActivity.whattodo ="customslide";
        grabVerse_Button.setVisibility(View.GONE);
        reusable_LinearLayout.setVisibility(View.VISIBLE);
        searchBible_LinearLayout.setVisibility(View.GONE);
        slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        addPageButton.setVisibility(View.VISIBLE);
        slideContentEditText.setVisibility(View.VISIBLE);
        slideImageTable.setVisibility(View.GONE);
        loopCheckBox.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.VISIBLE);
        timeEditText.setVisibility(View.VISIBLE);
        warningTextView.setVisibility(View.GONE);
    }

    public void switchViewToImage() {
        whattype = "image";
        FullscreenActivity.whattodo ="customimage";
        grabVerse_Button.setVisibility(View.GONE);
        reusable_LinearLayout.setVisibility(View.VISIBLE);
        searchBible_LinearLayout.setVisibility(View.GONE);
        slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        addPageButton.setVisibility(View.VISIBLE);
        slideContentEditText.setVisibility(View.GONE);
        slideImageTable.setVisibility(View.VISIBLE);
        loopCheckBox.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.VISIBLE);
        timeEditText.setVisibility(View.VISIBLE);
        warningTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d("onActivityResult","requestCode="+requestCode);
        Log.d("onActivityResult","resultCode="+resultCode);
        Log.d("onActivityResult","intent="+intent);

        if (intent!=null) {
            Uri uri = intent.getData();
            /*Log.d("onActivityResult","uri="+uri);
            String fullpath;
            if (Build.VERSION.SDK_INT >= 19) {
                fullpath = getRealPathFromURI_API19(uri);
            } else {
                fullpath = getRealPathFromURI_API11to18(uri);
            }*/

            if (requestCode==0) {
                // Create a new row in the table
                // Each row has the file name, an image thumbnail and a delete button
                if (uri!=null) {
                    addRow(uri.toString());
                }
            }
        }
    }

/*
    @SuppressLint("NewApi")
    public String getRealPathFromURI_API19(Uri uri){
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public String getRealPathFromURI_API11to18(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                getActivity(),
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }
*/

    @SuppressLint("SetJavaScriptEnabled")
    public void searchBible() {
        // Prepare the search strings
        String whattosearch = bibleSearch.getText().toString();
        String whatversion = bibleVersion.getText().toString();
        try {
            whattosearch = URLEncoder.encode(bibleSearch.getText().toString(), "UTF-8");
            whatversion = URLEncoder.encode(bibleVersion.getText().toString(), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        bibleGateway_WebView.getSettings().setJavaScriptEnabled(true);
        String webaddress = "https://www.biblegateway.com/quicksearch/?quicksearch="+whattosearch+"&qs_version="+whatversion;
        bibleGateway_WebView.loadUrl(webaddress);
        bibleGateway_WebView.setVisibility(View.VISIBLE);
        bibleGateway_WebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                searchBible_progressBar.setVisibility(View.GONE);
                if (url.contains("passage")) {
                    grabVerse_Button.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    public void addRow(String fullpath) {
        try {
            TableRow row = new TableRow(getActivity());
            TableLayout.LayoutParams layoutRow = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(layoutRow);
            row.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            row.setTag(fullpath);
            TextView filename = new TextView(getActivity());
            filename.setText(fullpath);
            filename.setTextSize(0.0f); // Make it take up no space (user doesn't need to see it).
            filename.setVisibility(View.GONE);
            ImageView thumbnail = new ImageView(getActivity());
            Bitmap ThumbImage;
            Resources res = getResources();
            BitmapDrawable bd;
            DocumentFile df = DocumentFile.fromSingleUri(getActivity(), Uri.parse(fullpath));
            if (!df.exists()) {
                Drawable notfound = getResources().getDrawable(R.drawable.notfound);
                thumbnail.setImageDrawable(notfound);
            } else {
                ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(fullpath), 200, 150);
                bd = new BitmapDrawable(res, ThumbImage);
                thumbnail.setImageDrawable(bd);
            }
            thumbnail.setBackgroundDrawable(getResources().getDrawable(R.drawable.presenter_box_black));
            thumbnail.setMaxWidth(200);
            thumbnail.setMaxHeight(150);
            TableRow.LayoutParams layoutImage = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
            layoutImage.width = 200;
            layoutImage.height = 150;
            thumbnail.setLayoutParams(layoutImage);
            ImageButton delete = new ImageButton(getActivity());
            delete.setImageDrawable(getResources().getDrawable(R.drawable.ic_delete_white_36dp));
            delete.setTag(fullpath + "_delete");
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String rowtag = v.getTag().toString();
                    rowtag = rowtag.replace("_delete", "");
                    try {
                        if (getView() != null) {
                            slideImageTable.removeView(getView().findViewWithTag(rowtag));
                        }
                    } catch (Exception e) {
                        // oh well
                        Log.d("error", "No table row with this tag");
                    }
                }
            });
            row.addView(filename);
            row.addView(thumbnail);
            row.addView(delete);
            slideImageTable.addView(row);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addScripture() {
        if (FullscreenActivity.scripture_title!=null &&
                !FullscreenActivity.scripture_title.equals("") &&
                FullscreenActivity.scripture_verse!=null &&
                !FullscreenActivity.scripture_verse.equals("")) {
            searchBible_progressBar.setVisibility(View.GONE);
            grabVerse_Button.setVisibility(View.GONE);
            slideTitleEditText.setText(FullscreenActivity.scripture_title);
            slideContentEditText.setText(FullscreenActivity.scripture_verse);
            reusable_LinearLayout.setVisibility(View.GONE);
            searchBible_LinearLayout.setVisibility(View.GONE);
            slideDetails_RelativeLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (update_fields!=null) {
            update_fields.cancel(true);
        }
        this.dismiss();
    }

}