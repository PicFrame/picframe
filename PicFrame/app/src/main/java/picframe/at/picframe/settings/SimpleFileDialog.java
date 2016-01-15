/*
    Copyright (C) 2015 Myra Fuchs, Linda Spindler, Clemens Hlawacek, Ebenezer Bonney Ussher

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

package picframe.at.picframe.settings;

/*
* original Source:   http://www.scorchworks.com/Blog/simple-file-dialog-for-android-applications/
*   => heavily deleted/cleared/modified
*/

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import picframe.at.picframe.R;
import picframe.at.picframe.helper.local_storage.SD_Card_Helper;

public class SimpleFileDialog {
    private String m_sdcardDirectory = "";
    private Context m_context;

    private String m_dir = "";
    private List<String> m_subdirs = null;
    private SimpleFileDialogListener m_SimpleFileDialogListener = null;
    private ArrayAdapter<String> m_listAdapter = null;
    private AlertDialog.Builder myDialog;

    //////////////////////////////////////////////////////
    // Callback interface for selected directory
    //////////////////////////////////////////////////////
    public interface SimpleFileDialogListener {
        public void onChosenDir(String chosenDir);
    }

    public SimpleFileDialog(Context context, SimpleFileDialogListener SimpleFileDialogListener) {
        m_context = context;
//        m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_sdcardDirectory = new SD_Card_Helper().getExteralStoragePath();
//        m_sdcardDirectory = "/storage";
        m_SimpleFileDialogListener = SimpleFileDialogListener;

        try {
            m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
        } catch (IOException ioe) {
            System.err.println("Error getting the canonical path of >"+m_sdcardDirectory+"<");
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // chooseFile_or_Dir() - load directory chooser dialog for initial
    // default sdcard directory
    ///////////////////////////////////////////////////////////////////////
    public void chooseFile_or_Dir() {
        // Initial directory is sdcard directory
        if (m_dir.equals(""))	chooseFile_or_Dir(m_sdcardDirectory);
        else chooseFile_or_Dir(m_dir);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // chooseFile_or_Dir(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    ////////////////////////////////////////////////////////////////////////////////
    public void chooseFile_or_Dir(String dir) {
        File dirFile = new File(dir);
        if (! dirFile.exists() || ! dirFile.isDirectory()) {
            dir = m_sdcardDirectory;
        }

        try {
            dir = new File(dir).getCanonicalPath();
        } catch (IOException ioe) {
            System.err.println("Error getting the canonical path of >"+dir+"<");
            return;
        }

        m_dir = dir;
        m_subdirs = getDirectories(dir);

        class SimpleFileDialogOnClickListener implements DialogInterface.OnClickListener {
            public void onClick(DialogInterface dialog, int item) {
                String sel = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
                if (sel.charAt(sel.length()-1) == '/')	sel = sel.substring(0, sel.length()-1);

                // Navigate into the sub-directory
                if (sel.equals("..")) {
                    m_dir = m_dir.substring(0, m_dir.lastIndexOf("/"));
                } else {
                    m_dir += "/" + sel;
                }
                updateDirectory();
            }
        }

        SimpleFileDialogOnClickListener onClickListener = new SimpleFileDialogOnClickListener();
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
                            if (m_SimpleFileDialogListener != null) {
                                m_SimpleFileDialogListener.onChosenDir(m_dir);
                            }
                    }
                });

        final AlertDialog dirsDialog = myDialog.create();
        // Show directory chooser dialog
        dirsDialog.show();
    }

    private List<String> getDirectories(String dir) {
        List<String> dirs = new ArrayList<String>();
        try {
            File dirFile = new File(dir);
            // if parent directory is not root, add ".." for going up one directory
            if (!dirFile.getParent().equals("/")) {
                dirs.add("..");
            }
            if (! dirFile.exists() || ! dirFile.isDirectory()) {
                return dirs;
            }
            for (File file : dirFile.listFiles()) {
                if ( file.isDirectory()) {      // Add "/" to directory names to identify them in the list
                    if (!(file.getName().startsWith("."))) {
                        dirs.add( file.getName() + "/" );
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error appeared while browsing dir: " + dir);
            e.printStackTrace();
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
        m_subdirs.addAll(getDirectories(m_dir));
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