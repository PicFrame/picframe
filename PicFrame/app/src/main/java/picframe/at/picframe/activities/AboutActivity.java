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

package picframe.at.picframe.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import picframe.at.picframe.R;


@SuppressWarnings("deprecation")
public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

    }

    public void showLicenseDialog(View view) {
        AlertDialog.Builder shortLicenseDialog = new AlertDialog.Builder(AboutActivity.this);
        shortLicenseDialog
                .setCancelable(true)
                .setMessage(R.string.about_gpl_license_short)
                .setPositiveButton(R.string.about_licenses_dialog_button_1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder secondDialog = new AlertDialog.Builder(AboutActivity.this);
                        secondDialog.setMessage(R.string.about_license_gpl_full);
                        AlertDialog myDialog = secondDialog.show();
                        TextView message = (TextView)myDialog.findViewById(android.R.id.message);
                        message.setGravity(Gravity.CENTER);
                    }
                })
                .setNeutralButton(R.string.about_licenses_dialog_button_2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AlertDialog.Builder secondDialog = new AlertDialog.Builder(AboutActivity.this);
                        secondDialog.setMessage(R.string.about_license_thirdparty);
                        AlertDialog myDialog = secondDialog.show();
                        TextView message = (TextView)myDialog.findViewById(android.R.id.message);
                        message.setGravity(Gravity.CENTER);
                    }
                });
        AlertDialog myDialog = shortLicenseDialog.show();
        TextView message = (TextView)myDialog.findViewById(android.R.id.message);
        message.setGravity(Gravity.CENTER);
    }
}
