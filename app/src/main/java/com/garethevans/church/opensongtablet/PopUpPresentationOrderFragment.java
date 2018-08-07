package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.provider.DocumentFile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PopUpPresentationOrderFragment extends DialogFragment {

    static PopUpPresentationOrderFragment newInstance() {
        PopUpPresentationOrderFragment frag;
        frag = new PopUpPresentationOrderFragment();
        return frag;
    }

    public interface MyInterface {
        void updatePresentationOrder();
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

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    TextView m_mPresentation;
    TextView popuppresorder_presorder_title;
    LinearLayout root_buttonshere;
    Button deletePresOrder;

    StorageAccess storageAccess;
    DocumentFile homeFolder;

    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        storageAccess = new StorageAccess();
        homeFolder = storageAccess.getHomeFolder(getActivity());
        View V = inflater.inflate(R.layout.popup_presentation_order, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.edit_song_presentation));
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

        // Define the views
        root_buttonshere = V.findViewById(R.id.songsectionstoadd);
        m_mPresentation = V.findViewById(R.id.popuppres_mPresentation);
        popuppresorder_presorder_title = V.findViewById(R.id.popuppresorder_presorder_title);
        deletePresOrder = V.findViewById(R.id.deletePresOrder);

        // Set the values
        popuppresorder_presorder_title.setText(FullscreenActivity.mTitle);
        m_mPresentation.setText(FullscreenActivity.mPresentation);

        // Set the buttons up
        int numbuttons = FullscreenActivity.foundSongSections_heading.size();
        for (int r=0;r<numbuttons;r++) {
            Button but = new Button(getActivity());
            but.setId(r);
            but.setText(FullscreenActivity.foundSongSections_heading.get(r));
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                but.setTextAppearance(getActivity(), android.R.style.TextAppearance_Small);
            } else {
                but.setTextAppearance(android.R.style.TextAppearance_Small);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                but.setBackground(getActivity().getDrawable(R.drawable.green_button));
            } else {
                but.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.green_button));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(12, 12, 12, 12);
            but.setLayoutParams(params);
            but.setMinHeight(0);
            but.setMinimumHeight(0);
            but.setPadding(5, 5, 5, 5);
            but.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int whichview = v.getId();
                    String currpres = m_mPresentation.getText().toString();
                    String addthis = currpres.trim() + " " + FullscreenActivity.foundSongSections_heading.get(whichview);
                    m_mPresentation.setText(addthis);
                }
            });
            root_buttonshere.addView(but);
        }

        deletePresOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_mPresentation.setText("");
            }
        });
        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doSave() {
        FullscreenActivity.mPresentation = m_mPresentation.getText().toString().trim();
        PopUpEditSongFragment.prepareSongXML();
        try {
            PopUpEditSongFragment.justSaveSongXML(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
            FullscreenActivity.myToastMessage = getActivity().getResources().getString(R.string.savesong) + " - " +
                    getActivity().getResources().getString(R.string.error);
            ShowToast.showToast(getActivity());
        }
       try {
            LoadXML loadXML = new LoadXML();
           loadXML.loadXML(getActivity(),homeFolder);
       } catch (Exception e) {
           e.printStackTrace();
       }
        if (mListener!=null) {
            mListener.updatePresentationOrder();
        }
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}