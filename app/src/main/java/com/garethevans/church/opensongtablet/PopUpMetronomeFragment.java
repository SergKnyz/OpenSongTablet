package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;

public class PopUpMetronomeFragment extends DialogFragment {

    static PopUpMetronomeFragment newInstance() {
        PopUpMetronomeFragment frag;
        frag = new PopUpMetronomeFragment();
        return frag;
    }

    public interface MyInterface {
        void pageButtonAlpha(String s);
        void openFragment();
    }

    public static MyInterface mListener;

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

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(), getDialog());
        }
    }

    NumberPicker bpm_numberPicker, timesig_numberPicker;
    TextView bpmtext, popupmetronome_volume_text, popupmetronome_pan_text;
    Button taptempo_Button, popupmetronome_startstopbutton;
    SeekBar popupmetronome_volume, popupmetronome_pan;
    SwitchCompat visualmetronome;

    public static String[] bpmValues;
    public static int tempo;
    public static short bpm;
    public static String[] timesigvals;

    // Variables for tap tempo
    long new_time = 0;
    long time_passed = 0;
    long old_time = 0;
    int calc_bpm;
    int total_calc_bpm;
    int total_counts = 0;
    int av_bpm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            this.dismiss();
        }
        if (getDialog()==null) {
            dismiss();
        }

        getDialog().setCanceledOnTouchOutside(true);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mListener!=null) {
            mListener.pageButtonAlpha("metronome");
        }

        View V = inflater.inflate(R.layout.popup_page_metronome, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.metronome));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                doSave();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        // Initialise the views
        bpm_numberPicker = V.findViewById(R.id.bpm_numberPicker);
        timesig_numberPicker = V.findViewById(R.id.timesig_numberPicker);
        bpmtext = V.findViewById(R.id.bpmtext);
        taptempo_Button = V.findViewById(R.id.taptempo_Button);
        popupmetronome_volume = V.findViewById(R.id.popupmetronome_volume);
        popupmetronome_volume_text = V.findViewById(R.id.popupmetronome_volume_text);
        popupmetronome_pan = V.findViewById(R.id.popupmetronome_pan);
        popupmetronome_pan_text = V.findViewById(R.id.popupmetronome_pan_text);
        visualmetronome = V.findViewById(R.id.visualmetronome);
        popupmetronome_startstopbutton = V.findViewById(R.id.popupmetronome_startstopbutton);

        // Set up the default values
        popupmetronome_pan_text.setText(FullscreenActivity.metronomepan);
        String vol = (int)(FullscreenActivity.metronomevol * 100.0f) + "%";
        popupmetronome_volume_text.setText(vol);

        if (FullscreenActivity.metronomeonoff.equals("on")) {
            popupmetronome_startstopbutton.setText(getResources().getString(R.string.stop));
        } else {
            popupmetronome_startstopbutton.setText(getResources().getString(R.string.start));
        }
        ProcessSong.processTimeSig();
        tempo = Metronome.getTempo(FullscreenActivity.mTempo);
        setPan();
        popupmetronome_volume.setProgress(getVolume(FullscreenActivity.metronomevol));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            visualmetronome.setChecked(FullscreenActivity.visualmetronome);
        }
        getTimeSigValues();
        getBPMValues();

        // Set the listeners for changes
        taptempo_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tapTempo();
                FullscreenActivity.metronomeok = Metronome.isMetronomeValid();
            }
        });
        popupmetronome_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                FullscreenActivity.metronomevol = (float) i/100.0f;
                String text = i + "%";
                popupmetronome_volume_text.setText(text);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FullscreenActivity.metronomeok = Metronome.isMetronomeValid();
                Preferences.savePreferences();
            }
        });
        popupmetronome_pan.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                switch (i) {
                    case 0:
                        FullscreenActivity.metronomepan = "L";
                        break;
                    case 1:
                        FullscreenActivity.metronomepan = "C";
                        break;
                    case 2:
                        FullscreenActivity.metronomepan = "R";
                        break;
                }
                popupmetronome_pan_text.setText(FullscreenActivity.metronomepan);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FullscreenActivity.metronomeok = Metronome.isMetronomeValid();
                Preferences.savePreferences();
            }
        });
        visualmetronome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                FullscreenActivity.visualmetronome = b;
                FullscreenActivity.metronomeok = Metronome.isMetronomeValid();
                Preferences.savePreferences();
            }
        });
        popupmetronome_startstopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doSave();
                if (FullscreenActivity.metronomeonoff.equals("off") && FullscreenActivity.metronomeok) {
                    popupmetronome_startstopbutton.setText(getResources().getString(R.string.stop));
                    FullscreenActivity.metronomeonoff = "on";
                    FullscreenActivity.clickedOnMetronomeStart = true;
                    FullscreenActivity.whichbeat = "b";
                    Metronome.metroTask = new Metronome.MetronomeAsyncTask();
                    try {
                        Metronome.metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } catch (Exception e) {
                        Log.d ("d","Error starting metronmone");
                    }
                    Metronome.startstopVisualMetronome();
                } else if (FullscreenActivity.metronomeonoff.equals("on")) {
                    Runtime.getRuntime().gc();
                    popupmetronome_startstopbutton.setText(getResources().getString(R.string.start));
                    FullscreenActivity.metronomeonoff = "off";
                    FullscreenActivity.clickedOnMetronomeStart = false;
                    if (Metronome.metroTask!=null) {
                        Metronome.metroTask.stop();
                    }
                } else {
                    FullscreenActivity.myToastMessage = getString(R.string.error_notset);
                    ShowToast.showToast(getActivity());
                }
            }
        });

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doSave() {
        PopUpEditSongFragment.prepareSongXML();
        try {
            PopUpEditSongFragment.justSaveSongXML(getActivity());
            FullscreenActivity.myToastMessage = getResources().getString(R.string.edit_save) + " - " +
                    getResources().getString(R.string.ok);
        } catch (Exception e) {
            e.printStackTrace();
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.savesong) + " - " +
                    getActivity().getResources().getString(R.string.error);
        }
        Preferences.savePreferences();
        ShowToast.showToast(getActivity());
        dismiss();
    }

    @SuppressLint("SetTextI18n")
    public void tapTempo() {
        // This function checks the previous tap_tempo time and calculates the bpm
        new_time = System.currentTimeMillis();
        time_passed = new_time - old_time;
        calc_bpm = Math.round((1 / ((float) time_passed / 1000)) * 60);

        // Need to decide on the time sig.
        // If it ends in /2, then double the tempo
        // If it ends in /4, then leave as is
        // If it ends in /8, then half it
        // If it isn't set, set it to default as 4/4
        if (FullscreenActivity.mTimeSig.isEmpty()) {
            timesig_numberPicker.setValue(6);
        } else if (FullscreenActivity.mTimeSig.endsWith("/2")) {
            calc_bpm = (int) ((float) calc_bpm * 2.0f);
        } else if (FullscreenActivity.mTimeSig.endsWith("/8")) {
            calc_bpm = (int) ((float) calc_bpm / 2.0f);
        }

        if (time_passed < 2000) {
            total_calc_bpm += calc_bpm;
            total_counts++;
        } else {
            // Waited too long, reset count
            total_calc_bpm = 0;
            total_counts = 0;
        }

        av_bpm = Math.round((float) total_calc_bpm / (float) total_counts);

        if (av_bpm < 200 && av_bpm >= 40) {
            bpmtext.setText(getResources().getString(R.string.bpm));
            bpm_numberPicker.setValue(av_bpm-40);
            FullscreenActivity.mTempo = "" + av_bpm;
        } else if (av_bpm<40) {
            bpm_numberPicker.setValue(160);
            bpmtext.setText("<40 bpm");
        }  else if (av_bpm>199) {
            bpm_numberPicker.setValue(160);
            bpmtext.setText(">199 bpm");
        }

        bpm = (short) av_bpm;
        old_time = new_time;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (Metronome.metroTask!=null) {
            Metronome.metroTask.cancel(true);
        }
        if (Metronome.visualMetronome!=null) {
            Log.d("d","visualMetronome not null");
        }
        this.dismiss();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
        }
    }

    public void setPan(){
        switch (FullscreenActivity.metronomepan) {
            case "L":
                popupmetronome_pan.setProgress(0);
                popupmetronome_pan_text.setText("L");
                break;
            case "C":
            default:
                popupmetronome_pan.setProgress(1);
                popupmetronome_pan_text.setText("C");
                break;
            case "R":
                popupmetronome_pan.setProgress(2);
                popupmetronome_pan_text.setText("R");
                break;
        }
    }

    public int getVolume(float v) {
        return (int) (v*100.0f);
    }

    public void getBPMValues() {
        bpmValues = new String[161];
        // Add the values 40 to 199 bpm
        for (int z=0;z<160;z++) {
            bpmValues[z] = "" + (z+40);
        }
        // Now add the 'Not set' value
        bpmValues[160] = getResources().getString(R.string.notset);

        bpm_numberPicker.setMinValue(0); //from array first value

        //Specify the maximum value/number of NumberPicker
        bpm_numberPicker.setMaxValue(bpmValues.length-1); //to array last value

        //Specify the NumberPicker data source as array elements
        bpm_numberPicker.setDisplayedValues(bpmValues);
        bpm_numberPicker.setValue(tempo-40);
        bpm_numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (i1==160) {
                    // This is the not set value
                    tempo = 161;
                    bpm = 0;
                    FullscreenActivity.mTempo = "";
                } else {
                    FullscreenActivity.mTempo = "" + (i1+40);
                    bpm = (short) (i1+40);
                }
                FullscreenActivity.metronomeok = Metronome.isMetronomeValid();
                Preferences.savePreferences();
            }
        });
    }

    public void getTimeSigValues() {
        timesig_numberPicker.setMinValue(0);
        // Max value is 1 bigger as we have still to include the not set value
        String[] oldvals = getResources().getStringArray(R.array.timesig);
        timesigvals = new String[oldvals.length];
        System.arraycopy(oldvals, 0, timesigvals, 0, oldvals.length);
        timesigvals[0] = getResources().getString(R.string.notset);
        timesig_numberPicker.setMaxValue(timesigvals.length-1);
        timesig_numberPicker.setDisplayedValues(timesigvals);

        // Set the defaut value:
        int defpos = 0;

        for (int i=0;i<timesigvals.length;i++) {
            if (FullscreenActivity.mTimeSig.equals(timesigvals[i])) {
                defpos = i;
            }
        }
        timesig_numberPicker.setValue(defpos);

        timesig_numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                if (i1 == 0) {
                    // First value, which is not set
                    FullscreenActivity.mTimeSig = "";
                    FullscreenActivity.beats = 0;
                    FullscreenActivity.noteValue = 0;
                } else {
                    FullscreenActivity.mTimeSig = timesigvals[i1];
                    Metronome.setBeatValues();
                    Metronome.setNoteValues();
                }
                FullscreenActivity.metronomeok = Metronome.isMetronomeValid();
                Preferences.savePreferences();
            }
        });
    }

}