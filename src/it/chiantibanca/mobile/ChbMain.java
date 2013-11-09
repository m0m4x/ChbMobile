package it.chiantibanca.mobile;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import android.support.v4.app.FragmentManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class ChbMain extends FragmentActivity 
		implements ChkDisclaimerDialogFragment.NoticeDialogListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Controllo Disclaimer
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		boolean accepted = sharedPref.getBoolean("pref_disclaimer_choice",
				false);
		if (accepted) {
			// OK Start Activity
			Intent chbmobile = new Intent(this, ChbMobile.class);
			chbmobile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			chbmobile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(chbmobile);
			this.finish();
		} else {
			// NO Display Dialogs
			DialogFragment ChkDisclaimer = new ChkDisclaimerDialogFragment();
			FragmentManager fm = getSupportFragmentManager();
			ChkDisclaimer.show(fm, "ChkDisclaimer");
		}
	}

	@Override
	public void onChkDisclaimerDialogPositiveClick(
			ChkDisclaimerDialogFragment dialog) {
		// Salva pref
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putBoolean("pref_disclaimer_choice", true);
		editor.commit();
		// Start Activity
		Intent chbmobile = new Intent(this, ChbMobile.class);
		chbmobile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		chbmobile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(chbmobile);
		this.finish();
	}

	@Override
	public void onChkDisclaimerDialogNegativeClick(
			ChkDisclaimerDialogFragment dialog) {
		finish();
	}
}

class ChkDisclaimerDialogFragment extends DialogFragment {
	
    public ChkDisclaimerDialogFragment() {
        // Empty constructor required for DialogFragment
    }

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface NoticeDialogListener {
		public void onChkDisclaimerDialogPositiveClick(
				ChkDisclaimerDialogFragment dialog);

		public void onChkDisclaimerDialogNegativeClick(
				ChkDisclaimerDialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	NoticeDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (NoticeDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		/*
		 * super.onCreate(savedInstanceState); PageData = (Message)
		 * getArguments().get("PageData");
		 */

		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		return builder
				.setMessage(R.string.dialog_disclaimer_message)
				.setCancelable(false)
				.setTitle(R.string.dialog_disclaimer_title)
				.setPositiveButton(R.string.dialog_accept,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User clicked OK button
								// Send the positive button event back to the
								// host activity
								mListener
										.onChkDisclaimerDialogPositiveClick(ChkDisclaimerDialogFragment.this);
							}
						})
				.setNegativeButton(R.string.dialog_reject,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// User cancelled the dialog
								// Send the negative button event back to the
								// host activity
								mListener
										.onChkDisclaimerDialogNegativeClick(ChkDisclaimerDialogFragment.this);
							}
						}).create();
	}

}