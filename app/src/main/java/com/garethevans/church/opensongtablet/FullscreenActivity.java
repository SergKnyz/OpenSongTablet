package com.garethevans.church.opensongtablet;

// This will be the holder for the variables
// It wil also be called if the user clicks on a recognised file
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.midi.MidiDevice;
import android.media.midi.MidiInputPort;
import android.media.midi.MidiManager;
import android.media.midi.MidiOutputPort;
import android.media.midi.MidiReceiver;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.peak.salut.Salut;
import com.peak.salut.SalutDataReceiver;
import com.peak.salut.SalutServiceData;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

class FullscreenActivity implements PopUpImportExportOSBFragment.MyInterface, PopUpImportExternalFile.MyInterface {

    // DEFINE THE VARIABLES TO BE USED IN THE APP
    // Then initialise them if required with the main setter

    // Here are the methods associated with FullscreenActivity

    void mainSetterOfVariables(Context c) {
        // This is called asynchronously when the app starts from the BootUpCheck activity
        // Once it is done the app will start

        // First up, load all of the variables
        myPreferences = c.getSharedPreferences("OpenSong",Context.MODE_PRIVATE);
        Preferences.loadPreferences();

        // Now go through some  of these variables loaded and set the app variables accordingly
        if (whattodo == null) {
            whattodo = "";
        }
        // If the song was loaded last time correctly, then we are good to continue
        // If it didn't load, then reset the starting song and folder
        if (!Preferences.wasSongLoaded()) {
            whichSongFolder = "";
            songfilename = "Welcome to OpenSongApp";
        }
        // If whichSongFolder is empty, reset to main
        if (whichSongFolder == null || whichSongFolder.isEmpty() || whichSongFolder.equals("")) {
            whichSongFolder = mainfoldername;
        }

        // Set up some string values
        mainfoldername = c.getResources().getString(R.string.mainfoldername);
        timesigs = c.getResources().getStringArray(R.array.timesig);

        // Next set the locale of the app
        guessLocale(c);
        changeLocale(c);

        // Get the song folders
        StorageAccess storageAccess = new StorageAccess();
        DocumentFile homeFolder = storageAccess.getHomeFolder(c);


        // Test for NFC capability
        testForNFC(c);

        // Initialise api
        currentapiVersion = Build.VERSION.SDK_INT;

        // Capable of dual head presentations
        dualDisplayCapable = currentapiVersion >= 17;

        // Set up the available typefaces
        // Initialise the typefaces available
        initialiseTypefaces(c);

        // Set up the user preferences for page colours and fonts
        SetUpColours.colours();
        SetTypeFace setTypeFace = new SetTypeFace();
        setTypeFace.setTypeface(c, homeFolder);

        // Copy assets (background images)
        storageAccess.copyAssets(c, homeFolder);

        // BootUpCheck will save the preferences before starting the app, so any fixes are saved
    }

