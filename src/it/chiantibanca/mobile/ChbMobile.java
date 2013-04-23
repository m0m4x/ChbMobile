package it.chiantibanca.mobile;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.*;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class ChbMobile extends Activity 
					implements 	SaveCredentialsDialogFragment.NoticeDialogListener,
					 			ChkRootDialogFragment.NoticeDialogListener {

	WebView myWebView;
	WebView myLoadView;
	static ViewSwitcher switcher;
	
	public int loaded_page = 0;
	
	static View layout1;
	static View layout2;
	
    private Bundle LastLoginData;
    private Boolean ToValidateLoginData;
	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chb_mobile);
        layout1=findViewById(R.id.layoutload);
        layout2=findViewById(R.id.layoutview);
        
        //Controllo Root
        Root ChkRoot = new Root();
        if (ChkRoot.isDeviceRooted() == true) {
			 //Chiedi Root
			 DialogFragment newFragment = new ChkRootDialogFragment();
			 newFragment.show(getFragmentManager(), "ChkRootDialog");
        }
        
        //Preferenze Iniziali
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        
        LastLoginData = null;
        ToValidateLoginData = false;
        
        // WebView
	        //Impostazioni
	        myWebView = (WebView) findViewById(R.id.webview);
	        WebSettings webSettings = myWebView.getSettings();
	        	webSettings.setJavaScriptEnabled(true);
	        	webSettings.setUserAgentString("ChiantiBanca Mobile App");
	        	webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
	        	//webSettings.set
	        myWebView.setWebViewClient(new MyWebViewClient());
	        myWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
	        //Carica pagina
	        myWebView.loadUrl("https://www.inbank.it/mobi/flow?_flowId=AccessFlow&_flowExecutionKey=e1s1");
        
        // LoadView
	        myLoadView = (WebView) findViewById(R.id.loadview);
	        String url_loading = "file:///android_asset/html/loading.html";
	        myLoadView.loadUrl(url_loading);
	        
	    //debug
         final Button button = (Button) findViewById(R.id.buttondbg);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
			public void onClick(View v) {
                 // Perform action on click
            	 
             	Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("action", "js");
                b.putString("arg1", "simloadok");
                msg.setData(b);
                
                // send message to the handler with the current message handler
                handler.sendMessage(msg);
            	 
             }
         });
        
         switcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
         
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.menu_settings:
            Intent i = new Intent(this, SettingsActivity.class);
            startActivityForResult(i, 0);
            break;
 
        }
 
        return true;
    }
    
    @Override
    public void onBackPressed (){
    	if((loaded_page > 0) && (myWebView.canGoBack())){ 
	        myWebView.goBack();   
        } else {
        	finish();
        }
    }
    
    @Override
    public void onDestroy(){
    	if(loaded_page > 0){
    		Thread longThread = new Thread(longOperation);
    		longThread.start();
    	}
    	super.onDestroy();
    }
    
    @Override
    public void onResume(){
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(sharedPref.getBoolean("pref_vcredentials_needreset", false) == true){
			SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
			sharedPrefeditor.putBoolean("pref_vcredentials_needreset", false);
			sharedPrefeditor.commit();
			
			//Imposta campo password come testo (o vice) alle pagine gia caricate
			if(sharedPref.getBoolean("pref_vcredentials", false) == true){
				myWebView.loadUrl("javascript:"+
		            	"	var elemDOM = document.getElementById('id_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('old_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('confirm_new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }"+
		            	"");
			}else{
				myWebView.loadUrl("javascript:"+
		            	"	var elemDOM = document.getElementById('id_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
		            	"	var elemDOM = document.getElementById('old_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
		            	"	var elemDOM = document.getElementById('new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
		            	"	var elemDOM = document.getElementById('confirm_new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }"+
		            	"");
			}
		}
    	super.onResume();
    }
    
    
    private final  Runnable longOperation = new Runnable(){
        @Override
        public void run() {
              myWebView.loadUrl("https://www.inbank.it/mobi/flow?_flowId=LogoutFlow");
        }
    };
    
    
    // Handler Javascript 
    //  Acquisisce messaggi da interfaccia Javascript e gestisce la UI
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {
    	

    	if (msg.getData().getString("action") == "js") {
         
    		String body = msg.getData().getString("arg2", "");
    		String successText = msg.getData().getString("arg3", "");
        	//Indentifica la pagina caricata (per tasto back)
	    		//mobile clearfix
	    		//bodyMobile pass-page
	    		//bodyMobile
	        	if(body.contains("clearfix")){
	        		loaded_page = 0;
	        	} else if (body.contains("pass-page")) {
	        		loaded_page = 1;
	        	} else if (body.contains("bodyMobile")) {
	        		loaded_page = 2;
	        	}
        	
	        //Validazione cambio password
        	if(	ToValidateLoginData == true && 
        		successText.contains("successo") && 
        		LastLoginData != null){
        		
        		 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        		 SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
        		 sharedPrefeditor.putString("cred_user", LastLoginData.getString("arg1"));
            	 sharedPrefeditor.putString("cred_pass", LastLoginData.getString("arg2"));
            	 sharedPrefeditor.commit();
        	}
        			
    		//Visualizza Webview
        	 if (msg.getData().getString("arg1") == "loadok" || msg.getData().getString("arg1") == "simloadok") {
    			 if (switcher.getCurrentView() != layout2)
    				 switcher.showNext();
        	 }
        	 
    	} else if (msg.getData().getString("action") == "say")  {
        	 
    		 Toast toast = Toast.makeText(getBaseContext(), msg.getData().getString("arg1"), Toast.LENGTH_SHORT);
    		 toast.show();
    		
    	} else if (msg.getData().getString("action") == "login")  {
            	 
    		 SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
             Boolean pref_rcredentials_choice = sharedPref.getBoolean("pref_rcredentials_choice", false);
             Boolean pref_rcredentials = sharedPref.getBoolean("pref_rcredentials", false);
       	     
             //Chiedi la prima volta
             if (pref_rcredentials_choice == false) {
            	 
            	 LastLoginData = msg.getData();
            	 
            	 //Lascia fare tutto al Dialog
            	 DialogFragment newFragment = new SaveCredentialsDialogFragment();
            	 newFragment.show(getFragmentManager(), "SaveCredentialsDialog");
            	 
             } else if(pref_rcredentials == true) {
            	 
            	 LastLoginData = msg.getData();
            	 
                 //Salva Utente e password
            	 SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
            	 String newuser = msg.getData().getString("arg1", null);
            	 if (newuser.equals("refresh")) {
            		 //Non aggiornare niente - Imposta flag da aggiornare password
            		 ToValidateLoginData = true;
            	 }else{
            		 sharedPrefeditor.putString("cred_user", msg.getData().getString("arg1"));
                	 sharedPrefeditor.putString("cred_pass", msg.getData().getString("arg2"));
                	 sharedPrefeditor.commit();
            	 }
                 
             }
             
    		 /*
    		 Toast toast = Toast.makeText(getBaseContext(), "pref "+pref_rcredentials.toString()+"saved"+msg.getData().getString("arg1")+msg.getData().getString("arg2"), Toast.LENGTH_SHORT);
        	 toast.show();
        	 */
        	 
        }
    	 
 
    	 
        }
    };
    
    
    // Interfaccia Javascript
    //  Fornisce un metodi per la pagina web e inoltra i relativi messaggi all'Handler
    class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }

        public void OkDone(String body,String successText) {
        	
	       	//Toast toast = Toast.makeText(getBaseContext(), "loaded", Toast.LENGTH_SHORT);
	   		//toast.show();
        	
        	Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("action", "js");
            b.putString("arg1", "loadok");
            b.putString("arg2", body);
            b.putString("arg3", successText);
            msg.setData(b);
            
            // send message to the handler with the current message handler
            handler.sendMessage(msg);
        	
        }
        
        public void SayThis(String phrase) {
        	
        	Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("action", "say");
            b.putString("arg1", phrase);
            msg.setData(b);
            
            // send message to the handler with the current message handler
            handler.sendMessage(msg);
        	
        }
        
        //Ricorda le credenziali
        public void ActionLogin(String user,String pass) {
        	
        	//Invia al salvataggio le nuove Credenziali tramite oggetto Bundle
        	Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("action", "login");
            b.putString("arg1", user);
            b.putString("arg2", pass);
            msg.setData(b);
            
            // send message to the handler with the current message handler
            handler.sendMessage(msg);
            
	       	//Toast toast = Toast.makeText(getBaseContext(), "actionlogin", Toast.LENGTH_SHORT);
	   		//toast.show();
        	
        }
    }
    
    
    // Componente Browser
    //  Visualizza le pagine web
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("www.inbank.it") | Uri.parse(url).getHost().equals("www.chiantibanca.it") ) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
        
        @Override
        public void onPageStarted (WebView view, String url, Bitmap favicon) {
        	
        	if (switcher.getCurrentView() != layout1)
        		switcher.showPrevious();   
        	
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
            // do your stuff here
        	/*Context context = getApplicationContext();
        	CharSequence text = "Caricamento finito,personalizzo!";
        	int duration = Toast.LENGTH_SHORT;
        	Toast toast = Toast.makeText(context, text, duration);
        	toast.show();*/
        	//Log.d("Finito", "Iteration ");
        	/*String text = "";
        	InputStream stream;
			try {
				stream = getAssets().open("html/histyle08673.css");
				int size = stream.available();
	        	byte[] buffer = new byte[size];
	        	stream.read(buffer);
	        	stream.close();
	        	text = new String(buffer);
			} catch (IOException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
        	*/
        	
        	//Personalizzazione
	        	view.loadUrl("javascript:" +
	        			
		        			"var flow = [];" +
	
	    					//css 8673
		        			"var fun = function() {"+
	    					"	var headDOM = document.getElementsByTagName('head')[0];" +
	    					"	var bodyDOM = document.getElementsByTagName('body')[0];"+
	    					"	var pers = document.createElement('link');"+
	    					"	pers.rel='stylesheet';"+
	    					"	pers.href='/static/common/css/custom/style08673.css?ver=2.22.0.0 FIX2';"+
	    					"	headDOM.appendChild(pers);"+
	    					"	bodyDOM.className='classic '+bodyDOM.className;" +
	    					"	var hipers = document.createElement('style');"+
	    					"	hipers.type='text/css';" +
	    					"	hipers.appendChild(document.createTextNode('" +
	    					"													#navUp, #navDown {	background: rgb(187, 214, 34);	background: -webkit-gradient(linear, left top, left bottom, color-stop(0, rgb(187, 214, 34)), color-stop(1, rgb(177, 204, 4)));	border-bottom: 1px solid rgb(177, 204, 4); border-top: 1px solid rgb(177, 204, 4); }  " +
	    					"													.secur-ui input { background-color: rgb(187, 214, 34); border: 1px solid rgb(177, 204, 4); }" +
	    					"													h2 { color: rgb(177, 204, 4);}" +
	    					"													input { background-color: rgb(187, 214, 34); border: 1px solid rgb(177, 204, 4); }" +
	    					"													.button { background-color: rgb(187, 214, 34); }" +
	    					"													.mobile header[role=banner] h1 { background: #ffffff; top: 0em; margin: auto; height: 0px; }" +
	    					//"												 	#header { background: url(\"/static/common/img/loghi/logo08673.jpg?ver=2.22.0.0 FIX2\") 100% 0% no-repeat; background-size: 180px; } " +
	    					"												'));"+
	    					"	headDOM.appendChild(hipers);"+
		        			"	};" +
	    					"flow.push(fun);" +
		        			
	    					//aggiungi logo chb pagine interne
	    					/*"var fun = function() {"+
							"	var headerDOM = document.getElementById('header');"+
							"	if(headerDOM){ " +
							"		var imgchb = document.createElement('a');"+
							"		imgchb.style.cssText='text-align:center;';"+
							"		imgchb.innerHTML = '" + 
							"								<img alt=\"Logo\" src=\"http://www.chiantibanca.it/img/logo_chb.png\" style=\"margin:auto;\">" +
							"							  ';"+
							"		headerDOM.appendChild(imgchb);" +
							"	}" +
							"	};" +
							"flow.push(fun);" +*/
							
	    					//rimuovi elementi
		        			"var fun = function() {"+
	    					"	var elemDOM = document.getElementById('mm1'); if(elemDOM){ elemDOM.parentNode.removeChild(document.getElementById('mm1')); }"+
		        			"	var elemDOM = document.getElementById('mobileMenu_mm1'); if(elemDOM){ elemDOM.parentNode.removeChild(document.getElementById('mobileMenu_mm1')); }"+
		        			"	var elemDOM = document.getElementById('menu'); if(elemDOM){ elemDOM.parentNode.removeChild(elemDOM); }" +
		        			"	var elemDOM = document.getElementsByClassName('menu-link')[0]; if(elemDOM){ elemDOM.parentNode.removeChild(elemDOM); }" +
	    					"	};" +
	    					"flow.push(fun);" +
	    					
	    					//form login
		        			"var fun = function() {" +
		        			"	var elemDOM = document.getElementById('frmlogin'); " +
		        			"	if(elemDOM){ " +
		        			"		var imgibk = document.createElement('div');"+
		        			"		imgibk.style.cssText='text-align:center;';"+
		        			"		imgibk.innerHTML = '" + 
		        			"							<img alt=\"Logo\" src=\"/mobi/static/common/img/mobile-logo.png\" style=\"margin:auto;width:40%;\">" +
		        			"						  ';"+
		        			"		var headerDOM = document.getElementsByTagName('header')[0];" +
		        			"		var imgchb = document.createElement('div');"+
		        			"		imgchb.style.cssText='text-align:center;';"+
		        			"		imgchb.innerHTML = '" + 
		        			"							<img alt=\"Logo\" src=\"http://www.chiantibanca.it/img/logo_chb.png\" style=\"margin:auto;width:100%;\">" +
		        			"						  ';"+
		        			"		" +
		        			"		elemDOM.parentNode.insertBefore(imgchb,document.getElementById('frmlogin'));" +
		        			"		elemDOM.parentNode.insertBefore(imgibk,document.getElementById('frmlogin'));"+
		        			
		        			"		elemDOM.parentNode.removeChild(document.getElementById('frmlogin').parentNode.firstChild); "+
		        			"		elemDOM.parentNode.removeChild(document.getElementById('frmlogin').parentNode.firstChild); "+
		        			"		elemDOM.removeChild(document.getElementById('frmlogin').lastChild); "+
		        			"		elemDOM.removeChild(document.getElementById('frmlogin').lastChild); " +
		        			"	" +
		        			"		$('#frmlogin').submit(function(){" +
		        			"			Android.ActionLogin($('#id_user').val(),$('#id_password').val());" +
		        			"			return true;" +
		        			"		});" +
		        			"	" +
		        			"	}" +
	    					"	};" +
	    					"flow.push(fun);" +
	    					
		        			//campo utente e token come numero
		        			"var fun = function() {"+
		        			"	var elemDOM = document.getElementById('id_user'); if(elemDOM){ elemDOM.setAttribute('type', 'number'); }" +
		        			"	var elemDOM = document.getElementById('tk_password'); if(elemDOM){ elemDOM.setAttribute('type', 'number'); }"+
	    					"	};" +
	    					"flow.push(fun);" +
	    					
	    					//avverti interfaccia
		        			"var fun = function() {" +
		        			"	var bodyDOM = document.getElementsByTagName('body')[0];" +
		        			"	var successText;" +
		        			"	var successDOM = document.getElementById('success'); if(successDOM){ successText = successDOM.textContent; } else { successText = ''; }"+ //textContent 
	    					"	setTimeout(Android.OkDone(bodyDOM.className,successText),300); " +
	    					"	};" +
	    					"flow.push(fun);" +
	
	    					//esegui codice
	    					"while (flow.length>0) { (flow.shift())(); }; "
	    					
	        	);
	        	
	        	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	        	//Password in chiaro
	        	if(sharedPref.getBoolean("pref_vcredentials", false) == true) {
	            	//Imposta campo password come testo
	            	view.loadUrl("javascript:"+
		            	"	var elemDOM = document.getElementById('id_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('old_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('confirm_new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }"+
		            	"");
	        	}
	        	//Scrivi Utente Password salvati
	             if(sharedPref.getBoolean("pref_rcredentials", false) == true &&
	            	(sharedPref.getString("cred_user", null)!=null)	 ) {
	            	 
	            	//Scrivi Credenziali
	            	view.loadUrl("javascript:"+
		            	"	$('#id_user').val('"+sharedPref.getString("cred_user", null)+"');" +
		            	"	$('#id_password').val('"+sharedPref.getString("cred_pass", null)+"');" +
		            	"");
	            	
	            	//Scrivi Credenziali (Pagine Interne)
		            view.loadUrl("javascript:"+
		            	"	var elemDOM = document.getElementById('old_password'); if(elemDOM){ elemDOM.value = '"+sharedPref.getString("cred_pass", null)+"'; };" +
		            	"");
	            	 
	 	            //Intercetta Cambio password
	 	            view.loadUrl("javascript:"+
		      			"	var submitnewpass = function() {										" +
		      			"		var elemDOMp = document.getElementById('new_password');				" +
		      			"		var elemDOMnewp = document.getElementById('confirm_new_password');	" +
		      			"		if(elemDOM){														" +
		      			"			if(elemDOMnewp){												" +
		      			"				if(elemDOMp.value == elemDOMnewp.value){					" +
		      			"				Android.ActionLogin('refresh',elemDOMnewp.value);			" +
		      			"				}															" +
		      			"			}																" +
		      			"		}																	" +
		 				"	};																		" +
		 				"	var elemDOM = document.getElementById('command');						" +
		 				"	elemDOM.setAttribute('onsubmit', 'submitnewpass();');					"
		 				);
	            	 
	             }
	             
	             view.invalidate();
	        	
        }
        
    }
    


	@Override
	public void onSaveCredentialsDialogPositiveClick(SaveCredentialsDialogFragment dialog) {
         SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
         SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
		 sharedPrefeditor.putBoolean("pref_rcredentials", true);
		 //Message PageData = dialog.getMsg();
		 /*	Toast toast = Toast.makeText(getBaseContext(), "DialogOK "+LastLoginData.getString("arg1")+" "+LastLoginData.getString("arg2"), Toast.LENGTH_SHORT);
		 	toast.show();
		 	String stringa = LastLoginData.getString("arg1");
		 */
		 if(LastLoginData != null){
		   	 sharedPrefeditor.putString("cred_user", LastLoginData.getString("arg1"));
		   	 sharedPrefeditor.putString("cred_pass", LastLoginData.getString("arg2"));
		 }
		 sharedPrefeditor.putBoolean("pref_rcredentials_choice", true);
		 sharedPrefeditor.commit();
		 LastLoginData = null;
	}


	@Override
	public void onSaveCredentialsDialogNegativeClick(SaveCredentialsDialogFragment dialog) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
   	 	sharedPrefeditor.putBoolean("pref_rcredentials", false);
   	 	sharedPrefeditor.putBoolean("pref_rcredentials_choice", true);
   	 	sharedPrefeditor.commit();
   	 	LastLoginData = null;
	}


	@Override
	public void onChkRootDialogPositiveClick(ChkRootDialogFragment dialog) {
		//Nulla
	}


	@Override
	public void onChkRootDialogNegativeClick(ChkRootDialogFragment dialog) {
		finish();
	}
    
}


