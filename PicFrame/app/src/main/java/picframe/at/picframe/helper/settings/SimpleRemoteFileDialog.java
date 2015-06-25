/*    Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher

        This file is part of PicFrame.

        PicFrame is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        PicFrame is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with PicFrame.  If not, see <http://www.gnu.org/licenses/>.
*/


package picframe.at.picframe.helper.settings;

/*
* original Source:   http://www.scorchworks.com/Blog/simple-file-dialog-for-android-applications/
*   => heavily deleted/cleared/modified
*/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import picframe.at.picframe.R;

public class SimpleRemoteFileDialog {
    private Context m_context;

    private RemoteFolder currDir; //former m_dir
    private List<String> m_subdirs = null;
    private SimpleRemoteFileDialogListener m_SimpleRemoteFileDialogListener = null;
    private ArrayAdapter<String> m_listAdapter = null;
    private AlertDialog.Builder myDialog;

    //////////////////////////////////////////////////////
    // Callback interface for selected directory
    //////////////////////////////////////////////////////
    public interface SimpleRemoteFileDialogListener {
        public void onChosenDir(RemoteFolder chosenDir);
    }

    public SimpleRemoteFileDialog(Context context, SimpleRemoteFileDialogListener SimpleRemoteFileDialogListener) {
        m_context = context;
        m_SimpleRemoteFileDialogListener = SimpleRemoteFileDialogListener;

    }

    ///////////////////////////////////////////////////////////////////////
    // chooseFile_or_Dir() - load directory chooser dialog for initial
    // default sdcard directory
    ///////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////
    // chooseFile_or_Dir(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    ////////////////////////////////////////////////////////////////////////////////
    public void chooseFile_or_Dir(RemoteFolder dir) {


        m_subdirs = getDirectories(dir);

        class SimpleRemoteFileDialogOnClickListener implements OnClickListener {
            public void onClick(DialogInterface dialog, int item) {
                String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                if (sel.charAt(sel.length()-1) == '/')	sel = sel.substring(0, sel.length()-1);

                // Navigate into the sub-directory
                if (sel.equals("..")) {
                    currDir = currDir.parent;
                } else {

                    for(RemoteFolder r : currDir.children) {
                        if(r.name.equals(sel)) {
                            currDir = r;
                            break;
                        }
                    }
                }
                updateDirectory();
            }
        }

        SimpleRemoteFileDialogOnClickListener onClickListener = new SimpleRemoteFileDialogOnClickListener();
        AlertDialog.Builder myDialog = new AlertDialog.Builder(m_context);
        m_listAdapter = createListAdapter(m_subdirs);
        myDialog.setTitle(m_context.getString(R.string.sett_dialog_title))
                .setCancelable(true)
                .setSingleChoiceItems(m_listAdapter, -1, onClickListener)
                .setNegativeButton(R.string.sett_dialog_negBtn, null)
                .setPositiveButton(R.string.sett_dialog_posBtn, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Current directory chosen => Call registered listener supplied with the chosen directory
                            if (m_SimpleRemoteFileDialogListener != null) {
                                m_SimpleRemoteFileDialogListener.onChosenDir(currDir);
                            }
                    }
                });

        final AlertDialog dirsDialog = myDialog.create();
        // Show directory chooser dialog
        dirsDialog.show();
    }

    private List<String> getDirectories(RemoteFolder dir) {
        List<String> dirs = new ArrayList<String>();

            // if directory is not the base sd card directory add ".." for going up one directory
            if (dir.parent != null) {
                dirs.add("..");
            }
            if (dir.children == null || dir.children.isEmpty()) {
                return dirs;
            }
            for (RemoteFolder folder : dir.children) {
                    if (!(folder.name.startsWith("."))) {
                        dirs.add( folder.name + "/" );
                    }
                }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return dirs;
    }

    private void updateDirectory() {
        m_subdirs.clear();
        m_subdirs.addAll(getDirectories(currDir));
        //myDialog.setTitle(m_context.getString(R.string.sett_dialog_title) + "\n" + m_dir); // can't update normal Dialog Title once shown
        //m_tv_Path.setText(m_dir);
        m_listAdapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> createListAdapter(List<String> items) {
        return new ArrayAdapter<String>(m_context, android.R.layout.select_dialog_item, android.R.id.text1, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    float myDensity = m_context.getResources().getDisplayMetrics().density;
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, (55/myDensity));
                //    tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
                    tv.getLayoutParams().height =  (int)(tv.getTextSize()*2.5);
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }
}