    private void guessLocale(Context c) {
        locale = Locale.getDefault();
        if  (locale==null) {
            locale = new Locale(Locale.getDefault().getDisplayLanguage());
        }

        SetLocale.setLocale(c);

        if (!locale.toString().equals("af") && !locale.toString().equals("cz") && !locale.toString().equals("de") &&
                !locale.toString().equals("el") && !locale.toString().equals("es") && !locale.toString().equals("fr") &&
                !locale.toString().equals("hu") && !locale.toString().equals("it") && !locale.toString().equals("ja") &&
                !locale.toString().equals("pl") && !locale.toString().equals("pt") && !locale.toString().equals("ru") &&
                !locale.toString().equals("sr") && !locale.toString().equals("zh")) {
            locale = new Locale("en");
        }
    }
    private void changeLocale(Context c) {
        SetLocale.setLocale(c);
    }
    private void initialiseTypefaces(Context c) {
        typeface0 = Typeface.DEFAULT;
        typeface1 = Typeface.MONOSPACE;
        typeface2 = Typeface.SANS_SERIF;
        typeface3 = Typeface.SERIF;
        typeface4 = Typeface.createFromAsset(c.getAssets(), "fonts/FiraSansOT-Light.otf");
        typeface4i = Typeface.createFromAsset(c.getAssets(), "fonts/FiraSans-LightItalic.otf");
        typeface5 = Typeface.createFromAsset(c.getAssets(), "fonts/FiraSansOT-Regular.otf");
        typeface5i = Typeface.createFromAsset(c.getAssets(), "fonts/FiraSans-Italic.otf");
        typeface6 = Typeface.createFromAsset(c.getAssets(), "fonts/KaushanScript-Regular.otf");
        typeface7 = Typeface.createFromAsset(c.getAssets(), "fonts/Lato-Lig.ttf");
        typeface7i = Typeface.createFromAsset(c.getAssets(), "fonts/Lato-LigIta.ttf");
        typeface8 = Typeface.createFromAsset(c.getAssets(), "fonts/Lato-Reg.ttf");
        typeface8i = Typeface.createFromAsset(c.getAssets(), "fonts/Lato-RegIta.ttf");
        typeface9 = Typeface.createFromAsset(c.getAssets(), "fonts/LeagueGothic-Regular.otf");
        typeface9i = Typeface.createFromAsset(c.getAssets(), "fonts/LeagueGothic-Italic.otf");
        typeface10 = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-Light.ttf");
        typeface10i = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-LightItalic.ttf");
        typeface11 = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-Thin.ttf");
        typeface11i = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-ThinItalic.ttf");
        typeface12 = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-Medium.ttf");
        typeface12i = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-MediumItalic.ttf");
    }
    private void testForNFC(Context c) {
        mAndroidBeamAvailable = true;
        try {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(c);
        } catch (Exception e) {
            mAndroidBeamAvailable = false;
        }
        if (mNfcAdapter==null) {
            mAndroidBeamAvailable = false;
        }
    }


    // Just for the popups - let StageMode or PresenterMode try to deal with them
    @Override
    public void onSongImportDone(String message) {
        Log.d("d","onSongImportDone() called from FullscreenActivity");
    }
    @Override
    public void refreshAll() {
        Log.d("d","refreshAll called from FullscreenActivity");
    }




