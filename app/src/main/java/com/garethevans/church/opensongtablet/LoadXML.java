package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.pdf.PdfRenderer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class LoadXML {

    private static boolean isxml = true;
    static String utf = "UTF-8";
    private static boolean needtoloadextra = false;

    // This bit loads the lyrics from the required file
    void loadXML(Context c, DocumentFile homeFolder) throws IOException {

        StorageAccess sa = new StorageAccess();
        FullscreenActivity.isPDF = false;
        FullscreenActivity.isSong = true;
        FullscreenActivity.isImage = false;
        FullscreenActivity.isSlide = false;
        FullscreenActivity.isScripture = false;
        FullscreenActivity.isImageSlide = false;

        // Clear the heading default
        FullscreenActivity.songSection_holder = "";

        // Set the song load status to false (helps check if it didn't load
        Preferences.loadSongPrep();

        // Just in case
        FullscreenActivity.myLyrics = c.getResources().getString(R.string.songdoesntexist) + "\n\n";
        FullscreenActivity.mLyrics = c.getResources().getString(R.string.songdoesntexist) + "\n\n";

        needtoloadextra = false;
        FullscreenActivity.myXML = null;
        FullscreenActivity.myXML = "";

        // Get the android version
        String filetype = "SONG";
        if (FullscreenActivity.songfilename.toLowerCase().endsWith(".pdf")) {
            filetype = "PDF";
            isxml = false;
        }
        if (FullscreenActivity.songfilename.toLowerCase().endsWith(".doc") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".docx")) {
            filetype = "DOC";
            isxml = false;
        }
        if (FullscreenActivity.songfilename.toLowerCase().endsWith(".jpg") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".jpeg") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".png") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".gif") ||
                FullscreenActivity.songfilename.toLowerCase().endsWith(".bmp")) {
            filetype = "IMG";
            isxml = false;
        }

        // Determine the file encoding
        getFileLocation(c);

        if (filetype.equals("SONG")) {
            utf = sa.getUTFEncoding(c, homeFolder,"Songs", FullscreenActivity.whichSongFolder,FullscreenActivity.songfilename);
        }

        // if (androidapi > 20 || !filetype.equals("PDF") && (!filetype.equals("DOC") && (!filetype.equals("IMG")))) {
        if (!filetype.equals("PDF") && !filetype.equals("DOC") && (!filetype.equals("IMG"))) {

            // Initialise all the xml tags a song should have
            initialiseSongTags();

            // Try to read the file as an xml file, if it isn't, then read it in as text
            isxml = true;
            if (!FullscreenActivity.songfilename.endsWith(".sqlite3") && !FullscreenActivity.songfilename.endsWith(".preferences")) {
                try {
                     grabOpenSongXML(c);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("d", "Error performing grabOpenSongXML()");
                    FullscreenActivity.thissong_scale = "W";
                }
            } else {
                FullscreenActivity.myXML = "<title>Welcome to OpenSong</title>\n<author></author>\n<lyrics>"
                        + c.getResources().getString(R.string.songdoesntexist) + "\n\n" + "</lyrics>";
                FullscreenActivity.myLyrics = "ERROR!";
            }

            if (isxml && !FullscreenActivity.myLyrics.equals("ERROR!")) {
                // Song was loaded correctly and was xml format
                Preferences.loadSongSuccess();
            }

            PopUpEditSongFragment.prepareSongXML();
            FullscreenActivity.myXML = FullscreenActivity.mynewXML;

            if (FullscreenActivity.mLyrics==null ||
                    FullscreenActivity.mLyrics.isEmpty() ||
                    FullscreenActivity.mLyrics.equals("")) {
                isxml = false;
            }

            // If the file hasn't been read properly, or mLyrics is empty, read it in as a text file
            if (!isxml) {

                try {
                    //NEW
                    InputStream inputStream = sa.getInputStreamFromUri(c, FullscreenActivity.file.getUri());
                    InputStreamReader streamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(streamReader);
                    if (validReadableFile()) {
                        FullscreenActivity.myXML = sa.readTextFile(c,inputStream);
                    } else {
                        FullscreenActivity.myXML = "";
                    }
                    FullscreenActivity.mLyrics = FullscreenActivity.myXML;
                    inputStream.close();
                    bufferedReader.close();
                    // Set the song load status to true:
                    Preferences.loadSongSuccess();

                } catch (java.io.FileNotFoundException e) {
                    // file doesn't exist
                    FullscreenActivity.myXML = "<title>Love everlasting</title>\n<author></author>\n<lyrics>"
                            + c.getResources().getString(R.string.songdoesntexist) + "\n\n" + "</lyrics>";
                    FullscreenActivity.myLyrics = "ERROR!";
                }

                // If the song is OnSong format - try to import it
                if (FullscreenActivity.songfilename.contains(".onsong")) {
                    // Run the ChordProConvert script
                    OnSongConvert onSongConvert = new OnSongConvert();
                    if (onSongConvert.doExtractFailed(c,homeFolder)) {
                        Log.d("d","Problem converting OnSong");
                    }
                    ListSongFiles listSongFiles = new ListSongFiles();
                    listSongFiles.getAllSongFiles(c, homeFolder);
                    getFileLocation(c);

                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML(c);
                    } catch (Exception e) {
                        Log.d("d","Error performing grabOpenSongXML()");
                    }
                  // If the song is usr format - try to import it
                } else if (FullscreenActivity.songfilename.contains(".usr")
                        || FullscreenActivity.myXML.contains("[File]")
                        || FullscreenActivity.myXML.contains("Type=")
                        || FullscreenActivity.myXML.contains("Words=")) {
                    // Run the UsrConvert script
                    UsrConvert usrConvert = new UsrConvert();
                    if (!usrConvert.doExtract(c, homeFolder)) {
                        Log.d("d","Problem extracting usr file");
                    }
                    ListSongFiles listSongFiles = new ListSongFiles();
                    listSongFiles.getAllSongFiles(c, homeFolder);
                    getFileLocation(c);

                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML(c);
                    } catch (Exception e) {
                        Log.d("d","Error performing grabOpenSongXML()");
                    }
                  // If the song is in ChordPro format - try to import it
                } else if (FullscreenActivity.myXML.contains("{title") ||
                        FullscreenActivity.myXML.contains("{t:") ||
                        FullscreenActivity.myXML.contains("{t :") ||
                        FullscreenActivity.myXML.contains("{subtitle") ||
                        FullscreenActivity.myXML.contains("{st:") ||
                        FullscreenActivity.myXML.contains("{st :") ||
                        FullscreenActivity.myXML.contains("{comment") ||
                        FullscreenActivity.myXML.contains("{c:") ||
                        FullscreenActivity.myXML.contains("{new_song") ||
                        FullscreenActivity.myXML.contains("{ns") ||
                        FullscreenActivity.songfilename.toLowerCase().contains(".pro") ||
                        FullscreenActivity.songfilename.toLowerCase().contains(".chopro") ||
                        FullscreenActivity.songfilename.toLowerCase().contains(".chordpro")) {
                    // Run the ChordProConvert script
                    if (!ChordProConvert.doExtract(c)) {
                        Log.d("d","Problem extracting chordpro");
                    }
                    ListSongFiles listSongFiles = new ListSongFiles();
                    listSongFiles.getAllSongFiles(c, homeFolder);
                    getFileLocation(c);
                    // Now read in the proper OpenSong xml file
                    try {
                        grabOpenSongXML(c);
                    } catch (Exception e) {
                        Log.d("d","Error performing grabOpenSongXML()");
                    }
                }
            }

            // Fix all the rogue code
            FullscreenActivity.mLyrics = ProcessSong.parseLyrics(FullscreenActivity.mLyrics, c);

            // Just in case we have improved the song, prepare the improved xml
            PopUpEditSongFragment.prepareSongXML();
            FullscreenActivity.myXML = FullscreenActivity.mynewXML;

            // Write what is left to the mLyrics field just incase the file is badly formatted
            if (!FullscreenActivity.myXML.contains("<lyrics")) {
                // Need to add a space to the start of each line
                String[] lines = FullscreenActivity.myXML.split("\n");
                StringBuilder text = new StringBuilder();
                for (int z=0;z<lines.length;z++) {
                    if (lines[z].indexOf("[")!=0 && lines[z].indexOf(".")!=0 && lines[z].indexOf(";")!=0 && lines[z].indexOf("---")!=0 && lines[z].indexOf(" ")!=0) {
                        lines[z] = " " + lines[z];
                    }
                    text.append(lines[z]).append("\n");
                }
                FullscreenActivity.mLyrics = text.toString();
            }

            FullscreenActivity.myLyrics = ProcessSong.removeUnderScores(FullscreenActivity.mLyrics,c);
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Very Fast", "140");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Fast", "120");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Moderate", "100");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Slow", "80");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replace("Very Slow", "60");
            FullscreenActivity.mTempo = FullscreenActivity.mTempo.replaceAll("[\\D]", "");

            if (!FullscreenActivity.mDuration.isEmpty()) {
                try {
                    FullscreenActivity.autoScrollDuration = Integer.parseInt(FullscreenActivity.mDuration.replaceAll("[\\D]",""));
                } catch (Exception e) {
                    FullscreenActivity.autoScrollDuration = -1;
                }
            }

            if (!FullscreenActivity.mPreDelay.isEmpty()) {
                try {
                    FullscreenActivity.autoScrollDelay = Integer.parseInt(FullscreenActivity.mPreDelay.replaceAll("[\\D]",""));
                } catch (Exception e) {
                    FullscreenActivity.autoScrollDelay = -1;
                }
            }

        } else {
            if (filetype.equals("PDF")) {
                FullscreenActivity.isPDF = true;
                FullscreenActivity.isSong = false;
                FullscreenActivity.isImage = false;
                FullscreenActivity.isImageSlide = false;
                FullscreenActivity.isSlide = false;
            } else if (filetype.equals("IMG")) {
                FullscreenActivity.isPDF = false;
                FullscreenActivity.isSong = false;
                FullscreenActivity.isImage = true;
                FullscreenActivity.isImageSlide = false;
                FullscreenActivity.isSlide = false;
            }
            // Initialise the variables
            initialiseSongTags();
        }

        String loc;
        if (FullscreenActivity.file!=null) {
            loc = FullscreenActivity.file.toString();
        } else {
            loc = "";
        }

        if (loc.contains("../Images")) {
            FullscreenActivity.isImageSlide = true;
        } else if (loc.contains("../Scripture")) {
            FullscreenActivity.isScripture = true;
        } else if (loc.contains("../Slides")) {
            FullscreenActivity.isSlide = true;
        } else if (loc.contains("../Variations")) {
            FullscreenActivity.isSong = true;
        }

        FullscreenActivity.thissong_scale = FullscreenActivity.toggleYScale;
    }

    void prepareLoadCustomReusable(Context c, DocumentFile homeFolder, String what) {

        String temp_myXML = FullscreenActivity.myXML;
        String temp_songfilename = FullscreenActivity.songfilename;
        String temp_whichSongFolder = FullscreenActivity.whichSongFolder;
        CharSequence temp_mTitle = FullscreenActivity.mTitle;
        CharSequence temp_mAuthor = FullscreenActivity.mAuthor;
        String temp_mUser1 = FullscreenActivity.mUser1;
        String temp_mUser2 = FullscreenActivity.mUser2;
        String temp_mUser3 = FullscreenActivity.mUser3;
        String temp_mAka = FullscreenActivity.mAka;
        String temp_mKeyLine = FullscreenActivity.mKeyLine;
        String temp_mHymnNumber = FullscreenActivity.mHymnNumber;
        String temp_mLyrics = FullscreenActivity.mLyrics;

        String[] tempfile = what.split("/");
        if (tempfile.length>0) {
            FullscreenActivity.songfilename = tempfile[tempfile.length - 1];
        }
        // Get the new whichSongFolder
        if (what.contains(c.getResources().getString(R.string.note)+"/")) {
            FullscreenActivity.whichSongFolder = "../Notes";
            FullscreenActivity.whattodo = "customreusable_note";
        } else if (what.contains(c.getResources().getString(R.string.slide)+"/")) {
            FullscreenActivity.whichSongFolder = "../Slides";
            FullscreenActivity.whattodo = "customreusable_slide";
        } else if (what.contains(c.getResources().getString(R.string.scripture)+"/")) {
            FullscreenActivity.whichSongFolder = "../Scripture";
            FullscreenActivity.whattodo = "customreusable_scripture";
        } else if (what.contains(c.getResources().getString(R.string.image)+"/")) {
            FullscreenActivity.whichSongFolder = "../Images";
            FullscreenActivity.whattodo = "customreusable_image";
        } else if (what.contains(c.getResources().getString(R.string.variation)+"/")) {
            FullscreenActivity.whichSongFolder = "../Variations";
            FullscreenActivity.whattodo = "customreusable_variation";
        }

        // Load up the XML
        try {
            loadXML(c,homeFolder);
        } catch (Exception e) {
            Log.d("d","Error performing loadXML()");
        }

        // Put the values in
        FullscreenActivity.customslide_title = FullscreenActivity.mTitle.toString();
        FullscreenActivity.customimage_time = FullscreenActivity.mUser1;
        FullscreenActivity.customimage_loop = FullscreenActivity.mUser2;
        FullscreenActivity.customimage_list = FullscreenActivity.mUser3;
        FullscreenActivity.customslide_content = FullscreenActivity.mLyrics;

        // Reset the main song variables back to their former glory
        FullscreenActivity.myXML = temp_myXML;
        FullscreenActivity.songfilename = temp_songfilename;
        FullscreenActivity.whichSongFolder = temp_whichSongFolder;
        FullscreenActivity.mTitle = temp_mTitle;
        FullscreenActivity.mAuthor = temp_mAuthor;
        FullscreenActivity.mUser1 = temp_mUser1;
        FullscreenActivity.mUser2 = temp_mUser2;
        FullscreenActivity.mUser3 = temp_mUser3;
        FullscreenActivity.mAka = temp_mAka;
        FullscreenActivity.mKeyLine = temp_mKeyLine;
        FullscreenActivity.mHymnNumber = temp_mHymnNumber;
        FullscreenActivity.mLyrics = temp_mLyrics;
        //Preferences.savePreferences();
    }

    void initialiseSongTags() {
        FullscreenActivity.mTitle = FullscreenActivity.songfilename;
        FullscreenActivity.mAuthor = "";
        FullscreenActivity.mCopyright = "";
        FullscreenActivity.mPresentation = "";
        FullscreenActivity.mHymnNumber = "";
        FullscreenActivity.mCapo = "";
        FullscreenActivity.mCapoPrint = "false";
        FullscreenActivity.mTempo = "";
        FullscreenActivity.mTimeSig = "";
        FullscreenActivity.mDuration = "";
        FullscreenActivity.mPreDelay = "";
        FullscreenActivity.mCCLI = "";
        FullscreenActivity.mTheme = "";
        FullscreenActivity.mAltTheme = "";
        FullscreenActivity.mUser1 = "";
        FullscreenActivity.mUser2 = "";
        FullscreenActivity.mUser3 = "";
        FullscreenActivity.mKey = "";
        FullscreenActivity.mAka = "";
        FullscreenActivity.mKeyLine = "";
        FullscreenActivity.mBooks = "";
        FullscreenActivity.mMidi = "";
        FullscreenActivity.mMidiIndex = "";
        FullscreenActivity.mPitch = "";
        FullscreenActivity.mRestrictions = "";
        //FullscreenActivity.mLyrics = "";
        FullscreenActivity.mLyrics = templyrics;
        FullscreenActivity.mNotes = "";
        FullscreenActivity.mStyle = "";
        FullscreenActivity.mLinkedSongs = "";
        FullscreenActivity.mPadFile = "";
        FullscreenActivity.mCustomChords = "";
        FullscreenActivity.mLinkYouTube = "";
        FullscreenActivity.mLinkWeb = "";
        FullscreenActivity.mLinkAudio = "";
        FullscreenActivity.mLoopAudio = "false";
        FullscreenActivity.mLinkOther = "";
        FullscreenActivity.mExtraStuff1 = "";
        FullscreenActivity.mExtraStuff2 = "";
    }

    private void grabOpenSongXML(Context c) throws Exception {
        // Extract all of the key bits of the song
        XmlPullParserFactory factory;
        factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        XmlPullParser xpp;
        xpp = factory.newPullParser();

        // Just in case use the Welcome to OpenSongApp file
        initialiseSongTags();

        Log.d("d","file.getUri()="+FullscreenActivity.file.getUri());
        StorageAccess sa = new StorageAccess();
        InputStream inputStream = sa.getInputStreamFromUri(c, FullscreenActivity.file.getUri());
        InputStreamReader lineReader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(lineReader);

        String line;
        try {
            line = buffreader.readLine();
            if (line!=null && line.contains("encoding=\"")) {
                int startpos = line.indexOf("encoding=\"")+10;
                int endpos = line.indexOf("\"",startpos);
                String enc = line.substring(startpos,endpos);
                if (enc.length() > 0) {
                    utf = enc.toUpperCase();
                }
            }
        } catch (Exception e) {
            Log.d("d","No encoding included in line 1");
            e.printStackTrace();
        }

        // Keep a note of this encoding incase we resave the song!
        FullscreenActivity.mEncoding = utf;

        // read every line of the file into the line-variable, on line at the time
        inputStream.close();
        inputStream = sa.getInputStreamFromUri(c, FullscreenActivity.file.getUri());

        xpp.setInput(inputStream, utf);

        int eventType;
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                switch (xpp.getName()) {
                    case "author":
                        FullscreenActivity.mAuthor = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "copyright":
                        FullscreenActivity.mCopyright = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "title":
                        String testthetitle = parseFromHTMLEntities(xpp.nextText());
                        if (testthetitle != null && !testthetitle.equals("") && !testthetitle.isEmpty()) {
                            FullscreenActivity.mTitle = parseFromHTMLEntities(testthetitle);
                        } else if (testthetitle != null && testthetitle.equals("")) {
                            FullscreenActivity.mTitle = FullscreenActivity.songfilename;
                        }
                        break;
                    case "lyrics":
                        String grabbedlyrics = xpp.nextText();
                        try {
                            FullscreenActivity.mLyrics = ProcessSong.fixStartOfLines(parseFromHTMLEntities(xpp.nextText()));
                        } catch (Exception e) {
                            FullscreenActivity.mLyrics = grabbedlyrics;
                        }

                        break;
                    case "ccli":
                        FullscreenActivity.mCCLI = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "theme":
                        FullscreenActivity.mTheme = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "alttheme":
                        FullscreenActivity.mAltTheme = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "presentation":
                        FullscreenActivity.mPresentation = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "hymn_number":
                        FullscreenActivity.mHymnNumber = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "user1":
                        FullscreenActivity.mUser1 = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "user2":
                        FullscreenActivity.mUser2 = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "user3":
                        FullscreenActivity.mUser3 = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "key":
                        FullscreenActivity.mKey = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "aka":
                        FullscreenActivity.mAka = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "key_line":
                        FullscreenActivity.mKeyLine = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "capo":
                        if (xpp.getAttributeCount() > 0) {
                            FullscreenActivity.mCapoPrint = xpp.getAttributeValue(0);
                        }
                        FullscreenActivity.mCapo = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "tempo":
                        FullscreenActivity.mTempo = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "time_sig":
                        FullscreenActivity.mTimeSig = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "duration":
                        FullscreenActivity.mDuration = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "predelay":
                        FullscreenActivity.mPreDelay = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "books":
                        FullscreenActivity.mBooks = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "midi":
                        FullscreenActivity.mMidi = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "midi_index":
                        FullscreenActivity.mMidiIndex = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "pitch":
                        FullscreenActivity.mPitch = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "restrictions":
                        FullscreenActivity.mRestrictions = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "notes":
                        FullscreenActivity.mNotes = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "linked_songs":
                        FullscreenActivity.mLinkedSongs = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "pad_file":
                        FullscreenActivity.mPadFile = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "custom_chords":
                        FullscreenActivity.mCustomChords = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_youtube":
                        FullscreenActivity.mLinkYouTube = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_web":
                        FullscreenActivity.mLinkWeb = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_audio":
                        FullscreenActivity.mLinkAudio = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "loop_audio":
                        FullscreenActivity.mLoopAudio = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "link_other":
                        FullscreenActivity.mLinkOther = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "abcnotation":
                        FullscreenActivity.mNotation = parseFromHTMLEntities(xpp.nextText());
                        break;
                    case "style":
                        // Simplest way to get this is to load the file in line by line as asynctask
                        needtoloadextra = true;
                        break;
                    case "backgrounds":
                        //FullscreenActivity.mExtraStuff2 = xpp.nextText();
                        // Simplest way to get this is to load the file in line by line as asynctask
                        needtoloadextra = true;
                        break;
                }
            }
            try {
                eventType = xpp.next();

            } catch (Exception e) {
                //Ooops!
                isxml = false;
                e.printStackTrace();
            }
        }

        if (FullscreenActivity.mLyrics.equals("")) {
            FullscreenActivity.mTitle = "Welcome to OpenSongApp";
            FullscreenActivity.mAuthor = "Gareth Evans";
            FullscreenActivity.mLinkWeb = "http://www.opensongapp.com";
            FullscreenActivity.mLyrics = templyrics;
            FullscreenActivity.myLyrics = templyrics;
        }

        // If we really have to load extra stuff, lets do it as an asynctask
        if (needtoloadextra) {
            SideTask loadextra = new SideTask(c);
            loadextra.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        FullscreenActivity.myXML = FullscreenActivity.mLyrics;
    }

    String[] getCCLILogInfo(Context c, DocumentFile homeFolder, String folder, String filename) {
        String[] vals = new String[4];
        vals[0] = ""; // Song title
        vals[1] = ""; // Author
        vals[2] = ""; // Copyright
        vals[3] = ""; // CCLI

        StorageAccess sa = new StorageAccess();
        DocumentFile filetocheck = sa.getFileLocationAsDocumentFile(c, homeFolder,"Songs", folder, filename);

        // Get the android version
        boolean fileisxml = true;
        if (filename.toLowerCase().endsWith(".pdf") ||
                filename.toLowerCase().endsWith(".doc") ||
                filename.toLowerCase().endsWith(".docx") ||
                filename.toLowerCase().endsWith(".jpg") ||
                filename.toLowerCase().endsWith(".jpeg") ||
                filename.toLowerCase().endsWith(".png") ||
                filename.toLowerCase().endsWith(".gif") ||
                filename.toLowerCase().endsWith(".bmp")) {
            fileisxml = false;
        }

        String fileutf = null;

        if (filetocheck.exists() && fileisxml) {
            fileutf = sa.getUTFEncoding(c, homeFolder,"Songs", folder, filename);
        }

        try {
            if (fileisxml && fileutf!=null && !fileutf.equals("")) {
                // Extract all of the ccli bits of the song
                XmlPullParserFactory factory;
                factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);
                XmlPullParser xpp;
                xpp = factory.newPullParser();

                vals[0] = ""; // Song title
                vals[1] = ""; // Author
                vals[2] = ""; // Copyright
                vals[3] = ""; // CCLI

                InputStream inputStream = sa.getInputStreamFromUri(c, filetocheck.getUri());
                xpp.setInput(inputStream, fileutf);

                int eventType;
                eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        switch (xpp.getName()) {
                            case "title":
                                vals[0] = parseFromHTMLEntities(xpp.nextText());
                                break;
                            case "author":
                                vals[1] = parseFromHTMLEntities(xpp.nextText());
                                break;
                            case "copyright":
                                vals[2] = parseFromHTMLEntities(xpp.nextText());
                                break;
                            case "ccli":
                                vals[3] = parseFromHTMLEntities(xpp.nextText());
                                break;
                        }
                    }
                    try {
                        eventType = xpp.next();
                    } catch (Exception e) {
                        //Ooops!
                    }
                }
            }
        } catch (Exception e) {
            // Ooops
        }
        return vals;
    }

    String parseFromHTMLEntities(String val) {
        //Fix broken stuff
        if (val==null) {
            val = "";
        }
        val = val.replace("&amp;apos;","'");
        val = val.replace("&amp;quote;","\"");
        val = val.replace("&amp;quot;","\"");
        val = val.replace("&amp;lt;","<");
        val = val.replace("&amp;gt;",">");
        val = val.replace("&amp;","&");
        val = val.replace("&lt;","<");
        val = val.replace("&gt;",">");
        val = val.replace("&apos;","'");
        val = val.replace("&quote;","\"");
        val = val.replace("&quot;","\"");
        return val;
    }

    @SuppressLint("StaticFieldLeak")
    private class SideTask extends AsyncTask<String, Void, String> {

        @SuppressLint("StaticFieldLeak")
        Context c;

        SideTask(Context ctx) {
            c = ctx;
        }

        @Override
        protected String doInBackground(String... params) {
            String full_text;
            StorageAccess sa = new StorageAccess();
            try {
                InputStream inputStream =sa.getInputStreamFromUri(c, FullscreenActivity.file.getUri());
                if (validReadableFile()) {
                    full_text = sa.readTextFile(c,inputStream);
                } else {
                    full_text = "";
                }
            } catch (Exception e) {
                Log.d("d","Error reading text file");
                full_text = "";
            }

            return full_text;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                int style_start = result.indexOf("<style");
                int style_end = result.indexOf("</style>");
                if (style_end > style_start && style_start > -1) {
                    FullscreenActivity.mExtraStuff1 = result.substring(style_start, style_end + 8);
                }
                int backgrounds_start = result.indexOf("<backgrounds");
                int backgrounds_end = result.indexOf("</backgrounds>");
                if (backgrounds_end < 0) {
                    backgrounds_end = result.indexOf("/>", backgrounds_start) + 2;
                } else {
                    backgrounds_end += 14;
                }
                if (backgrounds_end > backgrounds_start && backgrounds_start > -1) {
                    FullscreenActivity.mExtraStuff2 = result.substring(backgrounds_start, backgrounds_end);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    boolean validReadableFile() {
        boolean isvalid = false;
        // Get length of file in bytes
        long filesize = FullscreenActivity.file.length();
        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        filesize = filesize / 1024;
        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        filesize = filesize / 1024;
        String filename = FullscreenActivity.file.toString();
        if (filename.endsWith(".txt") || filename.endsWith(".TXT") ||
                filename.endsWith(".onsong") || filename.endsWith(".ONSONG") ||
                filename.endsWith(".crd") || filename.endsWith(".CRD") ||
                filename.endsWith(".chopro") || filename.endsWith(".CHOPRO") ||
                filename.endsWith(".chordpro") || filename.endsWith(".CHORDPRO") ||
                filename.endsWith(".usr") || filename.endsWith(".USR") ||
                filename.endsWith(".pro") || filename.endsWith(".pro")) {
            isvalid = true;
        } else if (filesize<2) {
            // Less than 2Mb
            isvalid = true;
        }
        return isvalid;
    }

    void getPDFPageCount(Context c) {
        // This only works for post Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getFileLocation(c);
            // FileDescriptor for file, it allows you to close file when you are done with it
            ParcelFileDescriptor mFileDescriptor = null;
            try {
                mFileDescriptor = c.getContentResolver().
                        openFileDescriptor(FullscreenActivity.file.getUri(), "r");
            } catch (Exception e) {
                e.printStackTrace();
            }
            PdfRenderer mPdfRenderer;
            try {
                if (mFileDescriptor!=null) {
                    mPdfRenderer = new PdfRenderer(mFileDescriptor);
                    FullscreenActivity.pdfPageCount = mPdfRenderer.getPageCount();
                    Preferences.loadSongSuccess();
                } else {
                    FullscreenActivity.pdfPageCount = 0;
                }
            } catch (IOException e) {
                e.printStackTrace();
                FullscreenActivity.pdfPageCount = 0;
            }
        }
    }

    void getFileLocation(Context c) {
        StorageAccess storageAccess = new StorageAccess();
        DocumentFile homeFolder = storageAccess.getHomeFolder(c);
        FullscreenActivity.file = storageAccess.getFileLocationAsDocumentFile(c,homeFolder,
                "Songs",FullscreenActivity.whichSongFolder,
                FullscreenActivity.songfilename);
    }


    private String templyrics = "[Intro]\n" +
            " Welcome to OpenSongApp!\n" +
            " This is a test page to show you some of the features of the app.\n" +
            " The app contains 2 modes -\n" +
            ";• Performance Mode - use your Android device as a song book on stage\n" +
            ";• Presentation Mode - hook up to a projector and displaying lyrics\n" +
            "\n" +
            "[Menus]\n" +
            " The left hand slide out menu - lists all folders and songs.\n" +
            " The right hand slide out menu - all options/settings.\n" +
            " The taskbar menu - access different modes, menus or features.\n" +
            " The page icons let you access special song features:\n" +
            ";Backing track, auto scroll, metronome, chords, notes, links, etc.\n" +
            "\n" +
            "[Get help with all of the cool features...]\n" +
            " To find out all of the app features and how to use them,\n" +
            " please check out the online help pages.\n" +
            " Use the link in the options menu under the 'Other' category -\n" +
            " http://www.opensongapp.com\n" +
            " You can also click on the link icon page button.\n" +
            "\n" +
            "[Adding songs]\n" +
            " You can manually enter/create any song you like\n" +
            " The app only comes with this 'song' by default due to copyright.\n" +
            " You can also import songs from other services:\n" +
            ";• Desktop OpenSong - I highly recommend syncing using Dropbox\n" +
            ";• OnSong files\n" +
            ";• ChordPro files\n" +
            ";• Ultimate Guitar website (using the import feature)\n" +
            ";• Chordie website (using the import feature)\n" +
            ";• SongSelect .usr files\n" +
            " Songs are stored in OpenSong format (XML files without the .xml etension)\n" +
            "\n" +
            " Songs are stored on your device in the /OpenSong/Songs folder.\n" +
            " This location is found at the storage location you chose\n" +
            " when you first ran the app.  Check out the 'Storage' category\n" +
            " of the options menu to see where this is.\n" +
            "\n" +
            "[Edit the song]\n" +
            " Click on the song title in the menu bar to see a song preview.\n" +
            " This also allows you to quickly edit the song\n" +
            "\n" +
            "[Chords at work]\n" +
            " This song has been set as having a key of G.\n" +
            " You can also set capo display options easily\n" +
            ".G     D      Em           C      G         G7\n" +
            " Chord lines (above) start with a full stop/period (.)\n" +
            ".C       Em            D      G\n" +
            " You can transpose the chords easily.\n" +
            " This is done from the chords category in the options menu\n" +
            " Nashville, European and Do Ré Mi chords are understood\n" +
            "\n" +
            "[Scaling]\n" +
            " By default OpenSongApp scales a song to fit the page.\n" +
            " You can change the auto scale options in the right hand menu\n" +
            "\n" +
            "[Sets]\n" +
            " Add songs to sets by long pressing on them in the song menu\n" +
            "\n" +
            "[Customising]\n" +
            " You can customise colours, themes,fonts, gestures, pedals, etc.\n" +
            " These customisations can be saved as user profiles.\n" +
            " Check out the categories in the right hand options menu.\n" +
            "\n" +
            "\n" +
            "\n" +
            "HERE IS AN EXAMPLE OF HOW A SONG WOULD LOOK\n" +
            "\n" +
            "\n" +
            "\n" +
            "[V1]\n" +
            ".G2                D/F#\n" +
            " Love everlasting, love without measure,\n" +
            ".Em7                 D       \n" +
            " Full of compassion, righteous and pure;\n" +
            ".C     G/B             Am7   C/D      D\n" +
            " Is my Saviour's love, is my Father's heart.\n" +
            "\n" +
            "[V2]\n" +
            ".G2                   D/F#\n" +
            " Loved by the Father, saved by the Son,\n" +
            ".Em7                 D           \n" +
            " Born of His Spirit, washed in the blood;\n" +
            ".C          G/B             Am7        C/D      D\n" +
            " This is my Saviour's love, this is my Father's heart.\n" +
            "\n" +
            "[B]\n" +
            ".Em            Cmaj7 Em                Cmaj7\n" +
            " Your love for me,   it reaches to the heavens,\n" +
            ".Em              Cmaj7             Bm7\n" +
            " Let my love for You just grow and grow !    \n" +
            ".Em            Cmaj7 Em                Cmaj7\n" +
            " Your love for me,   it reaches to the heavens,\n" +
            ".Em              Cmaj7             D  D/F#\n" +
            " Let my love for You just grow and grow !    \n" +
            "\n" +
            "[V3]\n" +
            ".G2                       D/F#\n" +
            " Love sent from heaven, received in our hearts,\n" +
            ".Em7                      D               \n" +
            " Loving each other, since Christ first loved us;\n" +
            ".C          G/B             Am7        C/D      D  \n" +
            " Sharing my Saviour's love, showing my Father's heart.";


    String grabNextSongInSetKey(Context c, DocumentFile homeFolder, String nextsong) {
        String nextkey = "";
        StorageAccess sa = new StorageAccess();
        DocumentFile nextfile = sa.getFileLocationAsDocumentFile(c,homeFolder,"Songs","",nextsong);
        // Get the android version
        boolean nextisxml = true;
        if (nextsong.toLowerCase().endsWith(".pdf") ||
                nextsong.toLowerCase().endsWith(".doc") ||
                nextsong.toLowerCase().endsWith(".docx") ||
                nextsong.toLowerCase().endsWith(".jpg") ||
                nextsong.toLowerCase().endsWith(".jpeg") ||
                nextsong.toLowerCase().endsWith(".png") ||
                nextsong.toLowerCase().endsWith(".gif") ||
                nextsong.toLowerCase().endsWith(".bmp") ||
                nextfile.getType().contains("image") ||
                nextfile.getType().contains("application")) {
            nextisxml = false;
        }

        String nextutf = null;

        if (nextisxml) {
            nextutf = sa.getUTFEncoding(c,homeFolder,"Songs","",nextsong);
        }

        try {
            if (nextisxml && nextutf!=null && !nextutf.equals("")) {
                // Extract all of the key bits of the song
                XmlPullParserFactory factory;
                factory = XmlPullParserFactory.newInstance();

                factory.setNamespaceAware(true);
                XmlPullParser xpp;
                xpp = factory.newPullParser();

                nextkey = "";

                InputStream inputStream = sa.getInputStreamFromUri(c, nextfile.getUri());
                xpp.setInput(inputStream, nextutf);

                int eventType;
                eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("key")) {
                            nextkey = parseFromHTMLEntities(xpp.nextText());
                        }
                    }
                    try {
                        eventType = xpp.next();
                    } catch (Exception e) {
                        //Ooops!
                    }
                }
            }
        } catch (Exception e) {
            // Ooops
        }

        return nextkey;
    }
}