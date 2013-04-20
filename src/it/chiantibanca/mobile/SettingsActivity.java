package it.chiantibanca.mobile;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity 
										 {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        CheckBoxPreference ChkBoxPreference = (CheckBoxPreference) findPreference("pref_rcredentials");
        
        ChkBoxPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean) newValue == false) {
               	 	// Set summary to be the user-description for the selected value
                    preference.setSummary(R.string.pref_rcredentials_summary_dis);
                    
                    //cancella dati salvati
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor sharedPrefeditor = sharedPref.edit();
                    sharedPrefeditor.putString("cred_user", null);
                    sharedPrefeditor.putString("cred_pass", null);
                    sharedPrefeditor.commit();
                } else {
               	 	// Set summary to be the user-description for the selected value
                    preference.setSummary(R.string.pref_rcredentials_summary);
                }
                return true;
            }
        });
        
    }
    
    /*public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("pref_rcredentials")) {
            Boolean pref_rcredentials_choice = sharedPreferences.getBoolean("pref_rcredentials", false);
            if (pref_rcredentials_choice == false) {
           	 	// Set summary to be the user-description for the selected value
                Preference thePref = findPreference("pref_rcredentials");
                thePref.setSummary(sharedPreferences.getString("pref_rcredentials", "Nessun dato salvato."));
                
                //cancella dati salvati
                SharedPreferences.Editor sharedPrefeditor = sharedPreferences.edit();
                sharedPrefeditor.putString("cred_user", null);
                sharedPrefeditor.putString("cred_pass", null);
                sharedPrefeditor.commit();
            }
            
        }
    }*/
    
}