    //TODO
    /*
    Need to figure out what happens when an appropriate file gets clicked
    if (getIntent() != null) {
        dealWithIntent(getIntent());


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        dealWithIntent(intent);
    }


    /*
@SuppressWarnings("deprecation")
@SuppressLint({"DefaultLocale", "RtlHardcoded", "InflateParams", "SdCardPath"})
public class FullscreenActivity extends AppCompatActivity implements PopUpImportExportOSBFragment.MyInterface,
        PopUpImportExternalFile.MyInterface {
*/


/*
    private void dealWithIntent(Context c, Intent intent) {
        try {
            if (intent != null) {
                if (intent.getData() != null && intent.getData().getPath() != null) {
                    file_location = intent.getData().getPath();
                    */
/*filechosen = new File(intent.getData().getPath());*//*

                    file_name = intent.getData().getLastPathSegment();
                }
                file_uri = intent.getData();
                String action = intent.getAction();
                String type = intent.getType();

                if (Intent.ACTION_SEND.equals(action) && type != null) {
                    if ("text/plain".equals(type)) {
                        handleSendText(c, intent); // Handle text being sent
                    } */
/*else if (type.startsWith("image/")) {
                        handleSendImage(intent); // Handle single image being sent
                    }*//*

                }

                if (file_name != null && file_location != null) {
                    // Check the file exists!
                    DocumentFile f = DocumentFile.fromSingleUri(c,file_uri);
                    if (f.exists() && f.canRead()) {
                        FullscreenActivity.incomingfile = intent;
                        if (file_name.endsWith(".osb")) {
                            // This is an OpenSong backup file
                            whattodo = "importfile_processimportosb";
                        } else {
                            // This is an file opensong can deal with (hopefully)
                            whattodo = "importfile_doimport";
                        }
                    } else {
                        // Cancel the intent
                        FullscreenActivity.incomingfile = null;
                    }
                }

            }
        } catch (Exception e) {
            // No file or intent data
            e.printStackTrace();
            // Just open the app
            // Clear the current intent data as we've dealt with it
            FullscreenActivity.incomingfile = null;
            FullscreenActivity.myToastMessage = c.getString(R.string.error);

        }
    }
*/

/*
    private void handleSendText(Context c, Intent intent) {
        StringBuilder sharedText = new StringBuilder(intent.getStringExtra(Intent.EXTRA_TEXT));
        String title;
        // Fix line breaks (if they exist)
        sharedText = new StringBuilder(ProcessSong.fixlinebreaks(sharedText.toString()));

        // If this is imported from YouVersion bible app, it should contain https://bible
        if (sharedText.toString().contains("https://bible")) {

            title = c.getString(R.string.scripture);
            // Split the text into lines
            String[] lines = sharedText.toString().split("\n");
            if (lines.length>0) {
                // Remove the last line (http reference)
                if (lines.length-1>0 && lines[lines.length-1]!=null &&
                        lines[lines.length-1].contains("https://bible")) {
                    lines[lines.length-1] = "";
                }

                // The 2nd last line is likely to be the verse title
                if (lines.length-2>0 && lines[lines.length-2]!=null) {
                    title = lines[lines.length-2];
                    lines[lines.length-2] = "";
                }

                // Now put the string back together.
                sharedText = new StringBuilder();
                for (String l:lines) {
                    sharedText.append(l).append("\n");
                }
                sharedText = new StringBuilder(sharedText.toString().trim());
            }

            // Now split it into smaller lines to better fit the screen size
            sharedText = new StringBuilder(Bible.shortenTheLines(sharedText.toString(), 40, 6));

            whattodo = "importfile_customreusable_scripture";
            scripture_title = title;
            scripture_verse = sharedText.toString();
        } else {
            // Just standard text, so create a new song
            whattodo = "importfile_newsong_text";
            scripture_title = "importedtext_in_scripture_verse";
            scripture_verse = sharedText.toString();
        }
    }
*/

    public static void restart(Context context) {
        Intent mStartActivity = new Intent(context, BootUpCheck.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        }
        Log.d("d","Something is wrong, trying to restart");
        System.exit(0);
    }

    @Override
    public void openFragment() {
        Log.d("d","openFragment() called from FullscreenActivity");
    }

    @Override
    public void backupInstall(String m) {
        Log.d("d","backupInstall() called from FullscreenActivity");
    }






    // BELOW THIS MESSAGE IS ALL OF THE STATIC VARIABLES TO BE USED!


    // The important starting up ones
    public static Locale locale;
    public static boolean firstload = true;
    public static int currentapiVersion;

    // Storage variables
    public static String mediaStore = "", prefStorage, customStorage, mStorage = "";
    public static Uri uriTree;

    // The song fields
    public static CharSequence mTitle = "", mAuthor = "Gareth Evans", mCopyright = "";
    public static String mLyrics = "", mCCLI = "", mAltTheme = "", mPresentation = "",
            mHymnNumber = "", mUser1 = "", mUser2 = "", mUser3 = "", mKey = "", mAka = "",
            mKeyLine = "", mStyle = "", mCapo = "", mCapoPrint = "", mTempo = "", mTimeSig = "",
            mDuration = "", mPreDelay = "", mBooks = "", mMidi = "", mMidiIndex = "", mPitch = "",
            mRestrictions = "", mNotes = "", temptranspChords = "", mLinkedSongs = "",
            mExtraStuff1 = "", mExtraStuff2 = "", mPadFile = "", mCustomChords = "",
            mLinkYouTube = "", mLinkWeb = "", mLinkAudio = "", mLoopAudio = "false", mLinkOther = "",
            mNotation = "", mEncoding = "UTF-8";

