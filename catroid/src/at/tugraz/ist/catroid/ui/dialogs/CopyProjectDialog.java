/**
 *  Catroid: An on-device graphical programming language for Android devices
 *  Copyright (C) 2010-2011 The Catroid Team
 *  (<http://code.google.com/p/catroid/wiki/Credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://www.catroid.org/catroid_license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *   
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.tugraz.ist.catroid.ui.dialogs;

import java.io.File;
import java.io.IOException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import at.tugraz.ist.catroid.R;
import at.tugraz.ist.catroid.io.StorageHandler;
import at.tugraz.ist.catroid.utils.UtilFile;
import at.tugraz.ist.catroid.utils.Utils;

public class CopyProjectDialog extends TextDialog {

	private class CopyProjectAsyncTask extends AsyncTask<String, Void, Boolean> {
		//ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... array) {
			String newProjectName = array[0];

			try {
				UtilFile.copyProject(newProjectName, oldProjectName);
			} catch (IOException exception) {
				UtilFile.deleteDirectory(new File(Utils.buildProjectPath(newProjectName)));
				Log.e("CATROID", "Error while copying project, destroy newly created directories.", exception);
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}

			if (result) {
				if (onCopyProjectListener != null) {
					onCopyProjectListener.onCopyProject();
				}
			}

			if (!result) {
				Utils.displayErrorMessage(getActivity(), getString(R.string.error_copy_project));
			}

		}
	}

	private static final String BUNDLE_ARGUMENTS_OLD_PROJECT_NAME = "old_project_name";
	public static final String DIALOG_FRAGMENT_TAG = "dialog_copy_project";

	private OnCopyProjectListener onCopyProjectListener;

	private String oldProjectName;
	private CopyProjectAsyncTask currentCopyProjectAsyncTask;
	ProgressDialog progressDialog;

	public static CopyProjectDialog newInstance(String oldProjectName) {
		CopyProjectDialog dialog = new CopyProjectDialog();

		Bundle arguments = new Bundle();
		arguments.putString(BUNDLE_ARGUMENTS_OLD_PROJECT_NAME, oldProjectName);
		dialog.setArguments(arguments);
		return dialog;
	}

	public void setOnCopyProjectListener(OnCopyProjectListener listener) {
		onCopyProjectListener = listener;
	}

	@Override
	protected void initialize() {
		oldProjectName = getArguments().getString(BUNDLE_ARGUMENTS_OLD_PROJECT_NAME);
		input.setText(oldProjectName);
	}

	@Override
	protected boolean handleOkButton() {
		String newProjectName = (input.getText().toString()).trim();

		if (newProjectName.equalsIgnoreCase("")) {
			Utils.displayErrorMessage(getActivity(), getString(R.string.notification_invalid_text_entered));
			return false;
		} else if (StorageHandler.getInstance().projectExistsIgnoreCase(newProjectName)) {
			Utils.displayErrorMessage(getActivity(), getString(R.string.error_project_exists));
			return false;
		}

		if (newProjectName != null && !newProjectName.equalsIgnoreCase("")) {

			String title = getString(R.string.please_wait);
			String message = getString(R.string.copying);
			progressDialog = ProgressDialog.show(getActivity(), title, message);

			currentCopyProjectAsyncTask = new CopyProjectAsyncTask();
			currentCopyProjectAsyncTask.execute(newProjectName);
		} else {
			Utils.displayErrorMessage(getActivity(), getString(R.string.notification_invalid_text_entered));
			return false;
		}
		return true;
	}

	@Override
	protected String getTitle() {
		return getString(R.string.copy_project);
	}

	@Override
	protected String getHint() {
		return null;
	}

	public interface OnCopyProjectListener {

		public void onCopyProject();

	}
}