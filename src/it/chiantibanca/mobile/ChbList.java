package it.chiantibanca.mobile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ChbList extends ListActivity  {
	 private List<String> path = null;
	 private File DownloadDir;
	 private String root="";
	 private TextView myPath;
	 
	 SharedPreferences DefaultSharedPref;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chb_list);
		
        // Tema Pelle
        Boolean pref_leathertheme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("pref_useleathertheme", false);
        if(pref_leathertheme){
        	View contentView = (View) findViewById(R.id.chb_list);
        	contentView.setBackgroundResource(R.drawable.leather_layer);
        }
		
        DownloadDir = new File (Environment.getExternalStorageDirectory(), getPackageName());
        if (!DownloadDir.exists()) { DownloadDir.mkdir(); }
        root = DownloadDir.getAbsolutePath();
        myPath = (TextView)findViewById(R.id.path);
        
        DefaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 

        getDir(root);
        
        registerForContextMenu(getListView());
        
	}
	
	private void getDir(String dirPath)
	    {
	     myPath.setText("Location: " + dirPath);
	     
	     getListView().setAdapter(null);
	     
	     //Files
	     File f = new File(dirPath);
	     File[] files = f.listFiles();
	     path = new ArrayList<String>();
	     
	     //Order
	     final String sortType = DefaultSharedPref.getString("chb_list_order_sortType", "alfa");
	     final boolean sortAsc = DefaultSharedPref.getBoolean("chb_list_order_sortAsc", true);
	     Comparator<File> comparator = new Comparator<File>(){
			    public int compare(File f1, File f2)
			    {
			    	if (sortType == "data") {
				    	if (sortAsc) {
		                    return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		                } else {
		                    return -1 * Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		                }
			    	} else {
			    		if (sortAsc) {
		                    return f1.getName().compareTo(f2.getName());
		                } else {
		                    return -1 * f1.getName().compareTo(f2.getName());
		                }
			    	}
			    }};
	     Arrays.sort(files, comparator);
	     
	     // Create the item mapping
	     String[] from = new String[] { "title", "description", "data" };
	     int[] to = new int[] { R.id.rowtitle, R.id.rowdescription, R.id.rowdata };
	     
	     List<HashMap<String, Object>> fillMaps = new ArrayList<HashMap<String, Object>>();
	     HashMap<String, Object> map = new HashMap<String, Object>();
	     
	     for(int i=0; i < files.length; i++)
	     {
	       File file = files[i];
	       if(!file.isDirectory()) {
	    	   if(file.getName().endsWith("pdf")){
	    		    map = new HashMap<String, Object>();
	    		    map.put("title", file.getName());
	    		    map.put("description", "Documento da InBank");
	    		    map.put("data", new SimpleDateFormat("dd/MM/yyyy hh:mm").format(
	    		    	    														new Date(file.lastModified()) 
	    		    																));
	    		    fillMaps.add(map);
	    		    path.add(file.getPath());
	    	   }
	       }
	     }
	     
	     SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_chb_list_row, from, to);
	     setListAdapter(adapter);
	     
	     /*
	     item = new ArrayList<String>();
	     path = new ArrayList<String>();
	     
	     if(!dirPath.equals(root))
	     {
	      item.add(root);
	      path.add(root);    
	     }
	     
	     		   item.add(file.getName());
		    	   path.add(file.getPath());
		    	   
	     ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.list_row, item);
	     setListAdapter(fileList);
	     */
	    }
	  
	  @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

	   File file = new File(path.get(position));

	   if (file.isDirectory())
	   {
		   //NON FARE NIENTE
	   }
	   else
	   {
		   Intent target = new Intent(Intent.ACTION_VIEW);
		   target.setDataAndType(Uri.fromFile(file),"application/pdf");
		   target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		   Intent intent = Intent.createChooser(target, "Open File");
		   try {
		       startActivity(intent);
		   } catch (ActivityNotFoundException e) {
		       // Instruct the user to install a PDF reader here, or something
	    		 Toast toast = Toast.makeText(getBaseContext(), "Nessun Reader Pdf installato...", Toast.LENGTH_SHORT);
	    		 toast.show();
		   }   
	   }
	  }
	
	  
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
        switch (item.getItemId()) {
	        case R.id.menu_settings:
	            i = new Intent(this, ChbSettings.class);
	            startActivityForResult(i, 0);
	            break;
	        case R.id.menu_list:
	            i = new Intent(this, ChbList.class);
	            startActivityForResult(i, 0);
	            break;

        }
        return true;
    }
	
    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	    ContextMenuInfo menuInfo) {
		  if (v.getId()==getListView().getId()) {
				menu.setHeaderTitle("Azioni disponibili");
		    	getMenuInflater().inflate(R.menu.chb_list_contextmenu, menu);
		  }
	}
	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	final File selectedFile = new File(path.get(info.position)); 
        switch (item.getItemId()) {
        	case R.id.chb_list_contextmenu_share:
        		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        		shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(selectedFile.getPath()));
        		shareIntent.setType("application/pdf");
        		startActivity(Intent.createChooser(shareIntent, "Share images to.."));
        		break;
	        case R.id.chb_list_contextmenu_rename:
	        	
	        	// get prompts.xml view
				LayoutInflater li = LayoutInflater.from(ChbList.this);
				View promptsView = li.inflate(R.layout.dialog_rename, null);
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChbList.this);
				
				// set prompts.xml to alertdialog builder
				alertDialogBuilder.setView(promptsView);
				// Valore default
				final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
				userInput.setText(getFileNameNoExtension(selectedFile));
	        	// set dialog message
				alertDialogBuilder
					.setCancelable(false)
					.setPositiveButton("OK",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
					    	//Rename
					    	selectedFile.renameTo(new File(selectedFile.getParent() + "/" + userInput.getText() + getFileExtension(selectedFile) ));
					    	getDir(root);
					    }
					  })
					.setNegativeButton("Cancel",
					  new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					    }
					  });
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				// show it
				alertDialog.show();
	        	
	            break;
	        case R.id.chb_list_contextmenu_delete:
	        	selectedFile.delete();
	        	getDir(root);
	            break;
	        case R.id.chb_list_contextmenu_order:
	        	final CharSequence[] items = {"Data (0-9)", "Data (9-0)", "Nome (A-Z)", "Nome (Z-A)" };
	        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        	builder.setTitle("Scegli ordinamento...")
	        	    .setSingleChoiceItems(items, DefaultSharedPref.getInt("chb_list_order_choice", 2) , new DialogInterface.OnClickListener() {
	        	    	SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
	        	        public void onClick(DialogInterface dialogInterface, int item) {
			        	   	 switch (item) {
			        	    	 case 0:
			        	    		 sharedPrefeditor.putInt("chb_list_order_choice", 0);
			        	    		 sharedPrefeditor.putString("chb_list_order_sortType", "data");
			        	    		 sharedPrefeditor.putBoolean("chb_list_order_sortAsc", true);
			        	    		 break;
			        	    	 case 1:
			        	    		 sharedPrefeditor.putInt("chb_list_order_choice", 1);
			        	    		 sharedPrefeditor.putString("chb_list_order_sortType", "data");
			        	    		 sharedPrefeditor.putBoolean("chb_list_order_sortAsc", false);
			        	    		 break;
			        	    	 case 2:
			        	    		 sharedPrefeditor.putInt("chb_list_order_choice", 2);
			        	    		 sharedPrefeditor.putString("chb_list_order_sortType", "alfa");
			        	    		 sharedPrefeditor.putBoolean("chb_list_order_sortAsc", true);
			        	    		 break;
			        	    	 case 3:
			        	    		 sharedPrefeditor.putInt("chb_list_order_choice", 3);
			        	    		 sharedPrefeditor.putString("chb_list_order_sortType", "alfa");
			        	    		 sharedPrefeditor.putBoolean("chb_list_order_sortAsc", false);
			        	    		 break;
			        	     };
			        	    sharedPrefeditor.commit();
	        	            getDir(root);
	        	        }
	        	    });
	        	 
	        	builder.create().show();
	            break;
        }
        return true;
    }
    
    private String getFileExtension(File filename){
    	String filepath = filename.getName();
    	int i = filepath.lastIndexOf('.');
    	if (i > 0 && filename.getName().length()-i<6) {
    	    return filepath.substring(i+1);
    	} else {
    		return "";
    	}
    }
    
    private String getFileNameNoExtension(File filename){
    	String filepath = filename.getName();
    	int i = filepath.lastIndexOf('.');
    	if (i > 0 && filename.getName().length()-i<6) {
    	    return filepath.substring(0,i-1);
    	} else {
    		return "";
    	}
    }

}