    // Song menu
    public static boolean needtorefreshsongmenu = false, safetosearch = false,
            showSetTickBoxInSongMenu, showAlphabeticalIndexInSongMenu;
    public static String indexlog = "", currentFolder = "", newFolder = "", whichSongFolder = "", randomFolders = "";
    public static ArrayList<String> searchFileName = new ArrayList<>(), searchFolder = new ArrayList<>(),
            searchTitle = new ArrayList<>(), searchAuthor = new ArrayList<>(),
            searchShortLyrics = new ArrayList<>(), searchTheme = new ArrayList<>(),
            searchKey = new ArrayList<>(), searchHymnNumber = new ArrayList<>(),
            allfilesforsearch = new ArrayList<>(), search_database = new ArrayList<>();
    public static float menuSize, alphabeticalSize;
    public static DocumentFile filechosen, file;
    public static String[] mSongFileNames, mSongFolderNames;
    public static ArrayList<String> allSongs, allSongFolders;
    public static ArrayList<Uri> currentSongsInFoldersUris;

    //instantiated new class here.
    public static SongFileList songfilelist = new SongFileList();
    //this could be a ref to an xmlObject.
    public static String[][] songDetails;
    public static int numDirs;
    public static Map<String, Integer> mapIndex;
    public static int currentSongIndex, previousSongIndex, nextSongIndex;
    @SuppressLint("StaticFieldLeak")
    public static SearchViewAdapter sva;

    // Option menu
    public static String whichOptionMenu = "MAIN";

    // MIDI stuff
    public static MidiDevice midiDevice;
    public static MidiManager midiManager;
    public static MidiInputPort midiInputPort;
    public static MidiOutputPort midiOutputPort;
    public static String midiDeviceName = "", midiDeviceAddress = "";
    public static MidiReceiver loggingReceiver;
    public static boolean midiAuto;

    // Metronome, pad and autoscroll stuff
    public static boolean metronomeok, padok, autoscrollok, pad1Playing, pad2Playing, pad1Fading,
            pad2Fading, padson = false, autostartautoscroll, clickedOnAutoScrollStart = false,
            pauseautoscroll = true, autoscrollispaused = false, isautoscrolling = false,
            visualmetronome = false, mTimeSigValid = false, usingdefaults = false,
            learnPreDelay = false, learnSongLength = false, autostartmetronome, autostartpad,
            clickedOnMetronomeStart = false, clickedOnPadStart = false;
    public static int beatoffcolour = 0xff232333, scrollpageHeight, currentBeat = 1,
            total_pixels_to_scroll = 0, timesigindex, default_autoscroll_songlength,
            default_autoscroll_predelay, default_autoscroll_predelay_max, keyindex, scrollSpeed, autoScrollDelay, autoScrollDuration,
            audiolength = -1, fadeWhichPad, padtime_length = 0;
    public static final int autoscroll_pause_time = 500; // specified in ms
    public static int autoscroll_modifier = 0;
    public static String customPadAb, customPadA, customPadBb, customPadB, customPadC, customPadDb,
            customPadD, customPadEb, customPadE, customPadF, customPadGb, customPadG, customPadAbm,
            customPadAm, customPadBbm, customPadBm, customPadCm, customPadDbm, customPadDm,
            customPadEbm, customPadEm, customPadFm, customPadGbm, customPadGm;
    public static String popupAutoscroll_stoporstart = "stop", whichbeat = "a",
            metronomeonoff = "off", metronomepan = "both", padpan = "both",
            autoscroll_default_or_prompt = "", autoscrollonoff = "false", pad_filename = "null";
    public static float timerFontSizePad, timerFontSizeAutoScroll, metrovol, metronomevol = 1.0f,
            padvol = 1.0f, autoscroll_pixels = 0.0f, newPosFloat = 0.0f, scrollDistance;
    public static long time_start, time_passed = 0;
    public static String[] timesigs;
    public static final short minBpm = 40, maxBpm = 199;
    public static short noteValue = 4, beats = 4;
    public static double beatSound = 1200, sound = 1600;
    public static MediaPlayer mPlayer1 = new MediaPlayer(), mPlayer2 = new MediaPlayer();
    public static boolean mPlayer1Paused = false, mPlayer2Paused = false;
    //public static boolean fadeout1 = false, fadeout2 = false;
    //public short initialVolume;

