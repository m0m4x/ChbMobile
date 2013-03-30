package it.chiantibanca.mobile;

import it.chiantibanca.mobile.R.string;

import java.io.IOException;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.webkit.*;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class ChbMobile extends Activity {

	
	WebView myWebView;
	WebView myLoadView;
	static ViewSwitcher switcher;
	
	static View layout1;
	static View layout2;
	
	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chb_mobile);
        
        layout1=findViewById(R.id.layoutload);
        layout2=findViewById(R.id.layoutview);
        
        // WebView
	        //Impostazioni
	        myWebView = (WebView) findViewById(R.id.webview);
	        WebSettings webSettings = myWebView.getSettings();
	        	webSettings.setJavaScriptEnabled(true);
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
    
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chb_mobile, menu);
        return true;
    }
    */
    
    
    // Handler Javascript 
    //  Acquisisce messaggi da interfaccia Javascript e gestisce la UI
    @SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
     @Override
     public void handleMessage(Message msg) {
    	 

    	if (msg.getData().getString("action") == "js") {
         
        	 if (msg.getData().getString("arg1") == "loadok" || msg.getData().getString("arg1") == "simloadok") {
    			 if (switcher.getCurrentView() != layout2)
    				 switcher.showNext();
        	 }
        	 
    	} else if (msg.getData().getString("action") == "say")  {
        	 
    		 Toast toast = Toast.makeText(getBaseContext(), msg.getData().getString("arg1"), Toast.LENGTH_SHORT);
    		 toast.show();
    		 
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

        public void OkDone() {
        	
        	Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("action", "js");
            b.putString("arg1", "loadok");
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
        	
        	Message msg = new Message();
            Bundle b = new Bundle();
            b.putString("action", "login");
            b.putString("arg1", user);
            b.putString("arg2", pass);
            msg.setData(b);
            
            // send message to the handler with the current message handler
            handler.sendMessage(msg);
        	
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
        	
        	//Personalizzazione
        	
	        	//css 8673
	        	view.loadUrl("javascript:"+
    					"var headDOM = document.getElementsByTagName('head')[0];"+
    					"var bodyDOM = document.getElementsByTagName('body')[0];"+
    					"var pers = document.createElement('link');"+
    					"pers.rel='stylesheet';"+
    					"pers.href='/static/common/css/custom/style08673.css?ver=2.22.0.0 FIX2';"+
    					"headDOM.appendChild(pers);"+
    					//"bodyDOM.className=bodyDOM.className.replace(/mobile/g,'classic');"
    					"bodyDOM.className='classic '+bodyDOM.className;"
	        	);
	    	
	        	//form login
	        	view.loadUrl("javascript:"+
	        			"var elemDOM = document.getElementById('frmlogin'); " +
	        			"if(elemDOM){ " +
	        			"	var diva = document.createElement('h1');"+
	        			"	diva.style.cssText='text-align:center;';"+
	        			"	diva.innerHTML = '" +
	        			"						<img alt=\"Logo\" src=\"/static/common/img/loghi/logo08673.jpg?ver=2.22.0.0 FIX2\" width=\"196\" height=\"87\" style=\"margin:auto;\">" +
	        			"					  ';"+
	        			"	elemDOM.parentNode.insertBefore(diva,document.getElementById('frmlogin'));"+
	        			"	elemDOM.parentNode.removeChild(document.getElementById('frmlogin').parentNode.firstChild); "+
	        			"	elemDOM.parentNode.removeChild(document.getElementById('frmlogin').parentNode.firstChild); "+
	        			"	elemDOM.removeChild(document.getElementById('frmlogin').lastChild); "+
	        			"	elemDOM.removeChild(document.getElementById('frmlogin').lastChild); " +
	        			"" +
	        			//"	$('#frmlogin').submit(function(){" +
	        			//"		Android.SayThis(\"jQuery Works!\");" +
	        			//"		return true;" +
	        			//"	});" +
	        			"" +
	        			"" +
	        			"}");
	        	
	        	
	        	//campo utente come numero
	        	view.loadUrl("javascript:"+		
	        			"var elemDOM = document.getElementById('id_user'); if(elemDOM){ elemDOM.setAttribute('type', 'number'); }"
	        			);
	        	
	        	
	        	//rimuovi elementi
	        	view.loadUrl("javascript:"+		
	        			"var elemDOM = document.getElementById('mm1'); if(elemDOM){ elemDOM.parentNode.removeChild(document.getElementById('mm1')); }"+
	        			"var elemDOM = document.getElementById('mobileMenu_mm1'); if(elemDOM){ elemDOM.parentNode.removeChild(document.getElementById('mobileMenu_mm1')); }"+
	        			"var elemDOM = document.getElementById('menu'); if(elemDOM){ elemDOM.parentNode.removeChild(elemDOM); }" +
	        			"var elemDOM = document.getElementsByClassName('menu-link')[0]; if(elemDOM){ elemDOM.parentNode.removeChild(elemDOM); }"
	        			);
	        	
	        	
	        	//avverti interfaccia
	        	view.loadUrl("javascript:"+		
						"setTimeout(function() {Android.OkDone();},250);"
	        			);
	        	
        }
        
    }
    
}
