package com.example.finalmoviles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit

class MainMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.btnPlay).setOnClickListener {
            parentFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragmentContainer, GameFragment())
                addToBackStack(null)
            }
        }

        view.findViewById<Button>(R.id.btnHighScores).setOnClickListener {
        }
    }

    companion object {
        fun newInstance() = MainMenuFragment()
    }
}