    // The toolbar, clock battery and titles
    public Toolbar toolbar;
    public ActionBar ab;
    public static int ab_height, batteryLine;
    public static boolean timeFormat24h, timeOn, batteryOn, batteryDialOn;
    public static float timeSize, batterySize, ab_titleSize, ab_authorSize;

    // Drawing stuff
    public static String drawingPenColor, drawingHighlightColor, drawingTool;
    public static int drawingPenSize, drawingEraserSize, drawingHighlightSize, highlightShowSecs;
    public static boolean highlightOn, toggleAutoHighlight, saveHighlight = false;

    // Set stuff
    public static String previousSongInSet = "", nextSongInSet = "", nextSongKeyInSet = "",
            whichSetCategory, whatsongforsetwork = "", mySet = "", newSetContents = "",
            settoload = "", setMoveDirection = "", mySetXML = "", setnamechosen = "",
            lastLoadedSetContent = "";
    public static String[] mySetsFileNames, mySetsFolderNames, mSet, mSetList, myParsedSet;
    /*public static File[] mySetsFiles, mySetsDirectories;*/
    public static boolean setView, showingSetsToLoad = false, doneshuffle = false,
            addingtoset = false;
    public static int setSize, indexSongInSet;
    public static ArrayList<String> mTempSetList;
    public static DocumentFile setfile;

    // Chords, capo and transpose stuff
    public static String prefChord_Aflat_Gsharp = "", prefChord_Bflat_Asharp = "",
            prefChord_Dflat_Csharp = "", prefChord_Eflat_Dsharp = "", prefChord_Gflat_Fsharp = "",
            prefChord_Aflatm_Gsharpm = "", prefChord_Bflatm_Asharpm = "",
            prefChord_Dflatm_Csharpm = "", prefChord_Eflatm_Dsharpm = "",
            prefChord_Gflatm_Fsharpm = "", alwaysPreferredChordFormat = "", chordFormat = "",
            oldchordformat = "", presenterChords = "", capokey = null, transposeDirection = "0",
            transposeStyle = "sharps", transposedLyrics = "";
    public static int transposeTimes = 1;
    public static float capoFontSizeInfoBar;
    public static boolean showChords, showLyrics, showCapo, showCapoChords, showNativeAndCapoChords,
            switchsharpsflats = false, showCapoAsNumerals = false, convertchords = false;

    // PopUp window size and alpha
    public static float popupAlpha_Set = 0.6f, popupDim_Set = 0.7f, popupScale_Set = 0.8f,
            popupAlpha_All = 0.6f, popupDim_All = 0.7f, popupScale_All = 0.8f, pageButtonAlpha = 0.4f;
    public static String popupPosition_Set = "c", popupPosition_All = "c";

    // Custom QuickLaunch buttons
    public static String quickLaunchButton_1 = "", quickLaunchButton_2 = "",
            quickLaunchButton_3 = "", quickLaunchButton_4 = "";
    public static int fabSize = FloatingActionButton.SIZE_MINI;
    public static boolean page_set_visible, page_pad_visible, page_metronome_visible,
            page_autoscroll_visible, page_extra_visible, page_custom_visible, page_chord_visible,
            page_links_visible, page_sticky_visible, page_highlight_visible, page_pages_visible,
            page_custom1_visible, page_custom2_visible, page_custom3_visible, page_custom4_visible,
            page_extra_grouped, page_custom_grouped, page_notation_visible;

    public static boolean orientationchanged = false;
    public static boolean sortAlphabetically = true;

    // Long and short key presses
    public static boolean longKeyPress = false, shortKeyPress = false;