class SaveCredentialsDialogFragment extends DialogFragment {
	
	/* The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks.
	 * Each method passes the DialogFragment in case the host needs to query it. */
	public interface NoticeDialogListener {
	    public void onSaveCredentialsDialogPositiveClick(SaveCredentialsDialogFragment dialog);
	    public void onSaveCredentialsDialogNegativeClick(SaveCredentialsDialogFragment dialog);
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
 	 	return builder.setMessage(R.string.dialog_rcredentials_message)
     	        .setTitle(R.string.dialog_rcredentials_title)
     	        .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
	                     @Override
						public void onClick(DialogInterface dialog, int id) {
	                         // User clicked OK button
	                         // Send the positive button event back to the host activity
	                         mListener.onSaveCredentialsDialogPositiveClick(SaveCredentialsDialogFragment.this);
	                     }
     	        })
     	        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
	                     @Override
						public void onClick(DialogInterface dialog, int id) {
	                         // User cancelled the dialog
	                         // Send the negative button event back to the host activity
	                         mListener.onSaveCredentialsDialogPositiveClick(SaveCredentialsDialogFragment.this);
	                     }
     	        })
 	 			.create(); 	 
	}
  
}

class ChkRootDialogFragment extends DialogFragment {
	
	/* The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks.
	 * Each method passes the DialogFragment in case the host needs to query it. */
	public interface NoticeDialogListener {
	    public void onChkRootDialogPositiveClick(ChkRootDialogFragment dialog);
	    public void onChkRootDialogNegativeClick(ChkRootDialogFragment dialog);
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
	                         mListener.onChkRootDialogPositiveClick(ChkRootDialogFragment.this);
	                     }
     	        })
     	        .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
	                     @Override
						public void onClick(DialogInterface dialog, int id) {
	                         // User cancelled the dialog
	                         // Send the negative button event back to the host activity
	                         mListener.onChkRootDialogNegativeClick(ChkRootDialogFragment.this);
	                     }
     	        })
 	 			.create(); 	 
	}
  
}
