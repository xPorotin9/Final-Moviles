package com.example.finalmoviles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit

/**
 * MainActivity is the entry point of the application.
 * It sets the content view and initializes the main menu fragment if there is no saved instance state.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. This is where most initialization should go.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Only add the initial fragment if there is no saved instance state
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, MainMenuFragment.newInstance())
            }
        }
    }
}