    // Updated scaled view stuff
    public static int[] viewwidth, viewheight;
    public static int padding = 18;
    // Song sections
    public static LinearLayout[] sectionviews;
    public static boolean[] sectionrendered;
    public static Bitmap[] sectionbitmaps;

    public static float stagemodeScale;
    public static boolean trimSections,trimSectionSpace, hideLyricsBox, showstartofpdf = true;

    public static String filetoselect = "", pagebutton_scale, profile;

    // This is for trying to automatically open songs via intent
    //private static Intent incomingfile;
    public static String file_name = "", file_location = "", file_type = "", file_contents = "";
    public static Uri file_uri;
    public static boolean stayinsameclass;

    // Screencapure variables
    public static Bitmap bmScreen;
    public static boolean abort = false;

    // Custom note/slide variables
    public static String noteorslide = "", customslide_title = "", customslide_content = "",
            customimage_list = "", customimage_loop = "", customimage_time = "",
            customreusabletoload = "", imagetext="", toggleScrollArrows = "";
    public static boolean customreusable = false, isImageSection = false;
    public static int checkscroll_time = 1600, delayswipe_time = 800, crossFadeTime = 8000;

    public static boolean converting = false;
    public static String phrasetosearchfor;

    public static int myWidthAvail, myHeightAvail;

    // Fonts
    public static Typeface typeface0, typeface1, typeface2, typeface3, typeface4, typeface4i,
            typeface5, typeface5i, typeface6, typeface7, typeface7i, typeface8, typeface8i, typeface9,
            typeface9i, typeface10, typeface10i, typeface11, typeface11i, typeface12, typeface12i;

    public static ArrayList<String> exportsetfilenames = new ArrayList<>(), exportsetfilenames_ost = new ArrayList<>();
    public static String lastSetName = "", chordInstrument = "g", showNextInSet = "top",
            allchords = "", chordnotes = "", capoDisplay = "", languageToLoad = "";

    // Stuff to deal with the splash screen/version
    public static int version, showSplashVersion;

    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    public static boolean receiveHostFiles;

    public static String emailtext = "";

    public static int maxvolrange;

    public static String whattodo;

    public static boolean pressing_button = false;

    public static final int REQUEST_CAMERA_CODE = 1973;
    public static String mCurrentPhotoPath;
    public static final int REQUEST_MICROPHONE_CODE = 1974;
    public static final int REQUEST_PDF_CODE = 1975;

    public static int pdfPageCurrent = 0, pdfPageCount = 0;
    public static boolean isPDF = false, isImage = false, isSong = false, isSlide = false,
            isScripture = false, isImageSlide = false, isVideo = false;

    public static String toggleAutoSticky = "";
    public static int stickyNotesShowSecs, stickyWidth;
    public static boolean hideActionBar;

    // CCLI
    public static String ccli_church, ccli_licence;
    public static boolean ccli_automatic;

    // Swipe
    public static int SWIPE_MIN_DISTANCE = 250, SWIPE_MAX_OFF_PATH = 200, SWIPE_THRESHOLD_VELOCITY = 600;
    public static boolean swipeForMenus, swipeForSongs;

    public static String whichMode = "";

    // Views and bits on the pages
    public static int mScreenOrientation;
    public static boolean scrollbutton = false, actionbarbutton = false;

    // Font sizes (relative)
    public static int linespacing;

