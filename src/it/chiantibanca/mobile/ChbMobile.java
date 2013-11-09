package it.chiantibanca.mobile;

import java.io.File;
import java.util.Random;
import java.util.UUID;

import android.util.Log;


import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.*;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

 
public class ChbMobile extends FragmentActivity 
					implements 	DialogFragmentSaveCredentials.NoticeDialogListener,
					 			DialogFragmentChkRoot.NoticeDialogListener {
 
	WebView baseWebView;
	MyWebViewClient baseWebViewClient;
	MyWebChromeClient baseWebChromeClient;
	
	WebView myDesktopView;
	WebView myLoadView;
	WebView myCurrentView;
	
    DownloadManager DownloadManager;
    File DownloadDir;
	
	private ProgressBar progress;
	
	static ViewSwitcher viewswitcher;
	static ViewFlipper webswitcher;
	
	public int loaded_page = -1;
	
	static View layout_load;
	static View layout_view;
	static View layout_view_mobile;
	static View layout_view_desktop;
	
    private Bundle LastLoginData;
    private Boolean ToValidateLoginData;
    
    static private String CurrentSessionId;
    
    SharedPreferences DefaultSharedPref;
	
    @SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chb_mobile);
        layout_load=findViewById(R.id.layoutload);
        layout_view=findViewById(R.id.layoutview);
        layout_view_mobile=findViewById(R.id.webmobile);
        
        //Controllo Root
        Root ChkRoot = new Root();
        if (ChkRoot.isDeviceRooted() == true) {
			 //Chiedi Root
       	/* int currentapiVersion = android.os.Build.VERSION.SDK_INT;
       	 if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){*/
       		 // > Honeycomb
			 DialogFragment newFragment = new DialogFragmentChkRoot();
			 FragmentManager fm = getSupportFragmentManager();
			 newFragment.show(fm, "ChkRootDialog");
       	 /*} else{
       		 */
       		 /* TODO
           	 AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
           	 alertDialog.setMessage(getResources().getString(R.string.dialog_rcredentials_message));
     	         alertDialog.setTitle(R.string.dialog_rcredentials_title);
           	 alertDialog.setButton("Accetto", new DialogInterface.OnClickListener() {
           	    public void onClick(DialogInterface dialog, int which) {
           	       // TODO Add your code for the button here.
           	    }
           	 });
           	 alertDialog.setButton("Accetto", new DialogInterface.OnClickListener() {
            	    public void onClick(DialogInterface dialog, int which) {
            	       // TODO Add your code for the button here.
            	    }
            	 });
           	 // Set the Icon for the Dialog
           	 alertDialog.show();*/
       /*	 }*/
			 
        }
        
        //Preferenze Iniziali
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        DefaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 
        
        //Personalizzazione x Chb
        /*
        Boolean pref_rcredentials_choice = DefaultSharedPref.getBoolean("pref_rcredentials_choice", false);
        if (pref_rcredentials_choice == false) {
        	SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
   	 		sharedPrefeditor.putBoolean("pref_rcredentials_choice", true);
   	 		sharedPrefeditor.commit();
        }
        */
        LastLoginData = null;
        ToValidateLoginData = false; 
        
        //Progress bar
        //getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        //final Activity MyActivity = this;
        progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setMax(100);
        
        // WebView
	        //Imposta DownloadManager e DownloadDir
	        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO){
	        	DownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
	        } else {
	        	DownloadManager = null;
	        }
	        DownloadDir = new File (Environment.getExternalStorageDirectory(), getPackageName());
	        //Impostazioni
	        baseWebView = (WebView) findViewById(R.id.webmobile);
	        WebSettings webSettings = baseWebView.getSettings();
	        	webSettings.setJavaScriptEnabled(true);
	        	webSettings.setUserAgentString("ChiantiBanca Mobile App");
	        	webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
	        	webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	        	webSettings.setSupportMultipleWindows(true);
	        	webSettings.setSupportZoom(true);
	        	webSettings.setLoadWithOverviewMode(true);
	        	webSettings.setUseWideViewPort(true);
	        baseWebViewClient = new MyWebViewClient();
	        baseWebChromeClient = new MyWebChromeClient();
	        baseWebView.setWebViewClient(baseWebViewClient);
	        baseWebView.setWebChromeClient(baseWebChromeClient);
	        baseWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
	        baseWebView.setDownloadListener(WebDownloadListener);

	        //Set Current
	        myCurrentView = baseWebView;
	        
	        //Carica pagina
	        caricaPrimaPagina();
        
        // LoadView - Dismissed
	    /*    myLoadView = (WebView) findViewById(R.id.loadview);
	        String url_loading = "file:///android_asset/html/loading.html";
	        myLoadView.loadUrl(url_loading);
	    */
	        
	    // Tema Pelle
        Boolean pref_leathertheme = DefaultSharedPref.getBoolean("pref_useleathertheme", false);
        if(pref_leathertheme){
        	layout_load.setBackgroundResource(R.drawable.leather_layer);
        }
	        
	    // Debug
         final Button button = (Button) findViewById(R.id.buttondbg);
         button.setOnClickListener(new View.OnClickListener() {
             @Override
			public void onClick(View v) {
            	 
            	SwitchViewVisible();
            	
            	/*
	                // Perform action on click
	             	Message msg = new Message();
	                Bundle b = new Bundle();
	                b.putString("action", "js");
	                b.putString("arg1", "simloadok");
	                msg.setData(b);
	                
	                // send message to the handler with the current message handler
	                handler.sendMessage(msg);
                */
            	 
             }
         });
        
         viewswitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
         webswitcher = (ViewFlipper) findViewById(R.id.webview);
         
    }
    
    public void caricaPrimaPagina(){
    	
        Boolean pref_usemobile = DefaultSharedPref.getBoolean("pref_usemobile", false);
        if (pref_usemobile == true) {
        	myCurrentView.loadUrl("https://www.inbank.it/mobi/flow?_flowId=AccessFlow&_flowExecutionKey=e1s1");
	        loaded_page = 0;
        } else {
        	myCurrentView.loadUrl("https://www.inbank.it/function/login/index.jsp?lang=it&abi=08673&css=08673");
        	loaded_page = 10;
        }
    	
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
    public void onBackPressed (){
    	
		 
    	//Se non posso andare indietro ma sono nella visualizzazione Desktop passa a visualizzazione Webview Mobile.
		 if(myCurrentView.canGoBack()) { 
	    		
	        myCurrentView.goBack();  
	        
        } else if (loaded_page == 0 || loaded_page == 10){
        	
    		myCurrentView.stopLoading();
    		myCurrentView.clearView();
    		finish();
    	
        } else if (myCurrentView == myDesktopView && loaded_page > 10) {
        	
        	//Switch View
		    	if(webswitcher.getCurrentView() != layout_view_mobile) {
		    		viewswitcher.showPrevious();   
		    	}
        	
        	//Close session
		    TeminateDesktopSession();
		    	
	    	
        } else if (myCurrentView == baseWebView && loaded_page > 0) {
        	
        	//Close session
        	TerminateMobileSession();
	    		
	    } else {
	    	
    		finish();
        	
        }
    }
    
    private void TerminateMobileSession() {
    	
    	Log.i ("info","Terminating Mobile Session!");
    	//Logout action
    	myCurrentView.loadUrl("https://www.inbank.it/mobi/flow?_flowId=LogoutFlow");
        //Clean Vars
    	loaded_page = 0;
    	
    }
    
    
	private void TeminateDesktopSession() {
		
    	Log.i ("info","Terminating Desktop Session!");
    	//Logout action
		myCurrentView.stopLoading();
    	myCurrentView.loadUrl("javascript:go_exit();");
    	
    	//Destroy
    	DestroyDesktopSession();
    	
    	//Remove View
    	loaded_page = 10;
    	myCurrentView = baseWebView;
    	
    	//Carica Pagina
    	caricaPrimaPagina();
    	
	}
    private void DestroyDesktopSession(){
    	
    	webswitcher.removeView(myDesktopView);
        myDesktopView.clearView();
        myDesktopView.destroy();
        //Clean Vars
        myDesktopView = null;
        layout_view_desktop = null;
        
    }
   
    
    
    @Override
    public void onResume(){
    	DefaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(DefaultSharedPref.getBoolean("pref_vcredentials_needreset", false) == true){
			SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
			sharedPrefeditor.putBoolean("pref_vcredentials_needreset", false);
			sharedPrefeditor.commit();
			
			myCurrentView.reload();
		}
		if(DefaultSharedPref.getBoolean("pref_vcredentials_needlightreset", false) == true){
			
			//Imposta campo password come testo (o vice) alle pagine gia caricate
			
			baseWebViewClient.SwitchVisibleCredentials(DefaultSharedPref.getBoolean("pref_vcredentials", false));
			
			SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
			sharedPrefeditor.putBoolean("pref_vcredentials_needlightreset", false);
			sharedPrefeditor.commit();
		}
		
    	super.onResume();
    }
    
    // Handler Javascript 
    //  Acquisisce messaggi da interfaccia Javascript e gestisce la UI
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
    	
    @SuppressLint("NewApi")
	@Override
     public void handleMessage(Message msg) {

    	if (msg.getData().getString("action") == "js") {
         
    		String bodyId = getBundleString(msg.getData(),"arg2", "");
    		String bodyClass = getBundleString(msg.getData(),"arg3", "");
    		String Url = getBundleString(msg.getData(),"arg4", "");
    		String successText = getBundleString(msg.getData(),"arg5", "");
    		
        	//Indentifica la pagina caricata (per tasto back)
	        	if(bodyClass.contains("clearfix") && Url.contains("/function/login")){
	        		loaded_page = 10; //desk login page
	        	} else if (bodyClass.contains("clearfix") && Url.contains("/mobi")){
	        		loaded_page = 0; //mobile login page
	        	} else if (bodyClass.contains("pass-page")) {
	        		loaded_page = 1;
	        	} else if (bodyClass.contains("bodyMobile")) {
	        		loaded_page = 2;
	        	} else if (bodyId.toString() == "in-bank-net" && bodyClass.toString() == "loading" ) {
	        		loaded_page = 11;
	        	} else if (bodyId.toString() == "in-bank-net" ) {
	        		loaded_page = 12;
	        	}
	        	Log.i ("info","HandleMessagge - js: loaded_page => " + loaded_page );
        	
	        Log.i("cpwd"," Validazione > " + ToValidateLoginData.toString() + " : " + successText + " : " + (LastLoginData != null));
        		
	        //Validazione cambio password
        	if(	ToValidateLoginData == true  
        		&& (successText.contains("successo")  || successText.contains("positivo") )
        		&& LastLoginData != null
        			){
        		
        		Log.i("cpwd","   Valido! > " + LastLoginData.getString("arg1") + ":" + LastLoginData.getString("arg2"));
        		
        		 SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
            	 sharedPrefeditor.putString("cred_pass", LastLoginData.getString("arg2"));
            	 sharedPrefeditor.commit();
        	}
        			
    		//Visualizza Webview
        	 if (msg.getData().getString("arg1") == "loadok" || msg.getData().getString("arg1") == "simloadok") {
        		 SwitchViewVisible();
        	 }
        	 
    	} else if (msg.getData().getString("action") == "say")  {
        	 
    		 Toast toast = Toast.makeText(getBaseContext(), msg.getData().getString("arg1"), Toast.LENGTH_SHORT);
    		 toast.show();
    		
    	} else if (msg.getData().getString("action") == "login")  {
            	 
             Boolean pref_rcredentials_choice = DefaultSharedPref.getBoolean("pref_rcredentials_choice", false);
             Boolean pref_rcredentials = DefaultSharedPref.getBoolean("pref_rcredentials", false);
       	     
             //Chiedi la prima volta
             if (pref_rcredentials_choice == false) {
            	 
            	 LastLoginData = msg.getData();
            	 
            	//Lascia fare tutto al Dialog
            	/* int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            	 if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB){*/
            		 // > Honeycomb
                	 DialogFragment newFragment = new DialogFragmentSaveCredentials();
                	 newFragment.show(getSupportFragmentManager(), "SaveCredentialsDialog");
            	/* } else{*/
            		 
            		 /*
                	 AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext()).create();
                	 alertDialog.setMessage(getResources().getString(R.string.dialog_rcredentials_message));
          	         alertDialog.setTitle(R.string.dialog_rcredentials_title);
                	 alertDialog.setButton("Accetto", new DialogInterface.OnClickListener() {
                	    public void onClick(DialogInterface dialog, int which) {
                	       // TODO Add your code for the button here.
                	    }
                	 });
                	 alertDialog.setButton("Accetto", new DialogInterface.OnClickListener() {
                 	    public void onClick(DialogInterface dialog, int which) {
                 	       // TODO Add your code for the button here.
                 	    }
                 	 });
                	 // Set the Icon for the Dialog
                	 alertDialog.show();
                	 */
            		 
            	/* }*/
            	 
             } else if(pref_rcredentials == true) {
            	 
            	 LastLoginData = msg.getData();
            	 
                 //Salva Utente e password
            	 SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
            	 String newuser = msg.getData().getString("arg1");
            	 if (newuser.equals("refresh")) {
            		 //Non aggiornare niente - Imposta flag da aggiornare password
            		 ToValidateLoginData = true;
            		 Log.i("cpwd"," # Action Login da validare" );
            	 }else{
            		 sharedPrefeditor.putString("cred_user", msg.getData().getString("arg1"));
                	 sharedPrefeditor.putString("cred_pass", msg.getData().getString("arg2"));
                	 sharedPrefeditor.commit();
            	 }
                 
             }
             
    		 
    		 /*Toast toast = Toast.makeText(getBaseContext(), "pref "+pref_rcredentials_choice+" -- "+pref_rcredentials.toString()+"saved"+msg.getData().getString("arg1")+msg.getData().getString("arg2"), Toast.LENGTH_SHORT);
        	 toast.show();*/
        	 
        	 
        }
    	 
 
    	 
        }
    };
    
    private void SwitchViewVisible(){
    	
		 //Progress
    	 ChbMobile.this.progress.setProgress(100);
		 progress.setVisibility(View.GONE);
		 
		 //Switch view
		 if (viewswitcher.getCurrentView() != layout_view)
			 viewswitcher.showNext();
    	
    }
    
    
    // Interfaccia Javascript
    //  Fornisce un metodi per la pagina web e inoltra i relativi messaggi all'Handler
    class JavaScriptInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        JavaScriptInterface(Context c) {
            mContext = c;
        }
        
        public void SetSessionID (String  SESSIONID) {
        	/*
        	Toast toast = Toast.makeText(getBaseContext(), "SessionId:" + SESSIONID, Toast.LENGTH_SHORT);
        	toast.show();
        	*/
        	ChbMobile.CurrentSessionId = SESSIONID;
        }

        public void OkDone(String bodyId, String bodyClass, String Url, String successText) {
        	
        	Log.i ("info","JS Interface - OkDone: " + Url.toString() + " - " + bodyId.toString() + " - " + bodyClass.toString() + " - " + successText.toString());
	       	//Toast toast = Toast.makeText(getBaseContext(), "loaded", Toast.LENGTH_SHORT);
	   		//toast.show();
        	
        	Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("action", "js");
            b.putString("arg1", "loadok");
            b.putString("arg2", bodyId);
            b.putString("arg3", bodyClass);
            b.putString("arg4", Url);
            b.putString("arg5", successText);
            msg.setData(b);
            
            // send message to the handler with the current message handler
            handler.sendMessage(msg);
        	
        }
        
        public void SayThis(String phrase) {
        	
        	Log.i ("info","JS Interface - SayThis: " + phrase);
	       	
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
            
	       	/*
	       	Toast toast = Toast.makeText(getBaseContext(), "actionlogin", Toast.LENGTH_SHORT);
	   		toast.show();
	   		*/
        	
        }
    }
    

    
    
    // Componente Browser
    //  Intercetta eventi Js
    public class MyWebChromeClient extends WebChromeClient {
    	
        @Override
        public void onProgressChanged(WebView view, int newProgress) {         
        	ChbMobile.this.progress.setProgress(newProgress);
            //progress.setProgress(newProgress);  
            super.onProgressChanged(view, newProgress);
        }
    	
	    @Override
	    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
	    	Log.i ("info","Chrome Client - OnCreateWindow:  " + resultMsg.obj.toString() );
	    	
	    	//webswitcher.removeAllViews();
	    	
	    	WebView desktopView = new WebView(view.getContext());
	    	WebSettings webSettings = desktopView.getSettings();
	        webSettings.setJavaScriptEnabled(true);
	        webSettings.setUserAgentString("ChiantiBanca Mobile App");
	        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
	        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	        webSettings.setSupportMultipleWindows(true);
	        webSettings.setBuiltInZoomControls(true);
	        webSettings.setSupportZoom(true);
        	webSettings.setLoadWithOverviewMode(true);
        	webSettings.setUseWideViewPort(true);
	        desktopView.setWebViewClient(new MyWebViewClient());
	        desktopView.setWebChromeClient(new MyWebChromeClient());
	        desktopView.addJavascriptInterface(new JavaScriptInterface(getBaseContext()), "Android");
	        desktopView.setDownloadListener(WebDownloadListener);
	        myDesktopView = desktopView;
	        layout_view_desktop = desktopView;
	    	myCurrentView = myDesktopView;
	        
	        webswitcher.addView(desktopView);
	        
	    	if(webswitcher.getCurrentView() != layout_view_desktop) {
	    		webswitcher.showNext();
	    	}
	    	
	    	loaded_page = 11;
	        
	        WebView.WebViewTransport transport =(WebView.WebViewTransport)resultMsg.obj;
	        transport.setWebView(myDesktopView);
	        resultMsg.sendToTarget();
	        
	        return false;
	    }
	    
	    @Override
	    public void onCloseWindow (WebView window){
        	Log.i ("info","Chrome Client - onCloseWindow: Terminating Desktop Session!");
        	
        	//Terminate session
        	TeminateDesktopSession();
	        caricaPrimaPagina();
	        	
        	//Switch View
		    	if(webswitcher.getCurrentView() != layout_view_mobile) {
		    		webswitcher.showPrevious();   
		    	}
        	

	    }
	    
	    @Override
	    public boolean onConsoleMessage (ConsoleMessage consoleMessage){
	    	Log.i ("info","Chrome Client - OnConsoleMessage:  " + consoleMessage.message().toString() + " ["+ consoleMessage.sourceId()+" at line: " +consoleMessage.lineNumber() +"]" );
	    	return true;
	    }
	    
	    @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    Log.i ("info","Chrome Client - OnJsMessage:  " + message.toString() + " " );
                    return super.onJsAlert(view, url, message, result);
	    }
	    
	    
    }
    
   
    // Componente Browser
    //  Visualizza le pagine web
    public class MyWebViewClient extends WebViewClient {
    	
    	/*Default DOMId*/
        String field_user;
        String field_pass;
        String field_otppass;
        String field_oldpass;
        String field_newpass;
        String field_confirmpass;
        String field_tel;
        String form_login;
        String form_newpass;
        String iframe_center;
        String form_newpass_success;
        
        
		@Override
        public void onLoadResource(WebView  view, String  url){
        	Log.i ("info","WebView Client - LoadResource: " + url.toString());
        	String test = " "+url.toLowerCase()+" ";
        	if (test.contains("singledownload") || test.contains("creapdf")) {
        		downloadPdf(url);
        	}
        }
    	
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	
        	Log.i ("info","WebView Client - ShouldOverride: " + url.toString());
        	Uri source = Uri.parse(url);
        	
            // InBank & ChiantiBanca
            if (source.getHost().equals("www.inbank.it") | source.getHost().equals("www.chiantibanca.it") ) {
                return false;
            }
            
            // Altro
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
        
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
        	
        	//Progress
        	ChbMobile.this.progress.setVisibility(View.VISIBLE);
        	ChbMobile.this.progress.setProgress(0);
        	
        	//Switch View
        	Log.i ("info","WebView Client - PageStarted: " + url.toString());
        	if (viewswitcher.getCurrentView() != layout_load)
        		viewswitcher.showPrevious();   

        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
        	//Toast.makeText(getBaseContext(),url.toString() , Toast.LENGTH_SHORT).show();
	    	Log.i ("info","WebView Client - Finished: " + url.toString());
	    	/*if (loaded_page == 9) {	return;	}*/ 
	    	
	    	//Progress 90
	    	ChbMobile.this.progress.setProgress(95);
	    	
	    	//Js Work
	    	try {
	    		InjectJs(view);
	    		ExecJs(view);
	    	} finally {
	    		
	    		//Visualizza view
	    		SwitchViewVisible();
	    	
	    	}
	    	
        }
        
        private void InjectJs(WebView view){
        	

	        //Get Preferences
	        boolean pref_usemobile = DefaultSharedPref.getBoolean("pref_usemobile", false);
	        boolean pref_dohistyles = DefaultSharedPref.getBoolean("pref_dohistyles", false);
	        
	        //Set Default DOM id
	        RefreshDOMId();
	        
	        //Toast toast = Toast.makeText(getBaseContext(), "usr: "+DefaultSharedPref.getString("cred_user", "").toString()+" pwd:"+DefaultSharedPref.getString("cred_pass", "").toString(), Toast.LENGTH_SHORT);
	        //toast.show();
	        
	        //TODO 
	        //Injecting Js - Global Functions
	        	view.loadUrl("javascript:" +
	        				"function readCookie(name) {var nameEQ = name + '=';var ca = document.cookie.split(';');for(var i=0;i < ca.length;i++) {var c = ca[i];while (c.charAt(0)==' ') c = c.substring(1,c.length);if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);}return null;}" +
	        				"function loadScripto() { return true; } " +
	        				"function loadScript(sScript64, oCallback) {  var sScript = window.atob(sScript64);  var oHead = document.getElementsByTagName('head')[0]; var oScript = document.createElement('script'); oScript.type = 'text/javascript'; oScript.onload = oCallback; oScript.onreadystatechange = function() { if (this.readyState == 'complete') { oCallback(); } }; try { oScript.appendChild(document.createTextNode(sScript)); } catch (e) { oScript.text = code; }; oHead.appendChild(oScript); }" +
	        				"function makeid(){    var text = '';    var possible = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';    for( var i=0; i < 5; i++ )        text += possible.charAt(Math.floor(Math.random() * possible.length));    return text;}" +
	        				"function hasClass(el, class_to_match) { var c; if (el && el.className && typeof class_to_match === 'string') { c = el.getAttribute('class'); c = ' '+ c + ' '; return c.indexOf(class_to_match) > -1; } else { return false; } }"+
	        				"var cloneOf = (function() {  function F(){}  return function(o) {    F.prototype = o;    return new F();  }}());" +
	        				"function getAllElementsWithAttribute(base,attribute,value){var matchingElements=[];var allElements=base.getElementsByTagName('*');for(var i=0;i<allElements.length;i++){if(allElements[i].getAttribute(attribute)==value){matchingElements.push(allElements[i])}}return matchingElements}"
	        	);
	        	
        	//Injecting Js - Init
	        	view.loadUrl("javascript:" +
		        			"var flow = [];"
	        	);
	        	
	        //Injecting Js - Say Hello (dubug purpose..)
	        	view.loadUrl("javascript:" +	
	        				
		        			//User & OTP pass as Number
		        			"var fun = function(base) {" +
		        			"	base = base || document;" +
		        			"" +
		        			"	$('#nav').find('> ul > li > a').each(function() {	" +
		        			"	$(this).off('click');	" +
		        			"	});	" +
		        			"	" +
		        			"	$('#nav').find('> ul > li > ul > li > a').each(function() {	" +
		        			"	$(this).off('click').off('mouseenter');	" +
		        			"	});	" +
		        			"	" +
		        			"	$('#nav').find('> ul > li > ul > li > ul > li > a').each(function() {	" +
		        			"	$(this).off('click');	" +
		        			"	});	" +
		        			"	" +
		        			"	$('#nav').find('> ul > li > ul').each(function() {	" +
		        			"	$(this).off('mouseleave').off('mouseenter');	" +
		        			"	});	" +
		        			"		" +
		        			"	var mainFrameId = 'centerFrame';	" +
		        			"		$('#nav').navmain({	" +
		        			"		iframeId: mainFrameId,	" +
		        			"		iAutoCloseTime: 50000	" +
		        			"	});	" +
		        			" } " +
		        			/*"	console.log('---> new flow execution!');"+
		        			"	base.body.style.backgroundColor = '#AA0000';"+*/
		        			/*"	console.log('---> debug: '+base.innerHTML);"+*/
	    					"	};" +
	    					"flow.push(fun);"
	        	);
	        	
	        //Injecting Js - Mobile Personalizations
		        if (loaded_page < 10) {	
		        		 //Hi-Styles
			             if (pref_dohistyles == true || loaded_page == 0) {
			 	        	view.loadUrl("javascript:" +
			 	    					//css 8673
			 		        			"var fun = function(base) {" +
			 		        			"	base = base || document;"+
			 	    					"	var headDOM = base.getElementsByTagName('head')[0];" +
			 	    					"	var bodyDOM = base.getElementsByTagName('body')[0];"+
			 	    					"	bodyDOM.className='classic '+bodyDOM.className;" +
			 	    					"	var hipers = base.createElement('style');"+
			 	    					"	hipers.type='text/css';" +
			 	    					"	hipers.appendChild(base.createTextNode('" +
			 	    					"													#navUp, #navDown {	background: rgb(187, 214, 34);	background: -webkit-gradient(linear, left top, left bottom, color-stop(0, rgb(187, 214, 34)), color-stop(1, rgb(177, 204, 4)));	border-bottom: 1px solid rgb(177, 204, 4); border-top: 1px solid rgb(177, 204, 4); }  " +
			 	    					"													.secur-ui input { background-color: rgb(187, 214, 34); border: 1px solid rgb(177, 204, 4); }" +
			 	    					"													h2 { color: rgb(177, 204, 4);}" +
			 	    					"													input { background-color: rgb(187, 214, 34); border: 1px solid rgb(177, 204, 4); }" +
			 	    					"													.button { background-color: rgb(187, 214, 34); }" +
			 	    					"													.mobile header[role=banner] h1 { background: #ffffff; top: 0em; margin: auto; height: 0px; }" +
			 				    		"													" +
			 				    		"													.classic{border-color:#bbd622}"+
			 				        	"													.classic .lang,.classic .credits,.classic .login button,.classic .login input{background-color:#bbd622}"+
			 				        	"													.classic .login h1{text-align:center}"+
			 				        	"													.classic .login input[type=text],.classic .login input[type=password]{color:#343434}"+
			 				        	"													.classic nav[role=navigation] .active a,.classic .info p,.classic .numero-verde p b{color:#bbd622}"+
			 				        	"													.classic .flex-direction-nav li a{background-image:url(../../img/green/control-direction.png)}"+
			 				        	"													.classic .flex-control-nav li a{background-image:url(../../img/green/control-dots.png)}"+
			 				        	"													.ie7 div[role=main]{background:none;padding-right:0;border:0}"+
			 				        	"													::-webkit-selection,::-moz-selection,::selection{background:#bbd622}"+
			 				        	"													@media only screen and min-width 992px {"+
			 				        	"													 div[role=main]{background:none;padding-right:0;border:0}"+
			 				        	"													 .news,.login{background:#fff;border:1px solid #dfdfdf;-webkit-border-radius:8px;-moz-border-radius:8px;border-radius:8px}"+
			 				        	"													 .login{width:25.7%;min-height:333px;margin-top:0;margin-left:0;margin-right:0;padding:1em 2%}"+
			 				        	"													 .ie7 .login{height:330px}"+
			 				        	"													 .news{position:relative;width:66.666%}"+
			 				        	"													}"+
			 	    					"													" +												
			 	    					"												'));"+
			 	    					"	headDOM.appendChild(hipers);"+
			 		        			"	};" +
			 	    					"flow.push(fun);"
			 	        	);	
			             }
			        	//Login Form
			        	view.loadUrl("javascript:" +	
									
			    					//rimuovi elementi
				        			"var fun = function(base) {" +
				        			"	base = base || document;"+
			    					"	var elemDOM = base.getElementById('mm1'); if(elemDOM){ elemDOM.parentNode.removeChild(base.getElementById('mm1')); }"+
				        			"	var elemDOM = base.getElementById('mobileMenu_mm1'); if(elemDOM){ elemDOM.parentNode.removeChild(base.getElementById('mobileMenu_mm1')); }"+
				        			"	var elemDOM = base.getElementById('menu'); if(elemDOM){ elemDOM.parentNode.removeChild(elemDOM); }" +
				        			"	var elemDOM = base.getElementsByClassName('menu-link')[0]; if(elemDOM){ elemDOM.parentNode.removeChild(elemDOM); }" +
			    					"	};" +
			    					"flow.push(fun);" +
			    					
			    					//form login
				        			"var fun = function(base) {" +
				        			"	base = base || document;" +
				        			"	var elemDOM = base.getElementById('frmlogin'); " +
				        			"	if(elemDOM){ " +
				        			"		var imgibk = base.createElement('div');"+
				        			"		imgibk.style.cssText='text-align:center;';"+
				        			"		imgibk.innerHTML = '" + 
				        			"							<img alt=\"Logo\" src=\"/mobi/static/common/img/mobile-logo.png\" style=\"margin:auto;width:40%;\">" +
				        			"						  ';"+
				        			"		var headerDOM = base.getElementsByTagName('header')[0];" +
				        			"		var imgchb = base.createElement('div');"+
				        			"		imgchb.style.cssText='text-align:center;';"+
				        			"		imgchb.innerHTML = '" + 
				        			"							<img alt=\"Logo\" src=\"http://www.chiantibanca.it/img/logo_chb.png\" style=\"margin:auto;width:100%;\">" +
				        			"						  ';"+
				        			"		" +
				        			"		elemDOM.parentNode.insertBefore(imgchb,base.getElementById('frmlogin'));" +
				        			"		elemDOM.parentNode.insertBefore(imgibk,base.getElementById('frmlogin'));"+
				        			
				        			"		elemDOM.parentNode.removeChild(base.getElementById('frmlogin').parentNode.firstChild); "+
				        			"		elemDOM.parentNode.removeChild(base.getElementById('frmlogin').parentNode.firstChild); "+
				        			"		elemDOM.removeChild(base.getElementById('frmlogin').lastChild); "+
				        			"		elemDOM.removeChild(base.getElementById('frmlogin').lastChild); " +
				        			"	" +
				        			"	}" +
			    					"	};" +
			    					"flow.push(fun);"
			        	);
		        }
	        
	        //Injecting Js - UI Adjustment
		        view.loadUrl("javascript:" +	
	        				
		        			//User & OTP pass as Number
		        			"var fun = function(base) {" +
		        			"	base = base || document;"+
		        			"	"+GetDOM(field_user)+" if(elemDOM){ elemDOM.setAttribute('type', 'number'); }" +
		        			/*"	"+GetDOM(field_user, null)+" if(elemDOM){ elemDOM.setAttribute('type', 'number'); }" +*/
		        			// OTP numero e autocplete off
		        			"	"+GetDOM(field_otppass)+" if(elemDOM){ elemDOM.setAttribute('type', 'number');  }" +
		        			"	"+GetDOM(field_otppass)+" if(elemDOM){ elemDOM.setAttribute('autocomplete', 'off');  }" +
		        			"	"+GetDOM(field_tel)+" if(elemDOM){ elemDOM.setAttribute('type', 'number');  }" +
	    					"	};" +
	    					"flow.push(fun);"
	        	);
	        
	        	
	        //Injecting Js - ActionLogin Save Credentials
		        if( DefaultSharedPref.getBoolean("pref_rcredentials", false) == true || DefaultSharedPref.getBoolean("pref_rcredentials_choice", false) ) {
		        	view.loadUrl("javascript:" +
		        			"var fun = function(base) {" +
		        			"		base = base || document;"+
							"		var elemDOM = base.getElementById('frmlogin');"+
							"		if(elemDOM){" +
							"			var formsub = function() {" +
							"				"+GetDOM(field_user, "field_user", "")+"" +
							"				"+GetDOM(field_pass, "field_pass", "")+""+
							"				var fldusr;" +
							"				var fldpwd;" +
							"				if(field_user){ fldusr = field_user.value; } else { fldusr = ''; }" +
							"				if(field_pass){ fldpwd = field_pass.value; } else { fldpwd = ''; }"+
							"				Android.ActionLogin(fldusr, fldpwd);" +
							"				return true;" +
							"			};" +
							"			elemDOM.addEventListener('submit', formsub, false);" +
							"		}"+
	    					"	};" +
	    					"flow.push(fun);"
		        	);
		        }
	        
	        	
	        //Injecting Js - Pref: Visible Credentials \ Visible Credentials 
	        	if(DefaultSharedPref.getBoolean("pref_vcredentials", false) == true) {
	            	//Imposta campo password come testo
	            	view.loadUrl("javascript:"+
	            			"var fun = function(base) {" +
	            			"	base = base || document;"+
	            			"	"+GetDOM(field_pass)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_pass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_oldpass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_newpass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_confirmpass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	    					"	};" +
	    					"flow.push(fun);"
	    			);
	        	}
	        
	        
        	
	        //Injecting Js - Pref: Saved Credentials - Password Changes
	            if((DefaultSharedPref.getBoolean("pref_rcredentials", false) == true)  && (DefaultSharedPref.getString("cred_user", null)!=null) ) {
		            	//Write Saved Credentials
		            	view.loadUrl("javascript: " +
		            		"var fun = function(base) {" +
		            		"		base = base || document;"+
			            	"		"+GetDOM(field_user)+" if(elemDOM){ elemDOM.value = '"+DefaultSharedPref.getString("cred_user", "")+"'; }" +
			            	"		"+GetDOM(field_pass)+" if(elemDOM){ elemDOM.value = '"+DefaultSharedPref.getString("cred_pass", "")+"'; }" +
	    					"	};" +
	    					"flow.push(fun);"
			            	);
		            	//Only Internal pages - Password Changes
		            	if (loaded_page != 10 && loaded_page != 0) {	
			 	            view.loadUrl("javascript:" +
				            	"var fun = function(base) {" +
				            	"	base = base || document;"+
			 	            		//Write Saved Credentials (Internal pages)
			 	            	"		"+GetDOM(field_oldpass, null, iframe_center)+" if(elemDOM){ elemDOM.value = '"+DefaultSharedPref.getString("cred_pass", "")+"'; }" +
			 	            		//Hijack password change
			 	            	"		"+GetDOM(form_newpass, null, iframe_center)+"								" +
				 				"		if(elemDOM){ 																" +
				      			"			var submitnewpass = function() {										" +
				      			"				"+GetDOM(form_newpass, null, iframe_center)+"						" +
				      			"				"+GetDOM(field_newpass,"elemDOMp", iframe_center)+"					" +
				      			"				"+GetDOM(field_confirmpass,"elemDOMnewp", iframe_center)+"			" +
				      			"				if(elemDOM){														" +
				      			"					if(elemDOMp){													" +
				      			"						if(elemDOMnewp){											" +
				      			"							if(elemDOMp.value == elemDOMnewp.value){				" +
				      			"							Android.ActionLogin('refresh',elemDOMnewp.value);		" +
				      			"							}														" +
				      			"						}															" +
				      			"					}																" +
				      			"				}																	" +
				 				"			};																		" +
				 				"			elemDOM.addEventListener('submit', submitnewpass, false); 				" +
				 				"		}																			" +
		    					"	};" +
		    					"flow.push(fun);"
					            );
		            	}   
		            	
		   //Injecting Js - OK Done
 	            view.loadUrl("javascript:" +
	            	"var fun = function(base) {" +
	            	"	base = base || document;"+
					"		var bodyId;" +
					"		var bodyClassName;" +
					"		var bodyDOM = base.getElementsByTagName('body')[0];" +
					"		var successText;" +
					"		"+GetDOM(form_newpass_success,"successDOM", iframe_center)+" " +
					"		if ( bodyDOM ) {" +
					"			if ( bodyDOM.hasOwnProperty('id') ) { bodyId = bodyDOM.id; } else { bodyId = '-empty-'; }" +
					"			if ( bodyDOM.hasOwnProperty('class') ) { bodyClassName = bodyDOM.className; } else { bodyClassName = '-empty-'; }" +
					"		} else { bodyId = '-Body-notfound-'; bodyClassName = '-Body-notfound-'; }" +
					"		if ( successDOM ) { successText = successDOM.textContent; } else { successText = ''; }"+ //textContent 
					"		setTimeout(Android.OkDone(bodyId,bodyClassName,document.URL,successText),300); " +
					"	};" +
					"flow.push(fun);"
					);
		            	
	             }
        	
        }
        
        private void ExecJs(WebView view){
        	
				view.loadUrl("javascript:" +
							"	var Ctrller = new function () {" +
							"			this.objs = [];" +
							"			this.scan_i = -1;" +
										//func Check
							"			this.time = [];" +
							"			this.time_loc = [];" +
										//func Queue
							"			this.timed_queue;" +
										//Settings
							"			this.check_maxrun = 5;" +
							"			this.queue_maxrun = 10;" + 

							"			this.scan = function scan(base,basecontent){" +
							"					this.scan_i = this.scan_i+1;" +
												//Prepara
							"					console.log(':Scan '+this.scan_i);" +
							"					var baseId = null;" +
							/*"					if(hasClass(basecontent.body,'load')){isLoading = true;}" +*/
													//Se base stringa prendi base con Id Dom
							"						if(typeof(base)==typeof('stringa')){" +
							"							baseId = base;" +
							"							base = this.getmarkeddom(baseId,true);" +
							"							if (base == null){ return false; }" +
							"							basecontent = base.contentDocument ? base.contentDocument : (base.contentWindow.document || base.document);" +
							"						}" +
												//Accoda
							"					baseId = this.add(base);" +
												//Scansiona interni
							"					ifrms = basecontent.getElementsByTagName('iframe');" +
							"					for(var num=0; num<ifrms.length; num++){" +
							"						ifrm = ifrms[num];" +
							"						console.log('   Ifr -'+num+' '+ifrm.id);" +
							"						ifrm_doc = ifrm.contentDocument ? ifrm.contentDocument : (ifrm.contentWindow.document || ifrm.document);" +
													//Scansiona sempre Iframe
							"						var ifrmId = this.scan(ifrm, ifrm_doc);" +
													//Aggiungi Check (solo se iframe id tra quelli da controllare ? NO uso di isLoading!)
							"						if(ifrm.id=='centerFrame'){" +
							"							console.log('   Ifr -'+num+' '+ifrmId+ ' Add Check...');" +
							"							var time_i = this.time.length + 1 -1;" +
							"							this.time_loc[time_i] = String(ifrm_doc.location);" + 
							"							var self = this;" +
							/*"							console.log('Adding Check... '+ifrmId+' '+ifrmLoc+' '+ifrmTimei+' = '+typeof(ifrmId)+' '+typeof(ifrmLoc)+' '+typeof(ifrmTimei));" +*/
							"							this.time.push( setInterval( function(){ self.check(ifrmId,time_i) } , 500 ) );" +
							/*"							console.log('Doc delayed:'+ifrm_doc.location+' '+ifrm_doc.body.innerHTML);" +*/
							"						}" +
							"					};" +
							"					return baseId;" +
							"				};\n" +
							"			this.check = function check(baseId,index){" +
							"					try{" +
							"						console.log('>Check '+index+' '+baseId);" +
							"						var doc = null;" +
							"						doc = this.getmarkeddom(baseId,false);" +
							"						if (doc != null){" +
							//"							console.log('Doc: '+doc.location+' '+doc.body.innerHTML);" +
							"							if(doc.location != this.time_loc[index]){" +
							"								console.log('   - New Loc! '+location+' -> '+doc.location);" +
															//Lancia nuova Scansione
							"								this.scan(baseId,null);" +
															//Salva nuova location
							"								this.time_loc[index] = doc.location;" +
															// Stoppa e cancella timeout a this.time[index]
							/*"								clearInterval(this.time[index]);" +
							"								this.time[index]=null;" +*/
							"							}" +
							"						} else {" +
							"							console.log('   -cancelled!');" +
							"							clearInterval(this.time[index]);" +
							"							this.time[index]=null;" +
							"						}" +
							"					} catch ( e ) {" +
							"						console.log('Check Error> '+e.message);" +
							"						clearInterval(this.time[index]);" +
							"						this.time[index]=null;" +
							"					}" +
							"				};\n" +
							"			this.add = function add(ifr){" +
							"					console.log(':Add ');" +
							"					var tmpId = this.markdom(ifr);" +
							"					this.objs.push([tmpId]);" +
							"					return tmpId;" +
							"				};\n" +
							"			this.markdom = function markdom(ifr){" +
							"				var tmpId = '';" +
							"				if (ifr === window){" +
							"					tmpId = '';" +
							"				} else {" +
							"					tmpId = ifr.getAttribute('tmpId');" +
							"					if (tmpId == null) {" +
							"						tmpId = makeid(); " +
							"						ifr.setAttribute('tmpId', tmpId);" +
							"					}" +
							"				}" +
							/*"				console.log(':markdom '+tmpId);" +*/
							"				return tmpId;" +
							"			};\n" + 
										// Analizza Iframe raccolti
							"			this.queue = function queue(){" +
							"					console.log('>Queue '); " +
												//Run
							"					var c = 0;" +
							"					while (this.objs.length>0) {" +
							"						c = c+1;" +
							"						console.log(':Queue '+c+'  l:'+this.time.length);" +
							"						var obj = this.objs.shift();" +
							"						var tmpId = obj[0];" +
							"						" + 
							"						this.run(tmpId);" +
							"					}" +
							"					" +
												//Check for active timeouts
							"					if(this.time.length > 0){" +
							"						if (this.timed_queue == null) { var self = this; this.timed_queue = setInterval( function(){ self.queue(); } , 600 ); }" +
							"					} else {" +
							"						clearInterval(this.timed_queue);" +
							"						this.timed_queue = null;" +
							"					}" +
							"				};\n" +
										// Esegui su Iframe
							"			this.run = function run(tmpId){" +
							"						console.log(':Run '+tmpId+ ' ');" +
							"						if(0==flow.length){ console.log('no flow!'); }" +
							"						var doc = null;" +
							"						doc = this.getmarkeddom(tmpId,false);" +
							"						if (doc != null){" +
							"							var tmpFlw = flow.slice();" +
							"							while (tmpFlw.length>0) { (tmpFlw.shift())(doc); };" +
							"						} else {" +
							"							console.log('no doc!');" +
							"						}" +
							"				};\n" +
							"			this.getmarkeddom = function getmarkeddom(baseId, returnBase){" +
													//Abbiamo marcato Window, ma qui per default ritorniamo document.
													//				  iframe								contentdocument
													//				  base									basecontent
							"						if(!returnBase) { returnBase = false; }" +
							"						if (baseId != ''){" +
							"							var docs = getAllElementsWithAttribute(document, 'tmpId', baseId);" +
							"							if(docs.length > 0){" +
							/*"								console.log(':found! '+baseId+ '   ('+returnBase+')');" +*/
							"								if (returnBase == true) {" +
							"									destObj = docs[0];" +
							"								} else {" +
							"									var doc = docs[0].contentDocument ? docs[0].contentDocument : (docs[0].contentWindow.document || docs[0].document);" +
							"									destObj = doc;" +
							"								}" +
							"							} else {" +
							/*"								console.log(':notfound '+baseId+ ' ');" +*/
							"								destObj = null;" +
							"							}" +
							"						} else {" +
							"							destObj = document;" +
							"						}" +
							"						return destObj;" +
							"			};\n" +
							"		};" +
							"		");
				
				view.loadUrl("javascript:" +
							"console.log('ChbMobile> Starting routine... ');" +
							"var JsOkDoneContainer = function() {"+
							"	try {" +
							"		var i=0;" +
							"			if (document.readyState === 'complete') {" +
							"				console.log('DOM is READY!');" +
							"				Ctrller.scan( window, document);" +
							"			} else {" +
							"				console.log('DOM is not yet ready.');" +
							"				document.addEventListener( 'Load', Ctrller.scan(window, document) , false);" +
							"			}\n" +
							"		Ctrller.queue();" +
							"	} catch ( e ) {" +
							"		console.log('error> '+e.message);" +
							"	} finally {" +
								//Avverti interfaccia
							"		var JsOkDone = function() {" +
							"				var bodyId;" +
							"				var bodyClassName;" +
							"				var bodyDOM = document.getElementsByTagName('body')[0];" +
							"				var successText;" +
							"				"+GetDOM(form_newpass_success,"successDOM", iframe_center)+" " +
							"				if ( bodyDOM ) {" +
							"					if ( bodyDOM.hasOwnProperty('id') ) { bodyId = bodyDOM.id; } else { bodyId = '-empty-'; }" +
							"					if ( bodyDOM.hasOwnProperty('class') ) { bodyClassName = bodyDOM.className; } else { bodyClassName = '-empty-'; }" +
							"				} else { bodyId = '-Body-notfound-'; bodyClassName = '-Body-notfound-'; }" +
							"				if ( successDOM ) { successText = successDOM.textContent; } else { successText = ''; }"+ //textContent 
							"				setTimeout(Android.OkDone(bodyId,bodyClassName,document.URL,successText),300); " +
							"			" +
							"		};" +
							"		JsOkDone();"+
							"	}"+
							"};" +
							/*
							 * Execute Now
							 */
							"JsOkDoneContainer();" +
							"" +
							"Android.SetSessionID(readCookie('JSESSIONID'));"
				);
        	
        }
        
        //Funzioni di Supporto
        private String GetDOM(String field, String param, String iframe){
        	String ret = "";
        	if (param == null) { param = "elemDOM"; }
        	if (loaded_page < 10) {	
        		//Mobile
        		ret = " var "+param+" = base.getElementById('"+field+"'); ";
        	}else{
        		//Desktop
        		//if (iframe.equals("")){
        			if (field.startsWith(":")) {
        				ret = ret + "	var "+param+" = getAllElementsWithAttribute(base, 'name','"+field.substring(1)+"')[0]; ";
        			} else {
        				ret = " var "+param+" = base.getElementById('"+field+"'); ";
        			}
        		/*}else{
        			ret = 	"var if_"+param+" = base.getElementById('"+iframe+"');" +
        					"if(if_"+param+"){ " +
        					"	var if_"+param+"_doc = if_"+param+".contentDocument || if_"+param+".contentWindow.document;";
        			//Field ByName (:) or ById (normal)
        			if (field.startsWith(":")) {
        				ret = ret + "	var "+param+" = getAllElementsWithAttribute(if_"+param+"_doc, 'name','"+field.substring(1)+"')[0]; ";
        			} else {
        				ret = ret + "	var "+param+" = if_"+param+"_doc.getElementById('"+field+"'); ";
        			}
        			ret = ret +"	} else { var "+param+" = null; }";
        			//ret = " var "+param+" = $('"+iframe+"').contents().find('"+field+"').get(0);";
        		}*/
        	}
        	return ret;
        }
        private String GetDOM(String field, String param){
        	return GetDOM(field, param, "");
        }
        private String GetDOM(String field){
        	return GetDOM(field, "elemDOM", "");
        }
        
        private void RefreshDOMId(){
	        if (loaded_page >= 10) {
	        	//Desktop Version
	        	//	DOM are selected with Pure Js
	        	//	used Id
	        	field_user = "access-code";
	        	field_pass = "password";
	        	iframe_center = "centerFrame";
	        	field_otppass = ":pass"; //input[name=pass]
	        	field_oldpass = ":oldpassword"; //input[name=oldpassword]
	        	field_newpass = ":password";	//input[name=password]
	        	field_confirmpass = ":pwdconfirm"; //input[name=pwdconfirm]
	        	field_tel = "ute_numeroTelefonico_fld";
	        	form_login = "frmlogin";
	        	form_newpass = ":logform";
	        	form_newpass_success = "successTableId";
	        } else {
	        	//Mobile Version
	        	//	DOM are selected with Pure Js
	        	//	used Id
	        	field_user = "id_user";
	        	field_pass = "id_password";
	        	iframe_center = "";
	        	field_otppass = "tk_password"; 
	        	field_oldpass = "old_password";								
	        	field_newpass = "new_password";
	        	field_confirmpass = "new_password";
	        	field_tel = "ute_numeroTelefonico_fld";
	        	form_login = "frmlogin";
	        	form_newpass = "command";
	        	form_newpass_success = "success";
	        }
        }
        
        public void SwitchVisibleCredentials(Boolean ActualPref){
        	
        	RefreshDOMId();
        	if(ActualPref == true){
				myCurrentView.loadUrl("javascript:"+
	        			"	base = document;"+
	        			"	"+GetDOM(field_pass)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	        			"	"+GetDOM(field_pass )+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	        			"	"+GetDOM(field_oldpass)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	        			"	"+GetDOM(field_newpass)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	        			"	"+GetDOM(field_confirmpass)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
						""
						);
			}else{
				myCurrentView.loadUrl("javascript:"+
	        			"	base = document;"+
	        			"	"+GetDOM(field_pass)+" if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
	        			"	"+GetDOM(field_pass)+" if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
	        			"	"+GetDOM(field_oldpass)+" if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
	        			"	"+GetDOM(field_newpass)+" if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
	        			"	"+GetDOM(field_confirmpass)+" if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
						""
						);
			}
        	
        }
        
        
    }
    


	@Override
	public void onSaveCredentialsDialogPositiveClick(DialogFragmentSaveCredentials dialog) {
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
	public void onSaveCredentialsDialogNegativeClick(DialogFragmentSaveCredentials dialog) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
   	 	sharedPrefeditor.putBoolean("pref_rcredentials", false);
   	 	sharedPrefeditor.putBoolean("pref_rcredentials_choice", true);
   	 	sharedPrefeditor.commit();
   	 	LastLoginData = null;
	}


	@Override
	public void onChkRootDialogPositiveClick(DialogFragmentChkRoot dialog) {
		//Nulla
	}


	@Override
	public void onChkRootDialogNegativeClick(DialogFragmentChkRoot dialog) {
		finish();
	}
	


	
	
	public String getBundleString(Bundle b, String key, String def)
	{
	    String value = b.getString(key);
	    if (value == null)
	        value = def;
	    return value;
	}
	
	
	
    
    DownloadListener WebDownloadListener = new DownloadListener() { 
		public void onDownloadStart(String url, String userAgent,
                String contentDisposition, String mimetype,
                long contentLength) {
        	
        	Log.i ("info","WebDownload Listener - onDownloadStart: " + url.toString());
        	downloadPdf(url);

        }
    };
    
    @SuppressLint("NewApi")
    public void downloadPdf(String url){
    	Uri source = Uri.parse(url);
    	String docId = source.getQueryParameter("docId");
    	if (docId == null){
    		docId = RandomString();
    	}
    	String filename = docId + ".pdf";
    	Log.i ("info","Download: " + source.toString() + " to " + UUID.randomUUID().toString()+".pdf   "+source.getLastPathSegment()+" Session:"+ChbMobile.CurrentSessionId);
    	
    	// FOR FROYO
    	if (DownloadManager == null){
    		Toast toast = Toast.makeText(getBaseContext(), "Download non ancora disponibile per le versioni di android precedenti alla 2.3 !", Toast.LENGTH_SHORT);
   		 	toast.show();
   		 	/*
    		Intent i = new Intent(Intent.ACTION_VIEW);
    		i.setData(Uri.parse(url));
    		startActivity(i);
    		*/
    		return;
    	}
    	
    	// >GINGERBREAD
    	if (!DownloadDir.exists()) { DownloadDir.mkdir(); }
    	DownloadManager.Request request = new DownloadManager.Request(source);
    	request.addRequestHeader("Cookie", "JSESSIONID=" + ChbMobile.CurrentSessionId);
    	File destinationFile = new File (DownloadDir, filename);
    	request.setDestinationUri(Uri.fromFile(destinationFile));
    	DownloadManager.enqueue(request);
    }
    
    private String RandomString(){
    	char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    	StringBuilder sb = new StringBuilder();
    	Random random = new Random();
    	for (int i = 0; i < 15; i++) {
    	    char c = chars[random.nextInt(chars.length)];
    	    sb.append(c);
    	}
    	String output = sb.toString();
    	return output;
    }
    
}
