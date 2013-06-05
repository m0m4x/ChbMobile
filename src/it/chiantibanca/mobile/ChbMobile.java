package it.chiantibanca.mobile;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.util.Log;

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
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.*;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import android.webkit.WebSettings.ZoomDensity;

public class ChbMobile extends Activity 
					implements 	SaveCredentialsDialogFragment.NoticeDialogListener,
					 			ChkRootDialogFragment.NoticeDialogListener {
 
	WebView myMobileView;
	WebView myDesktopView;
	WebView myLoadView;
	WebView myCurrentView;
	
	static ViewSwitcher viewswitcher;
	static ViewFlipper webswitcher;
	
	public int loaded_page = -1;
	
	static View layout_load;
	static View layout_view;
	static View layout_view_mobile;
	static View layout_view_desktop;
	
    private Bundle LastLoginData;
    private Boolean ToValidateLoginData;
    
    SharedPreferences DefaultSharedPref;
	
    @SuppressLint("SetJavaScriptEnabled")
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
			 DialogFragment newFragment = new ChkRootDialogFragment();
			 newFragment.show(getFragmentManager(), "ChkRootDialog");
        }
        
        //Preferenze Iniziali
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        DefaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); 
        
        LastLoginData = null;
        ToValidateLoginData = false;
        
        //Progress bar
        //getWindow().setFeatureInt( Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
        //final Activity MyActivity = this;
        
        // WebView
	        //Impostazioni
	        myMobileView = (WebView) findViewById(R.id.webmobile);
	        WebSettings webSettings = myMobileView.getSettings();
	        	webSettings.setJavaScriptEnabled(true);
	        	webSettings.setUserAgentString("ChiantiBanca Mobile App");
	        	webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
	        	webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
	        	webSettings.setSupportMultipleWindows(true);
	        	webSettings.setSupportZoom(true);
	        	webSettings.setLoadWithOverviewMode(true);
	        	webSettings.setUseWideViewPort(true);
	        /*myDesktopView = (WebView) findViewById(R.id.webdesktop);
		        webSettings = myDesktopView.getSettings();
		        webSettings.setJavaScriptEnabled(true);
		        webSettings.setUserAgentString("ChiantiBanca Mobile App");
		        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		        webSettings.setSupportMultipleWindows(true);*/
		        
	        	//webSettings.set
	        myMobileView.setWebViewClient(new MyWebViewClient());
	        myMobileView.setWebChromeClient(new MyWebChromeClient());
	        myMobileView.addJavascriptInterface(new JavaScriptInterface(this), "Android");
	        /*myDesktopView.setWebViewClient(new MyWebViewClient());
	        myDesktopView.setWebChromeClient(new MyWebChromeClient());
	        myDesktopView.addJavascriptInterface(new JavaScriptInterface(this), "Android");*/

	        //Set Current
	        myCurrentView = myMobileView;
	        
	        //Carica pagina
            Boolean pref_usedesktop = DefaultSharedPref.getBoolean("pref_usedesktop", false);
            if (pref_usedesktop == true) {
            	myCurrentView.loadUrl("https://www.inbank.it/function/login/index.jsp?lang=it&abi=08673&css=08673");
            	loaded_page = 10;
            } else {
            	myCurrentView.loadUrl("https://www.inbank.it/mobi/flow?_flowId=AccessFlow&_flowExecutionKey=e1s1");
    	        loaded_page = 0;
            }
        
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
        
         viewswitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
         webswitcher = (ViewFlipper) findViewById(R.id.webview);
         
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
    	//TODO Se non posso andare indietro ma sono nella visualizzazione Desktop passa a visualizzazione Webview Mobile.
    	if (loaded_page == 0 || loaded_page == 10){
    	
    		finish();
	        
        } else if (myCurrentView == myDesktopView && loaded_page > 10) {
        	
        	//Switch View
		    	if(webswitcher.getCurrentView() != layout_view_mobile) {
		    		viewswitcher.showPrevious();   
		    	}
        	
        	//Close session
        	Log.i ("info","Terminating Desktop Session!");
	        	//Logout action
	        	myCurrentView.loadUrl("javascript:go_exit();");
	        	//Remove View
		        webswitcher.removeView(myDesktopView);
		        //Clean Vars
		        myDesktopView = null;
		        layout_view_desktop = null;
		    	loaded_page = 10;
		    	myCurrentView = myMobileView;
		    	
	    	
        } else if (myCurrentView == myMobileView && loaded_page > 0) {
        	
        	//Close session
        	Log.i ("info","Terminating Mobile Session!");
	        	//Logout action
	        	myCurrentView.loadUrl("https://www.inbank.it/mobi/flow?_flowId=LogoutFlow");
		        //Clean Vars
		    	loaded_page = 0;
	    		
	    } else if(myCurrentView.canGoBack()){ 
	    		
	    	myCurrentView.goBack();   
	    		
    	} else {
    		
    		finish();
        	
        }
    }
    
    /*
    Thread longThread = new Thread(TerminateDesktopSession);
    longThread.start();
        	
    private final  Runnable TerminateDesktopSession = new Runnable(){
        @Override
        public void run() {
        	Log.i ("info","Terminating Desktop Session!");
        	
        	//Logout action
        	myCurrentView.loadUrl("javascript:go_exit();");
        	
        	//Remove View
	        webswitcher.removeView(myDesktopView);
	        
	        //Clean Vars
	        myDesktopView = null;
	        layout_view_desktop = null;
	    	loaded_page = 10;
        }
    };
    
    private final  Runnable TerminateMobileSession = new Runnable(){
        @Override
        public void run() {
        	Log.i ("info","Terminating Mobile Session!");
        	
        	//Logout action
        	myCurrentView.loadUrl("https://www.inbank.it/mobi/flow?_flowId=LogoutFlow");
        	
	        //Clean Vars
	    	loaded_page = 0;
        }
    };
    */
    
    /*
    @Override
    public void onDestroy(){
    	if(loaded_page > 0){
    		Thread longThread = new Thread(longOperation);
    		longThread.start();
    	}
    	super.onDestroy();
    }
    */
    
    @Override
    public void onResume(){
    	DefaultSharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(DefaultSharedPref.getBoolean("pref_vcredentials_needreset", false) == true){
			SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
			sharedPrefeditor.putBoolean("pref_vcredentials_needreset", false);
			sharedPrefeditor.commit();
			
			//Imposta campo password come testo (o vice) alle pagine gia caricate
			if(DefaultSharedPref.getBoolean("pref_vcredentials", false) == true){
				myCurrentView.loadUrl("javascript:"+
		            	"	var elemDOM = document.getElementById('id_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('old_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
		            	"	var elemDOM = document.getElementById('confirm_new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'text'); }"+
		            	"");
			}else{
				myCurrentView.loadUrl("javascript:"+
		            	"	var elemDOM = document.getElementById('id_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
		            	"	var elemDOM = document.getElementById('old_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
		            	"	var elemDOM = document.getElementById('new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }" +
		            	"	var elemDOM = document.getElementById('confirm_new_password'); if(elemDOM){ elemDOM.setAttribute('type', 'password'); }"+
		            	"");
			}
		}
    	super.onResume();
    }
    
    // Handler Javascript 
    //  Acquisisce messaggi da interfaccia Javascript e gestisce la UI
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {

    	if (msg.getData().getString("action") == "js") {
         
    		String bodyId = msg.getData().getString("arg2", "");
    		String bodyClass = msg.getData().getString("arg3", "");
    		String Url = msg.getData().getString("arg4", "");
    		String successText = msg.getData().getString("arg5", "");
    		
        	//Indentifica la pagina caricata (per tasto back)
	        	if(bodyClass.contains("clearfix") && Url.contains("/function/login")){
	        		loaded_page = 0;
	        	} else if (bodyClass.contains("clearfix") && Url.contains("/mobi")){
	        		loaded_page = 10;
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
        	
	        //Validazione cambio password
        	if(	ToValidateLoginData == true && 
        		successText.contains("successo") && 
        		LastLoginData != null){
        		
        		 SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
        		 sharedPrefeditor.putString("cred_user", LastLoginData.getString("arg1"));
            	 sharedPrefeditor.putString("cred_pass", LastLoginData.getString("arg2"));
            	 sharedPrefeditor.commit();
        	}
        			
    		//Visualizza Webview
        	 if (msg.getData().getString("arg1") == "loadok" || msg.getData().getString("arg1") == "simloadok") {
    			 if (viewswitcher.getCurrentView() != layout_view)
    				 viewswitcher.showNext();
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
            	 DialogFragment newFragment = new SaveCredentialsDialogFragment();
            	 newFragment.show(getFragmentManager(), "SaveCredentialsDialog");
            	 
             } else if(pref_rcredentials == true) {
            	 
            	 LastLoginData = msg.getData();
            	 
                 //Salva Utente e password
            	 SharedPreferences.Editor sharedPrefeditor = DefaultSharedPref.edit();
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
    //  Intercetta eventi Js
    public class MyWebChromeClient extends WebChromeClient {
	    @Override
	    public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
	    	Log.i ("info","Chrome Client - OnCreateWindow:  " + resultMsg.obj.toString() );
	    	
	    	//webswitcher.removeAllViews();
	    	
	    	WebView desktopView = new WebView(getBaseContext());
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
        	
        	//Switch View
		    	if(webswitcher.getCurrentView() != layout_view_mobile) {
		    		webswitcher.showPrevious();   
		    	}
        	
        	//Clean session
	        	//Remove View
		        webswitcher.removeView(myDesktopView);
		        //Clean Vars
		        myDesktopView = null;
		        layout_view_desktop = null;
		    	loaded_page = 10;
		    	myCurrentView = myMobileView;
	    }
	    
	    @Override
	    public boolean onConsoleMessage (ConsoleMessage consoleMessage){
	    	Log.i ("info","Chrome Client - OnConsoleMessage:  " + consoleMessage.message().toString() + " ["+ consoleMessage.sourceId()+" at line: " +consoleMessage.lineNumber() +"]" );
	    	return true;
	    }
	    
    }
    
    // Componente Browser
    //  Visualizza le pagine web
    private class MyWebViewClient extends WebViewClient {
    	
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
        	Log.i ("info","WebView Client - ShouldOverride: " + url.toString());
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
        	Log.i ("info","WebView Client - PageStarted: " + url.toString());
        	if (viewswitcher.getCurrentView() != layout_load)
        		viewswitcher.showPrevious();   
        	
        }
        
        @Override
        public void onPageFinished(WebView view, String url) {
        	//Toast.makeText(getBaseContext(),url.toString() , Toast.LENGTH_SHORT).show();
	    	Log.i ("info","WebView Client - Finished: " + url.toString());
	    	/*if (loaded_page == 9) {	return;	}*/
	    	
	        //Get Preferences
	        boolean pref_usedesktop = DefaultSharedPref.getBoolean("pref_usedesktop", false);
	        boolean pref_dohistyles = DefaultSharedPref.getBoolean("pref_usedesktop", false);
	        
	        //Set Default DOM id
	        String field_user;
	        String field_pass;
	        String field_otppass;
	        String field_oldpass;
	        String field_newpass;
	        String field_confirmpass;
	        String form_newpass;
	        String iframe_center;
	        if (loaded_page >= 10) {
	        	//Desktop Version
	        	//	DOM are selected with Jquery support
	        	//	used JQuery selectors
	        	field_user = "#access-code";
	        	field_pass = "#password";
	        	iframe_center = "#centerFrame";
	        	field_otppass = "input[name=pass]"; 
	        	field_oldpass = "input[name=oldpassword]";
	        	field_newpass = "input[name=password]";
	        	field_confirmpass = "input[name=pwdconfirm]";
	        	form_newpass = "";
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
	        	form_newpass = "command";
	        }
        	
        	//Injecting Js - Init
	        	view.loadUrl("javascript:" +
		        			"var flow = [];"
	        	);
	        	
	        //Injecting Js - Mobile Personalizations
	        if (loaded_page < 10) {	
	        	
	        		 //Hi-Styles
		             if (pref_dohistyles == true || loaded_page == 0) {
		            	 
		 	        	view.loadUrl("javascript:" +
		 	    					//css 8673
		 		        			"var fun = function() {"+
		 	    					"	var headDOM = document.getElementsByTagName('head')[0];" +
		 	    					"	var bodyDOM = document.getElementsByTagName('body')[0];"+
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
		 				        	"													div[role=main]{background:none;padding-right:0;border:0}"+
		 				        	"													.news,.login{background:#fff;border:1px solid #dfdfdf;-webkit-border-radius:8px;-moz-border-radius:8px;border-radius:8px}"+
		 				        	"													.login{width:25.7%;min-height:333px;margin-top:0;margin-left:0;margin-right:0;padding:1em 2%}"+
		 				        	"													.ie7 .login{height:330px}"+
		 				        	"													.news{position:relative;width:66.666%}"+
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
			        			"			Android.ActionLogin($('#"+field_user+"').val(),$('#"+field_pass+"').val());" +
			        			"			return true;" +
			        			"		});" +
			        			"	" +
			        			"	}" +
		    					"	};" +
		    					"flow.push(fun);"
		        	);
	        }
	        	
	        	
	        //Injecting Js - UI Adjustment
	        	view.loadUrl("javascript:" +	
	        				
		        			//User & OTP pass as Number
		        			"var fun = function() {"+
		        			"	"+GetDOM(field_user)+" if(elemDOM){ elemDOM.setAttribute('type', 'number'); }" +
		        			"	"+GetDOM(field_user, null,iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'number'); }" +
		        			"	"+GetDOM(field_otppass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'number'); }" +
	    					"	};" +
	    					"flow.push(fun);"
	        	);        
	        	
	        //Injecting Js - Final fixed code
	        	view.loadUrl("javascript:" +	
	    					//avverti interfaccia
		        			"var fun = function() {" +
		        			"		" +
		        			"			var bodyDOM = document.getElementsByTagName('body')[0];" +
		        			"			var successText;" +
		        			"			var successDOM = document.getElementById('success'); " +
		        			"			if(successDOM){ successText = successDOM.textContent; } else { successText = ''; }"+ //textContent 
	    					"			setTimeout(Android.OkDone(bodyDOM.id,bodyDOM.className,document.URL,successText),300); " +
	    					"		" +
	    					"	};" +
	    					"flow.push(fun);" +
	    					
	    					//esegui codice
	    					"while (flow.length>0) { (flow.shift())(); }; "	
	        	);
	        	
	        //Injecting Js - Pref: Visible Credentials \ Visible Credentials 
	        	if(DefaultSharedPref.getBoolean("pref_vcredentials", false) == true) {
	            	//Imposta campo password come testo
	            	view.loadUrl("javascript:"+
	            			"	"+GetDOM(field_pass)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_pass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_otppass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_oldpass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_newpass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	            			"	"+GetDOM(field_confirmpass, null, iframe_center)+" if(elemDOM){ elemDOM.setAttribute('type', 'text'); }" +
	    					"");
	        	}
	        	
	        //Injecting Js - Pref: Save Credentials \ Save Used Credentials 
	            if(DefaultSharedPref.getBoolean("pref_rcredentials", false) == true &&
	            		(DefaultSharedPref.getString("cred_user", null)!=null)	 ) {
	            	
	            	//Write Saved Credentials
	            	view.loadUrl("javascript: " +
		            	"		"+GetDOM(field_user)+" if(elemDOM){ elemDOM.value = '"+DefaultSharedPref.getString("cred_user", null)+"');" +
		            	"		"+GetDOM(field_pass)+" if(elemDOM){ elemDOM.value = '"+DefaultSharedPref.getString("cred_pass", null)+"');" +
		            	"");
	            	
	            	//Write Saved Credentials (Internal pages)
		            view.loadUrl("javascript:" +
		            	"		"+GetDOM(field_oldpass, null, iframe_center)+" if(elemDOM){ elemDOM.value = '"+DefaultSharedPref.getString("cred_pass", null)+"'; };" +
		            	"");
	            	 
	 	            //Hijack password change
	 	            view.loadUrl("javascript:" +
		      			"		var submitnewpass = function() {										" +
		      			"			"+GetDOM(field_newpass,"elemDOMp", iframe_center)+"								" +
		      			"			"+GetDOM(field_confirmpass,"elemDOMnewp", iframe_center)+"							" +
		      			"			if(elemDOM){														" +
		      			"				if(elemDOMnewp){												" +
		      			"					if(elemDOMp.value == elemDOMnewp.value){					" +
		      			"					Android.ActionLogin('refresh',elemDOMnewp.value);			" +
		      			"					}															" +
		      			"				}																" +
		      			"			}																	" +
		 				"		};																		" +
		 				"		"+GetDOM(form_newpass, null, iframe_center)+"												" +
		 				"		elemDOM.setAttribute('onsubmit', 'submitnewpass();');					" +
			            "");
	            	 
	             }
	             
	        	
        }
        
        //Funzioni di Supporto
        private String GetDOM(String field, String param, String iframe){
        	String ret = "";
        	if (loaded_page < 10) {	
        		//Mobile
        		ret = " var "+param+" = document.getElementById('"+field+"'); ";
        	}else{
        		//Desktop
        		if (iframe.equals("")){
        			ret = " var "+param+" = $('"+field+"').get(0);";
        		}else{
        			ret = " var "+param+" = $('"+iframe+"').contents().find('"+field+"').get(0);";
        		}
        	}
        	return ret;
        }
        private String GetDOM(String field, String param){
        	return GetDOM(field, param, "");
        }
        private String GetDOM(String field){
        	return GetDOM(field, "elemDOM", "");
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
	                         mListener.onSaveCredentialsDialogNegativeClick(SaveCredentialsDialogFragment.this);
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