    // Edit style
    public static boolean editAsChordPro;
    // Colours
    public static int dark_lyricsTextColor, dark_lyricsBackgroundColor, dark_lyricsVerseColor,
            dark_lyricsChorusColor, dark_lyricsBridgeColor, dark_lyricsCommentColor,
            dark_lyricsPreChorusColor, dark_lyricsTagColor, dark_lyricsChordsColor,
            dark_lyricsCustomColor, dark_lyricsCapoColor, dark_presoFont, dark_presoInfoFont,
            dark_presoAlertFont, dark_presoShadow, dark_metronome, dark_pagebuttons,
            dark_stickytext, dark_stickybg, light_lyricsTextColor, light_lyricsBackgroundColor,
            dark_extrainfobg, dark_extrainfo,
            light_lyricsVerseColor, light_lyricsChorusColor, light_lyricsBridgeColor,
            light_lyricsCommentColor, light_lyricsPreChorusColor, light_lyricsTagColor,
            light_lyricsChordsColor, light_lyricsCustomColor, light_lyricsCapoColor,
            light_presoFont, light_presoInfoFont, light_presoAlertFont, light_presoShadow,
            light_metronome, light_pagebuttons, light_stickytext, light_stickybg,
            light_extrainfobg, light_extrainfo,
            custom1_lyricsTextColor, custom1_lyricsBackgroundColor, custom1_lyricsVerseColor,
            custom1_lyricsChorusColor, custom1_lyricsBridgeColor, custom1_lyricsCommentColor,
            custom1_lyricsPreChorusColor, custom1_lyricsTagColor, custom1_lyricsChordsColor,
            custom1_lyricsCustomColor, custom1_lyricsCapoColor, custom1_presoFont,
            custom1_presoInfoFont, custom1_presoAlertFont, custom1_presoShadow, custom1_metronome,
            custom1_pagebuttons, custom1_stickytext, custom1_stickybg, custom2_lyricsTextColor,
            custom1_extrainfobg, custom1_extrainfo,
            custom2_lyricsBackgroundColor, custom2_lyricsVerseColor, custom2_lyricsChorusColor,
            custom2_lyricsBridgeColor, custom2_lyricsCommentColor, custom2_lyricsPreChorusColor,
            custom2_lyricsTagColor, custom2_lyricsChordsColor, custom2_lyricsCustomColor,
            custom2_lyricsCapoColor, custom2_presoFont, custom2_presoInfoFont,
            custom2_presoAlertFont, custom2_presoShadow, custom2_metronome, custom2_pagebuttons,
            custom2_stickytext, custom2_stickybg, lyricsBoxColor, lyricsTextColor,
            custom2_extrainfobg, custom2_extrainfo,
            lyricsBackgroundColor, lyricsChorusColor, lyricsVerseColor, lyricsBridgeColor,
            lyricsCommentColor, lyricsPreChorusColor, lyricsTagColor, lyricsChordsColor,
            lyricsCustomColor, lyricsCapoColor, metronomeColor, pagebuttonsColor,
            presoAlertFontColor, presoFontColor, presoInfoFontColor, presoShadowColor,
            stickytextColor, stickybgColor, extrainfobgColor, extrainfoColor;
    public static float commentfontscalesize, headingfontscalesize, chordfontscalesize,
            stickyOpacity, stickyTextSize;


    public static int pedal1, pedal2, pedal3, pedal4, pedal5, pedal6;
    public static String pedal1shortaction, pedal2shortaction, pedal3shortaction, pedal4shortaction,
            pedal5shortaction, pedal6shortaction;
    public static String pedal1longaction, pedal2longaction, pedal3longaction, pedal4longaction,
            pedal5longaction, pedal6longaction;
    public static String toggleScrollBeforeSwipe = "", togglePageButtons = "";

    // Set variables
    public static boolean wasscrolling = false;
    public static boolean isManualDragging = false;
    public static String gesture_doubletap = "", gesture_longpress = "",
            longpressdownpedalgesture = "", longpressuppedalgesture = "",
            longpresspreviouspedalgesture = "", longpressnextpedalgesture = "";
    public static String bibleFile = "";
    public static String swipeDrawer = "", swipeSet = "", tempswipeSet = "enable", whichDirection = "R2L";
    public static String mTheme = "";
    public static String mDisplayTheme = "Theme.Holo";


    // Presentation mode variables
    public static boolean presoAutoScale, presoShowChords;
    public static int presoFontSize, presoMaxFontSize;
    public static float presoTitleSize, presoAuthorSize, presoCopyrightSize, presoAlertSize;
    public static int presoLyricsAlign, presoInfoAlign;
    public static Typeface presoInfoFont;

