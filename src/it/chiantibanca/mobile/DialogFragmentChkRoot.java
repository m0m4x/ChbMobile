package it.chiantibanca.mobile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DialogFragmentChkRoot extends DialogFragment {

    public DialogFragmentChkRoot() {
        // Empty constructor required for DialogFragment
    }

	
	/* The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks.
	 * Each method passes the DialogFragment in case the host needs to query it. */
	public interface NoticeDialogListener {
	    public void onChkRootDialogPositiveClick(DialogFragmentChkRoot dialog);
	    public void onChkRootDialogNegativeClick(DialogFragmentChkRoot dialog);
	}
	
	// Use this instance of the interface to deliver action events
	NoticeDialogListener mListener;
	
	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    // Verify that the host activity implements the callback interface
	    try {
	        // Instantiate the NoticeDialogListener so we can send events to the host
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
        super.onCreate(savedInstanceState);
        PageData = (Message) getArguments().get("PageData");
        */
        
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 	 	return builder.setMessage(R.string.dialog_chkroot_message)
     	        .setTitle(R.string.dialog_chkroot_title)
     	        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
	                     @Override
						public void onClick(DialogInterface dialog, int id) {
	                         // User clicked OK button
	                         // Send the positive button event back to the host activity
	                         mListener.onChkRootDialogPositiveClick(DialogFragmentChkRoot.this);
	                     }
     	        })
     	        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
	                     @Override
						public void onClick(DialogInterface dialog, int id) {
	                         // User cancelled the dialog
	                         // Send the negative button event back to the host activity
	                         mListener.onChkRootDialogNegativeClick(DialogFragmentChkRoot.this);
	                     }
     	        })
 	 			.create(); 	 
	}
  
}