    public static String customLogo = "";
    public static float customLogoSize;
    public static float presoAlpha;
    public static String myAlert = "";
    public static boolean dualDisplayCapable;
    public static String backgroundImage1 = "", backgroundImage2 = "", backgroundVideo1 = "", backgroundVideo2 = "",
            backgroundToUse = "", backgroundTypeToUse = "";
    public static int xmargin_presentation, ymargin_presentation;
    public static boolean usePresentationOrder = false, multilineCompact = false;
    public static int presoTransitionTime = 800;


    // Song xml data
    public static ArrayList<String> foundSongSections_heading = new ArrayList<>();
    //public static ArrayList<String> foundSongSections_content = new ArrayList<>();



    public static boolean isPresenting, isHDMIConnected = false, autoProject;
    public static int scalingDensity = 240;

    // Info for the lyrics table
    public static boolean scalingfiguredout = false, botherwithcolumns;
    public static int splitpoint, thirdsplitpoint, twothirdsplitpoint, halfsplit_section,
            thirdsplit_section, twothirdsplit_section;
    public static String myLyrics = "";
    public static float mFontSize;
    public static int mMaxFontSize, mMinFontSize;
    public static boolean override_fullscale, override_widthscale;
    public static String toggleYScale = "", thissong_scale;
    public static String myXML = "", mynewXML = "";
    public static String songfilename = "", linkclicked = "";
    static SharedPreferences myPreferences;
    public static int numrowstowrite;
    public static String[] myParsedLyrics, myTransposedLyrics;

    public static String myToastMessage = "";

    public static String mScripture = "", incoming_text = "", scripture_title,
            scripture_verse, mainfoldername = "";
    public static int mylyricsfontnum, mychordsfontnum, mypresofontnum, mypresoinfofontnum;
    public static Typeface lyricsfont, commentfont, chordsfont, presofont, customfont;
    public static String customfontname = "";

    public static int whichPad = 0;

    public static String[] songSections, songSectionsLabels, songSectionsTypes;
    public static String[][] sectionContents, sectionLineTypes, projectedContents, projectedLineTypes;
    public static String songSection_holder; // This carries on section types after line breaks
    public static float[] sectionScaleValue;
    public static int currentSection;


    public static String pagebutton_position = "right";
    public static boolean grouppagebuttons = false;


    // Stuff for customising the export feature
    public static boolean exportOpenSongAppSet, exportOpenSongApp, exportDesktop, exportText,
            exportChordPro, exportOnSong, exportImage, exportPDF;
    public static String exportOpenSongAppSet_String = "", exportOpenSongApp_String = "",
            exportDesktop_String = "", exportText_String = "", exportChordPro_String = "",
            exportOnSong_String = "";



    // Salut / connect devices
    @SuppressLint("StaticFieldLeak")
    public static Button hostButton, clientButton;
    @SuppressLint("StaticFieldLeak")
    public static TextView connectionsLog;
    @SuppressLint("StaticFieldLeak")
    public static SalutDataReceiver dataReceiver;
    public static SalutServiceData serviceData;
    public static Salut network;
    public static BluetoothAdapter mBluetoothAdapter;
    public static String hostButtonText="", clientButtonText="", salutLog="",
            mBluetoothName, mySalutXML = "", presenterSendSong="";
    public static boolean firstSendingOfSalut = true, firstSendingOfSalutXML = true,
            firstSendingOfSalutSection = true, firstReceivingOfSalut = true,
            firstReceivingOfSalutXML = true, firstReceivingOfSalutSection = true,
            firstSendingOfSalutAutoscroll = true, firstReceivingOfSalutAutoscroll = true;
    static NfcAdapter mNfcAdapter;
    // Flag to indicate that Android Beam is available
    public static boolean mAndroidBeamAvailable  = false;
    public static boolean forcecastupdate;
 }